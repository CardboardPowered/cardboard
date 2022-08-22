package org.cardboardpowered.mixin.entity;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.cardboardpowered.impl.CardboardAttributable;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinLivingEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
public class MixinLivingEntity extends MixinEntity implements IMixinLivingEntity {

    private transient EntityPotionEffectEvent.Cause bukkitCause;
    private LivingEntity get() {
        return (LivingEntity)(Object)this;
    }

    @Shadow
    private AttributeContainer attributes;

    private boolean PICE_canceled = false;
    private CardboardAttributable craftAttributes;

    @Override
    public CardboardAttributable cardboard_getAttr() {
        if (null == craftAttributes) {
            this.craftAttributes = new CardboardAttributable(this.attributes);
        }
        return craftAttributes;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;finishUsing(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;"), 
            method = "consumeItem")
    public ItemStack doBukkitEvent_PlayerItemConsumeEvent(ItemStack s, World w, LivingEntity e) {
        PICE_canceled = false;
        if (get() instanceof ServerPlayerEntity) {
            org.bukkit.inventory.ItemStack craftItem = CraftItemStack.asBukkitCopy(get().activeItemStack);
            PlayerItemConsumeEvent event = new PlayerItemConsumeEvent((Player) ((IMixinServerEntityPlayer)((ServerPlayerEntity) get())).getBukkitEntity(), craftItem);
            Bukkit.getServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                ((Player)((IMixinServerEntityPlayer)((ServerPlayerEntity) get())).getBukkitEntity()).updateInventory();
                ((PlayerImpl)((IMixinServerEntityPlayer)((ServerPlayerEntity) get())).getBukkitEntity()).updateScaledHealth();
                PICE_canceled = true;
                return null;
            }
            return (craftItem.equals(event.getItem())) ? get().activeItemStack.finishUsing(get().world, get()) : CraftItemStack.asNMSCopy(event.getItem()).finishUsing(get().world, get());
        } else return get().activeItemStack.finishUsing(get().world, get());
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setStackInHand(Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;)V"),
            method = "consumeItem", cancellable = true)
    public void doBukkitEvent_PlayerItemConsumeEvent_FixCancel(CallbackInfo ci) {
        if (PICE_canceled) {
            ci.cancel();
            return;
        }
    }

    @Inject(at = @At("HEAD"), method = "drop", cancellable = true)
    public void cardboard_doDrop(DamageSource damagesource, CallbackInfo ci) {
        Entity entity = damagesource.getAttacker();

        boolean flag = get().playerHitTimer > 0;
        get().dropInventory();
        if (!get().isBaby() && this.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
            this.dropLoot(damagesource, flag);
            this.dropEquipment(damagesource, ((entity instanceof PlayerEntity) ? EnchantmentHelper.getLooting((LivingEntity) entity) : 0), flag);
        }

        BukkitEventFactory.callEntityDeathEvent(get(), ((IMixinEntity)this).cardboard_getDrops());
        ((IMixinEntity)this).cardboard_setDrops(new ArrayList<>());
        get().dropXp();
        ci.cancel();
        return;
    }

    // TODO: Testing
    @Shadow
    public int getXpToDrop(PlayerEntity entityhuman) {
        return 0;
    }

    @Override
    public int getExpReward() {
        if ((get().shouldAlwaysDropXp() || get().lastDamageTime > 0 && get().shouldAlwaysDropXp() && this.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT))) {
            int i = getXpToDrop(get().attackingPlayer);
            return i;
        } else return 0;
    }

    @Override
    public void pushEffectCause(EntityPotionEffectEvent.Cause cause) {
        bukkitCause = cause;
    }

    @Shadow
    public void dropLoot(DamageSource damagesource, boolean flag) {
    }

    @Shadow
    public void dropEquipment(DamageSource damagesource, int i, boolean flag) {
    }

    /**
     * @reason Bukkit RegainHealthEvent
     */
    @Inject(at = @At("HEAD"), method = "heal", cancellable = true)
    public void doRegainHealthEvent(float f, CallbackInfo ci) {
        heal(f, EntityRegainHealthEvent.RegainReason.CUSTOM);
        ci.cancel();
        return;
    }

    public void heal(float f, EntityRegainHealthEvent.RegainReason regainReason) {
        if (get().getHealth() > 0.0F) {
            EntityRegainHealthEvent event = new EntityRegainHealthEvent(this.getBukkitEntity(), f, regainReason);
            if (this.isValidBF()) {
                Bukkit.getPluginManager().callEvent(event);
                if (!event.isCancelled()) get().setHealth((float) (get().getHealth() + event.getAmount()));
            }
        }
    }

}