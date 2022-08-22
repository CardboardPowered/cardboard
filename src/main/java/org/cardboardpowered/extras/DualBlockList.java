package org.cardboardpowered.extras;

import net.minecraft.util.math.BlockPos;
import org.bukkit.World;
import org.bukkit.block.Block;
import java.util.AbstractList;
import java.util.List;

public class DualBlockList extends AbstractList<Block> {
    private final List<BlockPos> moved;
    private final List<BlockPos> broken;
    private final World world;

    public DualBlockList(List<BlockPos> moved, List<BlockPos> broken, World world) {
        this.moved = moved;
        this.broken = broken;
        this.world = world;
    }

    @Override
    public org.bukkit.block.Block get(int index) {
        if (index >= this.size() || index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        BlockPos pos = index < this.moved.size() ? this.moved.get(index) : this.broken.get(index - this.moved.size());
        return this.world.getBlockAt(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public int size() {
        return this.moved.size() + this.broken.size();
    }
}
