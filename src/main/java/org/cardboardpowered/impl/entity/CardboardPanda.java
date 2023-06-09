package org.cardboardpowered.impl.entity;

import com.google.common.base.Preconditions;
import net.minecraft.entity.passive.PandaEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Panda;

public class CardboardPanda extends AnimalsImpl implements Panda {

    public CardboardPanda(CraftServer server, PandaEntity entity) {
        super(server, entity);
    }

    @Override
    public PandaEntity getHandle() {
        return (PandaEntity) super.getHandle();
    }

    @Override
    public EntityType getType() {
        return EntityType.PANDA;
    }

    @Override
    public String toString() {
        return "Panda";
    }

    @Override
    public Gene getMainGene() {
        return fromNms(getHandle().getMainGene());
    }

    @Override
    public void setMainGene(Gene gene) {
        getHandle().setMainGene(toNms(gene));
    }

    @Override
    public Gene getHiddenGene() {
        return fromNms(getHandle().getHiddenGene());
    }

    @Override
    public void setHiddenGene(Gene gene) {
        getHandle().setHiddenGene(toNms(gene));
    }

    public static Gene fromNms(PandaEntity.Gene gene) {
        Preconditions.checkArgument(gene != null, "Gene must not be null");
        return Gene.values()[gene.ordinal()];
    }

    public static PandaEntity.Gene toNms(Gene gene) {
        Preconditions.checkArgument(gene != null, "Gene must not be null");
        return PandaEntity.Gene.values()[gene.ordinal()];
    }

	@Override
	public int getEatingTicks() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSneezeTicks() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getUnhappyTicks() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isOnBack() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRolling() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSitting() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSneezing() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setEatingTicks(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setIsOnBack(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRolling(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSitting(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSneezeTicks(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSneezing(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUnhappyTicks(int arg0) {
		// TODO Auto-generated method stub
		
	}

}
