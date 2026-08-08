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
import io.papermc.paper.adventure.PaperAdventure;
import net.minecraft.world.level.dimension.LevelStem;
import org.bukkit.block.BlockType;
import org.bukkit.craftbukkit.inventory.*;
import org.bukkit.craftbukkit.util.ApiVersion;
import org.bukkit.generator.BiomeProvider;
import org.cardboardpowered.BukkitLogger;
import org.cardboardpowered.bridge.advancements.AdvancementHolderBridge;
import org.cardboardpowered.bridge.server.MinecraftServerBridge;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;
import org.cardboardpowered.bridge.server.players.PlayerListBridge;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.world.item.crafting.RecipeManagerBridge;
import org.cardboardpowered.bridge.world.item.crafting.RecipeHolderBridge;
import org.cardboardpowered.bridge.world.level.LevelBridge;
import org.cardboardpowered.bridge.world.level.saveddata.maps.MapItemSavedDataBridge;
import org.cardboardpowered.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.cardboardpowered.impl.MetadataStoreImpl;
import org.bukkit.craftbukkit.scheduler.CraftScheduler;
import org.cardboardpowered.mohistremap.RemapUtilProvider;
import org.cardboardpowered.util.nms.RemapUtils;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.ban.BanListType;
import io.papermc.paper.configuration.PaperServerConfiguration;
import io.papermc.paper.configuration.ServerConfiguration;
import io.papermc.paper.datapack.DatapackManager;
import io.papermc.paper.math.Position;
import io.papermc.paper.plugin.manager.PaperPluginManagerImpl;
import io.papermc.paper.profile.PaperFilledProfileCache;
import io.papermc.paper.registry.RegistryAccess;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ConsoleInput;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.IpBanListEntry;
import net.minecraft.server.players.NameAndId;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.BanList.Type;
import org.bukkit.Warning.WarningState;
import org.bukkit.World.Environment;
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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.Conversable;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftEntityFactory;
import org.bukkit.craftbukkit.packs.CraftDataPackManager;
import org.bukkit.craftbukkit.packs.CraftResourcePack;
import org.bukkit.craftbukkit.scoreboard.CraftScoreboardManager;
import org.bukkit.craftbukkit.scoreboard.CraftCriteria;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityFactory;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.help.HelpMap;
import org.bukkit.inventory.*;
import org.bukkit.loot.LootTable;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataStoreBase;
// import org.bukkit.packs.DataPackManager;
import org.bukkit.packs.ResourcePack;
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
import org.bukkit.potion.PotionBrewer;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scheduler.BukkitWorker;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.structure.StructureManager;
import org.bukkit.util.StringUtil;
import org.bukkit.util.permissions.DefaultPermissions;
import org.cardboardpowered.RegistryUtil;
import org.cardboardpowered.impl.CardboardAbstractServer;
import org.cardboardpowered.impl.CardboardBossBar;
import org.cardboardpowered.impl.CraftProfileBanList;
import org.cardboardpowered.impl.IpBanList;
import org.cardboardpowered.impl.command.BukkitCommandWrapper;
import org.cardboardpowered.impl.command.CardboardConsoleCommandSender;
import org.cardboardpowered.impl.command.CommandMapImpl;
import org.cardboardpowered.impl.command.MinecraftCommandWrapper;
import org.cardboardpowered.impl.command.VersionCommand;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.util.CraftInventoryCreator;
import org.bukkit.craftbukkit.inventory.CraftSmokingRecipe;
import org.bukkit.craftbukkit.inventory.CraftStonecuttingRecipe;
import org.cardboardpowered.impl.map.MapViewImpl;
import org.cardboardpowered.impl.tag.CraftBlockTag;
import org.cardboardpowered.impl.tag.CraftGameEventTag;
import org.cardboardpowered.impl.tag.CraftEntityTag;
import org.bukkit.craftbukkit.tag.CraftFluidTag;
import org.cardboardpowered.impl.tag.CraftItemTag;
import org.cardboardpowered.impl.util.CommandPermissions;
import org.cardboardpowered.impl.util.CardboardCachedServerIcon;
import org.cardboardpowered.impl.util.SimpleHelpMap;
import org.cardboardpowered.impl.world.ChunkDataImpl;
import org.cardboardpowered.impl.world.CraftWorld;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spigotmc.SpigotConfig;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;


@SuppressWarnings("deprecation")
public class CraftServer extends CardboardAbstractServer implements Server {

    public final String serverName = "Cardboard";
    public final String bukkitVersion = "26.2"; // "26.1.2"; // "1.21.11-R0.1-SNAPSHOT"; // "1.21.10-R0.1-SNAPSHOT"; // "1.21.8-R0.1-SNAPSHOT"; // "1.21.1-R0.1-SNAPSHOT";

    private final Logger logger = BukkitLogger.getLogger();

    private final CommandMapImpl commandMap;

    private final SimplePluginManager pluginManager;
    public final PaperPluginManagerImpl paperPluginManager;

    public Set<String> activeCompatibilities = Collections.emptySet();

    private final CraftMagicNumbers unsafe = (CraftMagicNumbers) CraftMagicNumbers.INSTANCE;
    private final ServicesManager servicesManager = new SimpleServicesManager();
    private final CraftScheduler scheduler = new CraftScheduler();
    private final ConsoleCommandSender consoleCommandSender = new CardboardConsoleCommandSender();
    private final Map<UUID, OfflinePlayer> offlinePlayers = new MapMaker().weakValues().makeMap();

    public List<CraftPlayer> playerView;
    private WarningState warningState = WarningState.DEFAULT;
    public final Map<String, World> worlds = new LinkedHashMap<String, World>();

    private final SimpleHelpMap helpMap = new SimpleHelpMap(this);
    private final StandardMessenger messenger = new StandardMessenger();
    private final YamlConfiguration configuration;

    public static DedicatedServer console;
    protected final DedicatedPlayerList playerList;

    public static CraftServer INSTANCE;
    public ApiVersion minimumAPI;
    public CraftScoreboardManager scoreboardManager;

    private final MetadataStoreBase<Entity> entityMetadata = MetadataStoreImpl.newEntityMetadataStore();
    private final MetadataStoreBase<OfflinePlayer> playerMetadata = MetadataStoreImpl.newPlayerMetadataStore();
    private final MetadataStoreBase<World> worldMetadata = MetadataStoreImpl.newWorldMetadataStore();

    private final Map<Class<?>, org.bukkit.Registry<?>> registries = new HashMap<>();

    public CraftDataPackManager dataPackManager;

    private CraftServerTickManager serverTickManager;
    private CraftServerLinks serverLinks;

    private final ServerConfiguration serverConfig = new PaperServerConfiguration();

    private final PaperFilledProfileCache paperProfileCache;

    // TODO: Move to ApiServices
    public PaperFilledProfileCache getPaperFilledProfileCache() {
    	return paperProfileCache;
    }

    // @MonotonicNonNull
    // public final PluginRemapper pluginRemapper;

    // Paper start - Folia region threading API
    private final io.papermc.paper.threadedregions.scheduler.FallbackRegionScheduler regionizedScheduler = new io.papermc.paper.threadedregions.scheduler.FallbackRegionScheduler();
    private final io.papermc.paper.threadedregions.scheduler.FoliaAsyncScheduler asyncScheduler = new io.papermc.paper.threadedregions.scheduler.FoliaAsyncScheduler();
    private final io.papermc.paper.threadedregions.scheduler.FoliaGlobalRegionScheduler globalRegionScheduler = new io.papermc.paper.threadedregions.scheduler.FoliaGlobalRegionScheduler();

    @Override
    public final io.papermc.paper.threadedregions.scheduler.RegionScheduler getRegionScheduler() {
        return this.regionizedScheduler;
    }

    @Override
    public final io.papermc.paper.threadedregions.scheduler.AsyncScheduler getAsyncScheduler() {
        return this.asyncScheduler;
    }

    @Override
    public final io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler getGlobalRegionScheduler() {
        return this.globalRegionScheduler;
    }
    // Paper end - Folia reagion threading API

