package org.bukkit.craftbukkit.entity;

//<<<<<<< HEAD
//=======
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.damage.CraftDamageSource;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Consumer;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

//>>>>>>> upstream/ver/1.20
import com.destroystokyo.paper.block.TargetBlockInfo;
import com.destroystokyo.paper.block.TargetBlockInfo.FluidMode;
import com.destroystokyo.paper.entity.TargetEntityInfo;
import com.google.common.collect.Sets;
import com.javazilla.bukkitfabric.Utils;

import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.world.damagesource.CombatTracker;

import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.world.entity.LivingEntityBridge;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.TriState;
import net.minecraft.Optionull;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.waypoints.ServerWaypointManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownLingeringPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.waypoints.WaypointStyleAsset;
import net.minecraft.world.waypoints.WaypointStyleAssets;

import org.bukkit.entity.*;
import org.bukkit.craftbukkit.potion.CraftPotionUtil;
import org.bukkit.craftbukkit.inventory.CraftEntityEquipment;
import org.cardboardpowered.impl.world.CraftWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"deprecation", "removal"})
public class CraftLivingEntity extends CraftEntity implements LivingEntity {

    private CraftEntityEquipment equipment;

    public CraftLivingEntity(net.minecraft.world.entity.Entity entity) {
        super(entity);
        this.entity = (net.minecraft.world.entity.LivingEntity) entity;
        if (entity instanceof Mob || entity instanceof ArmorStand) {
            equipment = new CraftEntityEquipment(this);
        }
    }

    public CraftLivingEntity(CraftServer server, net.minecraft.world.entity.Entity entity) {
        this(entity);
    }

    @Override
    public AttributeInstance getAttribute(Attribute att) {
        return ((LivingEntityBridge)this.getHandle()).cardboard_getAttr().getAttribute(att); //.getAttribute(att, nms.getAttributes());
    }

    @Override
    public void registerAttribute(Attribute attribute) {
        ((LivingEntityBridge)this.getHandle()).cardboard_getAttr().registerAttribute(attribute);
    }

    @Override
    public void damage(double arg0) {
        // nms.damage(DamageSource.MAGIC, (float)arg0);
    	damage(arg0, (Entity) null);
    }

    @Override
    public void damage(double arg0, Entity source) {
        // nms.damage(DamageSource.mob((net.minecraft.entity.LivingEntity) arg1), (float) arg0);
    	DamageSource reason = getHandle().damageSources().generic();

        if (source instanceof HumanEntity) {
            reason = getHandle().damageSources().playerAttack(((CraftHumanEntity) source).getHandle());
        } else if (source instanceof LivingEntity) {
            reason = getHandle().damageSources().mobAttack(((CraftLivingEntity) source).getHandle());
        }

        // nms.damage(reason, (float) arg0);
        damage(arg0, reason);
    }
    
    private void damage(double amount, DamageSource damageSource) {
        // Preconditions.checkArgument(damageSource != null, "damageSource cannot be null");
        // Preconditions.checkState(!this.getHandle().generation, "Cannot damage entity during world generation");

        this.getHandle().hurt(damageSource, (float) amount);
    }

    @Override
    public double getAbsorptionAmount() {
        return this.getHandle().getAbsorptionAmount();
    }

    @Override
    public double getHealth() {
        return this.getHandle().getHealth();
    }

    @Override
    public double getMaxHealth() {
        // TODO Auto-generated method stub
        return this.getHandle().getMaxHealth();
    }

    @Override
    public void resetMaxHealth() {
        // TODO Auto-generated method stub
    }

    @Override
    public void setAbsorptionAmount(double arg0) {
        this.getHandle().setAbsorptionAmount((float)arg0);
    }

    @Override
    public void setHealth(double arg0) {
        this.getHandle().setHealth((float) arg0);
    }

    @Override
    public void setMaxHealth(double arg0) {
        // TODO Max health
        this.getHandle().setHealth((float) arg0);
    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> arg0) {
        return launchProjectile(arg0, null);
    }

    @Override
    public net.minecraft.world.entity.LivingEntity getHandle() {
        return (net.minecraft.world.entity.LivingEntity) this.entity;
    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> projectile, Vector velocity) {
    	return this.launchProjectile(projectile, velocity, null);
    }

    @Override
    public boolean addPotionEffect(PotionEffect effect) {
        return addPotionEffect(effect, false);
    }

    @Override
    public boolean addPotionEffect(PotionEffect effect, boolean force) {
        MobEffect type = BuiltInRegistries.MOB_EFFECT.byId(effect.getType().getId());

        me.isaiah.common.cmixin.IMixinEntity ic = ((me.isaiah.common.cmixin.IMixinEntity)(Object) entity);
        ic.IC$add_status_effect(type, effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.hasParticles());
        
        // nms.addStatusEffect(new StatusEffectInstance(type, effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.hasParticles())/*, EntityPotionEffectEvent.Cause.PLUGIN*/);

        return true;
    }

