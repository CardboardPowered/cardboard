package org.cardboardpowered.mixin;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cardboardpowered.library.Library;
import org.cardboardpowered.library.LibraryKey;
import org.cardboardpowered.library.LibraryManager;
import static org.cardboardpowered.library.LibraryManager.HashAlgorithm.SHA1;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import com.google.common.collect.ImmutableMap;

import net.fabricmc.loader.api.FabricLoader;

public class CardboardMixinPlugin implements IMixinConfigPlugin {

    private static final String MIXIN_PACKAGE_ROOT = "com.javazilla.bukkitfabric.mixin.";
    private final Logger logger = LogManager.getLogger("Cardboard");

    @Override
    public void onLoad(String mixinPackage) {
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
                new Library("org.apache.logging.log4j", "log4j-core", "2.8.1", SHA1, "4ac28ff2f1ddf05dae3043a190451e8c46b73c31", null)
            ).collect(ImmutableMap.toImmutableMap(Library::getLibraryKey, Function.identity()));
        new LibraryManager(repository, "lib", true, 2, libraries.values()).run();

        String internalVer = System.getProperty("java.class.version");
        double javaVer = Double.valueOf(internalVer) - 44;

        if (javaVer <= 8) {
            // The JDK and JRE are not separate post Java 8
            // So we don't need to worry about it.
            Map<LibraryKey, Library> libraries3 = Stream.of(
                    new Library("com.google.errorprone", "javac", "1.8.0-u20", SHA1, "b23b2b0e3f79e3f737496a9eca5bab65cdca791d", null)
                ).collect(ImmutableMap.toImmutableMap(Library::getLibraryKey, Function.identity()));
            new LibraryManager(repository, "lib", true, 2, libraries3.values()).run();
        } else {
            File jdk = new File("lib", "javac-1.8.0-u20.jar");
            if (jdk.exists())
                jdk.delete();
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String mixin = mixinClassName.substring(MIXIN_PACKAGE_ROOT.length());
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
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode target, String mixinClassName, IMixinInfo info) {
    }

}