package org.cardboardpowered.impl.block;

import net.minecraft.block.BellBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Direction;

import org.bukkit.Material;
import org.bukkit.block.Bell;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class CardboardBell extends CardboardBlockEntityState<BellBlockEntity> implements Bell {

    public CardboardBell(Block block) {
        super(block, BellBlockEntity.class);
    }

    public CardboardBell(Material material, BellBlockEntity te) {
        super(material, te);
    }

	@Override
	public int getResonatingTicks() {
        return this.isResonating() ? ((BellBlockEntity)this.getSnapshot()).ringTicks : 0;
	}

	@Override
	public int getShakingTicks() {
        return ((BellBlockEntity)this.getSnapshot()).ringTicks;
	}

	@Override
	public boolean isResonating() {
        return ((BellBlockEntity)this.getSnapshot()).resonating;
	}

	@Override
	public boolean isShaking() {
        return ((BellBlockEntity)this.getSnapshot()).ringing;
	}

    public boolean ring(org.bukkit.entity.Entity entity, BlockFace direction) {
        // Preconditions.checkArgument((direction == null || direction.isCartesian() ? 1 : 0) != 0, (String)"direction must be cartesian, given %s", (Object)direction);
        BlockEntity tileEntity = this.getTileEntityFromWorld();
        if (tileEntity == null) {
            return false;
        }
        net.minecraft.entity.Entity nmsEntity = entity != null ? ((CraftEntity)entity).getHandle() : null;
        Direction enumDirection = CraftBlock.blockFaceToNotch(direction);
        return ((BellBlock)Blocks.BELL).ring(nmsEntity, this.world.getHandle(), this.getPosition(), enumDirection);
    }

    public boolean ring(org.bukkit.entity.Entity entity) {
        return this.ring(entity, null);
    }

    public boolean ring(BlockFace direction) {
        return this.ring(null, direction);
    }

    public boolean ring() {
        return this.ring(null, null);
    }

}