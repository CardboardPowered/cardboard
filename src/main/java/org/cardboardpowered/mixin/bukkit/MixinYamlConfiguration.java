package org.cardboardpowered.mixin.bukkit;

import java.util.LinkedList;
import java.util.List;
import org.bukkit.configuration.file.YamlConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(YamlConfiguration.class)
public abstract class MixinYamlConfiguration {

    @Shadow(remap = false)
    private List<String> loadHeader(List<String> header) {
        LinkedList<String> list = new LinkedList<>(header);

        if (!list.isEmpty()) {
            list.removeLast();
        }

        while (!list.isEmpty() && list.peek() == null) {
            list.remove();
        }

        return list;
    }

}