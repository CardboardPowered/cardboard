package org.bukkit.craftbukkit;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.BanList.Type;
import org.bukkit.GameMode;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.StructureType;
import org.bukkit.UnsafeValues;
import org.bukkit.Warning.WarningState;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.Conversable;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.command.BukkitCommandWrapper;
import org.bukkit.craftbukkit.command.CraftCommandMap;
import org.bukkit.craftbukkit.command.CraftConsoleCommandSender;
import org.bukkit.craftbukkit.command.VanillaCommandWrapper;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.help.SimpleHelpMap;
import org.bukkit.craftbukkit.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.inventory.util.CraftInventoryCreator;
import org.bukkit.craftbukkit.scheduler.CraftScheduler;
import org.bukkit.craftbukkit.tag.CraftBlockTag;
import org.bukkit.craftbukkit.tag.CraftItemTag;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.craftbukkit.util.DatFileFilter;
import org.bukkit.craftbukkit.util.permissions.CraftDefaultPermissions;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.help.HelpMap;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.Recipe;
import org.bukkit.loot.LootTable;
import org.bukkit.map.MapView;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.SimpleServicesManager;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.CachedServerIcon;
import org.bukkit.util.StringUtil;
import org.bukkit.util.permissions.DefaultPermissions;

import com.javazilla.bukkitfabric.BukkitLogger;
import com.javazilla.bukkitfabric.Utils;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinLevelProperties;
import com.javazilla.bukkitfabric.interfaces.IMixinMinecraftServer;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;

import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.PendingServerCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.village.ZombieSiegeManager;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.WanderingTraderManager;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.CatSpawner;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.PhantomSpawner;
import net.minecraft.world.gen.PillagerSpawner;
import net.minecraft.world.gen.Spawner;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;

@SuppressWarnings("deprecation")
public class CraftServer implements Server {

    public final String serverName = "Bukkit4Fabric";
    public final String bukkitVersion = "1.16.2-R0.1-SNAPSHOT";
    public final String serverVersion;

    private final Logger logger = BukkitLogger.getLogger();

    private final CraftCommandMap commandMap;
    private final SimplePluginManager pluginManager;
    private final CraftMagicNumbers unsafe = (CraftMagicNumbers) CraftMagicNumbers.INSTANCE;
    private final ServicesManager servicesManager = new SimpleServicesManager();
    private final CraftScheduler scheduler = new CraftScheduler();
    private final ConsoleCommandSender consoleCommandSender = new CraftConsoleCommandSender();
    private final Map<UUID, OfflinePlayer> offlinePlayers = new MapMaker().weakValues().makeMap();
    public final List<CraftPlayer> playerView;
    private WarningState warningState = WarningState.DEFAULT;
    private final Map<String, World> worlds = new LinkedHashMap<String, World>();
    private final SimpleHelpMap helpMap = new SimpleHelpMap(this);
    private final StandardMessenger messenger = new StandardMessenger();
    private YamlConfiguration configuration;

    public static MinecraftDedicatedServer server;
    public static CraftServer INSTANCE;

