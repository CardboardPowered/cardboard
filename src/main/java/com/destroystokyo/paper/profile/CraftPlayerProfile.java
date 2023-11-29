package com.destroystokyo.paper.profile;

// import com.destroystokyo.paper.PaperConfig;

import com.google.common.base.Charsets;
import com.javazilla.bukkitfabric.interfaces.IUserCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.UserCache;
import net.minecraft.util.Util;

import org.apache.commons.lang3.Validate;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.profile.PlayerTextures;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spigotmc.SpigotConfig;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CraftPlayerProfile implements PlayerProfile {

    private GameProfile profile;
    private final PropertySet properties = new PropertySet();
 
    public CraftPlayerProfile(PlayerImpl player) {
        this.profile = player.getHandle().getGameProfile();
    }

    public CraftPlayerProfile(UUID id, String name) {
        this.profile = new GameProfile(id, name);
    }

    public CraftPlayerProfile(GameProfile profile) {
        Validate.notNull(profile, "GameProfile cannot be null!");
        this.profile = profile;
    }

    @Override
    public boolean hasProperty(String property) {
        return profile.getProperties().containsKey(property);
    }

    @Override
    public void setProperty(ProfileProperty property) {
        String name = property.getName();
        PropertyMap properties = profile.getProperties();
        properties.removeAll(name);
        properties.put(name, new Property(name, property.getValue(), property.getSignature()));
    }

    public GameProfile getGameProfile() {
        return profile;
    }

    @Override
    public UUID getId() {
        return profile.getId();
    }

    @Override
    public UUID setId(UUID uuid) {
        GameProfile prev = this.profile;
        this.profile = new GameProfile(uuid, prev.getName());
        copyProfileProperties(prev, this.profile);
        return prev.getId();
    }

    @Override
    public String getName() {
        return profile.getName();
    }

    @Override
    public String setName(String name) {
        GameProfile prev = this.profile;
        this.profile = new GameProfile(prev.getId(), name);
        copyProfileProperties(prev, this.profile);
        return prev.getName();
    }

    @Override
    public Set<ProfileProperty> getProperties() {
        return properties;
    }

    @Override
    public void setProperties(Collection<ProfileProperty> properties) {
        properties.forEach(this::setProperty);
    }

    @Override
    public void clearProperties() {
        profile.getProperties().clear();
    }

    @Override
    public boolean removeProperty(String property) {
        return !profile.getProperties().removeAll(property).isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CraftPlayerProfile that = (CraftPlayerProfile) o;
        return Objects.equals(profile, that.profile);
    }

    @Override
    public int hashCode() {
        return profile.hashCode();
    }

    @Override
    public String toString() {
        return profile.toString();
    }

    @Override
    public CraftPlayerProfile clone() {
        CraftPlayerProfile clone = new CraftPlayerProfile(this.getId(), this.getName());
        clone.setProperties(getProperties());
        return clone;
    }

    @Override
    public boolean isComplete() {
        return profile.isComplete();
    }

    @Override
    public boolean completeFromCache() {
        MinecraftServer server = CraftServer.INSTANCE.getServer();
        return completeFromCache(false, server.isOnlineMode() || (SpigotConfig.bungee /*&& PaperConfig.bungeeOnlineMode*/));
    }

    public boolean completeFromCache(boolean onlineMode) {
        return completeFromCache(false, onlineMode);
    }

    public boolean completeFromCache(boolean lookupUUID, boolean onlineMode) {
        MinecraftServer server = CraftServer.INSTANCE.getServer();
        String name = profile.getName();
        IUserCache userCache = CraftServer.getUC();
        if (profile.getId() == null) {
            final GameProfile profile;
            if (onlineMode) {
                profile = lookupUUID ? userCache.card_findByName(name).get() : userCache.card_findByName(name).get();
            } else {
                // Make an OfflinePlayer using an offline mode UUID since the name has no profile
                profile = new GameProfile(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8)), name);
            }
            if (profile != null) {
                // if old has it, assume its newer, so overwrite, else use cached if it was set and ours wasn't
                copyProfileProperties(this.profile, profile);
                this.profile = profile;
            }
        }

        if ((profile.getName() == null || !hasTextures()) && profile.getId() != null) {
            Optional<GameProfile> o = userCache.card_getByUuid(this.profile.getId());
            if (!o.isEmpty()) {
                GameProfile profile = o.get();
                if (profile != null) {
                    // if old has it, assume its newer, so overwrite, else use cached if it was set and ours wasn't
                    copyProfileProperties(this.profile, profile);
                    this.profile = profile;
                }
            }
        }
        return this.profile.isComplete();
    }

    public boolean complete(boolean textures) {
        MinecraftServer server = CraftServer.INSTANCE.getServer();
        return complete(textures, server.isOnlineMode() || (SpigotConfig.bungee /*&& PaperConfig.bungeeOnlineMode*/));
    }
    public boolean complete(boolean textures, boolean onlineMode) {
        MinecraftServer server = CraftServer.INSTANCE.getServer();

        boolean isCompleteFromCache = this.completeFromCache(true, onlineMode);
        if (onlineMode && (!isCompleteFromCache || textures && !hasTextures())) {
            GameProfile result = server.getSessionService().fillProfileProperties(profile, true);
            if (result != null)
                copyProfileProperties(result, this.profile, true);
            if (this.profile.isComplete()) {
                CraftServer.server.getUserCache().add(this.profile);
                CraftServer.server.getUserCache().save();
            }
        }
        return profile.isComplete() && (!onlineMode || !textures || hasTextures());
    }

    private static void copyProfileProperties(GameProfile source, GameProfile target) {
        copyProfileProperties(source, target, false);
    }

    private static void copyProfileProperties(GameProfile source, GameProfile target, boolean clearTarget) {
        PropertyMap sourceProperties = source.getProperties();
        PropertyMap targetProperties = target.getProperties();
        if (clearTarget) targetProperties.clear();
        if (sourceProperties.isEmpty()) return;

        for (Property property : sourceProperties.values()) {
            targetProperties.removeAll(property.getName());
            targetProperties.put(property.getName(), property);
        }
    }

    private static ProfileProperty toBukkit(Property property) {
        return new ProfileProperty(property.getName(), property.getValue(), property.getSignature());
    }

    public static PlayerProfile asBukkitCopy(GameProfile gameProfile) {
        CraftPlayerProfile profile = new CraftPlayerProfile(gameProfile.getId(), gameProfile.getName());
        copyProfileProperties(gameProfile, profile.profile);
        return profile;
    }

    public static PlayerProfile asBukkitMirror(GameProfile profile) {
        return new CraftPlayerProfile(profile);
    }

    public static Property asAuthlib(ProfileProperty property) {
        return new Property(property.getName(), property.getValue(), property.getSignature());
    }

    public static GameProfile asAuthlibCopy(PlayerProfile profile) {
        CraftPlayerProfile craft = ((CraftPlayerProfile) profile);
        return asAuthlib(craft.clone());
    }

    public static GameProfile asAuthlib(PlayerProfile profile) {
        CraftPlayerProfile craft = ((CraftPlayerProfile) profile);
        return craft.getGameProfile();
    }

    private class PropertySet extends AbstractSet<ProfileProperty> {

        @Override
        public Iterator<ProfileProperty> iterator() {
            return new ProfilePropertyIterator(profile.getProperties().values().iterator());
        }

        @Override
        public int size() {
            return profile.getProperties().size();
        }

        @Override
        public boolean add(ProfileProperty property) {
            setProperty(property);
            return true;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean addAll(Collection<? extends ProfileProperty> c) {
            setProperties((Collection<ProfileProperty>) c);
            return true;
        }

        @Override
        public boolean contains(Object o) {
            return o instanceof ProfileProperty && profile.getProperties().containsKey(((ProfileProperty) o).getName());
        }

        private class ProfilePropertyIterator implements Iterator<ProfileProperty> {
            private final Iterator<Property> iterator;

            ProfilePropertyIterator(Iterator<Property> iterator) {
                this.iterator = iterator;
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public ProfileProperty next() {
                return toBukkit(iterator.next());
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        }
    }

	@Override
	public @Nullable UUID getUniqueId() {
		// TODO Auto-generated method stub
		return getId();
	}

	@Override
    public @NotNull CompletableFuture<PlayerProfile> update() {
        return CompletableFuture.supplyAsync(() -> {
            final CraftPlayerProfile clone = clone();
            clone.complete(true);
            return clone;
        }, Util.getMainWorkerExecutor());
    }


	@Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        if (this.getId() != null) {
            map.put("uniqueId", this.getId().toString());
        }
        if (this.getName() != null) {
            map.put("name", getName());
        }
        if (!this.properties.isEmpty()) {
            List<Object> propertiesData = new ArrayList<>();
            for (ProfileProperty property : properties) {
                // propertiesData.add(CraftProfileProperty.serialize(new Property(property.getName(), property.getValue(), property.getSignature())));
            }
            map.put("properties", propertiesData);
        }
        return map;
    }
	@Override
	public @NotNull PlayerTextures getTextures() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTextures(@Nullable PlayerTextures arg0) {
		// TODO Auto-generated method stub
		
	}

}