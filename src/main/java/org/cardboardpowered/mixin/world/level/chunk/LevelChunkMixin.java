package org.cardboardpowered.mixin.world.level.chunk;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.CraftChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.cardboardpowered.bridge.world.level.chunk.LevelChunkBridge;

@Mixin(LevelChunk.class)
public class LevelChunkMixin implements LevelChunkBridge {

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
        // Singleplayer clients build their own LevelChunks on a ClientLevel; Bukkit only models server chunks.
        if (null == bukkit && ((LevelChunk)(Object)this).getLevel() instanceof ServerLevel) {
            this.bukkit = new CraftChunk((LevelChunk)(Object)this);
        }
    }

    /*
    @Override
    public BlockState setBlockState(BlockPos blockposition, BlockState iblockdata, boolean moved, boolean doPlace) {
    	// TODO: support doPlace
    	return ((WorldChunk)(Object)this).setBlockState(blockposition, iblockdata, moved);
    }
    */

}
