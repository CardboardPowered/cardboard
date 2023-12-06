package org.cardboardpowered.impl.inventory;

import org.bukkit.GameMode;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftContainer;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import com.javazilla.bukkitfabric.interfaces.IMixinScreenHandler;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;

import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;

public class CardboardInventoryView extends InventoryView {

    private final ScreenHandler container;
    private CraftHumanEntity player;
    private final CraftInventory viewing;
    private final String originalTitle;
    private String title;

    public CardboardInventoryView(HumanEntity player, Inventory viewing, ScreenHandler container) {
        this.player = (null !=player) ? (CraftHumanEntity) player : null;
        this.viewing = (CraftInventory) viewing;
        this.container = container;
        this.title = this.originalTitle = CraftChatMessage.fromComponent(((IMixinScreenHandler)container).getTitle() );
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
        return this.title;
    }

    public boolean isInTop(int rawSlot) {
        return rawSlot < viewing.getSize();
    }

    public ScreenHandler getHandle() {
        return container;
    }

	// @Override
	public @NotNull String getOriginalTitle() {
		return this.originalTitle;
	}

	// @Override
	public void setTitle(@NotNull String arg0) {
		sendInventoryTitleChange(this, title);
        this.title = arg0;
	}
	
    public static void sendInventoryTitleChange(InventoryView view, String title) {
        // Preconditions.checkArgument((view != null ? 1 : 0) != 0, (Object)"InventoryView cannot be null");
        // Preconditions.checkArgument((title != null ? 1 : 0) != 0, (Object)"Title cannot be null");
        // Preconditions.checkArgument((boolean)(view.getPlayer() instanceof Player), (Object)"NPCs are not currently supported for this function");
        // Preconditions.checkArgument((boolean)view.getTopInventory().getType().isCreatable(), (Object)"Only creatable inventories can have their title changed");
        ServerPlayerEntity entityPlayer = (ServerPlayerEntity)((CraftHumanEntity)view.getPlayer()).getHandle();
        int containerId = entityPlayer.currentScreenHandler.syncId;
        ScreenHandlerType windowType = CraftContainer.getNotchInventoryType(view.getTopInventory());
        entityPlayer.networkHandler.sendPacket(new OpenScreenS2CPacket(containerId, windowType, CraftChatMessage.fromString(title)[0]));
        ((Player)view.getPlayer()).updateInventory();
    }

}