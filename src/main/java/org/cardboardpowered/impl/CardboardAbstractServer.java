package org.cardboardpowered.impl;

import java.io.File;
import java.util.logging.Level;

import org.cardboardpowered.impl.command.VersionCommand;
import org.cardboardpowered.impl.util.CardboardCachedServerIcon;

import net.minecraft.SharedConstants;
import net.minecraft.server.dedicated.DedicatedServer;

public abstract class CardboardAbstractServer implements org.bukkit.Server {

	public static final String API_VERSION = "26.2";

	public final String serverName = "Cardboard";
	
	public final String serverVersion;
    public final String shortVersion;
    
    public CardboardCachedServerIcon icon;
    public static DedicatedServer server;
	
    public CardboardAbstractServer(DedicatedServer dserver) {
    	server = dserver;
    	String hash = VersionCommand.getGitHash().substring(0,7); // use short hash
        serverVersion = "git-Cardboard-" + hash;
        shortVersion = "git-" + hash;
	}

	@Override
    public String toString() {
        return "CraftServer{" + "serverName=" + serverName + ",serverVersion=" + serverVersion + ",minecraftVersion=" + SharedConstants.getCurrentVersion().name() + '}';
    }
	
    @Override
    public String getName() {
        return serverName;
    }
    
    public String getShortVersion() {
        return shortVersion + " (MC: " + server.getServerVersion() + ")";
    }
    
    public void loadIcon() {
        icon = new CardboardCachedServerIcon(null);
        try {
            final File file = new File(new File("."), "server-icon.png");
            if (file.isFile()) {
                icon = CardboardCachedServerIcon.createFromFile(file);
            }
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Couldn't load server icon", ex);
        }
    }

    @Override
    public CardboardCachedServerIcon getServerIcon() {
        return icon;
    }
    
}
