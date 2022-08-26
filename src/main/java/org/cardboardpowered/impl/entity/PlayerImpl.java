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
package org.cardboardpowered.impl.entity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.craftbukkit.CraftOfflinePlayer;
import org.bukkit.craftbukkit.CraftParticle;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftSound;
import org.bukkit.craftbukkit.CraftStatistic;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.scoreboard.CardboardScoreboard;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.event.player.PlayerKickEvent.Cause;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerUnregisterChannelEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.bukkit.scoreboard.Scoreboard;
import org.cardboardpowered.impl.AdvancementImpl;
import org.cardboardpowered.impl.AdvancementProgressImpl;
import org.cardboardpowered.impl.block.CardboardSign;

import com.destroystokyo.paper.ClientOption;
import com.destroystokyo.paper.Title;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.javazilla.bukkitfabric.BukkitFabricMod;
import org.cardboardpowered.impl.world.WorldImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.javazilla.bukkitfabric.interfaces.IMixinClientConnection;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinMinecraftServer;
import com.javazilla.bukkitfabric.interfaces.IMixinPlayNetworkHandler;
import com.javazilla.bukkitfabric.interfaces.IMixinPlayerManager;
import com.javazilla.bukkitfabric.interfaces.IMixinSignBlockEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;
import com.javazilla.bukkitfabric.nms.ReflectionRemapper;
import com.mojang.authlib.GameProfile;

import io.netty.buffer.Unpooled;
import me.isaiah.common.GameVersion;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ClearTitleS2CPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.WhitelistEntry;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.PacketByteBuf;

@DelegateDeserialization(CraftOfflinePlayer.class)
public class PlayerImpl extends CraftHumanEntity implements Player {

    private final Set<String> channels = new HashSet<String>();
    public ServerPlayerEntity nms;

    public PlayerImpl(ServerPlayerEntity entity) {
        super(entity);
        super.nms = entity;
        this.nms = entity;
    }

    @Override
    public ServerPlayerEntity getHandle() {
        return nms;
    }

    @Override
    public UUID getUniqueId() {
        return super.getUniqueId();
    }

