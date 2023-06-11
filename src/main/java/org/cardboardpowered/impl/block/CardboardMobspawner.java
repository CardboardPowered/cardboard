package org.cardboardpowered.impl.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.cardboardpowered.impl.world.WorldImpl;

import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

@SuppressWarnings("deprecation")
public class CardboardMobspawner extends CardboardBlockEntityState<MobSpawnerBlockEntity> implements CreatureSpawner {

    public CardboardMobspawner(final Block block) {
        super(block, MobSpawnerBlockEntity.class);
    }

    public CardboardMobspawner(final Material material, MobSpawnerBlockEntity te) {
        super(material, te);
    }

    @Override
    public EntityType getSpawnedType() {
        //Identifier key = this.getSnapshot().getLogic().getEntityId(((WorldImpl)this.getWorld()).getHandle(), 
       //         new BlockPos(this.getBlock().getPosition().x, this.getBlock().getPosition().y, this.getBlock().getPosition().z));
        Identifier key = null;
        return (key == null) ? EntityType.PIG : EntityType.fromName(key.getPath());
    }

    @Override
    public void setSpawnedType(EntityType entityType) {
        if (entityType == null || entityType.getName() == null)
            throw new IllegalArgumentException("Can't spawn EntityType " + entityType + " from mobspawners!");
        // this.getSnapshot().getLogic().setEntityId(net.minecraft.entity.EntityType.get(entityType.getName()).get());
        
        Random rand = (this.isPlaced()) ? ((WorldImpl)this.getWorld()).getHandle().getRandom() : Random.create();
        this.getSnapshot().setEntityType(net.minecraft.entity.EntityType.get(entityType.getName()).get(), rand);
    }

    @Override
    public String getCreatureTypeName() {
        return "PIG";// TODO 1.17ify this.getSnapshot().getLogic().getEntityId().getPath();
    }

    @Override
    public void setCreatureTypeByName(String creatureType) {
        EntityType type = EntityType.fromName(creatureType);
        if (type == null) return;
        setSpawnedType(type);
    }

    @Override
    public int getDelay() {
        return this.getSnapshot().getLogic().spawnDelay;
    }

    @Override
    public void setDelay(int delay) {
        this.getSnapshot().getLogic().spawnDelay = delay;
    }

    @Override
    public int getMinSpawnDelay() {
        return this.getSnapshot().getLogic().minSpawnDelay;
    }

    @Override
    public void setMinSpawnDelay(int spawnDelay) {
        this.getSnapshot().getLogic().minSpawnDelay = spawnDelay;
    }

    @Override
    public int getMaxSpawnDelay() {
        return this.getSnapshot().getLogic().maxSpawnDelay;
    }

    @Override
    public void setMaxSpawnDelay(int spawnDelay) {
        this.getSnapshot().getLogic().maxSpawnDelay = spawnDelay;
    }

    @Override
    public int getMaxNearbyEntities() {
        return this.getSnapshot().getLogic().maxNearbyEntities;
    }

    @Override
    public void setMaxNearbyEntities(int maxNearbyEntities) {
        this.getSnapshot().getLogic().maxNearbyEntities = maxNearbyEntities;
    }

    @Override
    public int getSpawnCount() {
        return this.getSnapshot().getLogic().spawnCount;
    }

    @Override
    public void setSpawnCount(int count) {
        this.getSnapshot().getLogic().spawnCount = count;
    }

    @Override
    public int getRequiredPlayerRange() {
        return this.getSnapshot().getLogic().requiredPlayerRange;
    }

    @Override
    public void setRequiredPlayerRange(int requiredPlayerRange) {
        this.getSnapshot().getLogic().requiredPlayerRange = requiredPlayerRange;
    }

    @Override
    public int getSpawnRange() {
        return this.getSnapshot().getLogic().spawnRange;
    }

    @Override
    public void setSpawnRange(int spawnRange) {
        this.getSnapshot().getLogic().spawnRange = spawnRange;
    }

    @Override
    public boolean isActivated() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void resetTimer() {
        // TODO Auto-generated method stub
    }

    @Override
    public void setSpawnedItem(ItemStack arg0) {
        // TODO Auto-generated method stub
    }

}