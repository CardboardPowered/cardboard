package org.cardboardpowered.bridge.world.inventory;

import org.bukkit.entity.Player;

/**
 * API Interface for LecternMenu
 *
 * <p>Vanilla's {@code LecternMenu} constructor only receives the lectern's own container, so the
 * viewing player has to be handed to it separately once {@code LecternBlockEntity#createMenu} knows
 * who is opening it.
 *
 * @implNote https://github.com/PaperMC/Paper/blob/main/paper-server/patches/sources/net/minecraft/world/inventory/LecternMenu.java.patch
 */
public interface LecternMenuBridge {

    void setPlayer(Player player);
}
