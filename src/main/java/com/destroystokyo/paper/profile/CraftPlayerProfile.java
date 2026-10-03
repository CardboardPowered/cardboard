package com.destroystokyo.paper.profile;

// import com.destroystokyo.paper.PaperConfig;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.datafixers.util.Either;

import io.papermc.paper.profile.MutablePropertyMap;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.Validate;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.profile.CraftPlayerTextures;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.craftbukkit.entity.CraftPlayer;
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

public class CraftPlayerProfile implements PlayerProfile, SharedPlayerProfile {

	private boolean emptyName;
	private boolean emptyUUID;
	
    private GameProfile profile;
    private final PropertySet properties = new PropertySet();
 
    public CraftPlayerProfile(CraftPlayer player) {
        this.profile = player.getHandle().getGameProfile();
    }

    public CraftPlayerProfile(UUID id, String name) {
    	this.profile = createAuthLibProfile(id, name);
        this.emptyName = name == null;
        this.emptyUUID = id == null;
    }

    public CraftPlayerProfile(GameProfile profile) {
        Validate.notNull(profile, "GameProfile cannot be null!");
        this.profile = profile;
    }
    
	public CraftPlayerProfile(NameAndId nameAndId) {
		this(nameAndId.id(), nameAndId.name());
	}

	public CraftPlayerProfile(ResolvableProfile resolvableProfile) {
	      this(resolvableProfile.unpack().map(GameProfile::id, p -> p.id().orElse(null)), resolvableProfile.unpack().map(GameProfile::name, p -> p.name().orElse(null)));
	      copyProfileProperties(resolvableProfile.partialProfile(), this.profile);
	}
	
	private static GameProfile createAuthLibProfile(UUID uniqueId, String name) {
        // Preconditions.checkArgument(name == null || name.length() <= 16, "Name cannot be longer than 16 characters");
        // Preconditions.checkArgument(name == null || StringUtil.isValidPlayerName(name), "The name of the profile contains invalid characters: %s", name);
        return new GameProfile(
            uniqueId != null ? uniqueId : Util.NIL_UUID,
            name != null ? name : "",
            new MutablePropertyMap()
        );
    }

	@Override
    public boolean hasProperty(String property) {
        return profile.properties().containsKey(property);
    }

    @Override
    public void setProperty(ProfileProperty property) {
        String name = property.getName();
        PropertyMap properties = profile.properties();
        properties.removeAll(name);
        properties.put(name, new Property(name, property.getValue(), property.getSignature()));
    }

    public GameProfile getGameProfile() {
        return profile;
    }

    @Override
    public UUID getId() {
        return profile.id();
    }

    @Override
    public UUID setId(UUID uuid) {
        GameProfile prev = this.profile;
        this.profile = createAuthLibProfile(uuid, prev.name());
        copyProfileProperties(prev, this.profile);
        return prev.id();
    }

    @Override
    public String getName() {
        return profile.name();
    }

    @Override
    public String setName(String name) {
        GameProfile prev = this.profile;
        this.profile = createAuthLibProfile(prev.id(), name);
        copyProfileProperties(prev, this.profile);
        return prev.name();
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
        profile.properties().clear();
    }

    @Override
    public boolean removeProperty(String property) {
        return !profile.properties().removeAll(property).isEmpty();
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
        return this.getUniqueId() != null && this.getName() != null && !getTextures().isEmpty();
    }

    @Override
    public boolean completeFromCache() {
        MinecraftServer server = CraftServer.INSTANCE.getServer();
        return completeFromCache(false, server.usesAuthentication() || (SpigotConfig.bungee /*&& PaperConfig.bungeeOnlineMode*/));
    }

    public boolean completeFromCache(boolean onlineMode) {
        return completeFromCache(false, onlineMode);
    }

    /*
    public boolean completeFromCache(boolean lookupUUID, boolean onlineMode) {
        MinecraftServer server = CraftServer.INSTANCE.getServer();
        String name = profile.name();
        IUserCache userCache = CraftServer.getUC();
        if (profile.id() == null) {
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

        if ((profile.name() == null || !hasTextures()) && profile.id() != null) {
            Optional<GameProfile> o = userCache.card_getByUuid(this.profile.id());
            if (o.isPresent()) {
                GameProfile profile = o.get();
                if (profile != null) {
                    // if old has it, assume its newer, so overwrite, else use cached if it was set and ours wasn't
                    copyProfileProperties(this.profile, profile);
                    this.profile = profile;
                }
            }
        }

        return isProfileComplete();
    }
    */
    
