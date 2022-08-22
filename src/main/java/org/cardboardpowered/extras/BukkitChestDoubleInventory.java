package org.cardboardpowered.extras;

import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class BukkitChestDoubleInventory implements NamedScreenHandlerFactory {
    public final net.minecraft.inventory.DoubleInventory inventory;
    private final ChestBlockEntity leftChest;
    private final ChestBlockEntity rightChest;

    public BukkitChestDoubleInventory(ChestBlockEntity leftChest, ChestBlockEntity rightChest,
                                      net.minecraft.inventory.DoubleInventory inventory) {
        this.leftChest = leftChest;
        this.rightChest = rightChest;
        this.inventory = inventory;
    }

    @Override
    public Text getDisplayName() {
        return this.leftChest.hasCustomName() ? this.leftChest.getDisplayName() :
                (this.rightChest.hasCustomName() ? this.rightChest.getDisplayName() :
                        new TranslatableText("container.chestDouble"));
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        if (this.leftChest.checkUnlocked(player) && this.rightChest.checkUnlocked(player)) {
            this.leftChest.checkLootInteraction(inv.player);
            this.rightChest.checkLootInteraction(inv.player);
            return GenericContainerScreenHandler.createGeneric9x6(syncId, inv, this.inventory);
        } else {
            return null;
        }
    }
}