    @Override
    public void abandonConversation(Conversation arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void abandonConversation(Conversation arg0, ConversationAbandonedEvent arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public void acceptConversationInput(String arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean beginConversation(Conversation arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isConversing() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void incrementStatistic(Statistic statistic) {
        CraftStatistic.incrementStatistic(getHandle().getStatHandler(), statistic);
    }

    @Override
    public void decrementStatistic(Statistic statistic) {
        CraftStatistic.decrementStatistic(getHandle().getStatHandler(), statistic);
    }

    @Override
    public int getStatistic(Statistic statistic) {
        return CraftStatistic.getStatistic(getHandle().getStatHandler(), statistic);
    }

    @Override
    public void incrementStatistic(Statistic statistic, int amount) {
        CraftStatistic.incrementStatistic(getHandle().getStatHandler(), statistic, amount);
    }

    @Override
    public void decrementStatistic(Statistic statistic, int amount) {
        CraftStatistic.decrementStatistic(getHandle().getStatHandler(), statistic, amount);
    }

    @Override
    public void setStatistic(Statistic statistic, int newValue) {
        CraftStatistic.setStatistic(getHandle().getStatHandler(), statistic, newValue);
    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material) {
        CraftStatistic.incrementStatistic(getHandle().getStatHandler(), statistic, material);
    }

    @Override
    public void decrementStatistic(Statistic statistic, Material material) {
        CraftStatistic.decrementStatistic(getHandle().getStatHandler(), statistic, material);
    }

    @Override
    public int getStatistic(Statistic statistic, Material material) {
        return CraftStatistic.getStatistic(getHandle().getStatHandler(), statistic, material);
    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material, int amount) {
        CraftStatistic.incrementStatistic(getHandle().getStatHandler(), statistic, material, amount);
    }

    @Override
    public void decrementStatistic(Statistic statistic, Material material, int amount) {
        CraftStatistic.decrementStatistic(getHandle().getStatHandler(), statistic, material, amount);
    }

    @Override
    public void setStatistic(Statistic statistic, Material material, int newValue) {
        CraftStatistic.setStatistic(getHandle().getStatHandler(), statistic, material, newValue);
    }

    @Override
    public void incrementStatistic(Statistic statistic, EntityType entityType) {
        CraftStatistic.incrementStatistic(getHandle().getStatHandler(), statistic, entityType);
    }

    @Override
    public void decrementStatistic(Statistic statistic, EntityType entityType) {
        CraftStatistic.decrementStatistic(getHandle().getStatHandler(), statistic, entityType);
    }

    @Override
    public int getStatistic(Statistic statistic, EntityType entityType) {
        return CraftStatistic.getStatistic(getHandle().getStatHandler(), statistic, entityType);
    }

    @Override
    public void incrementStatistic(Statistic statistic, EntityType entityType, int amount) {
        CraftStatistic.incrementStatistic(getHandle().getStatHandler(), statistic, entityType, amount);
    }

    @Override
    public void decrementStatistic(Statistic statistic, EntityType entityType, int amount) {
        CraftStatistic.decrementStatistic(getHandle().getStatHandler(), statistic, entityType, amount);
    }

    @Override
    public void setStatistic(Statistic statistic, EntityType entityType, int newValue) {
        CraftStatistic.setStatistic(getHandle().getStatHandler(), statistic, entityType, newValue);
    }

    @Override
    public long getFirstPlayed() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getLastPlayed() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void sendMessage(String message) {
        // nms.sendSystemMessage(new LiteralText(message), UUID.randomUUID());
    	nms.sendMessage(Text.literal(message));
    }

    @Override
    public PlayerImpl getPlayer() {
        return this;
    }

    @Override
    public boolean hasPlayedBefore() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isBanned() {
        return getServer().getBanList(org.bukkit.BanList.Type.NAME).isBanned(getName());
    }

    @Override
    public String getName() {
        return nms.getEntityName();
    }

    @Override
    public boolean isOnline() {
        return getServer().getPlayer(getUniqueId()) != null;
    }

    @Override
    public boolean isWhitelisted() {
        return CraftServer.server.getPlayerManager().isWhitelisted(nms.getGameProfile());
    }

    @Override
    public void setWhitelisted(boolean arg0) {
        if (arg0)
            nms.getServer().getPlayerManager().getWhitelist().add(new WhitelistEntry(nms.getGameProfile()));
        else nms.getServer().getPlayerManager().getWhitelist().remove(nms.getGameProfile());
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("name", getName());
        return result;
    }

    public void addChannel(String channel) {
        Preconditions.checkState(channels.size() < 128, "Cannot register channel '%s'. Too many channels registered!", channel);
        channel = StandardMessenger.validateAndCorrectChannel(channel);
        if (channels.add(channel))
            server.getPluginManager().callEvent(new PlayerRegisterChannelEvent(this, channel));
    }

    public void removeChannel(String channel) {
        channel = StandardMessenger.validateAndCorrectChannel(channel);
        if (channels.remove(channel))
            server.getPluginManager().callEvent(new PlayerUnregisterChannelEvent(this, channel));
    }

    @Override
    public Set<String> getListeningPluginChannels() {
        return ImmutableSet.copyOf(channels);
    }

    public void sendSupportedChannels() {
        if (getHandle().networkHandler == null) return;
        Set<String> listening = server.getMessenger().getIncomingChannels();

        if (!listening.isEmpty()) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            for (String channel : listening) {
                try {
                    stream.write(channel.getBytes("UTF8"));
                    stream.write((byte) 0);
                } catch (IOException ex) {
                    BukkitFabricMod.LOGGER.log(Level.SEVERE, "Could not send Plugin Channel REGISTER to " + getName(), ex);
                }
            }

            getHandle().networkHandler.sendPacket(new CustomPayloadS2CPacket(new Identifier("register"), new PacketByteBuf(Unpooled.wrappedBuffer(stream.toByteArray()))));
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void sendPluginMessage(Plugin source, String channel, byte[] message) {
        StandardMessenger.validatePluginMessage(Bukkit.getMessenger(), source, channel, message);
        if (getHandle().networkHandler == null) return;

        if (channels.contains(channel)) {
            channel = StandardMessenger.validateAndCorrectChannel(channel);
            CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(new Identifier(channel), new PacketByteBuf(Unpooled.wrappedBuffer(message)));
            getHandle().networkHandler.sendPacket(packet);
        }
    }

    @Override
    public boolean canSee(Player arg0) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void chat(String message) {
        ((IMixinPlayNetworkHandler)(Object)nms.networkHandler).chat(message, false);
    }

    @Override
    public InetSocketAddress getAddress() {
        if (nms.networkHandler == null) return null;

        SocketAddress addr = getHandle().networkHandler.connection.getAddress();
        return addr instanceof InetSocketAddress ? (InetSocketAddress) addr : null;
    }

    @Override
    public org.bukkit.advancement.AdvancementProgress getAdvancementProgress(org.bukkit.advancement.Advancement advancement) {
        Preconditions.checkArgument(advancement != null, "advancement");

        AdvancementImpl craft = (AdvancementImpl) advancement;
        PlayerAdvancementTracker data = getHandle().getAdvancementTracker();
        net.minecraft.advancement.AdvancementProgress progress = data.getProgress(craft.getHandle());

        return new AdvancementProgressImpl(craft, data, progress);
    }

    @Override
    public boolean getAllowFlight() {
        return getHandle().getAbilities().allowFlying;
    }

    @Override
    public int getClientViewDistance() {
        return Bukkit.getViewDistance(); // TODO Get Client view distance not server
    }

    @Override
    public Location getCompassTarget() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDisplayName() {
    	
    	
        return (null == nms.getCustomName()) ? this.getName() : nms.getCustomName().getString();
    }

    @Override
    public float getExhaustion() {
        return nms.getHungerManager().exhaustion;
    }

    @Override
    public float getExp() {
        return nms.experienceProgress;
    }

    @Override
    public float getFlySpeed() {
        return nms.airStrafingSpeed;
    }

    @Override
    public int getFoodLevel() {
        return nms.getHungerManager().getFoodLevel();
    }

    @Override
    public double getHealthScale() {
        // TODO Auto-generated method stub
        return 20;
    }

    @Override
    public int getLevel() {
        return nms.experienceLevel;
    }

    @Override
    public String getLocale() {
        return "en_US"; // TODO
    }

    @Override
    public String getPlayerListFooter() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPlayerListHeader() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPlayerListName() {
        return getHandle().getPlayerListName() == null ? getName() : CraftChatMessage.fromComponent(getHandle().getPlayerListName());
    }

    @Override
    public long getPlayerTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getPlayerTimeOffset() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public WeatherType getPlayerWeather() {
        // TODO Auto-generated method stub
        return WeatherType.CLEAR;
    }

    @Override
    public float getSaturation() {
        return nms.getHungerManager().getSaturationLevel();
    }

    @Override
    public CardboardScoreboard getScoreboard() {
        return server.getScoreboardManager().getPlayerBoard(this);
    }

    @Override
    public Entity getSpectatorTarget() {
        net.minecraft.entity.Entity followed = getHandle().getCameraEntity();
        return followed == getHandle() ? null : ((IMixinEntity)followed).getBukkitEntity();
    }

    @Override
    public int getTotalExperience() {
        return nms.totalExperience;
    }

    @Override
    public float getWalkSpeed() {
        return nms.forwardSpeed;
    }

    @Override
    public void giveExp(int arg0) {
        nms.addExperience(arg0);
    }

    @Override
    public void giveExpLevels(int arg0) {
        nms.addExperienceLevels(arg0);
    }

    @Override
    public void hidePlayer(Player arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void hidePlayer(Plugin arg0, Player arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean isFlying() {
        return nms.getAbilities().flying;
    }

    @Override
    public boolean isHealthScaled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isPlayerTimeRelative() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSleepingIgnored() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSneaking() {
        return nms.isSneaking();
    }

    @Override
    public boolean isSprinting() {
        return nms.isSprinting();
    }

    @Override
    public void kickPlayer(String arg0) {
        nms.networkHandler.disconnect(Text.of(arg0));
    }

    @Override
    public void loadData() {
        ((IMixinMinecraftServer)CraftServer.server).getSaveHandler_BF().loadPlayerData(nms);
    }

    @Override
    public void openBook(ItemStack book) {
        ItemStack hand = getInventory().getItemInMainHand();
        getInventory().setItemInMainHand(book);
        getHandle().useBook(org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(book), net.minecraft.util.Hand.MAIN_HAND);
        getInventory().setItemInMainHand(hand);
    }

    @Override
    public boolean performCommand(String arg0) {
        return getServer().dispatchCommand(this, arg0);
    }

    @Override
    public void playEffect(Location loc, Effect effect, int data) {
        if (getHandle().networkHandler == null) return;

        int packetData = effect.getId();
        WorldEventS2CPacket packet = new WorldEventS2CPacket(packetData, new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), data, false);
        getHandle().networkHandler.sendPacket(packet);
    }

    @Override
    public <T> void playEffect(Location arg0, Effect arg1, T arg2) {
        // TODO Auto-generated method stub
    }

    @Override
    public void playNote(Location loc, byte instrument, byte note) {
        if (getHandle().networkHandler == null) return;

        String name = null;
        switch (instrument) {
        case 0:
            name = "harp";
            break;
        case 1:
            name = "basedrum";
            break;
        case 2:
            name = "snare";
            break;
        case 3:
            name = "hat";
            break;
        case 4:
            name = "bass";
            break;
        case 5:
            name = "flute";
            break;
        case 6:
            name = "bell";
            break;
        case 7:
            name = "guitar";
            break;
        case 8:
            name = "chime";
            break;
        case 9:
            name = "xylophone";
            break;
        }

        float f = (float) Math.pow(2.0D, (note - 12.0D) / 12.0D);
     // TODO: 1.19
        //getHandle().networkHandler.sendPacket(new PlaySoundS2CPacket(CraftSound.getSoundEffect("block.note_block." + name), net.minecraft.sound.SoundCategory.RECORDS, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), 3.0f, f));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void playNote(Location loc, Instrument instrument, Note note) {
        if (getHandle().networkHandler == null) return;

        String instrumentName = null;
        switch (instrument.ordinal()) {
            case 0:
                instrumentName = "harp";
                break;
            case 1:
                instrumentName = "basedrum";
                break;
            case 2:
                instrumentName = "snare";
                break;
            case 3:
                instrumentName = "hat";
                break;
            case 4:
                instrumentName = "bass";
                break;
            case 5:
                instrumentName = "flute";
                break;
            case 6:
                instrumentName = "bell";
                break;
            case 7:
                instrumentName = "guitar";
                break;
            case 8:
                instrumentName = "chime";
                break;
            case 9:
                instrumentName = "xylophone";
                break;
            case 10:
                instrumentName = "iron_xylophone";
                break;
            case 11:
                instrumentName = "cow_bell";
                break;
            case 12:
                instrumentName = "didgeridoo";
                break;
            case 13:
                instrumentName = "bit";
                break;
            case 14:
                instrumentName = "banjo";
                break;
            case 15:
                instrumentName = "pling";
                break;
            case 16:
                instrumentName = "xylophone";
                break;
        }
        float f = (float) Math.pow(2.0D, (note.getId() - 12.0D) / 12.0D);
        // TODO: 1.19
        //getHandle().networkHandler.sendPacket(new PlaySoundS2CPacket(CraftSound.getSoundEffect("block.note_block." + instrumentName), net.minecraft.sound.SoundCategory.RECORDS, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), 3.0f, f));
    }

    @Override
    public void playSound(Location loc, Sound sound, float volume, float pitch) {
        playSound(loc, sound, org.bukkit.SoundCategory.MASTER, volume, pitch);
    }

    @Override
    public void playSound(Location loc, String sound, float volume, float pitch) {
        playSound(loc, sound, org.bukkit.SoundCategory.MASTER, volume, pitch);
    }

    @Override
    public void playSound(Location loc, Sound sound, org.bukkit.SoundCategory category, float volume, float pitch) {
        if (loc == null || sound == null || category == null || getHandle().networkHandler == null) return;

     // TODO: 1.19
        //PlaySoundS2CPacket packet = new PlaySoundS2CPacket(CraftSound.getSoundEffect(CraftSound.getSound(sound)), net.minecraft.sound.SoundCategory.valueOf(category.name()), loc.getX(), loc.getY(), loc.getZ(), volume, pitch);
       // getHandle().networkHandler.sendPacket(packet);
    }

    @Override
    public void playSound(Location loc, String sound, org.bukkit.SoundCategory category, float volume, float pitch) {
        if (loc == null || sound == null || category == null || getHandle().networkHandler == null) return;
     // TODO: 1.19
       // PlaySoundIdS2CPacket packet = new PlaySoundIdS2CPacket(new Identifier(sound), net.minecraft.sound.SoundCategory.valueOf(category.name()), new Vec3d(loc.getX(), loc.getY(), loc.getZ()), volume, pitch);
       // getHandle().networkHandler.sendPacket(packet);
    }

    @Override
    public void resetPlayerTime() {
        // TODO Auto-generated method stub
    }

    @Override
    public void resetPlayerWeather() {
        // TODO Auto-generated method stub
    }

    @Override
    public void resetTitle() {
        nms.networkHandler.sendPacket(new ClearTitleS2CPacket(true));
    }

    @Override
    public void saveData() {
        ((IMixinMinecraftServer)CraftServer.server).getSaveHandler_BF().savePlayerData(nms);
    }

    @Override
    public void sendBlockChange(Location loc, BlockData block) {
        if (getHandle().networkHandler == null) return;

        BlockUpdateS2CPacket packet = new BlockUpdateS2CPacket(new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), ((CraftBlockData) block).getState());
        getHandle().networkHandler.sendPacket(packet);
    }

    @Override
    public void sendBlockChange(Location loc, Material material, byte data) {
        if (getHandle().networkHandler == null) return;

        BlockUpdateS2CPacket packet = new BlockUpdateS2CPacket(new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), CraftMagicNumbers.getBlock(material, data));
        getHandle().networkHandler.sendPacket(packet);
    }

    @Override
    public boolean sendChunkChange(Location arg0, int arg1, int arg2, int arg3, byte[] arg4) {
        throw new NotImplementedException("Also not in Spigot");
    }

    @Override
    public void sendExperienceChange(float progress) {
        sendExperienceChange(progress, getLevel());
    }

    @Override
    public void sendExperienceChange(float progress, int level) {
        if (getHandle().networkHandler == null) return;

        ExperienceBarUpdateS2CPacket packet = new ExperienceBarUpdateS2CPacket(progress, getTotalExperience(), level);
        getHandle().networkHandler.sendPacket(packet);
    }

    @Override
    public void sendMap(MapView map) {
      // TODO 1.17ify
        /*if (getHandle().networkHandler == null) return;

        RenderData data = ((MapViewImpl) map).render(this);
        Collection<MapIcon> icons = new ArrayList<MapIcon>();
        for (MapCursor cursor : data.cursors) {
            if (cursor.isVisible())
                icons.add(new MapIcon(MapIcon.Type.byId(cursor.getRawType()), cursor.getX(), cursor.getY(), cursor.getDirection(), CraftChatMessage.fromStringOrNull(cursor.getCaption())));
        }

        MapUpdateS2CPacket packet = new MapUpdateS2CPacket(map.getId(), map.getScale().getValue(), true, map.isLocked(), icons, data.buffer, 0, 0, 128, 128);
        getHandle().networkHandler.sendPacket(packet);*/
    }

    @Override
    public void sendRawMessage(String arg0) {
        if (getHandle().networkHandler == null) return;

        //for (Text component : CraftChatMessage.fromString(arg0))
        //    getHandle().networkHandler.sendPacket(new GameMessageS2CPacket(component, MessageType.CHAT, Util.NIL_UUID));
        
        for (Text component : CraftChatMessage.fromString(arg0)) {
            this.getHandle().sendMessage(component);
        }
    }

    @Override
    public void sendSignChange(Location loc, String[] lines) {
       sendSignChange(loc, lines, DyeColor.BLACK);
    }

    @Override
    public void sendSignChange(Location loc, String[] lines, DyeColor dyeColor) {
        if (getHandle().networkHandler == null) return;
        if (lines == null) lines = new String[4];
        if (lines.length < 4)
            throw new IllegalArgumentException("Must have at least 4 lines");

        Text[] components = CardboardSign.sanitizeLines(lines);
        SignBlockEntity sign = new SignBlockEntity(new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), null);
        //sign.setPos(new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        sign.setTextColor(net.minecraft.util.DyeColor.byId(dyeColor.getWoolData()));
        System.arraycopy(components, 0, ((IMixinSignBlockEntity)sign).getTextBF(), 0, ((IMixinSignBlockEntity)sign).getTextBF().length);

        getHandle().networkHandler.sendPacket(sign.toUpdatePacket());
    }

    @Override
    public void sendTitle(String arg0, String arg1) {
        sendTitle(arg0, arg1, 10, 70, 20);
    }

    @Override
    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        TitleFadeS2CPacket times = new TitleFadeS2CPacket(fadeIn, stay, fadeOut);
        this.getHandle().networkHandler.sendPacket(times);
        if (title != null) {
            TitleS2CPacket packetTitle = new TitleS2CPacket(CraftChatMessage.fromStringOrNull(title));
            this.getHandle().networkHandler.sendPacket(packetTitle);
        }
        if (subtitle != null) {
            SubtitleS2CPacket packetSubtitle = new SubtitleS2CPacket(CraftChatMessage.fromStringOrNull(subtitle));
            this.getHandle().networkHandler.sendPacket(packetSubtitle);
        }
    }

    @Override
    public void setAllowFlight(boolean arg0) {
        if (isFlying() && !arg0)
            getHandle().getAbilities().flying = false;

        getHandle().getAbilities().allowFlying = arg0;
        getHandle().sendAbilitiesUpdate();
    }

    @Override
    public void setCompassTarget(Location arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setDisplayName(String arg0) {
        nms.setCustomNameVisible(true);
        nms.setCustomName(Text.literal(arg0));
    }

    @Override
    public void setExhaustion(float arg0) {
        nms.addExhaustion(arg0);
    }

    @Override
    public void setExp(float arg0) {
        nms.setExperiencePoints((int) arg0);
    }

    @Override
    public void setFlySpeed(float arg0) throws IllegalArgumentException {
        nms.airStrafingSpeed = arg0;
    }

    @Override
    public void setFlying(boolean arg0) {
        if (!getAllowFlight() && arg0)
            throw new IllegalArgumentException("getAllowFlight() is false, cannot set player flying");

        getHandle().getAbilities().flying = arg0;
        getHandle().sendAbilitiesUpdate();
    }

    @Override
    public void setFoodLevel(int arg0) {
        nms.getHungerManager().setFoodLevel(arg0);
    }

    @Override
    public void setHealthScale(double arg0) throws IllegalArgumentException {
        // TODO Auto-generated method stub
    }

    @Override
    public void setHealthScaled(boolean arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setLevel(int level) {
        nms.setExperienceLevel(level);
    }

    @Override
    public void setPlayerListFooter(String arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setPlayerListHeader(String arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setPlayerListHeaderFooter(String arg0, String arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setPlayerListName(String arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setPlayerTime(long arg0, boolean arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setPlayerWeather(WeatherType arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setResourcePack(String url) {
        nms.sendResourcePackUrl(url, "null", false, null);
    }

    @Override
    public void setResourcePack(String url, byte[] hash) {
        nms.sendResourcePackUrl(url, new String(hash), false, null);
    }

    @Override
    public void setSaturation(float arg0) {
        nms.getHungerManager().setSaturationLevel(arg0);
    }

    @Override
    public void setScoreboard(Scoreboard scoreboard) {
        ServerPlayNetworkHandler playerConnection = getHandle().networkHandler;
        if (playerConnection == null) throw new IllegalStateException("Cannot set scoreboard yet");
        if (((IMixinPlayNetworkHandler)playerConnection).isDisconnected())
            throw new IllegalStateException("Cannot set scoreboard for invalid PlayerImpl");

        this.server.getScoreboardManager().setPlayerBoard(this, scoreboard);
    }

    @Override
    public void setSleepingIgnored(boolean arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setSneaking(boolean arg0) {
        nms.setSneaking(arg0);
    }

    @Override
    public void setSpectatorTarget(Entity arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setSprinting(boolean arg0) {
        nms.setSprinting(arg0);
    }

    @Override
    public void setTexturePack(String arg0) {
        setResourcePack(arg0);
    }

    @Override
    public void setTotalExperience(int arg0) {
        nms.totalExperience = arg0;
    }

    @Override
    public void setWalkSpeed(float arg0) throws IllegalArgumentException {
        nms.getAbilities().setWalkSpeed(arg0);
    }

    @Override
    public void showPlayer(Player arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void showPlayer(Plugin arg0, Player arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count);
    }

    @Override
    public void spawnParticle(Particle particle, double x, double y, double z, int count) {
        spawnParticle(particle, x, y, z, count, null);
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, T data) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, data);
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, T data) {
        spawnParticle(particle, x, y, z, count, 0, 0, 0, data);
    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ);
    }

    @Override
    public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {
        spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, null);
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, T data) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, data);
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, T data) {
        spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, 1, data);
    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, extra);
    }

    @Override
    public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra) {
        spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, extra, null);
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, extra, data);
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {
        if (data != null && !particle.getDataType().isInstance(data))
            throw new IllegalArgumentException("data should be " + particle.getDataType() + " got " + data.getClass());
        ParticleS2CPacket packetplayoutworldparticles = new ParticleS2CPacket(CraftParticle.toNMS(particle, data), true, (float) x, (float) y, (float) z, (float) offsetX, (float) offsetY, (float) offsetZ, (float) extra, count);
        getHandle().networkHandler.sendPacket(packetplayoutworldparticles);

    }

    @Override
    public void stopSound(Sound sound) {
        stopSound(sound, null);
    }

    @Override
    public void stopSound(String sound) {
        stopSound(sound, null);
    }

    @Override
    public void stopSound(Sound sound, org.bukkit.SoundCategory category) {
        stopSound(CraftSound.getSound(sound), category);
    }

    @Override
    public void stopSound(String sound, org.bukkit.SoundCategory category) {
        if (getHandle().networkHandler == null) return;

        getHandle().networkHandler.sendPacket(new StopSoundS2CPacket(new Identifier(sound), category == null ? net.minecraft.sound.SoundCategory.MASTER : net.minecraft.sound.SoundCategory.valueOf(category.name())));
    }

    @Override
    public void updateCommands() {
        if (getHandle().networkHandler == null) return;

        nms.server.getCommandManager().sendCommandTree(nms);
    }

    @Override
    public void updateInventory() {
        this.getHandle().currentScreenHandler.syncState();
    }

    @SuppressWarnings("deprecation")
    @Override
    public GameMode getGameMode() {
        return GameMode.getByValue(getHandle().interactionManager.getGameMode().getId());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setGameMode(GameMode mode) {
        if (getHandle().networkHandler == null) return;

        if (mode == null)
            throw new IllegalArgumentException("GameMode cannot be null");

        getHandle().changeGameMode(net.minecraft.world.GameMode.byId(mode.getValue()));
    }

    public GameProfile getProfile() {
        Optional<GameProfile> opt = CraftServer.getUC().card_getByUuid(getUniqueId());

        // TODO Add a GameProfile API to iCommon
        return opt.isEmpty() ? null : opt.get();
    }

    @Override
    public boolean isOp() {
        try {
            return CraftServer.server.getPlayerManager().isOperator(getProfile());
        } catch (NullPointerException e) {
            try {
                return CraftServer.INSTANCE.getOperatorList().contains(getUniqueId().toString());
            } catch (IOException ex) {
                GameProfile gp = new GameProfile(super.getUniqueId(), this.getName());
                return CraftServer.server.getPlayerManager().isOperator(gp);
            }
        }
    }

    @Override
    public void setOp(boolean value) {
        if (value == isOp()) return;

        if (value)
             nms.server.getPlayerManager().addToOperators(nms.getGameProfile());
        else nms.server.getPlayerManager().removeFromOperators(nms.getGameProfile());

        perm.recalculatePermissions();
    }

    @Override
    public boolean teleport(Location location, PlayerTeleportEvent.TeleportCause cause) {
        Preconditions.checkArgument(location != null, "location");
        Preconditions.checkArgument(location.getWorld() != null, "location.world");
        location.checkFinite();
        ServerPlayerEntity entity = getHandle();

        if (getHealth() == 0 || entity.isRemoved() || entity.networkHandler == null || entity.hasPassengers())
            return false;

        Location from = this.getLocation();
        Location to = location;

        PlayerTeleportEvent event = new PlayerTeleportEvent(this, from, to, cause);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return false;

        entity.stopRiding();

        from = event.getFrom();
        to = event.getTo();

        ServerWorld toWorld = (ServerWorld) ((WorldImpl) to.getWorld()).getHandle();

        if (getHandle().inventory != getHandle().inventory)
            getHandle().closeHandledScreen();
        
        System.out.println("Hello! " + from.getWorld() + " / " + to.getWorld());

        if (from.getWorld().equals(to.getWorld()))
             ((IMixinPlayNetworkHandler)(Object)entity.networkHandler).teleport(to);
        else {
            //entity.moveToWorld(toWorld);
            //entity.teleport
            
            ((IMixinPlayerManager)(PlayerManager)CraftServer.server.getPlayerManager()).moveToWorld(entity, toWorld, true, to, true);
        }

        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OfflinePlayer))
            return false;

        OfflinePlayer other = (OfflinePlayer) obj;
        if ((this.getUniqueId() == null) || (other.getUniqueId() == null))
            return false;

        boolean uuidEquals = this.getUniqueId().equals(other.getUniqueId());
        boolean idEquals = true;

        if (other instanceof PlayerImpl)
            idEquals = this.getEntityId() == ((PlayerImpl) other).getEntityId();

        return uuidEquals && idEquals;
    }

    public InetSocketAddress getRawAddress_BF() {
        if (Bukkit.getPluginManager().isPluginEnabled("ProtocolSupport")) {
            System.out.println("PS getRawAddress");
            try {
                Class<?> ps = ReflectionRemapper.getClassFromJPL("protocolsupport.zplatform.impl.fabric.FabricMiscUtils");
                HashMap<InetSocketAddress, InetSocketAddress> map = (HashMap<InetSocketAddress, InetSocketAddress>) ps.getField("rawAddressMap").get(null);
                return map.get(this.getAddress());
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return (InetSocketAddress) ((IMixinClientConnection) (nms.networkHandler.connection)).getRawAddress();
    }

    private final Player.Spigot spigot = new Player.Spigot() {

        int err = 0;

        @Override
        public InetSocketAddress getRawAddress() {
            try {
                return getRawAddress_BF();
            } catch (NullPointerException ex) {
                // What is going on?
                if (err > 3) {
                    System.exit(0);
                    return null;
                } 
                err++;
                ex.printStackTrace();
                return ((java.net.InetSocketAddress)PlayerImpl.this.getAddress());
            }
        }

        @Override
        public boolean getCollidesWithEntities() {
            return PlayerImpl.this.isCollidable();
        }

        @Override
        public void setCollidesWithEntities(boolean collides) {
            PlayerImpl.this.setCollidable(collides);
        }

        @Override
        public void respawn() {
            if (getHealth() <= 0 && isOnline())
                nms.getServer().getPlayerManager().respawnPlayer( getHandle(), false );
        }

        @Override
        public Set<Player> getHiddenPlayers() {
            return java.util.Collections.emptySet();
        }

        @Override
        public void sendMessage(BaseComponent component) {
            sendMessage(new BaseComponent[] { component });
        }

        @Override
        public void sendMessage(BaseComponent... components) {
           if (null == getHandle().networkHandler) return;

           	// TODO: 1.19
            //GameMessageS2CPacket packet = new GameMessageS2CPacket(null, MessageType.SYSTEM, nms.getUuid());
            //((IGameMessagePacket)packet).setBungeeComponents(components);
            //getHandle().networkHandler.sendPacket(packet);
            
            getHandle().sendMessage( Text.literal( BaseComponent.toLegacyText(components) ) );
        }

        @Override
        public void sendMessage(net.md_5.bungee.api.ChatMessageType position, BaseComponent component) {
            sendMessage( position, new BaseComponent[] { component } );
        }

        @Override
        public void sendMessage(net.md_5.bungee.api.ChatMessageType position, BaseComponent... components) {
            if (null == getHandle().networkHandler) return;

            // TODO: 1.19
            
           /* GameMessageS2CPacket packet = new GameMessageS2CPacket(null, MessageType.byId((byte) position.ordinal()), nms.getUuid());
            if (position == net.md_5.bungee.api.ChatMessageType.ACTION_BAR)
                components = new BaseComponent[]{new net.md_5.bungee.api.chat.TextComponent(BaseComponent.toLegacyText(components))};
            
            ((IGameMessagePacket)packet).setBungeeComponents(components);
            getHandle().networkHandler.sendPacket(packet);*/
            
            getHandle().sendMessage( Text.literal( BaseComponent.toLegacyText(components) ) );

            //getHandle().networkHandler.sendPacket(new GameMessageS2CPacket(components, position == ChatMessageType.ACTION_BAR));
        }
    };

    @Override
    public org.bukkit.entity.Player.Spigot spigot() {
        return spigot;
    }

    @Override
    public Location getBedSpawnLocation() {
        World world = ((IMixinWorld)getHandle().server.getWorld(getHandle().getSpawnPointDimension())).getWorldImpl();
        BlockPos bed = getHandle().getSpawnPointPosition();

        if (world != null && bed != null) {
            Optional<Vec3d> spawnLoc = PlayerEntity.findRespawnPosition((ServerWorld) ((WorldImpl) world).getHandle(), bed, getHandle().getSpawnAngle(), getHandle().isSpawnForced(), true);
            if (spawnLoc.isPresent()) {
                Vec3d vec = spawnLoc.get();
                return new Location(world, vec.x, vec.y, vec.z);
            }
        }
        return null;
    }

    @Override
    public void setBedSpawnLocation(Location location) {
        setBedSpawnLocation(location, false);
    }

    @Override
    public void setBedSpawnLocation(Location location, boolean override) {
        if (location == null) {
            getHandle().setSpawnPoint(null, null, 0, override, false);
        } else getHandle().setSpawnPoint(((WorldImpl) location.getWorld()).getHandle().getRegistryKey(), new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()), location.getYaw(), override, false);
    }

    public void setFirstPlayed(long modified) {
        // TODO Auto-generated method stub
    }

    @Override
    public EntityType getType() {
        return EntityType.PLAYER;
    }

    public void updateScaledHealth() {
        // TODO Auto-generated method stub
    }

    // SPIGOT-759
    public void sendRawMessage(UUID uuid, String msg) {
        this.sendRawMessage(msg);
    }

    public void setHandle(ServerPlayerEntity plr) {
        this.nms = plr;
        super.nms = plr;
    }

    // PaperAPI - START
    public void setTitleTimes(int fadeInTicks, int stayTicks, int fadeOutTicks) {
     // TODO 1.17ify getHandle().networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.TIMES, null, 0, 0, 0));
    }

    public void showTitle(BaseComponent[] title) {
     // TODO 1.17ify   getHandle().networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.TITLE, Text.of(ComponentSerializer.toString(title)), 0, 0, 0));
    }

    public void setSubtitle(BaseComponent[] subtitle) {
     // TODO 1.17ify   getHandle().networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.SUBTITLE, Text.of(ComponentSerializer.toString(subtitle)), 0, 0, 0));
    }

    public void showTitle(BaseComponent title) {
        showTitle(new BaseComponent[]{title});
    }

    public void setSubtitle(BaseComponent subtitle) {
        showTitle(new BaseComponent[]{subtitle});
    }

    public void showTitle(BaseComponent[] title, BaseComponent[] subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        setTitleTimes(fadeInTicks, stayTicks, fadeOutTicks);
        setSubtitle(subtitle);
        showTitle(title);
    }

    public void showTitle(BaseComponent title, BaseComponent subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        setTitleTimes(fadeInTicks, stayTicks, fadeOutTicks);
        setSubtitle(subtitle);
        showTitle(title);
    }

    public void sendTitle(Title title) {
        Preconditions.checkNotNull(title, "Title is null");
        setTitleTimes(title.getFadeIn(), title.getStay(), title.getFadeOut());
        setSubtitle(title.getSubtitle() == null ? new BaseComponent[0] : title.getSubtitle());
        showTitle(title.getTitle());
    }

    public void updateTitle(Title title) {
        Preconditions.checkNotNull(title, "Title is null");
        setTitleTimes(title.getFadeIn(), title.getStay(), title.getFadeOut());
        if (title.getSubtitle() != null) {
            setSubtitle(title.getSubtitle());
        }
        showTitle(title.getTitle());
    }

    public void sendActionBar(BaseComponent[] message) {
        if (getHandle().networkHandler == null) return;
     // TODO 1.17ify getHandle().networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.TITLE, Text.of(ComponentSerializer.toString(message)), 0, 0, 0));
    }

    public void sendActionBar(String message) {
        if (getHandle().networkHandler == null || message == null || message.isEmpty()) return;
     // TODO 1.17ifygetHandle().networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.TITLE, CraftChatMessage.fromStringOrNull(message), 0, 0, 0));
    }

    public void sendActionBar(char alternateChar, String message) {
        if (message == null || message.isEmpty()) return;
        sendActionBar(org.bukkit.ChatColor.translateAlternateColorCodes(alternateChar, message));
    }

    public int getViewDistance() {
        throw new NotImplementedException("Was Removed from Paper");
    }

    public void setViewDistance(int viewDistance) {
        throw new NotImplementedException("Was Removed from Paper");
    }
    // PaperAPI - END

    @Override
    public void closeInventory(Reason arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Location getPotentialBedLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InventoryView openAnvil(Location arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InventoryView openCartographyTable(Location arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InventoryView openGrindstone(Location arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InventoryView openLoom(Location arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void openSign(Sign arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public InventoryView openSmithingTable(Location arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InventoryView openStonecutter(Location arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Entity releaseLeftShoulderEntity() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Entity releaseRightShoulderEntity() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getLastLogin() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getLastSeen() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getProtocolVersion() {
        // TODO Auto-generated method stub
        return GameVersion.INSTANCE.getProtocolVersion();
    }

    @Override
    public InetSocketAddress getVirtualHost() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int applyMending(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Firework boostElytra(ItemStack arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean getAffectsSpawning() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getClientBrandName() {
        // TODO Auto-generated method stub
        return "Vanilla";
    }

    @Override
    public <T> T getClientOption(ClientOption<T> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public float getCooldownPeriod() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getCooledAttackStrength(float arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public PlayerProfile getPlayerProfile() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getResourcePackHash() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Status getResourcePackStatus() {
        // TODO Auto-generated method stub
        return Status.SUCCESSFULLY_LOADED;
    }

    @Override
    public void giveExp(int arg0, boolean arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean hasResourcePack() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void hideTitle() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void resetCooldown() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setAffectsSpawning(boolean arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setPlayerListHeaderFooter(BaseComponent[] arg0, BaseComponent[] arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setPlayerListHeaderFooter(BaseComponent arg0, BaseComponent arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setPlayerProfile(PlayerProfile arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setResourcePack(String arg0, String arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public @NotNull Component displayName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void displayName(@Nullable Component arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int getPing() {
        return this.getHandle().pingMilliseconds;
    }

    @Override
    public @NotNull Set<Player> getTrackedPlayers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void kick(@Nullable Component arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void kick(@Nullable Component arg0, @NotNull Cause arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public @NotNull Locale locale() {
        // TODO Auto-generated method stub
        return Locale.ENGLISH;
    }

    @Override
    public @Nullable Component playerListFooter() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @Nullable Component playerListHeader() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @Nullable Component playerListName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void playerListName(@Nullable Component arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void sendBlockDamage(@NotNull Location loc, float progress) {
        Preconditions.checkArgument(loc != null, "loc must not be null");
        Preconditions.checkArgument((double)progress >= 0.0 && (double)progress <= 1.0, "progress must be between 0.0 and 1.0 (inclusive)");
        if (this.getHandle().networkHandler == null) {
            return;
        }
        int stage = (int)(9.0f * progress);
        BlockBreakingProgressS2CPacket packet = new BlockBreakingProgressS2CPacket(this.getHandle().getId(), new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), stage);
        this.getHandle().networkHandler.sendPacket(packet);
    }

    @Override
    public void sendOpLevel(byte arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void sendSignChange(@NotNull Location arg0, @Nullable List<Component> arg1) throws IllegalArgumentException {
        // TODO Auto-generated method stub
    }

    @Override
    public void sendSignChange(@NotNull Location arg0, @Nullable List<Component> arg1, @NotNull DyeColor arg2)
            throws IllegalArgumentException {
        // TODO Auto-generated method stub
    }

    //public BlockPos posAtLogin;
    //public boolean in;

    /*
     * Save BlockPos from {@link PlayerManager#onPlayerConnect}
     *
    public void setLoginPos(BlockPos pos) {
        this.posAtLogin = pos;
        this.in = nms.isInvulnerable();
    }*/

    // 1.17 API Start

    @Override
    public boolean breakBlock(@NotNull Block b) {
        return nms.interactionManager.tryBreakBlock(new BlockPos(b.getX(), b.getY(), b.getZ()));
    }

    @Override
    public int getNoTickViewDistance() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getSendViewDistance() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void sendSignChange(@NotNull Location arg0, @Nullable List<Component> arg1, @NotNull DyeColor arg2,
            boolean arg3) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void sendSignChange(@NotNull Location arg0, @Nullable String[] arg1, @NotNull DyeColor arg2, boolean arg3)
            throws IllegalArgumentException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setNoTickViewDistance(int i) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setResourcePack(@NotNull String arg0, @NotNull String arg1, boolean arg2) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setResourcePack(@NotNull String arg0, @NotNull String arg1, boolean arg2, @Nullable Component arg3) {
        // TODO Auto-generated method stub
        this.setResourcePack(arg0, arg1, arg2);
    }

    @Override
    public void setSendViewDistance(int i) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void stopAllSounds() {
        // TODO Auto-generated method stub
        
    }

}