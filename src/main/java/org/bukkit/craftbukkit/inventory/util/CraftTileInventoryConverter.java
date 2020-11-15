package org.bukkit.craftbukkit.inventory.util;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.cardboardpowered.impl.inventory.CardboardBrewerInventory;
import org.cardboardpowered.impl.inventory.CardboardFurnaceInventory;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.DropperBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.SmokerBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class CraftTileInventoryConverter implements CraftInventoryCreator.InventoryConverter {

    public abstract net.minecraft.inventory.Inventory getTileEntity();

    @Override
    public Inventory createInventory(InventoryHolder holder, InventoryType type) {
        return getInventory(getTileEntity());
    }

    @Override
    public Inventory createInventory(InventoryHolder holder, InventoryType type, String title) {
        net.minecraft.inventory.Inventory inventory = getTileEntity();
        if (inventory instanceof LootableContainerBlockEntity)
            ((LootableContainerBlockEntity) inventory).setCustomName(CraftChatMessage.fromStringOrNull(title));
        return getInventory(inventory);
    }

    public Inventory getInventory(net.minecraft.inventory.Inventory tileEntity) {
        return new CraftInventory(tileEntity);
    }

    public static class Furnace extends CraftTileInventoryConverter {

        @Override
        public net.minecraft.inventory.Inventory getTileEntity() {
            AbstractFurnaceBlockEntity furnace = new FurnaceBlockEntity(BlockPos.ORIGIN, null);
            //furnace.setLocation(CraftServer.server.getWorld(World.OVERWORLD), BlockPos.ORIGIN);
            furnace.setWorld(CraftServer.server.getWorld(World.OVERWORLD));
            return furnace;
        }

        @Override
        public Inventory createInventory(InventoryHolder owner, InventoryType type, String title) {
            net.minecraft.inventory.Inventory tileEntity = getTileEntity();
            ((AbstractFurnaceBlockEntity) tileEntity).setCustomName(CraftChatMessage.fromStringOrNull(title));
            return getInventory(tileEntity);
        }

        @Override
        public Inventory getInventory(net.minecraft.inventory.Inventory tileEntity) {
            return new CardboardFurnaceInventory((AbstractFurnaceBlockEntity) tileEntity);
        }
    }

    public static class BrewingStand extends CraftTileInventoryConverter {

        @Override
        public net.minecraft.inventory.Inventory getTileEntity() {
            return new BrewingStandBlockEntity(null, null);
        }

        @Override
        public Inventory createInventory(InventoryHolder holder, InventoryType type, String title) {
            net.minecraft.inventory.Inventory tileEntity = getTileEntity();
            if (tileEntity instanceof BrewingStandBlockEntity)
                ((BrewingStandBlockEntity) tileEntity).setCustomName(CraftChatMessage.fromStringOrNull(title));
            return getInventory(tileEntity);
        }

        @Override
        public Inventory getInventory(net.minecraft.inventory.Inventory tileEntity) {
            return new CardboardBrewerInventory(tileEntity);
        }
    }

    public static class Dispenser extends CraftTileInventoryConverter {
        @Override
        public net.minecraft.inventory.Inventory getTileEntity() {
            return new DispenserBlockEntity(null, null);
        }
    }

    public static class Dropper extends CraftTileInventoryConverter {
        @Override
        public net.minecraft.inventory.Inventory getTileEntity() {
            return new DropperBlockEntity(null, null);
        }
    }

    public static class Hopper extends CraftTileInventoryConverter {
        @Override
        public net.minecraft.inventory.Inventory getTileEntity() {
            return new HopperBlockEntity(null, null);
        }
    }

    public static class BlastFurnace extends CraftTileInventoryConverter {
        @Override
        public net.minecraft.inventory.Inventory getTileEntity() {
            return new BlastFurnaceBlockEntity(null, null);
        }
    }

    public static class Lectern extends CraftTileInventoryConverter {
        @Override
        public net.minecraft.inventory.Inventory getTileEntity() {
            return new LecternBlockEntity(null, null).inventory;
        }
    }

    public static class Smoker extends CraftTileInventoryConverter {
        @Override
        public net.minecraft.inventory.Inventory getTileEntity() {
            return new SmokerBlockEntity(null, null);
        }
    }

}