    public CraftServer(MinecraftDedicatedServer nms) {
        INSTANCE = this;
        serverVersion = "git-Bukkit4Fabric-" + Utils.getGitHash().substring(0,7); // use short hash
        server = nms;
        commandMap = new CraftCommandMap(this);
        pluginManager = new SimplePluginManager(this, commandMap);

        configuration = YamlConfiguration.loadConfiguration(new File("bukkit.yml"));
        configuration.options().copyDefaults(true);
        configuration.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("configurations/bukkit.yml"), Charsets.UTF_8)));

        System.out.println("SETTING UP PLAYERVIEW");
        this.playerView = new ArrayList<>();
    }

    public void addWorldToMap(CraftWorld world) {
        worlds.put(world.getName(), world);
    }

    public void loadPlugins() {
        pluginManager.registerInterface(JavaPluginLoader.class);

        File pluginFolder = new File("plugins");
        if (pluginFolder.exists()) {
            Plugin[] plugins = pluginManager.loadPlugins(pluginFolder);

            for (Plugin plugin : plugins) {
                try {
                    String message = String.format("Loading %s", plugin.getDescription().getFullName());
                    plugin.getLogger().info(message);
                    plugin.onLoad();
                } catch (Throwable ex) {
                    Bukkit.getLogger().log(Level.SEVERE, ex.getMessage() + " initializing " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
                }
            }
        } else pluginFolder.mkdir();
    }

    public void enablePlugins(PluginLoadOrder type) {
        Plugin[] plugins = pluginManager.getPlugins();

        for (Plugin plugin : plugins)
            if ((!plugin.isEnabled()) && (plugin.getDescription().getLoad() == type))
                enablePlugin(plugin);

        if (type == PluginLoadOrder.POSTWORLD) {
            commandMap.setFallbackCommands();
            setVanillaCommands();
            commandMap.registerServerAliases();
            DefaultPermissions.registerCorePermissions();
            CraftDefaultPermissions.registerCorePermissions();
            // loadCustomPermissions();
            helpMap.initializeCommands();
            syncCommands();
        }
    }

    private void setVanillaCommands() {
        CommandManager dispatcher = server.getCommandManager();

        // Build a list of all Vanilla commands and create wrappers
        for (CommandNode<ServerCommandSource> cmd : dispatcher.getDispatcher().getRoot().getChildren()) {
            if (cmd.getCommand() != null && cmd.getCommand() instanceof BukkitCommandWrapper)
                continue;
            commandMap.register("minecraft", new VanillaCommandWrapper(dispatcher, cmd));
        }
    }

    private void syncCommands() {
        // Clear existing commands
        CommandManager dispatcher = ((IMixinMinecraftServer) server).setCommandManager(new CommandManager(RegistrationEnvironment.ALL));

        // Register all commands, vanilla ones will be using the old dispatcher references
        for (Map.Entry<String, Command> entry : commandMap.getKnownCommands().entrySet()) {
            String label = entry.getKey();
            Command command = entry.getValue();

            if (command instanceof VanillaCommandWrapper) {
                LiteralCommandNode<ServerCommandSource> node = (LiteralCommandNode<ServerCommandSource>) ((VanillaCommandWrapper) command).vanillaCommand;
                if (!node.getLiteral().equals(label)) {
                    LiteralCommandNode<ServerCommandSource> clone = new LiteralCommandNode(label, node.getCommand(), node.getRequirement(), node.getRedirect(), node.getRedirectModifier(), node.isFork());

                    for (CommandNode<ServerCommandSource> child : node.getChildren())
                        clone.addChild(child);
                    node = clone;
                }

                dispatcher.getDispatcher().getRoot().addChild(node);
            } else new BukkitCommandWrapper(this, entry.getValue()).register(dispatcher.getDispatcher(), label);
        }

        // Refresh commands
        for (ServerPlayerEntity player : getHandle().getPlayerManager().getPlayerList())
            dispatcher.sendCommandTree(player);
    }

    private void enablePlugin(Plugin plugin) {
        try {
            List<Permission> perms = plugin.getDescription().getPermissions();

            for (Permission perm : perms) {
                try {
                    pluginManager.addPermission(perm, false);
                } catch (IllegalArgumentException ex) {
                    getLogger().log(Level.WARNING, "Plugin " + plugin.getDescription().getFullName() + " tried to register permission '" + perm.getName() + "' but it's already registered", ex);
                }
            }
            pluginManager.dirtyPermissibles();

            pluginManager.enablePlugin(plugin);
        } catch (Throwable ex) {
            Bukkit.getLogger().log(Level.SEVERE, ex.getMessage() + " loading " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
        }
    }

    public MinecraftDedicatedServer getServer() {
        return server;
    }

    @Override
    public Set<String> getListeningPluginChannels() {
        Set<String> result = new HashSet<String>();
        for (Player player : getOnlinePlayers())
            result.addAll(player.getListeningPluginChannels());

        return result;
    }

    @Override
    public void sendPluginMessage(Plugin source, String channel, byte[] message) {
        for (Player player : getOnlinePlayers())
            player.sendPluginMessage(source, channel, message);
    }

    @Override
    public String toString() {
        return "CraftServer{" + "serverName=" + serverName + ",serverVersion=" + serverVersion + ",minecraftVersion=" + getServer().getVersion() + '}';
    }

    @Override
    public boolean addRecipe(Recipe arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Iterator<Advancement> advancementIterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void banIP(String ip) {
        getServer().getPlayerManager().getIpBanList().add(new BannedIpEntry(ip));
    }

    @Override
    public int broadcast(String message, String permission) {
        Set<CommandSender> recipients = new HashSet<>();
        for (Permissible permissible : getPluginManager().getPermissionSubscriptions(permission))
            if (permissible instanceof CommandSender && permissible.hasPermission(permission))
                recipients.add((CommandSender) permissible);

        BroadcastMessageEvent event = new BroadcastMessageEvent(!Bukkit.isPrimaryThread(), message, recipients);
        getPluginManager().callEvent(event);

        if (event.isCancelled())
            return 0;

        message = event.getMessage();

        for (CommandSender recipient : recipients)
            recipient.sendMessage(message);

        return recipients.size();
    }

    @Override
    public int broadcastMessage(String message) {
        return broadcast(message, BROADCAST_CHANNEL_USERS);
    }

    @Override
    public void clearRecipes() {
        getServer().getRecipeManager().setRecipes(null);
    }

    @Override
    public BlockData createBlockData(Material material) {
        Validate.isTrue(material != null, "Must provide material");
        return createBlockData(material, (String) null);
    }

    @Override
    public BlockData createBlockData(String data) throws IllegalArgumentException {
        return createBlockData(null, data);
    }

    @Override
    public BlockData createBlockData(Material material, Consumer<BlockData> consumer) {
        BlockData data = createBlockData(material);
        if (consumer != null)
            consumer.accept(data);

        return data;
    }

    @Override
    public BlockData createBlockData(Material material, String data) throws IllegalArgumentException {
        Validate.isTrue(material != null || data != null, "Must provide one of material or data");

        return CraftBlockData.newData(material, data);
    }

    @Override
    public BossBar createBossBar(String arg0, BarColor arg1, BarStyle arg2, BarFlag... arg3) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KeyedBossBar createBossBar(NamespacedKey arg0, String arg1, BarColor arg2, BarStyle arg3, BarFlag... arg4) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ChunkData createChunkData(World arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ItemStack createExplorerMap(World arg0, Location arg1, StructureType arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ItemStack createExplorerMap(World arg0, Location arg1, StructureType arg2, int arg3, boolean arg4) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Inventory createInventory(InventoryHolder holder, InventoryType type) {
        return CraftInventoryCreator.INSTANCE.createInventory(holder, type);
    }

    @Override
    public Inventory createInventory(InventoryHolder arg0, int arg1) throws IllegalArgumentException {
        return CraftInventoryCreator.INSTANCE.createInventory(arg0, arg1);
    }

    @Override
    public Inventory createInventory(InventoryHolder arg0, InventoryType arg1, String arg2) {
        return CraftInventoryCreator.INSTANCE.createInventory(arg0, arg1, arg2);
    }

    @Override
    public Inventory createInventory(InventoryHolder arg0, int arg1, String arg2) throws IllegalArgumentException {
        return CraftInventoryCreator.INSTANCE.createInventory(arg0, arg1, arg2);
    }

    @Override
    public MapView createMap(World arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Merchant createMerchant(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public World createWorld(String name, World.Environment environment) {
        return WorldCreator.name(name).environment(environment).createWorld();
    }

    public World createWorld(String name, World.Environment environment, long seed) {
        return WorldCreator.name(name).environment(environment).seed(seed).createWorld();
    }

    public World createWorld(String name, Environment environment, ChunkGenerator generator) {
        return WorldCreator.name(name).environment(environment).generator(generator).createWorld();
    }

    public World createWorld(String name, Environment environment, long seed, ChunkGenerator generator) {
        return WorldCreator.name(name).environment(environment).seed(seed).generator(generator).createWorld();
    }

    @SuppressWarnings({ "resource", "unchecked" })
    @Override
    public World createWorld(WorldCreator creator) {
        String name = creator.name();
        ChunkGenerator generator = creator.generator();
        File folder = new File(getWorldContainer(), name);
        World world = getWorld(name);

        if (world != null)
            return world;

        if ((folder.exists()) && (!folder.isDirectory()))
            throw new IllegalArgumentException("File exists with the name '" + name + "' and isn't a folder");

        if (generator == null)
            generator = getGenerator(name);

        RegistryKey<DimensionOptions> actualDimension;
        switch (creator.environment()) {
            case NORMAL:
                actualDimension = DimensionOptions.OVERWORLD;
                break;
            case NETHER:
                actualDimension = DimensionOptions.NETHER;
                break;
            case THE_END:
                actualDimension = DimensionOptions.END;
                break;
            default:
                throw new IllegalArgumentException("Illegal dimension");
        }

        LevelStorage.Session worldSession;
        try {
            worldSession = LevelStorage.create(getWorldContainer().toPath()).createSession(name);//.c(name, actualDimension);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        MinecraftServer.convertLevel(worldSession); // Run conversion now

        boolean hardcore = creator.hardcore();

        RegistryOps<Tag> registryreadops = RegistryOps.of((DynamicOps) NbtOps.INSTANCE, server.serverResourceManager.getResourceManager(), server.getRegistryManager().create());
        LevelProperties worlddata = (LevelProperties) worldSession.readLevelProperties((DynamicOps) registryreadops, method_29735(server.dataPackManager));

        LevelInfo worldSettings;
        // See MinecraftServer.a(String, String, long, WorldType, JsonElement)
        if (worlddata == null) {
            Properties properties = new Properties();
            properties.put("generator-settings", Objects.toString(creator.generatorSettings()));
            properties.put("level-seed", Objects.toString(creator.seed()));
            properties.put("generate-structures", Objects.toString(creator.generateStructures()));
            properties.put("level-type", Objects.toString(creator.type().getName()));

            GeneratorOptions generatorsettings = GeneratorOptions.fromProperties(server.getRegistryManager(), properties);
            worldSettings = new LevelInfo(name, net.minecraft.world.GameMode.byId(getDefaultGameMode().getValue()), hardcore, Difficulty.NORMAL, false, new GameRules(), method_29735(server.dataPackManager));
            worlddata = new LevelProperties(worldSettings, generatorsettings, Lifecycle.stable());
        }
        ((IMixinLevelProperties)worlddata).checkName(name);
        worlddata.addServerBrand(server.getServerModName(), true);

        // TODO Force upgrade option: server.options.has("forceUpgrade")

        long j = BiomeAccess.hashSeed(creator.seed());
        List<Spawner> list = ImmutableList.of(new PhantomSpawner(), new PillagerSpawner(), new CatSpawner(), new ZombieSiegeManager(), new WanderingTraderManager(worlddata));
        SimpleRegistry<DimensionOptions> registrymaterials = worlddata.getGeneratorOptions().getDimensions();
        DimensionOptions worlddimension = (DimensionOptions) registrymaterials.get(actualDimension);
        DimensionType dimensionmanager;
        net.minecraft.world.gen.chunk.ChunkGenerator chunkgenerator;

        if (worlddimension == null) {
            dimensionmanager = (DimensionType) server.getRegistryManager().getDimensionTypes().getOrThrow(DimensionType.OVERWORLD_REGISTRY_KEY);
            chunkgenerator = GeneratorOptions.createOverworldGenerator(server.getRegistryManager().get(Registry.BIOME_KEY), server.getRegistryManager().get(Registry.NOISE_SETTINGS_WORLDGEN), (new Random()).nextLong());
        } else {
            dimensionmanager = worlddimension.getDimensionType();
            chunkgenerator = worlddimension.getChunkGenerator();
        }

        RegistryKey<net.minecraft.world.World> worldKey = RegistryKey.of(Registry.DIMENSION, new Identifier(name.toLowerCase(java.util.Locale.ENGLISH)));

        ServerWorld internal = (ServerWorld) new ServerWorld(server, server.workerExecutor, worldSession, worlddata, worldKey, dimensionmanager, getServer().worldGenerationProgressListenerFactory.create(11),
                chunkgenerator, worlddata.getGeneratorOptions().isDebugWorld(), j, creator.environment() == Environment.NORMAL ? list : ImmutableList.of(), true/*, creator.environment(), generator*/);

        if (!(worlds.containsKey(name.toLowerCase(java.util.Locale.ENGLISH))))
            return null;

        ((IMixinMinecraftServer)server).initWorld(internal, worlddata, worlddata, worlddata.getGeneratorOptions());

        internal.setMobSpawnOptions(true, true);
        server.worlds.put(internal.getRegistryKey(), internal);

        pluginManager.callEvent(new WorldInitEvent(((IMixinWorld)internal).getCraftWorld()));

        ((IMixinMinecraftServer)getServer()).loadSpawn(internal.getChunkManager().threadedAnvilChunkStorage.worldGenerationProgressListener, internal);

        pluginManager.callEvent(new WorldLoadEvent(((IMixinWorld)internal).getCraftWorld()));
        return ((IMixinWorld)internal).getCraftWorld();
    }

    public static DataPackSettings method_29735(ResourcePackManager resourcePackManager) {
        Collection<String> collection = resourcePackManager.getEnabledNames();
        ImmutableList<String> list = ImmutableList.copyOf(collection);
        List<String> list2 = resourcePackManager.getNames().stream().filter(string -> !collection.contains(string)).collect(ImmutableList.toImmutableList());
        return new DataPackSettings(list, list2);
    }

    public ChunkGenerator getGenerator(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean dispatchCommand(CommandSender sender, String commandLine) throws CommandException {
        if (commandMap.dispatch(sender, commandLine))
            return true;

        sender.sendMessage("Unknown command. Type " + (sender instanceof Player ? "\"/help\" for help." : "\"help\" for help."));
        return false;
    }

    @Override
    public Advancement getAdvancement(NamespacedKey arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean getAllowEnd() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getAllowFlight() {
        return getServer().getProperties().allowFlight;
    }

    @Override
    public boolean getAllowNether() {
        return getServer().getProperties().allowNether;
    }

    @Override
    public int getAmbientSpawnLimit() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getAnimalSpawnLimit() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public BanList getBanList(Type type) {
        switch (type) {
            case IP:
                return new CraftIpBanList();
            case NAME:
                return new CraftProfileBanList();
        }
        return null;
    }

    @Override
    public Set<OfflinePlayer> getBannedPlayers() {
        Set<OfflinePlayer> set = Sets.newHashSet();
        for (String s : getServer().getPlayerManager().getUserBanList().getNames())
            set.add(getOfflinePlayer(s));
        return null;
    }

    @Override
    public KeyedBossBar getBossBar(NamespacedKey arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<KeyedBossBar> getBossBars() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getBukkitVersion() {
        return bukkitVersion;
    }

    @Override
    public Map<String, String[]> getCommandAliases() {
        return new HashMap<String, String[]>();
    }

    @Override
    public long getConnectionThrottle() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ConsoleCommandSender getConsoleSender() {
        return consoleCommandSender;
    }

    @Override
    public GameMode getDefaultGameMode() {
        return Utils.fromFabric(getServer().getDefaultGameMode());
    }

    @Override
    public Entity getEntity(UUID uuid) {
        for (ServerWorld world : getServer().getWorlds()) {
            net.minecraft.entity.Entity entity = world.getEntity(uuid);
            if (entity != null)
                return ((IMixinEntity)entity).getBukkitEntity();
        }

        return null;
    }

    @Override
    public boolean getGenerateStructures() {
        return server.getProperties().generatorOptions.shouldGenerateStructures();
    }

    @Override
    public HelpMap getHelpMap() {
        return helpMap;
    }

    @Override
    public Set<String> getIPBans() {
        Set<String> set = Sets.newHashSet();
        for (String name : getServer().getPlayerManager().getIpBanList().getNames())
            set.add(name);
        return set;
    }

    @Override
    public int getIdleTimeout() {
        return server.getProperties().playerIdleTimeout.get();
    }

    @Override
    public String getIp() {
        return server.getProperties().serverIp;
    }

    @Override
    public ItemFactory getItemFactory() {
        return CraftItemFactory.instance();
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public LootTable getLootTable(NamespacedKey arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MapView getMap(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getMaxPlayers() {
        return getServer().getMaxPlayerCount();
    }

    @Override
    public Messenger getMessenger() {
        return messenger;
    }

    @Override
    public int getMonsterSpawnLimit() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getMotd() {
        return getServer().getMotd();
    }

    @Override
    public String getName() {
        return serverName;
    }

    @Override
    public OfflinePlayer getOfflinePlayer(String name) {
        OfflinePlayer result = getPlayerExact(name);
        if (result == null) {
            GameProfile profile = server.getUserCache().findByName(name);
            if (profile == null) {
                // Make an OfflinePlayer using an offline mode UUID since the name has no profile
                result = getOfflinePlayer(new GameProfile(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8)), name));
            } else result = getOfflinePlayer(profile);// Use the GameProfile even when we get a UUID so we ensure we still have a name
        } else offlinePlayers.remove(result.getUniqueId());

        return result;
    }

    @Override
    public OfflinePlayer getOfflinePlayer(UUID id) {
        System.out.println("OFFLINE UUID: " + id.toString());
        OfflinePlayer result = getPlayer(id);
        if (result == null) {
            result = offlinePlayers.get(id);
            if (result == null) {
                result = new CraftOfflinePlayer(this, new GameProfile(id, null));
                offlinePlayers.put(id, result);
            }
        } else offlinePlayers.remove(id);

        return result;
    }

    public OfflinePlayer getOfflinePlayer(GameProfile profile) {
        OfflinePlayer player = new CraftOfflinePlayer(this, profile);
        offlinePlayers.put(profile.getId(), player);
        return player;
    }

    @Override
    public OfflinePlayer[] getOfflinePlayers() {
        WorldSaveHandler storage = ((IMixinMinecraftServer)server).getSaveHandler_BF();
        String[] files = storage.playerDataDir.list(new DatFileFilter());
        Set<OfflinePlayer> players = new HashSet<OfflinePlayer>();

        for (String file : files) {
            try {
                players.add(getOfflinePlayer(UUID.fromString(file.substring(0, file.length() - 4))));
            } catch (IllegalArgumentException ex) {
                // Who knows what is in this directory, just ignore invalid files
            }
        }

        players.addAll(getOnlinePlayers());

        return players.toArray(new OfflinePlayer[players.size()]);
        //return offlinePlayers.values().toArray(new OfflinePlayer[1]);
    }

    @Override
    public boolean getOnlineMode() {
        return getServer().isOnlineMode();
    }

    @Override
    public Collection<? extends Player> getOnlinePlayers() {
        return this.playerView;
    }

    @Override
    public Set<OfflinePlayer> getOperators() {
        Set<OfflinePlayer> list = Sets.newHashSet();
        for (String op : getServer().getPlayerManager().getOpList().getNames())
            list.add(getOfflinePlayer(op));
        return list;
    }

    @Override
    public Player getPlayer(String name) {
        return getPlayer(getServer().getPlayerManager().getPlayer(name));
    }

    @Override
    public Player getPlayer(UUID uuid) {
        return getPlayer(getServer().getPlayerManager().getPlayer(uuid));
    }

    public Player getPlayer(ServerPlayerEntity e) {
        if (null == e)
            return null;
        return (Player) ((IMixinServerEntityPlayer)(Object)e).getBukkitEntity();
    }

    @Override
    public Player getPlayerExact(String arg0) {
        return getPlayer(arg0);
    }

    @Override
    public PluginCommand getPluginCommand(String name) {
        Command command = commandMap.getCommand(name);
        return command instanceof PluginCommand ? (PluginCommand) command : null;
    }

    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    @Override
    public int getPort() {
        return getServer().getPort();
    }

    @Override
    public List<Recipe> getRecipesFor(ItemStack arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CraftScheduler getScheduler() {
        return scheduler;
    }

    @Override
    public ScoreboardManager getScoreboardManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CachedServerIcon getServerIcon() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServicesManager getServicesManager() {
        return servicesManager;
    }

    @Override
    public String getShutdownMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getSpawnRadius() {
        return getServer().getSpawnProtectionRadius();
    }

    @SuppressWarnings("unchecked")
    public <T extends Keyed> org.bukkit.Tag<T> getTag(String registry, NamespacedKey tag, Class<T> clazz) {
        Identifier key = CraftNamespacedKey.toMinecraft(tag);

        switch (registry) {
            case org.bukkit.Tag.REGISTRY_BLOCKS:
                Preconditions.checkArgument(clazz == org.bukkit.Material.class, "Block namespace must have material type");
                return (org.bukkit.Tag<T>) new CraftBlockTag(server.getTagManager().getBlocks(), key);
            case org.bukkit.Tag.REGISTRY_ITEMS:
                Preconditions.checkArgument(clazz == org.bukkit.Material.class, "Item namespace must have material type");
                return (org.bukkit.Tag<T>) new CraftItemTag(server.getTagManager().getItems(), key);
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Keyed> Iterable<org.bukkit.Tag<T>> getTags(String registry, Class<T> clazz) {
        switch (registry) {
            case org.bukkit.Tag.REGISTRY_BLOCKS:
                Preconditions.checkArgument(clazz == org.bukkit.Material.class, "Block namespace must have material type");
                TagGroup<Block> blockTags = server.getTagManager().getBlocks();
                return blockTags.getTags().keySet().stream().map(key -> (org.bukkit.Tag<T>) new CraftBlockTag(blockTags, key)).collect(ImmutableList.toImmutableList());
            case org.bukkit.Tag.REGISTRY_ITEMS:
                Preconditions.checkArgument(clazz == org.bukkit.Material.class, "Item namespace must have material type");

                TagGroup<Item> itemTags = server.getTagManager().getItems();
                return itemTags.getTags().keySet().stream().map(key -> (org.bukkit.Tag<T>) new CraftItemTag(itemTags, key)).collect(ImmutableList.toImmutableList());
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public int getTicksPerAnimalSpawns() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getTicksPerMonsterSpawns() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public UnsafeValues getUnsafe() {
        return unsafe;
    }

    @Override
    public String getUpdateFolder() {
        return getUpdateFolderFile().getAbsolutePath();
    }

    @Override
    public File getUpdateFolderFile() {
        return new File("update");
    }

    @Override
    public String getVersion() {
        return serverVersion + " (MC: " + getServer().getVersion() + ")";
    }

    @Override
    public int getViewDistance() {
        return server.getProperties().viewDistance;
    }

    @Override
    public WarningState getWarningState() {
        return warningState;
    }

    @Override
    public int getWaterAnimalSpawnLimit() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Set<OfflinePlayer> getWhitelistedPlayers() {
        Set<OfflinePlayer> set = Sets.newHashSet();
        for (String name : getServer().getPlayerManager().getWhitelist().getNames())
            set.add(getOfflinePlayer(name));
        return set;
    }

    @Override
    public World getWorld(String name) {
        return worlds.get(name);
    }

    @Override
    public World getWorld(UUID uuid) {
        for (World world : worlds.values())
            if (world.getUID().equals(uuid))
                return world;
        return null;
    }

    @Override
    public File getWorldContainer() {
        return new File(".");
    }

    @Override
    public String getWorldType() {
        return server.getProperties().levelName; // TODO
    }

    @Override
    public List<World> getWorlds() {
        return new ArrayList<World>(worlds.values());
    }

    @Override
    public boolean hasWhitelist() {
        return server.getProperties().enforceWhitelist;
    }

    @Override
    public boolean isHardcore() {
        return server.getProperties().hardcore;
    }

    @Override
    public boolean isPrimaryThread() {
        return server.isOnThread();
    }

    @Override
    public CachedServerIcon loadServerIcon(File arg0) throws IllegalArgumentException, Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CachedServerIcon loadServerIcon(BufferedImage arg0) throws IllegalArgumentException, Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Player> matchPlayer(String partialName) {
        List<Player> matchedPlayers = new ArrayList<>();

        for (Player iterPlayer : this.getOnlinePlayers()) {
            String iterPlayerName = iterPlayer.getName();

            if (partialName.equalsIgnoreCase(iterPlayerName)) {
                // Exact match
                matchedPlayers.clear();
                matchedPlayers.add(iterPlayer);
                break;
            }
            if (iterPlayerName.toLowerCase(java.util.Locale.ENGLISH).contains(partialName.toLowerCase(java.util.Locale.ENGLISH)))
                matchedPlayers.add(iterPlayer); // Partial match
        }

        return matchedPlayers;
    }

    @Override
    public Iterator<Recipe> recipeIterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void reload() {
     // TODO Auto-generated method stub server.reload();
    }

    @Override
    public void reloadData() {
     // TODO Auto-generated method stub server.reload();
    }

    @Override
    public void reloadWhitelist() {
        server.getPlayerManager().reloadWhitelist();
    }

    @Override
    public boolean removeBossBar(NamespacedKey arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void resetRecipes() {
        // TODO Auto-generated method stubserver.reload();
    }

    @Override
    public void savePlayers() {
        server.getPlayerManager().saveAllPlayerData();
    }

    @Override
    public List<Entity> selectEntities(CommandSender arg0, String arg1) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setDefaultGameMode(GameMode arg0) {
        server.setDefaultGameMode(Utils.toFabric(arg0));
    }

    @Override
    public void setIdleTimeout(int arg0) {
        server.setPlayerIdleTimeout(arg0);
    }

    @Override
    public void setSpawnRadius(int arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setWhitelist(boolean arg0) {
        server.setUseWhitelist(arg0);
    }

    @Override
    public void shutdown() {
        server.shutdown();
    }

    @Override
    public void unbanIP(String arg0) {
        server.getPlayerManager().getIpBanList().remove(arg0);
    }

    @Override
    public boolean unloadWorld(String name, boolean save) {
        return unloadWorld(getWorld(name), save);
    }

    @Override
    public boolean unloadWorld(World world, boolean save) {
        if (world == null)
            return false;

        ServerWorld handle = (ServerWorld) ((CraftWorld) world).getHandle();

        if (!(((IMixinMinecraftServer)(Object)getServer()).getWorldMap().containsKey(handle.toServerWorld().getRegistryKey())))
            return false;

        if (handle.toServerWorld().getRegistryKey() == ServerWorld.OVERWORLD || handle.getPlayers().size() > 0)
            return false;

        WorldUnloadEvent e = new WorldUnloadEvent(world);
        pluginManager.callEvent(e);

        if (e.isCancelled())
            return false;

        try {
            if (save)
                handle.save(null, true, true);
            handle.getChunkManager().close();
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }

        worlds.remove(world.getName().toLowerCase(java.util.Locale.ENGLISH));
        ((IMixinMinecraftServer)(Object)getServer()).getWorldMap().remove(handle.toServerWorld().getRegistryKey());
        return true;
    }

    @Override
    public int getTicksPerAmbientSpawns() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getTicksPerWaterSpawns() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean removeRecipe(NamespacedKey arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public List<String> tabComplete(CommandSender sender, String message, ServerWorld world, Vec3d position, boolean forceCommand) {
        if (!(sender instanceof Player))
            return ImmutableList.of();

        Player player = (Player) sender;
        List<String> offers = (message.startsWith("/") || forceCommand) ? tabCompleteCommand(player, message, world, position) : tabCompleteChat(player, message);

        TabCompleteEvent tabEvent = new TabCompleteEvent(player, message, offers);
        getPluginManager().callEvent(tabEvent);

        return tabEvent.isCancelled() ? Collections.emptyList() : tabEvent.getCompletions();
    }

    public List<String> tabCompleteCommand(Player player, String message, ServerWorld world, Vec3d pos) {
        List<String> completions = null;
        try {
            if (message.startsWith("/"))
                message = message.substring(1);

            completions = (pos == null) ? getCommandMap().tabComplete(player, message) :
                    getCommandMap().tabComplete(player, message, new Location(((IMixinWorld)(Object)world).getCraftWorld(), pos.x, pos.y, pos.z));
        } catch (CommandException ex) {
            player.sendMessage(ChatColor.RED + "An internal error occurred while attempting to tab-complete this command");
            getLogger().log(Level.SEVERE, "Exception when " + player.getName() + " attempted to tab complete " + message, ex);
        }

        return completions == null ? ImmutableList.<String>of() : completions;
    }

    @SuppressWarnings("deprecation")
    public List<String> tabCompleteChat(Player player, String message) {
        List<String> completions = new ArrayList<String>();
        PlayerChatTabCompleteEvent event = new PlayerChatTabCompleteEvent(player, message, completions);
        String token = event.getLastToken();
        for (Player p : getOnlinePlayers())
            if (player.canSee(p) && StringUtil.startsWithIgnoreCase(p.getName(), token))
                completions.add(p.getName());

        pluginManager.callEvent(event);

        Iterator<?> it = completions.iterator();
        while (it.hasNext()) {
            Object current = it.next();
            if (!(current instanceof String))
                it.remove();
        }
        Collections.sort(completions, String.CASE_INSENSITIVE_ORDER);
        return completions;
    }

    public MinecraftDedicatedServer getHandle() {
        return getServer();
    }

    public CraftCommandMap getCommandMap() {
        return commandMap;
    }

    // Because PlayerManager is broken
    public List<String> getOperatorList() {
        File f = new File(MinecraftServer.USER_CACHE_FILE.getParentFile(), "ops.json");
        getLogger().info(f.getAbsolutePath() + ", EXISTS? " + f.exists());
        List<String> content = null;
        try {
            content = Files.readAllLines(f.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> toreturn = new ArrayList<>();
        for (String s : content) {
            s = s.trim();
            if (s.startsWith("\"uuid\":")) {
                s = s.substring(s.indexOf(":")+1).replace('"', ' ').trim();
                toreturn.add(s);
            }
        }

        return toreturn;
    }

    public int getWaterAmbientSpawnLimit() {
        return 0; // TODO
    }

    private final Spigot spigot = new Server.Spigot(){

        @Override
        public YamlConfiguration getConfig() {
            return null; // TODO
        }

        @Override
        public void restart() {
            // TODO
        }

        @Override
        public void broadcast(BaseComponent component) {
            for (Player player : getOnlinePlayers())
                player.spigot().sendMessage(component);
        }

        @Override
        public void broadcast(BaseComponent... components) {
            for (Player player : getOnlinePlayers())
                player.spigot().sendMessage(components);
        }
    };
    public boolean playerCommandState;

    @Override
    public Spigot spigot() {
        return spigot;
    }

    @Override
    public int getTicksPerWaterAmbientSpawns() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Recipe getRecipe(NamespacedKey arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean dispatchServerCommand(CommandSender sender, PendingServerCommand serverCommand) {
        if (sender instanceof Conversable) {
            Conversable conversable = (Conversable) sender;

            if (conversable.isConversing()) {
                conversable.acceptConversationInput(serverCommand.command);
                return true;
            }
        }
        try {
            this.playerCommandState = true;
            return dispatchCommand(sender, serverCommand.command);
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Unexpected exception while parsing console command \"" + serverCommand.command + '"', ex);
            return false;
        } finally {
            this.playerCommandState = false;
        }
    }

}