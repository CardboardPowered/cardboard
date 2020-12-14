package io.github.essentialsx.itemdbgenerator.providers.alias;

import com.google.common.collect.ObjectArrays;
import io.github.essentialsx.itemdbgenerator.providers.item.ItemProvider;
import org.bukkit.Material;

import java.util.regex.Pattern;
import java.util.stream.Stream;

public class WoodAliasProvider extends CompoundAliasProvider {
    @Override
    public Stream<String> get(ItemProvider.Item item) {
        WoodSpecies woodSpecies = WoodSpecies.of(item.getMaterial());
        WoodItemType itemType = WoodItemType.of(item.getMaterial());

        if (woodSpecies == null || itemType == null) return null;

        return getAliases(woodSpecies, itemType);
    }

    /**
     * Represents available varieties of wood in the game.
     */
    @SuppressWarnings("unused")
    private enum WoodSpecies implements CompoundModifier {
        ACACIA("ac", "a"),
        BIRCH("b", "light", "l", "white", "w"),
        DARK_OAK("darkoak", "do", "doak"),
        JUNGLE("j", "forest", "f"),
        OAK("o"),
        SPRUCE("pine", "p", "dark", "d", "s");

        private final String[] names;

        WoodSpecies(String... names) {
            this.names = ObjectArrays.concat(name().toLowerCase(), names);
        }

        public static WoodSpecies of(Material material) {
            String matName = material.name();
            for (WoodSpecies type : values()) {
                if (matName.contains(type.name())) {
                    return type;
                }
            }

            return null;
        }

        @Override
        public String[] getNames() {
            return names;
        }
    }

    /**
     * Represents the types of items that can have multiple different wood types.
     */
    @SuppressWarnings("unused")
    private enum WoodItemType implements CompoundType {
        BOAT(null, "boat%s", "%sboat", "%sraft"),
        BUTTON(null, "button%s", "%sbutton"),
        DOOR("[A-Z_]+_DOOR"),
        FENCE("[A-Z_]+_FENCE$"),
        FENCE_GATE(null, "%sgate", "%sfencegate", "gate%s"),
        LEAVES(null, "%sleaves", "%sleaf", "leaves%s", "leaf%s", "%streeleaves", "%slogleaves", "%strunkleaves", "%swoodleaves", "%streeleaf", "%slogleaf", "%strunkleaf", "%swoodleaf", "%sleaf", "%streeleave", "%slogleave", "%strunkleave", "%swoodleave", "%sleave"),
        LOG("^(?!STRIPPED_)[A-Z_]+_LOG", "log%s", "%slog", "%strunk", "%stree"),
        PLANKS(null, "%swoodenplank", "%swoodplank", "%swplank", "%splankwooden", "%splankwood", "%splankw", "%splank"),
        PRESSURE_PLATE(null, "%spplate", "%spressureplate", "%splate", "plate%s", "%spressplate"),
        SAPLING("^(?!POTTED_)[A-Z_]+_SAPLING", "%ssapling", "%streesapling", "%slogsapling", "%strunksapling", "%swoodsapling"),
        SIGN("^(?!WALL_)[A-Z_]+_SIGN", "%ssign"),
        SLAB("^(?!PETRIFIED_)[A-Z_]+_SLAB", "%swoodenstep", "%swoodstep", "%swstep", "%sstep", "%swoodenslab", "%swoodslab", "%swslab", "%swoodenhalfblock", "%swoodhalfblock", "%swhalfblock", "%shalfblock"),
        STAIRS(null, "%swoodenstairs", "%swoodstairs", "%swstairs", "%swoodenstair", "%swoodstair", "%swstair", "%sstair"),
        TRAPDOOR(null, "%strapdoor", "%sdoortrap", "%shatch", "%stdoor", "%sdoort", "%strapd", "%sdtrap"),
        WOOD("^(?!STRIPPED_)[A-Z_]+_WOOD", "%swood", "%slogall", "%strunkall", "%streeall", "wood%s"),
        POTTED_SAPLING("POTTED_[A-Z_]+_SAPLING", "%spot", "potted%s", "potted%ssapling"),
        STRIPPED_LOG("STRIPPED_[A-Z_]+_LOG", "stripped%slog", "log%sstripped", "str%slog", "%sstrippedlog", "%sbarelog", "stripped%stree", "bare%stree", "stripped%strunk", "bare%strunk"),
        STRIPPED_WOOD("STRIPPED_[A-Z_]+_WOOD", "stripped%swood", "wood%sstripped", "str%swood", "%sstrippedwood", "%sbarewood", "stripped%slogall", "bare%slogall", "stripped%strunkall", "bare%strunkall", "stripped%streeall", "bare%streeall"),
        ;

        private final Pattern regex;
        private final String[] formats;

        WoodItemType(String regex, String... formats) {
            this.regex = CompoundType.generatePattern(name(), regex);
            this.formats = CompoundType.generateFormats(name(), formats);
        }

        public static WoodItemType of(Material material) {
            String matName = material.name();

            for (WoodItemType type : values()) {
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
