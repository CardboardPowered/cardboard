package org.cardboardpowered.mixin.server.network;

import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.server.network.ServerGamePacketListenerImplBridge;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;
import org.cardboardpowered.bridge.server.level.ServerPlayerGameModeBridge;
import me.isaiah.common.cmixin.IMixinEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.papermc.paper.connection.PaperPlayerGameConnection;
import io.papermc.paper.connection.PlayerGameConnection;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

@SuppressWarnings("deprecation")
@Mixin(value = ServerGamePacketListenerImpl.class, priority = 800)
public abstract class ServerGamePacketListenerImplMixin extends ServerCommonPacketListenerImpl implements ServerGamePacketListenerImplBridge {

    public ServerGamePacketListenerImplMixin(MinecraftServer server, Connection connection, CommonListenerCookie clientData) {
        super(server, connection, clientData);
        throw new AssertionError("nuh uh");
    }

    @Override
	public Connection cb_get_connection() {
		return connection;
	}

    @Shadow 
    public ServerPlayer player;

    private volatile int messageCooldownBukkit;
    private static final AtomicIntegerFieldUpdater<ServerGamePacketListenerImpl> chatSpamField = AtomicIntegerFieldUpdater.newUpdater(ServerGamePacketListenerImpl.class, "messageCooldownBukkit");

    @Shadow
    public int awaitingTeleportTime;

    @Shadow
    public int tickCount;

    @Shadow
    public Vec3 awaitingPositionFromClient;

    @Shadow
    public int awaitingTeleport;

    @Shadow public double firstGoodX;
    @Shadow public double firstGoodY;
    @Shadow public double firstGoodZ;
    @Shadow public double lastGoodX;
    @Shadow public double lastGoodY;
    @Shadow public double lastGoodZ;
    @Shadow private boolean clientIsFloating;
    @Shadow private int receivedMovePacketCount;
    @Shadow private int knownMovePacketCount;

    private int lastTick = 0;
    public int allowedPlayerTicks = 1;
    private double lastPosX = Double.MAX_VALUE;
    private double lastPosY = Double.MAX_VALUE;
    private double lastPosZ = Double.MAX_VALUE;
    private float lastPitch = Float.MAX_VALUE;
    private float lastYaw = Float.MAX_VALUE;
    private boolean justTeleported = false;

    /**
     * CraftBukkit's guard against the quit handling running more than once. disconnect() calls
     * onDisconnect() itself so the quit event fires instantly, and then schedules
     * handleDisconnection(), which calls onDisconnect() a second time. Without this flag a single
     * kick or keep-alive timeout produced two "lost connection" / "left the game" rounds followed
     * by Connection's "handleDisconnection() called twice" warning.
     */
    @Unique
    private boolean cardboard$processedDisconnect;

    @Override
    public boolean isDisconnected() {

    	return !connection.isConnected() || this.cardboard$processedDisconnect;

    	// return player.isDisconnected(); // TODO
    }

    @Inject(method = "onDisconnect", at = @At("HEAD"), cancellable = true)
    private void cardboard$guardDoubleDisconnect(DisconnectionDetails details, CallbackInfo ci) {
        if (this.cardboard$processedDisconnect) {
            ci.cancel();
        } else {
            this.cardboard$processedDisconnect = true;
        }
    }

    /**
     * @author BukkitFabric
     * @reason PlayerKickEvent
     */
    @Override
    public void disconnect(Component reason) {
        if (this.cardboard$processedDisconnect) return;

        String leaveMessage = ChatFormatting.YELLOW +
                "" + this.player.getDisplayName() + " left the game.";

        PlayerKickEvent event = new PlayerKickEvent(CraftServer.INSTANCE.getPlayer(this.player), reason.getString(), leaveMessage);

        if (CraftServer.INSTANCE.getServer().isRunning())
            CraftServer.INSTANCE.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        reason = Component.nullToEmpty(event.getReason());
        final Component reason_final = reason;

        ServerGamePacketListenerImplBridge im = (ServerGamePacketListenerImplBridge) get();
        im.cb_get_connection().send(new ClientboundDisconnectPacket(reason), PacketSendListener.thenRun(() -> im.cb_get_connection().disconnect(reason_final)));
        get().onDisconnect(new DisconnectionDetails(reason));
        //im.cb_get_connection().disableAutoRead();
        im.cb_get_connection().setReadOnly();
        
        CraftServer.server.executeBlocking(im.cb_get_connection()::handleDisconnection);
    }

