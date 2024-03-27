package org.cardboardpowered.impl.block;

import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.util.math.BlockPointer;

import org.bukkit.block.Block;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.projectile.*;
import net.minecraft.entity.projectile.thrown.*;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;


import com.google.common.base.Preconditions;
import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.item.Items;

import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LingeringPotion;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.TippedArrow;
import org.bukkit.entity.WitherSkull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

public class CardboardBlockProjectileSource implements BlockProjectileSource {

    private final DispenserBlockEntity dispenserBlock;

    public CardboardBlockProjectileSource(DispenserBlockEntity dispenserBlock) {
        this.dispenserBlock = dispenserBlock;
    }

    @Override
    public Block getBlock() {
        return ((IMixinWorld)(Object)dispenserBlock.getWorld()).getWorldImpl().getBlockAt(dispenserBlock.getPos().getX(), dispenserBlock.getPos().getY(), dispenserBlock.getPos().getZ());
    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> projectile) {
        return this.launchProjectile(projectile, null);
    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> projectile, Vector velocity) {
        return this.launchProjectile(projectile, velocity, null);
    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> projectile, Vector velocity, Consumer<T> function) {
        Preconditions.checkArgument((this.getBlock().getType() == Material.DISPENSER ? 1 : 0) != 0, (Object)"Block is no longer dispenser");
        BlockPointer isourceblock = new BlockPointer((ServerWorld)this.dispenserBlock.getWorld(), this.dispenserBlock.getPos(), this.dispenserBlock.getCachedState(), this.dispenserBlock);
        
        Position iposition = DispenserBlock.getOutputLocation(isourceblock);
        Direction enumdirection = isourceblock.state().get(DispenserBlock.FACING);
        World world = this.dispenserBlock.getWorld();
        ProjectileEntity launch = null;
        if (Snowball.class.isAssignableFrom(projectile)) {
            launch = new SnowballEntity(world, iposition.getX(), iposition.getY(), iposition.getZ());
        } else if (Egg.class.isAssignableFrom(projectile)) {
            launch = new EggEntity(world, iposition.getX(), iposition.getY(), iposition.getZ());
        } else if (EnderPearl.class.isAssignableFrom(projectile)) {
            launch = new EnderPearlEntity(world, null);
            launch.setPosition(iposition.getX(), iposition.getY(), iposition.getZ());
        } else if (ThrownExpBottle.class.isAssignableFrom(projectile)) {
            launch = new ExperienceBottleEntity(world, iposition.getX(), iposition.getY(), iposition.getZ());
        } else if (ThrownPotion.class.isAssignableFrom(projectile)) {
            if (LingeringPotion.class.isAssignableFrom(projectile)) {
                launch = new PotionEntity(world, iposition.getX(), iposition.getY(), iposition.getZ());
                ((PotionEntity)launch).setItem(CraftItemStack.asNMSCopy(new ItemStack(Material.LINGERING_POTION, 1)));
            } else {
                launch = new PotionEntity(world, iposition.getX(), iposition.getY(), iposition.getZ());
                ((PotionEntity)launch).setItem(CraftItemStack.asNMSCopy(new ItemStack(Material.SPLASH_POTION, 1)));
            }
        } else if (AbstractArrow.class.isAssignableFrom(projectile)) {
            if (TippedArrow.class.isAssignableFrom(projectile)) {
                launch = new ArrowEntity(world, iposition.getX(), iposition.getY(), iposition.getZ(), new net.minecraft.item.ItemStack(Items.ARROW));
                ((Arrow) ((IMixinEntity)launch).getBukkitEntity() ).setBasePotionData(new PotionData(PotionType.WATER, false, false));
            } else {
                launch = SpectralArrow.class.isAssignableFrom(projectile) ?
                		new SpectralArrowEntity(world, iposition.getX(), iposition.getY(), iposition.getZ(), new net.minecraft.item.ItemStack(Items.SPECTRAL_ARROW)) :
                		new ArrowEntity(world, iposition.getX(), iposition.getY(), iposition.getZ(), new net.minecraft.item.ItemStack(Items.ARROW));
            }
            ((PersistentProjectileEntity)launch).pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;
            ((IMixinEntity)(PersistentProjectileEntity)launch).setProjectileSourceBukkit(this);

        } else if (Fireball.class.isAssignableFrom(projectile)) {
            double d0 = iposition.getX() + (double)((float)enumdirection.getOffsetX() * 0.3f);
            double d1 = iposition.getY() + (double)((float)enumdirection.getOffsetY() * 0.3f);
            double d2 = iposition.getZ() + (double)((float)enumdirection.getOffsetZ() * 0.3f);
            Random random = world.random;
            double d3 = random.nextGaussian() * 0.05 + (double)enumdirection.getOffsetX();
            double d4 = random.nextGaussian() * 0.05 + (double)enumdirection.getOffsetY();
            double d5 = random.nextGaussian() * 0.05 + (double)enumdirection.getOffsetZ();
            if (SmallFireball.class.isAssignableFrom(projectile)) {
                launch = new SmallFireballEntity(world, null, d0, d1, d2);
            } else if (WitherSkull.class.isAssignableFrom(projectile)) {
                launch = EntityType.WITHER_SKULL.create(world);
                launch.setPosition(d0, d1, d2);
                double d6 = Math.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
                ((ExplosiveProjectileEntity)launch).powerX = d3 / d6 * 0.1;
                ((ExplosiveProjectileEntity)launch).powerY = d4 / d6 * 0.1;
                ((ExplosiveProjectileEntity)launch).powerZ = d5 / d6 * 0.1;
            } else {
                launch = EntityType.FIREBALL.create(world);
                launch.setPosition(d0, d1, d2);
                double d6 = Math.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
                ((ExplosiveProjectileEntity)launch).powerX = d3 / d6 * 0.1;
                ((ExplosiveProjectileEntity)launch).powerY = d4 / d6 * 0.1;
                ((ExplosiveProjectileEntity)launch).powerZ = d5 / d6 * 0.1;
            }
            ((IMixinEntity)(ExplosiveProjectileEntity)launch).setProjectileSourceBukkit(this);
        }
        Preconditions.checkArgument((launch != null ? 1 : 0) != 0, (Object)"Projectile not supported");
        if (launch instanceof ProjectileEntity) {
            if (launch instanceof ThrownEntity) {
                ((IMixinEntity)(ThrownEntity)launch).setProjectileSourceBukkit(this);
            }
            float a2 = 6.0f;
            float b2 = 1.1f;
            if (launch instanceof PotionEntity || launch instanceof ThrownExpBottle) {
                a2 *= 0.5f;
                b2 *= 1.25f;
            }
            ((ProjectileEntity)launch).setVelocity(enumdirection.getOffsetX(), (float)enumdirection.getOffsetY() + 0.1f, enumdirection.getOffsetZ(), b2, a2);
        }
        if (velocity != null) {
            ((Projectile)((IMixinEntity)launch).getBukkitEntity()).setVelocity(velocity);
        }
        if (function != null) {
            // TODO function.accept(((IMixinEntity)launch).getBukkitEntity()));
        }
        world.spawnEntity(launch);
        return (T)((Projectile)((IMixinEntity)launch).getBukkitEntity());
    }
	
	
}