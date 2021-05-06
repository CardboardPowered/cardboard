package org.cardboardpowered.mixin.entity.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

@Mixin(BannerBlockEntity.class)
public class MixinBannerBlockEntity {

    @Shadow
    public NbtList patternListTag;

    @Inject(at = @At("TAIL"), method = "readNbt")
    public void bukkit_readNbt(NbtCompound nbttagcompound, CallbackInfo ci) {
        // Bukkit - TitleEntityBanner.patch
        while (this.patternListTag.size() > 20)
            this.patternListTag.remove(20);
    }

}