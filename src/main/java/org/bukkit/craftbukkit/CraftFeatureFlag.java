package org.bukkit.craftbukkit;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Identifier;
import org.bukkit.FeatureFlag;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.jetbrains.annotations.NotNull;

public class CraftFeatureFlag
implements FeatureFlag {
    private final NamespacedKey namespacedKey;
    private final net.minecraft.resource.featuretoggle.FeatureFlag featureFlag;

    public CraftFeatureFlag(Identifier minecraftKey, net.minecraft.resource.featuretoggle.FeatureFlag featureFlag) {
        this.namespacedKey = CraftNamespacedKey.fromMinecraft(minecraftKey);
        this.featureFlag = featureFlag;
    }

    public net.minecraft.resource.featuretoggle.FeatureFlag getHandle() {
        return this.featureFlag;
    }

    @NotNull
    public NamespacedKey getKey() {
        return this.namespacedKey;
    }

    public String toString() {
        return "CraftDataPack{key=" + this.getKey() + ",keyUniverse=" + this.getHandle().universe.toString() + "}";
    }

    public static Set<CraftFeatureFlag> getFromNMS(FeatureSet featureFlagSet) {
        HashSet<CraftFeatureFlag> set = new HashSet<CraftFeatureFlag>();
        FeatureFlags.FEATURE_MANAGER.featureFlags.forEach((minecraftkey, featureflag) -> {
            if (featureFlagSet.contains((net.minecraft.resource.featuretoggle.FeatureFlag)featureflag)) {
                set.add(new CraftFeatureFlag((Identifier)minecraftkey, (net.minecraft.resource.featuretoggle.FeatureFlag)featureflag));
            }
        });
        return set;
    }

    public static CraftFeatureFlag getFromNMS(NamespacedKey namespacedKey) {
        return FeatureFlags.FEATURE_MANAGER.featureFlags.entrySet().stream().filter(entry -> CraftNamespacedKey.fromMinecraft((Identifier)entry.getKey()).equals((Object)namespacedKey)).findFirst().map(entry -> new CraftFeatureFlag((Identifier)entry.getKey(), (net.minecraft.resource.featuretoggle.FeatureFlag)entry.getValue())).orElse(null);
    }
}

