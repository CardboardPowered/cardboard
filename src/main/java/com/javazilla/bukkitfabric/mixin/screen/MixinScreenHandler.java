package com.javazilla.bukkitfabric.mixin.screen;

import java.util.List;

import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.cardboardpowered.impl.inventory.CardboardInventoryView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.google.common.base.Preconditions;
import com.javazilla.bukkitfabric.BukkitFabricMod;
import com.javazilla.bukkitfabric.interfaces.IMixinInventory;
import com.javazilla.bukkitfabric.interfaces.IMixinScreenHandler;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

@Mixin(ScreenHandler.class)
public abstract class MixinScreenHandler implements IMixinScreenHandler {

    public boolean checkReachable = true;
    //public abstract InventoryView getBukkitView();

    public CardboardInventoryView getBukkitView() {
        BukkitFabricMod.LOGGER.info("Using generic InventoryView for ScreenHandler (inventory provided by a mod?)");
        CraftInventory cbi = new CraftInventory(new SimpleInventory( ((ScreenHandler)(Object)this).getStacks().toArray(new ItemStack[0]) ));
        return new CardboardInventoryView(null, cbi, ((ScreenHandler)(Object)this));
    }

    @Shadow
    public DefaultedList<ItemStack> trackedStacks;

    @Shadow
    public List<Slot> slots;

    @Override
    public void transferTo(ScreenHandler other, org.cardboardpowered.impl.entity.HumanEntityImpl player) {
        CardboardInventoryView source = this.getBukkitView(), destination = ((IMixinScreenHandler)other).getBukkitView();
        source.setPlayerIfNotSet(player);
        destination.setPlayerIfNotSet(player);

        ((IMixinInventory)((CraftInventory) source.getTopInventory()).getInventory()).onClose(player);
        ((IMixinInventory)((CraftInventory) source.getBottomInventory()).getInventory()).onClose(player);
        ((IMixinInventory)((CraftInventory) destination.getTopInventory()).getInventory()).onOpen(player);
        ((IMixinInventory)((CraftInventory) destination.getBottomInventory()).getInventory()).onOpen(player);
    }

    private Text title;

    @Override
    public final Text getTitle() {
        if (null == this.title)
            this.title = new LiteralText("CARDBOARD: TITLE NOT SET!");
        return this.title;
    }

    @Override
    public final void setTitle(Text title) {
        this.title = title;
    }

    @Override
    public DefaultedList<ItemStack> getTrackedStacksBF() {
        return trackedStacks;
    }

    @Override
    public void setTrackedStacksBF(DefaultedList<ItemStack> trackedStacks) {
        this.trackedStacks = trackedStacks;
    }

    @Override
    public void setSlots(List<Slot> slots) {
        this.slots = slots;
    }

    @Override
    public void setCheckReachable(boolean bl) {
        this.checkReachable = bl;
    }

    // TODO InventoryDragEvent

}