    @Override
    public CraftPlayer getPlayer() {
        return (CraftPlayer) ((ServerPlayerBridge)(Object)this.player).getBukkitEntity();
    }

    // TODO: 1.19
    /*@Override
    public void chat(String s, boolean async) {
        if (s.isEmpty() || this.player.getClientChatVisibility() == ChatVisibility.HIDDEN)
            return;

        if (!async && s.startsWith("/")) {
            get().executeCommand(s);
        } else if (this.player.getClientChatVisibility() == ChatVisibility.SYSTEM) {
            // Do nothing, this is coming from a plugin
        } else {
            Player player = this.getPlayer();
            AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(async, player, s, new LazyPlayerSet(CraftServer.server));
            Bukkit.getServer().getPluginManager().callEvent(event);

            if (PlayerChatEvent.getHandlerList().getRegisteredListeners().length != 0) {
                // Evil plugins still listening to deprecated event
                final PlayerChatEvent queueEvent = new PlayerChatEvent(player, event.getMessage(), event.getFormat(), event.getRecipients());
                queueEvent.setCancelled(event.isCancelled());
                Waitable<?> waitable = new WaitableImpl(()-> {
                    Bukkit.getPluginManager().callEvent(queueEvent);

                    if (queueEvent.isCancelled())
                        return;

                    String message = String.format(queueEvent.getFormat(), queueEvent.getPlayer().getDisplayName(), queueEvent.getMessage());
                    for (Text txt : CraftChatMessage.fromString(message))
                        CraftServer.server.sendSystemMessage(txt, queueEvent.getPlayer().getUniqueId());
                    if (((LazyPlayerSet) queueEvent.getRecipients()).isLazy()) {
                        for (ServerPlayerEntity plr : CraftServer.server.getPlayerManager().getPlayerList())
                            for (Text txt : CraftChatMessage.fromString(message))
                                plr.sendMessage(txt, false);
                    } else for (Player plr : queueEvent.getRecipients())
                        plr.sendMessage(message);
                });

                if (async)
                    ((IMixinMinecraftServer)CraftServer.server).getProcessQueue().add(waitable);
                else waitable.run();
                try {
                    waitable.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // This is proper habit for java. If we aren't handling it, pass it on!
                } catch (ExecutionException e) {
                    throw new RuntimeException("Exception processing chat event", e.getCause());
                }
            } else {
                if (event.isCancelled()) return;

                s = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
                server.sendSystemMessage(new LiteralText(s), player.getUniqueId());
                if (((LazyPlayerSet) event.getRecipients()).isLazy()) {
                    for (ServerPlayerEntity recipient : server.getPlayerManager().players)
                        for (Text txt : CraftChatMessage.fromString(s))
                            recipient.sendMessage(txt, false);
                } else for (Player recipient : event.getRecipients())
                    recipient.sendMessage(s);
            }
        }
    }*/

    @Override
    public void teleport(Location dest) {
    	
    	PositionMoveRotation pos = new PositionMoveRotation(CraftLocation.toVec3(dest), Vec3.ZERO, dest.getYaw(), dest.getPitch());
    	teleport(pos, Collections.emptySet());
    	
        // requestTeleport(dest.getX(), dest.getY(), dest.getZ(), dest.getYaw(), dest.getPitch(), Collections.emptySet());
    }

