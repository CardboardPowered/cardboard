package org.cardboardpowered.mixin.world.entity;

import java.util.Map;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fires Paper's PlayerArmorChangeEvent whenever a player's armour slots change.
 */
@Mixin(LivingEntity.class)
@MixinInfo(events = {"PlayerArmorChangeEvent"})
public class LivingEntityMixin_ArmorChangeEvent {

    @Shadow @Final private Map<EquipmentSlot, ItemStack> lastEquipmentItems;

    // HEAD: lastEquipmentItems still holds the previous items, they are overwritten further down.
    @Inject(method = "handleEquipmentChanges", at = @At("HEAD"))
    private void cardboard$armorChangeEvent(Map<EquipmentSlot, ItemStack> changes, CallbackInfo ci) {
        if (!((Object) this instanceof ServerPlayer player)) return;

        for (Map.Entry<EquipmentSlot, ItemStack> change : changes.entrySet()) {
            EquipmentSlot slot = change.getKey();
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;

            ItemStack old = this.lastEquipmentItems.get(slot);

            new PlayerArmorChangeEvent(
                    (org.bukkit.entity.Player) ((EntityBridge) player).getBukkitEntity(),
                    PlayerArmorChangeEvent.SlotType.valueOf(slot.name()),
                    CraftItemStack.asBukkitCopy(old == null ? ItemStack.EMPTY : old),
                    CraftItemStack.asBukkitCopy(change.getValue())
            ).callEvent();
        }
    }
}
