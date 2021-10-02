package org.cardboardpowered.mixin.world;

import org.bukkit.Chunk;
import org.cardboardpowered.impl.world.CardboardChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinWorldChunk;

import net.minecraft.world.chunk.WorldChunk;

@Mixin(WorldChunk.class)
public class MixinWorldChunk implements IMixinWorldChunk {

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

}
