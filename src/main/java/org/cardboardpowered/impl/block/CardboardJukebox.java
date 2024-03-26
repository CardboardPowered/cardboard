package org.cardboardpowered.impl.block;

import net.minecraft.block.Blocks;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.inventory.JukeboxInventory;
import org.cardboardpowered.impl.inventory.CraftInventoryJukebox;
import org.cardboardpowered.impl.world.WorldImpl;
import org.jetbrains.annotations.NotNull;

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
        ItemStack record = this.getSnapshot().getStack();
        return CraftItemStack.asBukkitCopy(record);
    }

    @Override
    public void setRecord(org.bukkit.inventory.ItemStack record) {
        ItemStack nms = CraftItemStack.asNMSCopy(record);
        this.getSnapshot().setStack(nms);
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
        boolean result = !jukebox.getStack().isEmpty();
        jukebox.dropRecord();
        
        return result;
    }

    @Override
    public void stopPlaying() {
        this.requirePlaced();
        BlockEntity tileEntity = this.getTileEntityFromWorld();
        if (!(tileEntity instanceof JukeboxBlockEntity)) {
            return;
        }
        JukeboxBlockEntity jukebox = (JukeboxBlockEntity)tileEntity;
        jukebox.isPlaying = false;
        this.getWorld().playEffect(this.getLocation(), Effect.IRON_DOOR_CLOSE, 0);
    }

	@Override
	public @NotNull JukeboxInventory getInventory() {
        if (!this.isPlaced()) {
            return this.getSnapshotInventory();
        }
        return new CraftInventoryJukebox((Inventory)this.getTileEntity());
	}

	@Override
	public @NotNull JukeboxInventory getSnapshotInventory() {
        return new CraftInventoryJukebox((Inventory)this.getSnapshot());
	}

	@Override
	public boolean hasRecord() {
        return this.getHandle().get(JukeboxBlock.HAS_RECORD) != false && !this.getPlaying().isAir();
	}

	@Override
	public boolean startPlaying() {
        this.requirePlaced();
        BlockEntity tileEntity = this.getTileEntityFromWorld();
        if (!(tileEntity instanceof JukeboxBlockEntity)) {
            return false;
        }
        JukeboxBlockEntity jukebox = (JukeboxBlockEntity)tileEntity;
        net.minecraft.item.ItemStack record = jukebox.getStack();
        if (record.isEmpty() || this.isPlaying()) {
            return false;
        }
        jukebox.isPlaying = true;
        jukebox.recordStartTick = jukebox.tickCount;
        this.getWorld().playEffect(this.getLocation(), Effect.RECORD_PLAY, (Object)CraftMagicNumbers.getMaterial(record.getItem()));
        return true;
	}

}