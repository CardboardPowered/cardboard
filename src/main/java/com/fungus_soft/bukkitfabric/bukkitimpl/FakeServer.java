package com.fungus_soft.bukkitfabric.bukkitimpl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
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
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.FakePluginLoader;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.CachedServerIcon;
import org.bukkit.util.permissions.DefaultPermissions;

import com.fungus_soft.bukkitfabric.bukkitimpl.command.VanillaCommandWrapper;
import com.fungus_soft.bukkitfabric.bukkitimpl.plugin.FakePluginManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;

import net.minecraft.server.dedicated.MinecraftDedicatedServer;

public class FakeServer implements Server {

    public final String NAME = "FabricBukkit";
    public final String BUKKIT_VERSION = "1.15.2-R0.1";
    public final String VERSION = "1.15.2";

    private final Logger logger = FakeLogger.getLogger();

    private final SimpleCommandMap commandMap = new SimpleCommandMap(this);
    private final FakePluginManager pluginManager = new FakePluginManager(this, commandMap);
    private final UnsafeValues unsafe = new FakeUnsafe();

    public MinecraftDedicatedServer server;

    public FakeServer() {
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
            //CraftDefaultPermissions.registerCorePermissions();
            //loadCustomPermissions();
            syncCommands();
        }
    }

    private void setVanillaCommands() {
        CommandDispatcher dispatcher = server.getCommandManager().getDispatcher();

        // Build a list of all Vanilla commands and create wrappers
        for (CommandNode cmd : (Collection<CommandNode>)dispatcher.getRoot().getChildren()) {
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void sendPluginMessage(Plugin arg0, String arg1, byte[] arg2) {
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
    }

    @Override
    public int broadcast(String arg0, String arg1) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int broadcastMessage(String arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void clearRecipes() {
        // TODO Auto-generated method stub
    }

    @Override
    public BlockData createBlockData(Material arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BlockData createBlockData(String arg0) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BlockData createBlockData(Material arg0, Consumer<BlockData> arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BlockData createBlockData(Material arg0, String arg1)
            throws IllegalArgumentException {
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
    public World createWorld(WorldCreator arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean dispatchCommand(CommandSender arg0, String arg1) throws CommandException {
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getAllowNether() {
        // TODO Auto-generated method stub
        return false;
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
    public BanList getBanList(Type arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<OfflinePlayer> getBannedPlayers() {
        // TODO Auto-generated method stub
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
        return BUKKIT_VERSION;
    }

    @Override
    public Map<String, String[]> getCommandAliases() {
        // TODO Auto-generated method stub
        return new HashMap<String, String[]>();
    }

    @Override
    public long getConnectionThrottle() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ConsoleCommandSender getConsoleSender() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GameMode getDefaultGameMode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Entity getEntity(UUID arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean getGenerateStructures() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public HelpMap getHelpMap() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> getIPBans() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getIdleTimeout() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getIp() {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Messenger getMessenger() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getMonsterSpawnLimit() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getMotd() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public OfflinePlayer getOfflinePlayer(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OfflinePlayer getOfflinePlayer(UUID arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OfflinePlayer[] getOfflinePlayers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean getOnlineMode() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Collection<? extends Player> getOnlinePlayers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<OfflinePlayer> getOperators() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Player getPlayer(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Player getPlayer(UUID arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Player getPlayerExact(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PluginCommand getPluginCommand(String arg0) {
        Command command = commandMap.getCommand("name");
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
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return new File("update");
    }

    @Override
    public String getVersion() {
        // TODO Auto-generated method stub
        return VERSION;
    }

    @Override
    public int getViewDistance() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public WarningState getWarningState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getWaterAnimalSpawnLimit() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Set<OfflinePlayer> getWhitelistedPlayers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public World getWorld(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public World getWorld(UUID arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public File getWorldContainer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getWorldType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<World> getWorlds() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasWhitelist() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isHardcore() {
        // TODO Auto-generated method stub
        return false;
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
    public List<Player> matchPlayer(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<Recipe> recipeIterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void reload() {
        // TODO Auto-generated method stub
    }

    @Override
    public void reloadData() {
        // TODO Auto-generated method stub
    }

    @Override
    public void reloadWhitelist() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean removeBossBar(NamespacedKey arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void resetRecipes() {
        // TODO Auto-generated method stub
    }

    @Override
    public void savePlayers() {
        // TODO Auto-generated method stub
    }

    @Override
    public List<Entity> selectEntities(CommandSender arg0, String arg1) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setDefaultGameMode(GameMode arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setIdleTimeout(int arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setSpawnRadius(int arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setWhitelist(boolean arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void shutdown() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void unbanIP(String arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean unloadWorld(String arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean unloadWorld(World arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return false;
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

}
