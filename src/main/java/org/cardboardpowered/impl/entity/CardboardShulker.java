package org.cardboardpowered.impl.entity;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.ShulkerEntity;

import java.lang.reflect.Field;

import org.bukkit.DyeColor;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Shulker;

public class CardboardShulker extends CardboardGolem implements Shulker {

    public CardboardShulker(CraftServer server, ShulkerEntity entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "Shulker";
    }

    @Override
    public EntityType getType() {
        return EntityType.SHULKER;
    }

    @Override
    public ShulkerEntity getHandle() {
        return (ShulkerEntity) nms;
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    @Override
    public DyeColor getColor() {
        TrackedData<Byte> d = null;
        try {
            Field f = ShulkerEntity.class.getDeclaredField("field_7343");
            f.setAccessible(true);
            d = (TrackedData<Byte>) f.get(null);
        } catch (Exception e) {
        }
        return DyeColor.getByWoolData(getHandle().getDataTracker().get(d));
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    @Override
    public void setColor(DyeColor color) {
        TrackedData<Byte> d = null;
        try {
            Field f = ShulkerEntity.class.getDeclaredField("field_7343");
            f.setAccessible(true);
            d = (TrackedData<Byte>) f.get(null);
        } catch (Exception e) {
        }
        getHandle().getDataTracker().set(d, (color == null) ? 16 : color.getWoolData());
    }

}