    public CraftServer(DedicatedServer nms) {
    	super(nms);
        INSTANCE = this;
        server = nms;
        console = nms;
        commandMap = new CommandMapImpl(this);

        // Paper start
        // this.commandMap = new CraftCommandMap(this);
        this.pluginManager = new SimplePluginManager(this, commandMap);
        this.paperPluginManager = new io.papermc.paper.plugin.manager.PaperPluginManagerImpl(this, this.commandMap, pluginManager);
        this.pluginManager.paperPluginManager = this.paperPluginManager;
         // Paper end

        this.paperProfileCache = new PaperFilledProfileCache();

        scoreboardManager = new CraftScoreboardManager(nms, server.getScoreboard());

        configuration = YamlConfiguration.loadConfiguration(new File("bukkit.yml"));
        configuration.options().copyDefaults(true);
        configuration.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("configurations/bukkit.yml"), Charsets.UTF_8)));
        saveConfig();

        this.playerView = Collections.unmodifiableList(Lists.transform(server.playerList.players, new Function<ServerPlayer, CraftPlayer>() {
            @Override
            public CraftPlayer apply(ServerPlayer player) {
                return (CraftPlayer) ((EntityBridge)player).getBukkitEntity();
            }
        }));

        this.dataPackManager = new CraftDataPackManager(this.getServer().getPackRepository());
        this.serverTickManager = new CraftServerTickManager(console.tickRateManager());
        this.serverLinks = new CraftServerLinks(console);
        this.minimumAPI = ApiVersion.getOrCreateVersion(this.configuration.getString("settings.minimum-api"));
        loadIcon();

        loadCompatibilities();
        ((CraftMagicNumbers) CraftMagicNumbers.INSTANCE).getCommodore().updateReroute(activeCompatibilities::contains);

        this.playerList = server.getPlayerList();

        // this.pluginRemapper = Boolean.getBoolean("paper.disablePluginRemapping") ? null : PluginRemapper.create(new File("plugins").toPath());

        // Register PotionEffectType
        // CardboardMod.registerPotionEffectType();

        // Register Registeries
        this.logger.info("Injecting into Bukkit registries.");
        RegistryUtil.inject_into_bukkit_registry(nms);
    }

    private void loadCompatibilities() {
    	// Paper - Big nope
    }

    @Override
    public ServerLinks getServerLinks() {
        return this.serverLinks;
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

    @Override
    public CardboardCachedServerIcon loadServerIcon(File file) throws Exception {
        Validate.notNull(file, "File cannot be null");
        if (!file.isFile())
            throw new IllegalArgumentException(file + " is not a file");
        return CardboardCachedServerIcon.createFromFile(file);
    }

    @Override
    public CardboardCachedServerIcon loadServerIcon(BufferedImage image) throws Exception {
        Validate.notNull(image, "Image cannot be null");
        return CardboardCachedServerIcon.createFromImage(image);
    }

    public void addWorldToMap(CraftWorld world) {
        worlds.put(world.getName(), world);
    }

    public void loadPlugins() {
        RemapUtils remapUtil = new RemapUtils();
        RemapUtilProvider.setInstance(remapUtil);
        remapUtil.init();

        pluginManager.registerInterface(JavaPluginLoader.class);

        io.papermc.paper.plugin.entrypoint.LaunchEntryPointHandler.INSTANCE.enter(io.papermc.paper.plugin.entrypoint.Entrypoint.PLUGIN); // Paper - replace implementation
    }
    
    public void enablePlugins(PluginLoadOrder type) {
        if (type == PluginLoadOrder.STARTUP) {
            this.helpMap.clear();
            this.helpMap.initializeGeneralTopics();
            // if (io.papermc.paper.configuration.GlobalConfiguration.get().misc.loadPermissionsYmlBeforePlugins) loadCustomPermissions(); // Paper
        }

        Plugin[] plugins = this.pluginManager.getPlugins();

        for (Plugin plugin : plugins) {
            if ((!plugin.isEnabled()) && (plugin.getDescription().getLoad() == type)) {
                this.enablePlugin(plugin);
            }
        }

        if (type == PluginLoadOrder.POSTWORLD) {
            // Spigot start - Allow vanilla commands to be forced to be the main command
            this.commandMap.setFallbackCommands();
            
            setVanillaCommands();
            commandMap.registerServerAliases();
            
            // Spigot end
            DefaultPermissions.registerCorePermissions();
           
            CommandPermissions.registerCorePermissions();
            
            // CraftDefaultPermissions.registerCorePermissions();
            // if (!io.papermc.paper.configuration.GlobalConfiguration.get().misc.loadPermissionsYmlBeforePlugins) this.loadCustomPermissions(); // Paper
            
            helpMap.initializeCommands();
            this.syncCommands();
        }
    }

    public Commands vanillaCommandManager;

    private void setVanillaCommands() {
        Commands dispatcher = (this.vanillaCommandManager = server.getCommands());

        // Build a list of all Vanilla commands and create wrappers
        for (CommandNode<CommandSourceStack> cmd : dispatcher.getDispatcher().getRoot().getChildren()) {
            if (cmd.getCommand() != null && cmd.getCommand() instanceof BukkitCommandWrapper)
                continue;
            commandMap.register("minecraft", new MinecraftCommandWrapper(dispatcher, cmd));
        }
    }


    @SuppressWarnings("unchecked")
    private void syncCommands() {
        // Clear existing commands
        Commands dispatcher = ((MinecraftServerBridge) server).setCommandManager(new Commands(Commands.CommandSelection.ALL, CommandBuildContext.simple(console.registryAccess(), FeatureFlagSet.of())));

        // Register all commands, vanilla ones will be using the old dispatcher references
        for (Map.Entry<String, Command> entry : commandMap.getKnownCommands().entrySet()) {
            String label = entry.getKey();
            Command command = entry.getValue();

            if (command instanceof MinecraftCommandWrapper) {
                LiteralCommandNode<CommandSourceStack> node = (LiteralCommandNode<CommandSourceStack>) ((MinecraftCommandWrapper) command).vanillaCommand;
                if (!node.getLiteral().equals(label)) {
                    LiteralCommandNode<CommandSourceStack> clone = new LiteralCommandNode<CommandSourceStack>(label, node.getCommand(), node.getRequirement(), node.getRedirect(), node.getRedirectModifier(), node.isFork());

                    for (CommandNode<CommandSourceStack> child : node.getChildren())
                        clone.addChild(child);
                    node = clone;
                }

                dispatcher.getDispatcher().getRoot().addChild(node);
            } else new BukkitCommandWrapper(entry.getValue()).register(dispatcher.getDispatcher(), label);
        }

        // Refresh commands
        for (ServerPlayer player : getHandle().getPlayers())
            dispatcher.sendCommands(player);
    }

    private void enablePlugin(Plugin plugin) {
        try {
            List<Permission> perms = plugin.getDescription().getPermissions();
            List<Permission> permsToLoad = new ArrayList<>(); // Paper
            for (Permission perm : perms) {
                // Paper start
                if (this.paperPluginManager.getPermission(perm.getName()) == null) {
                    permsToLoad.add(perm);
                } else {
                    this.getLogger().log(Level.WARNING, "Plugin " + plugin.getDescription().getFullName() + " tried to register permission '" + perm.getName() + "' but it's already registered");
                // Paper end
                }
            }
            this.paperPluginManager.addPermissions(permsToLoad); // Paper

            this.pluginManager.enablePlugin(plugin);
        } catch (Throwable ex) {
            Logger.getLogger(CraftServer.class.getName()).log(Level.SEVERE, ex.getMessage() + " loading " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
        }
    }

    public DedicatedServer getServer() {
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
        return "CraftServer{" + "serverName=" + serverName + ",serverVersion=" + serverVersion + ",minecraftVersion=" + SharedConstants.getCurrentVersion().name() + '}';
    }

    @Override
    public boolean addRecipe(Recipe recipe, boolean resendRecipes) {
        CraftRecipe toAdd;
        if (recipe instanceof CraftRecipe) {
            toAdd = (CraftRecipe) recipe;
        } else {
            if (recipe instanceof ShapedRecipe) {
                toAdd = CraftShapedRecipe.fromBukkitRecipe((ShapedRecipe) recipe);
            } else if (recipe instanceof ShapelessRecipe) {
                toAdd = CraftShapelessRecipe.fromBukkitRecipe((ShapelessRecipe) recipe);
            } else if (recipe instanceof FurnaceRecipe) {
                toAdd = CraftFurnaceRecipe.fromBukkitRecipe((FurnaceRecipe) recipe);
            } else if (recipe instanceof BlastingRecipe) {
                toAdd = CraftBlastingRecipe.fromBukkitRecipe((BlastingRecipe) recipe);
            } else if (recipe instanceof CampfireRecipe) {
                toAdd = CraftCampfireRecipe.fromBukkitRecipe((CampfireRecipe) recipe);
            } else if (recipe instanceof SmokingRecipe) {
                toAdd = CraftSmokingRecipe.fromBukkitRecipe((SmokingRecipe) recipe);
            } else if (recipe instanceof StonecuttingRecipe) {
                toAdd = CraftStonecuttingRecipe.fromBukkitRecipe((StonecuttingRecipe) recipe);
            } else if (recipe instanceof SmithingTransformRecipe) {
                toAdd = CraftSmithingTransformRecipe.fromBukkitRecipe((SmithingTransformRecipe) recipe);
            } else if (recipe instanceof SmithingTrimRecipe) {
                toAdd = CraftSmithingTrimRecipe.fromBukkitRecipe((SmithingTrimRecipe) recipe);
            } else if (recipe instanceof TransmuteRecipe) {
                toAdd = CraftTransmuteRecipe.fromBukkitRecipe((TransmuteRecipe) recipe);
            } else if (recipe instanceof ComplexRecipe) {
                throw new UnsupportedOperationException("Cannot add custom complex recipe");
            } else {
                return false;
            }
        }
        toAdd.addToCraftingManager();
        // Paper start - API for updating recipes on clients
        if (true || resendRecipes) { // Always needs to be resent now... TODO
            ((PlayerListBridge)this.playerList).cardboard$reloadRecipes();
        }
        // Paper end - API for updating recipes on clients
        return true;
    }

    @Override
    public Iterator<Advancement> advancementIterator() {
        return Iterators.unmodifiableIterator(Iterators.transform(server.getAdvancements().getAllAdvancements().iterator(), new Function<AdvancementHolder, org.bukkit.advancement.Advancement>() {
            @Override
            public Advancement apply(AdvancementHolder advancement) {
                return ((AdvancementHolderBridge)(Object) advancement).getBukkitAdvancement();
            }
        }));
    }

    @Override
    public void banIP(String ip) {
        getServer().getPlayerList().getIpBans().add(new IpBanListEntry(ip));
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
        ((RecipeManagerBridge)getServer().getRecipeManager()).cardboard$clearRecipes();
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
    public BlockData createBlockData(Material material, Consumer<? super BlockData> consumer) {
        BlockData data = createBlockData(material);
        if (consumer != null) consumer.accept(data);
        return data;
    }

    @Override
    /*
    public BlockData createBlockData(org.bukkit.Material material, String data) {
        Preconditions.checkArgument(material != null || data != null, "Must provide one of material or data");
        BlockType type = null;
        if (material != null) {
            type = material.asBlockType();
            Preconditions.checkArgument(type != null, "Provided material must be a block");
        }

        return CraftBlockData.newData(type, data);
    }*/
    
    public BlockData createBlockData(org.bukkit.Material material, String data) {
        Preconditions.checkArgument(material != null || data != null, "Must provide one of material or data");
        BlockType type = null;
        if (material != null) {
            type = material.asBlockType();
            Preconditions.checkArgument(type != null, "Provided material must be a block");
        }

        return CraftBlockData.fromString(type, data);
    }

    @Override
    public BossBar createBossBar(String title, BarColor color, BarStyle style, BarFlag... flags) {
        return new CardboardBossBar(title, color, style, flags);
    }

    @Override
    public KeyedBossBar createBossBar(NamespacedKey key, String title, BarColor barColor, BarStyle barStyle, BarFlag... barFlags) {
        Preconditions.checkArgument(key != null, "key");

        CustomBossEvent bossBattleCustom = this.getServer().getCustomBossEvents().create(net.minecraft.util.RandomSource.create(), CraftNamespacedKey.toMinecraft(key), CraftChatMessage.fromString(title, true)[0]);
        CardboardBossBar craftKeyedBossbar = new CardboardBossBar(bossBattleCustom);
        craftKeyedBossbar.setColor(barColor);
        craftKeyedBossbar.setStyle(barStyle);
        for (BarFlag flag : barFlags)
            craftKeyedBossbar.addFlag(flag);

        return craftKeyedBossbar;
    }

    @Override
    public Iterator<KeyedBossBar> getBossBars() {
        return Iterators.unmodifiableIterator(Iterators.transform(getServer().getCustomBossEvents().getEvents().iterator(), new Function<CustomBossEvent, org.bukkit.boss.KeyedBossBar>() {
            @Override
            public org.bukkit.boss.KeyedBossBar apply(CustomBossEvent bossBattleCustom) {
                return (KeyedBossBar) ((EntityBridge)bossBattleCustom).getBukkitEntity();
            }
        }));
    }

    @Override
    public KeyedBossBar getBossBar(NamespacedKey key) {
        Preconditions.checkArgument(key != null, "key");
        net.minecraft.server.bossevents.CustomBossEvent bossBattleCustom = getServer().getCustomBossEvents().get(CraftNamespacedKey.toMinecraft(key));
        return (bossBattleCustom == null) ? null : (KeyedBossBar) ((EntityBridge)bossBattleCustom).getBukkitEntity();
    }

    @Override
    public boolean removeBossBar(NamespacedKey key) {
        Preconditions.checkArgument(key != null, "key");
        net.minecraft.server.bossevents.CustomBossEvents bossBattleCustomData = getServer().getCustomBossEvents();
        net.minecraft.server.bossevents.CustomBossEvent bossBattleCustom = bossBattleCustomData.get(CraftNamespacedKey.toMinecraft(key));

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

    /*
    @Override
    public ChunkGenerator.ChunkData createChunkData(World world) {
        Preconditions.checkArgument(world != null, "World cannot be null");
        ServerWorld handle = ((CraftWorld) world).getHandle();
        return new ChunkDataImpl(world.getMinHeight(), world.getMaxHeight(), handle.getRegistryManager().lookupOrThrow(Registries.BIOME), world);
    }
    */

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

        ServerLevel worldServer = ((CraftWorld) world).getHandle();
        Location structureLocation = world.locateNearestStructure(location, structureType, radius, findUnexplored);
        BlockPos structurePosition = new BlockPos(structureLocation.getBlockX(), structureLocation.getBlockY(), structureLocation.getBlockZ());

        // Create map with trackPlayer = true, unlimitedTracking = true
        net.minecraft.world.item.ItemStack stack = MapItem.create(worldServer, structurePosition.getX(), structurePosition.getZ(), MapView.Scale.NORMAL.getValue(), true, true);
        MapItem.renderBiomePreviewMap(worldServer, stack);
        // "+" map ID taken from EntityVillager

        // method_8001
        // 1.19.2: getOrCreateMapState
        // 1.19.4: getMapState
        MapItem.getSavedData(stack, worldServer);
        // TODO: .addDecorationsNbt(stack, structurePosition, "+", net.minecraft.item.map.MapIcon.Type.byId(structureType.getMapIcon().getValue()));

        return CraftItemStack.asBukkitCopy(stack);
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
    public MapView createMap(World world) {
        Validate.notNull(world, "World cannot be null");

        net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(Items.MAP, 1);
        // MapState worldmap = FilledMapItem.getOrCreateMapState(stack, ((CraftWorld) world).getHandle());
        MapItemSavedData worldmap = MapItem.getSavedData(stack, ((CraftWorld) world).getHandle());
        return ((MapItemSavedDataBridge)worldmap).getMapViewBF();
    }

    @Override
    public Merchant createMerchant(String name) {
    	return new CraftMerchantCustom(name == null ? InventoryType.MERCHANT.getDefaultTitle() : name);
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
        Preconditions.checkState(this.console.getAllLevels().iterator().hasNext(), "Cannot create additional worlds on STARTUP");
        //Preconditions.checkState(!this.console.isIteratingOverLevels, "Cannot create a world while worlds are being ticked"); // Paper - Cat - Temp disable. We'll see how this goes.
        Preconditions.checkArgument(creator != null, "WorldCreator cannot be null");

        String name = creator.name();
        ChunkGenerator chunkGenerator = creator.generator();
        BiomeProvider biomeProvider = creator.biomeProvider();
        File folder = new File(this.getWorldContainer(), name);
        World world = this.getWorld(name);

        // Paper start
        World worldByKey = this.getWorld(creator.key());
        if (world != null || worldByKey != null) {
            if (world == worldByKey) {
                return world;
            }
            throw new IllegalArgumentException("Cannot create a world with key " + creator.key() + " and name " + name + " one (or both) already match a world that exists");
        }
        // Paper end

        if (folder.exists()) {
            Preconditions.checkArgument(folder.isDirectory(), "File (%s) exists and isn't a folder", name);
        }

        if (chunkGenerator == null) {
            chunkGenerator = this.getGenerator(name);
        }

        if (biomeProvider == null) {
            biomeProvider = this.getBiomeProvider(name);
        }

        ResourceKey<LevelStem> actualDimension = switch (creator.environment()) {
            case NORMAL -> LevelStem.OVERWORLD;
            case NETHER -> LevelStem.NETHER;
            case THE_END -> LevelStem.END;
            default -> throw new IllegalArgumentException("Illegal dimension (" + creator.environment() + ")");
        };

       /* LevelStorage.Session worldSession;
        try {
            worldSession = LevelStorage.create(getWorldContainer().toPath()).createSession(name);
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
                chunkgenerator, worlddata.getGeneratorOptions().isDebugWorld(), j, creator.environment() == Environment.NORMAL ? list : ImmutableList.of(), true);

        if (!(worlds.containsKey(name.toLowerCase(java.util.Locale.ENGLISH))))
            return null;

        ((IMixinMinecraftServer)server).initWorld(internal, worlddata, worlddata, worlddata.getGeneratorOptions());

        internal.setMobSpawnOptions(true, true);
        server.worlds.put(internal.getRegistryKey(), internal);

        pluginManager.callEvent(new WorldInitEvent(((IMixinWorld)internal).getCraftWorld()));

        ((IMixinMinecraftServer)getServer()).loadSpawn(internal.getChunkManager().threadedAnvilChunkStorage.worldGenerationProgressListener, internal);

        pluginManager.callEvent(new WorldLoadEvent(((IMixinWorld)internal).getCraftWorld()));
        return ((IMixinWorld)internal).getCraftWorld();*/
        return null;
    }

    /*public static DataPackSettings method_29735_(ResourcePackManager resourcePackManager) {
        // 1.20.4:  getNames
    	// 1.20.5+: getIds

    	Collection<String> collection = resourcePackManager.getEnabledNames();
        ImmutableList<String> list = ImmutableList.copyOf(collection);
        List<String> list2 = resourcePackManager.getNames().stream().filter(string -> !collection.contains(string)).collect(ImmutableList.toImmutableList());
        return new DataPackSettings(list, list2);
    }*/

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

    public BiomeProvider getBiomeProvider(String world) {
        ConfigurationSection section = this.configuration.getConfigurationSection("worlds");
        BiomeProvider result = null;

        if (section != null) {
            section = section.getConfigurationSection(world);

            if (section != null) {
                String name = section.getString("biome-provider");

                if (name != null && !name.isEmpty()) {
                    String[] split = name.split(":", 2);
                    String id = (split.length > 1) ? split[1] : null;
                    Plugin plugin = this.pluginManager.getPlugin(split[0]);

                    if (plugin == null) {
                        this.getLogger().severe("Could not set biome provider for default world '" + world + "': Plugin '" + split[0] + "' does not exist");
                    } else if (!plugin.isEnabled()) {
                        this.getLogger().severe("Could not set biome provider for default world '" + world + "': Plugin '" + plugin.getDescription().getFullName() + "' is not enabled yet (is it load:STARTUP?)");
                    } else {
                        try {
                            result = plugin.getDefaultBiomeProvider(world, id);
                            if (result == null) {
                                this.getLogger().severe("Could not set biome provider for default world '" + world + "': Plugin '" + plugin.getDescription().getFullName() + "' lacks a default world biome provider");
                            }
                        } catch (Throwable t) {
                            plugin.getLogger().log(Level.SEVERE, "Could not set biome provider for default world '" + world + "': Plugin '" + plugin.getDescription().getFullName(), t);
                        }
                    }
                }
            }
        }

        return result;
    }

	@Override
	public boolean dispatchCommand(CommandSender sender, String commandLine) throws CommandException {
		if(sender instanceof Entity) {
			ServerLevel world = (ServerLevel) ((CraftEntity) sender).getHandle().level();
			// A name-resolution source discards all command feedback, so commands whose
			// only result is text (/list, /help) appeared to do nothing. Players get a
			// real source that routes success/failure back to them.
			net.minecraft.world.entity.Entity handle = ((CraftEntity) sender).getHandle();
			CommandSourceStack source = (handle instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
					? serverPlayer.createCommandSourceStack()
					: handle.createCommandSourceStackForNameResolution(world);

			try {
				String theCommand;

				if(commandLine.startsWith("minecraft:")) {
					theCommand = commandLine.substring("minecraft:".length());
				} else {
					theCommand = commandLine;
				}

				int result = vanillaCommandManager.dispatcher.execute(theCommand, source);
				return result != -1;
			} catch(CommandSyntaxException e) {
				if(e.getType() != CommandSyntaxException
						.BUILT_IN_EXCEPTIONS
						.dispatcherUnknownCommand()) {
					source.sendFailure(ComponentUtils.fromMessage(e.getRawMessage()));
					if (e.getInput() != null && e.getCursor() >= 0) {
						int i = Math.min(e.getInput().length(), e.getCursor());
						MutableComponent mutableText = net.minecraft.network.chat.Component.empty().withStyle(ChatFormatting.GRAY);
								/*.styled((style) -> {
							return style.withClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, "/" + commandLine));
						});
						*/
						if (i > 10) {
							mutableText.append(CommonComponents.ELLIPSIS);
						}

						mutableText.append(e.getInput().substring(Math.max(0, i - 10), i));
						if (i < e.getInput().length()) {
							net.minecraft.network.chat.Component text = net.minecraft.network.chat.Component.literal(e.getInput().substring(i)).withStyle(ChatFormatting.RED, ChatFormatting.UNDERLINE);
							mutableText.append(text);
						}

						mutableText.append(net.minecraft.network.chat.Component.translatable("command.context.here").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
						source.sendFailure(mutableText);
					}

					return false;
				}
			}
		}

		if(commandMap.dispatch(sender, commandLine))
			return true;

		sender.sendMessage("Unknown command. Type \"/help\" for help.");
		return false;
	}

    @Override
    public Advancement getAdvancement(NamespacedKey arg0) {
        AdvancementHolder advancement = server.getAdvancements().get(CraftNamespacedKey.toMinecraft(arg0));
        return (advancement == null) ? null : ((AdvancementHolderBridge)(Object) advancement)
                .getBukkitAdvancement();
    }

    @Override
    public boolean getAllowEnd() {
        return this.configuration.getBoolean("settings.allow-end");
    }

    @Override
    public boolean getAllowFlight() {
        return getServer().allowFlight();
    }

    @Override
    public boolean getAllowNether() {

    	return true; // TODO

    	// return this.server.getProperties().allowNether;

        // return getServer().isNetherAllowed();
    }

    private DedicatedServerProperties getProperties() {
        return this.console.getProperties();
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
                return new IpBanList(server.playerList.getIpBans());
            case NAME:
            default:
                return new CraftProfileBanList(server.playerList.getBans());
        }
    }

    @Override
    public Set<OfflinePlayer> getBannedPlayers() {
        Set<OfflinePlayer> set = Sets.newHashSet();
        for (String s : getServer().getPlayerList().getBans().getUserList())
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
        return GameMode.getByValue(getServer().getDefaultGameType().getId());
    }

    @SuppressWarnings("resource")
    @Override
    public Entity getEntity(UUID uuid) {
        for (ServerLevel world : getServer().getAllLevels()) {
            net.minecraft.world.entity.Entity entity = world.getEntity(uuid);
            if (entity != null)
                return ((EntityBridge)entity).getBukkitEntity();
        }

        return null;
    }

    @Override
    public boolean getGenerateStructures() {
    	return this.getServer().getWorldGenSettings().options().generateStructures();
    }

    @Override
    public HelpMap getHelpMap() {
        return helpMap;
    }

    @Override
    public Set<String> getIPBans() {
        Set<String> set = Sets.newHashSet();
        for (String name : getServer().getPlayerList().getIpBans().getUserList())
            set.add(name);
        return set;
    }

    @Override
    public int getIdleTimeout() {
        return getServer().playerIdleTimeout;
    }

    @Override
    public String getIp() {
        return getServer().localIp;
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
    	ServerLevel overworld = server.getLevel(net.minecraft.world.level.Level.OVERWORLD);
    	me.isaiah.common.cmixin.IMixinWorld ic = (me.isaiah.common.cmixin.IMixinWorld) (Object) overworld;

    	MapItemSavedData worldmap = ic.IC$get_map_state(arg0);
        // MapState worldmap = server.getWorld(net.minecraft.world.World.OVERWORLD).getMapState("map_" + arg0);
        if (worldmap == null)
            return null;
        return ((MapItemSavedDataBridge)worldmap).getMapViewBF();
    }

    @Override
    public int getMaxPlayers() {
        return getServer().getMaxPlayers();
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
    public OfflinePlayer getOfflinePlayer(String name) {
        Preconditions.checkArgument(name != null, "name cannot be null");
        Preconditions.checkArgument(!name.isBlank(), "name cannot be empty");
        OfflinePlayer result = this.getPlayerExact(name);
        if (result == null) {
           NameAndId profile = null;
           //if (GlobalConfiguration.get().proxies.isProxyOnlineMode()) {
           if (this.getOnlineMode() || SpigotConfig.bungee) {
        	   profile = this.console.services().nameToIdCache().get(name).orElse(null);
           }

           if (profile == null) {
              result = this.getOfflinePlayer(NameAndId.createOffline(name));
           } else {
              result = this.getOfflinePlayer(profile);
           }
        } else {
           this.offlinePlayers.remove(result.getUniqueId());
        }

        return result;
     }

	public OfflinePlayer getOfflinePlayer(NameAndId nameAndId) {
		OfflinePlayer player = new CraftOfflinePlayer(this, nameAndId);
		this.offlinePlayers.put(nameAndId.id(), player);
		return player;
	}

    @Override
    public OfflinePlayer getOfflinePlayer(UUID id) {
        OfflinePlayer result = getPlayer(id);
        if (result == null) {
            result = offlinePlayers.get(id);
            if (result == null) {
                result = new CraftOfflinePlayer(this, new NameAndId(id, ""));
                offlinePlayers.put(id, result);
            }
        } else offlinePlayers.remove(id);

        return result;
    }

    @Override
    public OfflinePlayer[] getOfflinePlayers() {
        PlayerDataStorage storage = ((MinecraftServerBridge)server).getSaveHandler_BF();
        String[] files = storage.playerDir.list(new FilenameFilter() {
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
        return getServer().usesAuthentication();
    }

    @Override
    public Collection<? extends Player> getOnlinePlayers() {
        //return this.playerView;
        this.playerView = Collections.unmodifiableList(Lists.transform(server.playerList.players, new Function<ServerPlayer, CraftPlayer>() {
            @Override
            public CraftPlayer apply(ServerPlayer player) {
                return (CraftPlayer) ((EntityBridge)player).getBukkitEntity();
            }
        }));
        return this.playerView;
    }

    @Override
    public Set<OfflinePlayer> getOperators() {
        Set<OfflinePlayer> list = Sets.newHashSet();
        for (String op : getServer().getPlayerList().getOps().getUserList())
            list.add(getOfflinePlayer(op));
        return list;
    }

    @Override
    public Player getPlayer(String name) {
        return getPlayer(getServer().getPlayerList().getPlayerByName(name));
    }

    @Override
    public Player getPlayer(UUID uuid) {
        return getPlayer(getServer().getPlayerList().getPlayer(uuid));
    }

    public Player getPlayer(ServerPlayer e) {
        if (null == e)
            return null;
        return (Player) ((ServerPlayerBridge)(Object)e).getBukkitEntity();
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
    public List<Recipe> getRecipesFor(ItemStack result) {
        Validate.notNull(result, "Result cannot be null");

        List<Recipe> results = new ArrayList<>();
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
    public CraftScheduler getScheduler() {
        return scheduler;
    }

    @Override
    public CraftScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    @Override
    public ServicesManager getServicesManager() {
        return servicesManager;
    }

    @Override
    public String getShutdownMessage() {
        return "Server Shutdown";
    }

    @Override
    public int getSpawnRadius() {
        return getServer().spawnProtectionRadius();
    }

    public <T extends Keyed> Tag<T> getTag(String registry, NamespacedKey tag, Class<T> clazz) {
        Identifier key = CraftNamespacedKey.toMinecraft(tag);
        switch (registry) {
            case "blocks": {
              //  Preconditions.checkArgument((clazz == Material.class ? 1 : 0) != 0, (Object)"Block namespace must have material type");
                TagKey<Block> blockTagKey = TagKey.create(Registries.BLOCK, key);
                if (BuiltInRegistries.BLOCK.get(blockTagKey).isPresent()) {
                	return (Tag<T>) new CraftBlockTag((Registry<Block>)BuiltInRegistries.BLOCK, blockTagKey);
                }
                System.out.println("NULL BLOCKS! " + tag.toString());;
                break;
            }
            case "items": {
               // Preconditions.checkArgument((clazz == Material.class ? 1 : 0) != 0, (Object)"Item namespace must have material type");
                TagKey<Item> itemTagKey = TagKey.create(Registries.ITEM, key);
                if (BuiltInRegistries.ITEM.get(itemTagKey).isPresent()) {
                	return (Tag<T>) new CraftItemTag((Registry<Item>)BuiltInRegistries.ITEM, itemTagKey);
                }
                break;
            }
            case "fluids": {
              //  Preconditions.checkArgument((clazz == Fluid.class ? 1 : 0) != 0, (Object)"Fluid namespace must have fluid type");
                TagKey<Fluid> fluidTagKey = TagKey.create(Registries.FLUID, key);
                if (BuiltInRegistries.FLUID.get(fluidTagKey).isPresent()) {
                	return (Tag<T>) new CraftFluidTag((Registry<Fluid>)BuiltInRegistries.FLUID, fluidTagKey);
                }
                break;
            }
            case "entity_types": {
               // Preconditions.checkArgument((clazz == EntityType.class ? 1 : 0) != 0, (Object)"Entity type namespace must have entity type");
                TagKey<EntityType<?>> entityTagKey = TagKey.create(Registries.ENTITY_TYPE, key);
                if (BuiltInRegistries.ENTITY_TYPE.get(entityTagKey).isPresent()) {
                	return (Tag<T>) new CraftEntityTag((Registry<EntityType<?>>)BuiltInRegistries.ENTITY_TYPE, entityTagKey);
                }
                break;
            }
            case "game_events": {
                //Preconditions.checkArgument((clazz == GameEvent.class ? 1 : 0) != 0, (Object)"Game Event namespace must have GameEvent type");
                TagKey<GameEvent> gameEventTagKey = TagKey.create(Registries.GAME_EVENT, key);
                if (BuiltInRegistries.GAME_EVENT.get(gameEventTagKey).isPresent()) {
                	return (Tag<T>) new CraftGameEventTag((Registry<GameEvent>)BuiltInRegistries.GAME_EVENT, gameEventTagKey);
                }
                break;
            }
            default: {
                throw new IllegalArgumentException(registry);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Keyed> Iterable<Tag<T>> getTags(String registry, Class<T> clazz) {
        switch (registry) {
            case "blocks": {
              //  Preconditions.checkArgument((clazz == Material.class ? 1 : 0) != 0, (Object)"Block namespace must have material type");
                DefaultedRegistry<Block> blockTags = BuiltInRegistries.BLOCK;

                return (Iterable)(blockTags).getTags().map(pair -> new CraftBlockTag((Registry<Block>)blockTags, (TagKey)pair.key())).collect(ImmutableList.toImmutableList());
            }
            case "items": {
              //  Preconditions.checkArgument((clazz == Material.class ? 1 : 0) != 0, (Object)"Item namespace must have material type");
                DefaultedRegistry<Item> itemTags = BuiltInRegistries.ITEM;
                return (Iterable)(itemTags).getTags().map(pair -> new CraftItemTag((Registry<Item>)itemTags, (TagKey)pair.key())).collect(ImmutableList.toImmutableList());
            }
            case "fluids": {
              //  Preconditions.checkArgument((clazz == Material.class ? 1 : 0) != 0, (Object)"Fluid namespace must have fluid type");
                DefaultedRegistry<Fluid> fluidTags = BuiltInRegistries.FLUID;
                return (Iterable)(fluidTags).getTags().map(pair -> new CraftFluidTag((Registry<Fluid>)fluidTags, (TagKey)pair.key())).collect(ImmutableList.toImmutableList());
            }
            case "entity_types": {
              //  Preconditions.checkArgument((clazz == EntityType.class ? 1 : 0) != 0, (Object)"Entity type namespace must have entity type");
                DefaultedRegistry<EntityType<?>> entityTags = BuiltInRegistries.ENTITY_TYPE;
                return (Iterable)(entityTags).getTags().map(pair -> new CraftEntityTag((Registry<EntityType<?>>)entityTags, (TagKey)pair.key())).collect(ImmutableList.toImmutableList());
            }
            case "game_events": {
                // Preconditions.checkArgument((clazz == GameEvent.class ? 1 : 0) != 0);
                DefaultedRegistry<GameEvent> gameEvents = BuiltInRegistries.GAME_EVENT;
                return (Iterable)(gameEvents).getTags().map(pair -> new CraftGameEventTag((Registry<GameEvent>)gameEvents, pair.key())).collect(ImmutableList.toImmutableList());
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
        return getShortVersion();
    }

    @Override
    public int getViewDistance() {
        return server != null ? server.getProperties().viewDistance.get() : 12;
    }

    @Override
    public WarningState getWarningState() {
        return WarningState.DEFAULT;
    }

    @Override
    public int getWaterAnimalSpawnLimit() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Set<OfflinePlayer> getWhitelistedPlayers() {
        Set<OfflinePlayer> set = Sets.newHashSet();
        for (String name : getServer().getPlayerList().getWhiteList().getUserList())
            set.add(getOfflinePlayer(name));
        return set;
    }

    @Override
    public World getWorld(String name) {
        return worlds.get(name.toLowerCase(Locale.ROOT));
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
    	return this.getServer().storageSource.getLevelDirectory().path().getParent().toFile();
    }

    @Override
    public String getWorldType() {
        return server instanceof DedicatedServer ? ((DedicatedServer)getServer()).getProperties().properties.getProperty("level-type") : "NORMAL";
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
        boolean mainThread = server.isSameThread();
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
        this.minimumAPI = ApiVersion.getOrCreateVersion(this.configuration.getString("settings.minimum-api"));
        loadIcon();

        try {
            server.getPlayerList().getIpBans().load();
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Failed to load banned-ips.json, " + ex.getMessage());
        }
        try {
            server.getPlayerList().getBans().load();
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
        server.getPlayerList().reloadWhiteList();
    }

    @Override
    public void resetRecipes() {
        reloadData();
    }

    @Override
    public void savePlayers() {
        server.getPlayerList().saveAll();
    }

    @Override
    public List<Entity> selectEntities(CommandSender sender, String selector) {
        Preconditions.checkArgument(selector != null, "Selector cannot be null");
        Preconditions.checkArgument(sender != null, "Sender cannot be null");

        EntityArgument arg = EntityArgument.entities();
        List<? extends net.minecraft.world.entity.Entity> nms;

        try {
            StringReader reader = new StringReader(selector);
            nms = arg.parse(reader).findEntities(MinecraftCommandWrapper.getCommandSource(sender));
            Preconditions.checkArgument(!reader.canRead(), "Spurious trailing data in selector: " + selector);
        } catch (CommandSyntaxException ex) {
            throw new IllegalArgumentException("Could not parse selector: " + selector, ex);
        }
        return new ArrayList<>(Lists.transform(nms, (entity) -> ((EntityBridge)entity).getBukkitEntity()));
    }

    @Override
    public void setDefaultGameMode(GameMode gm) {
        server.setDefaultGameType(net.minecraft.world.level.GameType.byId(gm.getValue()));
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
        server.halt(false);
    }

    @Override
    public void unbanIP(String arg0) {
        server.getPlayerList().getIpBans().remove(arg0);
    }

    @Override
    public boolean unloadWorld(String name, boolean save) {
        return unloadWorld(getWorld(name), save);
    }

    @Override
    public boolean unloadWorld(World world, boolean save) {
        if (world == null) return false;

        ServerLevel handle = (ServerLevel) ((CraftWorld) world).getHandle();

        if (!(((MinecraftServerBridge)(Object)getServer()).getWorldMap().containsKey(handle.getLevel().dimension())))
            return false;

        if (handle.getLevel().dimension() == ServerLevel.OVERWORLD || handle.players().size() > 0)
            return false;

        WorldUnloadEvent e = new WorldUnloadEvent(world);
        pluginManager.callEvent(e);

        if (e.isCancelled()) return false;

        try {
            if (save) handle.save(null, true, true);
            handle.getChunkSource().close();
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }

        worlds.remove(world.getName().toLowerCase(java.util.Locale.ENGLISH));
        ((MinecraftServerBridge)(Object)getServer()).getWorldMap().remove(handle.getLevel().dimension());
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

    @Override
    public boolean removeRecipe(NamespacedKey recipeKey) {
        return this.removeRecipe(recipeKey, false);
    }

    @Override
    public boolean removeRecipe(NamespacedKey recipeKey, boolean resendRecipes) {
        Preconditions.checkArgument(recipeKey != null, "recipeKey == null");

        // Paper start - resend recipes on successful removal
        final ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> minecraftKey = CraftRecipe.toMinecraft(recipeKey);
        final boolean removed = ((RecipeManagerBridge)this.getServer().getRecipeManager()).cardboard$removeRecipe(minecraftKey);
        if (removed/* && resendRecipes*/) { // TODO Always need to resend them rn - deprecate this method?
            playerList_reloadRecipeData();
        }
        return removed;
        // Paper end - resend recipes on successful removal
    }

    public void playerList_reloadRecipeData() {
        RecipeManager craftingmanager = this.server.getRecipeManager();
        ClientboundUpdateRecipesPacket packetplayoutrecipeupdate = new ClientboundUpdateRecipesPacket(craftingmanager.getSynchronizedItemProperties(), craftingmanager.getSynchronizedStonecutterRecipes());
        for (ServerPlayer entityplayer : this.server.getPlayerList().players) {
            entityplayer.connection.send(packetplayoutrecipeupdate);
            entityplayer.getRecipeBook().sendInitialRecipeBook(entityplayer);
        }
    }

    /*
    @SuppressWarnings("resource")
    @Override
    public boolean removeRecipe(NamespacedKey recipeKey) {
        Preconditions.checkArgument(recipeKey != null, "recipeKey == null");

        Identifier mcKey = CraftNamespacedKey.toMinecraft(recipeKey);
        for (Map<Identifier, RecipeEntry<?>> recipes : ((IMixinRecipeManager)getServer().getRecipeManager()).getRecipes().values())
            if (recipes.remove(mcKey) != null)
                return true;

        return false;
    }
    */

    public List<String> tabComplete(CommandSender sender, String message, ServerLevel world, Vec3 position, boolean forceCommand) {
        if (!(sender instanceof Player))
            return ImmutableList.of();

        Player player = (Player) sender;
        List<String> offers = (message.startsWith("/") || forceCommand) ? tabCompleteCommand(player, message, world, position) : tabCompleteChat(player, message);

        TabCompleteEvent tabEvent = new TabCompleteEvent(player, message, offers);
        getPluginManager().callEvent(tabEvent);

        return tabEvent.isCancelled() ? Collections.emptyList() : tabEvent.getCompletions();
    }

    public List<String> tabCompleteCommand(Player player, String message, ServerLevel world, Vec3 pos) {
        List<String> completions = null;
        try {
            if (message.startsWith("/"))
                message = message.substring(1);

            completions = (pos == null) ? getCommandMap().tabComplete(player, message) :
                    getCommandMap().tabComplete(player, message, new Location(((LevelBridge)(Object)world).cardboard$getWorld(), pos.x, pos.y, pos.z));
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

    public DedicatedPlayerList getHandle() {
        return this.playerList;
    }

    @Override
    public SimpleCommandMap getCommandMap() {
        return commandMap;
    }

    // Because PlayerManager is broken
    @Deprecated
    public List<String> getOperatorList() throws IOException {
        File f = new File("ops.json");

        List<String> toreturn = new ArrayList<>();

        if (!f.exists()) {
        	return toreturn;
        }

        List<String> content = null;
        try {
            content = Files.readAllLines(f.toPath());
        } catch (IOException e) {throw e;}

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
        Optional<RecipeHolder<?>> opt = getServer().getRecipeManager().byKey(CraftRecipe.toMinecraft(recipeKey));

        return !opt.isPresent() ? null : ((RecipeHolderBridge)(Object) opt.get()).toBukkitRecipe();
    }

    public boolean dispatchServerCommand(CommandSender sender, ConsoleInput serverCommand) {
        if (sender instanceof Conversable) {
            Conversable conversable = (Conversable) sender;

            if (conversable.isConversing()) {
                conversable.acceptConversationInput(serverCommand.msg);
                return true;
            }
        }
        try {
            this.playerCommandState = true;
            return dispatchCommand(sender, serverCommand.msg);
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Unexpected exception while parsing console command \"" + serverCommand.msg + '"', ex);
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
        return new long[] {(long) server.tickCount};
    }

    public double getAverageTickTime() {
        return server.tickCount;
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
        return (player != null) ? new CraftPlayerProfile((CraftPlayer) player) : new CraftPlayerProfile(uuid, name);
    }

    @Override
    public int getCurrentTick() {
        return server.getTickCount();
    }

    @Override
    public int getMaxWorldSize() {
        // TODO Auto-generated method stub
        return ServerLevel.MAX_LEVEL_SIZE;
    }

    @Override
    public String getMinecraftVersion() {
        return server.getServerVersion();
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

    /** Bukkit's stock permission-denied text, overridable via bukkit.yml. */
    private static final String DEFAULT_PERMISSION_MESSAGE =
            "I'm sorry, but you do not have permission to perform this command. "
            + "Please contact the server administrators if you believe that this is in error.";

    @Override
    public String getPermissionMessage() {
        return this.configuration.getString("settings.permissions-message", DEFAULT_PERMISSION_MESSAGE);
    }

    @Override
    public UUID getPlayerUniqueId(String arg0) {
        return Bukkit.getPlayer(arg0).getUniqueId();
    }

    @Override
    public double[] getTPS() {
        return new double[] {server.tickCount};
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
    public @NotNull Merchant createMerchant(Component name) {
    	return new org.bukkit.craftbukkit.inventory.CraftMerchantCustom(name == null ? InventoryType.MERCHANT.defaultTitle() : name);
    }

    @Override
    public @NotNull DatapackManager getDatapackManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @Nullable World getWorld(@NotNull NamespacedKey key) {
        Identifier id = CraftNamespacedKey.toMinecraft(key);

        for (ServerLevel world : server.levels.values()) {
            Identifier name = world.dimension().identifier();
            if (name.equals(id))
                return ((LevelBridge)world).cardboard$getWorld();
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
        AbstractContainerMenu container = new AbstractContainerMenu(null, -1){

            //@Override
            public CraftInventoryView getBukkitView() {
                return null;
            }

            @Override
            public boolean stillValid(net.minecraft.world.entity.player.Player player) {
                return false;
            }

            public net.minecraft.world.item.ItemStack transferSlot(net.minecraft.world.entity.player.Player player, int index) {
                // TODO Auto-generated method stub
                return null;
            }

			// 1.19.4 @Override
			public net.minecraft.world.item.ItemStack quickMoveStack(net.minecraft.world.entity.player.Player player, int slot) {
				// TODO Auto-generated method stub
				return null;
			}
        };
        TransientCraftingContainer inventoryCrafting = new TransientCraftingContainer(container, 3, 3);
        Optional<RecipeHolder<CraftingRecipe>> opt = this.getNMSRecipe(craftingMatrix, inventoryCrafting, (CraftWorld)world);
        if (opt.isEmpty()) { return null; }

        return ((RecipeHolderBridge)(Object) opt.get()).toBukkitRecipe();
    }

    private Optional<RecipeHolder<CraftingRecipe>> getNMSRecipe(ItemStack[] craftingMatrix, TransientCraftingContainer inventoryCrafting, CraftWorld world) {
        Preconditions.checkArgument(craftingMatrix != null, "craftingMatrix must not be null");
        Preconditions.checkArgument(craftingMatrix.length == 9, "craftingMatrix must be an array of length 9");
        Preconditions.checkArgument(world != null, "world must not be null");
        int i = 0;
        while (i < craftingMatrix.length) {
            inventoryCrafting.setItem(i, CraftItemStack.asNMSCopy(craftingMatrix[i]));
            ++i;
        }
        return this.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, inventoryCrafting.asCraftInput(), world.getHandle());
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

	@Override
	public @NotNull CommandSender createCommandSender(@NotNull Consumer<? super Component> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PlayerProfile createPlayerProfile(UUID uuid) {
		return new CraftPlayerProfile(uuid, null);
	}

	@Override
	public PlayerProfile createPlayerProfile(String name) {
		return new CraftPlayerProfile(null, name);
	}

	@Override
	public PlayerProfile createPlayerProfile(UUID uuid, String name) {
		return new CraftPlayerProfile(uuid, name);
	}

	@Override
	public com.destroystokyo.paper.profile.@NotNull PlayerProfile createProfileExact(@Nullable UUID arg0,
			@Nullable String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull WorldBorder createWorldBorder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getHideOnlinePlayers() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public @NotNull PotionBrewer getPotionBrewer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull String getResourcePack() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull String getResourcePackHash() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull String getResourcePackPrompt() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getSimulationDistance() {
		// TODO Auto-generated method stub
		return 8;
	}

	@Override
	public int getSpawnLimit(@NotNull SpawnCategory arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getTicksPerSpawns(@NotNull SpawnCategory arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isResourcePackRequired() {
		return this.getServer().isResourcePackRequired();
	}

	@Override
	public int getMaxChainedNeighborUpdates() {
		return this.getServer().getMaxChainedNeighborUpdates();
	}

	@Override
	public <T extends Keyed> org.bukkit.@Nullable Registry<T> getRegistry(@NotNull Class<T> aClass) {
		return RegistryAccess.registryAccess().getRegistry(aClass);

		// Old: return (org.bukkit.Registry<T>) registries.computeIfAbsent(aClass, key -> CraftRegistry.createRegistry(aClass, console.getRegistryManager()));
	}

	@Override
	public @NotNull Criteria getScoreboardCriteria(@NotNull String arg0) {
		return CraftCriteria.getFromBukkit(arg0);
	}

	@Override
	public boolean isEnforcingSecureProfiles() {
        return this.getServer().enforceSecureProfile();
	}

	@Override
	public boolean isTickingWorlds() {
		// TODO Auto-generated method stub
		return true; // todo: paper api
	}

	@Override
	public @NotNull Component permissionMessage() {
		return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
				.deserialize(this.getPermissionMessage());
	}

	@Override
	public boolean shouldSendChatPreviews() {
		// TODO Auto-generated method stub
		return false;
	}

	// @Override
	@Deprecated(forRemoval = true)
	public @NotNull CraftDataPackManager getDataPackManager() {
        return this.dataPackManager;
	}

	@Override
	public @NotNull List<String> getInitialDisabledPacks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull List<String> getInitialEnabledPacks() {
		// TODO Auto-generated method stub
		return null;
	}

    public void setMotd(String motd) {
        this.console.setMotd(motd);
    }

    public void updateResources() {
        this.console.playerList.reloadResources();
    }

    public void updateRecipes() {
    	// TODO this.console.playerManager.reloadRecipeData();
    }

    // TODO: Tick Threads
    public final boolean isOwnedByCurrentRegion(World world, Position position) {
        return true;
    }

    public final boolean isOwnedByCurrentRegion(World world, Position position, int squareRadiusChunks) {
    	return true;
    }

    public final boolean isOwnedByCurrentRegion(Location location) {
    	return true;
    }

    public final boolean isOwnedByCurrentRegion(Location location, int squareRadiusChunks) {
    	return true;
    }

    public final boolean isOwnedByCurrentRegion(World world, int chunkX, int chunkZ) {
    	return true;
    }

    public final boolean isOwnedByCurrentRegion(World world, int chunkX, int chunkZ, int squareRadiusChunks) {
    	return true;
    }

    public final boolean isOwnedByCurrentRegion(org.bukkit.entity.Entity entity) {
    	return true;
    }

	@Override
	public @Nullable ItemStack createExplorerMap(@NotNull World world, @NotNull Location location,
			org.bukkit.generator.structure.@NotNull StructureType structureType,
			org.bukkit.map.MapCursor.@NotNull Type mapIcon, int radius, boolean findUnexplored) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void banIP(@NotNull InetAddress address) {
		((IpBanList)this.getBanList(BanList.Type.IP)).addBan(address, null, (java.util.Date)null, null);
	}

	@Override
	public void unbanIP(@NotNull InetAddress address) {
		((IpBanList)this.getBanList(BanList.Type.IP)).pardon(address);
	}

	@Override
	public void motd(@NotNull Component motd) {
		// TODO Auto-generated method stub

	}

	// 1.20.2 API:

	@Override
    public ItemStack craftItem(ItemStack[] craftingMatrix, World world) {
        return this.craftItemResult(craftingMatrix, world).getResult();
    }

	@Override
    public ItemCraftResult craftItemResult(ItemStack[] craftingMatrix, World world, Player player) {
        /*
		CraftWorld craftWorld = (CraftWorld)world;
        CraftPlayer craftPlayer = (CraftPlayer)player;
        CraftingScreenHandler container = new CraftingScreenHandler(-1, craftPlayer.getHandle().getInventory());
        CraftingInventory inventoryCrafting = container.craftSlots;
        CraftingResultInventory craftResult = container.result;
        Optional<RecipeEntry<CraftingRecipe>> recipe = this.getNMSRecipe(craftingMatrix, inventoryCrafting, craftWorld);
        net.minecraft.item.ItemStack itemstack = net.minecraft.item.ItemStack.EMPTY;
        if (recipe.isPresent()) {
            RecipeEntry<CraftingRecipe> recipeCrafting = recipe.get();
            if (craftResult.shouldCraftRecipe(craftWorld.getHandle(), craftPlayer.getHandle(), recipeCrafting)) {
                itemstack = recipeCrafting.value().craft(inventoryCrafting, craftWorld.getHandle().getRegistryManager());
            }
        }
        net.minecraft.item.ItemStack result = CraftEventFactory.callPreCraftEvent(inventoryCrafting, craftResult, itemstack, container.getBukkitView(), recipe.map(RecipeEntry::value).orElse(null) instanceof RepairItemRecipe);
        return this.createItemCraftResult(CraftItemStack.asBukkitCopy(result), inventoryCrafting, craftWorld.getHandle());
    	*/
		return null;
    }

	@Override
    public ItemCraftResult craftItemResult(ItemStack[] craftingMatrix, World world) {
        /*
		Preconditions.checkArgument((world != null ? 1 : 0) != 0, (Object)"world must not be null");
        CraftWorld craftWorld = (CraftWorld)world;

        RecipeInputInventory inventoryCrafting = this.createInventoryCrafting();
        Optional<RecipeEntry<CraftingRecipe>> recipe = this.getNMSRecipe(craftingMatrix, inventoryCrafting, craftWorld);
        net.minecraft.item.ItemStack itemStack = net.minecraft.item.ItemStack.EMPTY;
        if (recipe.isPresent()) {
            itemStack = recipe.get().value().craft(inventoryCrafting, craftWorld.getHandle().getRegistryManager());
        }
        return this.createItemCraftResult(CraftItemStack.asBukkitCopy(itemStack), inventoryCrafting, craftWorld.getHandle());
        */
        return null;
    }

	// 1.20.4 API:

	@Override
	public boolean isLoggingIPs() {
        return this.getServer().logIPs();
	}

	@Override
	public ServerTickManager getServerTickManager() {
		return this.serverTickManager;
	}

	@Override
	public ResourcePack getServerResourcePack() {
		return this.getServer().getServerResourcePack().map(CraftResourcePack::new).orElse(null);
	}

	@Override
	public <B extends BanList<E>, E> @NotNull B getBanList(BanListType<B> type) {
        if (type == BanListType.IP) {
            return (B)new IpBanList(this.playerList.getIpBans());
        }
        if (type == BanListType.PROFILE) {
            return (B)new CraftProfileBanList(this.playerList.getBans());
        }
        throw new IllegalArgumentException("Unknown BanListType: " + String.valueOf(type));
	}

	// 1.20.6 API:

	@Override
	public boolean isAcceptingTransfers() {
		return this.getServer().acceptsTransfers();
	}

	@Override
	public @NotNull EntityFactory getEntityFactory() {
		return CraftEntityFactory.instance();
	}

	@Override
	public World getWorld(Key worldKey) {
        ServerLevel worldServer = this.server.getLevel(ResourceKey.create(Registries.DIMENSION, PaperAdventure.asVanilla(worldKey)));
        if (worldServer == null) {
            return null;
        }
        return ((LevelBridge)worldServer).cardboard$getWorld();
    }

    // 1.21.4 API:
	@Override
    public boolean isPaused() {
        return this.console.isPaused();
		// TODO: return this.console.isTickPaused();
    }

    @Override
    public void allowPausing(final Plugin plugin, final boolean value) {
        // this.console.addPluginAllowingSleep(plugin.getName(), value);
    }

    @Override
    public int getPauseWhenEmptyTime() {
        return this.getProperties().pauseWhenEmptySeconds.get();
    }

    @Override
    public void setPauseWhenEmptyTime(int seconds) {
        // TODO
    	// this.getProperties().pauseWhenEmptySeconds = seconds;
    }

	@Override
	public boolean isOwnedByCurrentRegion(@NotNull World world, int minChunkX, int minChunkZ, int maxChunkX,
			int maxChunkZ) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isGlobalTickThread() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public @NotNull Merchant createMerchant() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void restart() {
		// TODO Auto-generated method stub
	}

	@Override
	public @NotNull ServerConfiguration getServerConfig() {
		return this.serverConfig;
	}

	@Override
	public boolean forcesDefaultGameMode() {
		return CraftServer.console.getProperties().forceGameMode.get();
	}

	@Override
	public @NotNull World getRespawnWorld() {
		return ((LevelBridge)this.console.findRespawnDimension()).cardboard$getWorld();
	}

    @Override
    public void setRespawnWorld(final World world) {
        Preconditions.checkArgument(world != null, "world cannot be null");

        ((PrimaryLevelDataBridge)this.console.overworld().serverLevelData).cardboard$setRespawnDimension(((CraftWorld) world).getHandle().dimension());
        this.console.updateEffectiveRespawnData();
    }

	@Override
	public @NotNull Path getLevelDirectory() {
		return this.getServer().storageSource.getLevelDirectory().path();
	}
    
    // public boolean isTickPaused() {
    //    return console.idleTickCount > 0 && console.idleTickCount >= console.getPauseWhenEmptySeconds() * 20;
    //}

}
