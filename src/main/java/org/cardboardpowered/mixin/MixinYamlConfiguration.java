package org.cardboardpowered.mixin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.reader.UnicodeReader;

@Mixin(YamlConfiguration.class)
public abstract class MixinYamlConfiguration extends FileConfiguration {
    
    @Shadow(remap = false) private LoaderOptions yamlLoaderOptions;
    @Shadow(remap = false) private Yaml yaml;

    //@Shadow
    //private Yaml yaml;
    @Shadow(remap = false)
    private void adjustNodeComments(final MappingNode node) {}
    
    @Shadow(remap = false)
    private void fromNodeTree(MappingNode input, ConfigurationSection section) {}

    @Shadow(remap = false)
    private List<String> getCommentLines(List<CommentLine> comments) {
    	return null;
    }

    
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
    
    public void loadFromString(String contents) throws InvalidConfigurationException {
        //Preconditions.checkArgument(contents != null, "Contents cannot be null");
        // yamlLoaderOptions.setProcessComments(options().parseComments());
        
        MappingNode node;
        try (Reader reader = new UnicodeReader(new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8)))) {
            Node rawNode = yaml.compose(reader);
            try {
                node = (MappingNode) rawNode;
            } catch (ClassCastException e) {
                throw new InvalidConfigurationException("Top level is not a Map.");
            }
        } catch (YAMLException | IOException | ClassCastException e) {
            throw new InvalidConfigurationException(e);
        }

        this.map.clear();

        if (node != null) {
            adjustNodeComments(node);
            options().setHeader(loadHeader(getCommentLines(node.getBlockComments())));
            options().setFooter(getCommentLines(node.getEndComments()));
            fromNodeTree(node, this);
        }
    }
    /*public void cardboard_setMaxAliasesForCollections(int max) {
        try {
           // loaderOptions.setMaxAliasesForCollections(Integer.MAX_VALUE); // SPIGOT-5881
        } catch (NoSuchMethodError | SecurityException e) {
            // A mod did not include YAML properly.
        }
    }*/

    /**
     * @author Cardboard
     * @reason Some mods come bundled with YAML. Unfortunately due to how
     *  class loading works for mods with Fabric we need to make sure we
     *  stay compatible with SnakeYAML pre-1.26
     */
   // @Overwrite(remap = false)
   /* public void loadFromString(String contents) throws InvalidConfigurationException {
        Validate.notNull(contents, "Contents cannot be null");

        Map<?, ?> input;
        try {
            cardboard_setMaxAliasesForCollections(Integer.MAX_VALUE); // SPIGOT-5881
            input = (Map<?, ?>) yaml.load(contents);
        } catch (YAMLException | ClassCastException e) {
            throw new InvalidConfigurationException();
        }

        String header = parseHeader(contents);
        if (header.length() > 0) options().header(header);

        this.map.clear();

        if (input != null) convertMapsToSections(input, (YamlConfiguration)(Object)this);
    }

    //@Shadow(remap = false)
    protected String parseHeader(String input) {
        return "mixin";
    }

   // @Shadow(remap = false)
    protected void convertMapsToSections(Map<?, ?> input, ConfigurationSection section) {
    }*/

}