package org.cardboardpowered.mixin.network;

import com.javazilla.bukkitfabric.BukkitFabricMod;
import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.handler.LegacyQueryHandler;
import net.minecraft.server.MinecraftServer;
import org.bukkit.craftbukkit.CraftServer;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

@MixinInfo(events = "ServerListPingEvent")
@Mixin(value = LegacyQueryHandler.class, priority = 999)
public class MixinLegacyQueryHandler {

    @Shadow private static ByteBuf createBuf(ByteBufAllocator allocator, String string) {return null;}
    @Shadow private static void reply(ChannelHandlerContext channelhandlercontext, ByteBuf bytebuf) {}

    /**
     * @reason Add ServerListPingEvent
     * @author bukkit4fabric
     */
    @Overwrite
    public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {
        ByteBuf bytebuf = (ByteBuf) object;

        bytebuf.markReaderIndex();
        boolean flag = true;

        try {
            if (bytebuf.readUnsignedByte() != 254)
                return;
            InetSocketAddress inetsocketaddress = (InetSocketAddress) ctx.channel().remoteAddress();
            MinecraftServer minecraftserver = CraftServer.server;
            int i = bytebuf.readableBytes();
            String s;
            org.bukkit.event.server.ServerListPingEvent event = BukkitEventFactory.callServerListPingEvent(CraftServer.INSTANCE, inetsocketaddress.getAddress(), minecraftserver.getServerMotd(), minecraftserver.getCurrentPlayerCount(), minecraftserver.getMaxPlayerCount()); // CraftBukkit

            switch (i) {
                case 0:
                    BukkitFabricMod.LOGGER.config("Ping: (<1.3.x) from " + inetsocketaddress.getAddress() + ":" + inetsocketaddress.getPort());
                    s = String.format("%s\u00a7%d\u00a7%d", event.getMotd(), event.getNumPlayers(), event.getMaxPlayers()); // CraftBukkit
                    reply(ctx, createBuf(ctx.alloc(), s));
                    break;
                case 1:
                    if (bytebuf.readUnsignedByte() != 1)
                        return;

                    BukkitFabricMod.LOGGER.config("Ping: (1.4-1.5.x) from " + inetsocketaddress.getAddress() + ":" + inetsocketaddress.getPort());
                    s = String.format("\u00a71\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", 127, minecraftserver.getVersion(), event.getMotd(), event.getNumPlayers(), event.getMaxPlayers()); // CraftBukkit
                    reply(ctx, createBuf(ctx.alloc(), s));
                    break;
                default:
                    boolean flag1 = bytebuf.readUnsignedByte() == 1;

                    flag1 &= bytebuf.readUnsignedByte() == 250;
                    flag1 &= "MC|PingHost".equals(new String(bytebuf.readBytes(bytebuf.readShort() * 2).array(), StandardCharsets.UTF_16BE));
                    int j = bytebuf.readUnsignedShort();

                    flag1 &= bytebuf.readUnsignedByte() >= 73;
                    flag1 &= 3 + bytebuf.readBytes(bytebuf.readShort() * 2).array().length + 4 == j;
                    flag1 &= bytebuf.readInt() <= 65535;
                    flag1 &= bytebuf.readableBytes() == 0;
                    if (!flag1)
                        return;
                    BukkitFabricMod.LOGGER.config("Ping: (1.6) from " + inetsocketaddress.getAddress() + ":" + inetsocketaddress.getPort());
                    String s1 = String.format("\u00a71\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", 127, minecraftserver.getVersion(), event.getMotd(), event.getNumPlayers(), event.getMaxPlayers()); // CraftBukkit
                    System.out.println("DEBUG: " + s1);
                    ByteBuf bytebuf1 = createBuf(ctx.alloc(), s1);

                    try {
                        reply(ctx, bytebuf1);
                    } finally {
                        bytebuf1.release();
                    }
            }

            bytebuf.release();
            flag = false;
        } catch (RuntimeException runtimeexception) {
            runtimeexception.printStackTrace();
        } finally {
            if (flag) {
                bytebuf.resetReaderIndex();
                ctx.channel().pipeline().remove("legacy_query");
                ctx.fireChannelRead(object);
            }

        }

    }

}
