package org.cardboardpowered.mixin.world.inventory;

import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.RemoteSlot;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.cardboardpowered.impl.inventory.CustomInventoryView;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import org.cardboardpowered.CardboardMod;
import org.cardboardpowered.bridge.world.ContainerBridge;
import org.cardboardpowered.bridge.world.inventory.AbstractContainerMenuBridge;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin implements AbstractContainerMenuBridge {

    public boolean checkReachable = true;

    @Override
    public InventoryView getBukkitView() {
        CraftInventory cbi = new CraftInventory(new SimpleContainer( ((AbstractContainerMenu)(Object)this).getItems().toArray(new ItemStack[0]) ));
        return new CustomInventoryView(null, cbi, ((AbstractContainerMenu)(Object)this));
    }

    @Shadow
    @Final
    @Mutable
    public NonNullList<ItemStack> lastSlots;

    /**
     * field_29206
     * 1.21.4: previousTrackedStacks
     * 1.21.8: trackedSlots
     */
    @Shadow
    @Final
    @Mutable
    public NonNullList<ItemStack> remoteSlots;
    
    @Shadow
    @Final
    @Mutable
    public NonNullList<Slot> slots;

    @Shadow
    public ItemStack getCarried() {
        return null;
    }

    @Shadow
    private RemoteSlot remoteCarried;

    @Shadow
    private @Nullable ContainerSynchronizer synchronizer;

    @Override
    public void transferTo(AbstractContainerMenu other, CraftHumanEntity player) {
        InventoryView source = this.getBukkitView(), destination = ((AbstractContainerMenuBridge)other).getBukkitView();

        // Fallback views (CustomInventoryView) have no player attached, so their bottom
        // inventory can not be resolved - skip them instead of throwing a NullPointerException.
        if (source instanceof CustomInventoryView || destination instanceof CustomInventoryView
                || source.getPlayer() == null || destination.getPlayer() == null) {
            return;
        }

        openOrClose( ((CraftInventory) source.getTopInventory()).getInventory(), player, false);
        openOrClose( ((CraftInventory) source.getBottomInventory()).getInventory(), player, false);
        openOrClose( ((CraftInventory) destination.getTopInventory()).getInventory(), player, true);
        openOrClose( ((CraftInventory) destination.getBottomInventory()).getInventory(), player, true);
    }

    public void openOrClose(Container in, CraftHumanEntity plr, boolean open) {
        if (in instanceof ContainerBridge) {
            ContainerBridge imi = (ContainerBridge) in;
            if (open) {
                imi.onOpen(plr);
            } else {
                imi.onClose(plr);
            }
        } else {
            if (FabricLoader.getInstance().isDevelopmentEnvironment())
                CardboardMod.LOGGER.info("Debug: " + in + " is not of type IMixinInventory");
        }
    }

    private Component title_cb;

    @Override
    public final Component getTitle() {
        if (null == this.title_cb)
            this.title_cb = Component.nullToEmpty(" nul ");
        return this.title_cb;
    }

    @Override
    public void setTitle(Component title) {
        this.title_cb = title;
    }

    @Override
    public NonNullList<ItemStack> getTrackedStacksBF() {
        return lastSlots;
    }
    
    @Override
    public NonNullList<ItemStack> cardboard_previousTrackedStacks() {
        return remoteSlots;
    }
    
    @Override
    public void cardboard_previousTrackedStacks(NonNullList<ItemStack> s) {
        this.remoteSlots = s;
    }

    @Override
    public void setTrackedStacksBF(NonNullList<ItemStack> trackedStacks) {
       this.lastSlots = trackedStacks;
    }

    @Override
    public void cardboard_setSlots(NonNullList<Slot> slots) {
        this.slots = slots;
    }

    @Override
    public void cardboard$setCheckReachable(boolean bl) {
        this.checkReachable = bl;
    }

    // TODO InventoryDragEvent

    // CraftBukkit start - from synchronizeCarriedToRemote
    @Override
    public void cardboard$broadcastCarriedItem() {
        ItemStack carried = this.getCarried();
        this.remoteCarried.force(carried);
        if (this.synchronizer != null) {
            this.synchronizer.sendCarriedChange((AbstractContainerMenu)(Object)this, carried.copy());
        }
    }
    // CraftBukkit end
}