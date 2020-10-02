package com.javazilla.bukkitfabric.impl.banlist;

import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;

import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;

import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.ServerConfigEntry;

public class ProfileBanList implements org.bukkit.BanList {

    private final BannedPlayerList list;

    public ProfileBanList(BannedPlayerList list) {
        this.list = list;
    }

    @Override
    public org.bukkit.BanEntry getBanEntry(String target) {
        GameProfile profile = getProfile(target);
        if (profile == null)
            return null;

        BannedPlayerEntry entry = (BannedPlayerEntry) list.get(profile);
        if (entry == null)
            return null;

        return new ProfileBanEntry(profile, entry, list);
    }

    @Override
    public org.bukkit.BanEntry addBan(String target, String reason, Date expires, String source) {
        GameProfile profile = getProfile(target);
        if (profile == null)
            return null;

        BannedPlayerEntry entry = new BannedPlayerEntry(profile, new Date(), StringUtils.isBlank(source) ? null : source, expires, StringUtils.isBlank(reason) ? null : reason);
        list.add(entry);

        try {
            list.save();
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to save banned-players.json, {0}", ex.getMessage());
        }

        return new ProfileBanEntry(profile, entry, list);
    }

    @Override
    public Set<org.bukkit.BanEntry> getBanEntries() {
        ImmutableSet.Builder<org.bukkit.BanEntry> builder = ImmutableSet.builder();
        for (ServerConfigEntry<?> entry : list.values())
            builder.add(new ProfileBanEntry((GameProfile) entry.getKey(), (BannedPlayerEntry) entry, list));

        return builder.build();
    }

    @Override
    public boolean isBanned(String target) {
        GameProfile profile = getProfile(target);
        if (profile == null)
            return false;

        return list.contains(profile);
    }

    @Override
    public void pardon(String target) {
        list.remove(getProfile(target));
    }

    private GameProfile getProfile(String target) {
        UUID uuid = null;

        try {
            uuid = UUID.fromString(target);
        } catch (IllegalArgumentException ex) {/**/}

        return (uuid != null) ? CraftServer.server.getUserCache().getByUuid(uuid) : CraftServer.server.getUserCache().findByName(target);
    }

}