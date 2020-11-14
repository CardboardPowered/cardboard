package org.cardboardpowered.impl.block;

import net.minecraft.block.Blocks;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.item.ItemStack;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;

import org.cardboardpowered.impl.world.WorldImpl;

public class CardboardJukebox extends CardboardBlockEntityState<JukeboxBlockEntity> implements Jukebox {

    public CardboardJukebox(final Block block) {
        super(block, JukeboxBlockEntity.class);
    }

    public CardboardJukebox(final Material material, JukeboxBlockEntity blockEntity) {
        super(material, blockEntity);
    }

    @Override
    public boolean update(boolean force, boolean applyPhysics) {
        boolean result = super.update(force, applyPhysics);
        if (result && this.isPlaced() && this.getType() == Material.JUKEBOX) {
            WorldImpl world = (WorldImpl) this.getWorld();
            Material record = this.getPlaying();
            world.getHandle().setBlockState(this.getPosition(), Blocks.JUKEBOX.getDefaultState().with(JukeboxBlock.HAS_RECORD, !(record == Material.AIR)), 3);
            world.playEffect(this.getLocation(), Effect.RECORD_PLAY, record);
        }
        return result;
    }

    @Override
    public Material getPlaying() {
        return getRecord().getType();
    }

    @Override
    public void setPlaying(Material record) {
        if (record == null || CraftMagicNumbers.getItem(record) == null) record = Material.AIR;
        setRecord(new org.bukkit.inventory.ItemStack(record));
    }

    @Override
    public org.bukkit.inventory.ItemStack getRecord() {
        ItemStack record = this.getSnapshot().getRecord();
        return CraftItemStack.asBukkitCopy(record);
    }

    @Override
    public void setRecord(org.bukkit.inventory.ItemStack record) {
        ItemStack nms = CraftItemStack.asNMSCopy(record);
        this.getSnapshot().setRecord(nms);
        this.data = this.data.with(JukeboxBlock.HAS_RECORD, !nms.isEmpty());
    }

    @Override
    public boolean isPlaying() {
        return getHandle().get(JukeboxBlock.HAS_RECORD);
    }

    @Override
    public boolean eject() {
        requirePlaced();
        BlockEntity tileEntity = this.getTileEntityFromWorld();
        if (!(tileEntity instanceof JukeboxBlockEntity)) return false;

        JukeboxBlockEntity jukebox = (JukeboxBlockEntity) tileEntity;
        boolean result = !jukebox.getRecord().isEmpty();
        WorldImpl world = (WorldImpl) this.getWorld();
        ((JukeboxBlock) Blocks.JUKEBOX).removeRecord(world.getHandle(), getPosition());
        return result;
    }

    @Override
    public void stopPlaying() {
        // TODO Auto-generated method stub
    }

}