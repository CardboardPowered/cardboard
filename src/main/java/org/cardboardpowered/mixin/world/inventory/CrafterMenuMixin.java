package org.cardboardpowered.mixin.world.inventory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CrafterMenu;
import org.bukkit.craftbukkit.inventory.CraftInventoryCrafter;
import org.bukkit.craftbukkit.inventory.view.CraftCrafterView;
import org.bukkit.entity.HumanEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.cardboardpowered.bridge.world.entity.EntityBridge;

@Mixin(CrafterMenu.class)
public class CrafterMenuMixin extends AbstractContainerMenuMixin {

    @Shadow
    @Final
    private CraftingContainer container;

    @Shadow
    @Final
    private Player player;

    private CraftCrafterView bukkitEntity;

    @Override
    public CraftCrafterView getBukkitView() {
        if (bukkitEntity != null)
            return bukkitEntity;

        bukkitEntity = new CraftCrafterView((HumanEntity) ((EntityBridge) this.player).getBukkitEntity(),
                new CraftInventoryCrafter(this.container), (CrafterMenu) (Object) this);
        return bukkitEntity;
    }

    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    public void stillValidCraftBukkit(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!this.checkReachable) cir.setReturnValue(true); // CraftBukkit
    }
}
