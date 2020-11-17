package org.cardboardpowered.login;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.apache.commons.lang3.Validate;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkEncryptionUtils;
import net.minecraft.network.encryption.NetworkEncryptionException;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerLoginNetworkHandler.State;

public class Login_1_16_4 extends LoginKeyHandler {

    public Login_1_16_4(State state, SecretKey key, PrivateKey privatekey) {
        super(state, key, privatekey);
    }

    @SuppressWarnings("unused")
    @Override
    public void onKey(LoginKeyC2SPacket keyPacket, ClientConnection connection, byte[] nonce, MinecraftServer server) {
        Validate.validState(this.state == ServerLoginNetworkHandler.State.KEY, "Unexpected key packet", new Object[0]);
        PrivateKey privatekey = server.getKeyPair().getPrivate();

        try {
            if (!Arrays.equals(nonce, keyPacket.decryptNonce(privatekey))) {
                throw new IllegalStateException("Protocol error");
            }
            this.secretKey = keyPacket.decryptSecretKey(privatekey);
            Cipher cipher = NetworkEncryptionUtils.cipherFromKey(2, this.secretKey);
            Cipher cipher2 = NetworkEncryptionUtils.cipherFromKey(1, this.secretKey);
            String string = new BigInteger(NetworkEncryptionUtils.generateServerId("", server.getKeyPair().getPublic(), this.secretKey)).toString(16);
            this.state = ServerLoginNetworkHandler.State.AUTHENTICATING;
            connection.setupEncryption(cipher, cipher2);
        } catch (NetworkEncryptionException networkEncryptionException) {
            throw new IllegalStateException("Protocol error", networkEncryptionException);
        }
    }

}