    // PlayerConfigEntry.toUncompletedGameProfile
    public GameProfile PlayerConfigEntry_toUncompletedGameProfile(NameAndId thiz) {
        return new GameProfile(thiz.id(), thiz.name());
    }
    
    public boolean completeFromCache(boolean lookupUUID, boolean onlineMode) {
        MinecraftServer server = CraftServer.INSTANCE.getServer();
        String name = this.profile.name();
        if (this.getId() == null) {
           GameProfile profile;
           if (onlineMode) {
              profile = /*server.getApiServices().paper().filledProfileCache()*/
            		  CraftServer.INSTANCE.getPaperFilledProfileCache().getIfCached(name);
              if (profile == null && lookupUUID) {
                 NameAndId nameAndId = server.services().nameToIdCache().get(name).orElse(null);
                 if (nameAndId != null) {
                	 profile = PlayerConfigEntry_toUncompletedGameProfile(nameAndId);
                    // profile = nameAndId.toUncompletedGameProfile();
                 }
              }
           } else {
              profile = UUIDUtil.createOfflineProfile(name);
           }

           if (profile != null) {
              GameProfile copy = new GameProfile(profile.id(), profile.name(), new MutablePropertyMap());
              copy.properties().putAll(profile.properties());
              copyProfileProperties(this.profile, copy);
              this.profile = copy;
              this.emptyUUID = false;
           }
        }

        if ((this.profile.name().isEmpty() || !this.hasTextures()) && this.getId() != null) {
           GameProfile profilex = /*server.getApiServices().paper().filledProfileCache()*/
        		   CraftServer.INSTANCE.getPaperFilledProfileCache().getIfCached(this.profile.id());
           if (profilex == null) {
              Optional<NameAndId> nameAndId = server.services().nameToIdCache().get(this.profile.id());
              if (nameAndId.isPresent()) {
                 profilex = PlayerConfigEntry_toUncompletedGameProfile(nameAndId.get());
              }
           }

           if (profilex != null) {
              if (this.profile.name().isEmpty()) {
                 GameProfile copy = new GameProfile(profilex.id(), profilex.name(), new MutablePropertyMap());
                 copy.properties().putAll(profilex.properties());
                 copyProfileProperties(this.profile, copy);
                 this.profile = copy;
                 this.emptyName = false;
              } else if (profilex != this.profile) {
                 copyProfileProperties(profilex, this.profile);
              }
           }
        }

        return this.isComplete();
     }

    public boolean complete(boolean textures) {
        MinecraftServer server = CraftServer.INSTANCE.getServer();
        return complete(textures, server.usesAuthentication() || (SpigotConfig.bungee /*&& PaperConfig.bungeeOnlineMode*/));
    }

    /*
    public boolean complete(boolean textures, boolean onlineMode) {
        MinecraftServer server = CraftServer.INSTANCE.getServer();

        boolean isCompleteFromCache = this.completeFromCache(true, onlineMode);
        if (onlineMode && (!isCompleteFromCache || textures && !hasTextures())) {
            GameProfile result = null; // TODO
            if (result != null)
                copyProfileProperties(result, this.profile, true);
            if (isProfileComplete()) {
                CraftServer.server.getUserCache().add(this.profile);
                CraftServer.server.getUserCache().save();
            }
        }
        return isProfileComplete() && (!onlineMode || !textures || hasTextures());
    }
    */

    public boolean complete(boolean textures, boolean onlineMode) {
        if (!this.isComplete() || textures && !this.hasTextures()) {
           MinecraftServer server = CraftServer.server;// MinecraftServer.getServer();
           boolean isCompleteFromCache = this.completeFromCache(true, onlineMode);
           if (onlineMode && (!isCompleteFromCache || textures && !this.hasTextures())) {
              ProfileResult result = server.services().sessionService().fetchProfile(this.profile.id(), true);
              if (result != null && result.profile() != null) {
                 copyProfileProperties(result.profile(), this.profile, true);
              }

              if (this.isComplete()) {
                 GameProfile copy = new GameProfile(this.profile.id(), this.profile.name(), new PropertyMap(this.profile.properties()));
                 // TODO 1.21.9
                 // server.getApiServices().paper().filledProfileCache().add(copy);
                 CraftServer.INSTANCE.getPaperFilledProfileCache().add(copy);
              }
           }

           return this.isComplete() && (!onlineMode || !textures || this.hasTextures());
        } else {
           return true;
        }
     }

