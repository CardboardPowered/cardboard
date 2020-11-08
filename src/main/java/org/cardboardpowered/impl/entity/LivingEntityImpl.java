package org.cardboardpowered.impl.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.LingeringPotion;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.TippedArrow;
import org.bukkit.entity.Trident;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import com.google.common.collect.Sets;
import com.javazilla.bukkitfabric.Utils;
import org.cardboardpowered.impl.world.WorldImpl;
import com.javazilla.bukkitfabric.impl.potion.PotionUtil;
import com.javazilla.bukkitfabric.interfaces.IMixinArrowEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;

import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.LlamaSpitEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.util.Hand;

@SuppressWarnings("deprecation")
public class LivingEntityImpl extends CraftEntity implements LivingEntity {

    public net.minecraft.entity.LivingEntity nms;

    public LivingEntityImpl(net.minecraft.entity.Entity entity) {
        super(entity);
        this.nms = (net.minecraft.entity.LivingEntity) entity;
    }

    public LivingEntityImpl(CraftServer server, net.minecraft.entity.Entity entity) {
        this(entity);
    }

    @Override
    public AttributeInstance getAttribute(Attribute arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void damage(double arg0) {
        nms.damage(DamageSource.MAGIC, (float)arg0);
    }

    @Override
    public void damage(double arg0, Entity arg1) {
        nms.damage(DamageSource.mob((net.minecraft.entity.LivingEntity) arg1), (float) arg0);
    }

    @Override
    public double getAbsorptionAmount() {
        return nms.getAbsorptionAmount();
    }

    @Override
    public double getHealth() {
        return nms.getHealth();
    }

    @Override
    public double getMaxHealth() {
        // TODO Auto-generated method stub
        return nms.getMaxHealth();
    }

    @Override
    public void resetMaxHealth() {
        // TODO Auto-generated method stub
    }

    @Override
    public void setAbsorptionAmount(double arg0) {
        nms.setAbsorptionAmount((float)arg0);
    }

    @Override
    public void setHealth(double arg0) {
        nms.setHealth((float) arg0);
    }

    @Override
    public void setMaxHealth(double arg0) {
        // TODO Max health
        nms.setHealth((float) arg0);
    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> arg0) {
        return launchProjectile(arg0, null);
    }

    @Override
    public net.minecraft.entity.LivingEntity getHandle() {
        return nms;
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> projectile, Vector arg1) {
        net.minecraft.world.World world = ((WorldImpl) getWorld()).getHandle();
        net.minecraft.entity.Entity launch = null;

        if (Snowball.class.isAssignableFrom(projectile)) {
            launch = new SnowballEntity(world, getHandle());
            ((ThrownEntity) launch).setProperties(getHandle(), getHandle().pitch, getHandle().yaw, 0.0F, 1.5F, 1.0F); // ItemSnowball
        } else if (Egg.class.isAssignableFrom(projectile)) {
            launch = new EggEntity(world, getHandle());
            ((ThrownEntity) launch).setProperties(getHandle(), getHandle().pitch, getHandle().yaw, 0.0F, 1.5F, 1.0F); // ItemEgg
        } else if (EnderPearl.class.isAssignableFrom(projectile)) {
            launch = new EnderPearlEntity(world, getHandle());
            ((ThrownEntity) launch).setProperties(getHandle(), getHandle().pitch, getHandle().yaw, 0.0F, 1.5F, 1.0F); // ItemEnderPearl
        } else if (AbstractArrow.class.isAssignableFrom(projectile)) {
            if (TippedArrow.class.isAssignableFrom(projectile)) {
                launch = new ArrowEntity(world, getHandle());
                ((IMixinArrowEntity)(ArrowEntity) launch).setType(PotionUtil.fromBukkit(new PotionData(PotionType.WATER, false, false)));
            } else if (SpectralArrow.class.isAssignableFrom(projectile)) {
                launch = new SpectralArrowEntity(world, getHandle());
            } else if (Trident.class.isAssignableFrom(projectile)) {
                launch = new TridentEntity(world, getHandle(), new net.minecraft.item.ItemStack(net.minecraft.item.Items.TRIDENT));
            } else {
                launch = new ArrowEntity(world, getHandle());
            }
            ((PersistentProjectileEntity) launch).setProperties(getHandle(), getHandle().pitch, getHandle().yaw, 0.0F, 3.0F, 1.0F); // ItemBow
        } else if (ThrownPotion.class.isAssignableFrom(projectile)) {
            if (LingeringPotion.class.isAssignableFrom(projectile)) {
                launch = new PotionEntity(world, getHandle());
                ((PotionEntity) launch).setItem(CraftItemStack.asNMSCopy(new ItemStack(org.bukkit.Material.LINGERING_POTION, 1)));
            } else {
                launch = new PotionEntity(world, getHandle());
                ((PotionEntity) launch).setItem(CraftItemStack.asNMSCopy(new ItemStack(org.bukkit.Material.SPLASH_POTION, 1)));
            }
            ((ThrownEntity) launch).setProperties(getHandle(), getHandle().pitch, getHandle().yaw, -20.0F, 0.5F, 1.0F); // ItemSplashPotion
        } else if (ThrownExpBottle.class.isAssignableFrom(projectile)) {
            launch = new ExperienceBottleEntity(world, getHandle());
            ((ThrownEntity) launch).setProperties(getHandle(), getHandle().pitch, getHandle().yaw, -20.0F, 0.7F, 1.0F); // ItemExpBottle
        } else if (FishHook.class.isAssignableFrom(projectile) && getHandle() instanceof PlayerEntity) {
            launch = new FishingBobberEntity((PlayerEntity) getHandle(), world, 0, 0);
        } else if (Fireball.class.isAssignableFrom(projectile)) {
            Location location = getEyeLocation();
            Vector direction = location.getDirection().multiply(10);

            if (SmallFireball.class.isAssignableFrom(projectile)) {
                launch = new SmallFireballEntity(world, getHandle(), direction.getX(), direction.getY(), direction.getZ());
            } else if (WitherSkull.class.isAssignableFrom(projectile)) {
                launch = new WitherSkullEntity(world, getHandle(), direction.getX(), direction.getY(), direction.getZ());
            } else if (DragonFireball.class.isAssignableFrom(projectile)) {
                launch = new DragonFireballEntity(world, getHandle(), direction.getX(), direction.getY(), direction.getZ());
            } else {
                launch = new FireballEntity(world, getHandle(), direction.getX(), direction.getY(), direction.getZ());
            }

            ((IMixinEntity) launch).setProjectileSourceBukkit(this);
            launch.refreshPositionAndAngles(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        } else if (LlamaSpit.class.isAssignableFrom(projectile)) {
            Location location = getEyeLocation();
            Vector direction = location.getDirection();

            launch = net.minecraft.entity.EntityType.LLAMA_SPIT.create(world);

            ((LlamaSpitEntity) launch).setOwner(getHandle());
            ((LlamaSpitEntity) launch).setVelocity(direction.getX(), direction.getY(), direction.getZ(), 1.5F, 10.0F); // EntityLlama
            launch.refreshPositionAndAngles(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        } else if (ShulkerBullet.class.isAssignableFrom(projectile)) {
            Location location = getEyeLocation();

            launch = new ShulkerBulletEntity(world, getHandle(), null, null);
            launch.refreshPositionAndAngles(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        } else if (Firework.class.isAssignableFrom(projectile)) {
            Location location = getEyeLocation();

            launch = new FireworkRocketEntity(world, net.minecraft.item.ItemStack.EMPTY, getHandle());
            launch.refreshPositionAndAngles(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        }

        Validate.notNull(launch, "Projectile not supported");

        if (arg1 != null) {
            ((T) ((IMixinEntity)launch).getBukkitEntity()).setVelocity(arg1);
        }

        world.spawnEntity(launch);
        return (T) ((IMixinEntity)launch).getBukkitEntity();
    }

    @Override
    public boolean addPotionEffect(PotionEffect effect) {
        return addPotionEffect(effect, false);
    }

    @Override
    public boolean addPotionEffect(PotionEffect effect, boolean force) {
        nms.addStatusEffect(new StatusEffectInstance(StatusEffect.byRawId(effect.getType().getId()), effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.hasParticles())/*, EntityPotionEffectEvent.Cause.PLUGIN*/);
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
        nms.attackLivingEntity(((LivingEntityImpl)arg0).nms);
    }

    @Override
    public Collection<PotionEffect> getActivePotionEffects() {
        List<PotionEffect> effects = new ArrayList<PotionEffect>();
        for (StatusEffectInstance handle : nms.activeStatusEffects.values())
            effects.add(new PotionEffect(PotionEffectType.getById(StatusEffect.getRawId(handle.getEffectType())), handle.getDuration(), handle.getAmplifier(), handle.isAmbient(), handle.shouldShowParticles()));
        return effects;
    }

    @Override
    public boolean getCanPickupItems() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public EntityEquipment getEquipment() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getEyeHeight() {
        return nms.getStandingEyeHeight();
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
        return nms.attackingPlayer == null ? null : (Player) ((IMixinEntity)nms.attackingPlayer).getBukkitEntity();
    }

    @Override
    public double getLastDamage() {
        return nms.lastDamageTaken;
    }

    @Override
    public List<Block> getLastTwoTargetBlocks(Set<Material> arg0, int arg1) {
        return getLineOfSight(arg0, arg1, 2);
    }

    @Override
    public Entity getLeashHolder() throws IllegalStateException {
        return ((IMixinEntity)((MobEntity) nms).getHoldingEntity()).getBukkitEntity();
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
        return nms.getMaxAir();
    }

    @Override
    public int getMaximumNoDamageTicks() {
        return nms.defaultMaxHealth;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getMemory(MemoryKey<T> arg0) {
        return (T) nms.getBrain().getOptionalMemory(Utils.fromMemoryKey(arg0)).map(Utils::fromNmsGlobalPos).orElse(null);
    }

    @Override
    public int getNoDamageTicks() {
        return nms.timeUntilRegen;
    }

    @Override
    public PotionEffect getPotionEffect(PotionEffectType arg0) {
        StatusEffectInstance handle = nms.getStatusEffect(StatusEffect.byRawId(arg0.getId()));
        return (handle == null) ? null : new PotionEffect(PotionEffectType.getById(StatusEffect.getRawId(handle.getEffectType())), handle.getDuration(), handle.getAmplifier(), handle.isAmbient(), handle.shouldShowParticles());
    }

    @Override
    public int getRemainingAir() {
        return nms.getAir();
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
        return (this.getHandle() instanceof MobEntity) ? !((MobEntity) this.getHandle()).isAiDisabled() : false;
    }

    @Override
    public boolean hasLineOfSight(Entity arg0) {
        return nms.canSee(((CraftEntity)arg0).nms);
    }

    @Override
    public boolean hasPotionEffect(PotionEffectType arg0) {
        return nms.hasStatusEffect(StatusEffect.byRawId(arg0.getId()));
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
        if (!(getHandle() instanceof MobEntity))
            return false;
        return ((MobEntity) getHandle()).getHoldingEntity() != null;
    }

    @Override
    public boolean isRiptiding() {
        return nms.isUsingRiptide();
    }

    @Override
    public boolean isSleeping() {
        return nms.isSleeping();
    }

    @Override
    public boolean isSwimming() {
        return nms.isSwimming();
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
        nms.removeStatusEffect(StatusEffect.byRawId(type.getId())/*, EntityPotionEffectEvent.Cause.PLUGIN*/);
    }

    @Override
    public void setAI(boolean arg0) {
        if (this.getHandle() instanceof MobEntity)
            ((MobEntity) this.getHandle()).setAiDisabled(!arg0);
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
        nms.setFlag(7, arg0);
    }

    @Override
    public void setLastDamage(double arg0) {
        nms.lastDamageTaken = (float) arg0;
    }

    @Override
    public boolean setLeashHolder(Entity holder) {
        if ((nms instanceof WitherEntity) || !(nms instanceof MobEntity))
            return false;

        if (holder == null)
            return unleash();

        if (holder.isDead())
            return false;

        unleash();
        ((MobEntity) nms).attachLeash(((CraftEntity) holder).getHandle(), true);
        return true;
    }

    private boolean unleash() {
        if (!isLeashed())
            return false;
        ((MobEntity) getHandle()).detachLeash(true, false);
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
        nms.setSwimming(arg0);
    }

    @Override
    public void swingMainHand() {
        nms.swingHand(Hand.MAIN_HAND);
    }

    @Override
    public void swingOffHand() {
        nms.swingHand(Hand.OFF_HAND);
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

    @Override
    public EntityType getType() {
        return EntityType.UNKNOWN;
    }

    // Spigot-743
    public boolean isInvisible() {
        return getHandle().isInvisible();
    }

    // Spigot-743
    public void setInvisible(boolean invisible) {
        // TODO getHandle().persistentInvisibility = invisible;
        getHandle().setFlag(5, invisible);
    }

    // PaperAPI - start
    public boolean isJumping() {
        return getHandle().jumping;
    }

    public void setJumping(boolean jumping) {
        getHandle().setJumping(jumping);
        if (jumping && getHandle() instanceof MobEntity)
            ((MobEntity) getHandle()).getJumpControl().tick();
    }
    // PaperAPI - end

}