/**
 * The Bukkit for Fabric Project
 * Copyright (C) 2020 Javazilla Software and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 */
package com.javazilla.bukkitfabric.mixin;

import java.util.Objects;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.javazilla.bukkitfabric.interfaces.IMixinServerPlayerInteractionManager;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CakeBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerActionResponseS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

@Mixin(ServerPlayerInteractionManager.class)
public class MixinServerPlayerInteractionManager implements IMixinServerPlayerInteractionManager {

    @Shadow public ServerPlayerEntity player;
    @Shadow public ServerWorld world;
    @Shadow private GameMode gameMode;
    @Shadow private boolean mining;
    @Shadow private int startMiningTime;
    @Shadow private BlockPos miningPos;
    @Shadow private int tickCounter;
    @Shadow private boolean failedToMine;
    @Shadow private BlockPos failedMiningPos;
    @Shadow private int failedStartMiningTime;
    @Shadow private int blockBreakingProgress;

    /**
     * @author BukkitFabric
     * @reason Interaction Events
     */
    @SuppressWarnings("deprecation")
    @Overwrite
    public void processBlockBreakingAction(BlockPos blockposition, PlayerActionC2SPacket.Action packetplayinblockdig_enumplayerdigtype, Direction enumdirection, int i) {
        double d0 = this.player.getX() - ((double) blockposition.getX() + 0.5D);
        double d1 = this.player.getY() - ((double) blockposition.getY() + 0.5D) + 1.5D;
        double d2 = this.player.getZ() - ((double) blockposition.getZ() + 0.5D);
        double d3 = d0 * d0 + d1 * d1 + d2 * d2;

        if (d3 > 36.0D) {
            this.player.networkHandler.sendPacket(new PlayerActionResponseS2CPacket(blockposition, this.world.getBlockState(blockposition), packetplayinblockdig_enumplayerdigtype, false, "too far"));
        } else if (blockposition.getY() >= i) {
            this.player.networkHandler.sendPacket(new PlayerActionResponseS2CPacket(blockposition, this.world.getBlockState(blockposition), packetplayinblockdig_enumplayerdigtype, false, "too high"));
        } else {
            BlockState iblockdata;

            if (packetplayinblockdig_enumplayerdigtype == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
                if (!this.world.canPlayerModifyAt((PlayerEntity) this.player, blockposition)) {
                    // CraftBukkit start - fire PlayerInteractEvent
                    BukkitEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_BLOCK, blockposition, enumdirection, this.player.inventory.getMainHandStack(), Hand.MAIN_HAND);
                    this.player.networkHandler.sendPacket(new PlayerActionResponseS2CPacket(blockposition, this.world.getBlockState(blockposition), packetplayinblockdig_enumplayerdigtype, false, "may not interact"));
                    // Update any tile entity data for this block
                    BlockEntity tileentity = world.getBlockEntity(blockposition);
                    if (tileentity != null)
                        this.player.networkHandler.sendPacket(tileentity.toUpdatePacket());
                    return;
                }

                PlayerInteractEvent event = BukkitEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_BLOCK, blockposition, enumdirection, this.player.inventory.getMainHandStack(), Hand.MAIN_HAND);
                if (event.isCancelled()) {
                    // Let the client know the block still exists
                    this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.world, blockposition));
                    // Update any tile entity data for this block
                    BlockEntity tileentity = this.world.getBlockEntity(blockposition);
                    if (tileentity != null)
                        this.player.networkHandler.sendPacket(tileentity.toUpdatePacket());
                    return;
                }

                if (this.gameMode.isCreative()) {
                    this.finishMining(blockposition, packetplayinblockdig_enumplayerdigtype, "creative destroy");
                    return;
                }

                if (this.player.isBlockBreakingRestricted((World) this.world, blockposition, this.gameMode)) {
                    this.player.networkHandler.sendPacket(new PlayerActionResponseS2CPacket(blockposition, this.world.getBlockState(blockposition), packetplayinblockdig_enumplayerdigtype, false, "block action restricted"));
                    return;
                }

                this.startMiningTime = this.tickCounter;
                float f = 1.0F;

                iblockdata = this.world.getBlockState(blockposition);
                // CraftBukkit start - Swings at air do *NOT* exist.
                if (event.useInteractedBlock() == Event.Result.DENY) {
                    // If we denied a door from opening, we need to send a correcting update to the client, as it already opened the door.
                    BlockState data = this.world.getBlockState(blockposition);
                    if (data.getBlock() instanceof DoorBlock) {
                        // For some reason *BOTH* the bottom/top part have to be marked updated.
                        boolean bottom = data.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER;
                        this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.world, blockposition));
                        this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.world, bottom ? blockposition.up() : blockposition.down()));
                    } else if (data.getBlock() instanceof TrapdoorBlock) {
                        this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.world, blockposition));
                    }
                } else if (!iblockdata.isAir()) {
                    iblockdata.onBlockBreakStart(this.world, blockposition, this.player);
                    f = iblockdata.calcBlockBreakingDelta(this.player, this.player.world, blockposition);
                }

                if (event.useItemInHand() == Event.Result.DENY) {
                    // If we 'insta destroyed' then the client needs to be informed.
                    if (f > 1.0f)
                        this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.world, blockposition));
                    return;
                }
                org.bukkit.event.block.BlockDamageEvent blockEvent = BukkitEventFactory.callBlockDamageEvent(this.player, blockposition.getX(), blockposition.getY(), blockposition.getZ(), this.player.inventory.getMainHandStack(), f >= 1.0f);

                if (blockEvent.isCancelled()) {
                    // Let the client know the block still exists
                    this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.world, blockposition));
                    return;
                }

                if (blockEvent.getInstaBreak())
                    f = 2.0f;

                if (!iblockdata.isAir() && f >= 1.0F) {
                    this.finishMining(blockposition, packetplayinblockdig_enumplayerdigtype, "insta mine");
                } else {
                    if (this.mining)
                        this.player.networkHandler.sendPacket(new PlayerActionResponseS2CPacket(this.miningPos, this.world.getBlockState(this.miningPos), PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, false, "abort destroying since another started (client insta mine, server disagreed)"));

                    this.mining = true;
                    this.miningPos = blockposition.toImmutable();
                    int j = (int) (f * 10.0F);

                    this.world.setBlockBreakingInfo(this.player.getEntityId(), blockposition, j);
                    this.player.networkHandler.sendPacket(new PlayerActionResponseS2CPacket(blockposition, this.world.getBlockState(blockposition), packetplayinblockdig_enumplayerdigtype, true, "actual start of destroying"));
                    this.blockBreakingProgress = j;
                }
            } else if (packetplayinblockdig_enumplayerdigtype == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK) {
                if (blockposition.equals(this.miningPos)) {
                    int k = this.tickCounter - this.startMiningTime;

                    iblockdata = this.world.getBlockState(blockposition);
                    if (!iblockdata.isAir()) {
                        float f1 = iblockdata.calcBlockBreakingDelta(this.player, this.player.world, blockposition) * (float) (k + 1);

                        if (f1 >= 0.7F) {
                            this.mining = false;
                            this.world.setBlockBreakingInfo(this.player.getEntityId(), blockposition, -1);
                            this.finishMining(blockposition, packetplayinblockdig_enumplayerdigtype, "destroyed");
                            return;
                        }

                        if (!this.failedToMine) {
                            this.mining = false;
                            this.failedToMine = true;
                            this.failedMiningPos = blockposition;
                            this.failedStartMiningTime = this.startMiningTime;
                        }
                    }
                }

                this.player.networkHandler.sendPacket(new PlayerActionResponseS2CPacket(blockposition, this.world.getBlockState(blockposition), packetplayinblockdig_enumplayerdigtype, true, "stopped destroying"));
            } else if (packetplayinblockdig_enumplayerdigtype == PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK) {
                this.mining = false;
                if (!Objects.equals(this.miningPos, blockposition)) {
                    this.world.setBlockBreakingInfo(this.player.getEntityId(), this.miningPos, -1);
                    this.player.networkHandler.sendPacket(new PlayerActionResponseS2CPacket(this.miningPos, this.world.getBlockState(this.miningPos), packetplayinblockdig_enumplayerdigtype, true, "aborted mismatched destroying"));
                }

                this.world.setBlockBreakingInfo(this.player.getEntityId(), blockposition, -1);
                this.player.networkHandler.sendPacket(new PlayerActionResponseS2CPacket(blockposition, this.world.getBlockState(blockposition), packetplayinblockdig_enumplayerdigtype, true, "aborted destroying"));
            }

        }
    }

    @Shadow public void finishMining(BlockPos blockposition, PlayerActionC2SPacket.Action packetplayinblockdig_enumplayerdigtype, String s) {}

    @Inject(at = @At("HEAD"), method = "tryBreakBlock", cancellable = true)
    public void blockBreak(BlockPos blockposition, CallbackInfoReturnable<Boolean> ci) {
        org.bukkit.block.Block bblock = CraftBlock.at(world, blockposition);

        boolean isSwordNoBreak = !this.player.getMainHandStack().getItem().canMine(this.world.getBlockState(blockposition), this.world, blockposition, this.player);
        if (world.getBlockEntity(blockposition) == null && !isSwordNoBreak) {
            BlockUpdateS2CPacket packet = new BlockUpdateS2CPacket(this.world, blockposition);
            packet.state = Blocks.AIR.getDefaultState();
            this.player.networkHandler.sendPacket(packet);
        }
        BlockBreakEvent event = new BlockBreakEvent(bblock, (Player) ((IMixinServerEntityPlayer)this.player).getBukkitEntity());
        event.setCancelled(isSwordNoBreak);

        CraftServer.INSTANCE.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            if (isSwordNoBreak)
                ci.setReturnValue(false);

            this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.world, blockposition)); // Let the client know the block still exists

            // Brute force all possible updates
            for (Direction dir : Direction.values())
                this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(world, blockposition.offset(dir)));

            // Update any tile entity data for this block
            BlockEntity tileentity = this.world.getBlockEntity(blockposition);
            if (tileentity != null)
                this.player.networkHandler.sendPacket(tileentity.toUpdatePacket());

            ci.setReturnValue(false);
            return;
        }
    }

    public boolean interactResult = false;
    public boolean firedInteract = false;

    @Override
    public boolean getFiredInteractBF() {
        return firedInteract;
    }

    @Override
    public void setFiredInteractBF(boolean b) {
        this.firedInteract = b;
    }

    @Override
    public boolean getInteractResultBF() {
        return interactResult;
    }

    @Override
    public void setInteractResultBF(boolean b) {
        this.interactResult = b;
    }

    @Inject(at = @At("HEAD"), method = "interactBlock", cancellable = true)
    public void interactBlock(ServerPlayerEntity entityplayer, World world, ItemStack itemstack, Hand enumhand, BlockHitResult movingobjectpositionblock,
            CallbackInfoReturnable<ActionResult> ci) {
        System.out.println("interactBlock 1 DEBUG!");
        BlockPos blockposition = movingobjectpositionblock.getBlockPos();
        BlockState iblockdata = world.getBlockState(blockposition);
        ActionResult enuminteractionresult = ActionResult.PASS;
        boolean cancelledBlock = false;

        if (this.gameMode == GameMode.SPECTATOR) {
            NamedScreenHandlerFactory itileinventory = iblockdata.createScreenHandlerFactory(world, blockposition);
            cancelledBlock = !(itileinventory instanceof NamedScreenHandlerFactory);
        }

        if (entityplayer.getItemCooldownManager().isCoolingDown(itemstack.getItem()))
            cancelledBlock = true;

        PlayerInteractEvent event = BukkitEventFactory.callPlayerInteractEvent(entityplayer, Action.RIGHT_CLICK_BLOCK, blockposition, movingobjectpositionblock.getSide(), itemstack, cancelledBlock, enumhand);
        firedInteract = true;
        interactResult = event.useItemInHand() == Event.Result.DENY;

        if (event.useInteractedBlock() == Event.Result.DENY) {
            // If we denied a door from opening, we need to send a correcting update to the client, as it already opened the door.
            if (iblockdata.getBlock() instanceof DoorBlock) {
                boolean bottom = iblockdata.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER;
                entityplayer.networkHandler.sendPacket(new BlockUpdateS2CPacket(world, bottom ? blockposition.up() : blockposition.down()));
            } else if (iblockdata.getBlock() instanceof CakeBlock) {
                // TODO ((CraftPlayer)((IMixinServerEntityPlayer)entityplayer).getBukkitEntity()).sendHealthUpdate();
            }
            ((CraftPlayer)((IMixinServerEntityPlayer)entityplayer).getBukkitEntity()).updateInventory();
            enuminteractionresult = (event.useItemInHand() != Event.Result.ALLOW) ? ActionResult.SUCCESS : ActionResult.PASS;
        } else if (this.gameMode == GameMode.SPECTATOR) {
            NamedScreenHandlerFactory itileinventory = iblockdata.createScreenHandlerFactory(world, blockposition);

            if (itileinventory != null) {
                entityplayer.openHandledScreen(itileinventory);
                ci.setReturnValue(ActionResult.SUCCESS);
            } else ci.setReturnValue(ActionResult.PASS);
            return;
        } else {
            boolean flag = !entityplayer.getMainHandStack().isEmpty() || !entityplayer.getOffHandStack().isEmpty();
            boolean flag1 = entityplayer.shouldCancelInteraction() && flag;
            ItemStack itemstack1 = itemstack.copy();

            if (!flag1) {
                enuminteractionresult = iblockdata.onUse(world, entityplayer, enumhand, movingobjectpositionblock);

                if (enuminteractionresult.isAccepted()) {
                    Criteria.ITEM_USED_ON_BLOCK.test(entityplayer, blockposition, itemstack1);
                    ci.setReturnValue(enuminteractionresult);
                    return;
                }
            }

            if (!itemstack.isEmpty() && enuminteractionresult != ActionResult.SUCCESS && !interactResult) { // add !interactResult SPIGOT-764
                ItemUsageContext itemactioncontext = new ItemUsageContext(entityplayer, enumhand, movingobjectpositionblock);
                ActionResult enuminteractionresult1;

                if (this.gameMode.isCreative()) {
                    int i = itemstack.getCount();
                    enuminteractionresult1 = itemstack.useOnBlock(itemactioncontext/*, enumhand*/);
                    itemstack.setCount(i);
                } else enuminteractionresult1 = itemstack.useOnBlock(itemactioncontext/*, enumhand*/);

                if (enuminteractionresult1.isAccepted())
                    Criteria.ITEM_USED_ON_BLOCK.test(entityplayer, blockposition, itemstack1);

                ci.setReturnValue(enuminteractionresult1);
                return;
            }
        }
        ci.setReturnValue(enuminteractionresult);
    }

}