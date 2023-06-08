package org.cardboardpowered.impl.block;

import org.cardboardpowered.impl.world.WorldImpl;

import com.google.common.base.Preconditions;

import me.isaiah.common.cmixin.IMixinBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.persistence.PersistentDataContainer;

public class CardboardBlockEntityState<T extends BlockEntity> extends CraftBlockState implements TileState {

    private final Class<T> tileEntityClass;
    private final T tileEntity;
    private final T snapshot;

    public CardboardBlockEntityState(Block block, Class<T> tileEntityClass) {
        super(block);
        this.tileEntityClass = tileEntityClass;

        WorldImpl world = (WorldImpl) this.getWorld();
        this.tileEntity = tileEntityClass.cast(world.getHandle().getBlockEntity(this.getPosition()));
        Preconditions.checkState(this.tileEntity != null, "BlockEntity = null. async access? " + block);
        this.snapshot = this.createSnapshot(tileEntity);
        this.load(snapshot);
    }

    @SuppressWarnings("unchecked")
    public CardboardBlockEntityState(Material material, T tileEntity) {
        super(material);
        this.tileEntityClass = (Class<T>) tileEntity.getClass();
        this.tileEntity = tileEntity;
        this.snapshot = this.createSnapshot(tileEntity);
        this.load(snapshot);
    }

    @SuppressWarnings("unchecked")
    private T createSnapshot(T tileEntity) {
        if (tileEntity == null) return null;

        IMixinBlockEntity ic = (IMixinBlockEntity)tileEntity;
        NbtCompound nbtTagCompound = ic.I_createNbtWithIdentifyingData();
        T snapshot = (T) BlockEntity.createFromNbt(getPosition(), data, nbtTagCompound);
        return snapshot;
    }

    private void copyData(T from, T to) {
        BlockPos pos = to.getPos();
        IMixinBlockEntity ic = (IMixinBlockEntity)tileEntity;
        NbtCompound nbtTagCompound = ic.I_createNbtWithIdentifyingData();
        to.setCachedState(data);
        to.readNbt(nbtTagCompound);
        to.pos = (pos);
    }

    public T getTileEntity() {
        return tileEntity;
    }

    public T getSnapshot() {
        return snapshot;
    }

    protected BlockEntity getTileEntityFromWorld() {
        requirePlaced();
        return ((WorldImpl) getWorld()).getHandle().getBlockEntity(getPosition());
    }

    public NbtCompound getSnapshotNBT() {
        applyTo(snapshot);
        IMixinBlockEntity ic = (IMixinBlockEntity)snapshot;
        return ic.I_createNbtWithIdentifyingData();
    }

    public void load(T blockEntity) {
        if (tileEntity != null && tileEntity != snapshot) copyData(blockEntity, snapshot);
    }

    public void applyTo(T blockEntity) {
        if (tileEntity != null && tileEntity != snapshot) copyData(snapshot, blockEntity);
    }

    public boolean isApplicable(BlockEntity tileEntity) {
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
        return ((com.javazilla.bukkitfabric.interfaces.IMixinBlockEntity)(Object)getSnapshot()).getPersistentDataContainer();
    }

	@Override
	public boolean isSnapshot() {
		// TODO Auto-generated method stub
		return false;
	}

}