package org.cardboardpowered.mixin.world;

import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.Entity;
import net.minecraft.world.ChunkRegion;

@Mixin(ChunkRegion.class)
public class MixinChunkRegion {

    public boolean addEntity(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        return spawnEntity(entity);
    }

    @Shadow
    public boolean spawnEntity(Entity entity) {
        return false;
    }

}