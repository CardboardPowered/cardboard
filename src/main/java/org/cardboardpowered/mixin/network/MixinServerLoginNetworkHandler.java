package org.cardboardpowered.mixin.network;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.Waitable;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.BukkitFabricMod;
import com.javazilla.bukkitfabric.interfaces.IMixinClientConnection;
import com.javazilla.bukkitfabric.interfaces.IMixinMinecraftServer;
import com.javazilla.bukkitfabric.interfaces.IMixinPlayerManager;
import com.javazilla.bukkitfabric.interfaces.IMixinServerLoginNetworkHandler;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.local.LocalAddress;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.encryption.NetworkEncryptionException;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginCompressionS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerLoginNetworkHandler.State;
import net.minecraft.text.Text;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import  net.minecraft.server.network.ServerLoginNetworkHandler.State;

@SuppressWarnings("deprecation")
@Mixin(value = ServerLoginNetworkHandler.class, priority = 999)
public class MixinServerLoginNetworkHandler implements IMixinServerLoginNetworkHandler {

    @Shadow private byte[] nonce = new byte[4];
    @Shadow private MinecraftServer server;
    @Shadow public ClientConnection connection;
    @Shadow private ServerLoginNetworkHandler.State state;
    @Shadow private GameProfile profile;
    // TODO 1.17ify: @Shadow private SecretKey secretKey;
    @Shadow public ServerPlayerEntity delayedPlayer;

    private Logger LOGGER_BF = LogManager.getLogger("Bukkit|ServerLoginNetworkHandler");
    public String hostname = ""; // Bukkit - add field
    private long theid = 0;

