package org.cardboardpowered.mixin.network;

import java.util.UUID;

import org.cardboardpowered.interfaces.IGameMessagePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.network.MessageType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;

@Mixin(GameMessageS2CPacket.class)
public class MixinGameMessageS2CPacket implements IGameMessagePacket {

    @Shadow private Text message;
    @Shadow private UUID sender;

    public net.md_5.bungee.api.chat.BaseComponent[] bungeeComponents;

    @Override
    public BaseComponent[] getBungeeComponents() {
        return bungeeComponents;
    }

    @Inject(at = @At("HEAD"), method = "write", cancellable = true)
    public void writePacket(PacketByteBuf buf, CallbackInfo ci) {
        if (bungeeComponents != null) {
            buf.writeString(net.md_5.bungee.chat.ComponentSerializer.toString(bungeeComponents));
            buf.writeByte(MessageType.CHAT.getId());
            buf.writeUuid(this.sender);
            ci.cancel();
        }
    }

    @Override
    public void setBungeeComponents(BaseComponent[] components) {
        this.bungeeComponents = components;
    }


}