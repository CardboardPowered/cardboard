package io.github.essentialsx.itemdbgenerator.providers.alias;

import com.google.common.collect.ObjectArrays;
import io.github.essentialsx.itemdbgenerator.providers.item.ItemProvider;
import org.bukkit.Material;

import java.util.regex.Pattern;
import java.util.stream.Stream;

public class NetherFungiAliasProvider extends CompoundAliasProvider {
    @Override
    public Stream<String> get(ItemProvider.Item item) {
        FungiSpecies fungiSpecies = FungiSpecies.of(item.getMaterial());
        FungiItemType itemType = FungiItemType.of(item.getMaterial());

        if (fungiSpecies == null || itemType == null) return null;

        return getAliases(fungiSpecies, itemType);
    }

    /**
     * Represents available varieties of wood in the game.
     */
    @SuppressWarnings("unused")
    private enum FungiSpecies implements CompoundModifier {
        CRIMSON("crim", "cr"),
        WARPED("warp"),
        ;

        private final String[] names;

        FungiSpecies(String... names) {
            this.names = ObjectArrays.concat(name().toLowerCase(), names);
        }

        public static FungiSpecies of(Material material) {
            String matName = material.name();
            for (FungiSpecies type : values()) {
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
    private enum FungiItemType implements CompoundType {
        BUTTON(null, "button%s", "%sbutton"),
        DOOR("[A-Z_]+_DOOR"),
        FENCE("[A-Z_]+_FENCE$"),
        FENCE_GATE(null, "%sgate", "%sfencegate", "gate%s"),
        FUNGUS("^(?!POTTED_)[A-Z_]+_FUNGUS", "%sfungus", "%sfun"),
        FUNGUS_ON_A_STICK(null, "%sstick", "%sonstick", "%sfunstick", "%sfungusstick"),
        HYPHAE("^(?!STRIPPED_)[A-Z_]+_HYPHAE", "%shyphae"),
        NYLIUM("[A-Z_]+_NYLIUM", "%snylium", "%ssoil"),
        PLANKS(null, "%swoodenplank", "%swoodplank", "%swplank", "%splankwooden", "%splankwood", "%splankw", "%splank"),
        PRESSURE_PLATE(null, "%spplate", "%spressureplate", "%splate", "plate%s", "%spressplate"),
        ROOTS("^(?!POTTED_)[A-Z_]+_ROOTS", "%sroots", "%sroot"),
        SIGN("^(?!WALL_)[A-Z_]+_SIGN", "%ssign"),
        SLAB("^(?!PETRIFIED_)[A-Z_]+_SLAB", "%swoodenstep", "%swoodstep", "%swstep", "%sstep", "%swoodenslab", "%swoodslab", "%swslab", "%swoodenhalfblock", "%swoodhalfblock", "%swhalfblock", "%shalfblock"),
        STAIRS(null, "%swoodenstairs", "%swoodstairs", "%swstairs", "%swoodenstair", "%swoodstair", "%swstair", "%sstair"),
        STEM("^(?!STRIPPED_)[A-Z_]+_STEM", "%sstem", "stem%s", "log%s", "%slog"),
        TRAPDOOR(null, "%strapdoor", "%sdoortrap", "%shatch", "%stdoor", "%sdoort", "%strapd", "%sdtrap"),
        WART_BLOCK(null, "%swart", "%swartblock"),
        POTTED_FUNGUS("POTTED_[A-Z_]+_FUNGUS", "potted%sfungus", "potted%sfun", "pot%sfungus", "pot%sfun"),
        POTTED_ROOTS("POTTED_[A-Z_]+_FUNGUS", "potted%sroots", "potted%sroot", "pot%sroots", "pot%sroot"),
        STRIPPED_HYPHAE("^STRIPPED_[A-Z_]+_HYPHAE", "stripped%shyphae", "str%shyphae"),
        STRIPPED_STEM("STRIPPED_[A-Z_]+_STEM", "stripped%slog", "log%sstripped", "str%slog", "%sstrippedlog", "%sbarelog", "stripped%stree", "bare%stree", "stripped%sstem", "bare%sstem"),
        ;

        private final Pattern regex;
        private final String[] formats;

        FungiItemType(String regex, String... formats) {
            this.regex = CompoundType.generatePattern(name(), regex);
            this.formats = CompoundType.generateFormats(name(), formats);
        }

        public static FungiItemType of(Material material) {
            String matName = material.name();

            for (FungiItemType type : values()) {
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
