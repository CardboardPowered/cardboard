package com.javazilla.bukkitfabric.impl.boss;

import net.minecraft.entity.boss.CommandBossBar;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;

public class KeyedBossBarImpl extends BossBarImpl implements KeyedBossBar {

    public KeyedBossBarImpl(CommandBossBar bossBattleCustom) {
        super(bossBattleCustom);
    }

    @Override
    public NamespacedKey getKey() {
        return CraftNamespacedKey.fromMinecraft(getHandle().getId());
    }

    @Override
    public CommandBossBar getHandle() {
        return (CommandBossBar) super.getHandle();
    }

}