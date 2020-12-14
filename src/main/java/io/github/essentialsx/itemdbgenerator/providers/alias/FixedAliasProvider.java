package io.github.essentialsx.itemdbgenerator.providers.alias;

import io.github.essentialsx.itemdbgenerator.providers.item.ItemProvider;
import org.bukkit.Material;

import java.util.EnumMap;
import java.util.stream.Stream;

public class FixedAliasProvider implements AliasProvider {
    private static final EnumMap<Material, String[]> FIXED_ALIASES = new EnumMap<>(Material.class);

    private static void add(Material material, String... aliases) {
        FIXED_ALIASES.put(material, aliases);
    }

    static {
        add(Material.BEDROCK, "oprock", "opblock", "adminblock", "adminrock", "adminium");
        add(Material.OBSIDIAN, "obsi", "obby");
        // == Interactive ==
        add(Material.CHEST, "container", "drawer");
        add(Material.CRAFTING_TABLE, "workbench", "craftingbench", "crafterbench", "craftbench", "worktable", "craftertable", "crafttable", "wbench", "cbench");
        add(Material.ENCHANTING_TABLE, "enchantmenttable", "enchanttable", "etable", "magicaltable", "magictable", "mtable", "enchantmentdesk", "enchantingdesk", "enchantdesk", "edesk", "magicaldesk", "magicdesk", "mdesk", "booktable", "bookdesk", "btable", "bdesk");
        add(Material.ENDER_CHEST, "endchest", "echest", "chestender", "chestend", "cheste", "endercontainer", "endcontainer", "econtainer");
        add(Material.BEACON, "beaconblock");
        add(Material.CHIPPED_ANVIL, "slightlydamagedanvil", "slightdamageanvil");
        add(Material.DAMAGED_ANVIL, "verydamagedanvil");
        // == Dirt ==
        add(Material.GRASS_BLOCK, "greendirt", "greenearth", "greenland");
        add(Material.DIRT, "earth", "land");
        add(Material.COARSE_DIRT, "cdirt", "grasslessdirt", "grasslessearth", "grasslessland", "coarseland", "coarseearth");
        add(Material.MYCELIUM, "mycel", "swampgrass", "sgrass", "mushroomgrass", "mushgrass");
        // == Redstone ==
        add(Material.REDSTONE_TORCH, "rstonetorch", "redstorch", "redtorch", "rstorch");
        add(Material.DISPENSER, "dispense");
        add(Material.NOTE_BLOCK, "musicblock", "nblock", "mblock");
        add(Material.JUKEBOX, "jbox");
        add(Material.TNT, "tntblock", "blocktnt", "bombblock", "blockbomb", "dynamiteblock", "blockdynamite", "bomb", "dynamite");
        add(Material.TRIPWIRE_HOOK, "trip", "tripwirelever", "triphook");
        add(Material.TRAPPED_CHEST, "trapchest", "chesttrapped", "chesttrap");
        add(Material.DAYLIGHT_DETECTOR, "daylightsensor", "daylightsense", "lightsensor", "lightsense", "daysensor", "daysense", "timesensor", "timesense");
        add(Material.HOPPER, "chestpuller", "chestpull", "cheststorer", "cheststore", "itempuller", "itempull", "itemstorer", "itemstore");
        add(Material.STONE_PRESSURE_PLATE, "smoothstonepressueplate", "smoothstonepressplate", "smoothstonepplate", "smoothstoneplate", "sstonepressureplate", "sstonepressplate", "sstonepplate", "sstoneplate");
        add(Material.COMMAND_BLOCK, "blockcommand", "cmdblock", "blockcmd", "macroblock", "blockmacro");
        add(Material.CHAIN_COMMAND_BLOCK, "chaincmdblock", "chcmdblock", "chainmacroblock", "chblockcmd");
        add(Material.REPEATING_COMMAND_BLOCK, "repcmdblock", "loopcmdblock", "loopmacroblock", "loopblockcmd");
        add(Material.DAYLIGHT_DETECTOR, "lightdetector", "photoresistor", "daydetector", "lightdetect", "solarpanel", "daydetect");
        // == Decorative ==
        add(Material.RED_SAND, "rsand");
        add(Material.GLASS, "blockglass", "glassblock");
        add(Material.GLASS_PANE, "glassp", "paneglass", "pglass", "flatglass", "fglass", "skinnyglass", "glassflat", "glassf", "glassskinny", "glasss");
        add(Material.BOOKSHELF, "bshelf", "bookcase", "casebook", "shelfbook", "bookblock", "blockbook");
        add(Material.TORCH, "burningstick", "burnstick");
        add(Material.GLOWSTONE, "glowingstoneblock", "lightstoneblock", "glowstoneblock", "blockglowingstone", "blocklightstone", "blockglowstone", "glowingstone", "lightstone", "glowingblock", "lightblock", "glowblock", "lstone");
        add(Material.LILY_PAD, "waterlily", "lily", "swamppad", "lpad", "wlily");
        add(Material.ANCIENT_DEBRIS, "debris");
        add(Material.CRYING_OBSIDIAN, "cryobsidian", "sadrock");
        add(Material.RESPAWN_ANCHOR, "respawnpoint", "spawnanchor", "respawnanc", "spawnanc", "netherbed");
        add(Material.LODESTONE, "lode");
        add(Material.SHROOMLIGHT, "shroomlamp", "netherlamp", "shlight");
        // "CUT_SANDSTONE" used to be called "SMOOTH_SANDSTONE"
        // "SMOOTH_SANDSTONE" is now a double slab
        add(Material.COBWEB, "spiderweb", "sweb", "cweb", "web");
        add(Material.IRON_BARS, "ironbarsb", "ironbarsblock", "ironfence", "metalbars", "metalbarsb", "metalbarsblock", "metalfence", "jailbars", "jailbarsb", "jailbarsblock", "jailfence", "mbars", "mbarsb", "mbarsblock", "mfence", "jbars", "jbarsb", "jbarsblock", "ibars", "ibarsb", "ibarsblock", "ifence");
        add(Material.ICE, "frozenwater", "waterfrozen", "freezewater", "waterfreeze");
        add(Material.HAY_BLOCK, "hay", "haybale", "baleofhay", "hayofbale");
        // == Plants (not crops) ==
        add(Material.TALL_GRASS, "longgrass", "wildgrass", "grasslong", "grasstall", "grasswild", "lgrass", "tgrass", "wgrass");
        add(Material.DEAD_BUSH, "bush", "deadshrub", "dshrub", "dbush", "deadsapling");
        add(Material.DANDELION, "yellowdandelion", "ydandelion", "yellowflower", "yflower", "flower");
        add(Material.POPPY, "rose", "redrose", "rrose", "redflower", "rflower", "poppy", "redpoppy");
        add(Material.BLUE_ORCHID, "cyanorchid", "lightblueorchid", "lblueorchid", "orchid", "cyanflower", "lightblueflower", "lblueflower");
        add(Material.ALLIUM, "magentaallium", "magentaflower");
        add(Material.AZURE_BLUET, "whiteazurebluet", "abluet", "azureb", "houstonia");
        add(Material.RED_TULIP, "tulipred", "rtulip", "tulipr");
        add(Material.WHITE_TULIP, "tulipwhite", "wtulip", "tulipw");
        add(Material.PINK_TULIP, "tulippink", "ptulip", "tulipp");
        add(Material.ORANGE_TULIP, "tuliporange", "otulip", "tulipo");
        add(Material.OXEYE_DAISY, "oxeye", "daisy", "daisyoxeye", "moondaisy", "daisymoon", "lightgrayoxeye", "lgrayoxeye", "lightgreyoxeye", "lgreyoxeye");
        add(Material.CACTUS, "cactuses", "cacti");
        add(Material.VINE, "vines", "greenvines", "greenvine", "gardenvines", "gardenvine", "vinesgreen", "vinegreen", "vinesgarden", "vinegarden", "vinesg", "vineg", "gvines", "gvine");
        add(Material.COCOA_BEANS, "cocoaplant", "cocoplant", "cplant", "cocoafruit", "cocofruit", "cfruit", "cocoapod", "cocopod", "cpod");
        add(Material.NETHER_SPROUTS, "nsprouts", "nethsprouts", "nsprout", "nethsprout", "nethersprout", "netherweed");
        add(Material.TWISTING_VINES, "twistvines", "twistvine");
        add(Material.WEEPING_VINES, "weepvines", "weepvine");
        // == Combat ==
        add(Material.SHIELD, "handshield", "woodshield", "woodenshield");
        add(Material.TOTEM_OF_UNDYING, "totem");
        // == Crops ==
        add(Material.CARVED_PUMPKIN, "hollowpumpkin", "cutpumpkin", "oldpumpkin", "legacypumpkin");
        add(Material.JACK_O_LANTERN, "pumpkinlantern", "glowingpumpkin", "lightpumpkin", "jpumpkin", "plantren", "glowpumpkin", "gpumpkin", "lpumpkin");
        add(Material.BEETROOT, "broot", "beet", "beets", "beetplant", "beetcrop");
        add(Material.BEETROOT_SEEDS, "beetrootseed", "brootseed", "brootseeds", "beetseed", "beetseeds", "beetsseeds", "beetplantseeds", "beetcropseeds");
        add(Material.MELON, "watermelon", "greenmelon", "melongreen", "melonblock", "watermelonblock", "greenmelonblock");
        // == Food ==
        add(Material.BEETROOT_SOUP, "brootsoup", "beetsoup", "beetssoup", "beetplantsoup", "beetcropsoup", "redsoup");
        add(Material.GOLDEN_APPLE, "goldapple", "newgoldapple", "notnotchapple");
        add(Material.ENCHANTED_GOLDEN_APPLE, "notchapple", "godapple", "enchgoldapple");
        // == End Materials ==
        add(Material.END_PORTAL, "endergoo", "enderportal", "endgoo", "eportal", "egoo");
        add(Material.END_PORTAL_FRAME, "endergooframe", "enderportalframe", "endgooframe", "eportalframe", "egooframe", "enderframe", "endframe");
        add(Material.END_STONE, "enderstone", "endrock", "enderrock", "erock", "estone");
        add(Material.DRAGON_EGG, "enderdragonegg", "endegg", "degg", "bossegg", "begg");
        add(Material.ELYTRA, "hangglider", "glider", "wings", "wing", "playerwings", "playerwing", "pwings", "pwing");
        add(Material.CHORUS_FRUIT, "chorus", "unpoppedchorus", "unpopchorus");
        add(Material.POPPED_CHORUS_FRUIT, "pchorus", "poppedchorus", "popchorus");
        add(Material.PHANTOM_MEMBRANE, "membrane", "superduperelytrarepairkit", "phmembrane", "pmembrane");
    }

    @Override
    public Stream<String> get(ItemProvider.Item item) {
        return null;
    }
}
