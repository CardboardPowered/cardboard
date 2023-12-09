package org.cardboardpowered.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.minecraft.text.Text;


final class WrapperAwareSerializer implements ComponentSerializer<Component, Component, Text> {

    @Override
    public Component deserialize(final Text input) {
        if (input instanceof CardboardAdventureComponent) {
            return ((CardboardAdventureComponent) input).adventure;
        }
        return CardboardAdventure.GSON.serializer().fromJson(Text.Serialization.toJsonTree(input), Component.class);
    }

    @Override
    public Text serialize(final Component component) {
        return Text.Serialization.fromJson(CardboardAdventure.GSON.serializer().toJson(component));
    }

}
