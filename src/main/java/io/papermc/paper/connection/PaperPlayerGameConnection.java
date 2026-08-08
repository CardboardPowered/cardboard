package io.papermc.paper.connection;

// import io.papermc.paper.connection.HorriblePlayerLoginEventHack;
import java.util.Set;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;

public class PaperPlayerGameConnection extends PaperCommonConnection<ServerGamePacketListenerImpl> implements PlayerGameConnection {

    public PaperPlayerGameConnection(ServerGamePacketListenerImpl serverConfigurationPacketListenerImpl) {
        super(serverConfigurationPacketListenerImpl);
    }

    @Override
    public ClientInformation getClientInformation() {
        return ((ServerGamePacketListenerImpl)this.handle).player.clientInformation();
    }
    
    @Override
    public void reenterConfiguration() {
    	System.out.println("WARNING: Attempted to use PlayerGameConnection#reenterConfiguration()");
    }

    /*
    public void reenterConfiguration() {
        if (((ServerPlayNetworkHandler)this.handle).connection.savedPlayerForLoginEventLegacy != null) {
            HorriblePlayerLoginEventHack.warnReenterConfiguration();
            return;
        }
        ((ServerPlayNetworkHandler)this.handle).reconfigure();
    }
    */

    public Player getPlayer() {
        return (Player) ((ServerPlayerBridge) ((ServerGamePacketListenerImpl)this.handle).getPlayer() ).getBukkitEntity();
    }

    public void sendPluginMessage(Plugin source, String channel, byte[] message) {
        this.getPlayer().sendPluginMessage(source, channel, message);
    }

    public Set<String> getListeningPluginChannels() {
    	return this.getPlayer().getListeningPluginChannels();
    }

	@Override
	public boolean isConnected() {
		return getPlayer().isConnected();
	}


    // 26.2: new on PlayerCommonConnection
    @Override
    public String getClientBrandName() {
        // captured from the minecraft:brand custom payload by ServerCommonPacketListenerImplMixin_Brand
        return ((org.cardboardpowered.bridge.server.network.ClientBrandBridge) this.handle).cardboard_getClientBrand();
    }

}