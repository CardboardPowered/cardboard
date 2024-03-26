package org.bukkit.craftbukkit.packs;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackCompatibility;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.metadata.PackResourceMetadata;

import org.bukkit.Bukkit;
import org.bukkit.FeatureFlag;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.CraftFeatureFlag;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.packs.DataPack;

public class CraftDataPack
implements DataPack {
    private final ResourcePackProfile handle;
    
    private PackResourceMetadata resourcePackInfo;

    public CraftDataPack(ResourcePackProfile handler) {
        this.handle = handler;
        
        try (ResourcePack pack = this.handle.packFactory.open(this.handle.getName())) {
        	this.resourcePackInfo = pack.parseMetadata(PackResourceMetadata.SERIALIZER);
        } catch (IOException e) { // This is already called in NMS then if in NMS not happen is secure this not throw here
        	throw new RuntimeException(e);
        }
    }

    public ResourcePackProfile getHandle() {
        return this.handle;
    }

    public String getRawId() {
        return this.getHandle().getName();
    }

    public String getTitle() {
        return CraftChatMessage.fromComponent(this.getHandle().getDisplayName());
    }

    public String getDescription() {
        return CraftChatMessage.fromComponent(this.getHandle().getDescription());
    }

    public int getPackFormat() {
        return resourcePackInfo.packFormat();
    }

    public boolean isRequired() {
        return this.getHandle().isAlwaysEnabled();
    }

    public DataPack.Compatibility getCompatibility() {
        return switch (this.getHandle().getCompatibility()) {
            default -> throw new IncompatibleClassChangeError();
            case COMPATIBLE -> DataPack.Compatibility.COMPATIBLE;
            case TOO_NEW -> DataPack.Compatibility.NEW;
            case TOO_OLD -> DataPack.Compatibility.OLD;
        };
    }

    public boolean isEnabled() {
        return ((CraftServer)Bukkit.getServer()).getServer().getDataPackManager().getEnabledNames().contains(this.getRawId());
    }

    public DataPack.Source getSource() {
        if (this.getHandle().getSource() == ResourcePackSource.BUILTIN) {
            return DataPack.Source.BUILT_IN;
        }
        if (this.getHandle().getSource() == ResourcePackSource.FEATURE) {
            return DataPack.Source.FEATURE;
        }
        if (this.getHandle().getSource() == ResourcePackSource.WORLD) {
            return DataPack.Source.WORLD;
        }
        if (this.getHandle().getSource() == ResourcePackSource.SERVER) {
            return DataPack.Source.SERVER;
        }
        return DataPack.Source.DEFAULT;
    }

    public Set<FeatureFlag> getRequestedFeatures() {
        return CraftFeatureFlag.getFromNMS(this.getHandle().getRequestedFeatures()).stream().map(FeatureFlag.class::cast).collect(Collectors.toUnmodifiableSet());
    }

    public NamespacedKey getKey() {
        return NamespacedKey.fromString((String)this.getRawId());
    }

    public String toString() {
        String requestedFeatures = this.getRequestedFeatures().stream().map(featureFlag -> featureFlag.getKey().toString()).collect(Collectors.joining(","));
        return "CraftDataPack{rawId=" + this.getRawId() + ",id=" + this.getKey() + ",title=" + this.getTitle() + ",description=" + this.getDescription() + ",packformat=" + this.getPackFormat() + ",compatibility=" + this.getCompatibility() + ",source=" + this.getSource() + ",enabled=" + this.isEnabled() + ",requestedFeatures=[" + requestedFeatures + "]}";
    }
}

