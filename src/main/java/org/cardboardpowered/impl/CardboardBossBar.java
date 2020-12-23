package org.cardboardpowered.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinServerBossBar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;

import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CardboardBossBar implements BossBar, KeyedBossBar {

    private final ServerBossBar handle;
    private Map<BarFlag, FlagContainer> flags;

    public CardboardBossBar(String title, BarColor color, BarStyle style, BarFlag... flags) {
        handle = new ServerBossBar(CraftChatMessage.fromString(title, true)[0], convertColor(color), convertStyle(style));

        this.initialize();
        for (BarFlag flag : flags) this.addFlag(flag);
        this.setColor(color);
        this.setStyle(style);
    }

    public CardboardBossBar(ServerBossBar bossBattleServer) {
        this.handle = bossBattleServer;
        this.initialize();
    }

    public CardboardBossBar(CommandBossBar bossBattleCustom) {
        this.handle = bossBattleCustom;
        this.initialize();
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        if (handle instanceof CommandBossBar) {
            return CraftNamespacedKey.fromMinecraft(((CommandBossBar)handle).getId());
        }
        throw new UnsupportedOperationException("BossBar is not keyed!");
    }

    private void initialize() {
        this.flags = new HashMap<>();

        this.flags.put(BarFlag.DARKEN_SKY, new FlagContainer(handle::getDarkenSky, handle::setDarkenSky));
        this.flags.put(BarFlag.PLAY_BOSS_MUSIC, new FlagContainer(handle::hasDragonMusic, handle::setDragonMusic));
        this.flags.put(BarFlag.CREATE_FOG, new FlagContainer(handle::getThickenFog, handle::setThickenFog));
    }

    private BarColor convertColor(net.minecraft.entity.boss.BossBar.Color color) {
        BarColor bukkitColor = BarColor.valueOf(color.name());
        return (bukkitColor == null) ? BarColor.WHITE : bukkitColor;
    }

    private net.minecraft.entity.boss.BossBar.Color convertColor(BarColor color) {
        net.minecraft.entity.boss.BossBar.Color nmsColor = net.minecraft.entity.boss.BossBar.Color.valueOf(color.name());
        return (nmsColor == null) ? net.minecraft.entity.boss.BossBar.Color.WHITE : nmsColor;
    }

    private net.minecraft.entity.boss.BossBar.Style convertStyle(BarStyle style) {
        switch (style) {
            default:
                case SOLID:
                    return net.minecraft.entity.boss.BossBar.Style.PROGRESS;
                case SEGMENTED_6:
                    return net.minecraft.entity.boss.BossBar.Style.NOTCHED_6;
                case SEGMENTED_10:
                    return net.minecraft.entity.boss.BossBar.Style.NOTCHED_10;
                case SEGMENTED_12:
                    return net.minecraft.entity.boss.BossBar.Style.NOTCHED_12;
                case SEGMENTED_20:
                    return net.minecraft.entity.boss.BossBar.Style.NOTCHED_20;
        }
    }

    private BarStyle convertStyle(net.minecraft.entity.boss.BossBar.Style style) {
        switch (style) {
            default:
                case PROGRESS:
                    return BarStyle.SOLID;
                case NOTCHED_6:
                    return BarStyle.SEGMENTED_6;
                case NOTCHED_10:
                    return BarStyle.SEGMENTED_10;
                case NOTCHED_12:
                    return BarStyle.SEGMENTED_12;
                case NOTCHED_20:
                    return BarStyle.SEGMENTED_20;
        }
    }

    @Override
    public String getTitle() {
        return CraftChatMessage.fromComponent(handle.name);
    }

    @Override
    public void setTitle(String title) {
        handle.name = CraftChatMessage.fromString(title, true)[0];
        ((IMixinServerBossBar)handle).sendPacketBF(BossBarS2CPacket.Type.UPDATE_NAME);
    }

    @Override
    public BarColor getColor() {
        return convertColor(handle.color);
    }

    @Override
    public void setColor(@NotNull BarColor color) {
        handle.color = convertColor(color);
        ((IMixinServerBossBar)handle).sendPacketBF(BossBarS2CPacket.Type.UPDATE_STYLE);
    }

    @Override
    public @NotNull BarStyle getStyle() {
        return convertStyle(handle.style);
    }

    @Override
    public void setStyle(@NotNull BarStyle style) {
        handle.style = convertStyle(style);
        ((IMixinServerBossBar)handle).sendPacketBF(BossBarS2CPacket.Type.UPDATE_STYLE);
    }

    @Override
    public void addFlag(@NotNull BarFlag flag) {
        FlagContainer flagContainer = flags.get(flag);
        if (flagContainer != null) flagContainer.set.accept(true);
    }

    @Override
    public void removeFlag(@NotNull BarFlag flag) {
        FlagContainer flagContainer = flags.get(flag);
        if (flagContainer != null) flagContainer.set.accept(false);
    }

    @Override
    public boolean hasFlag(@NotNull BarFlag flag) {
        FlagContainer flagContainer = flags.get(flag);
        return (flagContainer != null) ? flagContainer.get.get() : false;
    }

    @Override
    public void setProgress(double progress) {
        Preconditions.checkArgument(progress >= 0.0 && progress <= 1.0, "Progress must be between 0.0 and 1.0 (%s)", progress);
        handle.setPercent((float) progress);
    }

    @Override
    public double getProgress() {
        return handle.getPercent();
    }

    @Override
    public void addPlayer(@NotNull Player player) {
        Preconditions.checkArgument(true, "player == null");
        Preconditions.checkArgument(((PlayerImpl) player).getHandle().networkHandler != null, "player is not fully connected (wait for PlayerJoinEvent)");
        handle.addPlayer(((PlayerImpl) player).getHandle());
    }

    @Override
    public void removePlayer(@NotNull Player player) {
        Preconditions.checkArgument(true, "player == null");
        handle.removePlayer(((PlayerImpl) player).getHandle());
    }

    @Override
    public @NotNull List<Player> getPlayers() {
        ImmutableList.Builder<Player> players = ImmutableList.builder();
        for (ServerPlayerEntity p : handle.getPlayers())
            players.add((Player)((IMixinEntity)p).getBukkitEntity());
        return players.build();
    }

    @Override
    public void setVisible(boolean visible) {
        handle.setVisible(visible);
    }

    @Override
    public boolean isVisible() {
        return handle.visible;
    }

    @Override
    public void show() {
        handle.setVisible(true);
    }

    @Override
    public void hide() {
        handle.setVisible(false);
    }

    @Override
    public void removeAll() {
        for (Player player : getPlayers()) removePlayer(player);
    }

    private static final class FlagContainer {

        private final Supplier<Boolean> get;
        private final Consumer<Boolean> set;

        public FlagContainer(Supplier<Boolean> get, Consumer<Boolean> set) {
            this.get = get;
            this.set = set;
        }
    }

    public ServerBossBar getHandle() {
        return handle;
    }

}