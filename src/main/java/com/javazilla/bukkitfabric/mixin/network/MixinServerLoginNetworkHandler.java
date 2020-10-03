package com.javazilla.bukkitfabric.mixin.network;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.UUID;

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
import com.javazilla.bukkitfabric.interfaces.IMixinMinecraftServer;
import com.javazilla.bukkitfabric.interfaces.IMixinPlayerManager;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkEncryptionUtils;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginCompressionS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.logging.UncaughtExceptionLogger;

@SuppressWarnings("deprecation")
@Mixin(ServerLoginNetworkHandler.class)
public class MixinServerLoginNetworkHandler {

    @Shadow private byte[] nonce = new byte[4];
    @Shadow private MinecraftServer server;
    @Shadow public ClientConnection connection;
    @Shadow private ServerLoginNetworkHandler.State state;
    @Shadow private GameProfile profile;
    @Shadow private SecretKey secretKey;
    @Shadow public ServerPlayerEntity player;

    private Logger LOGGER_BF = LogManager.getLogger("Bukkit|ServerLoginNetworkHandler");
    public String hostname = ""; // CraftBukkit - add field

    /**
     * @reason Spigot basically overwrites this whole method.
     * @author BukkitFabric
     */
    @Overwrite
    public void onKey(LoginKeyC2SPacket packetlogininencryptionbegin) {
        Validate.validState(this.state == ServerLoginNetworkHandler.State.KEY, "Unexpected key packet", new Object[0]);
        PrivateKey privatekey = this.server.getKeyPair().getPrivate();

        if (!Arrays.equals(this.nonce, packetlogininencryptionbegin.decryptNonce(privatekey))) {
            throw new IllegalStateException("Invalid nonce!");
        } else {
            this.secretKey = packetlogininencryptionbegin.decryptSecretKey(privatekey);
            this.state = ServerLoginNetworkHandler.State.AUTHENTICATING;
            this.connection.setupEncryption(this.secretKey);
            Thread thread = new Thread("User Authenticator #" + ServerLoginNetworkHandler.authenticatorThreadId.incrementAndGet()) {
                public void run() {
                    GameProfile gameprofile = profile;

                    try {
                        String s = (new BigInteger(NetworkEncryptionUtils.generateServerId("", server.getKeyPair().getPublic(), secretKey))).toString(16);

                        profile = server.getSessionService().hasJoinedServer(new GameProfile((UUID)null, gameprofile.getName()), s, this.a());
                        if (profile != null) {
                            // CraftBukkit start - fire PlayerPreLoginEvent
                            if (!connection.isOpen())
                                return;
                            fireEvents();
                        } else if (server.isSinglePlayer()) {
                            LOGGER_BF.warn("Failed to verify username but will let them in anyway!");
                            profile = toOfflineProfile(gameprofile);
                            state = ServerLoginNetworkHandler.State.READY_TO_ACCEPT;
                        } else {
                            disconnect(new TranslatableText("multiplayer.disconnect.unverified_username"));
                            LOGGER_BF.error("Username '{}' tried to join with an invalid session", gameprofile.getName());
                        }
                    } catch (AuthenticationUnavailableException authenticationunavailableexception) {
                        if (server.isSinglePlayer()) {
                            LOGGER_BF.warn("Authentication servers are down but will let them in anyway!");
                            profile = toOfflineProfile(gameprofile);
                            state = ServerLoginNetworkHandler.State.READY_TO_ACCEPT;
                        } else {
                            disconnect(new TranslatableText("multiplayer.disconnect.authservers_down"));
                            LOGGER_BF.error("Couldn't verify username because servers are unavailable");
                        }
                        // CraftBukkit start - catch all exceptions
                    } catch (Exception exception) {
                        disconnect("Failed to verify username!");
                        LOGGER_BF.log(Level.WARN, "Exception verifying " + gameprofile.getName(), exception);
                        // CraftBukkit end
                    }

                }

                @Nullable
                private InetAddress a() {
                    SocketAddress socketaddress = connection.getAddress();
                    return server.shouldPreventProxyConnections() && socketaddress instanceof InetSocketAddress ? ((InetSocketAddress) socketaddress).getAddress() : null;
                }
            };

            thread.setUncaughtExceptionHandler(new UncaughtExceptionLogger(LogManager.getLogger("BukkitServerLoginManager")));
            thread.start();
        }
    }

    public void fireEvents() throws Exception {
        String playerName = profile.getName();
        java.net.InetAddress address = ((java.net.InetSocketAddress) connection.getAddress()).getAddress();
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
                }};

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
            Text ichatbasecomponent = new LiteralText(s);
            LOGGER_BF.info("Disconnecting {}: {}", "BUKKITFABRIC_TODO", s);
            this.connection.send(new LoginDisconnectS2CPacket(ichatbasecomponent));
            this.connection.disconnect(ichatbasecomponent);
        } catch (Exception exception) {
            LOGGER_BF.error("Error whilst disconnecting player", exception);
        }
    }

    /**
     * @author BukkitFabricMod
     * @reason Fire PlayerLoginEvent
     */
    @Overwrite
    public void acceptPlayer() {
        ServerPlayerEntity s = ((IMixinPlayerManager)this.server.getPlayerManager()).attemptLogin((ServerLoginNetworkHandler)(Object)this, this.profile, hostname);

        if (s != null) {
            this.state = ServerLoginNetworkHandler.State.ACCEPTED;
            if (this.server.getNetworkCompressionThreshold() >= 0 && !this.connection.isLocal()) {
                this.connection.send(new LoginCompressionS2CPacket(this.server.getNetworkCompressionThreshold()), (channelfuture) -> {
                    this.connection.setCompressionThreshold(this.server.getNetworkCompressionThreshold());
                });
            }
            this.connection.send(new LoginSuccessS2CPacket(this.profile));
            ServerPlayerEntity entityplayer = this.server.getPlayerManager().getPlayer(this.profile.getId());
            if (entityplayer != null) {
                this.state = ServerLoginNetworkHandler.State.DELAY_ACCEPT;
                this.player = s;
            } else this.server.getPlayerManager().onPlayerConnect(this.connection, s);
        }

    }

    @Shadow protected GameProfile toOfflineProfile(GameProfile gameprofile) {return null;}
    @Shadow public void disconnect(Text t) {}

}