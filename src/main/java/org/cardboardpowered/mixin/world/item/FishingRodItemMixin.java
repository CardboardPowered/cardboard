package org.cardboardpowered.mixin.world.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerFishEvent;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.cardboardpowered.bridge.world.entity.EntityBridge;

@MixinInfo(events = {"PlayerFishEvent"})
@Mixin(FishingRodItem.class)
public class FishingRodItemMixin {

    @Inject(at = @At("HEAD"), method = "use", cancellable = true)
    public void cardboard$fishingRodUse_PlayerFishEvent(Level world, Player entityhuman, InteractionHand enumhand, CallbackInfoReturnable<InteractionResult> ci) {
        // Leave the client's prediction to vanilla; the server copy spawns the hook and fires PlayerFishEvent.
        if (world.isClientSide()) return;
        if (null == entityhuman.fishing) {
            ItemStack itemstack = entityhuman.getItemInHand(enumhand);

            int i = (int)(EnchantmentHelper.getFishingTimeReduction((ServerLevel) world, itemstack, entityhuman) * 20.0f);
            int j = EnchantmentHelper.getFishingLuckBonus((ServerLevel) world, itemstack, entityhuman);
            
            FishingHook entityfishinghook = new FishingHook(entityhuman, world, j, i);
            PlayerFishEvent playerFishEvent = new PlayerFishEvent((org.bukkit.entity.Player) ((EntityBridge)entityhuman).getBukkitEntity(), null, (org.bukkit.entity.FishHook) ((EntityBridge)entityfishinghook).getBukkitEntity(), PlayerFishEvent.State.FISHING);
            Bukkit.getPluginManager().callEvent(playerFishEvent);
    
            if (playerFishEvent.isCancelled()) {
                entityhuman.fishing = null;
                ci.setReturnValue( InteractionResult.PASS );
                return;
            }
            world.addFreshEntity(entityfishinghook); 
            ci.setReturnValue( InteractionResult.SUCCESS );
            return;
        }
    }

}