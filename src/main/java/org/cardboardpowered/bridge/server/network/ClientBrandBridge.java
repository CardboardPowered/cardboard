package org.cardboardpowered.bridge.server.network;

/**
 * 26.2: Paper tracks the client brand on its patched connection class.
 * Cardboard captures it from the vanilla {@code minecraft:brand} custom payload instead.
 */
public interface ClientBrandBridge {

    String cardboard_getClientBrand();

    void cardboard_setClientBrand(String brand);
}
