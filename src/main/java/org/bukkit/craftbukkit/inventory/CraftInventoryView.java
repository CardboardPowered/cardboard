package org.bukkit.craftbukkit.inventory;

import org.bukkit.GameMode;
import org.cardboardpowered.impl.entity.HumanEntityImpl;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import com.javazilla.bukkitfabric.interfaces.IMixinScreenHandler;

import net.minecraft.screen.ScreenHandler;

public class CraftInventoryView extends InventoryView {

    private final ScreenHandler container;
    private final HumanEntityImpl player;
    private final CraftInventory viewing;

    public CraftInventoryView(HumanEntity player, Inventory viewing, ScreenHandler container) {
        this.player = (HumanEntityImpl) player;
        this.viewing = (CraftInventory) viewing;
        this.container = container;
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
        if (type == InventoryType.CRAFTING && player.getGameMode() == GameMode.CREATIVE)
            return InventoryType.CREATIVE;
        return type;
    }

    @Override
    public void setItem(int slot, ItemStack item) {
        net.minecraft.item.ItemStack stack = CraftItemStack.asNMSCopy(item);
        if (slot >= 0)
            container.getSlot(slot).setStack(stack);
        else
            player.getHandle().dropStack(stack);
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0)
            return null;
        return CraftItemStack.asCraftMirror(container.getSlot(slot).getStack());
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