    @Override
    public boolean addPotionEffects(Collection<PotionEffect> effects) {
        boolean success = true;
        for (PotionEffect effect : effects)
            success &= addPotionEffect(effect);
        return success;
    }

    @Override
    public void attack(Entity arg0) {
        this.getHandle().doAutoAttackOnTouch(((CraftLivingEntity)arg0).getHandle());
    }

    @Override
    public Collection<PotionEffect> getActivePotionEffects() {
        List<PotionEffect> effects = new ArrayList<>();
        for (MobEffectInstance handle :  this.getHandle().activeEffects.values()) {
                // effects.add(new PotionEffect(PotionEffectType.getById(Registries.STATUS_EFFECT.getRawId(handle.getEffectType())), handle.getDuration(), handle.getAmplifier(), handle.isAmbient(), handle.shouldShowParticles()));
                effects.add(CraftPotionUtil.toBukkit(handle));
        
        }
        return effects;
    }

    @Override
    public boolean getCanPickupItems() {
        if (getHandle() instanceof Mob) {
            return ((Mob) getHandle()).canPickUpLoot();
        }
        return true; // todo
    }

    @Override
    public EntityEquipment getEquipment() {
        return equipment;
    }

    @Override
    public double getEyeHeight() {
        return entity.getEyeHeight();
    }

    @Override
    public double getEyeHeight(boolean arg0) {
        return getEyeHeight();
    }

    @Override
    public Location getEyeLocation() {
        Location loc = getLocation();
        loc.setY(loc.getY() + getEyeHeight());
        return loc;
    }

    @Override
    public Player getKiller() {
        return Optionull.map(this.getHandle().getLastHurtByPlayer(), player -> (Player) ((EntityBridge)player).getBukkitEntity());
    }


    @Override
    public double getLastDamage() {
        return  this.getHandle().lastHurt;
    }

    @Override
    public List<Block> getLastTwoTargetBlocks(Set<Material> arg0, int arg1) {
        return getLineOfSight(arg0, arg1, 2);
    }

    @Override
    public Entity getLeashHolder() throws IllegalStateException {
        return ((EntityBridge)((Mob) entity).getLeashHolder()).getBukkitEntity();
    }

    private List<Block> getLineOfSight(Set<Material> transparent, int maxDistance, int maxLength) {
        if (transparent == null)
            transparent = Sets.newHashSet(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR);

        if (maxDistance > 120)
            maxDistance = 120;
        ArrayList<Block> blocks = new ArrayList<Block>();
        Iterator<Block> itr = new BlockIterator(this, maxDistance);
        while (itr.hasNext()) {
            Block block = itr.next();
            blocks.add(block);
            if (maxLength != 0 && blocks.size() > maxLength)
                blocks.remove(0);
            Material material = block.getType();
            if (!transparent.contains(material))
                break;
        }
        return blocks;
    }

    @Override
    public List<Block> getLineOfSight(Set<Material> transparent, int maxDistance) {
        return getLineOfSight(transparent, maxDistance, 0);
    }

    @Override
    public int getMaximumAir() {
        return entity.getMaxAirSupply();
    }

    @Override
    public int getMaximumNoDamageTicks() {
    	return 0; // TODO
    	// return this.getHandle().invulnerableDuration;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getMemory(MemoryKey<T> arg0) {
        return (T)  this.getHandle().getBrain().getMemoryInternal(Utils.fromMemoryKey(arg0)).map(Utils::fromNmsGlobalPos).orElse(null);
    }

    @Override
    public int getNoDamageTicks() {
        return entity.invulnerableTime;
    }

    @Override
    public PotionEffect getPotionEffect(PotionEffectType arg0) {
    	
    	me.isaiah.common.cmixin.IMixinEntity ic = ((me.isaiah.common.cmixin.IMixinEntity)(Object) entity);

    	MobEffectInstance handle = ic.IC$get_status_effect(arg0.getId());
    	
    	int typeId = ic.IC$get_status_effect_id(handle);
        return (handle == null) ? null : new PotionEffect(PotionEffectType.getById(typeId), handle.getDuration(), handle.getAmplifier(), handle.isAmbient(), handle.isVisible());
    }

    @Override
    public int getRemainingAir() {
        return entity.getAirSupply();
    }

    @Override
    public boolean getRemoveWhenFarAway() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Block getTargetBlock(Set<Material> arg0, int arg1) {
        List<Block> blocks = getLineOfSight(arg0, arg1, 1);
        return blocks.get(0);
    }

