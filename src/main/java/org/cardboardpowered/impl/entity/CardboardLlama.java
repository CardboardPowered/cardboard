package org.cardboardpowered.impl.entity;

import com.google.common.base.Preconditions;
import net.minecraft.entity.passive.LlamaEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.inventory.LlamaInventory;

public class CardboardLlama extends CardboardChestedHorse implements Llama {

    public CardboardLlama(CraftServer server, LlamaEntity entity) {
        super(server, entity);
    }

    @Override
    public LlamaEntity getHandle() {
        return (LlamaEntity)super.getHandle();
    }

    @Override
    public Llama.Color getColor() {
        return Llama.Color.values()[this.getHandle().getVariant()];
    }

    @Override
    public void setColor(Llama.Color color) {
        Preconditions.checkArgument(color != null, "color");
        this.getHandle().setVariant(color.ordinal());
    }

    @Override
    public LlamaInventory getInventory() {
        return null;//new CardboardInventoryLlama(this.getHandle().items);
    }

    @Override
    public int getStrength() {
        return this.getHandle().getStrength();
    }

    @Override
    public void setStrength(int strength) {
        Preconditions.checkArgument(1 <= strength && strength <= 5, "strength must be [1,5]");
        if (strength == this.getStrength()) {
            return;
        }
        //this.getHandle().setStrength(strength);
        //this.getHandle().onChestedStatusChanged();
    }

    @Override
    public Horse.Variant getVariant() {
        return Horse.Variant.LLAMA;
    }

    @Override
    public String toString() {
        return "Llama";
    }

    @Override
    public EntityType getType() {
        return EntityType.LLAMA;
    }

    @Override
    public void rangedAttack(LivingEntity arg0, float arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setChargingAttack(boolean bl) {
        // TODO Auto-generated method stub
        
    }

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
