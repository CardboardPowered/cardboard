package io.github.essentialsx.itemdbgenerator.providers.alias;

import com.google.common.collect.ObjectArrays;
import io.github.essentialsx.itemdbgenerator.providers.item.ItemProvider;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class MeatFishAliasProvider extends CompoundAliasProvider {

    private static final List<String> BLACKLIST = Arrays.asList("SPAWN_EGG", "BUCKET", "FOOT");

    @Override
    public Stream<String> get(ItemProvider.Item item) {
        if (isBlacklisted(item.getMaterial())) return null;

        Food food = Food.of(item.getMaterial());
        FoodType itemType = FoodType.of(item.getMaterial());

        if (food == null || itemType == null) {
            return null;
        }

        return getAliases(food, itemType);
    }

    private boolean isBlacklisted(Material material) {
        for (String str : BLACKLIST) {
            if (material.name().contains(str)) return true;
        }
        return false;
    }

    private enum Food implements CompoundModifier {
        BEEF("steak", "cowmeat"),
        CHICKEN,
        COD("fish"),
        MUTTON("sheepmeat"),
        PORKCHOP("pork"),
        PUFFERFISH("pufffish", "fishpuff", "pfish", "fishp"),
        RABBIT("hare", "hasenpfeffer"),
        SALMON("salmonfish", "sfish", "fishs"),
        TROPICAL_FISH("clownfish", "nemo", "clfish", "fishcl", "nfish", "fishn", "tfish", "fisht");
        private final String[] names;

        Food(String... names) {
            this.names = ObjectArrays.concat(name().toLowerCase(), names);
        }

        public static Food of(Material material) {
            String matName = material.name();

            for (Food food : values()) {
                if (matName.contains(food.name())) {
                    return food;
                }
            }

            return null;
        }

        @Override
        public String[] getNames() {
            return names;
        }
    }

    private enum FoodType implements CompoundType {
        COOKED("COOKED_[A-Z_]+", "%scooked", "%scook", "%sc", "%sgrilled", "%sgrill", "%sg", "%sroasted", "%sroast", "%sro", "%sbbq", "%stoasted", "cooked%s", "cook%s", "c%s", "grilled%s", "grill%s", "g%s", "roasted%s", "roast%s", "ro%s", "bbq%s", "toasted%s"),
        HIDE("[A-Z_]+_HIDE", "%shide", "%sskin", "%scoat", "%sfur"),
        STEW("[A-Z_]+_STEW", "%sstew", "%ssoup"),
        RAW("^(?!COOKED_)[A-Z_]+(?!_STEW)$", "raw%s", "ra%s", "uncooked%s", "plain%s", "%s"),
        ;

        private final Pattern regex;
        private final String[] formats;

        FoodType(String regex, String... formats) {
            this.regex = CompoundType.generatePattern(name(), regex);
            this.formats = CompoundType.generateFormats(name(), formats);
        }

        public static FoodType of(Material material) {
            String matName = material.name();

            for (FoodType type : values()) {
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