    @Override
    public Block getTargetBlockExact(int maxDistance) {
        return this.getTargetBlockExact(maxDistance, FluidCollisionMode.NEVER);
    }

    @Override
    public Block getTargetBlockExact(int maxDistance, FluidCollisionMode fluidCollisionMode) {
        RayTraceResult hitResult = this.rayTraceBlocks(maxDistance, fluidCollisionMode);
        return (hitResult != null ? hitResult.getHitBlock() : null);
    }

    @Override
    public boolean hasAI() {
        return (this.getHandle() instanceof Mob) ? !((Mob) this.getHandle()).isNoAi() : false;
    }

    @Override
    public boolean hasLineOfSight(Entity arg0) {
        return  this.getHandle().hasLineOfSight(((CraftEntity)arg0).entity);
    }

    @Override
    public boolean hasPotionEffect(PotionEffectType arg0) {
    	me.isaiah.common.cmixin.IMixinEntity ic = ((me.isaiah.common.cmixin.IMixinEntity)(Object) entity);
    	return ic.IC$has_status_effect(BuiltInRegistries.MOB_EFFECT.byId(arg0.getId()));
        // return nms.hasStatusEffect(Registries.STATUS_EFFECT.get(arg0.getId()));
    }

    @Override
    public boolean isCollidable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isGliding() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isLeashed() {
        if (!(getHandle() instanceof Mob))
            return false;
        return ((Mob) getHandle()).getLeashHolder() != null;
    }

    @Override
    public boolean isRiptiding() {
        return  this.getHandle().isAutoSpinAttack();
    }

    @Override
    public boolean isSleeping() {
        return  this.getHandle().isSleeping();
    }

    @Override
    public boolean isSwimming() {
        return entity.isSwimming();
    }

    @Override
    public RayTraceResult rayTraceBlocks(double maxDistance) {
        return this.rayTraceBlocks(maxDistance, FluidCollisionMode.NEVER);
    }

    @Override
    public RayTraceResult rayTraceBlocks(double maxDistance, FluidCollisionMode fluidCollisionMode) {
        Location eyeLocation = this.getEyeLocation();
        Vector direction = eyeLocation.getDirection();
        return this.getWorld().rayTraceBlocks(eyeLocation, direction, maxDistance, fluidCollisionMode, false);
    }

    @Override
    public void removePotionEffect(PotionEffectType type) {
    	me.isaiah.common.cmixin.IMixinEntity ic = ((me.isaiah.common.cmixin.IMixinEntity)(Object) entity);
    	
    	ic.IC$remove_status_effect( BuiltInRegistries.MOB_EFFECT.byId(type.getId()) );
    	
        //nms.removeStatusEffect(Registries.STATUS_EFFECT.get(type.getId())/*, EntityPotionEffectEvent.Cause.PLUGIN*/);
    }

    @Override
    public void setAI(boolean arg0) {
        if (this.getHandle() instanceof Mob)
            ((Mob) this.getHandle()).setNoAi(!arg0);
    }

