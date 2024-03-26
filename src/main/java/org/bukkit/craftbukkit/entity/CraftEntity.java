package org.bukkit.craftbukkit.entity;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.javazilla.bukkitfabric.interfaces.IMixinCommandOutput;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;
import com.mojang.brigadier.LiteralMessage;
import me.isaiah.common.entity.IRemoveReason;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage.EntityTracker;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftSound;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent;
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
import org.cardboardpowered.impl.world.WorldImpl;
import org.cardboardpowered.interfaces.IWorldChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.javazilla.bukkitfabric.BukkitFabricMod;
import com.javazilla.bukkitfabric.interfaces.IMixinCommandOutput;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import org.cardboardpowered.impl.entity.AbstractVillagerImpl;
import org.cardboardpowered.impl.entity.AnimalsImpl;
import org.cardboardpowered.impl.entity.ArmorStandImpl;
import org.cardboardpowered.impl.entity.ArrowImpl;
import org.cardboardpowered.impl.entity.CardboardAmbient;
import org.cardboardpowered.impl.entity.CardboardBat;
import org.cardboardpowered.impl.entity.CardboardBlaze;
import org.cardboardpowered.impl.entity.CardboardCat;
import org.cardboardpowered.impl.entity.CardboardCaveSpider;
import org.cardboardpowered.impl.entity.CardboardComplexPart;
import org.cardboardpowered.impl.entity.CardboardDolphin;
import org.cardboardpowered.impl.entity.CardboardDonkey;
import org.cardboardpowered.impl.entity.CardboardDragonPart;
import org.cardboardpowered.impl.entity.CardboardDrowned;
import org.cardboardpowered.impl.entity.CardboardEnderPearl;
import org.cardboardpowered.impl.entity.CardboardEnderdragon;
import org.cardboardpowered.impl.entity.CardboardEvoker;
import org.cardboardpowered.impl.entity.CardboardFirework;
import org.cardboardpowered.impl.entity.CardboardFish;
import org.cardboardpowered.impl.entity.CardboardFishCod;
import org.cardboardpowered.impl.entity.CardboardFishHook;
import org.cardboardpowered.impl.entity.CardboardFishPufferfish;
import org.cardboardpowered.impl.entity.CardboardFishSalmon;
import org.cardboardpowered.impl.entity.CardboardFishTropical;
import org.cardboardpowered.impl.entity.CardboardFlying;
import org.cardboardpowered.impl.entity.CardboardGhast;
import org.cardboardpowered.impl.entity.CardboardGiant;
import org.cardboardpowered.impl.entity.CardboardGuardian;
import org.cardboardpowered.impl.entity.CardboardGuardianElder;
import org.cardboardpowered.impl.entity.CardboardHanging;
import org.cardboardpowered.impl.entity.CardboardHorse;
import org.cardboardpowered.impl.entity.CardboardHusk;
import org.cardboardpowered.impl.entity.CardboardIllager;
import org.cardboardpowered.impl.entity.CardboardIllusioner;
import org.cardboardpowered.impl.entity.CardboardIronGolem;
import org.cardboardpowered.impl.entity.CardboardLlama;
import org.cardboardpowered.impl.entity.CardboardLlamaSpit;
import org.cardboardpowered.impl.entity.CardboardMagmaCube;
import org.cardboardpowered.impl.entity.CardboardMinecart;
import org.cardboardpowered.impl.entity.CardboardMinecartChest;
import org.cardboardpowered.impl.entity.CardboardMinecartFurnace;
import org.cardboardpowered.impl.entity.CardboardMinecartRideable;
import org.cardboardpowered.impl.entity.CardboardMule;
import org.cardboardpowered.impl.entity.CardboardPanda;
import org.cardboardpowered.impl.entity.CardboardPhantom;
import org.cardboardpowered.impl.entity.CardboardPig;
import org.cardboardpowered.impl.entity.CardboardPigZombie;
import org.cardboardpowered.impl.entity.CardboardPillager;
import org.cardboardpowered.impl.entity.CardboardShulker;
import org.cardboardpowered.impl.entity.CardboardSilverfish;
import org.cardboardpowered.impl.entity.CardboardSnowman;
import org.cardboardpowered.impl.entity.CardboardSpellcaster;
import org.cardboardpowered.impl.entity.CardboardSquid;
import org.cardboardpowered.impl.entity.CardboardThrownExpBottle;
import org.cardboardpowered.impl.entity.CardboardThrownPotion;
import org.cardboardpowered.impl.entity.CardboardTntCart;
import org.cardboardpowered.impl.entity.CardboardVex;
import org.cardboardpowered.impl.entity.CardboardVindicator;
import org.cardboardpowered.impl.entity.CardboardWaterMob;
import org.cardboardpowered.impl.entity.CardboardWitch;
import org.cardboardpowered.impl.entity.CardboardWither;
import org.cardboardpowered.impl.entity.ChickenImpl;
import org.cardboardpowered.impl.entity.CowImpl;
import org.cardboardpowered.impl.entity.CreatureImpl;
import org.cardboardpowered.impl.entity.CreeperImpl;
import org.cardboardpowered.impl.entity.EggImpl;
import org.cardboardpowered.impl.entity.EndermanImpl;
import org.cardboardpowered.impl.entity.EndermiteImpl;
import org.cardboardpowered.impl.entity.ExperienceOrbImpl;
import org.cardboardpowered.impl.entity.FallingBlockImpl;
import org.cardboardpowered.impl.entity.ItemEntityImpl;
import org.cardboardpowered.impl.entity.LightningStrikeImpl;
import org.cardboardpowered.impl.entity.LivingEntityImpl;
import org.cardboardpowered.impl.entity.MonsterImpl;
import org.cardboardpowered.impl.entity.MushroomImpl;
import org.cardboardpowered.impl.entity.OcelotImpl;
import org.cardboardpowered.impl.entity.ParrotImpl;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.cardboardpowered.impl.entity.PolarBearImpl;
import org.cardboardpowered.impl.entity.SkeletonImpl;
import org.cardboardpowered.impl.entity.SlimeImpl;
import org.cardboardpowered.impl.entity.SnowballImpl;
import org.cardboardpowered.impl.entity.SpiderImpl;
import org.cardboardpowered.impl.entity.StrayImpl;
import org.cardboardpowered.impl.entity.TntImpl;
import org.cardboardpowered.impl.entity.TridentImpl;
import org.cardboardpowered.impl.entity.TurtleImpl;
import org.cardboardpowered.impl.entity.UnknownEntity;
import org.cardboardpowered.impl.entity.VillagerImpl;
import org.cardboardpowered.impl.entity.VillagerZombieImpl;
import org.cardboardpowered.impl.entity.WanderingTraderImpl;
import org.cardboardpowered.impl.entity.WitherSkeletonImpl;
import org.cardboardpowered.impl.entity.WolfImpl;
import org.cardboardpowered.impl.entity.ZombieImpl;
import org.cardboardpowered.impl.world.WorldImpl;
import org.cardboardpowered.interfaces.IWorldChunk;
import com.mojang.brigadier.LiteralMessage;

