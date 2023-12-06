package org.cardboardpowered.impl.entity;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.CraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.loot.LootTable;

import com.destroystokyo.paper.entity.Pathfinder;
import com.javazilla.bukkitfabric.BukkitFabricMod;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;

public class MobImpl extends LivingEntityImpl implements Mob {

    protected final Random random = new Random();

    public MobImpl(CraftServer server, MobEntity entity) {
        super(server, entity);
    }

    @Override
    public void setTarget(LivingEntity target) {
        // TODO
    }

    @Override
    public LivingEntityImpl getTarget() {
        if (getHandle().getTarget() == null) return null;
        return (LivingEntityImpl) ((IMixinEntity)getHandle().getTarget()).getBukkitEntity();
    }

    @Override
    public void setAware(boolean aware) {
        // TODO
    }

    @Override
    public boolean isAware() {
        // TODO
        return false;
    }

    @Override
    public MobEntity getHandle() {
        return (MobEntity) nms;
    }

    @Override
    public String toString() {
        return "CraftMob";
    }

    @Override
    public void setLootTable(LootTable table) {
        getHandle().lootTable = (table == null) ? null : CraftNamespacedKey.toMinecraft(table.getKey());
    }

    @Override
    public LootTable getLootTable() {
        if (getHandle().lootTable == null)
            getHandle().lootTable = getHandle().getLootTable();

        NamespacedKey key = CraftNamespacedKey.fromMinecraft(getHandle().lootTable);
        return Bukkit.getLootTable(key);
    }

    @Override
    public void setSeed(long seed) {
        getHandle().lootTableSeed = seed;
    }

    @Override
    public long getSeed() {
        return getHandle().lootTableSeed;
    }

    // Paper start
    public boolean isInDaylight() {
        if (getHandle().world.isDay()) {
            float f = getHandle().getBrightnessAtEyes();
            BlockPos blockPos = getHandle().getVehicle() instanceof BoatEntity ? BlockPos.ofFloored(getHandle().getX(), Math.round(getHandle().getY()), getHandle().getZ()).up() : BlockPos.ofFloored(getHandle().getX(), Math.round(getHandle().getY()), getHandle().getZ());
            if (f > 0.5f && BukkitFabricMod.random.nextFloat() * 30.0f < (f - 0.4f) * 2.0f && getHandle().world.isSkyVisible(blockPos)) return true;
        }
        return false;
    }

    @Override
    public Pathfinder getPathfinder() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getHeadRotationSpeed() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getMaxHeadPitch() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void lookAt(@NotNull Location arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void lookAt(@NotNull Entity arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void lookAt(@NotNull Location arg0, float arg1, float arg2) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void lookAt(@NotNull Entity arg0, float arg1, float arg2) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void lookAt(double arg0, double arg1, double arg2) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void lookAt(double arg0, double arg1, double arg2, float arg3, float arg4) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isLeftHanded() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setLeftHanded(boolean bl) {
        // TODO Auto-generated method stub
    }

	@Override
	public Sound getAmbientSound() {
		return Sound.AMBIENT_CAVE;
	}

	@Override
	public int getPossibleExperienceReward() {
        return this.getHandle().getXpToDrop();
	}


}
