package com.javazilla.bukkitfabric.mixin.screen;

import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinAnvilScreenHandler;
import com.javazilla.bukkitfabric.interfaces.IMixinScreenHandlerContext;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;

import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;

@Mixin(AnvilScreenHandler.class)
public class MixinAnvilScreenHandler extends MixinForgingScreenHandler implements IMixinAnvilScreenHandler {

    // TODO Add AnvilPrepareEvent

    public int maximumRepairCost_BF = 40;
    public CraftInventoryView bukkitEntity;

    @Shadow public String newItemName;
    @Shadow public Property levelCost;

    @Override
    public CraftInventoryView getBukkitView() {
        if (bukkitEntity != null)
            return bukkitEntity;

        org.bukkit.craftbukkit.inventory.CraftInventory inventory = new org.bukkit.craftbukkit.inventory.CraftInventoryAnvil(
                ((IMixinScreenHandlerContext)context).getLocation(), this.input, this.output, (AnvilScreenHandler)(Object)this);
        bukkitEntity = new CraftInventoryView((Player)((IMixinServerEntityPlayer)this.player).getBukkitEntity(), inventory, (AnvilScreenHandler)(Object)this);
        return bukkitEntity;
    }

    @Override
    public String getNewItemName_BF() {
        return newItemName;
    }

    @Override
    public int getLevelCost_BF() {
        return levelCost.get();
    }

    @Override
    public void setLevelCost_BF(int i) {
        levelCost.set(i);
    }

    @Override
    public int getMaxRepairCost_BF() {
        return maximumRepairCost_BF;
    }

    @Override
    public void setMaxRepairCost_BF(int levels) {
        maximumRepairCost_BF = levels;
    }

}