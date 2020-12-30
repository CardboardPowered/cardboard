package org.cardboardpowered.impl;

public interface CardboardModdedMaterial {

    boolean isBlock();

    boolean isItem();

    boolean isEdible();

    short getDamage();

    String getId();

}