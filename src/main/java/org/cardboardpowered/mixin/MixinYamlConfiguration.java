package org.cardboardpowered.mixin;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

@Mixin(YamlConfiguration.class)
public abstract class MixinYamlConfiguration extends FileConfiguration {
    
    @Shadow private LoaderOptions loaderOptions;
    @Shadow private Yaml yaml;

    public void cardboard_setMaxAliasesForCollections(int max) {
        try {
            loaderOptions.setMaxAliasesForCollections(Integer.MAX_VALUE); // SPIGOT-5881: Not ideal, but was default pre SnakeYAML 1.26
        } catch (NoSuchMethodError | SecurityException e) {
            // A mod did not include YAML properly.
        }
    }

    /**
     * @author Cardboard
     * @reason Some mods come bundled with YAML. Unfortunately due to know
     *  class loading works for mods with Fabric we need to make sure we
     *  stay compatible with SnakeYAML pre-1.26
     */
    @Overwrite
    public void loadFromString(String contents) throws InvalidConfigurationException {
        Validate.notNull(contents, "Contents cannot be null");

        Map<?, ?> input;
        try {
            cardboard_setMaxAliasesForCollections(Integer.MAX_VALUE); // SPIGOT-5881: Not ideal, but was default pre SnakeYAML 1.26
            input = (Map<?, ?>) yaml.load(contents);
        } catch (YAMLException | ClassCastException e) {
            throw new InvalidConfigurationException();
        }

        String header = parseHeader(contents);
        if (header.length() > 0) options().header(header);

        this.map.clear();

        if (input != null) convertMapsToSections(input, (YamlConfiguration)(Object)this);
    }

    @Shadow
    protected String parseHeader(String input) {
        return "mixin";
    }

    @Shadow
    protected void convertMapsToSections(Map<?, ?> input, ConfigurationSection section) {
    }

}