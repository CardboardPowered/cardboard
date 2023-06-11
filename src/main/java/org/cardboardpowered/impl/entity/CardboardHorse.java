package org.cardboardpowered.impl.entity;

import net.minecraft.entity.passive.HorseColor;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.HorseMarking;

import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.inventory.HorseInventory;
import org.cardboardpowered.impl.inventory.CardboardInventoryHorse;
import org.cardboardpowered.interfaces.IHorseBaseEntity;
import org.jetbrains.annotations.Nullable;

public class CardboardHorse
extends CardboardAbstractHorse
implements Horse {
    public CardboardHorse(CraftServer server, HorseEntity entity) {
        super(server, entity);
    }

    @Override
    public HorseEntity getHandle() {
        return (HorseEntity)super.getHandle();
    }

    @Override
    public Horse.Variant getVariant() {
        return Horse.Variant.HORSE;
    }

    @Override
    public Horse.Color getColor() {
        return Horse.Color.values()[this.getHandle().getVariant().getId()];
    }

    @Override
    public void setColor(Horse.Color color) {
        Validate.notNull((Object)color, "Color cannot be null");
       // this.getHandle().setVariant(HorseColor.byIndex(color.ordinal()), this.getHandle().getMarking());
    }

    @Override
    public Horse.Style getStyle() {
        return Horse.Style.values()[this.getHandle().getMarking().getId()];
    }

    @Override
    public void setStyle(Horse.Style style) {
        Validate.notNull((Object)style, "Style cannot be null");
       // this.getHandle().setVariant(this.getHandle().getColor(), HorseMarking.byIndex(style.ordinal()));
    }

    @Override
    public boolean isCarryingChest() {
        return false;
    }

    @Override
    public void setCarryingChest(boolean chest) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public HorseInventory getInventory() {
        return new CardboardInventoryHorse( ((IHorseBaseEntity)this.getHandle()).cardboard$get_items() );
    }

    @Override
    public String toString() {
        return "CraftHorse{variant=" + (Object)((Object)this.getVariant()) + ", owner=" + this.getOwner() + '}';
    }

    @Override
    public EntityType getType() {
        return EntityType.HORSE;
    }

    // Paper API start
    @Override
    public boolean isEating() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEatingGrass() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRearing() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setEating(boolean bl) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setEatingGrass(boolean bl) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setRearing(boolean bl) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public UUID getOwnerUniqueId() {
        // TODO Auto-generated method stub
        return null;
    }
    // Paper API end

	@Override
	public boolean isEatingHaystack() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setEatingHaystack(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

}