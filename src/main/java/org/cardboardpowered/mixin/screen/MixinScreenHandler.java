package org.cardboardpowered.mixin.screen;

import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.cardboardpowered.impl.inventory.CardboardInventoryView;
import org.cardboardpowered.impl.inventory.CustomInventoryView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

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

    public CardboardInventoryView getBukkitView() {
        CraftInventory cbi = new CraftInventory(new SimpleInventory( ((ScreenHandler)(Object)this).getStacks().toArray(new ItemStack[0]) ));
        return new CustomInventoryView(null, cbi, ((ScreenHandler)(Object)this));
    }

    @Shadow
    @Final
    @Mutable
    public DefaultedList<ItemStack> trackedStacks;

    @Shadow
    @Final
    @Mutable
    public DefaultedList<ItemStack> previousTrackedStacks;
    
    @Shadow
    @Final
    @Mutable
    public DefaultedList<Slot> slots;

    @Override
    public void transferTo(ScreenHandler other, CraftHumanEntity player) {
        CardboardInventoryView source = this.getBukkitView(), destination = ((IMixinScreenHandler)other).getBukkitView();
        source.setPlayerIfNotSet(player);
        destination.setPlayerIfNotSet(player);

        if ((source.getTopInventory() instanceof CustomInventoryView) || source.getBottomInventory() instanceof CustomInventoryView ||
                destination.getTopInventory() instanceof CustomInventoryView || destination.getBottomInventory() instanceof CustomInventoryView) {
            return;
        }

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
    public DefaultedList<ItemStack> cardboard_previousTrackedStacks() {
        return previousTrackedStacks;
    }
    
    @Override
    public void cardboard_previousTrackedStacks(DefaultedList<ItemStack> s) {
        this.previousTrackedStacks = s;
    }

    @Override
    public void setTrackedStacksBF(DefaultedList<ItemStack> trackedStacks) {
       this.trackedStacks = trackedStacks;
    }

    @Override
    public void cardboard_setSlots(DefaultedList<Slot> slots) {
        this.slots = slots;
    }

    @Override
    public void setCheckReachable(boolean bl) {
        this.checkReachable = bl;
    }

    // TODO InventoryDragEvent

}