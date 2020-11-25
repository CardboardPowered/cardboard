package com.javazilla.bukkitfabric.mixin.network;

import static org.bukkit.craftbukkit.CraftServer.server;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.cardboardpowered.impl.util.LazyPlayerSet;
import org.cardboardpowered.impl.util.WaitableImpl;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.Waitable;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.BukkitLogger;
import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinMinecraftServer;
import com.javazilla.bukkitfabric.interfaces.IMixinPlayNetworkHandler;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.javazilla.bukkitfabric.interfaces.IMixinServerPlayerInteractionManager;
import com.javazilla.bukkitfabric.interfaces.IMixinSignBlockEntity;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.options.ChatVisibility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.MessageType;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.HeldItemChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.filter.TextStream;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.RaycastContext;

@SuppressWarnings("deprecation")
@Mixin(value = ServerPlayNetworkHandler.class, priority = 999)
public abstract class MixinServerPlayNetworkHandler implements IMixinPlayNetworkHandler {

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

        PlayerKickEvent event = new PlayerKickEvent(CraftServer.INSTANCE.getPlayer(this.player), reason.asString(), leaveMessage);

        if (CraftServer.INSTANCE.getServer().isRunning())
            CraftServer.INSTANCE.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        reason = new LiteralText(event.getReason());
        final Text reason_final = reason;

