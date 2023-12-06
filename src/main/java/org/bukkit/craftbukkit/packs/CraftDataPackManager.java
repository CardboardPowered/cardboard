package org.bukkit.craftbukkit.packs;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.util.Identifier;
import net.minecraft.world.level.LevelProperties;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.craftbukkit.packs.CraftDataPack;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.packs.DataPack;
import org.bukkit.packs.DataPackManager;
import org.cardboardpowered.impl.world.WorldImpl;

public class CraftDataPackManager
implements DataPackManager {
    private final ResourcePackManager handle;

    public CraftDataPackManager(ResourcePackManager resourcePackRepository) {
        this.handle = resourcePackRepository;
    }

    public ResourcePackManager getHandle() {
        return this.handle;
    }

    public Collection<DataPack> getDataPacks() {
        this.getHandle().scanPacks();
        Collection<ResourcePackProfile> availablePacks = this.getHandle().getProfiles();
        return availablePacks.stream().map(CraftDataPack::new).collect(Collectors.toUnmodifiableList());
    }

    public DataPack getDataPack(NamespacedKey namespacedKey) {
        Preconditions.checkArgument((namespacedKey != null ? 1 : 0) != 0, (Object)"namespacedKey cannot be null");
        return new CraftDataPack(this.getHandle().getProfile(namespacedKey.getKey()));
    }

    public Collection<DataPack> getEnabledDataPacks(World world) {
        Preconditions.checkArgument((world != null ? 1 : 0) != 0, (Object)"world cannot be null");
        WorldImpl craftWorld = (WorldImpl)world;
        return ((LevelProperties)craftWorld.getHandle().worldProperties).getDataConfiguration().dataPacks().getEnabled().stream().map(packName -> {
            ResourcePackProfile resourcePackLoader = this.getHandle().getProfile((String)packName);
            if (resourcePackLoader != null) {
                return new CraftDataPack(resourcePackLoader);
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toUnmodifiableList());
    }

    public Collection<DataPack> getDisabledDataPacks(World world) {
        Preconditions.checkArgument((world != null ? 1 : 0) != 0, (Object)"world cannot be null");
        WorldImpl craftWorld = (WorldImpl)world;

        return ((LevelProperties)craftWorld.getHandle().worldProperties).getDataConfiguration().dataPacks().getDisabled().stream().map(packName -> {
            ResourcePackProfile resourcePackLoader = this.getHandle().getProfile((String)packName);
            if (resourcePackLoader != null) {
                return new CraftDataPack(resourcePackLoader);
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toUnmodifiableList());
    }

    public boolean isEnabledByFeature(Material material, World world) {
        Preconditions.checkArgument((material != null ? 1 : 0) != 0, (Object)"material cannot be null");
        Preconditions.checkArgument((material.isItem() || material.isBlock() ? 1 : 0) != 0, (Object)"material need to be a item or block");
        Preconditions.checkArgument((world != null ? 1 : 0) != 0, (Object)"world cannot be null");
        WorldImpl craftWorld = (WorldImpl)world;
        if (material.isItem()) {
            return CraftMagicNumbers.getItem(material).isEnabled(craftWorld.getHandle().getEnabledFeatures());
        }
        if (material.isBlock()) {
            return CraftMagicNumbers.getBlock(material).isEnabled(craftWorld.getHandle().getEnabledFeatures());
        }
        return false;
    }

    public boolean isEnabledByFeature(org.bukkit.entity.EntityType entityType, World world) {
        Preconditions.checkArgument((entityType != null ? 1 : 0) != 0, (Object)"entityType cannot be null");
        Preconditions.checkArgument((world != null ? 1 : 0) != 0, (Object)"world cannot be null");
        Preconditions.checkArgument((entityType != org.bukkit.entity.EntityType.UNKNOWN ? 1 : 0) != 0, (Object)"EntityType.UNKNOWN its not allowed here");
        WorldImpl craftWorld = (WorldImpl)world;
        EntityType<?> nmsEntity = Registries.ENTITY_TYPE.get(new Identifier(entityType.getKey().getKey()));
        return nmsEntity.isEnabled(craftWorld.getHandle().getEnabledFeatures());
    }
}

