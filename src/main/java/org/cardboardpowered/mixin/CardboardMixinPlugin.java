package org.cardboardpowered.mixin;

import static org.cardboardpowered.library.LibraryManager.HashAlgorithm.SHA1;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cardboardpowered.CardboardConfig;
import org.cardboardpowered.library.KnotHelper;
import org.cardboardpowered.library.Library;
import org.cardboardpowered.library.LibraryKey;
import org.cardboardpowered.library.LibraryManager;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import com.google.common.collect.ImmutableMap;

import net.fabricmc.loader.api.FabricLoader;

public class CardboardMixinPlugin implements IMixinConfigPlugin {

    private static final String MIXIN_PACKAGE_ROOT = "org.cardboardpowered.mixin.";
    private final Logger logger = LogManager.getLogger("Cardboard");

    @Override
    public void onLoad(String mixinPackage) {
        try {
            CardboardConfig.setup();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("Loading Libraries...");
        loadLibs();
    }

    public static void loadLibs() {
        String repository = "https://repo1.maven.org/maven2/"; 

        Map<LibraryKey, Library> libraries = Stream.of(
                new Library("org.xerial", "sqlite-jdbc", "3.21.0.1", SHA1, "81a0bcda2f100dc91dc402554f60ed2f696cded5", null),
                new Library("mysql", "mysql-connector-java", "5.1.46", SHA1, "9a3e63b387e376364211e96827bc27db8d7a92e9", null),
                new Library("commons-lang", "commons-lang", "2.6", SHA1, "0ce1edb914c94ebc388f086c6827e8bdeec71ac2", null),
                new Library("org.apache.commons", "commons-collections4", "4.4", SHA1, "62ebe7544cb7164d87e0637a2a6a2bdc981395e8", null),
                new Library("commons-collections", "commons-collections", "3.2.1", SHA1, "761ea405b9b37ced573d2df0d1e3a4e0f9edc668", null),
                new Library("com.google.code.gson", "gson", "2.8.6", SHA1, "9180733b7df8542621dc12e21e87557e8c99b8cb", null),
                new Library("org.jline", "jline", "3.19.0", SHA1, "27edf6497c4fac20b63ca4cd8788581ca86cb83e", null),
                new Library("io.papermc", "paper-api", "1.17-dev", SHA1, "1011c06b51835ac752e2f0b2a22d9188c566c169", "paper"),
                new Library("org.cardboardpowered", "intermediary-adapter", "7.3", SHA1, "", null)
            ).collect(ImmutableMap.toImmutableMap(Library::getLibraryKey, Function.identity()));
        new LibraryManager(repository, "lib", true, 2, libraries.values()).run();

        System.setProperty("worldedit.bukkit.adapter", "com.sk89q.worldedit.bukkit.adapter.impl.Spigot_Cardboard");
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String mixin = mixinClassName.substring(MIXIN_PACKAGE_ROOT.length());
        if (CardboardConfig.disabledMixins.contains(mixinClassName)) {
            logger.info("Disabling mixin '" + mixin + "', was forced disabled in config.");
            return false;
        }

        if (mixin.equals("network.MixinServerPlayNetworkHandler_ChatEvent") && 
                FabricLoader.getInstance().getModContainer("architectury").isPresent()) {
            logger.info("Architectury Mod detected! Disabling async chat from NetworkHandler.");
            return false;
        }
        if (mixin.equals("network.MixinPlayerManager_ChatEvent")) {
            if (FabricLoader.getInstance().getModContainer("architectury").isPresent()) {
                logger.info("Architectury Mod detected! Using alternative async chat from PlayerManager");
                return true;
            } else return false;
        }
        if (CardboardConfig.ALT_CHAT) {
            logger.info("Alternative ChatEvent Mixin enabled in config. Changing status on: " + mixin);
            if (mixin.equals("network.MixinServerPlayNetworkHandler_ChatEvent")) return false;
            if (mixin.equals("network.MixinPlayerManager_ChatEvent")) return true;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String target, ClassNode targetClass, String mixinClass, IMixinInfo info) {
    }

    @Override
    public void postApply(String targetClass, ClassNode target, String mixinClass, IMixinInfo info) {
    }

}