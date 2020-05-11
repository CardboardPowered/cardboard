package org.bukkit.craftbukkit;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.BanList.Type;
import org.bukkit.GameMode;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.StructureType;
import org.bukkit.Tag;
import org.bukkit.UnsafeValues;
import org.bukkit.Warning.WarningState;
import org.bukkit.World;
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
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.MultipleCommandAlias;
import org.bukkit.command.PluginCommand;
import org.bukkit.craftbukkit.command.BukkitCommandWrapper;
import org.bukkit.craftbukkit.command.CraftCommandMap;
import org.bukkit.craftbukkit.command.CraftConsoleCommandSender;
import org.bukkit.craftbukkit.command.VanillaCommandWrapper;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.help.SimpleHelpMap;
import org.bukkit.craftbukkit.scheduler.CraftScheduler;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.permissions.CraftDefaultPermissions;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.bukkit.event.world.WorldUnloadEvent;
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
import org.bukkit.plugin.java.FakePluginLoader;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.CachedServerIcon;
import org.bukkit.util.permissions.DefaultPermissions;

import com.fungus_soft.bukkitfabric.FakeLogger;
import com.fungus_soft.bukkitfabric.Utils;
import com.fungus_soft.bukkitfabric.interfaces.IMixinBukkitGetter;
import com.fungus_soft.bukkitfabric.interfaces.IMixinEntity;
import com.fungus_soft.bukkitfabric.interfaces.IMixinMinecraftServer;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;

import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;

public class CraftServer implements Server {

    public final String serverName = "Bukkit4Fabric";
    public final String bukkitVersion = "1.15.2-R0.1";
    public final String serverVersion;

    private final Logger logger = FakeLogger.getLogger();

    private final CraftCommandMap commandMap;
    private final SimplePluginManager pluginManager;
    private final CraftMagicNumbers unsafe = (CraftMagicNumbers) CraftMagicNumbers.INSTANCE;
    private final ServicesManager servicesManager = new SimpleServicesManager();
    private final CraftScheduler scheduler = new CraftScheduler();
    private final ConsoleCommandSender consoleCommandSender = new CraftConsoleCommandSender();
    private final Map<UUID, OfflinePlayer> offlinePlayers = new MapMaker().weakValues().makeMap();
    private final List<CraftPlayer> playerView;
    private WarningState warningState = WarningState.DEFAULT;
    private final Map<String, World> worlds = new LinkedHashMap<String, World>();
    private final SimpleHelpMap helpMap = new SimpleHelpMap(this);
    private final StandardMessenger messenger = new StandardMessenger();

    public static MinecraftDedicatedServer server;

    public CraftServer(MinecraftDedicatedServer nms) {
        serverVersion = "git-Bukkit4Fabric-" + Utils.getGitHash();
        server = nms;
        commandMap = new CraftCommandMap(this);
        pluginManager = new SimplePluginManager(this, commandMap);

        this.playerView = Collections.unmodifiableList(Lists.transform(nms.getPlayerManager().getPlayerList(), new Function<ServerPlayerEntity, CraftPlayer>() {
            @Override
            public CraftPlayer apply(ServerPlayerEntity player) {
                return (CraftPlayer) ((IMixinBukkitGetter)player).getBukkitObject();
            }
        }));
    }

    public void addWorldToMap(ServerWorld w) {
        worlds.put(w.getLevelProperties().getLevelName(), new CraftWorld(w));
    }

    public void loadPlugins() {
        pluginManager.registerInterface(FakePluginLoader.class);

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
        CommandDispatcher dispatcher = server.getCommandManager().getDispatcher();

        // Build a list of all Vanilla commands and create wrappers
        for (CommandNode cmd : (Collection<CommandNode>)dispatcher.getRoot().getChildren()) {
            if (cmd.getCommand() != null && cmd.getCommand() instanceof BukkitCommandWrapper)
                continue;
            commandMap.register("minecraft", new VanillaCommandWrapper(dispatcher, cmd));
        }
    }

