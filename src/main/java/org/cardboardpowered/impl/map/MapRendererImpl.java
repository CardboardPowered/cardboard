package org.cardboardpowered.impl.map;

import net.minecraft.item.map.MapIcon;
import net.minecraft.item.map.MapState;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class MapRendererImpl extends MapRenderer {

    private final MapState worldMap;

    public MapRendererImpl(MapViewImpl mapView, MapState worldMap) {
        super(false);
        this.worldMap = worldMap;
    }

    @Override
    @Deprecated
    public void render(MapView map, MapCanvas canvas, Player player) {
        // Map
        for (int x = 0; x < 128; ++x)
            for (int y = 0; y < 128; ++y) canvas.setPixel(x, y, worldMap.colors[y * 128 + x]);

        // Cursors
        //MapCursorCollection cursors = canvas.getCursors();
        //while (cursors.size() > 0)
        //    cursors.removeCursor(cursors.getCursor(0));
     // TODO: 1.17ify
       /* for (Object key : worldMap.icons.keySet()) {
            // If this cursor is for a player check visibility with vanish system
            Player other = Bukkit.getPlayerExact((String) key);
            if (other != null && !player.canSee(other)) continue;

            MapIcon decoration = worldMap.icons.get(key);
            cursors.addCursor(decoration.getX(), decoration.getZ(), (byte) (decoration.getRotation() & 15), decoration.getType().getId(), true, CraftChatMessage.fromComponent(decoration.getText()));
        }*/

    }

}