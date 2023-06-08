package org.cardboardpowered.mixin.world;

import org.bukkit.Chunk;
import org.cardboardpowered.impl.world.CardboardChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.cardboardpowered.interfaces.IWorldChunk;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

@Mixin(WorldChunk.class)
public class MixinWorldChunk implements IWorldChunk {

    private Chunk bukkit;

    @Inject(method = "<init>*", at = @At("TAIL"))
    public void setBukkitChunk(CallbackInfo ci) {
        try {
            cardboard_set();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Chunk getBukkitChunk() {
        cardboard_set();
        return bukkit;
    }
    
    public void cardboard_set() {
        if (null == bukkit) {
            this.bukkit = new CardboardChunk((WorldChunk)(Object)this);
        }
    }
    
    @Override
    public BlockState setBlockState(BlockPos blockposition, BlockState iblockdata, boolean moved, boolean doPlace) {
    	// TODO: support doPlace
    	return ((WorldChunk)(Object)this).setBlockState(blockposition, iblockdata, moved);
    }

}
