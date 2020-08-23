package com.javazilla.bukkitfabric.mixin;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Mixin(ServerPlayerInteractionManager.class)
public class MixinServerPlayerInteractionManager {

    @Shadow public ServerPlayerEntity player;
    @Shadow public ServerWorld world;

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
        }
    }

}