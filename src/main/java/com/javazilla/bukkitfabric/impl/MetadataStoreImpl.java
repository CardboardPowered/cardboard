package com.javazilla.bukkitfabric.impl;

import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.MetadataStoreBase;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public class MetadataStoreImpl {

    public static MetadataStoreBase<Entity> newEntityMetadataStore() {
        return new MetaDataStoreBase<Entity>() {
            @Override
            protected String disambiguate(Entity entity, String metadataKey) {
                return entity.getUniqueId().toString() + ":" + metadataKey;
            }
        };
    }


    public static MetaDataStoreBase<OfflinePlayer> newPlayerMetadataStore() {
        return new MetaDataStoreBase<OfflinePlayer>() {
            @Override
            protected String disambiguate(OfflinePlayer player, String metadataKey) {
                return player.getUniqueId() + ":" + metadataKey;
            }
        };
    }

    public static MetaDataStoreBase<World> newWorldMetadataStore() {
        return new MetaDataStoreBase<World>() {
            @Override
            protected String disambiguate(World world, String metadataKey) {
                return world.getUID().toString() + ":" + metadataKey;
            }
        };
    }

    public static MetaDataStoreBase<Block> newBlockMetadataStore(World world) {
        return new MetaDataStoreBase<Block>() {
            @Override
            protected String disambiguate(Block block, String metadataKey) {
                return Integer.toString(block.getX()) + ":" + Integer.toString(block.getY()) + ":" + Integer.toString(block.getZ()) + ":" + metadataKey;
            }

            @Override
            public List<MetadataValue> getMetadata(Block block, String metadataKey) {
                if (block.getWorld() == world)
                    return super.getMetadata(block, metadataKey);
                else throw new IllegalArgumentException("Block does not belong to world " + world.getName());
            }

            @Override
            public boolean hasMetadata(Block block, String metadataKey) {
                if (block.getWorld() == world)
                    return super.hasMetadata(block, metadataKey);
                else throw new IllegalArgumentException("Block does not belong to world " + world.getName());
            }

            @Override
            public void removeMetadata(Block block, String metadataKey, Plugin owningPlugin) {
                if (block.getWorld() == world)
                    super.removeMetadata(block, metadataKey, owningPlugin);
                else throw new IllegalArgumentException("Block does not belong to world " + world.getName());
            }

            @Override
            public void setMetadata(Block block, String metadataKey, MetadataValue newMetadataValue) {
                if (block.getWorld() == world)
                    super.setMetadata(block, metadataKey, newMetadataValue);
                else throw new IllegalArgumentException("Block does not belong to world " + world.getName());
            }
        };
    }

}