package org.bukkit.craftbukkit.block;

import org.cardboardpowered.impl.world.WorldImpl;
import com.javazilla.bukkitfabric.interfaces.IMixinBlockEntity;
import com.google.common.base.Preconditions;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.persistence.PersistentDataContainer;

public class CraftBlockEntityState<T extends BlockEntity> extends CraftBlockState implements TileState {

    private final Class<T> tileEntityClass;
    private final T tileEntity;
    private final T snapshot;

    public CraftBlockEntityState(Block block, Class<T> tileEntityClass) {
        super(block);

        this.tileEntityClass = tileEntityClass;

        WorldImpl world = (WorldImpl) this.getWorld();
        this.tileEntity = tileEntityClass.cast(world.getHandle().getBlockEntity(this.getPosition()));
        Preconditions.checkState(this.tileEntity != null, "Tile is null. asynchronous access? " + block);

        this.snapshot = this.createSnapshot(tileEntity);
        this.load(snapshot);
    }

    @SuppressWarnings("unchecked")
    public CraftBlockEntityState(Material material, T tileEntity) {
        super(material);

        this.tileEntityClass = (Class<T>) tileEntity.getClass();
        this.tileEntity = tileEntity;

        this.snapshot = this.createSnapshot(tileEntity);
        this.load(snapshot);
    }

    @SuppressWarnings("unchecked")
    private T createSnapshot(T tileEntity) {
        if (tileEntity == null)
            return null;

        CompoundTag nbtTagCompound = tileEntity.toTag(new CompoundTag());
        T snapshot = (T) BlockEntity.createFromTag(data, nbtTagCompound);

        return snapshot;
    }

    private void copyData(T from, T to) {
        BlockPos pos = to.getPos();
        CompoundTag nbtTagCompound = from.toTag(new CompoundTag());
        to.fromTag(data, nbtTagCompound);

        to.setPos(pos);
    }

    // gets the wrapped TileEntity
    protected T getTileEntity() {
        return tileEntity;
    }

    protected T getSnapshot() {
        return snapshot;
    }

    protected BlockEntity getTileEntityFromWorld() {
        requirePlaced();
        return ((WorldImpl) getWorld()).getHandle().getBlockEntity(getPosition());
    }

    public CompoundTag getSnapshotNBT() {
        applyTo(snapshot);

        return snapshot.toTag(new CompoundTag());
    }

    protected void load(T tileEntity) {
        if (tileEntity != null && tileEntity != snapshot)
            copyData(tileEntity, snapshot);
    }

    protected void applyTo(T tileEntity) {
        if (tileEntity != null && tileEntity != snapshot)
            copyData(snapshot, tileEntity);
    }

    protected boolean isApplicable(BlockEntity tileEntity) {
        return tileEntityClass.isInstance(tileEntity);
    }

    @Override
    public boolean update(boolean force, boolean applyPhysics) {
        boolean result = super.update(force, applyPhysics);

        if (result && this.isPlaced()) {
            BlockEntity tile = getTileEntityFromWorld();

            if (isApplicable(tile)) {
                applyTo(tileEntityClass.cast(tile));
                tile.markDirty();
            }
        }

        return result;
    }

    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        return ((IMixinBlockEntity)(Object)getSnapshot()).getPersistentDataContainer();
    }

}