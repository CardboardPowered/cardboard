package spoutcraft.api;

import java.io.File;
import java.util.logging.Logger;

public interface SpoutClient {

    public String getName();

    public String getVersion();

    public Logger getLogger();

    public File getUpdateFolder();

    // TODO public SkyManager getSkyManager();

    // TODO public KeyBindingManager getKeyBindingManager();

    // TODO public BiomeManager getBiomeManager();

    // TODO public MaterialManager getMaterialManager();

    public boolean isSpoutEnabled();

    public long getServerVersion();

    public File getAudioCache();

    public File getTemporaryCache();

    public File getTextureCache();

    public File getTexturePackFolder();

    public File getStatsFolder();

    public long getTick();

    public Mode getMode();

    // TODO public RenderDelegate getRenderDelegate();

    public enum Mode {
        Single_Player,
        Multiplayer,
        Menu;
    }

    // TODO public WidgetManager getWidgetManager();

    public boolean hasPermission(String permission);

}