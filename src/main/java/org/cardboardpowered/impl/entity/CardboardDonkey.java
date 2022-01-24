package org.cardboardpowered.impl.entity;

import net.minecraft.entity.passive.DonkeyEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;

public class CardboardDonkey extends CardboardChestedHorse implements Donkey {

    public CardboardDonkey(CraftServer server, DonkeyEntity entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "Donkey";
    }

    @Override
    public EntityType getType() {
        return EntityType.DONKEY;
    }

    @Override
    public Horse.Variant getVariant() {
        return Horse.Variant.DONKEY;
    }

}