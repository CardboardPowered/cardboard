package io.github.essentialsx.itemdbgenerator.providers.alias;

import io.github.essentialsx.itemdbgenerator.providers.item.ItemProvider;
import org.bukkit.Material;

import java.util.stream.Stream;

public class MinecartAliasProvider extends CompoundAliasProvider {
    private static final SingleCompoundType MINECART_TYPE = new SingleCompoundType("^((TNT|HOPPER|CHEST|COMMAND_BLOCK|FURNACE)_)?MINECART$", "%sminecart", "%smcart", "%smc", "%scart");

    @Override
    public Stream<String> get(ItemProvider.Item item) {
        MinecartModifier modifier = MinecartModifier.of(item.getMaterial());
        if (modifier == null || !MINECART_TYPE.matches(item.getMaterial())) return null;

        return getAliases(modifier, MINECART_TYPE);
    }

    private enum MinecartModifier implements CompoundModifier {
        TNT("tnt", "dynamite", "bomb", "t", "d", "b"),
        HOPPER("hopper", "hop", "h"),
        CHEST("storage", "chest", "s", "c"),
        COMMAND_BLOCK("commandblock", "cmdblock", "cblock", "command", "cmd", "cb"),
        FURNACE("engine", "powered", "power", "furnace", "e", "p", "f"),
        MINECART("");

        private final String[] names;

        MinecartModifier(String... names) {
            this.names = names;
        }

        public static MinecartModifier of(Material material) {
            String matName = material.name();

            for (MinecartModifier minecart : values()) {
                if (matName.contains(minecart.name())) {
                    return minecart;
                }
            }

            return null;
        }

        @Override
        public String[] getNames() {
            return names;
        }
    }
}
