package org.cardboardpowered.impl.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

public class CardboardMinecart extends VehicleImpl implements Minecart {

    public CardboardMinecart(CraftServer server, AbstractMinecartEntity entity) {
        super(server, entity);
    }

    @Override
    public void setDamage(double damage) {
        getHandle().setDamageWobbleStrength((float) damage);
    }

    @Override
    public double getDamage() {
        return getHandle().getDamageWobbleStrength();
    }

    @Override
    public double getMaxSpeed() {
        return -1; // TODO
    }

    @Override
    public void setMaxSpeed(double speed) {
        // TODO
    }

    @Override
    public boolean isSlowWhenEmpty() {
        return false; // TODO
    }

    @Override
    public void setSlowWhenEmpty(boolean slow) {
        // TODO
    }

    @Override
    public Vector getFlyingVelocityMod() {
        return null; // TODO
    }

    @Override
    public void setFlyingVelocityMod(Vector flying) {
        // TODO
    }

    @Override
    public Vector getDerailedVelocityMod() {
        return null; // TODO
    }

    @Override
    public void setDerailedVelocityMod(Vector derailed) {
         // TODO
    }

    @Override
    public AbstractMinecartEntity getHandle() {
        return (AbstractMinecartEntity) nms;
    }

    @Override
    public void setDisplayBlock(MaterialData material) {
        if (material != null) {
            BlockState block = CraftMagicNumbers.getBlock(material);
            this.getHandle().setCustomBlock(block);
        } else {
            // Set block to air (default) and set the flag to not have a display block.
            this.getHandle().setCustomBlock(Blocks.AIR.getDefaultState());
            this.getHandle().setCustomBlockPresent(false);
        }
    }

    @Override
    public void setDisplayBlockData(BlockData blockData) {
        if (blockData != null) {
            BlockState block = ((CraftBlockData) blockData).getState();
            this.getHandle().setCustomBlock(block);
        } else {
            // Set block to air (default) and set the flag to not have a display block.
            this.getHandle().setCustomBlock(Blocks.AIR.getDefaultState());
            this.getHandle().setCustomBlockPresent(false);
        }
    }

    @Override
    public MaterialData getDisplayBlock() {
        BlockState blockData = getHandle().getContainedBlock();
        return CraftMagicNumbers.getMaterial(blockData);
    }

    @Override
    public BlockData getDisplayBlockData() {
        BlockState blockData = getHandle().getContainedBlock();
        return CraftBlockData.fromData(blockData);
    }

    @Override
    public void setDisplayBlockOffset(int offset) {
        getHandle().setCustomBlockOffset(offset);
    }

    @Override
    public int getDisplayBlockOffset() {
        return getHandle().getBlockOffset();
    }

    @Override
    public EntityType getType() {
        return EntityType.MINECART;
    }

}