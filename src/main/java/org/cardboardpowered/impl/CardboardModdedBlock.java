package org.cardboardpowered.impl;

import net.minecraft.block.Block;

public class CardboardModdedBlock implements CardboardModdedMaterial {

    private Block block;

    public CardboardModdedBlock(Block block) {
        this.block = block;
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

}