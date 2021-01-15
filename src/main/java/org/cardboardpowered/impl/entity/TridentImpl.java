package org.cardboardpowered.impl.entity;

import net.minecraft.entity.projectile.TridentEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;

public class TridentImpl extends ArrowImpl implements Trident {

    public TridentImpl(CraftServer server, TridentEntity entity) {
        super(server, entity);
    }

    @Override
    public TridentEntity getHandle() {
        return (TridentEntity) super.getHandle();
    }

    @Override
    public String toString() {
        return "Trident";
    }

    @Override
    public EntityType getType() {
        return EntityType.TRIDENT;
    }

    @Override
    public ItemStack getItem() {
        // TODO Auto-generated method stub
        return super.getItemStack();
    }

    @Override
    public void setItem(ItemStack arg0) {
        // TODO Auto-generated method stub
    }

}