    @Override
    public void setCanPickupItems(boolean arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setCollidable(boolean arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setGliding(boolean arg0) {
        entity.setSharedFlag(7, arg0);
    }

    @Override
    public void setLastDamage(double arg0) {
        this.getHandle().lastHurt = (float) arg0;
    }

    @Override
    public boolean setLeashHolder(Entity holder) {
        if ((entity instanceof WitherBoss) || !(entity instanceof Mob))
            return false;

        if (holder == null)
            return unleash();

        if (holder.isDead())
            return false;

        unleash();
        ((Mob) entity).setLeashedTo(((CraftEntity) holder).getHandle(), true);
        return true;
    }

    private boolean unleash() {
        if (!isLeashed())
            return false;
        // ((MobEntity) getHandle()).detachLeash(true, false);
        
        ((Mob) getHandle()).dropLeash();
        
        return true;
    }

    @Override
    public void setMaximumAir(int arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setMaximumNoDamageTicks(int arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public <T> void setMemory(MemoryKey<T> arg0, T arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setNoDamageTicks(int arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setRemainingAir(int arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setRemoveWhenFarAway(boolean arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setSwimming(boolean arg0) {
        entity.setSwimming(arg0);
    }

    @Override
    public void swingMainHand() {
        this.getHandle().swing(InteractionHand.MAIN_HAND);
    }

    @Override
    public void swingOffHand() {
        this.getHandle().swing(InteractionHand.OFF_HAND);
    }

    @Override
    public Set<UUID> getCollidableExemptions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EntityCategory getCategory() {
        // TODO Auto-generated method stub
        return EntityCategory.NONE;
    }

    public void setArrowsInBody(int i) {
        // TODO
    }

    public int getArrowsInBody() {
        return -1; // TODO
    }

    public void setArrowCooldown(int i) {}
    public int getArrowCooldown() { return -1; }

    // Spigot-743
    public boolean isInvisible() {
        return getHandle().isInvisible();
    }

    // Spigot-743
    public void setInvisible(boolean invisible) {
        // TODO getHandle().persistentInvisibility = invisible;
        getHandle().setSharedFlag(5, invisible);
    }

    // PaperAPI - start
    public boolean isJumping() {
        return getHandle().jumping;
    }

    public void setJumping(boolean jumping) {
        getHandle().setJumping(jumping);
        if (jumping && getHandle() instanceof Mob)
            ((Mob) getHandle()).getJumpControl().tick();
    }

    @Override
    public boolean fromMobSpawner() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Chunk getChunk() {
        return super.getChunk();
    }

    @Override
    public SpawnReason getEntitySpawnReason() {
        // TODO Auto-generated method stub
        return null;
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
        return false;
    }

    @Override
    public boolean isInWaterOrRainOrBubbleColumn() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void clearActiveItem() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public ItemStack getActiveItem() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getArrowsStuck() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getHandRaisedTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getHurtDirection() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getItemUseRemainingTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getShieldBlockingDelay() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Block getTargetBlock(int arg0, FluidMode arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BlockFace getTargetBlockFace(int arg0, FluidMode arg1) {
    	return this.getTargetBlockFace(arg0, arg1.bukkit);
    }

    @Override
    public TargetBlockInfo getTargetBlockInfo(int arg0, FluidMode arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Entity getTargetEntity(int arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TargetEntityInfo getTargetEntityInfo(int arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isHandRaised() {
    	return this.getHandle().isUsingItem();
    }

    @Override
    public void playPickupItemAnimation(Item arg0, int arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setArrowsStuck(int arg0) {
    	this.getHandle().setArrowCount(arg0);
    }

    @Override
    public void setHurtDirection(float arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setKiller(Player arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setShieldBlockingDelay(int arg0) {
    	// this.getHandle().setShieldBlockingDelay(arg0);
    }
    // PaperAPI - end

    @Override
    public @NotNull EquipmentSlot getHandRaised() {
        InteractionHand hand = this.getHandle().getUsedItemHand();
        return hand == InteractionHand.MAIN_HAND ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND;
    }

    @Override
    public boolean hasLineOfSight(@NotNull Location arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    // 1.17 API START
    @Override
    public boolean isClimbing() {
        return this.getHandle().onClimbable();
    }

    @Override
    public int getBeeStingerCooldown() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getBeeStingersInBody() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setBeeStingerCooldown(int i) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setBeeStingersInBody(int i) {
        // TODO Auto-generated method stub
    }
    
    // 1.19.2

	// @Override
	public <T extends Projectile> @NotNull T launchProjectile_old(@NotNull Class<? extends T> arg0, @Nullable Vector arg1,
			@Nullable Consumer<T> arg2) {
		// TODO Auto-generated method stub
		return null;
	}
	
	// @Override
	public <T extends Projectile> T launchProjectile(Class<? extends T> projectile, Vector velocity, java.util.function.Consumer<? super T> function) {
		ServerLevel world = ((CraftWorld)this.getWorld()).getHandle();
        net.minecraft.world.entity.projectile.Projectile launch = null;
        if (Snowball.class.isAssignableFrom(projectile)) {
            launch = new net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball(world, this.getHandle(), new net.minecraft.world.item.ItemStack(Items.SNOWBALL));
            ((ThrowableProjectile)launch).shootFromRotation(this.getHandle(), this.getHandle().getXRot(), this.getHandle().getYRot(), 0.0f, 1.5f, 1.0f);
        } else if (Egg.class.isAssignableFrom(projectile)) {
            launch = new ThrownEgg(world, this.getHandle(), new net.minecraft.world.item.ItemStack(Items.EGG));
            ((ThrowableProjectile)launch).shootFromRotation(this.getHandle(), this.getHandle().getXRot(), this.getHandle().getYRot(), 0.0f, 1.5f, 1.0f);
        } else if (EnderPearl.class.isAssignableFrom(projectile)) {
            launch = new ThrownEnderpearl(world, this.getHandle(), new net.minecraft.world.item.ItemStack(Items.ENDER_PEARL));
            ((ThrowableProjectile)launch).shootFromRotation(this.getHandle(), this.getHandle().getXRot(), this.getHandle().getYRot(), 0.0f, 1.5f, 1.0f);
        } else if (AbstractArrow.class.isAssignableFrom(projectile)) {
            if (TippedArrow.class.isAssignableFrom(projectile)) {
                launch = new Arrow(world, this.getHandle(), new net.minecraft.world.item.ItemStack(Items.ARROW), null);
                ((org.bukkit.entity.Arrow)launch.getBukkitEntity()).setBasePotionType(PotionType.WATER);
            } else {
                launch = SpectralArrow.class.isAssignableFrom(projectile) ? new net.minecraft.world.entity.projectile.arrow.SpectralArrow(world, this.getHandle(), new net.minecraft.world.item.ItemStack(Items.SPECTRAL_ARROW), null) : (Trident.class.isAssignableFrom(projectile) ? new ThrownTrident(world, this.getHandle(), new net.minecraft.world.item.ItemStack(Items.TRIDENT)) : new Arrow(world, this.getHandle(), new net.minecraft.world.item.ItemStack(Items.ARROW), null));
            }
            ((net.minecraft.world.entity.projectile.arrow.AbstractArrow)launch).shootFromRotation(this.getHandle(), this.getHandle().getXRot(), this.getHandle().getYRot(), 0.0f, Trident.class.isAssignableFrom(projectile) ? 2.5f : 3.0f, 1.0f);
        } else if (ThrownPotion.class.isAssignableFrom(projectile)) {
        	launch = LingeringPotion.class.isAssignableFrom(projectile) ? new ThrownLingeringPotion(world, this.getHandle(), new net.minecraft.world.item.ItemStack(Items.LINGERING_POTION)) : new ThrownSplashPotion(world, this.getHandle(), new net.minecraft.world.item.ItemStack(Items.SPLASH_POTION));
            ((ThrowableProjectile)launch).shootFromRotation(this.getHandle(), this.getHandle().getXRot(), this.getHandle().getYRot(), -20.0f, 0.5f, 1.0f);
        } else if (ThrownExpBottle.class.isAssignableFrom(projectile)) {
            launch = new ThrownExperienceBottle(world, this.getHandle(), new net.minecraft.world.item.ItemStack(Items.EXPERIENCE_BOTTLE));
            ((ThrowableProjectile)launch).shootFromRotation(this.getHandle(), this.getHandle().getXRot(), this.getHandle().getYRot(), -20.0f, 0.7f, 1.0f);
        } else if (FishHook.class.isAssignableFrom(projectile) && this.getHandle() instanceof net.minecraft.world.entity.player.Player) {
            // launch = new FishingBobberEntity((PlayerEntity)this.getHandle(), world, 0, 0, new net.minecraft.item.ItemStack(Items.FISHING_ROD));
            
        	launch = net.minecraft.world.entity.EntityTypes.FISHING_BOBBER.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        	// launch.refreshPositionAndAngles(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
            
        } else if (Fireball.class.isAssignableFrom(projectile)) {
            Location location = this.getEyeLocation();
            Vector direction = location.getDirection().multiply(10);
            Vec3 vec = new Vec3(direction.getX(), direction.getY(), direction.getZ());
            if (SmallFireball.class.isAssignableFrom(projectile)) {
                launch = new net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball(world, this.getHandle(), vec);
            } else if (WitherSkull.class.isAssignableFrom(projectile)) {
                launch = new net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull(world, this.getHandle(), vec);
            } else if (DragonFireball.class.isAssignableFrom(projectile)) {
                launch = new net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball(world, this.getHandle(), vec);
            } else if (AbstractWindCharge.class.isAssignableFrom(projectile)) {
                launch = BreezeWindCharge.class.isAssignableFrom(projectile)
                		? net.minecraft.world.entity.EntityTypes.BREEZE_WIND_CHARGE.create(world, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED)
                		: net.minecraft.world.entity.EntityTypes.WIND_CHARGE.create(world, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
                ((net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge)launch).setOwner(this.getHandle());
                ((net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge)launch).shootFromRotation(this.getHandle(), this.getHandle().getXRot(), this.getHandle().getYRot(), 0.0f, 1.5f, 1.0f);
            } else {
                launch = new LargeFireball(world, this.getHandle(), vec, 1);
            }
            ((AbstractHurtingProjectile)launch).setProjectileSourceBukkit(this);
            // TODO: launch.preserveMotion = true;
            launch.snapTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        } else if (LlamaSpit.class.isAssignableFrom(projectile)) {
            Location location = this.getEyeLocation();
            Vector direction = location.getDirection();
            launch = net.minecraft.world.entity.EntityTypes.LLAMA_SPIT.create(world, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
            ((net.minecraft.world.entity.projectile.LlamaSpit)launch).setOwner(this.getHandle());
            ((net.minecraft.world.entity.projectile.LlamaSpit)launch).shoot(direction.getX(), direction.getY(), direction.getZ(), 1.5f, 10.0f);
            launch.snapTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        } else if (ShulkerBullet.class.isAssignableFrom(projectile)) {
            Location location = this.getEyeLocation();
            launch = new net.minecraft.world.entity.projectile.ShulkerBullet(world, this.getHandle(), null, null);
            launch.snapTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        } else if (Firework.class.isAssignableFrom(projectile)) {
            Location location = this.getEyeLocation();
            
            // TODO
            
            /*
            launch = new FireworkRocketEntity(world, FireworkRocketEntity.getDefaultStack(), this.getHandle(), location.getX(), location.getY() - (double)0.15f, location.getZ(), true);
            float f2 = 0.0f;
            int projectileSize = 1;
            int i2 = 0;
            float f3 = projectileSize == 1 ? 0.0f : 2.0f * f2 / (float)(projectileSize - 1);
            float f4 = (float)((projectileSize - 1) % 2) * f3 / 2.0f;
            float f5 = 1.0f;
            float yaw = f4 + f5 * (float)((i2 + 1) / 2) * f3;
            Vec3d vec3 = this.getHandle().getOppositeRotationVector(1.0f);
            Quaternionf quaternionf = new Quaternionf().setAngleAxis((double)(yaw * ((float)Math.PI / 180)), vec3.x, vec3.y, vec3.z);
            Vec3d vec32 = this.getHandle().getRotationVec(1.0f);
            Vector3f vector3f = vec32.toVector3f().rotate((Quaternionfc)quaternionf);
            ((FireworkRocketEntity)launch).setVelocity(vector3f.x(), vector3f.y(), vector3f.z(), 1.6f, 1.0f);
            */
            
            launch = new FireworkRocketEntity(world, net.minecraft.world.item.ItemStack.EMPTY, getHandle());
            launch.snapTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
            
        }
        // Preconditions.checkArgument((launch != null ? 1 : 0) != 0, (String)"Projectile (%s) not supported", (Object)projectile.getName());
        if (velocity != null) {
            ((Projectile)launch.getBukkitEntity()).setVelocity(velocity);
        }
        if (function != null) {
            function.accept((T) (Projectile) launch.getBukkitEntity());
        }
        world.addFreshEntity(launch);
        return (T)((Projectile)launch.getBukkitEntity());
	}

	@Override
	public @NotNull TriState getFrictionState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFrictionState(@NotNull TriState arg0) {
		// TODO Auto-generated method stub
		
	}

	public void broadcastSlotBreak(EquipmentSlot slot) {
        this.getHandle().level().broadcastEntityEvent(this.getHandle(), net.minecraft.world.entity.LivingEntity.entityEventForEquipmentBreak(CraftEquipmentSlot.getNMS(slot)));
	}

	public void broadcastSlotBreak(EquipmentSlot slot, Collection<Player> players) {
		if (players.isEmpty()) {
			return;
		}
		ClientboundEntityEventPacket packet = new ClientboundEntityEventPacket(this.getHandle(), net.minecraft.world.entity.LivingEntity.entityEventForEquipmentBreak( CraftEquipmentSlot.getNMS(slot)));
		players.forEach(player -> ((CraftPlayer)player).getHandle().connection.send(packet));
	}

	@Override
    public boolean canBreatheUnderwater() {
        return this.getHandle().canBreatheUnderwater();
    }

	@Override
    public ItemStack damageItemStack(ItemStack stack, int amount) {
        net.minecraft.world.item.ItemStack nmsStack;
        if (stack instanceof CraftItemStack) {
            CraftItemStack craftItemStack = (CraftItemStack)stack;
            if (craftItemStack.handle == null || craftItemStack.handle.isEmpty()) {
                return stack;
            }
            nmsStack = craftItemStack.handle;
        } else {
            nmsStack = CraftItemStack.asNMSCopy(stack);
            stack = CraftItemStack.asCraftMirror(nmsStack);
        }
        this.damageItemStack0(nmsStack, amount, null);
        return stack;
    }

	@Override
    public void damageItemStack(EquipmentSlot slot, int amount) {
        net.minecraft.world.entity.EquipmentSlot nmsSlot = CraftEquipmentSlot.getNMS(slot);
        this.damageItemStack0(this.getHandle().getItemBySlot(nmsSlot), amount, nmsSlot);
    }
	
    private void damageItemStack0(net.minecraft.world.item.ItemStack nmsStack, int amount, net.minecraft.world.entity.EquipmentSlot slot) {
        /*nmsStack.damage(amount, this.getHandle(), livingEntity -> {
            if (slot != null) {
                livingEntity.sendEquipmentBreakStatus(slot);
            }
        });*/
        
        nmsStack.hurtAndBreak(amount, this.getHandle(), slot);
    }

	
	@Override
	public @Nullable Sound getDeathSound() {
		// TODO Auto-generated method stub
		return Sound.ENTITY_GENERIC_DEATH;
	}

	@Override
	public @NotNull Sound getDrinkingSound(@NotNull ItemStack arg0) {
		// TODO Auto-generated method stub
		return Sound.ENTITY_GENERIC_DRINK;
	}

	@Override
	public @NotNull Sound getEatingSound(@NotNull ItemStack arg0) {
		// TODO Auto-generated method stub
		return Sound.ENTITY_GENERIC_EAT;
	}

	@Override
	public @NotNull Sound getFallDamageSound(int arg0) {
		// TODO Auto-generated method stub
		return Sound.ENTITY_GENERIC_BIG_FALL;
	}

	@Override
	public @NotNull Sound getFallDamageSoundBig() {
		// TODO Auto-generated method stub
		return Sound.ENTITY_GENERIC_BIG_FALL;
	}

	@Override
	public @NotNull Sound getFallDamageSoundSmall() {
		// TODO Auto-generated method stub
		return Sound.ENTITY_GENERIC_SMALL_FALL;
	}

	@Override
	public @Nullable Sound getHurtSound() {
		// TODO Auto-generated method stub
		return Sound.ENTITY_GENERIC_HURT;
	}

	@Override
	public @Nullable Sound getHurtSound(org.bukkit.damage.DamageSource damageSource) {
		final DamageSource nms = damageSource instanceof CraftDamageSource craft
				? craft.getHandle() : this.getHandle().damageSources().generic();
		final net.minecraft.sounds.SoundEvent sound = this.getHandle().getHurtSound(nms);
		return sound == null ? null : org.bukkit.craftbukkit.CraftSound.minecraftToBukkit(sound);
	}

	@Override
	public float getSoundVolume() {
		return this.getHandle().getSoundVolume();
	}

	@Override
	public float getSoundPitch() {
		return this.getHandle().getVoicePitch();
	}

	@Override
	public void knockback(double arg0, double arg1, double arg2) {
		 // 26.2: LivingEntity#knockback(double,double,double) was removed
		 this.getHandle().push(arg0, arg1, arg2);
	}
	
	// 1.19.4:

	@Override
    public float getBodyYaw() {
        return this.getHandle().getVisualRotationYInDegrees();
    }

	@Override
    public BlockFace getTargetBlockFace(int maxDistance, FluidCollisionMode fluidMode) {
        RayTraceResult result = this.rayTraceBlocks(maxDistance, fluidMode);
        return result != null ? result.getHitBlockFace() : null;
    }

	@Override
    public RayTraceResult rayTraceEntities(int maxDistance, boolean ignoreBlocks) {
        EntityHitResult rayTrace = this.rayTraceEntity(maxDistance, ignoreBlocks);
        return null;
        //return rayTrace == null ? null : new RayTraceResult(CraftVector.toBukkit(rayTrace.getPos()), ((IMixinEntity)rayTrace.getEntity()).getBukkitEntity());
    }
	
    public EntityHitResult rayTraceEntity(int maxDistance, boolean ignoreBlocks) {
        return null;
    }

	@Override
    public void setArrowsInBody(int count, boolean fireEvent) {
        // Preconditions.checkArgument((count >= 0 ? 1 : 0) != 0, (Object)"New arrow amount must be >= 0");
        if (!fireEvent) {
            this.getHandle().getEntityData().set(net.minecraft.world.entity.LivingEntity.DATA_ARROW_COUNT_ID, count);
        } else {
            this.getHandle().setArrowCount(count);
        }
    }

	@Override
	public void setBodyYaw(float arg0) {
        this.getHandle().setYBodyRot(arg0);
	}

	@Override
	public int getNoActionTicks() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setNoActionTicks(int ticks) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean clearActivePotionEffects() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void playHurtAnimation(float yaw) {
		// TODO Auto-generated method stub
		
	}
	
	// 1.20.2 API:
	@Override
    public float getSidewaysMovement() {
        return this.getHandle().xxa;
    }

	@Override
    public float getForwardsMovement() {
        return this.getHandle().zza;
    }

	@Override
    public float getUpwardsMovement() {
        return this.getHandle().yya;
    }
   
	// 1.20.4 API:
	
	@Override
    public boolean hasActiveItem() {
        return this.getHandle().isUsingItem();
    }
	
	@Override
    public EquipmentSlot getActiveItemHand() {
        return CraftEquipmentSlot.getHand(this.getHandle().getUsedItemHand());
    }
	
    @Override
    public void setItemInUseTicks(int ticks) {
        // TODO
    	// this.getHandle().itemUseTimeLeft = ticks;
    }
    
    @Override
    public int getItemInUseTicks() {
        return this.getHandle().getUseItemRemainingTicks();
    }
    
    @Override
    public void startUsingItem(EquipmentSlot hand) {
        switch (hand) {
            case HAND: {
                this.getHandle().startUsingItem(InteractionHand.MAIN_HAND);
                break;
            }
            case OFF_HAND: {
                this.getHandle().startUsingItem(InteractionHand.OFF_HAND);
                break;
            }
            default: {
                throw new IllegalArgumentException("hand may only be HAND or OFF_HAND");
            }
        }
    }

    @Override
    public ItemStack getItemInUse() {
        net.minecraft.world.item.ItemStack item = this.getHandle().getUseItem();
        return item.isEmpty() ? null : CraftItemStack.asCraftMirror(item);
    }
    
    @Override
    public void completeUsingActiveItem() {
        // TODO
    	// this.getHandle().consumeItem();
    }
    
    @Override
    public int getActiveItemRemainingTime() {
        return this.getHandle().getUseItemRemainingTicks();
    }
    
    @Override
    public void setActiveItemRemainingTime(int ticks) {
    	// TODO
    	// this.getHandle().itemUseTimeLeft = ticks;
    }
    
    @Override
    public int getNextArrowRemoval() {
        return this.getHandle().removeArrowTime;
    }
    
    @Override
    public void setNextArrowRemoval(int ticks) {
        this.getHandle().removeArrowTime = ticks;
    }
    
    @Override
    public int getNextBeeStingerRemoval() {
        return this.getHandle().removeStingerTime;
    }
    
    @Override
    public void setNextBeeStingerRemoval(int ticks) {
        this.getHandle().removeStingerTime = ticks;
    }

	@Override
	public void damage(double amount, org.bukkit.damage.@NotNull DamageSource damageSource) {
		// TODO Auto-generated method stub
		
		

		// TODO this.damage(amount, ((CraftDamageSource)damageSource).getHandle());
	}

	@Override
	public int getActiveItemUsedTime() {
		return this.getHandle().getTicksUsingItem();
	}
	
	// 1.20.6 API:

	@Override
	public void heal(double amount, @NotNull RegainReason reason) {
		// TODO Auto-generated method stub
		this.heal(amount);
	}

	@Override
	public boolean canUseEquipmentSlot(@NotNull EquipmentSlot slot) {
		net.minecraft.world.entity.EquipmentSlot es = CraftEquipmentSlot.getNMS(slot);
		return this.getHandle().canUseSlot( es );
	}
	
	// 1.21:

	@Override
	public void broadcastHurtAnimation(@NotNull Collection<Player> players) {
		 for (Player player : players) {
			 ((CraftPlayer)player).sendHurtAnimation(0.0f, this);
		 }
	}

	@Override
	public void setRiptiding(boolean riptiding) {
		// this.getHandle().setLivingFlag(4, riptiding);
	}

	@Override
	public @NotNull CombatTracker getCombatTracker() {
		// TODO Auto-generated method stub
		// TODO: return this.getHandle().getDamageTracker().paperCombatTracker;
		return null;
	}

	@Override
	public void setWaypointStyle(@Nullable Key key) {
		final ResourceKey<WaypointStyleAsset> newKey = key == null
				? WaypointStyleAssets.DEFAULT
						: PaperAdventure.asVanilla(WaypointStyleAssets.ROOT_ID, key);
		if (Objects.equals(getHandle().waypointIcon().style, newKey)) return;

		getHandle().waypointIcon().style = newKey;
		retrack_waypoint();
	}

	@Override
	public void setWaypointColor(@Nullable Color color) {
		final Optional<Integer> newColor = Optional.ofNullable(color).map(Color::asARGB);
        if (Objects.equals(getHandle().waypointIcon().color, newColor)) {
        	return;
        }

        getHandle().waypointIcon().color = newColor;
        retrack_waypoint();
	}
	
	private void retrack_waypoint() {
        ServerWaypointManager manager = ((ServerLevel) getHandle().level()).getWaypointManager();
        manager.untrackWaypoint(getHandle());
        manager.trackWaypoint(getHandle());
    }

	@Override
	public Key getWaypointStyle() {
		return PaperAdventure.asAdventure(getHandle().waypointIcon().style.identifier());
	}

	@Override
	public Color getWaypointColor() {
		return getHandle().waypointIcon().color.map(Color::fromARGB).orElse(null);
	}

	@Override
	public void kill(org.bukkit.damage.DamageSource damageSource) {
        // Preconditions.checkState(!this.getHandle().generation, "Cannot kill entity during world generation");
        // Preconditions.checkArgument(damageSource != null, "damageSource cannot be null");

        this.getHandle().setHealth(0);
        this.getHandle().die(((CraftDamageSource) damageSource).getHandle());
    }

}
