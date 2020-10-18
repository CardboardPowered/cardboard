package org.bukkit.craftbukkit.entity;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.craftbukkit.CraftOfflinePlayer;
import org.bukkit.craftbukkit.CraftParticle;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftSound;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.bukkit.scoreboard.Scoreboard;
import com.google.common.base.Preconditions;
import com.javazilla.bukkitfabric.Utils;
import com.javazilla.bukkitfabric.impl.WorldImpl;
import com.javazilla.bukkitfabric.impl.advancements.AdvancementImpl;
import com.javazilla.bukkitfabric.impl.advancements.AdvancementProgressImpl;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinGameMessagePacket;
import com.javazilla.bukkitfabric.interfaces.IMixinMinecraftServer;
import com.javazilla.bukkitfabric.interfaces.IMixinPlayNetworkHandler;
import com.javazilla.bukkitfabric.interfaces.IMixinPlayerManager;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;
import com.mojang.authlib.GameProfile;

import io.netty.buffer.Unpooled;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.WhitelistEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.PacketByteBuf;

@DelegateDeserialization(CraftOfflinePlayer.class)
public class CraftPlayer extends CraftHumanEntity implements Player {

    public ServerPlayerEntity nms;

    public CraftPlayer(ServerPlayerEntity entity) {
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
    public void decrementStatistic(Statistic arg0) throws IllegalArgumentException {
        // TODO Auto-generated method stub
    }

    @Override
    public void decrementStatistic(Statistic arg0, int arg1) throws IllegalArgumentException {
        // TODO Auto-generated method stub
    }

    @Override
    public void decrementStatistic(Statistic arg0, Material arg1) throws IllegalArgumentException {
        // TODO Auto-generated method stub
    }

    @Override
    public void decrementStatistic(Statistic arg0, EntityType arg1) throws IllegalArgumentException {
        // TODO Auto-generated method stub
    }

    @Override
    public void decrementStatistic(Statistic arg0, Material arg1, int arg2) throws IllegalArgumentException {
        // TODO Auto-generated method stub
    }

    @Override
    public void decrementStatistic(Statistic arg0, EntityType arg1, int arg2) {
        // TODO Auto-generated method stub
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
        nms.sendSystemMessage(new LiteralText(message), UUID.randomUUID());
    }

    @Override
    public CraftPlayer getPlayer() {
        return this;
    }

    @Override
    public int getStatistic(Statistic arg0) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getStatistic(Statistic arg0, Material arg1) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getStatistic(Statistic arg0, EntityType arg1) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean hasPlayedBefore() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void incrementStatistic(Statistic arg0) throws IllegalArgumentException {
        // TODO Auto-generated method stub
    }

    @Override
    public void incrementStatistic(Statistic arg0, int arg1) throws IllegalArgumentException {
        // TODO Auto-generated method stub
    }

    @Override
    public void incrementStatistic(Statistic arg0, Material arg1) throws IllegalArgumentException {
        // TODO Auto-generated method stub
    }

    @Override
    public void incrementStatistic(Statistic arg0, EntityType arg1) throws IllegalArgumentException {
        // TODO Auto-generated method stub
    }

    @Override
    public void incrementStatistic(Statistic arg0, Material arg1, int arg2) throws IllegalArgumentException {
        // TODO Auto-generated method stub
    }

    @Override
    public void incrementStatistic(Statistic arg0, EntityType arg1, int arg2) throws IllegalArgumentException {
        // TODO Auto-generated method stub
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
    public void setStatistic(Statistic arg0, int arg1) throws IllegalArgumentException {
        // TODO Auto-generated method stub
    }

    @Override
    public void setStatistic(Statistic arg0, Material arg1, int arg2) throws IllegalArgumentException {
        // TODO Auto-generated method stub
    }

    @Override
    public void setStatistic(Statistic arg0, EntityType arg1, int arg2) {
        // TODO Auto-generated method stub
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

    @Override
    public Set<String> getListeningPluginChannels() {
        // TODO Auto-generated method stub
        return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void sendPluginMessage(Plugin source, String channel, byte[] message) {
        StandardMessenger.validatePluginMessage(Bukkit.getMessenger(), source, channel, message);
        if (getHandle().networkHandler == null) return;

        //if (channels.contains(channel)) {
            channel = StandardMessenger.validateAndCorrectChannel(channel);
            CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(new Identifier(channel), new PacketByteBuf(Unpooled.wrappedBuffer(message)));
            getHandle().networkHandler.sendPacket(packet);
        //}
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
        return getHandle().abilities.allowFlying;
    }

    @Override
    public int getClientViewDistance() {
        return 8; // TODO Get Client view distance not server
    }

    @Override
    public Location getCompassTarget() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDisplayName() {
        return nms.getDisplayName().asString();
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
        return nms.flyingSpeed;
    }

    @Override
    public int getFoodLevel() {
        return nms.getHungerManager().getFoodLevel();
    }

    @Override
    public double getHealthScale() {
        // TODO Auto-generated method stub
        return 0;
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
        return null;
    }

    @Override
    public float getSaturation() {
        return nms.getHungerManager().getSaturationLevel();
    }

    @Override
    public Scoreboard getScoreboard() {
        // TODO Auto-generated method stub
        return null;
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
        return nms.abilities.flying;
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
        nms.networkHandler.disconnect(new LiteralText(arg0));
    }

    @Override
    public void loadData() {
        ((IMixinMinecraftServer)CraftServer.server).getSaveHandler_BF().loadPlayerData(nms);
    }

    @Override
    public void openBook(ItemStack book) {
        ItemStack hand = getInventory().getItemInMainHand();
        getInventory().setItemInMainHand(book);
        getHandle().openEditBookScreen(org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(book), net.minecraft.util.Hand.MAIN_HAND);
        getInventory().setItemInMainHand(hand);
    }

    @Override
    public boolean performCommand(String arg0) {
        return getServer().dispatchCommand(this, arg0);
    }

    @Override
    public void playEffect(Location arg0, Effect arg1, int arg2) {
        // TODO Auto-generated method stub
    }

    @Override
    public <T> void playEffect(Location arg0, Effect arg1, T arg2) {
        // TODO Auto-generated method stub
    }

    @Override
    public void playNote(Location loc, byte instrument, byte note) {
        if (getHandle().networkHandler == null) return;

        String instrumentName = null;
        switch (instrument) {
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
        }

        float f = (float) Math.pow(2.0D, (note - 12.0D) / 12.0D);
        getHandle().networkHandler.sendPacket(new PlaySoundS2CPacket(CraftSound.getSoundEffect("block.note_block." + instrumentName), net.minecraft.sound.SoundCategory.RECORDS, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), 3.0f, f));
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
        getHandle().networkHandler.sendPacket(new PlaySoundS2CPacket(CraftSound.getSoundEffect("block.note_block." + instrumentName), net.minecraft.sound.SoundCategory.RECORDS, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), 3.0f, f));
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

        PlaySoundS2CPacket packet = new PlaySoundS2CPacket(CraftSound.getSoundEffect(CraftSound.getSound(sound)), net.minecraft.sound.SoundCategory.valueOf(category.name()), loc.getX(), loc.getY(), loc.getZ(), volume, pitch);
        getHandle().networkHandler.sendPacket(packet);
    }

    @Override
    public void playSound(Location loc, String sound, org.bukkit.SoundCategory category, float volume, float pitch) {
        if (loc == null || sound == null || category == null || getHandle().networkHandler == null) return;

        PlaySoundIdS2CPacket packet = new PlaySoundIdS2CPacket(new Identifier(sound), net.minecraft.sound.SoundCategory.valueOf(category.name()), new Vec3d(loc.getX(), loc.getY(), loc.getZ()), volume, pitch);
        getHandle().networkHandler.sendPacket(packet);
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
        nms.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.RESET, null));
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
    public void sendMap(MapView arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void sendRawMessage(String arg0) {
        if (getHandle().networkHandler == null) return;

        for (Text component : CraftChatMessage.fromString(arg0))
            getHandle().networkHandler.sendPacket(new GameMessageS2CPacket(component, MessageType.CHAT, Util.NIL_UUID));
    }

    @Override
    public void sendSignChange(Location loc, String[] lines) {
       sendSignChange(loc, lines, DyeColor.BLACK);
    }

    @Override
    public void sendSignChange(Location arg0, String[] arg1, DyeColor arg2) throws IllegalArgumentException {
        // TODO Auto-generated method stub
    }

    @Override
    public void sendTitle(String arg0, String arg1) {
        sendTitle(arg0, arg1, 10, 70, 20);
    }

    @Override
    public void sendTitle(String arg0, String arg1, int arg2, int arg3, int arg4) {
        TitleS2CPacket times = new TitleS2CPacket(arg2, arg3, arg4);
        nms.networkHandler.sendPacket(times);

        if (arg0 != null)
            nms.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.TITLE, CraftChatMessage.fromStringOrNull(arg0)));

        if (arg1 != null)
            nms.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.SUBTITLE, CraftChatMessage.fromStringOrNull(arg1)));
    }

    @Override
    public void setAllowFlight(boolean arg0) {
        if (isFlying() && !arg0)
            getHandle().abilities.flying = false;

        getHandle().abilities.allowFlying = arg0;
        getHandle().sendAbilitiesUpdate();
    }

    @Override
    public void setCompassTarget(Location arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setDisplayName(String arg0) {
        nms.setCustomNameVisible(true);
        nms.setCustomName(new LiteralText(arg0));
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
        nms.flyingSpeed = arg0;
    }

    @Override
    public void setFlying(boolean arg0) {
        if (!getAllowFlight() && arg0)
            throw new IllegalArgumentException("getAllowFlight() is false, cannot set player flying");

        getHandle().abilities.flying = arg0;
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
        nms.sendResourcePackUrl(url, null);
    }

    @Override
    public void setResourcePack(String url, byte[] hash) {
        nms.sendResourcePackUrl(url, new String(hash));
    }

    @Override
    public void setSaturation(float arg0) {
        nms.getHungerManager().setSaturationLevelClient(arg0);
    }

    @Override
    public void setScoreboard(Scoreboard arg0) throws IllegalArgumentException, IllegalStateException {
        // TODO Auto-generated method stub
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
        nms.abilities.setWalkSpeed(arg0);
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
        nms.openHandledScreen(nms.currentScreenHandler);
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

        getHandle().setGameMode(net.minecraft.world.GameMode.byId(mode.getValue()));
    }

    public GameProfile getProfile() {
        return CraftServer.server.getUserCache().getByUuid(getUniqueId());
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

        if (getHealth() == 0 || entity.removed || entity.networkHandler == null || entity.hasPassengers())
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
            getHandle().closeCurrentScreen();

        if (from.getWorld().equals(to.getWorld()))
             ((IMixinPlayNetworkHandler)(Object)entity.networkHandler).teleport(to);
        else ((IMixinPlayerManager)(PlayerManager)CraftServer.server.getPlayerManager()).moveToWorld(entity, toWorld, true, to, true);

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

        if (other instanceof CraftPlayer)
            idEquals = this.getEntityId() == ((CraftPlayer) other).getEntityId();

        return uuidEquals && idEquals;
    }

    private final Player.Spigot spigot = new Player.Spigot() {

        @Override
        public InetSocketAddress getRawAddress() {
            return (InetSocketAddress) getHandle().networkHandler.connection.getAddress();
        }

        @Override
        public boolean getCollidesWithEntities() {
            return CraftPlayer.this.isCollidable();
        }

        @Override
        public void setCollidesWithEntities(boolean collides) {
            CraftPlayer.this.setCollidable(collides);
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

            GameMessageS2CPacket packet = new GameMessageS2CPacket(null, MessageType.SYSTEM, nms.getUuid());
            ((IMixinGameMessagePacket)packet).setBungeeComponents(components);
            getHandle().networkHandler.sendPacket(packet);
        }

        @Override
        public void sendMessage(net.md_5.bungee.api.ChatMessageType position, BaseComponent component) {
            sendMessage( position, new BaseComponent[] { component } );
        }

        @Override
        public void sendMessage(net.md_5.bungee.api.ChatMessageType position, BaseComponent... components) {
            if (null == getHandle().networkHandler) return;

            GameMessageS2CPacket packet = new GameMessageS2CPacket(null, MessageType.byId((byte) position.ordinal()), nms.getUuid());
            if (position == net.md_5.bungee.api.ChatMessageType.ACTION_BAR)
                components = new BaseComponent[]{new net.md_5.bungee.api.chat.TextComponent(BaseComponent.toLegacyText(components))};

            ((IMixinGameMessagePacket)packet).setBungeeComponents(components);
            getHandle().networkHandler.sendPacket(packet);
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
            Optional<Vec3d> spawnLoc = PlayerEntity.findRespawnPosition((ServerWorld) ((WorldImpl) world).getHandle(), bed, getHandle().getSpawnAngle(), getHandle().isSpawnPointSet(), true);
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

}