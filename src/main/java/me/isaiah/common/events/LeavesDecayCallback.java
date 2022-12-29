package me.isaiah.common.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerLoginNetworkHandler.State;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

/**
 * TODO: This is here for testing; will be moved to icommonlib
 */
public interface LeavesDecayCallback {
    
    Event<LeavesDecayCallback> EVENT = EventFactory.createArrayBacked(LeavesDecayCallback.class,
        (listeners) -> (state, world, pos) -> {
            for (LeavesDecayCallback listener : listeners) {
                ActionResult result = listener.interact(state, world, pos);
 
                if(result != ActionResult.PASS) {
                    return result;
                }
            }
 
        return ActionResult.PASS;
    });
 
    ActionResult interact(BlockState state, ServerWorld world, BlockPos pos);
}