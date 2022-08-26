package org.cardboardpowered.impl.inventory;

import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

public class CardboardChestBlockDoubleInventory implements NamedScreenHandlerFactory {

    private final ChestBlockEntity tileentitychest;
    private final ChestBlockEntity tileentitychest1;
    public final net.minecraft.inventory.DoubleInventory inventorylargechest;

    public CardboardChestBlockDoubleInventory(ChestBlockEntity tileentitychest, ChestBlockEntity tileentitychest1, net.minecraft.inventory.DoubleInventory inventorylargechest) {
        this.tileentitychest = tileentitychest;
        this.tileentitychest1 = tileentitychest1;
        this.inventorylargechest = inventorylargechest;
    }

    @Override
    public ScreenHandler createMenu(int i, PlayerInventory playerinventory, PlayerEntity entityhuman) {
        if (tileentitychest.checkUnlocked(entityhuman) && tileentitychest1.checkUnlocked(entityhuman)) {
            tileentitychest.checkLootInteraction(playerinventory.player);
            tileentitychest1.checkLootInteraction(playerinventory.player);
            return GenericContainerScreenHandler.createGeneric9x6(i, playerinventory, inventorylargechest);
        } else return null;
    }

    @Override
    public Text getDisplayName() {
        return (Text) (tileentitychest.hasCustomName() ? tileentitychest.getDisplayName() : (tileentitychest1.hasCustomName() ? tileentitychest1.getDisplayName() : Text.translatable("container.chestDouble")));
    }

}