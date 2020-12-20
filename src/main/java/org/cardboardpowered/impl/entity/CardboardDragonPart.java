package org.cardboardpowered.impl.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderDragonPart;
import org.bukkit.entity.Entity;

public class CardboardDragonPart extends CardboardComplexPart implements EnderDragonPart {

    public CardboardDragonPart(CraftServer server, net.minecraft.entity.boss.dragon.EnderDragonPart entity) {
        super(server, entity);
    }

    @Override
    public EnderDragon getParent() {
        return (EnderDragon) super.getParent();
    }

    @Override
    public net.minecraft.entity.boss.dragon.EnderDragonPart getHandle() {
        return (net.minecraft.entity.boss.dragon.EnderDragonPart) nms;
    }

    @Override
    public void damage(double amount) {
        getParent().damage(amount);
    }

    @Override
    public void damage(double amount, Entity source) {
        getParent().damage(amount, source);
    }

    @Override
    public double getHealth() {
        return getParent().getHealth();
    }

    @Override
    public void setHealth(double health) {
        getParent().setHealth(health);
    }

    @Override
    public double getAbsorptionAmount() {
        return getParent().getAbsorptionAmount();
    }

    @Override
    public void setAbsorptionAmount(double amount) {
        getParent().setAbsorptionAmount(amount);
    }

    @Override
    public double getMaxHealth() {
        return getParent().getMaxHealth();
    }

    @Override
    public void setMaxHealth(double health) {
        getParent().setMaxHealth(health);
    }

    @Override
    public void resetMaxHealth() {
        getParent().resetMaxHealth();
    }
}
