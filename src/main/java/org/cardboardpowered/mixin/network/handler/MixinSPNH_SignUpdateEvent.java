package org.cardboardpowered.mixin.network.handler;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.cardboardpowered.impl.block.CardboardSign;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinMinecraftServer;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.javazilla.bukkitfabric.interfaces.IMixinSignBlockEntity;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.util.Formatting;

@MixinInfo(events = {"SignChangeEvent"})
@Mixin(value = ServerPlayNetworkHandler.class, priority = 800)
public class MixinSPNH_SignUpdateEvent {

    @Shadow 
    public ServerPlayerEntity player;

    @SuppressWarnings("deprecation")
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/c2s/play/UpdateSignC2SPacket;getText()[Ljava/lang/String;"), method = "onUpdateSign", cancellable = true)
    public void fireSignUpdateEvent(UpdateSignC2SPacket packet, CallbackInfo ci) {
        try {
            String[] astring = packet.getText();
    
            Player player = (Player) ((IMixinServerEntityPlayer)this.player).getBukkitEntity();
            int x = packet.getPos().getX();
            int y = packet.getPos().getY();
            int z = packet.getPos().getZ();
            String[] lines = new String[4];
    
            for (int i = 0; i < astring.length; ++i)
                lines[i] = Formatting.strip(new LiteralTextContent (Formatting.strip(astring[i])).toString());
    
            ((IMixinMinecraftServer)CraftServer.server).cardboard_runOnMainThread(() -> {
                try {
                    SignChangeEvent event = new SignChangeEvent((org.bukkit.craftbukkit.block.CraftBlock) player.getWorld().getBlockAt(x, y, z), player, lines);
                    CraftServer.INSTANCE.getPluginManager().callEvent(event);
            
                    if (!event.isCancelled()) {
                        BlockEntity tileentity = this.player.getWorld().getBlockEntity(packet.getPos());
                        SignBlockEntity tileentitysign = (SignBlockEntity) tileentity;
                        System.arraycopy(CardboardSign.sanitizeLines(event.getLines()), 0, ((IMixinSignBlockEntity)tileentitysign).getTextBF(), 0, 4);
                        tileentitysign.editable = false;
                     }
                } catch (NullPointerException serverNoLikeSigns) {}
            });
        } catch (NullPointerException serverNoLikeSigns) {}
    }


}