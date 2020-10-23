package com.javazilla.bukkitfabric.mixin.network;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.CraftServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinNetworkIo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.DecoderHandler;
import net.minecraft.network.LegacyQueryHandler;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.RateLimitedConnection;
import net.minecraft.network.SizePrepender;
import net.minecraft.network.SplitterHandler;
import net.minecraft.server.ServerNetworkIo;
import net.minecraft.server.network.ServerHandshakeNetworkHandler;
import net.minecraft.util.Lazy;

@Mixin(ServerNetworkIo.class)
public class MixinServerNetworkIo implements IMixinNetworkIo {

    @Shadow
    @Final
    public List<ChannelFuture> channels;

    @Override
    public void acceptConnections() {
        synchronized (channels) {
            for (ChannelFuture future : channels)
                future.channel().config().setAutoRead(true);
        }
    }

    private ServerNetworkIo getBF() {
        return (ServerNetworkIo)(Object)this;
    }

    /**
     * @author .
     * @reason AUTO_READ to be false.
     */
    @Overwrite
    public void bind(InetAddress inetaddress, int i) throws IOException {
        Logger LOGBF = LogManager.getLogger("Bukkit|ServerNetworkIo");
        List list = this.channels;

        synchronized (this.channels) {
            Class oclass;
            Lazy lazyinitvar;

            if (Epoll.isAvailable() && CraftServer.server.isUsingNativeTransport()) {
                oclass = EpollServerSocketChannel.class;
                lazyinitvar = ServerNetworkIo.EPOLL_CHANNEL;
                LOGBF.info("Using epoll channel type");
            } else {
                oclass = NioServerSocketChannel.class;
                lazyinitvar = ServerNetworkIo.DEFAULT_CHANNEL;
                LOGBF.info("Using default channel type");
            }

            this.channels.add(((ServerBootstrap) ((ServerBootstrap) (new ServerBootstrap()).channel(oclass)).childHandler(new ChannelInitializer<Channel>() {
                protected void initChannel(Channel channel) throws Exception {
                    try {
                        channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                    } catch (ChannelException channelexception) {
                        ;
                    }

                    channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30)).addLast("legacy_query", new LegacyQueryHandler(getBF())).addLast("splitter", new SplitterHandler()).addLast("decoder", new DecoderHandler(NetworkSide.SERVERBOUND)).addLast("prepender", new SizePrepender()).addLast("encoder", new PacketEncoder(NetworkSide.CLIENTBOUND));
                    int j = CraftServer.server.getRateLimit();
                    Object object = j > 0 ? new RateLimitedConnection(j) : new ClientConnection(NetworkSide.SERVERBOUND);

                    getBF().connections.add((ClientConnection) object);
                    channel.pipeline().addLast("packet_handler", (ChannelHandler) object);
                    ((ClientConnection) object).setPacketListener(new ServerHandshakeNetworkHandler(CraftServer.server, (ClientConnection) object));
                }
            }).group((EventLoopGroup) lazyinitvar.get()).localAddress(inetaddress, i)).option(ChannelOption.AUTO_READ, false).bind().syncUninterruptibly()); // Bukkit
        }
    }

}