    private boolean isProfileComplete() {
        return profile.id() != null && StringUtils.isNotBlank(profile.name());
    }

    private static void copyProfileProperties(GameProfile source, GameProfile target) {
        copyProfileProperties(source, target, false);
    }

    private static void copyProfileProperties(GameProfile source, GameProfile target, boolean clearTarget) {
        PropertyMap sourceProperties = source.properties();
        PropertyMap targetProperties = target.properties();
        if (clearTarget) targetProperties.clear();
        if (sourceProperties.isEmpty()) return;

        for (Property property : sourceProperties.values()) {
            targetProperties.removeAll(property.name());
            targetProperties.put(property.name(), property);
        }
    }

    private static ProfileProperty toBukkit(Property property) {
        return new ProfileProperty(property.name(), property.value(), property.signature());
    }

    public static PlayerProfile asBukkitCopy(GameProfile gameProfile) {
        CraftPlayerProfile profile = new CraftPlayerProfile(gameProfile.id(), gameProfile.name());
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
            return new ProfilePropertyIterator(profile.properties().values().iterator());
        }

        @Override
        public int size() {
            return profile.properties().size();
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
            return o instanceof ProfileProperty && profile.properties().containsKey(((ProfileProperty) o).getName());
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
        }, Util.backgroundExecutor());
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
		return new CraftPlayerTextures(this);
	}

	@Override
	public void setTextures(@Nullable PlayerTextures textures) {
		if (textures == null) {
			this.removeProperty("textures");
		} else {
			CraftPlayerTextures craftPlayerTextures = new CraftPlayerTextures(this);
			craftPlayerTextures.copyFrom(textures);
			craftPlayerTextures.rebuildPropertyIfDirty();
		}
	}

    public GameProfile buildGameProfile() {
        return new GameProfile(this.profile.id(), this.profile.name(), new MutablePropertyMap(this.profile.properties()));
    }

    static final String PROPERTY_NAME = "textures";
    
    public static GameProfile validateSkullProfile(GameProfile gameProfile) {
        // The GameProfile needs to contain either both a uuid and textures, or a name.
        // The GameProfile always has a name or a uuid, so checking if it has a name is sufficient.
        boolean isValidSkullProfile = (gameProfile.name() != null)
                || gameProfile.properties().containsKey(PROPERTY_NAME);
        // Preconditions.checkArgument(isValidSkullProfile, "The skull profile is missing a name or textures!");
        return gameProfile;
    }

	@Override
	public @Nullable Property getProperty(@NotNull String var1) {
		return (Property)Iterables.getFirst(this.profile.properties().get(var1), null);
	}

	@Override
	public void setProperty(@NotNull String propertyName, @Nullable Property property) {
		if (property != null) {
			this.setProperty(new ProfileProperty(propertyName, property.value(), property.signature()));
		} else {
			this.profile.properties().removeAll(propertyName);
		}
	}

	// accessible method net/minecraft/component/type/ProfileComponent$Dynamic <init> (Lcom/mojang/datafixers/util/Either;Lnet/minecraft/entity/player/SkinTextures$SkinOverride;)V
	
	@Override
	public @NotNull ResolvableProfile buildResolvableProfile() {
		return (ResolvableProfile)(this.emptyName != this.emptyUUID && this.properties.isEmpty()
		         ? new ResolvableProfile.Dynamic(this.emptyName ? Either.right(this.profile.id()) : Either.left(this.profile.name()), PlayerSkin.Patch.EMPTY)
		         : ResolvableProfile.createResolved(this.buildGameProfile()));
	}

	public static ResolvableProfile asResolvableProfileCopy(@Nullable PlayerProfile profile2) {
		return ((SharedPlayerProfile)profile2).buildResolvableProfile();
	}

}
