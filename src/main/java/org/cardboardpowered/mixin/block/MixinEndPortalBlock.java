package org.cardboardpowered.mixin.block;

import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.block.BlockState;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

@MixinInfo(events = {"EntityPortalEnterEvent"})
@Mixin(EndPortalBlock.class)
public class MixinEndPortalBlock {

    @Inject(at = @At("HEAD"), method = "onEntityCollision")
    public void callBukkitEvent_EntityPortalEnterEvent(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (world instanceof ServerWorld && !entity.hasVehicle() && !entity.hasPassengers() && entity.canUsePortals() && VoxelShapes.matchesAnywhere(VoxelShapes.cuboid(entity.getBoundingBox().offset(-pos.getX(), -pos.getY(), -pos.getZ())), state.getOutlineShape(world, pos), BooleanBiFunction.AND)) {
            EntityPortalEnterEvent event = new EntityPortalEnterEvent(((IMixinEntity)entity).getBukkitEntity(), new org.bukkit.Location(((IMixinWorld)world).getWorldImpl(), pos.getX(), pos.getY(), pos.getZ()));
            Bukkit.getPluginManager().callEvent(event);
        }
    }

}