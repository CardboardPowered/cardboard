package com.javazilla.bukkitfabric.impl.entity;

import com.google.common.base.Preconditions;
import net.minecraft.entity.passive.CatEntity;
import org.bukkit.DyeColor;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Cat;
import org.bukkit.entity.EntityType;

public class CatImpl extends TameableAnimalImpl implements Cat {

    public CatImpl(CraftServer server, CatEntity entity) {
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
        return Type.values()[getHandle().getCatType()];
    }

    @Override
    public void setCatType(Type type) {
        Preconditions.checkArgument(type != null, "Cannot have null Type");

        getHandle().setCatType(type.ordinal());
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

}