package com.javazilla.bukkitfabric.impl.banlist;

import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import org.bukkit.Bukkit;

public final class ProfileBanEntry implements org.bukkit.BanEntry {

    private final BannedPlayerList list;
    private final GameProfile profile;
    private Date created;
    private String source;
    private Date expiration;
    private String reason;

    public ProfileBanEntry(GameProfile profile, BannedPlayerEntry entry, BannedPlayerList list) {
        this.list = list;
        this.profile = profile;
        this.created = null; // TODO Bukkit4Fabric
        this.source = entry.getSource();
        this.expiration = entry.getExpiryDate() != null ? new Date(entry.getExpiryDate().getTime()) : null;
        this.reason = entry.getReason();
    }

    @Override
    public String getTarget() {
        return this.profile.getName();
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
        if (expiration != null && expiration.getTime() == new Date(0, 0, 0, 0, 0, 0).getTime())
            expiration = null; // Forces "forever"
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
        BannedPlayerEntry entry = new BannedPlayerEntry(profile, this.created, this.source, this.expiration, this.reason);
        this.list.add(entry);
        try {
            this.list.save();
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to save banned-players.json, {0}", ex.getMessage());
        }
    }

}