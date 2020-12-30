package org.cardboardpowered.impl;

public interface CardboardModdedMaterial {

    public boolean isBlock();

    public boolean isItem();

    public boolean isEdible();

    short getDamage();

    String getId();

}