        get().connection.send(new DisconnectS2CPacket(reason), (future) -> get().connection.disconnect(reason_final));
        get().onDisconnected(reason);
        get().connection.disableAutoRead();
        get().connection.getClass();
    }

    /**
     * @reason Bukkit
     * @author Bukkit4Fabric
     */
    @Inject(at = @At("HEAD"), method = "executeCommand", cancellable = true)
    public void executeCommand(String string, CallbackInfo ci) {
        BukkitLogger.getLogger().info(this.player.getName().getString() + " issued server command: " + string);
        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(getPlayer(), string, new LazyPlayerSet(CraftServer.server));
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        try {
            boolean b = Bukkit.getServer().dispatchCommand(event.getPlayer(), event.getMessage().substring(1));
            if (b) {
                ci.cancel();
                return;
            }
        } catch (org.bukkit.command.CommandException ex) {
            getPlayer().sendMessage(org.bukkit.ChatColor.RED + "An internal error occurred while attempting to perform this command");
            java.util.logging.Logger.getLogger(ServerPlayNetworkHandler.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    public PlayerImpl getPlayer() {
        return (PlayerImpl) ((IMixinServerEntityPlayer)(Object)this.player).getBukkitEntity();
    }

    @Override
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
                            recipient.sendMessage(txt, MessageType.CHAT, player.getUniqueId());
                } else for (Player recipient : event.getRecipients())
                    recipient.sendMessage(s);
            }
        }
    }

    /**
     * @reason Fixes AsyncChatEvent
     * @author Bukkit4Fabric
     */
    @Overwrite
    public void filterText(String text, Consumer<String> consumer) {
        consumer.accept(text); // Skip filtering so we can stay off the primary server thread.
    }

    /**
     * @reason Bukkit AsyncChat
     * @author Bukkit4Fabric
     */
    @Overwrite
    public void method_31286(String message) {
        if (this.player.removed || this.player.getClientChatVisibility() == ChatVisibility.HIDDEN) {
            this.sendPacket(new GameMessageS2CPacket((new TranslatableText("chat.cannotSend")).formatted(Formatting.RED), MessageType.CHAT, player.getUuid()));
        } else {
            boolean isSync = message.startsWith("/");
            this.player.updateLastActionTime();

            if (isSync)
                get().executeCommand(message);
            else if (message.isEmpty())
                BukkitLogger.getLogger().warning(this.player.getEntityName() + " tried to send an empty message");
            else if (this.player.getClientChatVisibility() == ChatVisibility.SYSTEM) {
                TranslatableText chatmessage = new TranslatableText("chat.cannotSend", new Object[0]);

                chatmessage.getStyle().withColor(Formatting.RED);
                this.sendPacket(new GameMessageS2CPacket(chatmessage, MessageType.CHAT, player.getUuid()));
            } else this.chat(message, true);

            if (chatSpamField.addAndGet((ServerPlayNetworkHandler)(Object)this, 20) > 200 && !server.getPlayerManager().isOperator(this.player.getGameProfile())) {
                if (!isSync) {
                    Waitable<?> waitable = new WaitableImpl(() -> get().disconnect(new TranslatableText("disconnect.spam", new Object[0])));

                    ((IMixinMinecraftServer)(Object)server).getProcessQueue().add(waitable);

                    try {
                        waitable.get();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                } else get().disconnect(new TranslatableText("disconnect.spam", new Object[0]));

            }
        }
    }

    @Override
    public void teleport(Location location) {
        double d0 = location.getX();
        double d1 = location.getY();
        double d2 = location.getZ();
        float f = location.getYaw();
        float f1 = location.getPitch();
        Set<PlayerPositionLookS2CPacket.Flag> set = Collections.emptySet();

        if (Float.isNaN(f)) f = 0;
        if (Float.isNaN(f1)) f1 = 0;

        double d3 = set.contains(PlayerPositionLookS2CPacket.Flag.X) ? this.player.getX() : 0.0D;
        double d4 = set.contains(PlayerPositionLookS2CPacket.Flag.Y) ? this.player.getY() : 0.0D;
        double d5 = set.contains(PlayerPositionLookS2CPacket.Flag.Z) ? this.player.getZ() : 0.0D;
        float f2 = set.contains(PlayerPositionLookS2CPacket.Flag.Y_ROT) ? this.player.yaw : 0.0F;
        float f3 = set.contains(PlayerPositionLookS2CPacket.Flag.X_ROT) ? this.player.pitch : 0.0F;

        this.requestedTeleportPos = new Vec3d(d0, d1, d2);
        if (++this.requestedTeleportId == Integer.MAX_VALUE)
            this.requestedTeleportId = 0;

        this.teleportRequestTick = this.ticks;
        this.player.updatePositionAndAngles(d0, d1, d2, f, f1);
        this.player.networkHandler.sendPacket(new PlayerPositionLookS2CPacket(d0 - d3, d1 - d4, d2 - d5, f - f2, f1 - f3, set, this.requestedTeleportId));
    }

    @Inject(at = @At("HEAD"), method = "onClientCommand", cancellable = true)
    public void onClientCommand(ClientCommandC2SPacket packetplayinentityaction, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packetplayinentityaction, get(), this.player.getServerWorld());
        if (this.player.removed) return;
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

    @Inject(at = @At("TAIL"), method = "onSignUpdate", cancellable = true)
    public void fireSignUpdateEvent(UpdateSignC2SPacket packet, CallbackInfo ci) {
        String[] astring = packet.getText();

        Player player = (Player) ((IMixinServerEntityPlayer)this.player).getBukkitEntity();
        int x = packet.getPos().getX();
        int y = packet.getPos().getY();
        int z = packet.getPos().getZ();
        String[] lines = new String[4];

        for (int i = 0; i < astring.length; ++i)
            lines[i] = Formatting.strip(new LiteralText(Formatting.strip(astring[i])).getString());

        SignChangeEvent event = new SignChangeEvent((org.bukkit.craftbukkit.block.CraftBlock) player.getWorld().getBlockAt(x, y, z), player, lines);
        CraftServer.INSTANCE.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            BlockEntity tileentity = this.player.getServerWorld().getBlockEntity(packet.getPos());
            SignBlockEntity tileentitysign = (SignBlockEntity) tileentity;
            System.arraycopy(org.bukkit.craftbukkit.block.CraftSign.sanitizeLines(event.getLines()), 0, ((IMixinSignBlockEntity)tileentitysign).getTextBF(), 0, 4);
            tileentitysign.editable = false;
         }
    }

    @Inject(at = @At("TAIL"), method = "tick")
    public void decreaseChatSpamField(CallbackInfo ci) {
        for (int spam; (spam = this.messageCooldownBukkit) > 0 && !chatSpamField.compareAndSet((ServerPlayNetworkHandler)(Object)this, spam, spam - 1); );
    }

    private ServerPlayNetworkHandler get() {
        return (ServerPlayNetworkHandler) (Object) this;
    }

    //@Overwrite
    @Inject(at = @At("HEAD"), method = "onPlayerMove", cancellable = true)
    public void onPlayerMove(PlayerMoveC2SPacket packetplayinflying, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packetplayinflying, (ServerPlayNetworkHandler)(Object)this, this.player.getServerWorld());
        if (ServerPlayNetworkHandler.validatePlayerMove(packetplayinflying))
            this.disconnect(new TranslatableText("multiplayer.disconnect.invalid_player_movement"));
        else {
            if (!this.player.notInAnyWorld) {
                if (this.ticks == 0) ((ServerPlayNetworkHandler)(Object)this).syncWithPlayerPosition();
                if (this.requestedTeleportPos != null) {
                    if (this.ticks - this.teleportRequestTick > 20) {
                        this.teleportRequestTick = this.ticks;
                        this.requestTeleport(this.requestedTeleportPos.x, this.requestedTeleportPos.y, this.requestedTeleportPos.z, this.player.yaw, this.player.pitch);
                    }
                    this.allowedPlayerTicks = 20;
                } else {
                    this.teleportRequestTick = this.ticks;
                    if (this.player.hasVehicle()) {
                        this.player.updatePositionAndAngles(this.player.getX(), this.player.getY(), this.player.getZ(), packetplayinflying.getYaw(this.player.yaw), packetplayinflying.getPitch(this.player.pitch));
                        this.player.getServerWorld().getChunkManager().updateCameraPosition(this.player);
                        this.allowedPlayerTicks = 20;
                    } else {
                        double prevX = player.getX();
                        double prevY = player.getY();
                        double prevZ = player.getZ();
                        float prevYaw = player.yaw;
                        float prevPitch = player.pitch;
                        double d0 = this.player.getX();
                        double d1 = this.player.getY();
                        double d2 = this.player.getZ();
                        double d3 = this.player.getY();
                        double d4 = packetplayinflying.getX(this.player.getX());
                        double d5 = packetplayinflying.getY(this.player.getY());
                        double d6 = packetplayinflying.getZ(this.player.getZ());
                        float f = packetplayinflying.getYaw(this.player.yaw);
                        float f1 = packetplayinflying.getPitch(this.player.pitch);
                        double d7 = d4 - this.lastTickX;
                        double d8 = d5 - this.lastTickY;
                        double d9 = d6 - this.lastTickZ;
                        double d10 = this.player.getVelocity().lengthSquared();
                        double d11 = d7 * d7 + d8 * d8 + d9 * d9;

                        if (this.player.isSleeping()) {
                            if (d11 > 1.0D) this.requestTeleport(this.player.getX(), this.player.getY(), this.player.getZ(), packetplayinflying.getYaw(this.player.yaw), packetplayinflying.getPitch(this.player.pitch));
                        } else {
                            ++this.movePacketsCount;
                            int i = this.movePacketsCount - this.lastTickMovePacketsCount;
                            this.allowedPlayerTicks += (System.currentTimeMillis() / 50) - this.lastTick;
                            this.allowedPlayerTicks = Math.max(this.allowedPlayerTicks, 1);
                            this.lastTick = (int) (System.currentTimeMillis() / 50);
                            if (i > Math.max(this.allowedPlayerTicks, 5)) i = 1;

                            if (d11 > 0) allowedPlayerTicks -= 1;
                            else allowedPlayerTicks = 20;

                            double speed = player.abilities.flying ? (player.abilities.getFlySpeed() * 20f) : (player.abilities.getWalkSpeed() * 10f);
                            if (!this.player.isInTeleportationState() && (!this.player.getServerWorld().getGameRules().getBoolean(GameRules.DISABLE_ELYTRA_MOVEMENT_CHECK) || !this.player.isFallFlying())) {
                                float f2 = this.player.isFallFlying() ? 300.0F : 100.0F;
                                if (d11 - d10 > Math.max(f2, Math.pow((double) ((float) i * speed), 2))) {
                                    this.requestTeleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.yaw, this.player.pitch);
                                    ci.cancel();
                                    return;
                                }
                            }
                            d7 = d4 - this.updatedX;
                            d8 = d5 - this.updatedY;
                            d9 = d6 - this.updatedZ;
                            boolean flag = d8 > 0.0D;

                            if (this.player.isOnGround() && !packetplayinflying.isOnGround() && flag) this.player.jump();
                            this.player.move(MovementType.PLAYER, new Vec3d(d7, d8, d9));
                            this.player.setOnGround(packetplayinflying.isOnGround());
                            double d12 = d8;

                            d7 = d4 - this.player.getX();
                            d8 = d5 - this.player.getY();
                            if (d8 > -0.5D || d8 < 0.5D) d8 = 0.0D;
                            d9 = d6 - this.player.getZ();
                            d11 = d7 * d7 + d8 * d8 + d9 * d9;

                            this.player.updatePositionAndAngles(d4, d5, d6, f, f1);

                            // Bukkit - fire PlayerMoveEvent
                            this.player.updatePositionAndAngles(prevX, prevY, prevZ, prevYaw, prevPitch);

                            Player player = this.getPlayer();
                            Location from = new Location(player.getWorld(), lastPosX, lastPosY, lastPosZ, lastYaw, lastPitch);
                            Location to = player.getLocation().clone();

                            to.setX(packetplayinflying.getX(to.getX()));
                            to.setY(packetplayinflying.getY(to.getY()));
                            to.setZ(packetplayinflying.getZ(to.getZ()));
                            to.setYaw(packetplayinflying.getYaw(to.getYaw()));
                            to.setPitch(packetplayinflying.getPitch(to.getPitch()));

                            double delta = Math.pow(this.lastPosX - to.getX(), 2) + Math.pow(this.lastPosY - to.getY(), 2) + Math.pow(this.lastPosZ - to.getZ(), 2);
                            float deltaAngle = Math.abs(this.lastYaw - to.getYaw()) + Math.abs(this.lastPitch - to.getPitch());

                            if ((delta > 1f / 256 || deltaAngle > 10f)) {
                                this.lastPosX = to.getX();
                                this.lastPosY = to.getY();
                                this.lastPosZ = to.getZ();
                                this.lastYaw = to.getYaw();
                                this.lastPitch = to.getPitch();

                                if (from.getX() != Double.MAX_VALUE) {
                                    Location oldTo = to.clone();
                                    PlayerMoveEvent event = new PlayerMoveEvent(player, from, to);
                                    CraftServer.INSTANCE.getPluginManager().callEvent(event);

                                    if (event.isCancelled()) {
                                        teleport(from);
                                        ci.cancel();
                                        return;
                                    }
                                    if (!oldTo.equals(event.getTo()) && !event.isCancelled()) {
                                        ((IMixinServerEntityPlayer)this.player).getBukkitEntity().teleport(event.getTo(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                                        ci.cancel();
                                        return;
                                    }
                                    if (!from.equals(this.getPlayer().getLocation()) && this.justTeleported) {
                                        this.justTeleported = false;
                                        ci.cancel();
                                        return;
                                    }
                                }
                            }
                            this.player.updatePositionAndAngles(d4, d5, d6, f, f1);
                            this.floating = d12 >= -0.03125D && this.player.interactionManager.getGameMode() != GameMode.SPECTATOR && !CraftServer.server.isFlightEnabled() && !this.player.abilities.allowFlying && !this.player.hasStatusEffect(StatusEffects.LEVITATION) && !this.player.isFallFlying() && this.method_29780((Entity) this.player) && !this.player.isUsingRiptide();
                            this.player.getServerWorld().getChunkManager().updateCameraPosition(this.player);
                            this.player.handleFall(this.player.getY() - d3, packetplayinflying.isOnGround());
                            if (flag) this.player.fallDistance = 0.0F;
                            this.player.increaseTravelMotionStats(this.player.getX() - d0, this.player.getY() - d1, this.player.getZ() - d2);
                            this.updatedX = this.player.getX();
                            this.updatedY = this.player.getY();
                            this.updatedZ = this.player.getZ();
                        }
                    }
                }
            }
        }
        //ci.cancel();
       // return;
    }

    @Shadow
    public void requestTeleport(double d0, double d1, double d2, float f, float f1) {}

    @Shadow
    private boolean method_29780(Entity entity) {return false;}

    /**
     * @author BukkitFabricMod
     * @reason Events
     */
    @Inject(at = @At("HEAD"), method = "onHandSwing", cancellable = true)
    public void onHandSwingBF(HandSwingC2SPacket packetplayinarmanimation, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packetplayinarmanimation, get(), this.player.getServerWorld());
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
        HitResult movingobjectposition = ((ServerWorld)this.player.world).raycast(new RaycastContext(vec3d, vec3d1, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));

        if (movingobjectposition == null || movingobjectposition.getType() != HitResult.Type.BLOCK)
            BukkitEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_AIR, this.player.inventory.getMainHandStack(), Hand.MAIN_HAND);

        // Arm swing animation
        PlayerAnimationEvent event = new PlayerAnimationEvent(this.getPlayer());
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;
        this.player.swingHand(packetplayinarmanimation.getHand());

        ci.cancel();
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
            HitResult movingobjectposition = ((ServerWorld)this.player.world).raycast(new RaycastContext(vec3d, vec3d1, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));

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
                this.sendPacket(new HeldItemChangeS2CPacket(this.player.inventory.selectedSlot));
                this.player.updateLastActionTime();
                return;
            }
            if (this.player.inventory.selectedSlot != packetplayinhelditemslot.getSelectedSlot() && this.player.getActiveHand() == Hand.MAIN_HAND) this.player.clearActiveItem();
            this.player.inventory.selectedSlot = packetplayinhelditemslot.getSelectedSlot();
            this.player.updateLastActionTime();
        } else {
            System.out.println(this.player.getName().getString() + " tried to set an invalid carried item");
            this.disconnect(new LiteralText("Invalid hotbar selection (Hacking?)")); // CraftBukkit
        }
    }

}