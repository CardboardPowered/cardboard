package org.cardboardpowered.impl.inventory;

import org.bukkit.GameMode;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import com.javazilla.bukkitfabric.interfaces.IMixinScreenHandler;

import net.minecraft.screen.ScreenHandler;

public class CardboardInventoryView extends InventoryView {

    private final ScreenHandler container;
    private CraftHumanEntity player;
    private final CraftInventory viewing;

    public CardboardInventoryView(HumanEntity player, Inventory viewing, ScreenHandler container) {
        this.player = (null !=player) ? (CraftHumanEntity) player : null;
        this.viewing = (CraftInventory) viewing;
        this.container = container;
    }

    public void setPlayerIfNotSet(HumanEntity player) {
        if (null == this.player)
            this.player = (CraftHumanEntity) player;
    }

    @Override
    public Inventory getTopInventory() {
        return viewing;
    }

    @Override
    public Inventory getBottomInventory() {
        return player.getInventory();
    }

    @Override
    public HumanEntity getPlayer() {
        return player;
    }

    @Override
    public InventoryType getType() {
        InventoryType type = viewing.getType();
        return (type == InventoryType.CRAFTING && player.getGameMode() == GameMode.CREATIVE) ? InventoryType.CREATIVE : type;
    }

    @Override
    public void setItem(int slot, ItemStack item) {
        net.minecraft.item.ItemStack stack = CraftItemStack.asNMSCopy(item);
        if (slot >= 0) container.getSlot(slot).setStack(stack);
        else player.getHandle().dropStack(stack);
    }

    @Override
    public ItemStack getItem(int slot) {
        return (slot < 0) ? null : CraftItemStack.asCraftMirror(container.getSlot(slot).getStack());
    }

    @Override
    public String getTitle() {
        return CraftChatMessage.fromComponent(((IMixinScreenHandler)container).getTitle());
    }

    public boolean isInTop(int rawSlot) {
        return rawSlot < viewing.getSize();
    }

    public ScreenHandler getHandle() {
        return container;
    }

}