    private void syncCommands() {
        // TODO
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
    public void banIP(String arg0) {
        getServer().getPlayerManager().getIpBanList().add(new BannedIpEntry(arg0));
    }

    @Override
    public int broadcast(String message, String permission) {
        Set<CommandSender> recipients = new HashSet<>();
        for (Permissible permissible : getPluginManager().getPermissionSubscriptions(permission))
            if (permissible instanceof CommandSender && permissible.hasPermission(permission))
                recipients.add((CommandSender) permissible);

        BroadcastMessageEvent broadcastMessageEvent = new BroadcastMessageEvent(!Bukkit.isPrimaryThread(), message, recipients);
        getPluginManager().callEvent(broadcastMessageEvent);

        if (broadcastMessageEvent.isCancelled())
            return 0;

        message = broadcastMessageEvent.getMessage();

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
        // TODO Auto-generated method stub
        return null;
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
    public Inventory createInventory(InventoryHolder arg0, InventoryType arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Inventory createInventory(InventoryHolder arg0, int arg1) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Inventory createInventory(InventoryHolder arg0, InventoryType arg1, String arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Inventory createInventory(InventoryHolder arg0, int arg1, String arg2) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
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

    @Override
    public World createWorld(WorldCreator creator) {
        // TODO
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
        return getServer().shouldGenerateStructures();
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
        return getServer().getProperties().playerIdleTimeout.get();
    }

    @Override
    public String getIp() {
        return getServer().getProperties().serverIp;
    }

    @Override
    public ItemFactory getItemFactory() {
        // TODO Auto-generated method stub
        return null;
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
            // This is potentially blocking :(
            GameProfile profile = getServer().getUserCache().findByName(name);
            if (profile == null) {
                // Make an OfflinePlayer using an offline mode UUID since the name has no profile
                result = getOfflinePlayer(new GameProfile(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8)), name));
            } else {
                // Use the GameProfile even when we get a UUID so we ensure we still have a name
                result = getOfflinePlayer(profile);
            }
        } else offlinePlayers.remove(result.getUniqueId());

        return result;
    }

    @Override
    public OfflinePlayer getOfflinePlayer(UUID id) {
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
        // TODO Check performance
        return offlinePlayers.values().toArray(new OfflinePlayer[1]);
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
        CraftPlayer plr = new CraftPlayer(getServer().getPlayerManager().getPlayer(name));
        return plr;
    }

    @Override
    public Player getPlayer(UUID arg0) {
        CraftPlayer plr = new CraftPlayer(getServer().getPlayerManager().getPlayer(arg0));
        return plr;
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
    public BukkitScheduler getScheduler() {
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

    @Override
    public <T extends Keyed> Tag<T> getTag(String arg0, NamespacedKey arg1, Class<T> arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends Keyed> Iterable<Tag<T>> getTags(String arg0, Class<T> arg1) {
        // TODO Auto-generated method stub
        return null;
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
        return serverVersion;
    }

    @Override
    public int getViewDistance() {
        return getServer().getProperties().viewDistance;
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
        return getServer().getProperties().levelType.getName();
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
        return getServer().isOnThread();
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
        getServer().reload();
    }

    @Override
    public void reloadData() {
        getServer().reload();
    }

    @Override
    public void reloadWhitelist() {
        getServer().getPlayerManager().reloadWhitelist();
    }

    @Override
    public boolean removeBossBar(NamespacedKey arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void resetRecipes() {
        getServer().reload();
    }

    @Override
    public void savePlayers() {
        getServer().getPlayerManager().saveAllPlayerData();
    }

    @Override
    public List<Entity> selectEntities(CommandSender arg0, String arg1) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setDefaultGameMode(GameMode arg0) {
        getServer().setDefaultGameMode(Utils.toFabric(arg0));
    }

    @Override
    public void setIdleTimeout(int arg0) {
        getServer().setPlayerIdleTimeout(arg0);
    }

    @Override
    public void setSpawnRadius(int arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setWhitelist(boolean arg0) {
        getServer().setUseWhitelist(arg0);
    }

    @Override
    public void shutdown() {
        getServer().shutdown();
    }

    @Override
    public void unbanIP(String arg0) {
        getServer().getPlayerManager().getIpBanList().remove(arg0);
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

        if (!(((IMixinMinecraftServer)getServer()).getWorldMap().containsKey(handle.getWorld().getDimension().getType())))
            return false;

        if (handle.getWorld().getDimension().getType() == DimensionType.OVERWORLD)
            return false;

        if (handle.getPlayers().size() > 0)
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
        ((IMixinMinecraftServer)getServer()).getWorldMap().remove(handle.getWorld().getDimension().getType());
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

    public List<String> tabComplete(CommandSender bukkitSender, String input, ServerWorld world, Vec3d position, boolean b) {
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    public MinecraftDedicatedServer getHandle() {
        return getServer();
    }

    public CraftCommandMap getCommandMap() {
        return commandMap;
    }

}
