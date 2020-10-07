package org.bukkit.craftbukkit;

import com.javazilla.bukkitfabric.interfaces.IMixinMinecraftServer;
import com.javazilla.bukkitfabric.interfaces.IMixinWorldSaveHandler;
import com.mojang.authlib.GameProfile;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.WhitelistEntry;
import net.minecraft.world.WorldSaveHandler;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.Statistic;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

@SerializableAs("Player")
public class CraftOfflinePlayer implements OfflinePlayer, ConfigurationSerializable {

    private final GameProfile profile;
    private final CraftServer server;
    private final WorldSaveHandler storage;

    protected CraftOfflinePlayer(CraftServer server, GameProfile profile) {
        this.server = server;
        this.profile = profile;
        this.storage = ((IMixinMinecraftServer)server.getServer()).getSaveHandler_BF();
    }

    public GameProfile getProfile() {
        return profile;
    }

    @Override
    public boolean isOnline() {
        System.out.println("isOnline: " + profile.toString() + ", " + (getPlayer() != null));
        return getPlayer() != null;
    }

    @Override
    public String getName() {
        Player player = getPlayer();
        if (player != null)
            return player.getName();

        // This might not match lastKnownName but if not it should be more correct
        if (profile.getName() != null)
            return profile.getName();

        CompoundTag data = getBukkitData();
        return (data != null && data.contains("lastKnownName")) ? data.getString("lastKnownName") : null;
    }

    @Override
    public UUID getUniqueId() {
        return profile.getId();
    }

    public Server getServer() {
        return server;
    }

    @Override
    public boolean isOp() {
        return server.getHandle().getPlayerManager().isOperator(profile);
    }

    @Override
    public void setOp(boolean value) {
        if (value == isOp()) return;

        if (value) server.getHandle().getPlayerManager().addToOperators(profile);
        else server.getHandle().getPlayerManager().removeFromOperators(profile);
    }

    @Override
    public boolean isBanned() {
        return (getName() == null) ? false : server.getBanList(BanList.Type.NAME).isBanned(getName());
    }

    public void setBanned(boolean value) {
        if (getName() == null) {
            return;
        }

        if (value) {
            server.getBanList(BanList.Type.NAME).addBan(getName(), null, null, null);
        } else {
            server.getBanList(BanList.Type.NAME).pardon(getName());
        }
    }

    @Override
    public boolean isWhitelisted() {
        return server.getHandle().getPlayerManager().getWhitelist().isAllowed(profile);
    }

    @Override
    public void setWhitelisted(boolean value) {
        if (value) {
            server.getHandle().getPlayerManager().getWhitelist().add(new WhitelistEntry(profile));
        } else {
            server.getHandle().getPlayerManager().getWhitelist().remove(profile);
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        result.put("UUID", profile.getId().toString());

        return result;
    }

    public static OfflinePlayer deserialize(Map<String, Object> args) {
        // Backwards comparability
        if (args.get("name") != null)
            return Bukkit.getServer().getOfflinePlayer((String) args.get("name"));
        return Bukkit.getServer().getOfflinePlayer(UUID.fromString((String) args.get("UUID")));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[UUID=" + profile.getId() + "]";
    }

    @Override
    public Player getPlayer() {
        return server.getPlayer(getUniqueId());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof OfflinePlayer)) {
            return false;
        }

        OfflinePlayer other = (OfflinePlayer) obj;
        if ((this.getUniqueId() == null) || (other.getUniqueId() == null)) {
            return false;
        }

