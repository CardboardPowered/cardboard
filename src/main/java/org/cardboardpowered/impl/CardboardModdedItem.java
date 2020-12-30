package org.cardboardpowered.impl;

import net.minecraft.item.Item;

public class CardboardModdedItem implements CardboardModdedMaterial {

    private Item item;
    private String id;

    public CardboardModdedItem(String id) {
        this.id = id;
        this.item = net.minecraft.util.registry.Registry.ITEM.get(new net.minecraft.util.Identifier(id));
    }

    public CardboardModdedItem(Item item) {
        this.item = item;
    }

    @Override
    public short getDamage() {
        return (short) item.getMaxDamage();
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

    @Override
    public String getId() {
        return id;
    }

}