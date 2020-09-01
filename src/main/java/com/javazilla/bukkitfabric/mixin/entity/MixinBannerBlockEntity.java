package com.javazilla.bukkitfabric.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

@Mixin(BannerBlockEntity.class)
public class MixinBannerBlockEntity {

    @Shadow
    public ListTag patternListTag;

    @Inject(at = @At("TAIL"), method = "fromTag")
    public void fromTag(BlockState iblockdata, CompoundTag nbttagcompound, CallbackInfo ci) {
        // Bukkit - TitleEntityBanner.patch
        while (this.patternListTag.size() > 20)
            this.patternListTag.remove(20);
    }

}