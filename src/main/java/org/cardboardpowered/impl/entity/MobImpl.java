package org.cardboardpowered.impl.entity;

import net.minecraft.entity.mob.MobEntity;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.CraftServer;
import org.cardboardpowered.impl.entity.LivingEntityImpl;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.loot.LootTable;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;

public abstract class MobImpl extends LivingEntityImpl implements Mob {

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

}