        return this.getUniqueId().equals(other.getUniqueId());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.getUniqueId() != null ? this.getUniqueId().hashCode() : 0);
        return hash;
    }

    private CompoundTag getData() {
        return ((IMixinWorldSaveHandler)storage).getPlayerData(getUniqueId().toString());
    }

    private CompoundTag getBukkitData() {
        CompoundTag result = getData();

        if (result != null) {
            if (!result.contains("bukkit"))
                result.put("bukkit", new CompoundTag());
            result = result.getCompound("bukkit");
        }

        return result;
    }

    private File getDataFile() {
        return new File(storage.playerDataDir, getUniqueId() + ".dat");
    }

    @Override
    public long getFirstPlayed() {
        Player player = getPlayer();
        if (player != null) return player.getFirstPlayed();

        CompoundTag data = getBukkitData();

        if (data != null) {
            if (data.contains("firstPlayed")) {
                return data.getLong("firstPlayed");
            } else {
                File file = getDataFile();
                return file.lastModified();
            }
        } else return 0;
    }

    @Override
    public long getLastPlayed() {
        Player player = getPlayer();
        if (player != null) return player.getLastPlayed();

        CompoundTag data = getBukkitData();
        return data != null ? ((data.contains("lastPlayed")) ? data.getLong("lastPlayed") : getDataFile().lastModified()) : 0;
    }

    @Override
    public boolean hasPlayedBefore() {
        return getData() != null;
    }

    @Override
    public Location getBedSpawnLocation() {
        CompoundTag data = getData();
        if (data == null) return null;

        if (data.contains("SpawnX") && data.contains("SpawnY") && data.contains("SpawnZ")) {
            String spawnWorld = data.getString("SpawnWorld");
            if (spawnWorld.equals(""))
                spawnWorld = server.getWorlds().get(0).getName();
            return new Location(server.getWorld(spawnWorld), data.getInt("SpawnX"), data.getInt("SpawnY"), data.getInt("SpawnZ"));
        }
        return null;
    }

    public void setMetadata(String metadataKey, MetadataValue metadataValue) {
        // TODO auto-generated method stub
    }

    public List<MetadataValue> getMetadata(String metadataKey) {
        // TODO auto-generated method stub
        return null;
    }

    public boolean hasMetadata(String metadataKey) {
        // TODO auto-generated method stub
        return false;
    }

    public void removeMetadata(String metadataKey, Plugin plugin) {
        // TODO auto-generated method stub
    }

    @Override
    public void incrementStatistic(Statistic statistic) {
        if (isOnline()) {
            getPlayer().incrementStatistic(statistic);
        } else {
            // TODO auto-generated method stub
        }
    }

    @Override
    public void decrementStatistic(Statistic statistic) {
        if (isOnline()) {
            getPlayer().decrementStatistic(statistic);
        } else {
            // TODO auto-generated method stub
        }
    }

    @Override
    public int getStatistic(Statistic statistic) {
        if (isOnline()) {
            return getPlayer().getStatistic(statistic);
        } else {
            return 0;    // TODO auto-generated method stub
        }
    }

    @Override
    public void incrementStatistic(Statistic statistic, int amount) {
        if (isOnline()) {
            getPlayer().incrementStatistic(statistic, amount);
        } else {
            // TODO auto-generated method stub
        }
    }

    @Override
    public void decrementStatistic(Statistic statistic, int amount) {
        if (isOnline()) {
            getPlayer().decrementStatistic(statistic, amount);
        } else {
            // TODO auto-generated method stub
        }
    }

    @Override
    public void setStatistic(Statistic statistic, int newValue) {
        if (isOnline()) {
            getPlayer().setStatistic(statistic, newValue);
        } else {
            // TODO auto-generated method stub
        }
    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material) {
        if (isOnline()) {
            getPlayer().incrementStatistic(statistic, material);
        } else {
            // TODO auto-generated method stub
        }
    }

    @Override
    public void decrementStatistic(Statistic statistic, Material material) {
        if (isOnline()) {
            getPlayer().decrementStatistic(statistic, material);
        } else {
            // TODO auto-generated method stub
        }
    }

    @Override
    public int getStatistic(Statistic statistic, Material material) {
        if (isOnline()) {
            return getPlayer().getStatistic(statistic, material);
        } else {
            return 0;     // TODO auto-generated method stub
        }
    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material, int amount) {
        if (isOnline()) {
            getPlayer().incrementStatistic(statistic, material, amount);
        } else {
            // TODO auto-generated method stub
        }
    }

    @Override
    public void decrementStatistic(Statistic statistic, Material material, int amount) {
        if (isOnline()) {
            getPlayer().decrementStatistic(statistic, material, amount);
        } else {
            // TODO auto-generated method stub
        }
    }

    @Override
    public void setStatistic(Statistic statistic, Material material, int newValue) {
        if (isOnline()) {
            getPlayer().setStatistic(statistic, material, newValue);
        } else {
            // TODO auto-generated method stub
        }
    }

    @Override
    public void incrementStatistic(Statistic statistic, EntityType entityType) {
        if (isOnline()) {
            getPlayer().incrementStatistic(statistic, entityType);
        } else {
            // TODO auto-generated method stub
        }
    }

    @Override
    public void decrementStatistic(Statistic statistic, EntityType entityType) {
        if (isOnline()) {
            getPlayer().decrementStatistic(statistic, entityType);
        } else {
            // TODO auto-generated method stub
        }
    }

    @Override
    public int getStatistic(Statistic statistic, EntityType entityType) {
        if (isOnline()) {
            return getPlayer().getStatistic(statistic, entityType);
        } else {
            // TODO auto-generated method stub
            return 0;
        }
    }

    @Override
    public void incrementStatistic(Statistic statistic, EntityType entityType, int amount) {
        if (isOnline()) {
            getPlayer().incrementStatistic(statistic, entityType, amount);
        } else {
            // TODO auto-generated method stub
        }
    }

    @Override
    public void decrementStatistic(Statistic statistic, EntityType entityType, int amount) {
        if (isOnline()) {
            getPlayer().decrementStatistic(statistic, entityType, amount);
        } else {
            // TODO auto-generated method stub
        }
    }

    @Override
    public void setStatistic(Statistic statistic, EntityType entityType, int newValue) {
        // TODO auto-generated method stub
    }

}