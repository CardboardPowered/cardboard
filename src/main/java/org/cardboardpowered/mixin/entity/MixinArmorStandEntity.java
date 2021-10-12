package org.cardboardpowered.mixin.entity;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.javazilla.bukkitfabric.interfaces.IMixinArmorStandEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;

import net.minecraft.entity.MovementType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

@Mixin(ArmorStandEntity.class)
public class MixinArmorStandEntity extends MixinEntity implements IMixinArmorStandEntity {

    public boolean canMove = true; // Paper

    @Override
    public void setHideBasePlateBF(boolean b) {
        setHideBasePlate(b);
    }

    @Override
    public void setShowArmsBF(boolean arms) {
        setShowArms(arms);
    }

    @Override
    public void setSmallBF(boolean small) {
        setSmall(small);
    }

    @Override
    public void setMarkerBF(boolean marker) {
        setMarker(marker);
    }

    @Override
    public boolean canMoveBF() {
        return canMove;
    }

    @Override
    public void setCanMoveBF(boolean b) {
        this.canMove = b;
    }

    @Shadow public void setHideBasePlate(boolean flag) {}
    @Shadow public void setMarker(boolean flag) {}
    @Shadow public void setShowArms(boolean flag) {}
    @Shadow public void setSmall(boolean flag) {}

    // Paper start
    @Override
    public void move(MovementType moveType, Vec3d vec3d) {
        if (this.canMove) super.move(moveType, vec3d);
    }
    // Paper end

    /**
     * PlayerArmorStandManipulateEvent
     * 
     * @author Arclight
     * @author Cardboard
     */
    @Inject(method = "equip", cancellable = true,at = @At(value = "INVOKE", target =
            "Lnet/minecraft/entity/player/PlayerEntity;getAbilities()Lnet/minecraft/entity/player/PlayerAbilities;"))
    public void doBukkitEvent_PlayerArmorStandManipulateEvent(PlayerEntity playerEntity, net.minecraft.entity.EquipmentSlot slotType, ItemStack itemStack,
            Hand hand, CallbackInfoReturnable<Boolean> cir) {
        ItemStack itemStack1 = ((ArmorStandEntity)(Object)this).getEquippedStack(slotType);

        org.bukkit.inventory.ItemStack armorStandItem = CraftItemStack.asCraftMirror(itemStack1);
        org.bukkit.inventory.ItemStack playerHeldItem = CraftItemStack.asCraftMirror(itemStack);

        Player player = (Player) ((IMixinEntity) playerEntity).getBukkitEntity();
        ArmorStand self = (ArmorStand) ((IMixinEntity) this).getBukkitEntity();

        EquipmentSlot slot = com.javazilla.bukkitfabric.Utils.getSlot(slotType);
        PlayerArmorStandManipulateEvent event = new PlayerArmorStandManipulateEvent(player, self, playerHeldItem, armorStandItem, slot);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            cir.setReturnValue(true);
        }
    }


}