package org.cardboardpowered.mixin.network;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
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
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.cardboardpowered.impl.inventory.CardboardInventoryView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.BukkitFabricMod;
import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinPlayNetworkHandler;
import com.javazilla.bukkitfabric.interfaces.IMixinResourcePackStatusC2SPacket;
import com.javazilla.bukkitfabric.interfaces.IMixinScreenHandler;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.javazilla.bukkitfabric.interfaces.IMixinServerPlayerInteractionManager;
import me.isaiah.common.cmixin.IMixinEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdatePlayerAbilitiesC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.WorldView;

@SuppressWarnings("deprecation")
@Mixin(value = ServerPlayNetworkHandler.class, priority = 800)
public abstract class MixinServerPlayNetworkHandler implements IMixinPlayNetworkHandler {

	@Shadow
	private ClientConnection connection;
	
	@Override
	public ClientConnection cb_get_connection() {
		return connection;
	}

    @Shadow 
    public ServerPlayerEntity player;

    @Shadow
    public abstract void sendPacket(Packet<?> packet);

    private volatile int messageCooldownBukkit;
    private static final AtomicIntegerFieldUpdater<ServerPlayNetworkHandler> chatSpamField = AtomicIntegerFieldUpdater.newUpdater(ServerPlayNetworkHandler.class, "messageCooldownBukkit");

    @Shadow
    public int teleportRequestTick;

    @Shadow
    public int ticks;

    @Shadow
    public Vec3d requestedTeleportPos;

    @Shadow
    public int requestedTeleportId;

    @Shadow public double lastTickX;
    @Shadow public double lastTickY;
    @Shadow public double lastTickZ;
    @Shadow public double updatedX;
    @Shadow public double updatedY;
    @Shadow public double updatedZ;
    @Shadow private boolean floating;
    @Shadow private int movePacketsCount;
    @Shadow private int lastTickMovePacketsCount;

    private int lastTick = 0;
    public int allowedPlayerTicks = 1;
    private double lastPosX = Double.MAX_VALUE;
    private double lastPosY = Double.MAX_VALUE;
    private double lastPosZ = Double.MAX_VALUE;
    private float lastPitch = Float.MAX_VALUE;
    private float lastYaw = Float.MAX_VALUE;
    private boolean justTeleported = false;

    @Override
    public boolean isDisconnected() {
    	return false; // TODO
    }

    /**
     * @author BukkitFabric
     * @reason PlayerKickEvent
     */
    @Overwrite
    public void disconnect(Text reason) {
        String leaveMessage = Formatting.YELLOW + this.player.getEntityName() + " left the game.";

        PlayerKickEvent event = new PlayerKickEvent(CraftServer.INSTANCE.getPlayer(this.player), reason.getString(), leaveMessage);

        if (CraftServer.INSTANCE.getServer().isRunning())
            CraftServer.INSTANCE.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        reason = Text.of(event.getReason());
        final Text reason_final = reason;

        IMixinPlayNetworkHandler im = (IMixinPlayNetworkHandler) get();
        im.cb_get_connection().send(new DisconnectS2CPacket(reason), PacketCallbacks.always(() -> im.cb_get_connection().disconnect(reason_final)));
        get().onDisconnected(reason);
        im.cb_get_connection().disableAutoRead();
        CraftServer.server.submitAndJoin(im.cb_get_connection()::handleDisconnection);
    }

    public PlayerImpl getPlayer() {
        return (PlayerImpl) ((IMixinServerEntityPlayer)(Object)this.player).getBukkitEntity();
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
        requestTeleport(dest.getX(), dest.getY(), dest.getZ(), dest.getYaw(), dest.getPitch(), Collections.emptySet());
    }

