package org.cardboardpowered.adventure;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.IOException;
import java.util.UUID;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.gson.LegacyHoverEventSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.kyori.adventure.util.Codec;
import net.minecraft.nbt.StringNbtReader;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

final class NBTLegacyHoverEventSerializer implements LegacyHoverEventSerializer {
    public static final NBTLegacyHoverEventSerializer INSTANCE = new NBTLegacyHoverEventSerializer();
    private static Codec<NbtCompound, String, CommandSyntaxException, RuntimeException>  SNBT_CODEC = Codec.of(StringNbtReader::parse, NbtElement::toString);

    static final String ITEM_TYPE = "id";
    static final String ITEM_COUNT = "Count";
    static final String ITEM_TAG = "tag";

    static final String ENTITY_NAME = "name";
    static final String ENTITY_TYPE = "type";
    static final String ENTITY_ID = "id";

    NBTLegacyHoverEventSerializer() {
    }

    @Override
    public HoverEvent.ShowItem deserializeShowItem(final Component input) throws IOException {
        final String raw = PlainComponentSerializer.plain().serialize(input);
        try {
            final NbtCompound contents = SNBT_CODEC.decode(raw);
            final NbtCompound tag = contents.getCompound(ITEM_TAG);
            return HoverEvent.ShowItem.of(
                Key.key(contents.getString(ITEM_TYPE)),
                contents.contains(ITEM_COUNT) ? contents.getByte(ITEM_COUNT) : 1,
                tag.isEmpty() ? null : BinaryTagHolder.encode(tag, SNBT_CODEC)
            );
        } catch (final CommandSyntaxException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public HoverEvent.ShowEntity deserializeShowEntity(final Component input, final Codec.Decoder<Component, String, ? extends RuntimeException> componentCodec) throws IOException {
        final String raw = PlainComponentSerializer.plain().serialize(input);
        try {
            final NbtCompound contents = SNBT_CODEC.decode(raw);
            return HoverEvent.ShowEntity.of(
                Key.key(contents.getString(ENTITY_TYPE)),
                UUID.fromString(contents.getString(ENTITY_ID)),
                componentCodec.decode(contents.getString(ENTITY_NAME))
            );
        } catch (final CommandSyntaxException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public Component serializeShowItem(final HoverEvent.ShowItem input) throws IOException {
        final NbtCompound tag = new NbtCompound();
        tag.putString(ITEM_TYPE, input.item().asString());
        tag.putByte(ITEM_COUNT, (byte) input.count());
        if (input.nbt() != null) {
            try {
                tag.put(ITEM_TAG, input.nbt().get(SNBT_CODEC));
            } catch (final CommandSyntaxException ex) {
                throw new IOException(ex);
            }
        }
        return Component.text(SNBT_CODEC.encode(tag));
    }

    @Override
    public Component serializeShowEntity(final HoverEvent.ShowEntity input, final Codec.Encoder<Component, String, ? extends RuntimeException> componentCodec) throws IOException {
        final NbtCompound tag = new NbtCompound();
        tag.putString(ENTITY_ID, input.id().toString());
        tag.putString(ENTITY_TYPE, input.type().asString());
        if (input.name() != null) {
            tag.putString(ENTITY_NAME, componentCodec.encode(input.name()));
        }
        return Component.text(SNBT_CODEC.encode(tag));
    }
}
