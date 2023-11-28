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
		return this.getHandle().getSneezeProgress();
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
		return this.getHandle().isPlaying();
	}

	@Override
	public boolean isSitting() {
		return this.getHandle().isSitting();
	}

	@Override
	public boolean isSneezing() {
		return this.getHandle().isSneezing();
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
		this.getHandle().setPlaying(arg0);
	}

	@Override
	public void setSitting(boolean arg0) {
		this.getHandle().setSitting(arg0);
	}

	@Override
	public void setSneezeTicks(int arg0) {
		this.getHandle().setSneezeProgress(arg0);
	}

	@Override
	public void setSneezing(boolean arg0) {
		this.getHandle().setSneezing(arg0);
	}

	@Override
	public void setUnhappyTicks(int arg0) {
		this.getHandle().setAskForBambooTicks(arg0);
	}
	
	// 1.19.2:

	@Override
	public boolean isEating() {
		return this.getHandle().isEating();
	}

	@Override
	public boolean isScared() {
		return this.getHandle().isScaredByThunderstorm();
	}

	@Override
	public void setEating(boolean arg0) {
		this.getHandle().setEating(arg0);
	}

	@Override
	public void setOnBack(boolean arg0) {
		this.getHandle().setLyingOnBack(arg0);
	}

}
