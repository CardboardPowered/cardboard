package org.bukkit.craftbukkit.block;

import net.minecraft.world.level.block.entity.PotentSulfurBlockEntity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.PotentSulfur;

// 26.2: Potent Sulfur is a new block entity. The Bukkit interface adds no
// members beyond TileState, so this is a plain block-entity state wrapper.
public class CraftPotentSulfur extends CraftBlockEntityState<PotentSulfurBlockEntity> implements PotentSulfur {

    public CraftPotentSulfur(World world, PotentSulfurBlockEntity blockEntity) {
        super(world, blockEntity);
    }

    protected CraftPotentSulfur(CraftPotentSulfur state, Location location) {
        super(state, location);
    }

    @Override
    public CraftPotentSulfur copy() {
        return new CraftPotentSulfur(this, null);
    }

    @Override
    public CraftPotentSulfur copy(Location location) {
        return new CraftPotentSulfur(this, location);
    }
}
