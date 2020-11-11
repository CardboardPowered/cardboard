package org.cardboardpowered.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.bukkit.BanEntry;
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

    public class IpBanEntry implements BanEntry {

        private final BannedIpList list;
        private final String target;
        private Date created;
        private String source;
        private Date expiration;
        private String reason;

        public IpBanEntry(String target, BannedIpEntry entry, BannedIpList list) {
            this.list = list;
            this.target = target;
            this.created = null; // TODO Bukkit4Fabric
            this.source = entry.getSource();
            this.expiration = entry.getExpiryDate() != null ? new Date(entry.getExpiryDate().getTime()) : null;
            this.reason = entry.getReason();
        }

        @Override
        public String getTarget() {
            return this.target;
        }

        @Override
        public Date getCreated() {
            return this.created == null ? null : (Date) this.created.clone();
        }

        @Override
        public void setCreated(Date created) {
            this.created = created;
        }

        @Override
        public String getSource() {
            return this.source;
        }

        @Override
        public void setSource(String source) {
            this.source = source;
        }

        @Override
        public Date getExpiration() {
            return this.expiration == null ? null : (Date) this.expiration.clone();
        }

        @SuppressWarnings("deprecation")
        @Override
        public void setExpiration(Date expiration) {
            if (expiration != null && expiration.getTime() == new Date(0, 0, 0, 0, 0, 0).getTime()) expiration = null; // Forces "forever"
            this.expiration = expiration;
        }

        @Override
        public String getReason() {
            return this.reason;
        }

        @Override
        public void setReason(String reason) {
            this.reason = reason;
        }

        @Override
        public void save() {
            BannedIpEntry entry = new BannedIpEntry(target, this.created, this.source, this.expiration, this.reason);
            this.list.add(entry);
            try {
                this.list.save();
            } catch (IOException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to save banned-ips.json, {0}", ex.getMessage());
            }
        }

    }

}