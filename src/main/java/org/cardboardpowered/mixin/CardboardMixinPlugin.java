package org.cardboardpowered.mixin;

import static org.cardboardpowered.library.LibraryManager.HashAlgorithm.SHA1;
import static org.cardboardpowered.library.LibraryManager.HashAlgorithm.MD5;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cardboardpowered.CardboardConfig;
import org.cardboardpowered.library.Library;
import org.cardboardpowered.library.LibraryManager;
import org.cardboardpowered.util.GameVersion;
import org.cardboardpowered.util.JarReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.fabricmc.loader.api.FabricLoader;

public class CardboardMixinPlugin implements IMixinConfigPlugin {

    private static final String MIXIN_PACKAGE_ROOT = "org.cardboardpowered.mixin.";
    private final Logger logger = LogManager.getLogger("Cardboard");
    public static boolean libload = true;
    private static boolean read_plugins = false;

    @Override
    public void onLoad(String mixinPackage) {
    	
    	// testing
        /*if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
        	System.out.println("HELLO");
        	MappTest.do_test(null);
        	System.exit(0);
        	return;
        }*/
    	
        try {
            CardboardConfig.setup();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        File pl = new File("plugins");
        if (pl.exists()) {
            try {
                JarReader.read_plugins(pl);
                read_plugins = true;
            } catch (Exception e) {
                read_plugins = false;
                e.printStackTrace();
            }
        }

        logger.info("Loading Libraries...");
        loadLibs();
    }

    public static void loadLibs() {
        String repository = "https://repo1.maven.org/maven2/"; 
        String mcver = GameVersion.create().getReleaseTarget();

        List<Library> libraries = new ArrayList<>();

        // Paper API
        // libraries.add( new Library("io.papermc", "paper-api", "1.18.2-167", SHA1, "83a5712c916379a405bc0ab330a724e7edfdaca5", "paper") );
        // libraries.add( new Library("io.papermc", "paper-api", "1.19.2-307", SHA1, "b44b4312df3673f9ea85133b61334dfac7f7dc14", "paper") );
        // libraries.add( new Library("io.papermc", "paper-api", "1.19.4-550", SHA1, "3407bd5d6dcc3223dc4ea17eb5eebdafbc1bba92", "paper") );

        // libraries.add( new Library("io.papermc", "paper-api", "1.20.1-R0.1-20230921.165944-178", SHA1, "71680be2f2e80f25e20ae4b49ebd84539d306a44", "paper") );
        // libraries.add( new Library("io.papermc", "paper-api", "1.20.2-R0.1-20231203.034718-122", SHA1, "faf51e2d9f8eacd902e990a05b2760798acc0b37", "paper") );
        // libraries.add( new Library("io.papermc", "paper-api", "1.20.3-R0.1-20231207.043048-3", SHA1, "c54200af33674c0fdc965317a1c72e3a0e02e236", "paper") );
        // libraries.add( new Library("io.papermc", "paper-api", "1.20.4-R0.1-20240528.102248-175", SHA1, "1655f2444d10a3eb8f176f97c3431731e7cc7c1a", "paper") );
        // libraries.add( new Library("io.papermc", "paper-api", "1.20.5-R0.1-20240429.035133-20", SHA1, "5ea381742186b489251fdbf6e7dba7fdcc142b4d", "paper") );
        // libraries.add( new Library("io.papermc", "paper-api", "1.20.6-R0.1-20240916.192025-126", SHA1, "15ac8ae6289dddb563d17a20dd3ed80ad89eb629", "paper"));

        // libraries.add( new Library("io.papermc", "paper-api", "1.21.1-R0.1-20240810.105324-1", SHA1, "f1f242327127cc802d94fba1ec823d420d86ff2d", "paper"));
        // libraries.add( new Library("io.papermc", "paper-api", "1.21.1-R0.1-20241021.162528-124", SHA1, "2bc94e08501ef8145e562ae57daa03a5363630a7", "paper"));
        // libraries.add( new Library("io.papermc", "paper-api", "1.21.4-R0.1-20241215.095037-18", SHA1, "83ab82c30f937f3b7620e1f24191e98ee6270a60", "paper"));
        // libraries.add( new Library("io.papermc", "paper-api", "1.21.4-R0.1-20241226.084728-40", SHA1, "982e25808ef6c419a4be15c88ff2c7bd0bc23258", "paper"));
        
        // libraries.add( new Library("io.papermc", "paper-api", "1.21.4-R0.1-20250118.134032-116", SHA1, "6a1c95efe04acca299d975021cfc9f33ca28d4c0", "paper"));
        // libraries.add( new Library("io.papermc", "paper-api", "1.21.4-R0.1-20250303.170158-184", SHA1, "c4f8188cbd61e52623069cf19054c43086fcbef7", "paper"));

        // libraries.add( new Library("io.papermc", "paper-api", "1.21.4-R0.1-20250313.115134-207", SHA1, "8531d7de9321971d382e2dd1a3e4555e006a7326", "paper"));
        libraries.add( new Library("io.papermc", "paper-api", "1.21.4-R0.1-20250511.205801-225", SHA1, "664d0e9471c4a71af483734af88d6e57bbbd226c", "paper"));

        
        // Paper API Libraries
        libraries.add( new Library("org.xerial", "sqlite-jdbc", "3.41.0.0", MD5, "0d63ee5b583e9a75ea1717ffce63fed8", null));
        libraries.add( new Library("com.mysql", "mysql-connector-j", "8.0.32", MD5, "25bf3b3cd262065283962078dc82e99c", null));
		libraries.add( new Library("commons-lang", "commons-lang", "2.6", SHA1, "0ce1edb914c94ebc388f086c6827e8bdeec71ac2", null) );
        libraries.add( new Library("org.apache.commons", "commons-collections4", "4.4", SHA1, "62ebe7544cb7164d87e0637a2a6a2bdc981395e8", null) );
        libraries.add( new Library("commons-collections", "commons-collections", "3.2.1", SHA1, "761ea405b9b37ced573d2df0d1e3a4e0f9edc668", null) );


        // net/md-5/bungeecord-chat/1.16-R0.4/
        
        libraries.add( new Library("net.md-5", "bungeecord-chat", "1.16-R0.4", SHA1, "e043e8eed8fdb5c157090a84ac8fd64a6a8d0d88", null) );
        
        // TODO: Check Adventure version
        // String adventureVersion = "4.9.3";
        // String adventureVersion = "4.13.1";
        // String adventureVersion = "4.16.0";
        String adventureVersion = "4.18.0";

        libraries.add( new Library("net.kyori", "adventure-api", adventureVersion, SHA1, "2c26e57011830cb86f86b9f9d936fcffdf43e8b8", null) );
        libraries.add( new Library("net.kyori", "adventure-key", adventureVersion, SHA1, "dc9db1a20e8ddc9ca50bebbf0a5af7d7a8812f95", null) );
        libraries.add( new Library("net.kyori", "adventure-text-serializer-gson", adventureVersion, SHA1, "f0f59c9bc9747626d6e51d4a99bc5a76eaca59ab", null) );
        libraries.add( new Library("net.kyori", "adventure-text-serializer-legacy", adventureVersion, SHA1, "04bf5d9605f768ebff17cf44db9998bf24a56572", null) );
        libraries.add( new Library("net.kyori", "adventure-text-serializer-plain", adventureVersion, SHA1, "5870a0c2d8825ada8e127086592419c67003a5c0", null) );
        libraries.add( new Library("net.kyori", "adventure-text-minimessage", adventureVersion, SHA1, "b4d004e56668e9670836c3c191a501cd92f3784e", null) );

         
        // libraries.add( new Library("net.kyori", "adventure-api", adventureVersion, SHA1, "cb966704b813d30d4ee9f0b97167b4063a249c34", null) );
        // libraries.add( new Library("net.kyori", "adventure-key", adventureVersion, SHA1, "b695c40a7d2fd658246de78ea428e8f8dc7ffd2d", null) );
        // libraries.add( new Library("net.kyori", "adventure-text-serializer-gson", adventureVersion, SHA1, "5650ed18040e070aa05855ebcb890e6e1e36ee0e", null) );
        // libraries.add( new Library("net.kyori", "adventure-text-serializer-legacy", adventureVersion, SHA1, "21b9450c659146ea4ac6ef3555b5ea1008566b69", null) );
        // libraries.add( new Library("net.kyori", "adventure-text-serializer-plain", adventureVersion, SHA1, "4d5e8b73aac1e5e17dd24a75edd8fd077098e18c", null) );
        
        // libraries.add( new Library("net.kyori", "adventure-api", adventureVersion, SHA1, "b0054b3a4d144f09962fe72abc746191e7f931a2", null) );
        // libraries.add( new Library("net.kyori", "adventure-text-serializer-gson", adventureVersion, SHA1, "141df0329e00b791bcc8b2921cd715ac7d506bbe", null) );
        // libraries.add( new Library("net.kyori", "adventure-text-serializer-legacy", adventureVersion, SHA1, "9e7811601e508e0af6d53e35eab3588ee607bff6", null) );
        // libraries.add( new Library("net.kyori", "adventure-text-serializer-plain", adventureVersion, SHA1, "61d39db7e84c11d91be551cb9bc50c1aa7e64983", null) );

        if (mcver.contains("1.17")) {
    		//libraries.add( new Library("org.xerial", "sqlite-jdbc", "3.21.0.1", SHA1, "81a0bcda2f100dc91dc402554f60ed2f696cded5", null) );
            //libraries.add( new Library("mysql", "mysql-connector-java", "5.1.46", SHA1, "9a3e63b387e376364211e96827bc27db8d7a92e9", null) );
        	//libraries.add( new Library("org.cardboardpowered", "intermediary-adapter", "7.3", SHA1, "", null) );
            libraries.add( new Library("org.jline", "jline", "3.19.0", SHA1, "27edf6497c4fac20b63ca4cd8788581ca86cb83e", null) );
        }
        
        // Set WorldEdit adapter class name here
        // as this provides more verbose stacktraces.
        System.setProperty("worldedit.bukkit.adapter", "com.sk89q.worldedit.bukkit.adapter.impl.v1_21_4.PaperweightAdapter");

        new LibraryManager(repository, "lib", true, 2, libraries).run();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    public static boolean is_event_found(String event) {
        if (!read_plugins) {
            return true;
        }
        return JarReader.found.contains(event);
    }
    
    // private static final TestCl dummy = new TestCl();

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String mixin = mixinClassName.substring(MIXIN_PACKAGE_ROOT.length());
        if (CardboardConfig.disabledMixins.contains(mixinClassName)) {
            logger.info("Disabling mixin '" + mixin + "', was forced disabled in config.");
            return false;
        }

        if (mixin.equals("item.MixinChorusFruitItem")) {
            FabricLoader loader = FabricLoader.getInstance();
            boolean create_mod = loader.isModLoaded("porting_lib");
            if (create_mod) {
                return false;
            }
        }

        if (mixin.equals("network.MixinServerPlayNetworkHandler_ChatEvent") && 
                should_force_alternate_chat()) {
            logger.info("Architectury Mod detected! Disabling async chat from NetworkHandler.");
            return false;
        }

        /*if (mixin.equals("network.MixinPlayerManager_ChatEvent")) {
            if (should_force_alternate_chat()) {
                logger.info("Architectury Mod detected! Using alternative async chat from PlayerManager");
                return true;
            } else return false;
        }
        if (CardboardConfig.ALT_CHAT && (mixin.contains("_ChatEvent"))) {
            logger.info("Alternative ChatEvent Mixin enabled in config. Changing status on: " + mixin);
            if (mixin.equals("network.MixinServerPlayNetworkHandler_ChatEvent")) return false;
            if (mixin.equals("network.MixinPlayerManager_ChatEvent")) return true;
        }*/
        
        String mcver = GameVersion.create().getReleaseTarget();
        if (mcver.contains("1.18") && mixin.endsWith("_1_19")) {
        	return false;
        }
        if (mcver.contains("1.19") && mixin.endsWith("_1_18")) {
        	return false;
        }
        if (mcver.contains("1.20") && mixin.endsWith("_1_18")) {
        	return false;
        }


        // Disable mixin if event is not found in plugins.
        /*if (not_has_event(mixin, "LeashKnotEntity", "PlayerLeashEntityEvent") && not_has_event(mixin, "LeashKnotEntity", "PlayerUnleashEntityEvent")) return false;
        if (not_has_event(mixin, "GoToWorkTask", "VillagerCareerChangeEvent")) return false;
        if (not_has_event(mixin, "LoseJobOnSiteLossTask", "VillagerCareerChangeEvent")) return false;
        if (not_has_event(mixin, "PiglinBrain", "EntityPickupItemEvent")) return false;
        if (not_has_event(mixin, "DyeItem", "SheepDyeWoolEvent")) return false;
        if (not_has_event(mixin, "FrostWalkerEnchantment", "BlockFormEvent")) return false;
        if (not_has_event(mixin, "ExperienceOrbEntity", "PlayerItemMendEvent") || not_has_event(mixin, "ExperienceOrbEntity", "PlayerExpChangeEvent")) return false;
        if (not_has_event(mixin, "Explosion", "EntityExplodeEvent") && not_has_event(mixin, "Explosion", "BlockExplodeEvent")) return false;
        if (not_has_event(mixin, "LeavesBlock", "LeavesDecayEvent")) return false;
        if (not_has_event(mixin, "PlayerAdvancementTracker", "PlayerAdvancementDoneEvent")) return false;
*/
        if (mixinClassName.contains("ServerPlayNetworkHandler")) return true;

        try {
            URL[] jar = {
                    FabricLoader.getInstance().getModContainer("cardboard").get().getRootPath().toUri().toURL(),
                    FabricLoader.getInstance().getModContainer("minecraft").get().getRootPath().toUri().toURL(),
                    FabricLoader.getInstance().getModContainer("fabricloader").get().getRootPath().toUri().toURL(),
                    new File(new File("lib"), "paper-api-1.17-dev.jar").toURI().toURL()
            };

            Class<?> c = Class.forName(mixinClassName, false, new URLClassLoader(jar));

            for (Annotation a : c.getAnnotations()) {
                String e = a.toString().split("events=")[1].substring(1);
                e = e.substring(0, e.lastIndexOf("}")).replace("\"", "");
                String[] events = e.split(", ");
                if (events.length > 0) {
                    if (events[0].length() < 4) {
                        return true; // No events
                    }
                    
                    boolean disable = true;
                    for (String ev : events) {
                        if (!not_has_event(mixin, mixin, ev))
                            disable = false;
                    }
                    //if (disable)
                    //    return false;
                }
            }
        
        } catch (Exception e) {
            logger.info(e.getMessage());
        }

        return true;
    }

    public boolean not_has_event(String mix, String mixin, String event) {
        if (mix.contains(mixin)) {
            boolean dev = FabricLoader.getInstance().isDevelopmentEnvironment();
            if (is_event_found(event)) {
                if (dev) {logger.info("DEBUG: Status of " + mixin + ": true. (" + event + ")");}
                return false;
            } else {
                if (dev) {logger.info("DEBUG: Status of " + mixin + ": false. (" + event + ")");}
                return true;
            }
        }
        return false;
    }

    /**
     * Check for mods that overwrite onGameMessage for chat event.
     */
    public boolean should_force_alternate_chat() {
        FabricLoader loader = FabricLoader.getInstance();
        String[] bad_mods = {"architectury", "dynmap"};

        for (String s : bad_mods) {
            if (loader.getModContainer(s).isPresent())
                return true;
        }
        return false;
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