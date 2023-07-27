package org.cardboardpowered.impl.entity;

import com.google.common.base.Preconditions;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.entity.Raider;

public abstract class CardboardRaider extends MonsterImpl implements Raider {

    public CardboardRaider(CraftServer server, RaiderEntity entity) {
        super(server, entity);
    }

    @Override
    public RaiderEntity getHandle() {
        return (RaiderEntity) super.getHandle();
    }

    @Override
    public String toString() {
        return "Raider";
    }

    @Override
    public Block getPatrolTarget() {
        return getHandle().getPatrolTarget() == null ? null : CraftBlock.at((ServerWorld) getHandle().getWorld(), getHandle().getPatrolTarget());
    }

    @Override
    public void setPatrolTarget(Block block) {
        if (block == null) {
            getHandle().setPatrolTarget((BlockPos) null);
        } else {
            Preconditions.checkArgument(block.getWorld().equals(this.getWorld()), "Block must be in same world");
            getHandle().setPatrolTarget(new BlockPos(block.getX(), block.getY(), block.getZ()));
        }
    }

    @Override
    public boolean isPatrolLeader() {
        return getHandle().isPatrolLeader();
    }

    @Override
    public void setPatrolLeader(boolean leader) {
        getHandle().setPatrolLeader(leader);
    }

    @Override
    public boolean isCanJoinRaid() {
        return getHandle().canJoinRaid();
    }

    @Override
    public void setCanJoinRaid(boolean join) {
        getHandle().setAbleToJoinRaid(join);
    }

}