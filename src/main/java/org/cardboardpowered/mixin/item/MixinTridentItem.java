package org.cardboardpowered.mixin.item;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.world.World;

@MixinInfo(events = {"PlayerRiptideEvent"})
@Mixin(TridentItem.class)
public class MixinTridentItem {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;incrementStat(Lnet/minecraft/stat/Stat;)V"), method = "Lnet/minecraft/item/TridentItem;onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V")
    public void doBukkitEvent_PlayerRiptideEvent(ItemStack itemstack, World world, LivingEntity entity, int i, CallbackInfo ci) {
        int k = EnchantmentHelper.getRiptide(itemstack);
        if (k > 0) {
            PlayerRiptideEvent event = new PlayerRiptideEvent((Player)((IMixinEntity)entity).getBukkitEntity(), CraftItemStack.asCraftMirror(itemstack));
            event.getPlayer().getServer().getPluginManager().callEvent(event);
        }
    }

}