import io.papermc.paper.entity.TeleportFlag;
import me.isaiah.common.entity.IEntity;
import me.isaiah.common.entity.IRemoveReason;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.CaveSpiderEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.GiantEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.entity.mob.IllusionerEntity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.mob.SpellcastingIllagerEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.StrayEntity;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.entity.mob.VindicatorEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.CodEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.MuleEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.passive.PufferfishEntity;
import net.minecraft.entity.passive.SalmonEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.LlamaSpitEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class CraftEntity implements Entity, CommandSender, IMixinCommandOutput {

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
        return nms.getName().getString();
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
    	// 1.19.2: addScoreboardTag
    	// 1.19.4: addCommandTag
        return nms.addCommandTag(arg0);
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
        List<net.minecraft.entity.Entity> notchEntityList = nms.getWorld().getOtherEntities(nms, nms.getBoundingBox().expand(x, y, z), null);
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
        return nms.getCommandTags();
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
            this.getHandle().getWorld().sendEntityStatus(getHandle(), type.getData());
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
        return nms.removeCommandTag(arg0);
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
        nms.setPortalCooldown(arg0);
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
    public boolean teleport(Location loc, TeleportCause arg1) {
        loc.checkFinite();

        if (nms.hasPassengers() || !nms.isAlive())
            return false;

        nms.stopRiding();

        if(loc.getWorld() == null || loc.getWorld().equals(getWorld())) {
            nms.updatePositionAndAngles(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
            nms.setHeadYaw(loc.getYaw());
        } else {
            nms.teleport(
		            ((WorldImpl) loc.getWorld()).getHandle(),
                    loc.getX(), loc.getY(), loc.getZ(),
                    EnumSet.allOf(PositionFlag.class),
                    loc.getYaw(), loc.getPitch());
            return true;
        }
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
        return nms.isInsideBubbleColumn();
    }

    @Override
    public boolean isInWaterOrBubbleColumn() {
        // TODO Auto-generated method stub
        return nms.isInsideWaterOrBubbleColumn();
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
        if (com instanceof TextComponent txt) {
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
        ImmutableSet.Builder<Player> players = ImmutableSet.builder();
        ServerWorld world = (ServerWorld) nms.getWorld();
        EntityTracker entityTracker = world.getChunkManager()
                .threadedAnvilChunkStorage.entityTrackers
                .get(this.getEntityId());
        if (entityTracker != null) {

	        for(PlayerAssociatedNetworkHandler connection : entityTracker.listeners) {
		        players.add((Player) ((IMixinServerEntityPlayer) connection.getPlayer()).getBukkitEntity());
	        }
        }

        return players.build();
    }

	@Override
	public @NotNull SpawnCategory getSpawnCategory() {
		// TODO Auto-generated method stub
		return SpawnCategory.MISC;
	}

	@Override
	public boolean isFreezeTickingLocked() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInPowderedSnow() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void lockFreezeTicks(boolean arg0) {
		// TODO Auto-generated method stub
		
	}
	
	// 1.19.2:

	@Override
    public boolean collidesAt(@NotNull Location location) {
        Box aabb = ((IMixinEntity)this.getHandle()).cardboad_getBoundingBoxAt(location.getX(), location.getY(), location.getZ());
        return !this.getHandle().getWorld().isSpaceEmpty(this.getHandle(), aabb);
    }

	@Override
	public @NotNull Sound getSwimHighSpeedSplashSound() {
		return Sound.ENTITY_GENERIC_SWIM; 
	}

	@Override
	public @NotNull Sound getSwimSound() {
		return Sound.ENTITY_GENERIC_SWIM; 
	}

	@Override
	public Sound getSwimSplashSound() {
        return Sound.ENTITY_GENERIC_SWIM; // //CraftSound.getBukkit(this.getHandle().sound);
    }
	
	@Override
	public @NotNull EntityType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public boolean isUnderWater() {
        return this.getHandle().isSubmergedInWater();
    }

	// @Override
	public boolean teleport(@NotNull Location arg0, @NotNull TeleportCause arg1, boolean arg2, boolean arg3) {
		// TODO Auto-generated method stub
		return this.teleport(arg0, arg1);
	}

	@Override
    public boolean wouldCollideUsing(@NotNull BoundingBox boundingBox) {
        Box aabb = new Box(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
        return !this.getHandle().getWorld().isSpaceEmpty(this.getHandle(), aabb);
    }
	
	// 1.19.4:

	// @Override
    public boolean isSneaking() {
        return this.getHandle().isSneaking();
    }

	// @Override
    public void setSneaking(boolean sneak) {
        this.getHandle().setSneaking(sneak);
    }

    public static CraftEntity getEntity(CraftServer server, net.minecraft.entity.Entity entity) {
        /*
         * Order is *EXTREMELY* important -- keep it right! =D
         */
        // CHECKSTYLE:OFF
        if (entity instanceof LivingEntity) {
            // Players
            if (entity instanceof PlayerEntity) {
                if (entity instanceof ServerPlayerEntity) { return new PlayerImpl((ServerPlayerEntity) entity); }
                else { return new CraftHumanEntity((PlayerEntity) entity); }
            }
            // Water Animals
            else if (entity instanceof WaterCreatureEntity) {
                if (entity instanceof SquidEntity) { return new CardboardSquid(server, (SquidEntity) entity); }
                else if (entity instanceof FishEntity) {
                    if (entity instanceof CodEntity) { return new CardboardFishCod(server, (CodEntity) entity); }
                    else if (entity instanceof PufferfishEntity) { return new CardboardFishPufferfish(server, (PufferfishEntity) entity); }
                    else if (entity instanceof SalmonEntity) { return new CardboardFishSalmon(server, (SalmonEntity) entity); }
                    else if (entity instanceof TropicalFishEntity) { return new CardboardFishTropical(server, (TropicalFishEntity) entity); }
                    else { return new CardboardFish(server, (FishEntity) entity); }
                }
                else if (entity instanceof DolphinEntity) { return new CardboardDolphin(server, (DolphinEntity) entity); }
                else { return new CardboardWaterMob(server, (WaterCreatureEntity) entity); }
            }
            else if (entity instanceof PathAwareEntity) {
                // Animals
                if (entity instanceof AnimalEntity) {
                    if (entity instanceof ChickenEntity) { return new ChickenImpl(server, (ChickenEntity) entity); }
                    else if (entity instanceof CowEntity) {
                        if (entity instanceof MooshroomEntity) { return new MushroomImpl(server, (MooshroomEntity) entity); }
                        else { return new CowImpl(server, (CowEntity) entity); }
                    }
                    else if (entity instanceof PigEntity) { return new CardboardPig(server, (PigEntity) entity); }
                    else if (entity instanceof TameableEntity) {
                        if (entity instanceof WolfEntity) { return new WolfImpl(server, (WolfEntity) entity); }
                        else if (entity instanceof CatEntity) { return new CardboardCat(server, (CatEntity) entity); }
                        else if (entity instanceof ParrotEntity) { return new ParrotImpl(server, (ParrotEntity) entity); }
                    }
                    //else if (entity instanceof SheepEntity) { return new CraftSheep(server, (SheepEntity) entity); }
                    else if (entity instanceof AbstractHorseEntity) {
                        if (entity instanceof AbstractDonkeyEntity){
                            if (entity instanceof DonkeyEntity) { return new CardboardDonkey(server, (DonkeyEntity) entity); }
                            else if (entity instanceof MuleEntity) { return new CardboardMule(server, (MuleEntity) entity); }
                            //else if (entity instanceof TraderLlamaEntity) { return new CardboardTraderLlama(server, (TraderLlamaEntity) entity); }
                            else if (entity instanceof LlamaEntity) { return new CardboardLlama(server, (LlamaEntity) entity); }
                        } else if (entity instanceof HorseEntity) { return new CardboardHorse(server, (HorseEntity) entity); }
                        //else if (entity instanceof SkeletonHorseEntity) { return new CraftSkeletonHorse(server, (SkeletonHorseEntity) entity); }
                        //else if (entity instanceof ZombieHorseEntity) { return new CraftZombieHorse(server, (ZombieHorseEntity) entity); }
                    }
                    //else if (entity instanceof RabbitEntity) { return new CraftRabbit(server, (RabbitEntity) entity); }
                    else if (entity instanceof PolarBearEntity) { return new PolarBearImpl(server, (PolarBearEntity) entity); }
                    else if (entity instanceof TurtleEntity) { return new TurtleImpl(server, (TurtleEntity) entity); }
                    else if (entity instanceof OcelotEntity) { return new OcelotImpl(server, (OcelotEntity) entity); }
                    else if (entity instanceof PandaEntity) { return new CardboardPanda(server, (PandaEntity) entity); }
                    //else if (entity instanceof FoxEntity) { return new CraftFox(server, (FoxEntity) entity); }
                    //else if (entity instanceof BeeEntity) { return new CraftBee(server, (BeeEntity) entity); }
                    //else if (entity instanceof HoglinEntity) { return new CraftHoglin(server, (HoglinEntity) entity); }
                    //else if (entity instanceof StriderEntity) { return new CraftStrider(server, (StriderEntity) entity); }
                    else  { return new AnimalsImpl(server, (AnimalEntity) entity); }
                }
                // Monsters
                else if (entity instanceof HostileEntity) {
                    if (entity instanceof ZombieEntity) {
                        if (entity instanceof ZombifiedPiglinEntity) { return new CardboardPigZombie(server, (ZombifiedPiglinEntity) entity); }
                        else if (entity instanceof HuskEntity) { return new CardboardHusk(server, (HuskEntity) entity); }
                        else if (entity instanceof ZombieVillagerEntity) { return new VillagerZombieImpl(server, (ZombieVillagerEntity) entity); }
                        else if (entity instanceof DrownedEntity) { return new CardboardDrowned(server, (DrownedEntity) entity); }
                        else { return new ZombieImpl(server, (ZombieEntity) entity); }
                    }
                    else if (entity instanceof CreeperEntity) { return new CreeperImpl(server, (CreeperEntity) entity); }
                    else if (entity instanceof EndermanEntity) { return new EndermanImpl(server, (EndermanEntity) entity); }
                    else if (entity instanceof SilverfishEntity) { return new CardboardSilverfish(server, (SilverfishEntity) entity); }
                    else if (entity instanceof GiantEntity) { return new CardboardGiant(server, (GiantEntity) entity); }
                    else if (entity instanceof AbstractSkeletonEntity) {
                        if (entity instanceof StrayEntity) { return new StrayImpl(server, (StrayEntity) entity); }
                        else if (entity instanceof WitherSkeletonEntity) { return new WitherSkeletonImpl(server, (WitherSkeletonEntity) entity); }
                        else { return new SkeletonImpl(server, (AbstractSkeletonEntity) entity); }
                    }
                    else if (entity instanceof BlazeEntity) { return new CardboardBlaze(server, (BlazeEntity) entity); }
                    else if (entity instanceof WitchEntity) { return new CardboardWitch(server, (WitchEntity) entity); }
                    else if (entity instanceof WitherEntity) { return new CardboardWither(server, (WitherEntity) entity); }
                    else if (entity instanceof SpiderEntity) {
                        if (entity instanceof CaveSpiderEntity) { return new CardboardCaveSpider(server, (CaveSpiderEntity) entity); }
                        else { return new SpiderImpl(server, (SpiderEntity) entity); }
                    }
                    else if (entity instanceof EndermiteEntity) { return new EndermiteImpl(server, (EndermiteEntity) entity); }
                    else if (entity instanceof GuardianEntity) {
                        if (entity instanceof ElderGuardianEntity) { return new CardboardGuardianElder(server, (ElderGuardianEntity) entity); }
                        else { return new CardboardGuardian(server, (GuardianEntity) entity); }
                    }
                    else if (entity instanceof VexEntity) { return new CardboardVex(server, (VexEntity) entity); }
                    else if (entity instanceof IllagerEntity) {
                        if (entity instanceof SpellcastingIllagerEntity) {;
                            if (entity instanceof EvokerEntity) { return new CardboardEvoker(server, (EvokerEntity) entity); }
                            else if (entity instanceof IllusionerEntity) { return new CardboardIllusioner(server, (IllusionerEntity) entity); }
                            else {  return new CardboardSpellcaster(server, (SpellcastingIllagerEntity) entity); }
                        }
                        else if (entity instanceof VindicatorEntity) { return new CardboardVindicator(server, (VindicatorEntity) entity); }
                        else if (entity instanceof PillagerEntity) { return new CardboardPillager(server, (PillagerEntity) entity); }
                        else { return new CardboardIllager(server, (IllagerEntity) entity); }
                    }
                    //else if (entity instanceof RavagerEntity) { return new CraftRavager(server, (RavagerEntity) entity); }
                    //else if (entity instanceof AbstractPiglinEntity) {
                        //if (entity instanceof PiglinEntity) return new CraftPiglin(server, (PiglinEntity) entity);
                        //else if (entity instanceof PiglinBruteEntity) { return new CraftPiglinBrute(server, (PiglinBruteEntity) entity); }
                        //else { return new CraftPiglinAbstract(server, (AbstractPiglinEntity) entity); }
                    //}
                    //else if (entity instanceof ZoglinEntity) { return new CraftZoglin(server, (ZoglinEntity) entity); }

                    else  { return new MonsterImpl(server, (HostileEntity) entity); }
                }
                else if (entity instanceof GolemEntity) {
                    if (entity instanceof SnowGolemEntity) { return new CardboardSnowman(server, (SnowGolemEntity) entity); }
                    else if (entity instanceof IronGolemEntity) { return new CardboardIronGolem(server, (IronGolemEntity) entity); }
                    else if (entity instanceof ShulkerEntity) { return new CardboardShulker(server, (ShulkerEntity) entity); }
                }
                else if (entity instanceof MerchantEntity) {
                    if (entity instanceof VillagerEntity) { return new VillagerImpl(server, (VillagerEntity) entity); }
                    else if (entity instanceof WanderingTraderEntity) { return new WanderingTraderImpl(server, (WanderingTraderEntity) entity); }
                    else { return new AbstractVillagerImpl(server, (MerchantEntity) entity); }
                }
                else { return new CreatureImpl(server, (PathAwareEntity) entity); }
            }
            // Slimes are a special (and broken) case
            else if (entity instanceof SlimeEntity) {
                if (entity instanceof MagmaCubeEntity) { return new CardboardMagmaCube(server, (MagmaCubeEntity) entity); }
                else { return new SlimeImpl(server, (SlimeEntity) entity); }
            }
            // Flying
            else if (entity instanceof FlyingEntity) {
                if (entity instanceof GhastEntity) { return new CardboardGhast(server, (GhastEntity) entity); }
                else if (entity instanceof PhantomEntity) { return new CardboardPhantom(server, (PhantomEntity) entity); }
                else { return new CardboardFlying(server, (FlyingEntity) entity); }
            }
            else if (entity instanceof EnderDragonEntity) {
                return new CardboardEnderdragon(server, (EnderDragonEntity) entity);
            }
            // Ambient
            else if (entity instanceof AmbientEntity) {
                if (entity instanceof BatEntity) { return new CardboardBat(server, (BatEntity) entity); }
                else { return new CardboardAmbient(server, (AmbientEntity) entity); }
            }
            else if (entity instanceof ArmorStandEntity) { return new ArmorStandImpl(server, (ArmorStandEntity) entity); }
            else  { return new LivingEntityImpl(server, (LivingEntity) entity); }
        }
        else if (entity instanceof EnderDragonPart) {
            EnderDragonPart part = (EnderDragonPart) entity;
            if (part.owner instanceof EnderDragonEntity) { return new CardboardDragonPart(server, (EnderDragonPart) entity); }
            else { return new CardboardComplexPart(server, (EnderDragonPart) entity); }
        }
        else if (entity instanceof ExperienceOrbEntity) { return new ExperienceOrbImpl(server, (ExperienceOrbEntity) entity); }
        //else if (entity instanceof ArrowEntity) { return new CraftTippedArrow(server, (ArrowEntity) entity); }
        //else if (entity instanceof SpectralArrowEntity) { return new CraftSpectralArrow(server, (SpectralArrowEntity) entity); }
        else if (entity instanceof PersistentProjectileEntity) {
            if (entity instanceof TridentEntity) { return new TridentImpl(server, (TridentEntity) entity); }
            else { return new ArrowImpl(server, (PersistentProjectileEntity) entity); }
        }
        //else if (entity instanceof BoatEntity) { return new CraftBoat(server, (BoatEntity) entity); }
        else if (entity instanceof ThrownEntity) {
            if (entity instanceof EggEntity) { return new EggImpl(server, (EggEntity) entity); }
            else if (entity instanceof SnowballEntity) { return new SnowballImpl(server, (SnowballEntity) entity); }
            else if (entity instanceof PotionEntity) { return new CardboardThrownPotion(server, (PotionEntity) entity); }
            else if (entity instanceof EnderPearlEntity) { return new CardboardEnderPearl(server, (EnderPearlEntity) entity); }
            else if (entity instanceof ExperienceBottleEntity) { return new CardboardThrownExpBottle(server, (ExperienceBottleEntity) entity); }
            
        }
        else if (entity instanceof FallingBlockEntity) { return new FallingBlockImpl(server, (FallingBlockEntity) entity); }
        else if (entity instanceof ExplosiveProjectileEntity) {
            //if (entity instanceof SmallFireballEntity) { return new CraftSmallFireball(server, (SmallFireballEntity) entity); }
            //else if (entity instanceof FireballEntity) { return new CraftLargeFireball(server, (FireballEntity) entity); }
           // else if (entity instanceof WitherSkullEntity) { return new CraftWitherSkull(server, (WitherSkullEntity) entity); }
           // else if (entity instanceof DragonFireballEntity) { return new CraftDragonFireball(server, (DragonFireballEntity) entity); }
            //else { return new CraftFireball(server, (ExplosiveProjectileEntity) entity); }
        }
        //else if (entity instanceof EyeOfEnderEntity) { return new CraftEnderSignal(server, (EyeOfEnderEntity) entity); }
        //else if (entity instanceof EndCrystalEntity) { return new CraftEnderCrystal(server, (EndCrystalEntity) entity); }
        else if (entity instanceof FishingBobberEntity) { return new CardboardFishHook(server, (FishingBobberEntity) entity); }
        else if (entity instanceof ItemEntity) { return new ItemEntityImpl(server, (ItemEntity) entity); }
        else if (entity instanceof LightningEntity) { return new LightningStrikeImpl(server, (LightningEntity) entity); }
        else if (entity instanceof AbstractMinecartEntity) {
            if (entity instanceof FurnaceMinecartEntity) { return new CardboardMinecartFurnace(server, (FurnaceMinecartEntity) entity); }
            else if (entity instanceof ChestMinecartEntity) { return new CardboardMinecartChest(server, (ChestMinecartEntity) entity); }
            else if (entity instanceof TntMinecartEntity) { return new CardboardTntCart(server, (TntMinecartEntity) entity); }
            //else if (entity instanceof HopperMinecartEntity) { return new CraftMinecartHopper(server, (HopperMinecartEntity) entity); }
            //else if (entity instanceof SpawnerMinecartEntity) { return new CraftMinecartMobSpawner(server, (SpawnerMinecartEntity) entity); }
            else if (entity instanceof MinecartEntity) { return new CardboardMinecartRideable(server, (MinecartEntity) entity); }
            //else if (entity instanceof CommandBlockMinecartEntity) { return new CraftMinecartCommand(server, (CommandBlockMinecartEntity) entity); }*/
            else return new CardboardMinecart(server, (AbstractMinecartEntity) entity);
        } else if (entity instanceof AbstractDecorationEntity) {
            //if (entity instanceof PaintingEntity) { return new CraftPainting(server, (PaintingEntity) entity); }
            //else if (entity instanceof ItemFrameEntity) { return new CraftItemFrame(server, (ItemFrameEntity) entity); }
            //else if (entity instanceof LeashKnotEntity) { return new CraftLeash(server, (LeashKnotEntity) entity); }
            //else { return new CraftHanging(server, (AbstractDecorationEntity) entity); }
            return new CardboardHanging(server, (AbstractDecorationEntity) entity);
        }
        else if (entity instanceof TntEntity) { return new TntImpl(server, (TntEntity) entity); }
        else if (entity instanceof FireworkRocketEntity) {return new CardboardFirework(server, (FireworkRocketEntity) entity); }
        //else if (entity instanceof ShulkerBulletEntity) { return new CraftShulkerBullet(server, (ShulkerBulletEntity) entity); }
        //else if (entity instanceof AreaEffectCloudEntity) { return new CraftAreaEffectCloud(server, (AreaEffectCloudEntity) entity); }
        //else if (entity instanceof EvokerFangsEntity) { return new CraftEvokerFangs(server, (EvokerFangsEntity) entity); }
        else if (entity instanceof LlamaSpitEntity) { return new CardboardLlamaSpit(server, (LlamaSpitEntity) entity); }
        // CHECKSTYLE:ON

        
        return (entity instanceof net.minecraft.entity.LivingEntity) ? new LivingEntityImpl(entity) : new UnknownEntity(entity); // TODO
        //throw new AssertionError("Unknown entity " + (entity == null ? null : entity.getClass()));
    }

	// TODO 1.19.4
	@Override
    public boolean teleport(Location location, TeleportCause cause, TeleportFlag ... flags) {
        Preconditions.checkArgument((location != null ? 1 : 0) != 0, (Object)"location cannot be null");
        location.checkFinite();
        Set<TeleportFlag> flagSet = Set.of(flags);
        boolean dismount = !flagSet.contains(TeleportFlag.EntityState.RETAIN_VEHICLE);
        boolean ignorePassengers = flagSet.contains(TeleportFlag.EntityState.RETAIN_PASSENGERS);
        if (flagSet.contains(TeleportFlag.EntityState.RETAIN_PASSENGERS) && this.nms.hasPassengers() && location.getWorld() != this.getWorld()) {
            return false;
        }
        if (!dismount && this.nms.hasVehicle() && location.getWorld() != this.getWorld()) {
            return false;
        }
        if (!ignorePassengers && this.nms.hasPassengers() || this.nms.isRemoved()) {
            return false;
        }
        if (dismount) {
            this.nms.stopRiding();
        }
        if (location.getWorld() != null && !location.getWorld().equals(this.getWorld())) {
            // Preconditions.checkState((!this.nms.generation ? 1 : 0) != 0, (Object)"Cannot teleport entity to an other world during world generation");
            // TODO
        	// this.nms.teleportTo(((WorldImpl)location.getWorld()).getHandle(), CraftLocation.toPosition(location));
            return true;
        }
        this.nms.refreshPositionAndAngles(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        this.nms.setHeadYaw(location.getYaw());
        return true;
    }

	@Override
	public boolean isVisibleByDefault() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void setVisibleByDefault(boolean arg0) {
		// TODO Auto-generated method stub
	}

}
