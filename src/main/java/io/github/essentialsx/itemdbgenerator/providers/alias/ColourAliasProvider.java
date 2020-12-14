package io.github.essentialsx.itemdbgenerator.providers.alias;

import com.google.common.collect.ObjectArrays;
import io.github.essentialsx.itemdbgenerator.providers.item.ItemProvider;
import org.bukkit.Material;

import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ColourAliasProvider extends CompoundAliasProvider {
    @Override
    public Stream<String> get(ItemProvider.Item item) {
        Colour colour = Colour.of(item.getMaterial());
        ColourableItemType itemType = ColourableItemType.of(item.getMaterial());

        if (colour == null || itemType == null) return null;

        return getAliases(colour, itemType);
    }

    /**
     * Represents available varieties of wood in the game.
     */
    @SuppressWarnings("unused")
    private enum Colour implements CompoundModifier {
        WHITE("w", "white"),
        ORANGE("o", "orange"),
        MAGENTA("m", "magenta"),
        LIGHT_BLUE("lb", "lblu", "lightblu", "lblue", "lightblue"),
        YELLOW("y", "yellow"),
        LIME("l", "lime", "lgre", "lightgre", "lgreen", "lightgreen"),
        PINK("pi", "pink"),
        GRAY("gra", "grey", "gray", "dgra", "darkgra", "dgrey", "dgray", "darkgrey", "darkgray"),
        LIGHT_GRAY("lg", "lgra", "lgrey", "lgray", "lightgra", "lightgrey", "lightgray", "si", "sia", "silver"),
        CYAN("c", "cyan"),
        PURPLE("pu", "purple"),
        BLUE("blu", "blue"),
        BROWN("br", "bro", "brown"),
        GREEN("gre", "dgre", "darkgre", "green", "dgreen", "darkgreen"),
        RED("r", "red"),
        BLACK("bk", "bla", "black");

        private static final Colour[] lightValues = {LIGHT_BLUE, LIGHT_GRAY};

        private final String[] names;

        Colour(String... names) {
            this.names = ObjectArrays.concat(name().toLowerCase(), names);
        }

        public static Colour of(Material material) {
            String matName = material.name();

            for (Colour colour : lightValues()) {
                if (matName.contains(colour.name())) {
                    return colour;
                }
            }

            for (Colour colour : values()) {
                if (matName.contains(colour.name())) {
                    return colour;
                }
            }

            return null;
        }

        public static Colour[] lightValues() {
            return lightValues;
        }

        @Override
        public String[] getNames() {
            return names;
        }
    }

    /**
     * Represents the types of materials with coloured variants.
     */
    @SuppressWarnings("unused")
    private enum ColourableItemType implements CompoundType {
        BANNER("[A-Z_]+_(?<!WALL_)BANNER", "%sbanner"),
        BED(null, "%sbed"),
        CARPET(null, "%scarpet", "%sfloor"),
        CONCRETE("[A-Z_]+_CONCRETE(?!_POWDER)", "%sconcrete"),
        CONCRETE_POWDER(null, "%sconcretepowder", "%sconcretesand", "%scpowder", "%scdust", "%scp"),
        DYE(null, "%sdye"),
        GLAZED_TERRACOTTA(null, "%sglazedtcota", "%sglazedterra", "%sglazedterracotta", "%sglazedterracota", "%sgtcotta", "%sgterra"),
        SHULKER_BOX(null, "%sshulkerbox", "%ssbox"),
        STAINED_GLASS("[A-Z_]+_STAINED_GLASS(?!_PANE)", "%sglass", "%ssglass", "%sstainedglass"),
        STAINED_GLASS_PANE(null, "%sglasspane", "%ssglasspane", "%sstainedglasspane", "%sgpane"),
        TERRACOTTA(null, "%sclay", "%ssclay", "%sstainedclay", "%sterra", "%stcota", "%sterracota", "%sterracotta"),
        WOOL(null, "%swool", "%scloth", "%scotton");

        private final Pattern regex;
        private final String[] formats;

        ColourableItemType(String regex, String... formats) {
            this.regex = CompoundType.generatePattern(name(), regex);
            this.formats = CompoundType.generateFormats(name(), formats);
        }

        public static ColourableItemType of(Material material) {
            String matName = material.name();

            for (ColourableItemType type : values()) {
                if (type.regex.matcher(matName).matches()) {
                    return type;
                }
            }

            return null;
        }

        @Override
        public String[] getFormats() {
            return formats;
        }
    }
}
