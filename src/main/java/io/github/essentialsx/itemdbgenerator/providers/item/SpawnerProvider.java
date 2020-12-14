package io.github.essentialsx.itemdbgenerator.providers.item;

import io.github.essentialsx.itemdbgenerator.providers.alias.MobAliasProvider;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class SpawnerProvider implements ItemProvider {
    @Override
    public Stream<Item> get() {
        return Arrays.stream(EntityType.values())
                .filter(MobAliasProvider::isSpawnable)
                .map(type -> new SpawnerItem(Material.SPAWNER, type));
    }

    public static class SpawnerItem extends Item {

        private final EntityType entity;

        public SpawnerItem(Material material, EntityType entity) {
            super(material);
            this.entity = entity;
        }

        @Override
        public String getName() {
            return entity.name().toLowerCase() + "_spawner";
        }

        public EntityType getEntity() {
            return entity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(getMaterial(), entity);
        }
    }
}
