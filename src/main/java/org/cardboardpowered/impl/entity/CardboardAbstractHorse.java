package org.cardboardpowered.impl.entity;

import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Horse;
import org.bukkit.inventory.AbstractHorseInventory;
import org.cardboardpowered.impl.inventory.CardboardInventoryAbstractHorse;
import org.cardboardpowered.interfaces.IHorseBaseEntity;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.AbstractHorseEntity;

public abstract class CardboardAbstractHorse
extends AnimalsImpl
implements AbstractHorse {

    public CardboardAbstractHorse(CraftServer server, AbstractHorseEntity  entity) {
        super(server, entity);
    }

    @Override
    public AbstractHorseEntity getHandle() {
        return (AbstractHorseEntity)this.nms;
    }

    @Override
    public void setVariant(Horse.Variant variant) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getDomestication() {
        return this.getHandle().getTemper();
    }

    @Override
    public void setDomestication(int value) {
        Validate.isTrue(value >= 0, "Domestication cannot be less than zero");
        Validate.isTrue(value <= this.getMaxDomestication(), "Domestication cannot be greater than the max domestication");
        this.getHandle().setTemper(value);
    }

    @Override
    public int getMaxDomestication() {
        return this.getHandle().getMaxTemper();
    }

    @Override
    public void setMaxDomestication(int value) {
        Validate.isTrue(value > 0, "Max domestication cannot be zero or less");
        // TODO: this.getHandle().maxDomestication = value;
    }

    @Override
    public double getJumpStrength() {
        return this.getHandle().getJumpStrength();
    }

    @Override
    public void setJumpStrength(double strength) {
        Validate.isTrue(strength >= 0.0, "Jump strength cannot be less than zero");
        this.getHandle().getAttributeInstance(EntityAttributes.HORSE_JUMP_STRENGTH).setBaseValue(strength);
    }

    @Override
    public boolean isTamed() {
        return this.getHandle().isTame();
    }

    @Override
    public void setTamed(boolean tamed) {
        this.getHandle().setTame(tamed);
    }

    @Override
    public AnimalTamer getOwner() {
        if (this.getOwnerUUID() == null) {
            return null;
        }
        return this.getServer().getOfflinePlayer(this.getOwnerUUID());
    }

    @Override
    public void setOwner(AnimalTamer owner) {
        if (owner != null) {
            this.setTamed(true);
            // TODO this.getHandle().setGoalTarget(null, null, false);
            this.setOwnerUUID(owner.getUniqueId());
        } else {
            this.setTamed(false);
            this.setOwnerUUID(null);
        }
    }

    public UUID getOwnerUUID() {
        return this.getHandle().getOwnerUuid();
    }

    public void setOwnerUUID(UUID uuid) {
        this.getHandle().setOwnerUuid(uuid);
    }

    @Override
    public AbstractHorseInventory getInventory() {
        return new CardboardInventoryAbstractHorse( ((IHorseBaseEntity)this.getHandle()).cardboard$get_items() );
    }

}
