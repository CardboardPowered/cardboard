package io.github.essentialsx.itemdbgenerator.providers.alias;

import com.google.common.collect.ObjectArrays;
import io.github.essentialsx.itemdbgenerator.providers.item.ItemProvider;
import io.github.essentialsx.itemdbgenerator.providers.item.PotionProvider;

import java.util.Arrays;
import java.util.stream.Stream;

public class PotionAliasProvider implements AliasProvider {
    @Override
    public Stream<String> get(ItemProvider.Item item) {
        if (!(item instanceof PotionProvider.PotionItem)) return null;
        PotionProvider.PotionItem pItem = (PotionProvider.PotionItem) item;

        PotionMaterial material = PotionMaterial.of(pItem);
        PotionType type = PotionType.of(pItem);
        PotionModifier modifier = PotionModifier.of(pItem);

        return Arrays.stream(material.getFormats())
                .flatMap(format -> Arrays.stream(type.getNames())
                        .map(name -> format.replaceAll("\\{t}", name)))
                .flatMap(format -> Arrays.stream(modifier.getNames())
                        .map(name -> format.replaceAll("\\{m}", name)));
    }

    enum PotionMaterial {
        POTION("{t}{m}potion", "{t}{m}pot", "potionof{t}{m}", "potof{t}{m}"),
        SPLASH_POTION("splash{t}{m}potion", "spl{t}{m}potion", "{t}{m}splashpotion", "splash{t}{m}pot", "spl{t}{m}pot", "{t}{m}splashpot"),
        LINGERING_POTION("lingerpot{t}{m}", "{t}lingerpot{m}", "aoepotion{t}{m}", "{t}aoepoiont{m}", "aoepot{t}{m}", "{t}aoepot{m}", "areapotion{t}{m}", "{t}areapotion{m}", "areapot{t}{m}", "{t}areapot{m}", "cloudpotion{t}{m}", "{t}cloudpotion{m}", "cloudpot{t}{m}", "{t}cloudpot{m}"),
        TIPPED_ARROW("arrow{t}{m}", "{t}arrow{m}", "{t}{m}tippedarrow", "{t}{m}tarrow", "{t}{m}tarr"),
        ;

        private final String[] formats;

        PotionMaterial(String... formats) {
            this.formats = formats;
        }

        static PotionMaterial of(PotionProvider.PotionItem item) {
            return valueOf(item.getMaterial().name());

        }

        public String[] getFormats() {
            return formats;
        }
    }

    enum PotionType {
        UNCRAFTABLE,
        WATER,
        MUNDANE,
        THICK,
        AWKWARD,
        NIGHT_VISION("nv", "nvision", "nightv", "darkvis", "dvision", "darkv"),
        INVISIBILITY("invis", "invisible", "inv"),
        JUMP("leaping", "leap"),
        FIRE_RESISTANCE("fireresist", "fireres"),
        SPEED("swiftness", "swift"),
        SLOWNESS("slow"),
        WATER_BREATHING("wb", "waterbreath", "breathing", "breath"),
        INSTANT_HEAL("healing", "heal", "life", "h"),
        INSTANT_DAMAGE("harming", "damage", "dmg", "d"),
        POISON("acid", "p"),
        REGEN("regeneration", "regenerate"),
        STRENGTH("strong", "str"),
        WEAKNESS("weak", "we"),
        LUCK("lucky", "clover"),
        TURTLE_MASTER("turtle", "tm"),
        SLOW_FALLING("slowfall", "sf"),
        ;

        private final String[] names;

        PotionType(String... names) {
            this.names = ObjectArrays.concat(names, name().toLowerCase().replaceAll("_", ""));
        }

        static PotionType of(PotionProvider.PotionItem item) {
            return valueOf(item.getPotionData().getType().name());
        }

        public String[] getNames() {
            return names;
        }
    }

    enum PotionModifier {
        NONE(""),
        LONG("2", "long", "extended", "ex", "level2"),
        STRONG("ii", "strong", "levelii"),
        ;

        private final String[] names;

        PotionModifier(String... names) {
            this.names = names;
        }

        static PotionModifier of(PotionProvider.PotionItem item) {
            if (item.getPotionData().isExtended()) {
                return LONG;
            } else if (item.getPotionData().isUpgraded()) {
                return STRONG;
            } else {
                return NONE;
            }
        }

        public String[] getNames() {
            return names;
        }
    }
}
