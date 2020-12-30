package org.cardboardpowered.login;

import java.lang.reflect.Method;
import java.security.PrivateKey;

import javax.crypto.SecretKey;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerLoginNetworkHandler.State;

/**
 * @deprecated We no longer support 1.16.3
 */
@Deprecated
public class Login_1_16_3 extends LoginKeyHandler {

    public Login_1_16_3(State state, SecretKey key, PrivateKey privatekey) {
        super(state, key, privatekey);
    }

    @Override
    public void onKey(LoginKeyC2SPacket keyPacket, ClientConnection connection, byte[] nonce, MinecraftServer server) {
        try {
            this.secretKey = keyPacket.decryptSecretKey(privatekey);
            this.state = ServerLoginNetworkHandler.State.AUTHENTICATING;
            Method m = connection.getClass().getDeclaredMethod("method_10746", SecretKey.class);
            m.invoke(connection, this.secretKey);
        } catch (Exception e) {
            throw new IllegalStateException("Protocol error", e);
        }
    }

}