    /**
     * @author cardboard
     * @reason PlayerTeleportEvent
     */
    @Overwrite
    //public void requestTeleport(double d0, double d1, double d2, float f, float f1, Set<PositionFlag> set) {
    public void teleport( PositionMoveRotation pos, Set<Relative> flags) {

    	Player player = this.getPlayer();
        Location from = player.getLocation();

        // double x = d0;
        // double y = d1;
        // double z = d2;
        // float yaw = f;
        // float pitch = f1;
        
        Vec3 poss = pos.position();
        double x = poss.x;
        double y = poss.x;
        double z = poss.x;
        
        float yaw = pos.yRot();
        float pitch = pos.xRot();

        Location to = new Location(this.getPlayer().getWorld(), x, y, z, yaw, pitch);
        // SPIGOT-5171: Triggered on join
        if (from.equals(to)) {
        	this.cardboard$internalTeleport(pos, flags);
        	// this.internalTeleport(d0, d1, d2, f, f1, set, false);
            return;
        }

        PlayerTeleportEvent event = new PlayerTeleportEvent(player, from.clone(), to.clone(), PlayerTeleportEvent.TeleportCause.UNKNOWN);
        Bukkit.getPluginManager().callEvent(event);

        /*
        if (event.isCancelled() || !to.equals(event.getTo())) {
            set.clear(); // Can't relative teleport
            to = event.isCancelled() ? event.getFrom() : event.getTo();
            d0 = to.getX();
            d1 = to.getY();
            d2 = to.getZ();
            f = to.getYaw();
            f1 = to.getPitch();
        }
        */
        
        if (event.isCancelled() || !to.equals(event.getTo())) {
        	flags.clear(); // Can't relative teleport
        	
            to = event.isCancelled() ? event.getFrom() : event.getTo();
            pos = new PositionMoveRotation(CraftLocation.toVec3(to), Vec3.ZERO, to.getYaw(), to.getPitch());
        }

        this.cardboard$internalTeleport(pos, flags);
        
        // this.internalTeleport(d0, d1, d2, f, f1, set, false);
        return;
    }

    @Override
    public void cardboard$internalTeleport(Location dest) {
        this.cardboard$internalTeleport(dest.getX(), dest.getY(), dest.getZ(), dest.getYaw(), dest.getPitch());
    }

    @Override
    public void cardboard$internalTeleport(double x, double y, double z, float yRot, float xRot) {
        this.cardboard$internalTeleport(new PositionMoveRotation(new Vec3(x, y, z), Vec3.ZERO, yRot, xRot), Collections.emptySet());
    }

    @Override
    public void cardboard$internalTeleport(PositionMoveRotation posMoveRotation, Set<Relative> relatives) {
        org.spigotmc.AsyncCatcher.catchOp("teleport"); // Paper
        // Paper start - Prevent teleporting dead entities
        if (this.player.isRemoved()) {
            LOGGER.info("Attempt to teleport removed player {} restricted", player.getScoreboardName());
            //if (this.server.isDebugging()) io.papermc.paper.util.TraceUtil.dumpTraceForThread("Attempt to teleport removed player");
            return;
        }
        // Paper end - Prevent teleporting dead entities
        if (Float.isNaN(posMoveRotation.yRot())) {
            posMoveRotation = new PositionMoveRotation(posMoveRotation.position(), posMoveRotation.deltaMovement(), 0, posMoveRotation.xRot());
        }
        if (Float.isNaN(posMoveRotation.xRot())) {
            posMoveRotation = new PositionMoveRotation(posMoveRotation.position(), posMoveRotation.deltaMovement(), posMoveRotation.yRot(), 0);
        }

        this.justTeleported = true;
        // CraftBukkit end
        this.awaitingTeleportTime = this.tickCount;
        if (++this.awaitingTeleport == Integer.MAX_VALUE) {
            this.awaitingTeleport = 0;
        }

        this.player.teleportSetPosition(posMoveRotation, relatives);
        this.awaitingPositionFromClient = this.player.position();
        // CraftBukkit start - update last location
        this.lastPosX = this.awaitingPositionFromClient.x;
        this.lastPosY = this.awaitingPositionFromClient.y;
        this.lastPosZ = this.awaitingPositionFromClient.z;
        this.lastYaw = this.player.getYRot();
        this.lastPitch = this.player.getXRot();
        // CraftBukkit end
        this.send(ClientboundPlayerPositionPacket.of(this.awaitingTeleport, posMoveRotation, relatives));
    }

