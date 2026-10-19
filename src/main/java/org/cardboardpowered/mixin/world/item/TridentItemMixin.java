package org.cardboardpowered.mixin.world.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.cardboardpowered.bridge.world.entity.EntityBridge;

@MixinInfo(events = {"PlayerRiptideEvent"})
@Mixin(TridentItem.class)
public class TridentItemMixin {

    @Inject(at =
    		@At(
    				value = "INVOKE",
    				target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"
    		),
    		method =
    		"releaseUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)Z")
    public void doBukkitEvent_PlayerRiptideEvent(ItemStack itemstack, Level world, LivingEntity entity, int i, CallbackInfoReturnable<Boolean> ci) {
        // The riptide branch also runs as client prediction with a LocalPlayer
        if (world.isClientSide()) return;
        float k = EnchantmentHelper.getTridentSpinAttackStrength(itemstack, entity);
        if (k > 0.0f) {
            PlayerRiptideEvent event = new PlayerRiptideEvent((Player)((EntityBridge)entity).getBukkitEntity(), CraftItemStack.asCraftMirror(itemstack));
            event.getPlayer().getServer().getPluginManager().callEvent(event);
        }
    }

}