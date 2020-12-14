package io.github.essentialsx.itemdbgenerator.providers.alias;

import io.github.essentialsx.itemdbgenerator.providers.item.ItemProvider;
import io.github.essentialsx.itemdbgenerator.providers.item.MaterialEnumProvider;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class RecordAliasProvider extends CompoundAliasProvider {
    private static final List<String> MUSIC_DISC_NAMES = Arrays.asList("musicrecord", "musicdisk", "musicdisc", "musiccd", "mrecord", "mdisk", "mdisc", "mcd", "record", "disk", "disc", "cd");

    @Override
    public Stream<String> get(ItemProvider.Item item) {
        if (!(item instanceof MaterialEnumProvider.MaterialEnumItem)) return null;

        Track track = Track.of(item.getMaterial());
        if (track == null) return null;

        if (item.getMaterial().name().contains("MUSIC_DISK")) {
            System.out.println(track);
        }

        return MUSIC_DISC_NAMES.stream()
                .flatMap(track::format);
    }

    private enum Track implements CompoundType {
        THIRTEEN("MUSIC_DISC_13", "13%s", "gold%s", "go%s", "%s1", "1%s"),
        CAT("MUSIC_DISC_CAT", "cat%s", "green%s", "gr%s", "%s2", "2%s"),
        BLOCKS("MUSIC_DISC_BLOCKS", "blocks%s", "orange%s", "or%s", "%s3", "3%s"),
        CHIRP("MUSIC_DISC_CHIRP", "chirp%s", "red%s", "re%s", "%s4", "4%s"),
        FAR("MUSIC_DISC_FAR", "far%s", "lightgreen%s", "lgreen%s", "lightgr%s", "lgr%s", "%s5", "5%s"),
        MALL("MUSIC_DISC_MALL", "mall%s", "purple%s", "pu%s", "%s6", "6%s"),
        MELLOHI("MUSIC_DISC_MELLOHI", "mellohi%s", "pink%s", "pi%s", "%s7", "7%s"),
        STAL("MUSIC_DISC_STAL", "stal%s", "black%s", "bl%s", "%s8", "8%s"),
        STRAD("MUSIC_DISC_STRAD", "strad%s", "white%s", "wh%s", "%s9", "9%s"),
        WARD("MUSIC_DISC_WARD", "ward%s", "darkgreen%s", "dgreen%s", "darkgr%s", "dgr%s", "%s10", "10%s"),
        ELEVEN("MUSIC_DISC_11", "11%s", "cracked%s", "crack%s", "c%s", "%s11"),
        WAIT("MUSIC_DISC_WAIT", "wait%s", "blue%s", "cyan%s", "bl%s", "cy%s", "%s12", "12%s"),
        PIGSTEP("MUSIC_DISC_PIGSTEP", "pigstep%s", "nether%s", "dark%s", "neth%s", "pig%s", "%s14", "14%s"),
        ;

        private final Pattern regex;
        private final String[] formats;

        Track(String regex, String... formats) {
            this.regex = CompoundType.generatePattern(name(), regex);
            this.formats = CompoundType.generateFormats(name(), formats);
        }

        public static Track of(Material material) {
            String matName = material.name();

            for (Track type : values()) {
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
