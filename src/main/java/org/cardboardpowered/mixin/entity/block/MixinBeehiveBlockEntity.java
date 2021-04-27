package org.cardboardpowered.mixin.entity.block;

import org.bukkit.craftbukkit.block.CraftBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;

import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.server.world.ServerWorld;

@Mixin(BeehiveBlockEntity.class)
public class MixinBeehiveBlockEntity extends BlockEntity {

    public MixinBeehiveBlockEntity(BlockEntityType<?> type) {
        super(type);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;stopRiding()V"), cancellable = true,
            method = "Lnet/minecraft/block/entity/BeehiveBlockEntity;tryEnterHive(Lnet/minecraft/entity/Entity;ZI)V")
    public void bukkitize_tryEnterHive(Entity entity, boolean flag, int i, CallbackInfo ci) {
        if (this.world != null) {
            org.bukkit.event.entity.EntityEnterBlockEvent event = new org.bukkit.event.entity.EntityEnterBlockEvent(((IMixinEntity)entity).getBukkitEntity(), CraftBlock.at((ServerWorld) world, getPos()));
            org.bukkit.Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                if (entity instanceof BeeEntity)
                    ((BeeEntity) entity).setCannotEnterHiveTicks(400);
                ci.cancel();
                return;
            }
        }
    }

}
