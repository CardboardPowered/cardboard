package org.cardboardpowered.impl;

import net.minecraft.block.Block;

public class CardboardModdedBlock implements CardboardModdedMaterial {

    private Block block;
    private String id;

    public CardboardModdedBlock(String id) {
        this.id = id;
        this.block = net.minecraft.registry.Registries.BLOCK.get(new net.minecraft.util.Identifier(id));
    }

    public CardboardModdedBlock(Block block) {
        this.block = block;
    }

    @Override
    public short getDamage() {
        return 0;
    }

    @Override
    public boolean isBlock() {
        return true;
    }

    @Override
    public boolean isItem() {
        return false;
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