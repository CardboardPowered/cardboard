/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftLeaves extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.Leaves {

    public CraftLeaves() {
        super();
    }

    public CraftLeaves(net.minecraft.block.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftLeaves

    private static final net.minecraft.state.property.IntProperty DISTANCE = getInteger(net.minecraft.block.LeavesBlock.class, "distance");
    private static final net.minecraft.state.property.BooleanProperty PERSISTENT = getBoolean(net.minecraft.block.LeavesBlock.class, "persistent");

    @Override
    public boolean isPersistent() {
        return get(PERSISTENT);
    }

    @Override
    public void setPersistent(boolean persistent) {
        set(PERSISTENT, persistent);
    }

    @Override
    public int getDistance() {
        return get(DISTANCE);
    }

    @Override
    public void setDistance(int distance) {
        set(DISTANCE, distance);
    }

	@Override
	public int getMaximumDistance() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMinimumDistance() {
		// TODO Auto-generated method stub
		return 0;
	}
}
