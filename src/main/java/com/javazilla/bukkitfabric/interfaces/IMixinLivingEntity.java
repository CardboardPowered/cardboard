package com.javazilla.bukkitfabric.interfaces;

import org.cardboardpowered.impl.CardboardAttributable;

public interface IMixinLivingEntity {

    int getExpReward();

    CardboardAttributable cardboard_getAttr();

}