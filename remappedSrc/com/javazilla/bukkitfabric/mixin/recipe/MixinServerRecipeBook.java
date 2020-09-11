package com.javazilla.bukkitfabric.mixin.recipe;

import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.packet.s2c.play.UnlockRecipesS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerRecipeBook;
import net.minecraft.util.Identifier;

@Mixin(ServerRecipeBook.class)
public class MixinServerRecipeBook {

    @Inject(at = @At("HEAD"), method = "sendUnlockRecipesPacket", cancellable = true)
    private void dontSendPacketBeforeLogin(UnlockRecipesS2CPacket.Action packetplayoutrecipes_action, ServerPlayerEntity entityplayer, List<Identifier> list, CallbackInfo ci) {
        // See SPIGOT-4478
        if (entityplayer.networkHandler == null)
            ci.cancel();
    }

}