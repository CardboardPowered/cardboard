package org.cardboardpowered.impl;

import net.minecraft.item.Item;

public class CardboardModdedItem implements CardboardModdedMaterial {

    private Item item;

    public CardboardModdedItem(Item item) {
        this.item = item;
    }

    @Override
    public boolean isBlock() {
        return false;
    }

    @Override
    public boolean isItem() {
        return true;
    }

    @Override
    public boolean isEdible() {
        return false;
    }

}