package org.cardboardpowered.adventure;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.netty.util.AttributeKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.util.Codec;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;

import org.bukkit.ChatColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CardboardAdventure {
    public static final AttributeKey<Locale> LOCALE_ATTRIBUTE = AttributeKey.valueOf("adventure:locale");
    private static final Pattern LOCALIZATION_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?s");
    public static final ComponentFlattener FLATTENER = ComponentFlattener.basic().toBuilder()
        .complexMapper(TranslatableComponent.class, (translatable, consumer) -> {
            final @NonNull String translated = Language.getInstance().get(translatable.key());

            final Matcher matcher = LOCALIZATION_PATTERN.matcher(translated);
            final List<Component> args = translatable.args();
            int argPosition = 0;
            int lastIdx = 0;
            while (matcher.find()) {
                // append prior
                if (lastIdx < matcher.start()) {
                    consumer.accept(Component.text(translated.substring(lastIdx, matcher.start())));
                }
                lastIdx = matcher.end();

                final @Nullable String argIdx = matcher.group(1);
                // calculate argument position
                if (argIdx != null) {
                    try {
                        final int idx = Integer.parseInt(argIdx) - 1;
                        if (idx < args.size()) {
                            consumer.accept(args.get(idx));
                        }
                    } catch (final NumberFormatException ex) {
                        // ignore, drop the format placeholder
                    }
                } else {
                    final int idx = argPosition++;
                    if (idx < args.size()) {
                        consumer.accept(args.get(idx));
                    }
                }
            }

            // append tail
            if (lastIdx < translated.length()) {
                consumer.accept(Component.text(translated.substring(lastIdx)));
            }
        })
        .build();
    public static final LegacyComponentSerializer LEGACY_SECTION_UXRC = LegacyComponentSerializer.builder().flattener(FLATTENER).hexColors().useUnusualXRepeatedCharacterHexFormat().build();
    public static final PlainComponentSerializer PLAIN = PlainComponentSerializer.builder().flattener(FLATTENER).build();
    public static final GsonComponentSerializer GSON = GsonComponentSerializer.builder()
        .legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.INSTANCE)
        .build();
    public static final GsonComponentSerializer COLOR_DOWNSAMPLING_GSON = GsonComponentSerializer.builder()
        .legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.INSTANCE)
        .downsampleColors()
        .build();
    private static final Codec<NbtCompound, String, IOException, IOException> NBT_CODEC = new Codec<NbtCompound, String, IOException, IOException>() {
        @Override
        public @NonNull NbtCompound decode(final @NonNull String encoded) throws IOException {
            try {
                return StringNbtReader.parse(encoded);
            } catch (final CommandSyntaxException e) {
                throw new IOException(e);
            }
        }

        @Override
        public @NonNull String encode(final @NonNull NbtCompound decoded) {
            return decoded.toString();
        }
    };
    static final WrapperAwareSerializer WRAPPER_AWARE_SERIALIZER = new WrapperAwareSerializer();

    private CardboardAdventure() {
    }

    // Key

    public static Identifier asVanilla(final Key key) {
        return new Identifier(key.namespace(), key.value());
    }

    public static Identifier asVanillaNullable(final Key key) {
        if (key == null) {
            return null;
        }
        return new Identifier(key.namespace(), key.value());
    }

    // Component

    public static Component asAdventure(final Text component) {
        return component == null ? Component.empty() : GSON.serializer().fromJson(Text.Serializer.toJsonTree(component), Component.class);
    }

    public static ArrayList<Component> asAdventure(final List<Text> vanillas) {
        final ArrayList<Component> adventures = new ArrayList<>(vanillas.size());
        for (final Text vanilla : vanillas) {
            adventures.add(asAdventure(vanilla));
        }
        return adventures;
    }

    public static ArrayList<Component> asAdventureFromJson(final List<String> jsonStrings) {
        final ArrayList<Component> adventures = new ArrayList<>(jsonStrings.size());
        for (final String json : jsonStrings) {
            adventures.add(GsonComponentSerializer.gson().deserialize(json));
        }
        return adventures;
    }

    public static List<String> asJson(final List<Component> adventures) {
        final List<String> jsons = new ArrayList<>(adventures.size());
        for (final Component component : adventures) {
            jsons.add(GsonComponentSerializer.gson().serialize(component));
        }
        return jsons;
    }

    public static Text asVanilla(final Component component) {
        if (true) return new CardboardAdventureComponent(component);
        return Text.Serializer.fromJson(GSON.serializer().toJsonTree(component));
    }

    public static List<Text> asVanilla(final List<Component> adventures) {
        final List<Text> vanillas = new ArrayList<>(adventures.size());
        for (final Component adventure : adventures) {
            vanillas.add(asVanilla(adventure));
        }
        return vanillas;
    }

    public static String asJsonString(final Component component, final Locale locale) {
        return GSON.serialize(
            GlobalTranslator.render(
                component,
                // play it safe
                locale != null
                    ? locale
                    : Locale.US
            )
        );
    }

    public static String asJsonString(final Text component, final Locale locale) {
        if ((Object)component instanceof CardboardAdventureComponent) {
            return asJsonString(((CardboardAdventureComponent)(Object) component).adventure, locale);
        }
        return Text.Serializer.toJson(component);
    }

    // thank you for being worse than wet socks, Bukkit
    public static String superHackyLegacyRepresentationOfComponent(final Component component, final String string) {
        return LEGACY_SECTION_UXRC.serialize(component) + ChatColor.getLastColors(string);
    }

    // BossBar

    public static net.minecraft.entity.boss.BossBar.Color asVanilla(final BossBar.Color color) {
        if (color == BossBar.Color.PINK) {
            return net.minecraft.entity.boss.BossBar.Color.PINK;
        } else if (color == BossBar.Color.BLUE) {
            return net.minecraft.entity.boss.BossBar.Color.BLUE;
        } else if (color == BossBar.Color.RED) {
            return net.minecraft.entity.boss.BossBar.Color.RED;
        } else if (color == BossBar.Color.GREEN) {
            return net.minecraft.entity.boss.BossBar.Color.GREEN;
        } else if (color == BossBar.Color.YELLOW) {
            return net.minecraft.entity.boss.BossBar.Color.YELLOW;
        } else if (color == BossBar.Color.PURPLE) {
            return net.minecraft.entity.boss.BossBar.Color.PURPLE;
        } else if (color == BossBar.Color.WHITE) {
            return net.minecraft.entity.boss.BossBar.Color.WHITE;
        }
        throw new IllegalArgumentException(color.name());
    }

    public static BossBar.Color asAdventure(final net.minecraft.entity.boss.BossBar.Color color) {
        if(color == net.minecraft.entity.boss.BossBar.Color.PINK) {
            return BossBar.Color.PINK;
        } else if(color == net.minecraft.entity.boss.BossBar.Color.BLUE) {
            return BossBar.Color.BLUE;
        } else if(color == net.minecraft.entity.boss.BossBar.Color.RED) {
            return BossBar.Color.RED;
        } else if(color == net.minecraft.entity.boss.BossBar.Color.GREEN) {
            return BossBar.Color.GREEN;
        } else if(color == net.minecraft.entity.boss.BossBar.Color.YELLOW) {
            return BossBar.Color.YELLOW;
        } else if(color == net.minecraft.entity.boss.BossBar.Color.PURPLE) {
            return BossBar.Color.PURPLE;
        } else if(color == net.minecraft.entity.boss.BossBar.Color.WHITE) {
            return BossBar.Color.WHITE;
        }
        throw new IllegalArgumentException(color.name());
    }

    public static net.minecraft.entity.boss.BossBar.Style asVanilla(final BossBar.Overlay overlay) {
        if (overlay == BossBar.Overlay.PROGRESS) {
            return net.minecraft.entity.boss.BossBar.Style.PROGRESS;
        } else if (overlay == BossBar.Overlay.NOTCHED_6) {
            return net.minecraft.entity.boss.BossBar.Style.NOTCHED_6;
        } else if (overlay == BossBar.Overlay.NOTCHED_10) {
            return net.minecraft.entity.boss.BossBar.Style.NOTCHED_10;
        } else if (overlay == BossBar.Overlay.NOTCHED_12) {
            return net.minecraft.entity.boss.BossBar.Style.NOTCHED_12;
        } else if (overlay == BossBar.Overlay.NOTCHED_20) {
            return net.minecraft.entity.boss.BossBar.Style.NOTCHED_20;
        }
        throw new IllegalArgumentException(overlay.name());
    }

    public static BossBar.Overlay asAdventure(final net.minecraft.entity.boss.BossBar.Style overlay) {
        if (overlay == net.minecraft.entity.boss.BossBar.Style.PROGRESS) {
            return BossBar.Overlay.PROGRESS;
        } else if (overlay == net.minecraft.entity.boss.BossBar.Style.NOTCHED_6) {
            return BossBar.Overlay.NOTCHED_6;
        } else if (overlay == net.minecraft.entity.boss.BossBar.Style.NOTCHED_10) {
            return BossBar.Overlay.NOTCHED_10;
        } else if (overlay == net.minecraft.entity.boss.BossBar.Style.NOTCHED_12) {
            return BossBar.Overlay.NOTCHED_12;
        } else if (overlay == net.minecraft.entity.boss.BossBar.Style.NOTCHED_20) {
            return BossBar.Overlay.NOTCHED_20;
        }
        throw new IllegalArgumentException(overlay.name());
    }

    public static void setFlag(final BossBar bar, final BossBar.Flag flag, final boolean value) {
        if (value) {
            bar.addFlag(flag);
        } else {
            bar.removeFlag(flag);
        }
    }

    // Book

    public static ItemStack asItemStack(final Book book, final Locale locale) {
        final ItemStack item = new ItemStack(Items.WRITTEN_BOOK, 1);
        final NbtCompound tag = item.getOrCreateNbt();
        tag.putString("title", asJsonString(book.title(), locale));
        tag.putString("author", asJsonString(book.author(), locale));
        final NbtList pages = new NbtList();
        for (final Component page : book.pages()) {
            pages.add(NbtString.of(asJsonString(page, locale)));
        }
        tag.put("pages", pages);
        return item;
    }

    // Sounds

    public static net.minecraft.sound.SoundCategory asVanilla(final Sound.Source source) {
        if (source == Sound.Source.MASTER) {
            return SoundCategory.MASTER;
        } else if (source == Sound.Source.MUSIC) {
            return SoundCategory.MUSIC;
        } else if (source == Sound.Source.RECORD) {
            return SoundCategory.RECORDS;
        } else if (source == Sound.Source.WEATHER) {
            return SoundCategory.WEATHER;
        } else if (source == Sound.Source.BLOCK) {
            return SoundCategory.BLOCKS;
        } else if (source == Sound.Source.HOSTILE) {
            return SoundCategory.HOSTILE;
        } else if (source == Sound.Source.NEUTRAL) {
            return SoundCategory.NEUTRAL;
        } else if (source == Sound.Source.PLAYER) {
            return SoundCategory.PLAYERS;
        } else if (source == Sound.Source.AMBIENT) {
            return SoundCategory.AMBIENT;
        } else if (source == Sound.Source.VOICE) {
            return SoundCategory.VOICE;
        }
        throw new IllegalArgumentException(source.name());
    }

    public static @Nullable SoundCategory asVanillaNullable(final Sound.@Nullable Source source) {
        if (source == null) {
            return null;
        }
        return asVanilla(source);
    }

    // NBT

    public static @Nullable BinaryTagHolder asBinaryTagHolder(final @Nullable NbtCompound tag) {
        if (tag == null) {
            return null;
        }
        try {
            return BinaryTagHolder.encode(tag, NBT_CODEC);
        } catch (final IOException e) {
            return null;
        }
    }

    // Colors

    public static @NonNull TextColor asAdventure(Formatting minecraftColor) {
        if (minecraftColor.getColorValue() == null) {
            throw new IllegalArgumentException("Not a valid color");
        }
        return TextColor.color(minecraftColor.getColorValue());
    }

    public static @Nullable Formatting asVanilla(TextColor color) {
        return Formatting.byColorIndex(color.value());
    }
}