    @Inject(at = @At("TAIL"), method = "<init>*")
    public void setBF(MinecraftServer minecraftserver, ClientConnection networkmanager, CallbackInfo ci) {
        BukkitFabricMod.NETWORK_CACHE.add((ServerLoginNetworkHandler)(Object)this);
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public void setHostname(String s) {
        this.hostname = s;
    }

    @Overwrite
    public void onKey(LoginKeyC2SPacket packet) {
        Validate.validState((this.state == State.KEY ? 1 : 0) != 0, (String)"Unexpected key packet", (Object[])new Object[0]);
        PrivateKey privateKey = this.server.getKeyPair().getPrivate();
        String id = "";
        try {
            if (!Arrays.equals(this.nonce, packet.decryptNonce(privateKey))) {
                throw new IllegalStateException("Protocol error");
            }
            SecretKey secretKey = packet.decryptSecretKey(privateKey);
            Cipher cipher = NetworkEncryptionUtils.cipherFromKey(2, secretKey);
            Cipher cipher2 = NetworkEncryptionUtils.cipherFromKey(1, secretKey);
            String string = new BigInteger(NetworkEncryptionUtils.computeServerId("", this.server.getKeyPair().getPublic(), secretKey)).toString(16);
            id = string;
            this.state = State.AUTHENTICATING;
            this.connection.setupEncryption(cipher, cipher2);
        }
        catch (NetworkEncryptionException networkEncryptionException) {
            throw new IllegalStateException("Protocol error", networkEncryptionException);
        }
        final String s = id;
        Thread thread = new Thread("User Authenticator #" + theid++) {
            @Override
            public void run() {
                GameProfile gameprofile = profile;

                try {
                    //String s = server.serverId;// TODO Check 1.17ify (new BigInteger(NetworkEncryptionUtils.generateServerId("", server.getKeyPair().getPublic(), secretKey))).toString(16);
                    profile = server.getSessionService().hasJoinedServer(new GameProfile((UUID)null, gameprofile.getName()), s, this.a());
                    if (profile != null) {
                        // Fire PlayerPreLoginEvent
                        if (!connection.isOpen()) return;
                        fireEvents();
                    } else if (server.isSingleplayer()) {
                        LOGGER_BF.warn("Failed to verify username but will let them in anyway!");
                        profile = toOfflineProfile(gameprofile);
                        state = ServerLoginNetworkHandler.State.READY_TO_ACCEPT;
                    } else {
                        disconnect("multiplayer.disconnect.unverified_username");
                        LOGGER_BF.error("Username '{}' tried to join with an invalid session", gameprofile.getName());
                    }
                } catch (AuthenticationUnavailableException authenticationunavailableexception) {
                    if (server.isSingleplayer()) {
                        LOGGER_BF.warn("Authentication servers are down but will let them in anyway!");
                        profile = toOfflineProfile(gameprofile);
                        state = ServerLoginNetworkHandler.State.READY_TO_ACCEPT;
                    } else {
                        disconnect("multiplayer.disconnect.authservers_down");
                        LOGGER_BF.error("Couldn't verify username because servers are unavailable");
                    }
                } catch (Exception exception) {
                    disconnect("Failed to verify username!");
                    LOGGER_BF.log(Level.WARN, "Exception verifying " + gameprofile.getName(), exception);
                }
            }

            @Nullable
            private InetAddress a() {
                SocketAddress socketaddress = connection.getAddress();
                return server.shouldPreventProxyConnections() && socketaddress instanceof InetSocketAddress ? ((InetSocketAddress) socketaddress).getAddress() : null;
            }
        };
        // TODO: thread.setUncaughtExceptionHandler(new UncaughtExceptionLogger(LogManager.getLogger("BukkitServerLoginManager")));
        thread.start();
    }

    public void fireEvents() throws Exception {
        String playerName = profile.getName();
        java.net.InetAddress address;
        if (connection.getAddress() instanceof LocalAddress) {
            address = InetAddress.getLocalHost();
        } else address = ((java.net.InetSocketAddress) connection.getAddress()).getAddress();
        UUID uniqueId = profile.getId();
        final org.bukkit.craftbukkit.CraftServer server = CraftServer.INSTANCE;

        AsyncPlayerPreLoginEvent asyncEvent = new AsyncPlayerPreLoginEvent(playerName, address, uniqueId);
        server.getPluginManager().callEvent(asyncEvent);

        if (PlayerPreLoginEvent.getHandlerList().getRegisteredListeners().length != 0) {
            final PlayerPreLoginEvent event = new PlayerPreLoginEvent(playerName, address, uniqueId);
            if (asyncEvent.getResult() != PlayerPreLoginEvent.Result.ALLOWED)
                event.disallow(asyncEvent.getResult(), asyncEvent.getKickMessage());

            Waitable<PlayerPreLoginEvent.Result> waitable = new Waitable<PlayerPreLoginEvent.Result>() {
                @Override
                protected PlayerPreLoginEvent.Result evaluate() {
                    server.getPluginManager().callEvent(event);
                    return event.getResult();
                }
            };

            ((IMixinMinecraftServer)CraftServer.server).getProcessQueue().add(waitable);
            if (waitable.get() != PlayerPreLoginEvent.Result.ALLOWED) {
                disconnect(event.getKickMessage());
                return;
            }
        } else {
            if (asyncEvent.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
                disconnect(asyncEvent.getKickMessage());
                return;
            }
        }
        LOGGER_BF.info("UUID of player {} is {}", profile.getName(), profile.getId());
        state = ServerLoginNetworkHandler.State.READY_TO_ACCEPT;
    }

    public void disconnect(String s) {
        try {
            Text text = Text.of(s);
            LOGGER_BF.info("Disconnecting BUKKITFABRIC_TODO: " + s);
            this.connection.send(new LoginDisconnectS2CPacket(text));
            this.connection.disconnect(text);
        } catch (Exception exception) {
            LOGGER_BF.error("Error whilst disconnecting player", exception);
        }
    }

    private ServerPlayerEntity cardboard_player;

    @Redirect(at = @At(value = "INVOKE", 
            target = "Lnet/minecraft/server/PlayerManager;checkCanJoin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/text/Text;"),
            method = "acceptPlayer")
    public Text acceptPlayer_checkCanJoin(PlayerManager man, SocketAddress a, GameProfile b) {
        ServerPlayerEntity s = ((IMixinPlayerManager)this.server.getPlayerManager()).attemptLogin((ServerLoginNetworkHandler)(Object)this, this.profile, hostname);
        cardboard_player = s;

        return null;
    }

    @Redirect(at = @At(value = "INVOKE", 
            target = "Lnet/minecraft/server/PlayerManager;createPlayer(Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/server/network/ServerPlayerEntity;"),
            method = "acceptPlayer")
    public ServerPlayerEntity acceptPlayer_createPlayer(PlayerManager man, GameProfile a) {
        return cardboard_player;
    }

    @Inject(at = @At("HEAD"), method="onHello", cancellable = true)
    public void spigotHello1(LoginHelloC2SPacket p, CallbackInfo ci) {
        if (state != State.HELLO) {
            ((ServerLoginNetworkHandler)(Object)this).acceptPlayer();
            ci.cancel();
        }
    }

    @Inject(at = @At("TAIL"), method="onHello")
    public void spigotHello(LoginHelloC2SPacket packetlogininstart, CallbackInfo ci) {
        if (!(this.server.isOnlineMode() && !this.connection.isLocal())) {
            // Spigot start
            new Thread("User Authenticator #" + theid++) {
                @Override
                public void run() {
                    try {
                        initUUID();
                        fireEvents();
                    } catch (Exception ex) {
                        disconnect("Failed to verify username!");
                        CraftServer.INSTANCE.getLogger().log(java.util.logging.Level.WARNING, "Exception verifying " + profile.getName(), ex);
                    }
                }
            }.start();
            // Spigot end
        }
    }

    // Spigot start
    public void initUUID() {
        UUID uuid;
        if ( ((IMixinClientConnection)connection).getSpoofedUUID() != null )
            uuid = ((IMixinClientConnection)connection).getSpoofedUUID();
        else uuid = PlayerEntity.getOfflinePlayerUuid( this.profile.getName() );

        this.profile = new GameProfile( uuid, this.profile.getName() );

        if (((IMixinClientConnection)connection).getSpoofedProfile() != null)
            for ( com.mojang.authlib.properties.Property property : ((IMixinClientConnection)connection).getSpoofedProfile() )
                this.profile.getProperties().put( property.getName(), property );
    }
    // Spigot end

    @Shadow protected GameProfile toOfflineProfile(GameProfile gameprofile) {return null;}
    @Shadow public void disconnect(Text t) {}

}