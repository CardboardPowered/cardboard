package org.cardboardpowered.mixin.block;

import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import org.cardboardpowered.extras.BukkitChestDoubleInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(targets = "net.minecraft.block.ChestBlock$2")
public class MixinChestBlockv2 {
    @Inject(method = "getFromBoth(Lnet/minecraft/block/entity/ChestBlockEntity;Lnet/minecraft/block/entity/ChestBlockEntity;)Ljava/util/Optional;", at = @At("RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void bukkitCustomInventory(ChestBlockEntity chestBlockEntity, ChestBlockEntity chestBlockEntity2, CallbackInfoReturnable<Optional<NamedScreenHandlerFactory>> cir, Inventory inventory) {
        cir.setReturnValue(Optional
                .of(new BukkitChestDoubleInventory(chestBlockEntity, chestBlockEntity2,
                        (net.minecraft.inventory.DoubleInventory) inventory)));
    }
}
