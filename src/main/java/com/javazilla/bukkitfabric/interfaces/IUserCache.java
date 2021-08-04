package com.javazilla.bukkitfabric.interfaces;

import java.util.Optional;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

public interface IUserCache {

    Optional<GameProfile> card_getByUuid(UUID uuid);

    Optional<GameProfile> card_findByName(String name);

}