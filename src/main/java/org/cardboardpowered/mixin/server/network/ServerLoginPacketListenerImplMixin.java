package org.cardboardpowered.mixin.server.network;

import org.cardboardpowered.bridge.network.ConnectionBridge;
import org.cardboardpowered.bridge.server.MinecraftServerBridge;
import org.cardboardpowered.bridge.server.network.ServerLoginPacketListenerImplBridge;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.yggdrasil.ProfileResult;

import io.netty.channel.local.LocalAddress;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.ClientboundLoginFinishedPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.network.protocol.login.ServerboundLoginAcknowledgedPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.server.network.ServerLoginPacketListenerImpl.State;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import org.apache.commons.lang3.Validate;
import org.cardboardpowered.bridge.server.players.PlayerListBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.Waitable;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent.Result;
import org.cardboardpowered.bridge.server.network.ServerConfigurationPacketListenerImplBridge;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import io.papermc.paper.connection.PaperPlayerLoginConnection;

@SuppressWarnings("deprecation")
@Mixin(value = ServerLoginPacketListenerImpl.class, priority = 999)
public abstract class ServerLoginPacketListenerImplMixin implements ServerLoginPacketListenerImplBridge {

	private static Logger LOGGER_BF = LoggerFactory.getLogger("PaperMC|ServerLoginNetworkHandler"); // LogManager.getLogger("Bukkit|ServerLoginNetworkHandler");
	
