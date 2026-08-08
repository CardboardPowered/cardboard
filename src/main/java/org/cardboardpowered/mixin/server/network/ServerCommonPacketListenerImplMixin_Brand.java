package org.cardboardpowered.mixin.server.network;

import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.cardboardpowered.bridge.server.network.ClientBrandBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 26.2: capture the client brand from the vanilla minecraft:brand payload,
// which is what Paper exposes as PlayerCommonConnection#getClientBrandName.
@Mixin(ServerCommonPacketListenerImpl.class)
public class ServerCommonPacketListenerImplMixin_Brand implements ClientBrandBridge {

    @Unique
    private String cardboard$clientBrand;

    @Inject(method = "handleCustomPayload", at = @At("HEAD"))
    private void cardboard$captureBrand(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        if (packet.payload() instanceof BrandPayload brandPayload) {
            this.cardboard$clientBrand = brandPayload.brand();
        }
    }

    @Override
    public String cardboard_getClientBrand() {
        return this.cardboard$clientBrand;
    }

    @Override
    public void cardboard_setClientBrand(String brand) {
        this.cardboard$clientBrand = brand;
    }
}
