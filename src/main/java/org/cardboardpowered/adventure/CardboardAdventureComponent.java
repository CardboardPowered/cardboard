package org.cardboardpowered.adventure;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
// import net.minecraft.text.TextContent;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class CardboardAdventureComponent implements Text {
    final Component wrapped;
    private @MonotonicNonNull Text converted;

    public CardboardAdventureComponent(final Component wrapped) {
        this.wrapped = wrapped;
    }

    public Text deepConverted() {
        Text converted = this.converted;
        if (converted == null) {
            converted = CardboardAdventure.WRAPPER_AWARE_SERIALIZER.serialize(this.wrapped);
            this.converted = converted;
        }
        return converted;
    }

    public @Nullable Text deepConvertedIfPresent() {
        return this.converted;
    }

    @Override
    public Style getStyle() {
        return this.deepConverted().getStyle();
    }

    @Override
    public String asString() {
        if (this.wrapped instanceof TextComponent) {
            return ((TextComponent) this.wrapped).content();
        } else {
            return this.deepConverted().asString();
        }
    }

    @Override
    public String getString() {
        return CardboardAdventure.PLAIN.serialize(this.wrapped);
    }

    @Override
    public List<Text> getSiblings() {
        return this.deepConverted().getSiblings();
    }

    @Override
    public MutableText copy() {
        return this.deepConverted().copy();
    }

    @Override
    public MutableText shallowCopy() {
        return this.deepConverted().shallowCopy();
    }

    public static class Serializer implements JsonSerializer<CardboardAdventureComponent> {
        @Override
        public JsonElement serialize(final CardboardAdventureComponent src, final Type type, final JsonSerializationContext context) {
            return CardboardAdventure.GSON.serializer().toJsonTree(src.wrapped, Component.class);
        }
    }

    @Override
    public OrderedText asOrderedText() {
        return this.deepConverted().asOrderedText();
    }

    /**
     * Minecraft 1.19
     */
    //public TextContent getContent() {
    //    // TODO Auto-generated method stub
    //    return null;
    //}

}
