package org.cardboardpowered.adventure;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.PlainTextContent.Literal;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;

public final class CardboardAdventureComponent implements Text {

    final Component adventure;
    private @MonotonicNonNull Text vanilla;

    public CardboardAdventureComponent(Component adventure) {
        this.adventure = adventure;
    }

    public Text deepConverted() {
        Text vanilla = this.vanilla;
        if (vanilla == null) {
            this.vanilla = vanilla = CardboardAdventure.WRAPPER_AWARE_SERIALIZER.serialize(this.adventure);
        }
        return vanilla;
    }

    public @Nullable Text deepConvertedIfPresent() {
        return this.vanilla;
    }

    @Override
    public Style getStyle() {
        return this.deepConverted().getStyle();
    }

    @Override
    public TextContent getContent() {
        if (this.adventure instanceof TextComponent) {
            return new Literal(((TextComponent)this.adventure).content());
        }
        return this.deepConverted().getContent();
    }

    @Override
    public String getString() {
        return PlainTextComponentSerializer.plainText().serialize(this.adventure);
    }

    @Override
    public List<Text> getSiblings() {
        return this.deepConverted().getSiblings();
    }

    @Override
    public MutableText copyContentOnly() {
        return this.deepConverted().copyContentOnly();
    }

    @Override
    public MutableText copy() {
        return this.deepConverted().copy();
    }

    @Override
    public OrderedText asOrderedText() {
        return this.deepConverted().asOrderedText();
    }

    public static class Serializer
    implements JsonSerializer<CardboardAdventureComponent> {
        public JsonElement serialize(CardboardAdventureComponent src, Type type, JsonSerializationContext context) {
            return GsonComponentSerializer.gson().serializer().toJsonTree((Object)src.adventure, Component.class);
        }
    }
}