    @Overwrite
    public void requestTeleport(double d0, double d1, double d2, float f, float f1, Set<PositionFlag> set) {

    	Player player = this.getPlayer();
        Location from = player.getLocation();

        double x = d0;
        double y = d1;
        double z = d2;
        float yaw = f;
        float pitch = f1;

        Location to = new Location(this.getPlayer().getWorld(), x, y, z, yaw, pitch);
        // SPIGOT-5171: Triggered on join
        if (from.equals(to)) {
            this.internalTeleport(d0, d1, d2, f, f1, set, false);
            return;
        }

        PlayerTeleportEvent event = new PlayerTeleportEvent(player, from.clone(), to.clone(), PlayerTeleportEvent.TeleportCause.UNKNOWN);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled() || !to.equals(event.getTo())) {
            set.clear(); // Can't relative teleport
            to = event.isCancelled() ? event.getFrom() : event.getTo();
            d0 = to.getX();
            d1 = to.getY();
            d2 = to.getZ();
            f = to.getYaw();
            f1 = to.getPitch();
        }

        this.internalTeleport(d0, d1, d2, f, f1, set, false);
        return;
    }

    public void internalTeleport(double d0, double d1, double d2, float f, float f1, Set<PositionFlag> set, boolean shouldDismount_unused) {
        if (Float.isNaN(f)) f = 0.0f;
        if (Float.isNaN(f1)) f1 = 0.0f;
        
        /*BlockPos pos = BlockPos.ofFloored(d0, d1, d2);
        if (!player.getWorld().getBlockState(pos).isAir()) {
            BukkitFabricMod.LOGGER.info("Safe Teleport stopped teleport.");
        }*/

        this.justTeleported = true;
        double d3 = set.contains(PositionFlag.X) ? this.player.getX() : 0.0;
        double d4 = set.contains(PositionFlag.Y) ? this.player.getY() : 0.0;
        double d5 = set.contains(PositionFlag.Z) ? this.player.getZ() : 0.0;
        float f2 = set.contains(PositionFlag.Y_ROT) ? this.player.getYaw() : 0.0f;
        float f3 = set.contains(PositionFlag.X_ROT) ? this.player.getPitch() : 0.0f;

        this.requestedTeleportPos = new Vec3d(d0, d1, d2);
        if (++this.requestedTeleportId == Integer.MAX_VALUE)
            this.requestedTeleportId = 0;

        this.teleportRequestTick = this.ticks;
        this.player.updatePositionAndAngles(d0, d1, d2, f, f1);

        this.player.networkHandler.sendPacket(new PlayerPositionLookS2CPacket(d0 - d3, d1 - d4, d2 - d5, f - f2, f1 - f3, set, this.requestedTeleportId));
    }

    @Inject(at = @At("HEAD"), method = "onClientCommand", cancellable = true)
    public void onClientCommand(ClientCommandC2SPacket packetplayinentityaction, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packetplayinentityaction, get(), (ServerWorld) this.player.getWorld());
        
        IMixinEntity e = (IMixinEntity) this.player;

        if (e.ic_isRemoved()) return;
        switch (packetplayinentityaction.getMode()) {
            case PRESS_SHIFT_KEY:
            case RELEASE_SHIFT_KEY:
                PlayerToggleSneakEvent event = new PlayerToggleSneakEvent(this.getPlayer(), packetplayinentityaction.getMode() == ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY);
                CraftServer.INSTANCE.getPluginManager().callEvent(event);
                if (event.isCancelled()) ci.cancel();
                break;
            case START_SPRINTING:
            case STOP_SPRINTING:
                PlayerToggleSprintEvent e2 = new PlayerToggleSprintEvent(this.getPlayer(), packetplayinentityaction.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING);
                CraftServer.INSTANCE.getPluginManager().callEvent(e2);
                if (e2.isCancelled()) ci.cancel();
                break;
            default:
                break;
        }
    }

    @Inject(at = @At("TAIL"), method = "tick")
    public void decreaseChatSpamField(CallbackInfo ci) {
        for (int spam; (spam = this.messageCooldownBukkit) > 0 && !chatSpamField.compareAndSet((ServerPlayNetworkHandler)(Object)this, spam, spam - 1); );
    }

    private ServerPlayNetworkHandler get() {
        return (ServerPlayNetworkHandler) (Object) this;
    }
    
    

    /**
     * @author Cardboard
     * @reason Bukkit just adds too much for us to not do an Overwrite.
     *         Luckly we can set our priority so other mods will still work  
     */
    @Overwrite
    public void onPlayerMove(PlayerMoveC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, (ServerPlayNetworkHandler)(Object)this, (ServerWorld)this.player.getWorld());
        boolean sfly = false;
        if (sfly/*validateVehicleMove(packet.a(0.0D), packet.isOnGround(0.0D), packet.c(0.0D), packet.a(0.0F), packet.isOnGround(0.0F))*/) {
            //this.disconnect(new ChatMessage("multiplayer.disconnect.invalid_player_movement"));
        } else {
            ServerWorld worldserver = (ServerWorld) this.player.getWorld();

            if (/*!this.player.wonGame &&*/ !this.player.isDead()) { // CraftBukkit
                if (this.ticks == 0) ((ServerPlayNetworkHandler)(Object)this).syncWithPlayerPosition();

                if (this.requestedTeleportPos != null) {
                    if (this.ticks - this.teleportRequestTick > 20) {
                        this.teleportRequestTick = this.ticks;
                        this.requestTeleport(this.requestedTeleportPos.x, this.requestedTeleportPos.y, this.requestedTeleportPos.z, this.player.getYaw(), this.player.getPitch());
                    }
                    this.allowedPlayerTicks = 20; // Bukkit
                } else {
                    this.teleportRequestTick = this.ticks;
                    double d0 = packet.getX(this.player.getX()); // clamp
                    double d1 = packet.getY(this.player.getY());
                    double d2 = packet.getZ(this.player.getZ());
                    float f = MathHelper.wrapDegrees(packet.getYaw(this.player.getYaw()));
                    float f1 = MathHelper.wrapDegrees(packet.getPitch(this.player.getPitch()));

                    if (this.player.hasVehicle()) {
                        this.player.updatePositionAndAngles(this.player.getX(), this.player.getY(), this.player.getZ(), f, f1);
                        //this.player.getWorld().getChunkManager().updatePosition(this.player);
                        worldserver.getChunkManager().updatePosition(this.player);
                        this.allowedPlayerTicks = 20; // Bukkit
                    } else {
                        double prevX = player.getX();
                        double prevY = player.getY();
                        double prevZ = player.getZ();
                        float prevYaw = player.getYaw();
                        float prevPitch = player.getPitch();

                        double d3 = this.player.getX();
                        double d4 = this.player.getY();
                        double d5 = this.player.getZ();
                        double d6 = this.player.getY();
                        double d7 = d0 - this.lastTickX;
                        double d8 = d1 - this.lastTickY;
                        double d9 = d2 - this.lastTickZ;
                        double d10 = this.player.getVelocity().lengthSquared();
                        double d11 = d7 * d7 + d8 * d8 + d9 * d9;

                        if (this.player.isSleeping()) {
                            if (d11 > 1.0D) {
                                this.requestTeleport(this.player.getX(), this.player.getY(), this.player.getZ(), f, f1);
                            }

                        } else {
                            ++this.movePacketsCount;
                            int i = this.movePacketsCount - this.lastTickMovePacketsCount;

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
                                speed = player.getAbilities().getFlySpeed() * 20f;
                            } else {
                                speed = player.getAbilities().getWalkSpeed() * 10f;
                            }
                            //double speed = 1;

                            if (!this.player.isInTeleportationState() && (! this.player.getWorld().getGameRules().getBoolean(GameRules.DISABLE_ELYTRA_MOVEMENT_CHECK) || !this.player.isFallFlying())) {
                                float f2 = this.player.isFallFlying() ? 300.0F : 100.0F;

                                /*if (d11 - d10 > Math.max(f2, Math.pow((double) (org.spigotmc.SpigotConfig.movedTooQuicklyMultiplier * (float) i * speed), 2))) {
                                // CraftBukkit end
                                   // ServerPlayNetworkHandler.LOGGER.warn("{} moved too quickly! {},{},{}", this.player.getDisplayName().getString(), d7, d8, d9);
                                    this.requestTeleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.getYaw(), this.player.getPitch());
                                    return;
                                }*/
                            }

                            Box axisalignedbb = this.player.getBoundingBox();

                            d7 = d0 - this.updatedX;
                            d8 = d1 - this.updatedY;
                            d9 = d2 - this.updatedZ;
                            boolean flag = d8 > 0.0D;

                            if (this.player.isOnGround() && !packet.isOnGround() && flag) {
                                this.player.jump();
                            }

                            this.player.move(MovementType.PLAYER, new Vec3d(d7, d8, d9));
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

                            if (!this.player.isInTeleportationState() && d11 > org.spigotmc.SpigotConfig.movedWronglyThreshold && !this.player.isSleeping() && !this.player.isCreative() && !this.player.isSpectator()) { // Spigot
                                flag1 = true;
                            }

                            this.player.updatePositionAndAngles(d0, d1, d2, f, f1);
                            if (!this.player.noClip && !this.player.isSleeping() && (flag1 && worldserver.isSpaceEmpty(this.player, axisalignedbb) || this.isPlayerNotCollidingWithBlocks(worldserver, axisalignedbb, d0, d1, d2))) {
                                this.requestTeleport(d3, d4, d5, f, f1);
                            } else {
                                this.player.updatePositionAndAngles(prevX, prevY, prevZ, prevYaw, prevPitch);

                                Player player = this.getPlayer();
                                Location from = new Location(player.getWorld(), lastPosX, lastPosY, lastPosZ, lastYaw, lastPitch);
                                Location to = player.getLocation().clone();

                                to.setX( packet.getX( this.player.getX() ) );
                                to.setY( packet.getY( this.player.getY() ) );
                                to.setZ( packet.getZ( this.player.getZ() ) );
                                to.setYaw(packet.getYaw(this.player.getYaw()));
                                to.setPitch(packet.getPitch(this.player.getPitch()));

                                double delta = Math.pow(this.lastPosX - to.getX(), 2) + Math.pow(this.lastPosY - to.getY(), 2) + Math.pow(this.lastPosZ - to.getZ(), 2);
                                float deltaAngle = Math.abs(this.lastYaw - to.getYaw()) + Math.abs(this.lastPitch - to.getPitch());

                                if ((delta > 1f / 256 || deltaAngle > 10f) && !this.player.isDead()) {
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
                                            ((Player)((com.javazilla.bukkitfabric.interfaces.IMixinEntity)this.player).getBukkitEntity()).
                                                    teleport(event.getTo(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                                            return;
                                        }

                                        if (!from.equals(this.getPlayer().getLocation()) && this.justTeleported) {
                                            this.justTeleported = false;
                                            return;
                                        }
                                    }
                                }
                                this.player.updatePositionAndAngles(d0, d1, d2, f, f1); // Copied from above

                                this.floating = d12 >= -0.03125D && this.player.interactionManager.getGameMode() != GameMode.SPECTATOR && !CraftServer.server.isFlightEnabled() && !this.player.abilities.allowFlying && !this.player.hasStatusEffect(StatusEffects.LEVITATION) && !this.player.isFallFlying() && this.isEntityOnAir((Entity) this.player) && !this.player.isUsingRiptide();
                                //this.player.getWorld().getChunkManager().updatePosition(this.player);
                                worldserver.getChunkManager().updatePosition(this.player);
                                //this.player.handleFall(this.player.getY() - d6, packet.isOnGround());
                                
                                this.player.handleFall(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5, packet.isOnGround());

                                
                                if (flag) this.player.fallDistance = 0.0F;

                                this.player.increaseTravelMotionStats(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5);
                                this.updatedX = this.player.getX();
                                this.updatedY = this.player.getY();
                                this.updatedZ = this.player.getZ();
                            }
                        }
                    }
                }
            }
        }
    }

    @Shadow
    private boolean isPlayerNotCollidingWithBlocks(WorldView world, Box box, double d0, double d1, double d2) {
        return false;
    }


    @Shadow
    public void requestTeleport(double d0, double d1, double d2, float f, float f1) {}

    @Shadow
    private boolean isEntityOnAir(Entity entity) {return false;}

    public ServerWorld get_server_world() {
    	return (ServerWorld) this.player.getServerWorld();
    }
    
    /**
     * @author Cardboard
     * @reason Events
     */
    @Inject(at = @At("HEAD"), method = "onHandSwing", cancellable = true)
    public void onHandSwingBF(HandSwingC2SPacket packet, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packet, get(), this.player.getServerWorld());
        this.player.updateLastActionTime();
        float f1 = this.player.pitch;
        float f2 = this.player.yaw;
        double d0 = this.player.getX();
        double d1 = this.player.getY() + (double) this.player.getStandingEyeHeight();
        double d2 = this.player.getZ();
        Vec3d vec3d = new Vec3d(d0, d1, d2);

        float f3 = MathHelper.cos(-f2 * 0.017453292F - 3.1415927F);
        float f4 = MathHelper.sin(-f2 * 0.017453292F - 3.1415927F);
        float f5 = -MathHelper.cos(-f1 * 0.017453292F);
        float f6 = MathHelper.sin(-f1 * 0.017453292F);
        float f7 = f4 * f5;
        float f8 = f3 * f5;
        double d3 = player.interactionManager.getGameMode()== GameMode.CREATIVE ? 5.0D : 4.5D;
        Vec3d vec3d1 = vec3d.add((double) f7 * d3, (double) f6 * d3, (double) f8 * d3);
        HitResult movingobjectposition = ((ServerWorld)this.player.getServerWorld()).raycast(new RaycastContext(vec3d, vec3d1, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));

        if (movingobjectposition == null || movingobjectposition.getType() != HitResult.Type.BLOCK)
            BukkitEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_AIR, this.player.inventory.getMainHandStack(), Hand.MAIN_HAND);

        // Arm swing animation
        PlayerAnimationEvent event = new PlayerAnimationEvent(this.getPlayer());
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            ci.cancel();
            return;
        }
        this.player.swingHand(packet.getHand());
        return;
    }

    @Inject(at = @At("HEAD"), method = "onPlayerInteractItem", cancellable = true)
    public void onPlayerInteractItemBF(PlayerInteractItemC2SPacket packetplayinblockplace, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packetplayinblockplace, get(), this.player.getServerWorld());
        Hand enumhand = packetplayinblockplace.getHand();
        ItemStack itemstack = this.player.getStackInHand(enumhand);

        this.player.updateLastActionTime();
        if (!itemstack.isEmpty()) {
            float f1 = this.player.pitch;
            float f2 = this.player.yaw;
            double d0 = this.player.getX();
            double d1 = this.player.getY() + (double) this.player.getStandingEyeHeight();
            double d2 = this.player.getZ();
            Vec3d vec3d = new Vec3d(d0, d1, d2);

            float f3 = MathHelper.cos(-f2 * 0.017453292F - 3.1415927F);
            float f4 = MathHelper.sin(-f2 * 0.017453292F - 3.1415927F);
            float f5 = -MathHelper.cos(-f1 * 0.017453292F);
            float f6 = MathHelper.sin(-f1 * 0.017453292F);
            float f7 = f4 * f5;
            float f8 = f3 * f5;
            double d3 = player.interactionManager.getGameMode()== GameMode.CREATIVE ? 5.0D : 4.5D;
            Vec3d vec3d1 = vec3d.add((double) f7 * d3, (double) f6 * d3, (double) f8 * d3);
            HitResult movingobjectposition = ((ServerWorld)this.player.getServerWorld()).raycast(new RaycastContext(vec3d, vec3d1, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));

            boolean cancelled;
            if (movingobjectposition == null || movingobjectposition.getType() != HitResult.Type.BLOCK) {
                org.bukkit.event.player.PlayerInteractEvent event = BukkitEventFactory.callPlayerInteractEvent(this.player, Action.RIGHT_CLICK_AIR, itemstack, enumhand);
                cancelled = event.useItemInHand() == org.bukkit.event.Event.Result.DENY;
            } else {
                if (((IMixinServerPlayerInteractionManager)player.interactionManager).getFiredInteractBF()) {
                    ((IMixinServerPlayerInteractionManager)player.interactionManager).setFiredInteractBF(false);
                    cancelled = ((IMixinServerPlayerInteractionManager)player.interactionManager).getInteractResultBF();
                } else {
                    BlockHitResult movingobjectpositionblock = (BlockHitResult) movingobjectposition;
                    org.bukkit.event.player.PlayerInteractEvent event = BukkitEventFactory.callPlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, movingobjectpositionblock.getBlockPos(), movingobjectpositionblock.getSide(), itemstack, true, enumhand);
                    cancelled = (event.useItemInHand() == org.bukkit.event.Event.Result.DENY);
                }
            }

            if (cancelled) {
                ((Player)((IMixinServerEntityPlayer)this.player).getBukkitEntity()).updateInventory(); // SPIGOT-2524
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
    public void onUpdateSelectedSlot(UpdateSelectedSlotC2SPacket packetplayinhelditemslot) {
        NetworkThreadUtils.forceMainThread(packetplayinhelditemslot, get(), this.player.getServerWorld());
        if (packetplayinhelditemslot.getSelectedSlot() >= 0 && packetplayinhelditemslot.getSelectedSlot() < PlayerInventory.getHotbarSize()) {
            PlayerItemHeldEvent event = new PlayerItemHeldEvent(this.getPlayer(), this.player.inventory.selectedSlot, packetplayinhelditemslot.getSelectedSlot());
            CraftServer.INSTANCE.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                this.sendPacket(new UpdateSelectedSlotS2CPacket(this.player.inventory.selectedSlot));
                this.player.updateLastActionTime();
                return;
            }
            if (this.player.inventory.selectedSlot != packetplayinhelditemslot.getSelectedSlot() && this.player.getActiveHand() == Hand.MAIN_HAND) this.player.clearActiveItem();
            this.player.inventory.selectedSlot = packetplayinhelditemslot.getSelectedSlot();
            this.player.updateLastActionTime();
        } else {
            System.out.println(this.player.getName().getString() + " tried to set an invalid carried item");
            this.disconnect(Text.of("Invalid hotbar selection (Hacking?)")); // CraftBukkit
        }
    }

    // 1.17 - onPlayerAbilities, 1.18 - onUpdatePlayerAbilities
    @Inject(at = @At("TAIL"), method = "onUpdatePlayerAbilities")
    public void doBukkitEvent_PlayerToggleFlightEvent(UpdatePlayerAbilitiesC2SPacket packet, CallbackInfo ci) {
        if (this.player.abilities.allowFlying && this.player.abilities.flying != packet.isFlying()) {
            PlayerToggleFlightEvent event = new PlayerToggleFlightEvent((Player)(((IMixinServerEntityPlayer)this.player).getBukkitEntity()), packet.isFlying());
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                this.player.abilities.flying = packet.isFlying();
            } else this.player.sendAbilitiesUpdate();
        }
    }

    @Inject(at = @At("HEAD"), method = "onResourcePackStatus")
    public void doBukkitEvent_PlayerResourcePackStatusEvent(ResourcePackStatusC2SPacket packet, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packet, get(), this.player.getServerWorld());
        Bukkit.getPluginManager().callEvent(new PlayerResourcePackStatusEvent(getPlayer(), PlayerResourcePackStatusEvent.Status.values()[((IMixinResourcePackStatusC2SPacket)packet).getStatus_Bukkit().ordinal()]));
    }

    // 1.19.2 = closeScreenHandler
    // 1.19.4 = onHandledScreenClosed
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;onHandledScreenClosed()V", shift = At.Shift.BEFORE), method = "onCloseHandledScreen")
    public void doBukkit_InventoryCloseEvent(CallbackInfo ci) {
        IMixinScreenHandler handler = (IMixinScreenHandler) player.currentScreenHandler;
        CardboardInventoryView view = handler.getBukkitView();
        view.setPlayerIfNotSet(((IMixinServerEntityPlayer)player).getBukkit());
        InventoryCloseEvent event = new InventoryCloseEvent(view);
        Bukkit.getServer().getPluginManager().callEvent(event);
        handler.transferTo(player.playerScreenHandler, ((IMixinServerEntityPlayer)player).getBukkit());
    }

}