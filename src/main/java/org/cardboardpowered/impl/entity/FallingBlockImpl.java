package org.cardboardpowered.impl.entity;

import net.kyori.adventure.text.Component;
import net.minecraft.entity.FallingBlockEntity;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.jetbrains.annotations.Nullable;

public class FallingBlockImpl extends CraftEntity implements FallingBlock {

    public FallingBlockImpl(CraftServer server, FallingBlockEntity entity) {
        super(entity);
    }

    @Override
    public FallingBlockEntity getHandle() {
        return (FallingBlockEntity) nms;
    }

    @Override
    public String toString() {
        return "BF_FallingBlock";
    }

    @Override
    public EntityType getType() {
        return EntityType.FALLING_BLOCK;
    }

    @Override
    public Material getMaterial() {
        return getBlockData().getMaterial();
    }

    @Override
    public BlockData getBlockData() {
        return CraftBlockData.fromData(getHandle().getBlockState());
    }

    @Override
    public boolean getDropItem() {
        return getHandle().dropItem;
    }

    @Override
    public void setDropItem(boolean drop) {
        getHandle().dropItem = drop;
    }

    @Override
    public boolean canHurtEntities() {
        return getHandle().hurtEntities;
    }

    @Override
    public void setHurtEntities(boolean hurtEntities) {
        getHandle().hurtEntities = hurtEntities;
    }

    @Override
    public void setTicksLived(int value) {
        super.setTicksLived(value);

        // Second field for EntityFallingBlock
        getHandle().timeFalling = value;
    }

    @Override
    public @Nullable Component customName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void customName(@Nullable Component arg0) {
        // TODO Auto-generated method stub
        
    }

	@Override
	public boolean doesAutoExpire() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void shouldAutoExpire(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
    public float getDamagePerBlock() {
        return this.getHandle().fallHurtAmount;
    }


	@Override
	public int getMaxDamage() {
        return this.getHandle().fallHurtMax;
	}

	@Override
    public void setDamagePerBlock(float damage) {
        // Preconditions.checkArgument(((double)damage >= 0.0 ? 1 : 0) != 0, (String)"damage must be >= 0.0, given %s", (Object)Float.valueOf(damage));
        this.getHandle().fallHurtAmount = damage;
        if ((double)damage > 0.0) {
            this.setHurtEntities(true);
        }
    }

	@Override
	public void setMaxDamage(int damage) {
        this.getHandle().fallHurtMax = damage;
        if (damage > 0) {
            this.setHurtEntities(true);
        }
	}

}