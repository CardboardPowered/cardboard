package io.github.essentialsx.itemdbgenerator.providers.alias;

import io.github.essentialsx.itemdbgenerator.providers.item.ItemProvider;
import org.bukkit.Material;

import java.util.regex.Pattern;
import java.util.stream.Stream;

public class PistonAliasProvider extends CompoundAliasProvider {
    @Override
    public Stream<String> get(ItemProvider.Item item) {
        return null;
    }

    private enum RedstoneType implements CompoundType {
        PISTON(null, "%spiston", "piston%s", "%sp", "p%s", "piston%sbase", "%spistonbase")
        ;

        private final Pattern regex;
        private final String[] formats;

        RedstoneType(String regex, String... formats) {
            this.regex = CompoundType.generatePattern(name(), regex);
            this.formats = CompoundType.generateFormats(name(), formats);
        }

        public static RedstoneType of(Material material) {
            String matName = material.name();

            for (RedstoneType type : values()) {
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

    private enum RedstoneModifier implements CompoundModifier {
        STICKY("sticky", "stick", "s"),
        ;

        private final String[] names;

        RedstoneModifier(String... names) {
            this.names = names;
        }

        @Override
        public String[] getNames() {
            return names;
        }
    }
}
