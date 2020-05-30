package org.bukkit.craftbukkit.entity;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;

import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Statistic;
import org.bukkit.WeatherType;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.block.data.BlockData;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.network.MessageType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public class CraftPlayer extends CraftHumanEntity implements Player {

    public ServerPlayerEntity nms;

    public CraftPlayer(ServerPlayerEntity entity) {
        super(entity);
        super.nms = entity;
        this.nms = entity;
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
        nms.sendChatMessage(new LiteralText(message), MessageType.SYSTEM);
    }

    @Override
    public Player getPlayer() {
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
        return false;
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
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isOnline() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isWhitelisted() {
        // TODO Auto-generated method stub
        return false;
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
        // TODO Auto-generated method stub
    }

    @Override
    public Map<String, Object> serialize() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> getListeningPluginChannels() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void sendPluginMessage(Plugin source, String channel, byte[] message) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeBytes(message);
        String[] id = channel.split(":");
        ClientSidePacketRegistry.INSTANCE.sendToServer(new Identifier(id[0], id[1]), buffer);
    }

    @Override
    public boolean canSee(Player arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void chat(String mesage) {
        // TODO Auto-generated method stub
    }

    @Override
    public InetSocketAddress getAddress() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AdvancementProgress getAdvancementProgress(Advancement arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean getAllowFlight() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getClientViewDistance() {
        // TODO Auto-generated method stub
        return 0;
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
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getExp() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getFlySpeed() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getFoodLevel() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getHealthScale() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getLevel() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getLocale() {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Scoreboard getScoreboard() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Entity getSpectatorTarget() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getTotalExperience() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getWalkSpeed() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void giveExp(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void giveExpLevels(int arg0) {
        // TODO Auto-generated method stub

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
        // TODO Auto-generated method stub
        return nms.isFallFlying();
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
        // TODO Auto-generated method stub
    }

    @Override
    public void loadData() {
        // TODO Auto-generated method stub
    }

    @Override
    public void openBook(ItemStack arg0) {
        // TODO Auto-generated method stub
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
    public void playNote(Location arg0, byte arg1, byte arg2) {
        // TODO Auto-generated method stub
    }

    @Override
    public void playNote(Location arg0, Instrument arg1, Note arg2) {
        // TODO Auto-generated method stub
    }

    @Override
    public void playSound(Location arg0, Sound arg1, float arg2, float arg3) {
        // TODO Auto-generated method stub

    }

    @Override
    public void playSound(Location arg0, String arg1, float arg2, float arg3) {
        // TODO Auto-generated method stub

    }

    @Override
    public void playSound(Location arg0, Sound arg1, SoundCategory arg2, float arg3, float arg4) {
        // TODO Auto-generated method stub

    }

    @Override
    public void playSound(Location arg0, String arg1, SoundCategory arg2, float arg3, float arg4) {
        // TODO Auto-generated method stub

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
        // TODO Auto-generated method stub
    }

    @Override
    public void saveData() {
        // TODO Auto-generated method stub
    }

    @Override
    public void sendBlockChange(Location arg0, BlockData arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public void sendBlockChange(Location arg0, Material arg1, byte arg2) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean sendChunkChange(Location arg0, int arg1, int arg2, int arg3, byte[] arg4) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void sendExperienceChange(float arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void sendExperienceChange(float arg0, int arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public void sendMap(MapView arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void sendRawMessage(String arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void sendSignChange(Location arg0, String[] arg1) throws IllegalArgumentException {
        // TODO Auto-generated method stub
    }

    @Override
    public void sendSignChange(Location arg0, String[] arg1, DyeColor arg2) throws IllegalArgumentException {
        // TODO Auto-generated method stub
    }

    @Override
    public void sendTitle(String arg0, String arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public void sendTitle(String arg0, String arg1, int arg2, int arg3, int arg4) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setAllowFlight(boolean arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setCompassTarget(Location arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setDisplayName(String arg0) {
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
        // TODO Auto-generated method stub
    }

    @Override
    public void setFlying(boolean arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setFoodLevel(int arg0) {
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub

    }

    @Override
    public void setTexturePack(String arg0) {
        setResourcePack(arg0);
    }

    @Override
    public void setTotalExperience(int arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setWalkSpeed(float arg0) throws IllegalArgumentException {
        // TODO Auto-generated method stub

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
    public void spawnParticle(Particle arg0, Location arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> void spawnParticle(Particle arg0, Location arg1, int arg2, T arg3) {
        // TODO Auto-generated method stub

    }

    @Override
    public void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4, T arg5) {
        // TODO Auto-generated method stub

    }

    @Override
    public void spawnParticle(Particle arg0, Location arg1, int arg2, double arg3, double arg4, double arg5) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> void spawnParticle(Particle arg0, Location arg1, int arg2, double arg3, double arg4, double arg5,
            T arg6) {
        // TODO Auto-generated method stub

    }

    @Override
    public void spawnParticle(Particle arg0, Location arg1, int arg2, double arg3, double arg4, double arg5,
            double arg6) {
        // TODO Auto-generated method stub

    }

    @Override
    public void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4, double arg5, double arg6,
            double arg7) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> void spawnParticle(Particle arg0, Location arg1, int arg2, double arg3, double arg4, double arg5,
            double arg6, T arg7) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4, double arg5,
            double arg6, double arg7, T arg8) {
        // TODO Auto-generated method stub
    }

    @Override
    public void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4, double arg5, double arg6,
            double arg7, double arg8) {
        // TODO Auto-generated method stub
    }

    @Override
    public <T> void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4, double arg5,
            double arg6, double arg7, double arg8, T arg9) {
        // TODO Auto-generated method stub
    }

    @Override
    public void stopSound(Sound arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void stopSound(String arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void stopSound(Sound arg0, SoundCategory arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public void stopSound(String arg0, SoundCategory arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public void updateCommands() {
        // TODO Auto-generated method stub
    }

    @Override
    public void updateInventory() {
        // TODO Auto-generated method stub
    }

    @Override
    public org.bukkit.entity.Player.Spigot spigot() {
        // TODO Auto-generated method stub
        return null;
    }

}