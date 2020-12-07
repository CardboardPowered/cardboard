/**
 * CardboardPowered - Bukkit/Spigot for Fabric
 * Copyright (C) CardboardPowered.org and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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