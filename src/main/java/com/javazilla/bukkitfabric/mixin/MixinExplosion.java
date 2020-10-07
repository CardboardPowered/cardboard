/**
 * The Bukkit for Fabric Project
 * Copyright (C) 2020 Javazilla Software and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.javazilla.bukkitfabric.mixin;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.block.BlockExplodeEvent;

import org.bukkit.event.entity.EntityExplodeEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;
import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;

@Mixin(Explosion.class)
public class MixinExplosion {

    @Shadow @Final private World world;
    @Shadow @Final private double x;
    @Shadow @Final private double y;
    @Shadow @Final private double z;
    @Shadow @Final private List<BlockPos> affectedBlocks;
    @Shadow @Final public Entity entity;
    @Shadow @Final private float power;
    @Shadow @Final private DamageSource damageSource;
    @Shadow @Final private ExplosionBehavior behavior;
    @Shadow @Final private Explosion.DestructionType destructionType;
    @Shadow @Final private boolean createFire;
    @Shadow @Final private Random random;
    @Shadow @Final private Map<PlayerEntity, Vec3d> affectedPlayers = Maps.newHashMap();

    @Shadow private static void method_24023(ObjectArrayList<Pair<ItemStack, BlockPos>> objectarraylist, ItemStack itemstack, BlockPos blockposition) {}
    @Shadow public DamageSource getDamageSource() {return null;}

    public boolean wasCanceled = false; // Added by Bukkit

    /**
     * @author BukkitFabric
     * @reason Explosion Events
     */
    @Overwrite
    public void affectWorld(boolean flag) {
        if (this.world.isClient)
            this.world.playSound(this.x, this.y, this.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2F) * 0.7F, false);

        boolean flag1 = this.destructionType != Explosion.DestructionType.NONE;

        if (flag) {
            if (this.power >= 2.0F && flag1)
                this.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
            else this.world.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
        }

        if (flag1) {
            ObjectArrayList<Pair<ItemStack, BlockPos>> objectarraylist = new ObjectArrayList<Pair<ItemStack, BlockPos>>();
            Collections.shuffle(this.affectedBlocks, this.world.random);
            Iterator<BlockPos> iterator = this.affectedBlocks.iterator();

            // CraftBukkit start
            org.bukkit.World bworld = ((IMixinWorld)(ServerWorld)this.world).getWorldImpl();
            org.bukkit.entity.Entity explode = this.entity == null ? null : ((IMixinEntity)this.entity).getBukkitEntity();
            Location location = new Location(bworld, this.x, this.y, this.z);

            List<org.bukkit.block.Block> blockList = Lists.newArrayList();
            for (int i1 = this.affectedBlocks.size() - 1; i1 >= 0; i1--) {
                BlockPos cpos = (BlockPos) this.affectedBlocks.get(i1);
                org.bukkit.block.Block bblock = bworld.getBlockAt(cpos.getX(), cpos.getY(), cpos.getZ());
                if (!bblock.getType().isAir())
                    blockList.add(bblock);
            }

            boolean cancelled;
            List<org.bukkit.block.Block> bukkitBlocks;
            float yield;

            if (explode != null) {
                EntityExplodeEvent event = new EntityExplodeEvent(explode, location, blockList, this.destructionType == Explosion.DestructionType.DESTROY ? 1.0F / this.power : 1.0F);
                CraftServer.INSTANCE.getPluginManager().callEvent(event);
                cancelled = event.isCancelled();
                bukkitBlocks = event.blockList();
                yield = event.getYield();
            } else {
                BlockExplodeEvent event = new BlockExplodeEvent(location.getBlock(), blockList, this.destructionType == Explosion.DestructionType.DESTROY ? 1.0F / this.power : 1.0F);
                CraftServer.INSTANCE.getPluginManager().callEvent(event);
                cancelled = event.isCancelled();
                bukkitBlocks = event.blockList();
                yield = event.getYield();
            }
            this.affectedBlocks.clear();

            for (org.bukkit.block.Block bblock : bukkitBlocks) {
                BlockPos coords = new BlockPos(bblock.getX(), bblock.getY(), bblock.getZ());
                affectedBlocks.add(coords);
            }

            if (cancelled) {
                this.wasCanceled = true;
                return;
            }
            iterator = this.affectedBlocks.iterator();

            while (iterator.hasNext()) {
                BlockPos blockposition = (BlockPos) iterator.next();
                BlockState iblockdata = this.world.getBlockState(blockposition);
                Block block = iblockdata.getBlock();

                if (!iblockdata.isAir()) {
                    BlockPos blockposition1 = blockposition.toImmutable();

                    this.world.getProfiler().push("explosion_blocks");
                    if (block.shouldDropItemsOnExplosion((Explosion)(Object)this) && this.world instanceof ServerWorld) {
                        BlockEntity tileentity = block.hasBlockEntity() ? this.world.getBlockEntity(blockposition) : null;
                        LootContext.Builder loottableinfo_builder = (new LootContext.Builder((ServerWorld) this.world)).random(this.world.random).parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter((Vec3i) blockposition)).parameter(LootContextParameters.TOOL, ItemStack.EMPTY).optionalParameter(LootContextParameters.BLOCK_ENTITY, tileentity).optionalParameter(LootContextParameters.THIS_ENTITY, this.entity);

                        if (this.destructionType ==Explosion.DestructionType.DESTROY || yield < 1.0F)
                            loottableinfo_builder.parameter(LootContextParameters.EXPLOSION_RADIUS, 1.0F / yield);

                        iblockdata.getDroppedStacks(loottableinfo_builder).forEach((itemstack) -> method_24023(objectarraylist, itemstack, blockposition1));
                    }

                    this.world.setBlockState(blockposition, Blocks.AIR.getDefaultState(), 3);
                    block.onDestroyedByExplosion(this.world, blockposition, (Explosion)(Object)this);
                    this.world.getProfiler().pop();
                }
            }

            ObjectListIterator<Pair<ItemStack, BlockPos>> objectlistiterator = objectarraylist.iterator();

            while (objectlistiterator.hasNext()) {
                Pair<ItemStack, BlockPos> pair = (Pair<ItemStack, BlockPos>) objectlistiterator.next();
                Block.dropStack(this.world, (BlockPos) pair.getSecond(), (ItemStack) pair.getFirst());
            }
        }

        if (this.createFire) {
            Iterator<BlockPos> iterator1 = this.affectedBlocks.iterator();
            while (iterator1.hasNext()) {
                BlockPos blockposition2 = (BlockPos) iterator1.next();
                if (this.random.nextInt(3) == 0 && this.world.getBlockState(blockposition2).isAir() && this.world.getBlockState(blockposition2.down()).isOpaqueFullCube(this.world, blockposition2.down()))
                    if (!BukkitEventFactory.callBlockIgniteEvent(this.world, blockposition2.getX(), blockposition2.getY(), blockposition2.getZ(), (Explosion)(Object)this).isCancelled())
                        this.world.setBlockState(blockposition2, AbstractFireBlock.getState((BlockView) this.world, blockposition2));
            }
        }

    }

    /**
     * @author BukkitFabric
     * @reason Explosion Events
     */
    @SuppressWarnings("unused")
    @Overwrite
    public void collectBlocksAndDamageEntities() {
        if (this.power < 0.1F)
            return;
        Set<BlockPos> set = Sets.newHashSet();
        int i;int j;

        for (int k = 0; k < 16; ++k) {
            for (i = 0; i < 16; ++i) {
                for (j = 0; j < 16; ++j) {
                    if (k == 0 || k == 15 || i == 0 || i == 15 || j == 0 || j == 15) {
                        double d0 = (double) ((float) k / 15.0F * 2.0F - 1.0F);
                        double d1 = (double) ((float) i / 15.0F * 2.0F - 1.0F);
                        double d2 = (double) ((float) j / 15.0F * 2.0F - 1.0F);
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                        d0 /= d3;
                        d1 /= d3;
                        d2 /= d3;
                        float f = this.power * (0.7F + this.world.random.nextFloat() * 0.6F);
                        double d4 = this.x;
                        double d5 = this.y;
                        double d6 = this.z;
                        for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                            BlockPos blockposition = new BlockPos(d4, d5, d6);
                            BlockState iblockdata = this.world.getBlockState(blockposition);
                            FluidState fluid = this.world.getFluidState(blockposition);
                            Optional<Float> optional = this.behavior.getBlastResistance((Explosion)(Object)this, this.world, blockposition, iblockdata, fluid);
                            if (optional.isPresent())
                                f -= ((Float) optional.get() + 0.3F) * 0.3F;
                            if (f > 0.0F && this.behavior.canDestroyBlock((Explosion)(Object)this, this.world, blockposition, iblockdata, f) && blockposition.getY() < 256 && blockposition.getY() >= 0)
                                set.add(blockposition);
                            d4 += d0 * 0.30000001192092896D;
                            d5 += d1 * 0.30000001192092896D;
                            d6 += d2 * 0.30000001192092896D;
                        }
                    }
                }
            }
        }

        this.affectedBlocks.addAll(set);
        float f2 = this.power * 2.0F;

        i = MathHelper.floor(this.x - (double) f2 - 1.0D);
        j = MathHelper.floor(this.x + (double) f2 + 1.0D);
        int l = MathHelper.floor(this.y - (double) f2 - 1.0D);
        int i1 = MathHelper.floor(this.y + (double) f2 + 1.0D);
        int j1 = MathHelper.floor(this.z - (double) f2 - 1.0D);
        int k1 = MathHelper.floor(this.z + (double) f2 + 1.0D);
        List<Entity> list = this.world.getOtherEntities(this.entity, new Box((double) i, (double) l, (double) j1, (double) j, (double) i1, (double) k1));
        Vec3d vec3d = new Vec3d(this.x, this.y, this.z);

        for (int l1 = 0; l1 < list.size(); ++l1) {
            Entity entity = (Entity) list.get(l1);
            if (!entity.isImmuneToExplosion()) {
                double d7 = (double) (MathHelper.sqrt(entity.squaredDistanceTo(vec3d)) / f2);

                if (d7 <= 1.0D) {
                    double d8 = entity.getX() - this.x;
                    double d9 = (entity instanceof TntEntity ? entity.getY() : entity.getEyeY()) - this.y;
                    double d10 = entity.getZ() - this.z;
                    double d11 = (double) MathHelper.sqrt(d8 * d8 + d9 * d9 + d10 * d10);

                    if (d11 != 0.0D) {
                        d8 /= d11;
                        d9 /= d11;
                        d10 /= d11;
                        double d12 = (double) Explosion.getExposure(vec3d, entity);
                        double d13 = (1.0D - d7) * d12;

                        BukkitEventFactory.entityDamage = entity;
                        boolean wasDamaged = entity.damage(this.getDamageSource(), (float) ((int) ((d13 * d13 + d13) / 2.0D * 7.0D * (double) f2 + 1.0D)));
                        BukkitEventFactory.entityDamage = null;
                        if (!wasDamaged && !(entity instanceof TntEntity || entity instanceof FallingBlockEntity) /*&& !entity.forceExplosionKnockback*/)
                            continue;
                        double d14 = d13;

                        if (entity instanceof LivingEntity)
                            d14 = ProtectionEnchantment.transformExplosionKnockback((LivingEntity) entity, d13);

                        entity.setVelocity(entity.getVelocity().add(d8 * d14, d9 * d14, d10 * d14));
                        if (entity instanceof PlayerEntity) {
                            PlayerEntity entityhuman = (PlayerEntity) entity;

                            if (!entityhuman.isSpectator() && (!entityhuman.isCreative() || !entityhuman.abilities.flying))
                                this.affectedPlayers.put(entityhuman, new Vec3d(d8 * d13, d9 * d13, d10 * d13));
                        }
                    }
                }
            }
        }

    }

}