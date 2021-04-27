package org.cardboardpowered.mixin.entity.block;

import org.bukkit.Location;
import org.spongepowered.asm.mixin.Mixin;

import com.javazilla.bukkitfabric.interfaces.IMixinLockableContainerBlockEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.util.math.BlockPos;

@Mixin(LockableContainerBlockEntity.class)
public class MixinLockableContainerBlockEntity implements IMixinLockableContainerBlockEntity {

    @Override
    public Location getLocation() {
        LockableContainerBlockEntity lc = (LockableContainerBlockEntity)(Object)this;
        BlockPos pos = lc.getPos();
        return new Location(((IMixinWorld)lc.world).getWorldImpl(), pos.getX(), pos.getY(), pos.getZ());
    }

}