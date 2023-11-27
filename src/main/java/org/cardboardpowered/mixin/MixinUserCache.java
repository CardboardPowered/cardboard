package org.cardboardpowered.mixin;

import com.javazilla.bukkitfabric.interfaces.IUserCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import me.isaiah.common.ICommonMod;
import net.minecraft.util.UserCache;
import net.minecraft.util.UserCache.Entry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.bukkit.craftbukkit.CraftServer.server;

@Mixin(UserCache.class)
public class MixinUserCache implements IUserCache {

    @Shadow private Map<UUID, Entry> byUuid;
    @Shadow private Map<String, Entry> byName;
    @Shadow private GameProfileRepository profileRepository;

    @Override
    public Optional<GameProfile> card_getByUuid(UUID uuid) {
        Entry entry = this.byUuid.get(uuid);
        if (entry == null)
            return Optional.empty();
        entry.setLastAccessed(this.incrementAndGetAccessCount());
        return Optional.of(entry.getProfile());
    }

    @Override
    public Optional<GameProfile> card_findByName(String name) {
        Optional<GameProfile> optional2 = null;
        String string = name.toLowerCase(Locale.ROOT);
        Entry entry = this.byName.get(string);
        boolean bl = false;
        if (entry != null && new Date().getTime() >= entry.getExpirationDate().getTime()) {
            this.byUuid.remove(entry.getProfile().getId());
            this.byName.remove(entry.getProfile().getName().toLowerCase(Locale.ROOT));
            bl = true;
            entry = null;
        }
        if (entry != null) {
            entry.setLastAccessed(this.incrementAndGetAccessCount());
            optional2 = Optional.of(entry.getProfile());
        } else {
            optional2 = card_findProfileByName(this.profileRepository, string);
            if (optional2.isPresent()) {
                server.getUserCache().add(optional2.get());
                bl = false;
            }
        }
        if (bl)
            server.getUserCache().save();
        return optional2;
    }
 
    private static Optional<GameProfile> card_findProfileByName(GameProfileRepository repository, String name) {
        final AtomicReference<GameProfile> atomicReference = new AtomicReference();
        ProfileLookupCallback profileLookupCallback = new ProfileLookupCallback(){
            public void onProfileLookupSucceeded(GameProfile profile) {
                atomicReference.set(profile);
            }
            @Override
            public void onProfileLookupFailed(String profileName, Exception exception) {
                atomicReference.set(null);
            }
        };
        repository.findProfilesByNames(new String[]{name}, profileLookupCallback);
        GameProfile gameProfile = (GameProfile)atomicReference.get();
        if (!shouldUseRemote() && gameProfile == null) {
            
        	
        	// TODO: 1.19
        	// UUID uUID = DynamicSerializableUuid.getUuidFromProfile(new GameProfile((UUID)null, name));
        	UUID uUID = ICommonMod.getIServer().get_uuid_from_profile(new GameProfile((UUID)null, name));
        	
        	// 1.18: UUID uUID = PlayerEntity.getUuidFromProfile((GameProfile)new GameProfile(null, name));
            return Optional.of(new GameProfile(uUID, name));
        }
        return Optional.ofNullable(gameProfile);
    }

    @Shadow
    private static boolean shouldUseRemote() {
        return false;
    }

    @Shadow
    private long incrementAndGetAccessCount() {
        return 0;
    }

}
