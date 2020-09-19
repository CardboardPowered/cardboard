package com.javazilla.bukkitfabric.mixin.screen;

import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Mixin;

import com.google.common.base.Preconditions;
import com.javazilla.bukkitfabric.interfaces.IMixinInventory;
import com.javazilla.bukkitfabric.interfaces.IMixinScreenHandler;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

@Mixin(ScreenHandler.class)
public abstract class MixinScreenHandler implements IMixinScreenHandler {

    public boolean checkReachable = true;
    public abstract InventoryView getBukkitView();

    @Override
    public void transferTo(ScreenHandler other, org.bukkit.craftbukkit.entity.CraftHumanEntity player) {
        InventoryView source = this.getBukkitView(), destination = ((IMixinScreenHandler)other).getBukkitView();
        ((IMixinInventory)((CraftInventory) source.getTopInventory()).getInventory()).onClose(player);
        ((IMixinInventory)((CraftInventory) source.getBottomInventory()).getInventory()).onClose(player);
        ((IMixinInventory)((CraftInventory) destination.getTopInventory()).getInventory()).onOpen(player);
        ((IMixinInventory)((CraftInventory) destination.getBottomInventory()).getInventory()).onOpen(player);
    }

    private Text title;

    @Override
    public final Text getTitle() {
        Preconditions.checkState(this.title != null, "Title not set");
        return this.title;
    }

    @Override
    public final void setTitle(Text title) {
        Preconditions.checkState(this.title == null, "Title already set");
        this.title = title;
    }

    // TODO InventoryDragEvent

}