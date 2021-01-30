package org.cardboardpowered.impl.inventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import net.minecraft.screen.ScreenHandler;

public class CustomInventoryView extends CardboardInventoryView {

    public CustomInventoryView(HumanEntity player, Inventory viewing, ScreenHandler container) {
        super(player, viewing, container);
    }

}