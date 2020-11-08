package com.javazilla.bukkitfabric.impl.banlist;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;

import com.google.common.collect.ImmutableSet;

import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.BannedIpList;

public class IpBanList implements org.bukkit.BanList {

    private final BannedIpList list;

    public IpBanList(BannedIpList list) {
        this.list = list;
    }

    @Override
    public org.bukkit.BanEntry getBanEntry(String target) {
        BannedIpEntry entry = (BannedIpEntry) list.get(target);
        return (entry == null) ? null : new IpBanEntry(target, entry, list);
    }

    @Override
    public org.bukkit.BanEntry addBan(String target, String reason, Date expires, String source) {
        BannedIpEntry entry = new BannedIpEntry(target, new Date(), StringUtils.isBlank(source) ? null : source, expires, StringUtils.isBlank(reason) ? null : reason);

        list.add(entry);

        try {
            list.save();
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to save banned-ips.json, {0}", ex.getMessage());
        }

        return new IpBanEntry(target, entry, list);
    }

    @Override
    public Set<org.bukkit.BanEntry> getBanEntries() {
        ImmutableSet.Builder<org.bukkit.BanEntry> builder = ImmutableSet.builder();
        for (String target : list.getNames())
            builder.add(new IpBanEntry(target, (BannedIpEntry) list.get(target), list));
        return builder.build();
    }

    @Override
    public boolean isBanned(String target) {
        return list.isBanned(InetSocketAddress.createUnresolved(target, 0));
    }

    @Override
    public void pardon(String target) {
        list.remove(target);
    }

}