package org.cardboardpowered.impl.entity;

import com.google.common.base.Preconditions;
import com.javazilla.bukkitfabric.BukkitFabricMod;

import net.minecraft.entity.passive.CatEntity;

import org.bukkit.DyeColor;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Cat;
import org.bukkit.entity.EntityType;

public class CardboardCat extends TameableAnimalImpl implements Cat {

    public CardboardCat(CraftServer server, CatEntity entity) {
        super(server, entity);
    }

    @Override
    public CatEntity getHandle() {
        return (CatEntity) super.getHandle();
    }

    @Override
    public EntityType getType() {
        return EntityType.CAT;
    }

    @Override
    public String toString() {
        return "Cat";
    }

    @Override
    public Type getCatType() {
        return Type.ALL_BLACK;// TODO: 1.19: Type.values()[getHandle().getCatType()];
    }

    @Override
    public void setCatType(Type type) {
        if (null == type) {
            BukkitFabricMod.LOGGER.info("Error: Cannot have null Cat Type, defaulting to ALL_BLACK");
            type = Type.ALL_BLACK;
        }

       // getHandle().setCatType(type.ordinal());
    }

    @SuppressWarnings("deprecation")
    @Override
    public DyeColor getCollarColor() {
        return DyeColor.getByWoolData((byte) getHandle().getCollarColor().getId());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setCollarColor(DyeColor color) {
        getHandle().setCollarColor(net.minecraft.util.DyeColor.byId(color.getWoolData()));
    }

    @Override
    public boolean isHeadUp() {
        return !getHandle().isHeadDown();
    }

    @Override
    public boolean isLyingDown() {
        return getHandle().isSitting();
    }

    @Override
    public void setHeadUp(boolean bl) {
        getHandle().setHeadDown(!bl);
    }

    @Override
    public void setLyingDown(boolean bl) {
        getHandle().setSitting(bl);
    }

}