    /**
     * NOTE:
     * TODO: Move PlayerToggleSneakEvent to onPlayerInput
     */
    @Inject(at = @At("HEAD"), method = "handlePlayerCommand", cancellable = true)
    public void onClientCommand(ServerboundPlayerCommandPacket packetplayinentityaction, CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(packetplayinentityaction, get(), (ServerLevel) this.player.level());
        
        IMixinEntity e = (IMixinEntity) this.player;

        if (e.ic_isRemoved()) return;
        switch (packetplayinentityaction.getAction()) {
            /*
        	case PRESS_SHIFT_KEY:
            case RELEASE_SHIFT_KEY:
                PlayerToggleSneakEvent event = new PlayerToggleSneakEvent(this.getPlayer(), packetplayinentityaction.getMode() == ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY);
                CraftServer.INSTANCE.getPluginManager().callEvent(event);
                if (event.isCancelled()) ci.cancel();
                break;
            */
            case START_SPRINTING:
            case STOP_SPRINTING:
                PlayerToggleSprintEvent e2 = new PlayerToggleSprintEvent(this.getPlayer(), packetplayinentityaction.getAction() == ServerboundPlayerCommandPacket.Action.START_SPRINTING);
                CraftServer.INSTANCE.getPluginManager().callEvent(e2);
                if (e2.isCancelled()) ci.cancel();
                break;
            default:
                break;
        }
    }

    @Inject(at = @At("TAIL"), method = "tick")
    public void decreaseChatSpamField(CallbackInfo ci) {
        for (int spam; (spam = this.messageCooldownBukkit) > 0 && !chatSpamField.compareAndSet((ServerGamePacketListenerImpl)(Object)this, spam, spam - 1); );
    }

    private ServerGamePacketListenerImpl get() {
        return (ServerGamePacketListenerImpl) (Object) this;
    }
    
    

