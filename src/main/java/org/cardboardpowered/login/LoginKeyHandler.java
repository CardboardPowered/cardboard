package org.cardboardpowered.login;

import java.security.PrivateKey;

import javax.crypto.SecretKey;

import org.bukkit.craftbukkit.CraftServer;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;

/**
 * Mojang changed the onKey process of the network handler in 1.16.4
 * This class is used to retain compatibility with 1.16.3
 */
public abstract class LoginKeyHandler {

    public ServerLoginNetworkHandler.State state;
    public SecretKey secretKey;
    public PrivateKey privatekey;

    public LoginKeyHandler(ServerLoginNetworkHandler.State state, SecretKey key, PrivateKey privatekey) {
        this.state = state;
        this.secretKey = key;
        this.privatekey = privatekey;
    }

    public static LoginKeyHandler getLoginKeyHandler(ServerLoginNetworkHandler.State state, SecretKey key, PrivateKey privatekey) {
        if (CraftServer.server.getVersion().equalsIgnoreCase("1.16.3")) {
            return new Login_1_16_3(state, key, privatekey);
        } else {
            return new Login_1_16_4(state, key, privatekey);
        }
    }

    public abstract void onKey(LoginKeyC2SPacket keyPacket, ClientConnection connection, byte[] nonce, MinecraftServer server);

}