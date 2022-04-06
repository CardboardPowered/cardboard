/**
 * CardboardPowered - Bukkit/Spigot for Fabric
 * Copyright (C) CardboardPowered.org and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.bukkit.craftbukkit;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import org.apache.commons.lang.Validate;
import org.bukkit.BanList;
import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.Conversable;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.scoreboard.CardboardScoreboardManager;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.help.HelpMap;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.ComplexRecipe;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.inventory.SmokingRecipe;
import org.bukkit.inventory.StonecuttingRecipe;
import org.bukkit.loot.LootTable;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataStoreBase;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.SimpleServicesManager;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.bukkit.scheduler.BukkitWorker;
import org.bukkit.structure.StructureManager;
import org.bukkit.util.StringUtil;
import org.bukkit.util.permissions.DefaultPermissions;
import org.cardboardpowered.impl.CardboardBossBar;
import org.cardboardpowered.impl.IpBanList;
import org.cardboardpowered.impl.ProfileBanList;
import org.cardboardpowered.impl.command.BukkitCommandWrapper;
import org.cardboardpowered.impl.command.CommandMapImpl;
import org.cardboardpowered.impl.command.CardboardConsoleCommandSender;
import org.cardboardpowered.impl.command.MinecraftCommandWrapper;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.cardboardpowered.impl.inventory.recipe.CardboardBlastingRecipe;
import org.cardboardpowered.impl.inventory.recipe.CardboardCampfireRecipe;
import org.cardboardpowered.impl.inventory.recipe.CardboardFurnaceRecipe;
import org.cardboardpowered.impl.inventory.recipe.CardboardShapedRecipe;
import org.cardboardpowered.impl.inventory.recipe.CardboardShapelessRecipe;
import org.cardboardpowered.impl.inventory.recipe.CardboardSmithingRecipe;
import org.cardboardpowered.impl.inventory.recipe.CardboardSmokingRecipe;
import org.cardboardpowered.impl.inventory.recipe.CardboardStonecuttingRecipe;
import org.cardboardpowered.impl.inventory.recipe.RecipeInterface;
import org.cardboardpowered.impl.inventory.recipe.RecipeIterator;
import org.cardboardpowered.impl.inventory.InventoryCreator;
import org.cardboardpowered.impl.map.MapViewImpl;
import org.cardboardpowered.impl.tag.BlockTagImpl;
import org.cardboardpowered.impl.tag.EntityTagImpl;
import org.cardboardpowered.impl.tag.ItemTagImpl;
import org.cardboardpowered.impl.tag.Tags;
import org.cardboardpowered.impl.util.CommandPermissions;
import org.cardboardpowered.impl.util.IconCacheImpl;
import org.cardboardpowered.impl.util.SimpleHelpMap;
import org.cardboardpowered.impl.world.ChunkDataImpl;
import org.cardboardpowered.impl.world.WorldImpl;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spigotmc.SpigotConfig;

import com.destroystokyo.paper.entity.ai.MobGoals;
import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import com.javazilla.bukkitfabric.BukkitLogger;
import com.javazilla.bukkitfabric.PaperMetrics;
import com.javazilla.bukkitfabric.Utils;
import com.javazilla.bukkitfabric.impl.MetaDataStoreBase;
import com.javazilla.bukkitfabric.impl.MetadataStoreImpl;
import com.javazilla.bukkitfabric.impl.scheduler.BukkitSchedulerImpl;
import com.javazilla.bukkitfabric.interfaces.IMixinAdvancement;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinLevelProperties;
import com.javazilla.bukkitfabric.interfaces.IMixinMapState;
import com.javazilla.bukkitfabric.interfaces.IMixinMinecraftServer;
import com.javazilla.bukkitfabric.interfaces.IMixinRecipe;
import com.javazilla.bukkitfabric.interfaces.IMixinRecipeManager;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;
import com.javazilla.bukkitfabric.interfaces.IUserCache;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.papermc.paper.datapack.DatapackManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.block.Block;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.PendingServerCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.TagGroup;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.DynamicRegistryManager;
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

    public final String serverName = "Cardboard";
    public final String bukkitVersion = "1.18.1-R0.1-SNAPSHOT";
    public final String serverVersion;
    public final String shortVersion;

    private final Logger logger = BukkitLogger.getLogger();

    private final CommandMapImpl commandMap;
    private final SimplePluginManager pluginManager;
    private final CraftMagicNumbers unsafe = (CraftMagicNumbers) CraftMagicNumbers.INSTANCE;
    private final ServicesManager servicesManager = new SimpleServicesManager();
    private final BukkitSchedulerImpl scheduler = new BukkitSchedulerImpl();
    private final ConsoleCommandSender consoleCommandSender = new CardboardConsoleCommandSender();
    private final Map<UUID, OfflinePlayer> offlinePlayers = new MapMaker().weakValues().makeMap();
    public final List<PlayerImpl> playerView;
    private WarningState warningState = WarningState.DEFAULT;
    public final Map<String, World> worlds = new LinkedHashMap<String, World>();
    private final SimpleHelpMap helpMap = new SimpleHelpMap(this);
    private final StandardMessenger messenger = new StandardMessenger();
    private YamlConfiguration configuration;
    private IconCacheImpl icon;

    public static MinecraftDedicatedServer server;
    public static MinecraftDedicatedServer console;
    public static CraftServer INSTANCE;
    public CardboardScoreboardManager scoreboardManager;

    private final MetadataStoreBase<Entity> entityMetadata = MetadataStoreImpl.newEntityMetadataStore();
    private final MetaDataStoreBase<OfflinePlayer> playerMetadata = MetadataStoreImpl.newPlayerMetadataStore();
    private final MetaDataStoreBase<World> worldMetadata = MetadataStoreImpl.newWorldMetadataStore();

    public CraftServer(MinecraftDedicatedServer nms) {
        INSTANCE = this;
        serverVersion = "git-Cardboard-" + Utils.getGitHash().substring(0,7); // use short hash
        shortVersion = "git-" + Utils.getGitHash().substring(0,7);
        server = nms;
        console = nms;
        commandMap = new CommandMapImpl(this);
        pluginManager = new SimplePluginManager(this, commandMap);
        scoreboardManager = new CardboardScoreboardManager(nms, server.getScoreboard());

        configuration = YamlConfiguration.loadConfiguration(new File("bukkit.yml"));
        configuration.options().copyDefaults(true);
        configuration.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("configurations/bukkit.yml"), Charsets.UTF_8)));
        saveConfig();

        this.playerView = new ArrayList<>();
        loadIcon();
    }
 
    public static IUserCache getUC() {
        return (IUserCache) server.getUserCache();
    }

    public void saveConfig() {
        try {
            configuration.save(getConfigFile());
        } catch (IOException ex) {
            Logger.getLogger(CraftServer.class.getName()).log(Level.SEVERE, "Could not save " + getConfigFile(), ex);
        }
    }

    public File getConfigFile() {
        return new File("bukkit.yml");
    }

    private void loadIcon() {
        icon = new IconCacheImpl(null);
        try {
            final File file = new File(new File("."), "server-icon.png");
            if (file.isFile())
                icon = loadServerIcon0(file);
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Couldn't load server icon", ex);
        }
    }

    @Override
    public IconCacheImpl loadServerIcon(File file) throws Exception {
        Validate.notNull(file, "File cannot be null");
        if (!file.isFile())
            throw new IllegalArgumentException(file + " is not a file");
        return loadServerIcon0(file);
    }

    static IconCacheImpl loadServerIcon0(File file) throws Exception {
        return loadServerIcon0(ImageIO.read(file));
    }

    @Override
    public IconCacheImpl loadServerIcon(BufferedImage image) throws Exception {
        Validate.notNull(image, "Image cannot be null");
        return loadServerIcon0(image);
    }

    static IconCacheImpl loadServerIcon0(BufferedImage image) throws Exception {
        ByteBuf bytebuf = Unpooled.buffer();

        Validate.isTrue(image.getWidth() == 64, "Error: not 64*64");
        Validate.isTrue(image.getHeight() == 64, "Error: not 64*64");
        ImageIO.write(image, "PNG", new ByteBufOutputStream(bytebuf));
        ByteBuffer bytebuffer = Base64.getEncoder().encode(bytebuf.nioBuffer());

        return new IconCacheImpl("data:image/png;base64," + StandardCharsets.UTF_8.decode(bytebuffer));
    }

    public void addWorldToMap(WorldImpl world) {
        worlds.put(world.getName(), world);
    }

    public void loadPlugins() {
        File pluginFolder = new File("plugins");
        if (pluginFolder.exists()) {
            for (File f : pluginFolder.listFiles()) {
                if (f.getName().endsWith(".jar")) {
                    try {
                        com.javazilla.bukkitfabric.nms.Remapper.remap(f); // Cardboard: Remap Jar file
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
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
            CommandPermissions.registerCorePermissions();
            // loadCustomPermissions();
            helpMap.initializeCommands();
            syncCommands();
        }
    }

    public CommandManager vanillaCommandManager;

    private void setVanillaCommands() {
        CommandManager dispatcher = (this.vanillaCommandManager = server.getCommandManager());

        // Build a list of all Vanilla commands and create wrappers
        for (CommandNode<ServerCommandSource> cmd : dispatcher.getDispatcher().getRoot().getChildren()) {
            if (cmd.getCommand() != null && cmd.getCommand() instanceof BukkitCommandWrapper)
                continue;
            commandMap.register("minecraft", new MinecraftCommandWrapper(dispatcher, cmd));
        }
    }

    @SuppressWarnings("unchecked")
    private void syncCommands() {
        // Clear existing commands
        CommandManager dispatcher = ((IMixinMinecraftServer) server).setCommandManager(new CommandManager(RegistrationEnvironment.ALL));

        // Register all commands, vanilla ones will be using the old dispatcher references
        for (Map.Entry<String, Command> entry : commandMap.getKnownCommands().entrySet()) {
            String label = entry.getKey();
            Command command = entry.getValue();

            if (command instanceof MinecraftCommandWrapper) {
                LiteralCommandNode<ServerCommandSource> node = (LiteralCommandNode<ServerCommandSource>) ((MinecraftCommandWrapper) command).vanillaCommand;
                if (!node.getLiteral().equals(label)) {
                    LiteralCommandNode<ServerCommandSource> clone = new LiteralCommandNode<ServerCommandSource>(label, node.getCommand(), node.getRequirement(), node.getRedirect(), node.getRedirectModifier(), node.isFork());

                    for (CommandNode<ServerCommandSource> child : node.getChildren())
                        clone.addChild(child);
                    node = clone;
                }

                dispatcher.getDispatcher().getRoot().addChild(node);
            } else new BukkitCommandWrapper(entry.getValue()).register(dispatcher.getDispatcher(), label);
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
    public boolean addRecipe(Recipe recipe) {
        RecipeInterface toAdd;
        if (recipe instanceof RecipeInterface) {
            toAdd = (RecipeInterface) recipe;
        } else {
            if (recipe instanceof ShapedRecipe) {
                toAdd = CardboardShapedRecipe.fromBukkitRecipe((ShapedRecipe) recipe);
            } else if (recipe instanceof ShapelessRecipe) {
                toAdd = CardboardShapelessRecipe.fromBukkitRecipe((ShapelessRecipe) recipe);
            } else if (recipe instanceof FurnaceRecipe) {
                toAdd = CardboardFurnaceRecipe.fromBukkitRecipe((FurnaceRecipe) recipe);
            } else if (recipe instanceof BlastingRecipe) {
                toAdd = CardboardBlastingRecipe.fromBukkitRecipe((BlastingRecipe) recipe);
            } else if (recipe instanceof CampfireRecipe) {
                toAdd = CardboardCampfireRecipe.fromBukkitRecipe((CampfireRecipe) recipe);
            } else if (recipe instanceof SmokingRecipe) {
                toAdd = CardboardSmokingRecipe.fromBukkitRecipe((SmokingRecipe) recipe);
            } else if (recipe instanceof StonecuttingRecipe) {
                toAdd = CardboardStonecuttingRecipe.fromBukkitRecipe((StonecuttingRecipe) recipe);
            } else if (recipe instanceof SmithingRecipe) {
                toAdd = CardboardSmithingRecipe.fromBukkitRecipe((SmithingRecipe) recipe);
            } else if (recipe instanceof ComplexRecipe) {
                throw new UnsupportedOperationException("Cannot add custom complex recipe");
            } else return false;
        }
        toAdd.addToCraftingManager();
        return true;
    }

    @Override
    public Iterator<Advancement> advancementIterator() {
        return Iterators.unmodifiableIterator(Iterators.transform(server.getAdvancementLoader().getAdvancements().iterator(), new Function<net.minecraft.advancement.Advancement, org.bukkit.advancement.Advancement>() {
            @Override
            public org.bukkit.advancement.Advancement apply(net.minecraft.advancement.Advancement advancement) {
                return ((IMixinAdvancement)advancement).getBukkitAdvancement();
            }
        }));
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
        ((IMixinRecipeManager)getServer().getRecipeManager()).clearRecipes();
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
        if (consumer != null) consumer.accept(data);
        return data;
    }

    @Override
    public BlockData createBlockData(Material material, String data) throws IllegalArgumentException {
        Validate.isTrue(material != null || data != null, "Must provide one of material or data");
        return CraftBlockData.newData(material, data);
    }

    @Override
    public BossBar createBossBar(String title, BarColor color, BarStyle style, BarFlag... flags) {
        return new CardboardBossBar(title, color, style, flags);
    }

    @Override
    public KeyedBossBar createBossBar(NamespacedKey key, String title, BarColor barColor, BarStyle barStyle, BarFlag... barFlags) {
        Preconditions.checkArgument(key != null, "key");

        CommandBossBar bossBattleCustom = getServer().getBossBarManager().add(CraftNamespacedKey.toMinecraft(key), CraftChatMessage.fromString(title, true)[0]);
        CardboardBossBar craftKeyedBossbar = new CardboardBossBar(bossBattleCustom);
        craftKeyedBossbar.setColor(barColor);
        craftKeyedBossbar.setStyle(barStyle);
        for (BarFlag flag : barFlags)
            craftKeyedBossbar.addFlag(flag);

        return craftKeyedBossbar;
    }

    @Override
    public Iterator<KeyedBossBar> getBossBars() {
        return Iterators.unmodifiableIterator(Iterators.transform(getServer().getBossBarManager().getAll().iterator(), new Function<CommandBossBar, org.bukkit.boss.KeyedBossBar>() {
            @Override
            public org.bukkit.boss.KeyedBossBar apply(CommandBossBar bossBattleCustom) {
                return (KeyedBossBar) ((IMixinEntity)bossBattleCustom).getBukkitEntity();
            }
        }));
    }

    @Override
    public KeyedBossBar getBossBar(NamespacedKey key) {
        Preconditions.checkArgument(key != null, "key");
        net.minecraft.entity.boss.CommandBossBar bossBattleCustom = getServer().getBossBarManager().get(CraftNamespacedKey.toMinecraft(key));
        return (bossBattleCustom == null) ? null : (KeyedBossBar) ((IMixinEntity)bossBattleCustom).getBukkitEntity();
    }

    @Override
    public boolean removeBossBar(NamespacedKey key) {
        Preconditions.checkArgument(key != null, "key");
        net.minecraft.entity.boss.BossBarManager bossBattleCustomData = getServer().getBossBarManager();
        net.minecraft.entity.boss.CommandBossBar bossBattleCustom = bossBattleCustomData.get(CraftNamespacedKey.toMinecraft(key));

        if (bossBattleCustom != null) {
            bossBattleCustomData.remove(bossBattleCustom);
            return true;
        }
        return false;
    }

    @Override
    public ChunkData createChunkData(World arg0) {
        return new ChunkDataImpl(arg0);
    }

    @Override
    public ItemStack createExplorerMap(World world, Location location, StructureType structureType) {
        return this.createExplorerMap(world, location, structureType, 100, true);
    }

    @SuppressWarnings("static-access")
    @Override
    public ItemStack createExplorerMap(World world, Location location, StructureType structureType, int radius, boolean findUnexplored) {
        Validate.notNull(world, "World cannot be null");
        Validate.notNull(structureType, "StructureType cannot be null");
        Validate.notNull(structureType.getMapIcon(), "Cannot create explorer maps for StructureType " + structureType.getName());

        ServerWorld worldServer = ((WorldImpl) world).getHandle();
        Location structureLocation = world.locateNearestStructure(location, structureType, radius, findUnexplored);
        BlockPos structurePosition = new BlockPos(structureLocation.getBlockX(), structureLocation.getBlockY(), structureLocation.getBlockZ());

        // Create map with trackPlayer = true, unlimitedTracking = true
        net.minecraft.item.ItemStack stack = FilledMapItem.createMap(worldServer, structurePosition.getX(), structurePosition.getZ(), MapView.Scale.NORMAL.getValue(), true, true);
        FilledMapItem.fillExplorationMap(worldServer, stack);
        // "+" map ID taken from EntityVillager

        FilledMapItem.getOrCreateMapState(stack, worldServer).addDecorationsNbt(stack, structurePosition, "+", net.minecraft.item.map.MapIcon.Type.byId(structureType.getMapIcon().getValue()));

        return CraftItemStack.asBukkitCopy(stack);
    }

    @Override
    public Inventory createInventory(InventoryHolder holder, InventoryType type) {
        return InventoryCreator.INSTANCE.createInventory(holder, type);
    }

    @Override
    public Inventory createInventory(InventoryHolder arg0, int arg1) throws IllegalArgumentException {
        return InventoryCreator.INSTANCE.createInventory(arg0, arg1);
    }

    @Override
    public Inventory createInventory(InventoryHolder arg0, InventoryType arg1, String arg2) {
        return InventoryCreator.INSTANCE.createInventory(arg0, arg1, arg2);
    }

    @Override
    public Inventory createInventory(InventoryHolder arg0, int arg1, String arg2) throws IllegalArgumentException {
        return InventoryCreator.INSTANCE.createInventory(arg0, arg1, arg2);
    }

    @Override
    public MapView createMap(World world) {
        Validate.notNull(world, "World cannot be null");

        net.minecraft.item.ItemStack stack = new net.minecraft.item.ItemStack(Items.MAP, 1);
        MapState worldmap = FilledMapItem.getOrCreateMapState(stack, ((WorldImpl) world).getHandle());
        return ((IMixinMapState)worldmap).getMapViewBF();
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

    @SuppressWarnings("resource")
    @Override
    public World createWorld(WorldCreator creator) {
        System.out.println("Bukkit#createWorld 0");
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

        boolean hardcore = creator.hardcore();

        RegistryOps<NbtElement> registryreadops = RegistryOps.of((DynamicOps<NbtElement>) NbtOps.INSTANCE, server.serverResourceManager.getResourceManager(), DynamicRegistryManager.create());
        LevelProperties worlddata = (LevelProperties) worldSession.readLevelProperties((DynamicOps<NbtElement>) registryreadops, method_29735(server.dataPackManager));

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

        long j = BiomeAccess.hashSeed(creator.seed());
        List<Spawner> list = ImmutableList.of(new PhantomSpawner(), new PillagerSpawner(), new CatSpawner(), new ZombieSiegeManager(), new WanderingTraderManager(worlddata));
        SimpleRegistry<DimensionOptions> registrymaterials = worlddata.getGeneratorOptions().getDimensions();
        DimensionOptions worlddimension = (DimensionOptions) registrymaterials.get(actualDimension);
        DimensionType dimensionmanager;
        net.minecraft.world.gen.chunk.ChunkGenerator chunkgenerator;

        if (worlddimension == null) {
            dimensionmanager = //(DimensionType) server.getRegistryManager().getDimensionTypes().getOrThrow(DimensionType.OVERWORLD_REGISTRY_KEY);
            server.getOverworld().getDimension();
            GeneratorOptions.createOverworldGenerator(null, 0);
            
            me.isaiah.common.cmixin.IMixinMinecraftServer ic = (me.isaiah.common.cmixin.IMixinMinecraftServer) server;
            chunkgenerator = ic.I_createOverworldGenerator();
        } else {
            dimensionmanager = worlddimension.getDimensionType();
            chunkgenerator = worlddimension.getChunkGenerator();
        }

        RegistryKey<net.minecraft.world.World> worldKey = RegistryKey.of(Registry.WORLD_KEY, new Identifier(name.toLowerCase(java.util.Locale.ENGLISH)));

        ServerWorld internal = (ServerWorld) new ServerWorld(server, server.workerExecutor, worldSession, worlddata, worldKey, dimensionmanager, getServer().worldGenerationProgressListenerFactory.create(11),
                chunkgenerator, worlddata.getGeneratorOptions().isDebugWorld(), j, creator.environment() == Environment.NORMAL ? list : ImmutableList.of(), true/*, creator.environment(), generator*/);

        if (!(worlds.containsKey(name.toLowerCase(java.util.Locale.ENGLISH))))
            return null;

        ((IMixinMinecraftServer)server).initWorld(internal, worlddata, worlddata, worlddata.getGeneratorOptions());

        internal.setMobSpawnOptions(true, true);
        server.worlds.put(internal.getRegistryKey(), internal);

        pluginManager.callEvent(new WorldInitEvent(((IMixinWorld)internal).getWorldImpl()));

        ((IMixinMinecraftServer)getServer()).loadSpawn(internal.getChunkManager().threadedAnvilChunkStorage.worldGenerationProgressListener, internal);

        pluginManager.callEvent(new WorldLoadEvent(((IMixinWorld)internal).getWorldImpl()));
        return ((IMixinWorld)internal).getWorldImpl();
    }

    public static DataPackSettings method_29735(ResourcePackManager resourcePackManager) {
        Collection<String> collection = resourcePackManager.getEnabledNames();
        ImmutableList<String> list = ImmutableList.copyOf(collection);
        List<String> list2 = resourcePackManager.getNames().stream().filter(string -> !collection.contains(string)).collect(ImmutableList.toImmutableList());
        return new DataPackSettings(list, list2);
    }

    public ChunkGenerator getGenerator(String world) {
        ConfigurationSection section = configuration.getConfigurationSection("worlds");
        ChunkGenerator result = null;

        if (section != null) {
            section = section.getConfigurationSection(world);

            if (section != null) {
                String name = section.getString("generator");

                if ((name != null) && (!name.equals(""))) {
                    String[] split = name.split(":", 2);
                    String id = (split.length > 1) ? split[1] : null;
                    Plugin plugin = pluginManager.getPlugin(split[0]);

                    if (plugin == null) {
                        getLogger().severe("Could not set generator for default world '" + world + "': Plugin '" + split[0] + "' does not exist");
                    } else if (!plugin.isEnabled()) {
                        getLogger().severe("Could not set generator for default world '" + world + "': Plugin '" + plugin.getDescription().getFullName() + "' is not enabled yet (is it load:STARTUP?)");
                    } else {
                        try {
                            result = plugin.getDefaultWorldGenerator(world, id);
                            if (result == null) {
                                getLogger().severe("Could not set generator for default world '" + world + "': Plugin '" + plugin.getDescription().getFullName() + "' lacks a default world generator");
                            }
                        } catch (Throwable t) {
                            plugin.getLogger().log(Level.SEVERE, "Could not set generator for default world '" + world + "': Plugin '" + plugin.getDescription().getFullName(), t);
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    public boolean dispatchCommand(CommandSender sender, String commandLine) throws CommandException {
        if (commandLine.startsWith("minecraft:") && sender instanceof Entity) {
            try {
                int result = vanillaCommandManager.dispatcher.execute(commandLine.replace("minecraft:", ""), ((CraftEntity)sender).nms.getCommandSource());
                return result != -1;
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
                throw new CommandException("Vanilla command syntax error: " + e.getMessage());
            }
        }

        if (commandMap.dispatch(sender, commandLine))
            return true;

        sender.sendMessage("Unknown command. Type " + (sender instanceof Player ? "\"/help\" for help." : "\"help\" for help."));
        return false;
    }

    @Override
    public Advancement getAdvancement(NamespacedKey arg0) {
        net.minecraft.advancement.Advancement advancement = server.getAdvancementLoader().get(CraftNamespacedKey.toMinecraft(arg0));
        return (advancement == null) ? null : ((IMixinAdvancement)advancement).getBukkitAdvancement();
    }

    @Override
    public boolean getAllowEnd() {
        return this.configuration.getBoolean("settings.allow-end");
    }

    @Override
    public boolean getAllowFlight() {
        return getServer().isFlightEnabled();
    }

    @Override
    public boolean getAllowNether() {
        return getServer().isNetherAllowed();
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
                return new IpBanList(server.playerManager.getIpBanList());
            case NAME:
            default:
                return new ProfileBanList(server.playerManager.getUserBanList());
        }
    }

    @Override
    public Set<OfflinePlayer> getBannedPlayers() {
        Set<OfflinePlayer> set = Sets.newHashSet();
        for (String s : getServer().getPlayerManager().getUserBanList().getNames())
            set.add(getOfflinePlayer(s));
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
        return this.configuration.getInt("settings.connection-throttle");
    }

    @Override
    public ConsoleCommandSender getConsoleSender() {
        return consoleCommandSender;
    }

    @Override
    public GameMode getDefaultGameMode() {
        return GameMode.getByValue(getServer().getDefaultGameMode().getId());
    }

    @SuppressWarnings("resource")
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
        return getServer().getSaveProperties().getGeneratorOptions().shouldGenerateStructures();
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
        return getServer().playerIdleTimeout;
    }

    @Override
    public String getIp() {
        return getServer().serverIp;
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
    public MapViewImpl getMap(int arg0) {
        MapState worldmap = server.getWorld(net.minecraft.world.World.OVERWORLD).getMapState("map_" + arg0);
        if (worldmap == null)
            return null;
        return ((IMixinMapState)worldmap).getMapViewBF();
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
        return getServer().getServerMotd();
    }

    @Override
    public String getName() {
        return serverName;
    }

    @Override
    public OfflinePlayer getOfflinePlayer(String name) {
        OfflinePlayer result = getPlayerExact(name);
        if (result == null) {
            GameProfile profile = null;
            if (this.getOnlineMode() || SpigotConfig.bungee) {
                profile = server.getUserCache().findByName(name).orElse(null);
            }
            result = profile == null ? this.getOfflinePlayer(new GameProfile(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8)), name)) : this.getOfflinePlayer(profile);
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
        WorldSaveHandler storage = ((IMixinMinecraftServer)server).getSaveHandler_BF();
        String[] files = storage.playerDataDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".dat");
            }
        });
        Set<OfflinePlayer> players = new HashSet<OfflinePlayer>();

        for (String file : files) {
            try {
                players.add(getOfflinePlayer(UUID.fromString(file.substring(0, file.length() - 4))));
            } catch (IllegalArgumentException ex) {/* Who knows what is in this directory, just ignore invalid files*/}
        }

        players.addAll(getOnlinePlayers());

        return players.toArray(new OfflinePlayer[players.size()]);
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
        return getServer().getServerPort();
    }

    @Override
    public List<Recipe> getRecipesFor(ItemStack result) {
        Validate.notNull(result, "Result cannot be null");

        List<Recipe> results = new ArrayList<Recipe>();
        Iterator<Recipe> iter = recipeIterator();
        while (iter.hasNext()) {
            Recipe recipe = iter.next();
            ItemStack stack = recipe.getResult();
            if (stack.getType() != result.getType())
                continue;
            if (result.getDurability() == -1 || result.getDurability() == stack.getDurability())
                results.add(recipe);
        }
        return results;
    }

    @Override
    public BukkitSchedulerImpl getScheduler() {
        return scheduler;
    }

    @Override
    public CardboardScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    @Override
    public IconCacheImpl getServerIcon() {
        return icon;
    }

    @Override
    public ServicesManager getServicesManager() {
        return servicesManager;
    }

    @Override
    public String getShutdownMessage() {
        // TODO Auto-generated method stub
        return "Server Shutdown";
    }

    @Override
    public int getSpawnRadius() {
        return getServer().getSpawnProtectionRadius();
    }

    @SuppressWarnings("unchecked")
    public <T extends Keyed> org.bukkit.Tag<T> getTag(String registry, NamespacedKey tag, Class<T> clazz) {
        Identifier key = CraftNamespacedKey.toMinecraft(tag);
        

        /*switch (registry) {
            case org.bukkit.Tag.REGISTRY_BLOCKS:
                Preconditions.checkArgument(clazz == org.bukkit.Material.class, "Block namespace must have material type");
                return (org.bukkit.Tag<T>) new BlockTagImpl(server.getTagManager().getOrCreateTagGroup(Registry.BLOCK_KEY), key);
            case org.bukkit.Tag.REGISTRY_ITEMS:
                Preconditions.checkArgument(clazz == org.bukkit.Material.class, "Item namespace must have material type");
                return (org.bukkit.Tag<T>) new ItemTagImpl(server.getTagManager().getOrCreateTagGroup(Registry.ITEM_KEY), key);
            case org.bukkit.Tag.REGISTRY_FLUIDS:
                Preconditions.checkArgument(clazz == org.bukkit.Fluid.class, "Fluid namespace must have fluid type");
                return (org.bukkit.Tag<T>) new Tags.FluidTagImpl(server.getTagManager().getOrCreateTagGroup(Registry.FLUID_KEY), key);
            default:
                throw new IllegalArgumentException();
        }*/
        switch (registry) {
        case "blocks": {
            Preconditions.checkArgument(clazz == Material.class, "Block namespace must have material type");
            return (Tag<T>) new BlockTagImpl(BlockTags.getTagGroup(), key);
        }
        case "items": {
            Preconditions.checkArgument(clazz == Material.class, "Item namespace must have material type");
            return (org.bukkit.Tag<T>) new ItemTagImpl(ItemTags.getTagGroup(), key);
        }
        case "fluids": {
            //Preconditions.checkArgument(clazz == Fluid.class, "Fluid namespace must have fluid type");
            return (org.bukkit.Tag<T>) new Tags.FluidTagImpl(FluidTags.getTagGroup(), key);
        }
        case "entity_types": {
            Preconditions.checkArgument(clazz == org.bukkit.entity.EntityType.class, "Entity type namespace must have entity type");
            return (org.bukkit.Tag<T>) new EntityTagImpl(EntityTypeTags.getTagGroup(), key);
        }
        default:
            throw new IllegalArgumentException();
    }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Keyed> Iterable<org.bukkit.Tag<T>> getTags(String registry, Class<T> clazz) {
        /*switch (registry) {
            case org.bukkit.Tag.REGISTRY_BLOCKS:
                Preconditions.checkArgument(clazz == org.bukkit.Material.class, "Block namespace must have material type");
                TagGroup<Block> blockTags = server.getTagManager().getOrCreateTagGroup(Registry.BLOCK_KEY);
                return blockTags.getTags().keySet().stream().map(key -> (org.bukkit.Tag<T>) new BlockTagImpl(blockTags, key)).collect(ImmutableList.toImmutableList());
            case org.bukkit.Tag.REGISTRY_ITEMS:
                Preconditions.checkArgument(clazz == org.bukkit.Material.class, "Item namespace must have material type");

                TagGroup<Item> itemTags = server.getTagManager().getOrCreateTagGroup(Registry.ITEM_KEY);
                return itemTags.getTags().keySet().stream().map(key -> (org.bukkit.Tag<T>) new ItemTagImpl(itemTags, key)).collect(ImmutableList.toImmutableList());
            case org.bukkit.Tag.REGISTRY_FLUIDS:
                Preconditions.checkArgument(clazz == org.bukkit.Material.class, "Fluid namespace must have fluid type");

                TagGroup<Fluid> fluidTags = server.getTagManager().getOrCreateTagGroup(Registry.FLUID_KEY);
                return fluidTags.getTags().keySet().stream().map(key -> (org.bukkit.Tag<T>) new Tags.FluidTagImpl(fluidTags, key)).collect(ImmutableList.toImmutableList());
            default:
                throw new IllegalArgumentException();
        }*/
        switch (registry) {
            case "blocks": {
                Preconditions.checkArgument(clazz == Material.class, "Block namespace must have material type");
                TagGroup<Block> blockTags = BlockTags.getTagGroup();
                return blockTags.getTags().keySet().stream().map(key -> (org.bukkit.Tag<T>) new BlockTagImpl(blockTags, (Identifier)key)).collect(ImmutableList.toImmutableList());
            }
            case "items": {
                Preconditions.checkArgument(clazz == Material.class, "Item namespace must have material type");
                TagGroup<Item> itemTags = ItemTags.getTagGroup();
                return itemTags.getTags().keySet().stream().map(key -> (org.bukkit.Tag<T>) new ItemTagImpl(itemTags, (Identifier)key)).collect(ImmutableList.toImmutableList());
            }
            case "fluids": {
                Preconditions.checkArgument(clazz == Material.class, "Fluid namespace must have fluid type");
                TagGroup<net.minecraft.fluid.Fluid> fluidTags = FluidTags.getTagGroup();
                return fluidTags.getTags().keySet().stream().map(key -> (org.bukkit.Tag<T>) new Tags.FluidTagImpl(fluidTags, (Identifier)key)).collect(ImmutableList.toImmutableList());
            }
            case "entity_types": {
                Preconditions.checkArgument(clazz == org.bukkit.entity.EntityType.class, "Entity type namespace must have entity type");
                TagGroup<EntityType<?>> entityTags = EntityTypeTags.getTagGroup();
                return entityTags.getTags().keySet().stream().map(key -> (org.bukkit.Tag<T>) new EntityTagImpl(entityTags, (Identifier)key)).collect(ImmutableList.toImmutableList());
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public int getTicksPerAnimalSpawns() {
        return this.configuration.getInt("ticks-per.animal-spawns");
    }

    @Override
    public int getTicksPerMonsterSpawns() {
        return this.configuration.getInt("ticks-per.monster-spawns");
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
    	// Some plugins like WorldEdit use PaperLib.getMinecraftVersion() for version checks
        // Update: WorldEdit now has a preview 1.17 build
        return getShortVersion(); //serverVersion + " (MC: 1.17.1)";
    }

    public String getShortVersion() {
        return shortVersion + " (MC: " + server.getVersion() + ")";
    }

    @Override
    public int getViewDistance() {
        return server instanceof MinecraftDedicatedServer ? ((MinecraftDedicatedServer)getServer()).getProperties().viewDistance : 12;
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
        return ((IMixinMinecraftServer)this.getServer()).getSessionBF().getWorldDirectory(net.minecraft.world.World.OVERWORLD).getParent().toFile();
    }

    @Override
    public String getWorldType() {
        return server instanceof MinecraftDedicatedServer ? ((MinecraftDedicatedServer)getServer()).getProperties().properties.getProperty("level-type") : "NORMAL";
    }

    @Override
    public List<World> getWorlds() {
        return new ArrayList<World>(worlds.values());
    }

    @Override
    public boolean hasWhitelist() {
        return getServer().isEnforceWhitelist();
    }

    @Override
    public boolean isHardcore() {
        return getServer().isHardcore();
    }

    @Override
    public boolean isPrimaryThread() {
        boolean mainThread = server.isOnThread();
        if (!mainThread) {
            // Check if thread a DimensionalThreading thread, these threads are
            // safe to perform operations on as if they were the main thread.
            return Thread.currentThread().getName().startsWith("dimthread");
        }
        return mainThread;
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
        return new RecipeIterator();
    }

    @Override
    public void reload() {
        loadIcon();

        try {
            server.getPlayerManager().getIpBanList().load();
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Failed to load banned-ips.json, " + ex.getMessage());
        }
        try {
            server.getPlayerManager().getUserBanList().load();
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Failed to load banned-players.json, " + ex.getMessage());
        }

        pluginManager.clearPlugins();
        commandMap.clearCommands();
        resetRecipes();
        reloadData();

        int pollCount = 0;

        // Wait for at most 2.5 seconds for plugins to close their threads
        while (pollCount < 50 && getScheduler().getActiveWorkers().size() > 0) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {}
            pollCount++;
        }

        List<BukkitWorker> overdueWorkers = getScheduler().getActiveWorkers();
        for (BukkitWorker worker : overdueWorkers) {
            Plugin plugin = worker.getOwner();
            String author = "<NoAuthorGiven>";
            if (plugin.getDescription().getAuthors().size() > 0) author = plugin.getDescription().getAuthors().get(0);
            getLogger().log(Level.SEVERE, "Nag author: '" + author + "' of '" + plugin.getDescription().getName() + "' about the following: " +
                "This plugin is not properly shutting down its async tasks when it is being reloaded. This may cause conflicts with the newly loaded version of the plugin");
        }
        loadPlugins();
        enablePlugins(PluginLoadOrder.STARTUP);
        enablePlugins(PluginLoadOrder.POSTWORLD);
        getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.RELOAD));
    }

    @Override
    public void reloadData() {
        // TODO Auto-generated method stub
    }

    @Override
    public void reloadWhitelist() {
        server.getPlayerManager().reloadWhitelist();
    }

    @Override
    public void resetRecipes() {
        reloadData();
    }

    @Override
    public void savePlayers() {
        server.getPlayerManager().saveAllPlayerData();
    }

    @Override
    public List<Entity> selectEntities(CommandSender sender, String selector) {
        Preconditions.checkArgument(selector != null, "Selector cannot be null");
        Preconditions.checkArgument(sender != null, "Sender cannot be null");

        EntityArgumentType arg = EntityArgumentType.entities();
        List<? extends net.minecraft.entity.Entity> nms;

        try {
            StringReader reader = new StringReader(selector);
            nms = arg.parse(reader).getEntities(MinecraftCommandWrapper.getCommandSource(sender));
            Preconditions.checkArgument(!reader.canRead(), "Spurious trailing data in selector: " + selector);
        } catch (CommandSyntaxException ex) {
            throw new IllegalArgumentException("Could not parse selector: " + selector, ex);
        }
        return new ArrayList<>(Lists.transform(nms, (entity) -> ((IMixinEntity)entity).getBukkitEntity()));
    }

    @Override
    public void setDefaultGameMode(GameMode gm) {
        server.setDefaultGameMode(net.minecraft.world.GameMode.byId(gm.getValue()));
    }

    @Override
    public void setIdleTimeout(int arg0) {
        server.setPlayerIdleTimeout(arg0);
    }

    @Override
    public void setSpawnRadius(int arg0) {
        // TODO Auto-generated method stub
        // server.getProperties().spawnProtection = arg0;
    }

    @Override
    public void setWhitelist(boolean arg0) {
        server.setEnforceWhitelist(arg0);
    }

    @Override
    public void shutdown() {
        server.stop(false);
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
        if (world == null) return false;

        ServerWorld handle = (ServerWorld) ((WorldImpl) world).getHandle();

        if (!(((IMixinMinecraftServer)(Object)getServer()).getWorldMap().containsKey(handle.toServerWorld().getRegistryKey())))
            return false;

        if (handle.toServerWorld().getRegistryKey() == ServerWorld.OVERWORLD || handle.getPlayers().size() > 0)
            return false;

        WorldUnloadEvent e = new WorldUnloadEvent(world);
        pluginManager.callEvent(e);

        if (e.isCancelled()) return false;

        try {
            if (save) handle.save(null, true, true);
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
        return this.configuration.getInt("ticks-per.ambient-spawns");
    }

    @Override
    public int getTicksPerWaterSpawns() {
        return this.configuration.getInt("ticks-per.water-spawns");
    }

    @SuppressWarnings("resource")
    @Override
    public boolean removeRecipe(NamespacedKey recipeKey) {
        Preconditions.checkArgument(recipeKey != null, "recipeKey == null");

        Identifier mcKey = CraftNamespacedKey.toMinecraft(recipeKey);
        for (Map<Identifier, net.minecraft.recipe.Recipe<?>> recipes : ((IMixinRecipeManager)getServer().getRecipeManager()).getRecipes().values())
            if (recipes.remove(mcKey) != null)
                return true;

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
                    getCommandMap().tabComplete(player, message, new Location(((IMixinWorld)(Object)world).getWorldImpl(), pos.x, pos.y, pos.z));
        } catch (CommandException ex) {
            player.sendMessage(ChatColor.RED + "An internal error occurred while attempting to tab-complete this command");
            getLogger().log(Level.SEVERE, "Exception when " + player.getName() + " attempted to tab complete " + message, ex);
        }

        return completions == null ? ImmutableList.<String>of() : completions;
    }

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

    public MinecraftServer getHandle() {
        return getServer();
    }

    public CommandMapImpl getCommandMap() {
        return commandMap;
    }

    // Because PlayerManager is broken
    public List<String> getOperatorList() throws IOException {
        File f = new File(MinecraftServer.USER_CACHE_FILE.getParentFile(), "ops.json");
        List<String> content = null;
        try {
            content = Files.readAllLines(f.toPath());
        } catch (IOException e) {throw e;}

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
            return SpigotConfig.config; // TODO
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
    public Recipe getRecipe(NamespacedKey recipeKey) {
        Preconditions.checkArgument(recipeKey != null, "recipeKey == null");
        Optional<? extends net.minecraft.recipe.Recipe<?>> opt = getServer().getRecipeManager().get(CraftNamespacedKey.toMinecraft(recipeKey));

        return !opt.isPresent() ? null : ((IMixinRecipe)opt.get()).toBukkitRecipe();
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

    public MetadataStoreBase<Entity> getEntityMetadata() {
        return entityMetadata;
    }

    public MetadataStoreBase<OfflinePlayer> getPlayerMetadata() {
        return playerMetadata;
    }

    public MetadataStoreBase<World> getWorldMetadata() {
        return worldMetadata;
    }

    // PaperAPI - start
    public long[] getTickTimes() {
        return new long[] {(long) server.tickTime};
    }

    public double getAverageTickTime() {
        return server.tickTime;
    }

    @Override
    public com.destroystokyo.paper.profile.PlayerProfile createProfile(UUID uuid) {
        return createProfile(uuid, null);
    }

    @Override
    public com.destroystokyo.paper.profile.PlayerProfile createProfile(String name) {
        return createProfile(null, name);
    }

    @Override
    public com.destroystokyo.paper.profile.PlayerProfile createProfile(UUID uuid, String name) {
        Player player = uuid != null ? Bukkit.getPlayer(uuid) : (name != null ? Bukkit.getPlayerExact(name) : null);
        return (player != null) ? new CraftPlayerProfile((PlayerImpl) player) : new CraftPlayerProfile(uuid, name);
    }


    @Override
    public ChunkData createVanillaChunkData(World arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getCurrentTick() {
        return server.getTicks();
    }

    @Override
    public int getMaxWorldSize() {
        // TODO Auto-generated method stub
        return ServerWorld.HORIZONTAL_LIMIT;
    }

    @Override
    public String getMinecraftVersion() {
        return server.getVersion();
    }

    @Override
    public MobGoals getMobGoals() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OfflinePlayer getOfflinePlayerIfCached(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPermissionMessage() {
        return "No Permission";
    }

    @Override
    public UUID getPlayerUniqueId(String arg0) {
        return Bukkit.getPlayer(arg0).getUniqueId();
    }

    @Override
    public double[] getTPS() {
        return new double[] {server.tickTime};
    }

    @Override
    public boolean isStopping() {
        return !server.isRunning();
    }

    @Override
    public boolean reloadCommandAliases() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void reloadPermissions() {
        // TODO Auto-generated method stub
    }

    @Override
    public void setMaxPlayers(int arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean suggestPlayerNamesWhenNullTabCompletions() {
        // TODO Auto-generated method stub
        return false;
    }
    // PaperAPI - end

    @Override
    public @NonNull Iterable<? extends Audience> audiences() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int broadcast(@NotNull Component arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int broadcast(@NotNull Component arg0, @NotNull String arg1) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public @NotNull Inventory createInventory(@Nullable InventoryHolder arg0, @NotNull InventoryType arg1,
            @NotNull Component arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @NotNull Inventory createInventory(@Nullable InventoryHolder arg0, int arg1, @NotNull Component arg2)
            throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @NotNull Merchant createMerchant(@Nullable Component arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @NotNull DatapackManager getDatapackManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @Nullable World getWorld(@NotNull NamespacedKey key) {
        Identifier id = CraftNamespacedKey.toMinecraft(key);

        for (ServerWorld world : server.worlds.values()) {
            Identifier name = world.getRegistryKey().getValue();
            if (name.equals(id))
                return ((IMixinWorld)world).getWorldImpl();
        }

        return null;
    }

    @Override
    public @NotNull Component motd() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @Nullable Component shutdownMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @NotNull ItemStack craftItem(ItemStack[] craftingMatrix, World world, Player player) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @Nullable Recipe getCraftingRecipe(ItemStack[] craftingMatrix, World world) {
        ScreenHandler container = new ScreenHandler(null, -1){

            //@Override
            public InventoryView getBukkitView() {
                return null;
            }

            @Override
            public boolean canUse(PlayerEntity player) {
                return false;
            }
        };
        CraftingInventory inventoryCrafting = new CraftingInventory(container, 3, 3);
        Optional<CraftingRecipe> opt = this.getNMSRecipe(craftingMatrix, inventoryCrafting, (WorldImpl)world);
        if (opt.isEmpty()) { return null; }
        
        return ((IMixinRecipe)opt.get()).toBukkitRecipe();
    }

    private Optional<CraftingRecipe> getNMSRecipe(ItemStack[] craftingMatrix, CraftingInventory inventoryCrafting, WorldImpl world) {
        Preconditions.checkArgument(craftingMatrix != null, "craftingMatrix must not be null");
        Preconditions.checkArgument(craftingMatrix.length == 9, "craftingMatrix must be an array of length 9");
        Preconditions.checkArgument(world != null, "world must not be null");
        int i = 0;
        while (i < craftingMatrix.length) {
            inventoryCrafting.setStack(i, CraftItemStack.asNMSCopy(craftingMatrix[i]));
            ++i;
        }
        return this.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, inventoryCrafting, world.getHandle());
    }

    @Override
    public @NotNull File getPluginsFolder() {
        return new File("plugins");
    }

    @Override
    public StructureManager getStructureManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getTicksPerWaterUndergroundCreatureSpawns() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getWaterUndergroundCreatureSpawnLimit() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isWhitelistEnforced() {
        return server.isEnforceWhitelist();
    }

    @Override
    public void setWhitelistEnforced(boolean bl) {
        server.setEnforceWhitelist(bl);
    }

}