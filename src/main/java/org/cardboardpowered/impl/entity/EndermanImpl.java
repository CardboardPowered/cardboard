package org.cardboardpowered.impl.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.mob.EndermanEntity;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.material.MaterialData;

@SuppressWarnings("deprecation")
public class EndermanImpl extends MonsterImpl implements Enderman {

    public EndermanImpl(CraftServer server, EndermanEntity entity) {
        super(server, entity);
    }

    @Override
    public MaterialData getCarriedMaterial() {
        BlockState blockData = getHandle().getCarriedBlock();
        return (blockData == null) ? Material.AIR.getNewData((byte) 0) : CraftMagicNumbers.getMaterial(blockData);
    }

    @Override
    public BlockData getCarriedBlock() {
        BlockState blockData = getHandle().getCarriedBlock();
        return (blockData == null) ? null : CraftBlockData.fromData(blockData);
    }

    @Override
    public void setCarriedMaterial(MaterialData data) {
        getHandle().setCarriedBlock(CraftMagicNumbers.getBlock(data));
    }

    @Override
    public void setCarriedBlock(BlockData blockData) {
        getHandle().setCarriedBlock(blockData == null ? null : ((CraftBlockData) blockData).getState());
    }

    @Override
    public EndermanEntity getHandle() {
        return (EndermanEntity) nms;
    }

    @Override
    public String toString() {
        return "FabricEnderman";
    }

    @Override
    public EntityType getType() {
        return EntityType.ENDERMAN;
    }
}
