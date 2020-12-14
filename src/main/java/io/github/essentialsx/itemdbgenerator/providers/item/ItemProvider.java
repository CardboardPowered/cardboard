package io.github.essentialsx.itemdbgenerator.providers.item;

import org.bukkit.Material;

import java.util.Objects;
import java.util.stream.Stream;

public interface ItemProvider {

    Stream<Item> get();

    abstract class Item implements Comparable {
        private final Material material;
        private final String[] fallbacks;

        public Item(Material material) {
            this.material = material;
            this.fallbacks = MaterialFallbacks.get(material);
        }

        public abstract String getName();

        public Material getMaterial() {
            return material;
        }

        public String[] getFallbacks() {
            return fallbacks;
        }

        @Override
        public int hashCode() {
            return Objects.hash(material);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Item)) return false;
            return hashCode() == obj.hashCode();
        }

        @Override
        public int compareTo(Object obj) {
            Item item = (Item) obj;
            return getName().compareToIgnoreCase(item.getName());
        }
    }
}
