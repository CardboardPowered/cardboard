package com.javazilla.bukkitfabric.mixin;

import org.bukkit.Material;
import org.cardboardpowered.impl.CardboardModdedMaterial;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.javazilla.bukkitfabric.interfaces.IMixinMaterial;

@Mixin(value = Material.class, remap = false)
public class MixinMaterial implements IMixinMaterial {

    private CardboardModdedMaterial moddedData;

    @Override
    public boolean isModded() {
        return null != moddedData;
    }

    @Override
    public CardboardModdedMaterial getModdedData() {
        return moddedData;
    }

    @Override
    public void setModdedData(CardboardModdedMaterial data) {
        this.moddedData = data;
    }

    @Inject(at = @At("HEAD"), method = "isBlock", cancellable = true)
    public void isBlock_BF(CallbackInfoReturnable<Boolean> ci) {
        if (isModded()) {
            ci.setReturnValue(moddedData.isBlock());
            return;
        }
    }

    @Inject(at = @At("HEAD"), method = "isBlock", cancellable = true)
    public void isItem_BF(CallbackInfoReturnable<Boolean> ci) {
        if (isModded()) {
            ci.setReturnValue(moddedData.isItem());
            return;
        }
    }

    @Inject(at = @At("HEAD"), method = "isEdible", cancellable = true)
    public void isEdible_BF(CallbackInfoReturnable<Boolean> ci) {
        if (isModded()) {
            ci.setReturnValue(moddedData.isEdible());
            return;
        }
    }

    @Inject(at = @At("HEAD"), method = "isRecord", cancellable = true)
    public void isRecord_BF(CallbackInfoReturnable<Boolean> ci) {
        if (isModded()) {
            ci.setReturnValue(false);
            return;
        }
    }

    @Inject(at = @At("HEAD"), method = "isSolid", cancellable = true)
    public void isSolid_BF(CallbackInfoReturnable<Boolean> ci) {
        if (isModded()) {
            ci.setReturnValue(moddedData.isBlock());
            return;
        }
    }

    @Inject(at = @At("HEAD"), method = "isAir", cancellable = true)
    public void isAir_BF(CallbackInfoReturnable<Boolean> ci) {
        if (isModded()) {
            ci.setReturnValue(false);
            return;
        }
    }

    @Inject(at = @At("HEAD"), method = "isTransparent", cancellable = true)
    public void isTransparent_BF(CallbackInfoReturnable<Boolean> ci) {
        if (isModded()) {
            ci.setReturnValue(false);
            return;
        }
    }

    @Inject(at = @At("HEAD"), method = "isBurnable", cancellable = true)
    public void isBurnable_BF(CallbackInfoReturnable<Boolean> ci) {
        if (isModded()) {
            ci.setReturnValue(false);
            return;
        }
    }

    @Inject(at = @At("HEAD"), method = "isOccluding", cancellable = true)
    public void isOccluding_BF(CallbackInfoReturnable<Boolean> ci) {
        if (isModded()) {
            ci.setReturnValue(moddedData.isBlock());
            return;
        }
    }

    @Inject(at = @At("HEAD"), method = "hasGravity", cancellable = true)
    public void hasGravity_BF(CallbackInfoReturnable<Boolean> ci) {
        if (isModded()) {
            ci.setReturnValue(false);
            return;
        }
    }

    @Inject(at = @At("HEAD"), method = "isInteractable", cancellable = true)
    public void isInteractable_BF(CallbackInfoReturnable<Boolean> ci) {
        if (isModded()) {
            ci.setReturnValue(false);
            return;
        }
    }

    @Inject(at = @At("HEAD"), method = "getHardness", cancellable = true)
    public void getHardness_BF(CallbackInfoReturnable<Float> ci) {
        if (isModded()) {
            ci.setReturnValue(1f);
            return;
        }
    }

    @Inject(at = @At("HEAD"), method = "getCraftingRemainingItem", cancellable = true)
    public void getCraftingRemainingItem_BF(CallbackInfoReturnable<Material> ci) {
        if (isModded()) {
            ci.setReturnValue(null);
            return;
        }
    }


}