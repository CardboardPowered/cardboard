package io.github.essentialsx.itemdbgenerator.providers.alias;

import io.github.essentialsx.itemdbgenerator.providers.item.ItemProvider;
import org.bukkit.Material;

import java.util.stream.Stream;

public class RailAliasProvider extends CompoundAliasProvider {
    private static final SingleCompoundType RAIL_TYPE = new SingleCompoundType("^(ACTIVATOR_|DETECTOR_|POWERED_)?RAIL$", "%srails", "%srail", "%strack");

    @Override
    public Stream<String> get(ItemProvider.Item item) {
        RailModifier modifier = RailModifier.of(item.getMaterial());
        if (modifier == null || !RAIL_TYPE.matches(item.getMaterial())) return null;

        return getAliases(modifier, RAIL_TYPE);
    }

    private enum RailModifier implements CompoundModifier {
        ACTIVATOR("activator", "activate", "trigger", "act", "tr", "a", "t"),
        DETECTOR("detector", "detecting", "detect", "det", "d"),
        POWERED("powered", "booster", "power", "boost", "pow", "pwr", "p", "b"),
        RAIL("", "minecart", "mcart", "mc"),
        ;

        private final String[] names;

        RailModifier(String... names) {
            this.names = names;
        }

        public static RailModifier of(Material material) {
            String matName = material.name();

            for (RailModifier rail : values()) {
                if (matName.contains(rail.name())) {
                    return rail;
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
