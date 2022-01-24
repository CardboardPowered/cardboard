/*
 * Decompiled with CFR 0.151.
 */
package org.cardboardpowered.impl.entity;

import net.minecraft.entity.passive.MuleEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Mule;

public class CardboardMule extends CardboardChestedHorse implements Mule {

    public CardboardMule(CraftServer server, MuleEntity entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "Mule";
    }

    @Override
    public EntityType getType() {
        return EntityType.MULE;
    }

    @Override
    public Horse.Variant getVariant() {
        return Horse.Variant.MULE;
    }

}