    /**
     * @author Cardboard
     * @reason Bukkit just adds too much for us to not do an Overwrite.
     *         Luckly we can set our priority so other mods will still work  
     */
    /// @Overwrite
    public void onPlayerMove_old(ServerboundMovePlayerPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, (ServerGamePacketListenerImpl)(Object)this, (ServerLevel)this.player.level());
        boolean sfly = false;
        if (sfly/*validateVehicleMove(packet.a(0.0D), packet.isOnGround(0.0D), packet.c(0.0D), packet.a(0.0F), packet.isOnGround(0.0F))*/) {
            //this.disconnect(new ChatMessage("multiplayer.disconnect.invalid_player_movement"));
        } else {
            ServerLevel worldserver = (ServerLevel) this.player.level();

            if (/*!this.player.wonGame &&*/ !this.player.isDeadOrDying()) { // CraftBukkit
                if (this.tickCount == 0) ((ServerGamePacketListenerImpl)(Object)this).resetPosition();

                if (this.awaitingPositionFromClient != null) {
                    if (this.tickCount - this.awaitingTeleportTime > 20) {
                        this.awaitingTeleportTime = this.tickCount;
                        this.teleport(this.awaitingPositionFromClient.x, this.awaitingPositionFromClient.y, this.awaitingPositionFromClient.z, this.player.getYRot(), this.player.getXRot());
                    }
                    this.allowedPlayerTicks = 20; // Bukkit
                } else {
                    this.awaitingTeleportTime = this.tickCount;
                    double d0 = packet.getX(this.player.getX()); // clamp
                    double d1 = packet.getY(this.player.getY());
                    double d2 = packet.getZ(this.player.getZ());
                    float f = Mth.wrapDegrees(packet.getYRot(this.player.getYRot()));
                    float f1 = Mth.wrapDegrees(packet.getXRot(this.player.getXRot()));

                    if (this.player.isPassenger()) {
                        this.player.absSnapTo(this.player.getX(), this.player.getY(), this.player.getZ(), f, f1);
                        //this.player.getWorld().getChunkManager().updatePosition(this.player);
                        worldserver.getChunkSource().move(this.player);
                        this.allowedPlayerTicks = 20; // Bukkit
                    } else {
                        double prevX = player.getX();
                        double prevY = player.getY();
                        double prevZ = player.getZ();
                        float prevYaw = player.getYRot();
                        float prevPitch = player.getXRot();

                        double d3 = this.player.getX();
                        double d4 = this.player.getY();
                        double d5 = this.player.getZ();
                        double d6 = this.player.getY();
                        double d7 = d0 - this.firstGoodX;
                        double d8 = d1 - this.firstGoodY;
                        double d9 = d2 - this.firstGoodZ;
                        double d10 = this.player.getDeltaMovement().lengthSqr();
                        double d11 = d7 * d7 + d8 * d8 + d9 * d9;

                        if (this.player.isSleeping()) {
                            if (d11 > 1.0D) {
                                this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), f, f1);
                            }

                        } else {
                            ++this.receivedMovePacketCount;
                            int i = this.receivedMovePacketCount - this.knownMovePacketCount;

                            // CraftBukkit start - handle custom speeds and skipped ticks
                            this.allowedPlayerTicks += (System.currentTimeMillis() / 50) - this.lastTick;
                            this.allowedPlayerTicks = Math.max(this.allowedPlayerTicks, 1);
                            this.lastTick = (int) (System.currentTimeMillis() / 50);

                            if (i > Math.max(this.allowedPlayerTicks, 5)) {
                                //ServerPlayNetworkHandler.LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)", this.player.getDisplayName().getString(), i);
                                i = 1;
                            }

                         /*   if (packet.hasRot || d11 > 0) {
                                allowedPlayerTicks -= 1;
                            } else {
                                allowedPlayerTicks = 20;
                            }*/
                            double speed;
                            if (player.getAbilities().flying) {
                                speed = player.getAbilities().getFlyingSpeed() * 20f;
                            } else {
                                speed = player.getAbilities().getWalkingSpeed() * 10f;
                            }
                            //double speed = 1;

                            // TODO: 1.21.4
                            /*
                            if (!this.player.isInTeleportationState() && (! this.player.getWorld().getGameRules().getBoolean(GameRules.DISABLE_ELYTRA_MOVEMENT_CHECK) || !this.player.isFallFlying())) {
                                float f2 = this.player.isFallFlying() ? 300.0F : 100.0F;

                                /*if (d11 - d10 > Math.max(f2, Math.pow((double) (org.spigotmc.SpigotConfig.movedTooQuicklyMultiplier * (float) i * speed), 2))) {
                                // CraftBukkit end
                                   // ServerPlayNetworkHandler.LOGGER.warn("{} moved too quickly! {},{},{}", this.player.getDisplayName().getString(), d7, d8, d9);
                                    this.requestTeleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.getYaw(), this.player.getPitch());
                                    return;
                                }* /
                            }*/

                            AABB axisalignedbb = this.player.getBoundingBox();

                            d7 = d0 - this.lastGoodX;
                            d8 = d1 - this.lastGoodY;
                            d9 = d2 - this.lastGoodZ;
                            boolean flag = d8 > 0.0D;

                            if (this.player.onGround() && !packet.isOnGround() && flag) {
                                this.player.jumpFromGround();
                            }

                            this.player.move(MoverType.PLAYER, new Vec3(d7, d8, d9));
                            this.player.setOnGround(packet.isOnGround());
                            double d12 = d8;

                            d7 = d0 - this.player.getX();
                            d8 = d1 - this.player.getY();
                            if (d8 > -0.5D || d8 < 0.5D) {
                                d8 = 0.0D;
                            }

                            d9 = d2 - this.player.getZ();
                            d11 = d7 * d7 + d8 * d8 + d9 * d9;
                            boolean flag1 = false;

                            if (!this.player.isChangingDimension() && d11 > org.spigotmc.SpigotConfig.movedWronglyThreshold && !this.player.isSleeping() && !this.player.isCreative() && !this.player.isSpectator()) { // Spigot
                                flag1 = true;
                            }

                            this.player.absSnapTo(d0, d1, d2, f, f1);
                            if (!this.player.noPhysics && !this.player.isSleeping() && (flag1 && worldserver.noCollision(this.player, axisalignedbb) || this.isEntityCollidingWithAnythingNew(worldserver, this.player, axisalignedbb, d0, d1, d2))) {
                                this.teleport(d3, d4, d5, f, f1);
                            } else {
                                this.player.absSnapTo(prevX, prevY, prevZ, prevYaw, prevPitch);

                                Player player = this.getPlayer();
                                Location from = new Location(player.getWorld(), lastPosX, lastPosY, lastPosZ, lastYaw, lastPitch);
                                Location to = player.getLocation().clone();

                                to.setX( packet.getX( this.player.getX() ) );
                                to.setY( packet.getY( this.player.getY() ) );
                                to.setZ( packet.getZ( this.player.getZ() ) );
                                to.setYaw(packet.getYRot(this.player.getYRot()));
                                to.setPitch(packet.getXRot(this.player.getXRot()));

                                double delta = Math.pow(this.lastPosX - to.getX(), 2) + Math.pow(this.lastPosY - to.getY(), 2) + Math.pow(this.lastPosZ - to.getZ(), 2);
                                float deltaAngle = Math.abs(this.lastYaw - to.getYaw()) + Math.abs(this.lastPitch - to.getPitch());

                                if ((delta > 1f / 256 || deltaAngle > 10f) && !this.player.isDeadOrDying()) {
                                    this.lastPosX = to.getX();
                                    this.lastPosY = to.getY();
                                    this.lastPosZ = to.getZ();
                                    this.lastYaw = to.getYaw();
                                    this.lastPitch = to.getPitch();

                                    if (from.getX() != Double.MAX_VALUE) {
                                        Location oldTo = to.clone();
                                        PlayerMoveEvent event = new PlayerMoveEvent(player, from, to);
                                        Bukkit.getPluginManager().callEvent(event);

                                        if (event.isCancelled()) {
                                            teleport(from);
                                            return;
                                        }

                                        if (!oldTo.equals(event.getTo()) && !event.isCancelled()) {
                                            ((Player)((EntityBridge)this.player).getBukkitEntity()).
                                                    teleport(event.getTo(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                                            return;
                                        }

                                        if (!from.equals(this.getPlayer().getLocation()) && this.justTeleported) {
                                            this.justTeleported = false;
                                            return;
                                        }
                                    }
                                }
                                this.player.absSnapTo(d0, d1, d2, f, f1); // Copied from above

                                boolean flaga = this.player.isFallFlying();
                                boolean flag2 = this.player.verticalCollisionBelow;
                                boolean flag4 = this.player.isAutoSpinAttack();
                                
                                // this.floating = d12 >= -0.03125D && this.player.interactionManager.getGameMode() != GameMode.SPECTATOR && !CraftServer.server.isFlightEnabled() && !this.player.abilities.allowFlying && !this.player.hasStatusEffect(StatusEffects.LEVITATION) && !this.player.isFallFlying() && this.isEntityOnAir((Entity) this.player) && !this.player.isUsingRiptide();
                                this.clientIsFloating = d12 >= -0.03125D && !flag2 && this.player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR && !this.server.allowFlight() && !this.player.getAbilities().mayfly && !this.player.hasEffect(MobEffects.LEVITATION) && !flaga && !flag4 && this.noBlocksAround(this.player);

                                
                                //this.player.getWorld().getChunkManager().updatePosition(this.player);
                                worldserver.getChunkSource().move(this.player);
                                //this.player.handleFall(this.player.getY() - d6, packet.isOnGround());
                                
                                this.player.doCheckFallDamage(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5, packet.isOnGround());

                                
                                if (flag) this.player.fallDistance = 0.0F;

                                this.player.checkMovementStatistics(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5);
                                this.lastGoodX = this.player.getX();
                                this.lastGoodY = this.player.getY();
                                this.lastGoodZ = this.player.getZ();
                            }
                        }
                    }
                }
            }
        }
    }

    @Shadow
    private boolean isEntityCollidingWithAnythingNew(LevelReader world, Entity e, AABB box, double d0, double d1, double d2) {
        return false;
    }


    @Shadow
    public void teleport(double d0, double d1, double d2, float f, float f1) {}

    @Shadow
    private boolean noBlocksAround(Entity entity) {return false;}

    public ServerLevel get_server_world() {
    	return (ServerLevel) this.player.level();
    }
    
    /**
     * @author Cardboard
     * @reason Events
     */
    @Inject(at = @At("HEAD"), method = "handleAnimate", cancellable = true)
    public void onHandSwingBF(ServerboundSwingPacket packet, CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(packet, get(), this.player.level());
        this.player.resetLastActionTime();
        float f1 = this.player.xRot;
        float f2 = this.player.yRot;
        double d0 = this.player.getX();
        double d1 = this.player.getY() + (double) this.player.getEyeHeight();
        double d2 = this.player.getZ();
        Vec3 vec3d = new Vec3(d0, d1, d2);

        float f3 = Mth.cos(-f2 * 0.017453292F - 3.1415927F);
        float f4 = Mth.sin(-f2 * 0.017453292F - 3.1415927F);
        float f5 = -Mth.cos(-f1 * 0.017453292F);
        float f6 = Mth.sin(-f1 * 0.017453292F);
        float f7 = f4 * f5;
        float f8 = f3 * f5;
        double d3 = player.gameMode.getGameModeForPlayer()== GameType.CREATIVE ? 5.0D : 4.5D;
        Vec3 vec3d1 = vec3d.add((double) f7 * d3, (double) f6 * d3, (double) f8 * d3);
        HitResult movingobjectposition = ((ServerLevel)this.player.level()).clip(new ClipContext(vec3d, vec3d1, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

        if (movingobjectposition == null || movingobjectposition.getType() != HitResult.Type.BLOCK)
            CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_AIR, this.player.inventory.getSelectedItem(), InteractionHand.MAIN_HAND);

        // Arm swing animation
        PlayerAnimationEvent event = new PlayerAnimationEvent(this.getPlayer());
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            ci.cancel();
            return;
        }
        this.player.swing(packet.getHand());
        return;
    }

    @Inject(at = @At("HEAD"), method = "handleUseItem", cancellable = true)
    public void onPlayerInteractItemBF(ServerboundUseItemPacket packetplayinblockplace, CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(packetplayinblockplace, get(), this.player.level());
        InteractionHand enumhand = packetplayinblockplace.getHand();
        ItemStack itemstack = this.player.getItemInHand(enumhand);

        this.player.resetLastActionTime();
        if (!itemstack.isEmpty()) {
            float f1 = this.player.xRot;
            float f2 = this.player.yRot;
            double d0 = this.player.getX();
            double d1 = this.player.getY() + (double) this.player.getEyeHeight();
            double d2 = this.player.getZ();
            Vec3 vec3d = new Vec3(d0, d1, d2);

            float f3 = Mth.cos(-f2 * 0.017453292F - 3.1415927F);
            float f4 = Mth.sin(-f2 * 0.017453292F - 3.1415927F);
            float f5 = -Mth.cos(-f1 * 0.017453292F);
            float f6 = Mth.sin(-f1 * 0.017453292F);
            float f7 = f4 * f5;
            float f8 = f3 * f5;
            double d3 = player.gameMode.getGameModeForPlayer()== GameType.CREATIVE ? 5.0D : 4.5D;
            Vec3 vec3d1 = vec3d.add((double) f7 * d3, (double) f6 * d3, (double) f8 * d3);
            HitResult movingobjectposition = ((ServerLevel)this.player.level()).clip(new ClipContext(vec3d, vec3d1, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

            boolean cancelled;
            if (movingobjectposition == null || movingobjectposition.getType() != HitResult.Type.BLOCK) {
                org.bukkit.event.player.PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(this.player, Action.RIGHT_CLICK_AIR, itemstack, enumhand);
                cancelled = event.useItemInHand() == org.bukkit.event.Event.Result.DENY;
            } else {
                if (((ServerPlayerGameModeBridge)player.gameMode).getFiredInteractBF()) {
                    ((ServerPlayerGameModeBridge)player.gameMode).setFiredInteractBF(false);
                    cancelled = ((ServerPlayerGameModeBridge)player.gameMode).getInteractResultBF();
                } else {
                    BlockHitResult movingobjectpositionblock = (BlockHitResult) movingobjectposition;
                    org.bukkit.event.player.PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, movingobjectpositionblock.getBlockPos(), movingobjectpositionblock.getDirection(), itemstack, true, enumhand);
                    cancelled = (event.useItemInHand() == org.bukkit.event.Event.Result.DENY);
                }
            }

            if (cancelled) {
                ((Player)((ServerPlayerBridge)this.player).getBukkitEntity()).updateInventory(); // SPIGOT-2524
                ci.cancel();
                return;
            }
        }
    }

    /**
     * @reason .
     * @author .
     */
    @Overwrite
    public void handleSetCarriedItem(ServerboundSetCarriedItemPacket packetplayinhelditemslot) {
        PacketUtils.ensureRunningOnSameThread(packetplayinhelditemslot, get(), this.player.level());
        if (packetplayinhelditemslot.getSlot() >= 0 && packetplayinhelditemslot.getSlot() < Inventory.getSelectionSize()) {
            PlayerItemHeldEvent event = new PlayerItemHeldEvent(this.getPlayer(), this.player.inventory.selected, packetplayinhelditemslot.getSlot());
            CraftServer.INSTANCE.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                this.send(new ClientboundSetHeldSlotPacket(this.player.inventory.selected));
                this.player.resetLastActionTime();
                return;
            }
            if (this.player.inventory.selected != packetplayinhelditemslot.getSlot() && this.player.getUsedItemHand() == InteractionHand.MAIN_HAND) this.player.stopUsingItem();
            this.player.inventory.selected = packetplayinhelditemslot.getSlot();
            this.player.resetLastActionTime();
        } else {
            System.out.println(this.player.getName().getString() + " tried to set an invalid carried item");
            this.disconnect(Component.nullToEmpty("Invalid hotbar selection (Hacking?)")); // CraftBukkit
        }
    }

    // 1.17 - onPlayerAbilities, 1.18 - onUpdatePlayerAbilities
    @Inject(at = @At("TAIL"), method = "handlePlayerAbilities")
    public void doBukkitEvent_PlayerToggleFlightEvent(ServerboundPlayerAbilitiesPacket packet, CallbackInfo ci) {
        if (this.player.abilities.mayfly && this.player.abilities.flying != packet.isFlying()) {
            PlayerToggleFlightEvent event = new PlayerToggleFlightEvent((Player)(((ServerPlayerBridge)this.player).getBukkitEntity()), packet.isFlying());
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                this.player.abilities.flying = packet.isFlying();
            } else this.player.onUpdateAbilities();
        }
    }

    @Override
    public void handleResourcePackResponse(ServerboundResourcePackPacket packet) {
        super.handleResourcePackResponse(packet);
        int statusOrdinal = packet.action().ordinal();
        PlayerResourcePackStatusEvent event = new PlayerResourcePackStatusEvent(getPlayer(), packet.id(), PlayerResourcePackStatusEvent.Status.values()[statusOrdinal]);
        Bukkit.getPluginManager().callEvent(event);
    }

    // 1.19.2 = closeScreenHandler
    // 1.19.4 = onHandledScreenClosed
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;doCloseContainer()V", shift = At.Shift.BEFORE), method = "handleContainerClose")
    public void doBukkit_InventoryCloseEvent(CallbackInfo ci) {
        if (this.player.isImmobile()) return; // CraftBukkit
        CraftEventFactory.handleInventoryCloseEvent(this.player, InventoryCloseEvent.Reason.UNKNOWN); // CraftBukkit // Paper
    }

    
    @Unique
    public PaperPlayerGameConnection cb$playerGameConnection;
    
    /**
     * @since 1.21.7
     */
	@Override
	public PlayerGameConnection cardboard$playerGameConnection() {
		if (null == cb$playerGameConnection) {
			// TODO: Paper has this on class init
			this.cb$playerGameConnection = new PaperPlayerGameConnection( (ServerGamePacketListenerImpl) (Object) this);
		}
		return this.cb$playerGameConnection;
	}


	@Shadow
	void restartClientLoadTimerAfterRespawn( ) {}

    @Shadow
    @Final
    private static Logger LOGGER;

    @Override
	public void cardboard$spigot_player_respawn() {
		restartClientLoadTimerAfterRespawn();
	}
}
