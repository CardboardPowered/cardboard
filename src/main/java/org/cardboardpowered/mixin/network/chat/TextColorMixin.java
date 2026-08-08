package org.cardboardpowered.mixin.network.chat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import org.cardboardpowered.bridge.network.chat.TextColorBridge;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextColor.class)
public class TextColorMixin implements TextColorBridge {
    @Nullable
    @Unique
    public ChatFormatting format;

    @Inject(method = "<init>(ILjava/lang/String;)V", at = @At("RETURN"))
    private void initFormat(int value, String name, CallbackInfo ci) {
        if (name != null) {
            // 26.2: ChatFormatting.getByName was removed
            this.format = java.util.Arrays.stream(ChatFormatting.values())
                    .filter(f -> f.name().equalsIgnoreCase(name)).findFirst().orElse(null);
        } else {
            this.format = null;
        }
    }

    @Inject(method = "<init>(I)V", at = @At("RETURN"))
    private void initFormat(int value, CallbackInfo ci) {
        this.format = null;
    }

    @Override
    public @Nullable ChatFormatting cardboard$getFormat() {
        return format;
    }
}