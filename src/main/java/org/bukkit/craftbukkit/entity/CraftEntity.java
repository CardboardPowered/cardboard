package org.bukkit.craftbukkit.entity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.javazilla.bukkitfabric.BukkitFabricMod;
import com.javazilla.bukkitfabric.interfaces.IMixinCommandOutput;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;
import org.cardboardpowered.interfaces.IWorldChunk;
import com.mojang.brigadier.LiteralMessage;

import me.isaiah.common.entity.IEntity;
import me.isaiah.common.entity.IRemoveReason;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public abstract class CraftEntity implements Entity, CommandSender, IMixinCommandOutput {

    protected static PermissibleBase perm;
    private static final CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY = new CraftPersistentDataTypeRegistry();

    public net.minecraft.entity.Entity nms;
    private final CraftPersistentDataContainer persistentDataContainer = new CraftPersistentDataContainer(DATA_TYPE_REGISTRY);

    protected final CraftServer server = CraftServer.INSTANCE;

    public CraftEntity(net.minecraft.entity.Entity entity) {
        this.nms = entity;
    }

    public net.minecraft.entity.Entity getHandle() {
        return nms;
    }

    @Override
    public List<MetadataValue> getMetadata(String metadataKey) {
        return server.getEntityMetadata().getMetadata(this, metadataKey);
    }

    @Override
    public boolean hasMetadata(String metadataKey) {
        return server.getEntityMetadata().hasMetadata(this, metadataKey);
    }

    @Override
    public void removeMetadata(String metadataKey, Plugin owningPlugin) {
        server.getEntityMetadata().removeMetadata(this, metadataKey, owningPlugin);
    }

    @Override
    public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
        server.getEntityMetadata().setMetadata(this, metadataKey, newMetadataValue);
    }

    @Override
    public String getName() {
        return nms.getEntityName();
    }

    @Override
    public void sendMessage(String message) {
    	
    	me.isaiah.common.cmixin.IMixinEntity e = (me.isaiah.common.cmixin.IMixinEntity) nms;
    	e.IsendText(Text.of(message), UUID.randomUUID());
    }

    @Override
    public void sendMessage(String[] arg0) {
        for (String str : arg0)
            sendMessage(str);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin arg0) {
        return getPermissibleBase().addAttachment(arg0);
    }

    @Override
    public  PermissionAttachment addAttachment(Plugin arg0, int arg1) {
        return getPermissibleBase().addAttachment(arg0, arg1);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2) {
        return getPermissibleBase().addAttachment(arg0, arg1, arg2);
    }

    @Override
    public  PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2, int arg3) {
        return getPermissibleBase().addAttachment(arg0, arg1, arg2, arg3);
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return getPermissibleBase().getEffectivePermissions();
    }

    @Override
    public boolean hasPermission(String arg0) {
        return getPermissibleBase().hasPermission(arg0);
    }

    @Override
    public boolean hasPermission(Permission arg0) {
        return getPermissibleBase().hasPermission(arg0);
    }

    @Override
    public boolean isPermissionSet(String arg0) {
        return getPermissibleBase().isPermissionSet(arg0);
    }

    @Override
    public boolean isPermissionSet(Permission arg0) {
        return getPermissibleBase().isPermissionSet(arg0);
    }

    @Override
    public void recalculatePermissions() {
        getPermissibleBase().recalculatePermissions();
    }

    @Override
    public void removeAttachment(PermissionAttachment arg0) {
        getPermissibleBase().removeAttachment(arg0);
    }

    @Override
    public boolean isOp() {
        return getPermissibleBase().isOp();
    }

    @Override
    public void setOp(boolean arg0) {
        getPermissibleBase().setOp(arg0);
    }

    @Override
    public String getCustomName() {
        return nms.getCustomName().getString();
    }

    @Override
    public void setCustomName(String name) {
        nms.setCustomName(Texts.toText(new LiteralMessage(name)));
    }

    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        return persistentDataContainer;
    }

    @Override
    public boolean addPassenger(Entity arg0) {
        return ((CraftEntity) arg0).getHandle().startRiding(getHandle(), true);
    }

    @Override
    public boolean addScoreboardTag(String arg0) {
        return nms.addScoreboardTag(arg0);
    }

    @Override
    public boolean eject() {
        if (isEmpty()) return false;
        nms.removeAllPassengers();
        return true;
    }

    @Override
    public BoundingBox getBoundingBox() {
        Box b = nms.getBoundingBox();
        return new BoundingBox(b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ);
    }

    @Override
    public int getEntityId() {
        return nms.getId();
    }

    @Override
    public BlockFace getFacing() {
        return CraftBlock.notchToBlockFace(nms.getMovementDirection());
    }

    @Override
    public float getFallDistance() {
        return nms.fallDistance;
    }

    @Override
    public int getFireTicks() {
        return nms.fireTicks;
    }

    @Override
    public double getHeight() {
        return nms.getHeight();
    }

    @Override
    public EntityDamageEvent getLastDamageCause() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Location getLocation() {
        return new Location(getWorld(), nms.getX(), nms.getY(), nms.getZ(), nms.yaw, nms.pitch);
    }

    @Override
    public Location getLocation(Location loc) {
        if (loc != null) {
            loc.setWorld(getWorld());
            loc.setX(nms.getX());
            loc.setY(nms.getY());
            loc.setZ(nms.getZ());
            loc.setYaw(nms.yaw);
            loc.setPitch(nms.pitch);
        }
        return loc;
    }

    @Override
    public int getMaxFireTicks() {
        return nms.getBurningDuration();
    }

    @Override
    public List<org.bukkit.entity.Entity> getNearbyEntities(double x, double y, double z) {
        List<net.minecraft.entity.Entity> notchEntityList = nms.world.getOtherEntities(nms, nms.getBoundingBox().expand(x, y, z), null);
        List<org.bukkit.entity.Entity> bukkitEntityList = new java.util.ArrayList<org.bukkit.entity.Entity>(notchEntityList.size());

        for (net.minecraft.entity.Entity e : notchEntityList)
            bukkitEntityList.add(((IMixinEntity)e).getBukkitEntity());
        return bukkitEntityList;
    }

    @Override
    public Entity getPassenger() {
        return isEmpty() ? null : ((IMixinEntity)getHandle().getFirstPassenger()).getBukkitEntity();
    }

    @Override
    public List<Entity> getPassengers() {
        return Lists.newArrayList(Lists.transform(getHandle().getPassengerList(), new Function<net.minecraft.entity.Entity, org.bukkit.entity.Entity>() {
            @Override
            public org.bukkit.entity.Entity apply(net.minecraft.entity.Entity input) {
                return ((IMixinEntity)input).getBukkitEntity();
            }
        }));
    }

    @SuppressWarnings("deprecation")
    @Override
    public PistonMoveReaction getPistonMoveReaction() {
        return PistonMoveReaction.getById(nms.getPistonBehavior().ordinal());
    }

    @Override
    public int getPortalCooldown() {
        return nms.getDefaultPortalCooldown();
    }

    @Override
    public Pose getPose() {
        return Pose.values()[nms.getPose().ordinal()];
    }

    @Override
    public Set<String> getScoreboardTags() {
        return nms.scoreboardTags;
    }

    @Override
    public Server getServer() {
        return Bukkit.getServer();
    }

    @Override
    public int getTicksLived() {
        return nms.age;
    }

    @Override
    public UUID getUniqueId() {
        return nms.getUuid();
    }

    @Override
    public Entity getVehicle() {
        if (!isInsideVehicle())
            return null;
        return ((IMixinEntity)nms.getVehicle()).getBukkitEntity();
    }

    @Override
    public Vector getVelocity() {
        Vec3d vec3d = nms.getVelocity();
        return new Vector(vec3d.x, vec3d.y, vec3d.z);
    }

    @Override
    public double getWidth() {
        return nms.getWidth();
    }

    @Override
    public World getWorld() {
        return ((IMixinWorld)nms.getEntityWorld()).getWorldImpl();
    }

    @Override
    public boolean hasGravity() {
        return !nms.hasNoGravity();
    }

    @Override
    public boolean isCustomNameVisible() {
        return nms.isCustomNameVisible();
    }

    @Override
    public boolean isDead() {
        return !nms.isAlive();
    }

    @Override
    public boolean isEmpty() {
        return !nms.hasPassengers();
    }

    @Override
    public boolean isGlowing() {
        return nms.isGlowing();
    }

    @Override
    public boolean isInsideVehicle() {
        return nms.hasVehicle();
    }

    @Override
    public boolean isInvulnerable() {
        return nms.isInvulnerable();
    }

    @Override
    public boolean isOnGround() {
        if (nms instanceof ProjectileEntity)
            return ((ProjectileEntity) nms).isOnGround();

        return nms.isOnGround();
    }

    @Override
    public boolean isPersistent() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSilent() {
        return nms.isSilent();
    }

    @Override
    public boolean isValid() {
        return nms.isAlive();
    }

    @Override
    public boolean leaveVehicle() {
        if (!isInsideVehicle())
            return false;
        nms.stopRiding();
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void playEffect(EntityEffect type) {
        if (type.getApplicable().isInstance(this))
            this.getHandle().world.sendEntityStatus(getHandle(), type.getData());
    }

    @Override
    public void remove() {
        me.isaiah.common.cmixin.IMixinEntity common = (me.isaiah.common.cmixin.IMixinEntity)this.nms;
        common.Iremove(IRemoveReason.DISCARDED);
    }

    @Override
    public boolean removePassenger(Entity passenger) {
        ((CraftEntity) passenger).getHandle().stopRiding();
        return true;
    }

    @Override
    public boolean removeScoreboardTag(String arg0) {
        return nms.removeScoreboardTag(arg0);
    }

    @Override
    public void setCustomNameVisible(boolean arg0) {
        nms.setCustomNameVisible(arg0);
    }

    @Override
    public void setFallDistance(float arg0) {
        nms.fallDistance = arg0;
    }

    @Override
    public void setFireTicks(int arg0) {
        nms.setFireTicks(arg0);
    }

    @Override
    public void setGlowing(boolean arg0) {
        nms.setGlowing(arg0);
    }

    @Override
    public void setGravity(boolean arg0) {
        nms.setNoGravity(!arg0);
    }

    @Override
    public void setInvulnerable(boolean arg0) {
        nms.setInvulnerable(arg0);
    }

    @Override
    public void setLastDamageCause(EntityDamageEvent arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean setPassenger(org.bukkit.entity.Entity passenger) {
        Preconditions.checkArgument(!this.equals(passenger), "Entity cannot ride itself.");
        if (passenger instanceof CraftEntity) {
            eject();
            return ((CraftEntity) passenger).getHandle().startRiding(getHandle());
        } else return false;
    }

    @Override
    public void setPersistent(boolean arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setPortalCooldown(int arg0) {
        //nms.netherPortalCooldown = arg0;
    }

    @Override
    public void setRotation(float yaw, float pitch) {
        yaw = Location.normalizeYaw(yaw);
        pitch = Location.normalizePitch(pitch);

        nms.setYaw(yaw);
        nms.setPitch(pitch);
        nms.prevYaw = yaw;
        nms.prevPitch = pitch;
        nms.setHeadYaw(yaw);
    }

    @Override
    public void setSilent(boolean arg0) {
        nms.setSilent(arg0);
    }

    @Override
    public void setTicksLived(int arg0) {
        nms.age = arg0;
    }

    @Override
    public void setVelocity(Vector vec) {
        nms.setVelocity(new Vec3d(vec.getX(), vec.getY(), vec.getZ()));
        nms.velocityModified = true;
    }

    @Override
    public boolean teleport(Location arg0) {
        return teleport(arg0, TeleportCause.PLUGIN);
    }

    @Override
    public boolean teleport(Entity arg0) {
        return teleport(arg0, TeleportCause.PLUGIN);
    }

    @Override
    public boolean teleport(Location location, TeleportCause arg1) {
        location.checkFinite();

        if (nms.hasPassengers() || nms.isRemoved())
            return false;

        nms.stopRiding();

        // TODO: Cross world teleporting
        //if (!location.getWorld().equals(getWorld())) {
        //    nms.teleportTo(((WorldImpl) location.getWorld()).getHandle(), new BlockPos(location.getX(), location.getY(), location.getZ()));
        //    return true;
        //}
        nms.teleport(location.getX(), location.getY(), location.getZ());

        nms.updatePositionAndAngles(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        nms.setHeadYaw(location.getYaw());

        return true;
    }

    @Override
    public boolean teleport(Entity arg0, TeleportCause arg1) {
        return teleport(arg0.getLocation(), arg1);
    }

    @Override
    public CommandSender getBukkitSender(ServerCommandSource serverCommandSource) {
        return this;
    }

    public static PermissibleBase getPermissibleBase() {
        if (perm == null) {
            perm = new PermissibleBase(new ServerOperator() {

                @Override
                public boolean isOp() {
                    return false;
                }

                @Override
                public void setOp(boolean value) {
                }
            });
        }
        return perm;
    }

    private final Entity.Spigot spigot = new Entity.Spigot(){

        @Override
        public void sendMessage(net.md_5.bungee.api.chat.BaseComponent component){
        }

        @Override
        public void sendMessage(net.md_5.bungee.api.chat.BaseComponent... components) {
        }
    };

    @Override
    public org.bukkit.entity.Entity.Spigot spigot() {
        return spigot;
    }

    public NbtCompound save() {
        NbtCompound nbttagcompound = new NbtCompound();

        nbttagcompound.putString("id", getHandle().getSavedEntityId());
        getHandle().writeNbt(nbttagcompound);

        return nbttagcompound;
    }

    // SPIGOT-759
    public void sendMessage(UUID sender, String message) {
        this.sendMessage(message);
    }

    // SPIGOT-759
    public void sendMessage(UUID sender, String[] messages) {
        this.sendMessage(messages);
    }

    // PaperAPI - START
    public Location getOrigin() {
        Location origin = ((IMixinEntity)getHandle()).getOriginBF();
        return origin == null ? null : origin.clone();
    }

    public boolean isTicking() {
        return true; // TODO: 1.17ify: nms.getEntityWorld().getChunkManager().shouldTickEntity(nms);
    }

    public boolean isInLava() {
        return nms.isInLava();
    }

    public boolean isInWater() {
        return nms.isSubmergedInWater();
    }

    public boolean isInRain() {
        return nms.isBeingRainedOn();
    }

    @Override
    public Chunk getChunk() {
        IWorldChunk wc = (IWorldChunk) nms.getEntityWorld().getWorldChunk(nms.getBlockPos());
        return wc.getBukkitChunk();
    }

    @Override
    public SpawnReason getEntitySpawnReason() {
        // TODO Auto-generated method stub
        return SpawnReason.DEFAULT;
    }

    @Override
    public boolean isInBubbleColumn() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInWaterOrBubbleColumn() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInWaterOrRain() {
        // TODO Auto-generated method stub
        return nms.isTouchingWaterOrRain();
    }

    @Override
    public boolean isInWaterOrRainOrBubbleColumn() {
        // TODO Auto-generated method stub
        return nms.isInsideWaterOrBubbleColumn();
    }

    @Override
    public boolean fromMobSpawner() {
        // TODO Auto-generated method stub
        return false;
    }
    // PaperAPI - END

    @Override
    public @Nullable Component customName() {
        return Component.text(this.getCustomName());
    }

    @Override
    public void customName(@Nullable Component com) {
        if (com instanceof TextComponent) {
            TextComponent txt = (TextComponent) com;
            this.setCustomName(txt.content());
        }
    }

    @Override
    public int getFreezeTicks() {
        return nms.getFrozenTicks();
    }

    @Override
    public int getMaxFreezeTicks() {
        return nms.getFrozenTicks();
    }

    @Override
    public boolean isFrozen() {
        return nms.isFrozen();
    }

    @Override
    public boolean isVisualFire() {
        return nms.doesRenderOnFire();
    }

    @Override
    public void setFreezeTicks(int arg0) {
        nms.setFrozenTicks(arg0);
    }

    @Override
    public void setVisualFire(boolean arg0) {
        nms.setOnFire(arg0);
    }
    
    @Override
    public boolean spawnAt(@NotNull Location arg0, @NotNull SpawnReason arg1) {

        return this.spawnAt(arg0);
    }

    @Override
    public Component teamDisplayName() {
        return Component.text(this.getCustomName());
    }

    @Override
    public Component name() {
        return Component.text(getName());
    }
    
    @Override
    public @NotNull Set<Player> getTrackedPlayers() {
        return null;
    }

}