	// Cardboard: Paper - Use ExecutorService
	private static final ExecutorService authenticatorPool = Executors.newCachedThreadPool(
		      new ThreadFactoryBuilder().setNameFormat("User Authenticator #%d").setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER_BF)).build()
		   );
	
	@Shadow @Nullable private String requestedUsername;
	@Shadow
	abstract void startClientVerification(GameProfile profile);
	@Shadow private byte[] challenge = new byte[4];
	@Shadow private MinecraftServer server;
	@Shadow public Connection connection;
	@Shadow private ServerLoginPacketListenerImpl.State state;
	@Shadow private GameProfile authenticatedProfile;
	
	@Shadow private static AtomicInteger UNIQUE_THREAD_ID;
	
	public ServerPlayer delayedPlayer;

	@Override
	public Connection cb_get_connection() {
		return connection;
	}

	public String hostname = ""; // Bukkit - add field
	private long theid = 0;
	
	// Cardboard: field added by bukkit
	private ServerPlayer player;
	public UUID requestedUuid;
	private PaperPlayerLoginConnection paperLoginConnection1;
	
	private PaperPlayerLoginConnection paperLoginConnection() {
		if (null == paperLoginConnection1) {
			this.paperLoginConnection1 = new PaperPlayerLoginConnection((ServerLoginPacketListenerImpl) (Object) this);
		}
		return this.paperLoginConnection1;
	}
	
	@Inject(at = @At("HEAD"), method = "handleHello")
	public void cardboard$setRequestedUuid(ServerboundHelloPacket packet, CallbackInfo ci) {
		this.requestedUuid = packet.profileId();
	}
	
	@Override
	public UUID cardboard$requestedUuid() {
		return this.requestedUuid;
	}
	
	@Override
	public String cardboard$profileName() {
		return this.requestedUsername;
	}
	
	@Override
	public boolean cardboard$transferred() {
		return this.transferred;
	}

	@Override
	public String getHostname() {
		return hostname;
	}

	@Override
	public void setHostname(String s) {
		this.hostname = s;
	}

	/**
	 * @author cardboard
	 * @reason Bukkit login changes
	 */
	@Overwrite
	public void handleKey(ServerboundKeyPacket packet) {
		Validate.validState(this.state == ServerLoginPacketListenerImpl.State.KEY, "Unexpected key packet", new Object[0]);
		final String string;
		try {
			PrivateKey _private = this.server.getKeyPair().getPrivate();
			if (!packet.isChallengeValid(this.challenge, _private)) {
				throw new IllegalStateException("Protocol error");
			}

			SecretKey secretKey = packet.getSecretKey(_private);
			Cipher cipher = Crypt.getCipher(2, secretKey);
			Cipher cipher1 = Crypt.getCipher(1, secretKey);
			string = (new BigInteger(Crypt.digestData("", this.server.getKeyPair()
					.getPublic(), secretKey))).toString(16);

			this.state = ServerLoginPacketListenerImpl.State.AUTHENTICATING;
			this.connection.setEncryptionKey(cipher, cipher1);

		} catch (CryptException var5) {
			throw new IllegalStateException("Protocol error", var5);
		}

		authenticatorPool.execute(
				new Runnable() {
					@Override
					public void run() {
						String string1 = Objects.requireNonNull(requestedUsername, "Player name not initialized");

						try {
							ProfileResult profileResult = server
									.services()
									.sessionService()
									.hasJoinedServer(string1, string, this.getClientAddress());
							if (profileResult != null) {
								GameProfile gameProfile = profileResult.profile();
								if (!connection.isConnected()) {
									return;
								}

								gameProfile = callPlayerPreLoginEvents(gameProfile);
								LOGGER_BF.info("UUID of player {} is {}", gameProfile.name(), gameProfile.id());
								startClientVerification(gameProfile);
							} else if (server.isSingleplayer()) {
								LOGGER_BF.warn("Failed to verify username but will let them in anyway!");
								startClientVerification(createOfflineProfile(string1));
							} else {
								disconnect(net.minecraft.network.chat.Component.translatable("multiplayer.disconnect.unverified_username"));
								LOGGER_BF.error("Username '{}' tried to join with an invalid session", string1);
							}
						} catch (AuthenticationUnavailableException var4) {
							if (server.isSingleplayer()) {
								LOGGER_BF.warn("Authentication servers are down but will let them in anyway!");
								startClientVerification(createOfflineProfile(string1));
							} else {
								// this.disconnect(PaperAdventure.asVanilla(GlobalConfiguration.get().messages.kick.authenticationServersDown));
								disconnect(PaperAdventure.asVanilla( Component.translatable("multiplayer.disconnect.authservers_down")) );
								LOGGER_BF.error("Couldn't verify username because servers are unavailable");
							}
						} catch (Exception var5) {
							disconnect("Failed to verify username!");
							LOGGER_BF.warn("Exception verifying {}", string1, var5);
						}
					}

					@Nullable
					private InetAddress getClientAddress() {
						SocketAddress remoteAddress = connection.getRemoteAddress();
						return server.getPreventProxyConnections() && remoteAddress instanceof InetSocketAddress
								? ((InetSocketAddress)remoteAddress).getAddress()
										: null;
					}
				}
				);
	}

	protected GameProfile createOfflineProfile(String s) {
		/*  
		UUID uuid;
	      if (this.connection.spoofedUUID != null) {
	         uuid = this.connection.spoofedUUID;
	      } else {
	         uuid = Uuids.getOfflinePlayerUuid(s);
	      }

	      Builder<String, Property> props = ImmutableMultimap.builder();
	      if (this.connection.spoofedProfile != null) {
	         for (Property property : this.connection.spoofedProfile) {
	            if (ServerHandshakeNetworkHandler.PROP_PATTERN.matcher(property.name()).matches()) {
	               props.put(property.name(), property);
	            }
	         }
	      }

	      return new GameProfile(uuid, s, new PropertyMap(props.build()));
		 */
		// TODO
		return UUIDUtil.createOfflineProfile(s);
	}

	private GameProfile callPlayerPreLoginEvents(GameProfile gameprofile) throws Exception {
		if (false /*this.velocityLoginMessageId == -1 && GlobalConfiguration.get().proxies.velocity.enabled*/) {
			this.disconnect("This server requires you to connect with Velocity.");
			return gameprofile;
		} else {
			String playerName = gameprofile.name();
			InetAddress address = ((InetSocketAddress)this.connection.getRemoteAddress()).getAddress();
			UUID uniqueId = gameprofile.id();
			final CraftServer server = CraftServer.INSTANCE;
			InetAddress rawAddress = ((InetSocketAddress)this.connection.channel.remoteAddress()).getAddress();
			PlayerProfile profile = CraftPlayerProfile.asBukkitCopy(gameprofile);


			AsyncPlayerPreLoginEvent asyncEvent = new AsyncPlayerPreLoginEvent(playerName, address, uniqueId);
			/*
	         AsyncPlayerPreLoginEvent asyncEvent = new AsyncPlayerPreLoginEvent(
	            playerName, address, rawAddress, uniqueId, this.transferred, profile, this.connection.hostname, this.paperLoginConnection
	         );
			 */
			server.getPluginManager().callEvent(asyncEvent);
			profile = asyncEvent.getPlayerProfile();
			profile.complete(true);
			gameprofile = CraftPlayerProfile.asAuthlibCopy(profile);
			playerName = gameprofile.name();
			uniqueId = gameprofile.id();
			if (PlayerPreLoginEvent.getHandlerList().getRegisteredListeners().length != 0) {
				final PlayerPreLoginEvent event = new PlayerPreLoginEvent(playerName, address, uniqueId);
				if (asyncEvent.getResult() != Result.ALLOWED) {
					event.disallow(asyncEvent.getResult(), asyncEvent.kickMessage());
				}

				Waitable<Result> waitable = new Waitable<Result>() {
					protected Result evaluate() {
						server.getPluginManager().callEvent(event);
						return event.getResult();
					}
				};
				((MinecraftServerBridge) CraftServer.server).getProcessQueue().add(waitable);
				if (waitable.get() != Result.ALLOWED) {
					this.disconnect(PaperAdventure.asVanilla(event.kickMessage()));
				}
			} else if (asyncEvent.getLoginResult() != org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.ALLOWED) {
				this.disconnect(PaperAdventure.asVanilla(asyncEvent.kickMessage()));
			}

			return gameprofile;
		}
	}

	public void fireEvents(GameProfile profile) throws Exception {
		String playerName = profile.name();
		java.net.InetAddress address;
		if(connection.getRemoteAddress() instanceof LocalAddress) {
			address = InetAddress.getLocalHost();
		} else address = ((java.net.InetSocketAddress) connection.getRemoteAddress()).getAddress();
		UUID uniqueId = profile.id();
		final org.bukkit.craftbukkit.CraftServer server = CraftServer.INSTANCE;

		AsyncPlayerPreLoginEvent asyncEvent = new AsyncPlayerPreLoginEvent(playerName, address, uniqueId);
		server.getPluginManager().callEvent(asyncEvent);

		if(PlayerPreLoginEvent.getHandlerList().getRegisteredListeners().length != 0) {
			final PlayerPreLoginEvent event = new PlayerPreLoginEvent(playerName, address, uniqueId);
			if(asyncEvent.getResult() != PlayerPreLoginEvent.Result.ALLOWED)
				event.disallow(asyncEvent.getResult(), asyncEvent.getKickMessage());

			Waitable<PlayerPreLoginEvent.Result> waitable = new Waitable<PlayerPreLoginEvent.Result>() {
				@Override
				protected PlayerPreLoginEvent.Result evaluate() {
					server.getPluginManager().callEvent(event);
					return event.getResult();
				}
			};

			((MinecraftServerBridge) CraftServer.server).getProcessQueue().add(waitable);
			if(waitable.get() != PlayerPreLoginEvent.Result.ALLOWED) {
				disconnect(event.getKickMessage());
				return;
			}
		} else {
			if(asyncEvent.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
				disconnect(asyncEvent.getKickMessage());
				return;
			}
		}
		LOGGER_BF.info("UUID of player {} is {}", profile.name(), profile.id());
		startClientVerification(profile);
	}

	public void disconnect(String s) {
		try {
			net.minecraft.network.chat.Component text = net.minecraft.network.chat.Component.nullToEmpty(s);
			LOGGER_BF.info("Disconnecting BUKKITFABRIC_TODO: " + s);
			this.connection.send(new ClientboundLoginDisconnectPacket(text));
			this.connection.disconnect(text);
		} catch(Exception exception) {
			LOGGER_BF.error("Error whilst disconnecting player", exception);
		}
	}

	/**
	 * @author cardboard mod
	 * @reason
	 */
	// TODO: Overwrite can be replaced with something else.
	@Overwrite
	private void verifyLoginAndFinishConnectionSetup(GameProfile profile) {
		PlayerList playerList = this.server.getPlayerList();
		net.minecraft.network.chat.Component component = org.bukkit.craftbukkit.event.CraftEventFactory.handleLoginResult(((PlayerListBridge)playerList).cardboard$canPlayerLogin(this.connection.getRemoteAddress(), new NameAndId(profile)), this.paperLoginConnection(), this.connection, profile, this.server, true); // Paper
		if (component != null) {
			this.disconnect(component);
		} else {
			if (this.server.getCompressionThreshold() >= 0 && !this.connection.isMemoryConnection()) {
				this.connection
						.send(
								new ClientboundLoginCompressionPacket(this.server.getCompressionThreshold()),
								PacketSendListener.thenRun(() -> this.connection.setupCompression(this.server.getCompressionThreshold(), true))
						);
			}

			boolean flag = playerList.disconnectAllPlayersWithProfile(profile.id());
			if (flag) {
				this.state = ServerLoginPacketListenerImpl.State.WAITING_FOR_DUPE_DISCONNECT;
			} else {
				this.finishLoginAndWaitForClient(profile);
			}
		}
	}
    
    @Shadow
    private void finishLoginAndWaitForClient(GameProfile profile) {
        this.state = State.PROTOCOL_SWITCHING;
        this.connection.send(new ClientboundLoginFinishedPacket(profile, java.util.UUID.randomUUID()));
    }

	@Inject(at = @At("TAIL"), method = "handleHello")
	public void spigotHello(ServerboundHelloPacket packetlogininstart, CallbackInfo ci) {
		if(!(this.server.usesAuthentication() && !this.connection.isMemoryConnection())) {
			// Spigot start
			new Thread("User Authenticator #" + theid++) {
				@Override
				public void run() {
					try {
						initUUID();
						fireEvents(authenticatedProfile);
					} catch(Exception ex) {
						disconnect("Failed to verify username!");
						CraftServer.INSTANCE.getLogger()
								.log(java.util.logging.Level.WARNING, "Exception verifying " + authenticatedProfile.name(), ex);
					}
				}
			}.start();
			// Spigot end
		}
	}

	// Spigot start
	public void initUUID() {
		UUID uuid;
		if(((ConnectionBridge) connection).getSpoofedUUID() != null)
			uuid = ((ConnectionBridge) connection).getSpoofedUUID();
		else {
			// Note: PlayerEntity (1.18) -> DynamicSerializableUuid (1.19) -> Uuids (1.19.4)
			uuid = UUIDUtil.createOfflinePlayerUUID(this.authenticatedProfile.name());
		}

		this.authenticatedProfile = new GameProfile(uuid, this.authenticatedProfile.name());

		if(((ConnectionBridge) connection).getSpoofedProfile() != null)
			for(com.mojang.authlib.properties.Property property : ((ConnectionBridge) connection).getSpoofedProfile())
				this.authenticatedProfile.properties().put(property.name(), property);
	}
	// Spigot end

    @Shadow
    private boolean transferred;

    /*
	@Inject(method = "onEnterConfiguration",
			locals = LocalCapture.CAPTURE_FAILEXCEPTION,
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/ClientConnection;setPacketListener(Lnet/minecraft/network/listener/PacketListener;)V"
				)
			)
	private void onCreateNetworkConfig(EnterConfigurationC2SPacket packet, CallbackInfo ci, ConnectedClientData connectedClientData, ServerConfigurationNetworkHandler networkConfig) {
		if(cardboard_player != null) {
			((INetworkConfiguration) networkConfig).cardboard_setPlayer(cardboard_player);
		}
	}
	*/

	@Shadow
	public void disconnect(net.minecraft.network.chat.Component t) {}

}
