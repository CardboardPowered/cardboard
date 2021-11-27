package org.cardboardpowered.util;

import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.JsonObject;

import net.minecraft.MinecraftVersion;
import net.minecraft.util.JsonHelper;

public class GameVersion {

    /*
     */
    public static GameVersion create() {
        if (null != INSTANCE) return INSTANCE;
        try (InputStream inputStream = MinecraftVersion.class.getResourceAsStream("/version.json");){
            if (inputStream == null) return null;
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream);){
                return new GameVersion(JsonHelper.deserialize(inputStreamReader));
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Bad version info", exception);
        }
    }

    public static GameVersion INSTANCE;

    private final int protocolVersion;
    private final String releaseTarget;
    public final int world_version;

    public GameVersion(JsonObject jsonObject) {
        INSTANCE = this;
        this.releaseTarget = JsonHelper.getString(jsonObject, "release_target");
        this.protocolVersion = JsonHelper.getInt(jsonObject, "protocol_version");
        this.world_version = JsonHelper.getInt(jsonObject, "world_version");
    }

    /**
     */
    public String getReleaseTarget() {
        return releaseTarget;
    }

    /**
     */
    public int getProtocolVersion() {
        return protocolVersion;
    }

}
