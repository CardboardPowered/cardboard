/**
 * CardboardPowered - Bukkit/Spigot for Fabric
 * Copyright (C) CardboardPowered.org and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.mixin.world.level.saveddata.maps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.map.CraftMapCursor;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapRenderer;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.world.level.saveddata.maps.MapItemSavedDataBridge;
import org.cardboardpowered.impl.map.MapRendererImpl;
import org.cardboardpowered.impl.map.MapViewImpl;
import org.cardboardpowered.impl.map.RenderData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

/**
 * Vanilla builds the map update packet straight out of MapItemSavedData#colors, which no
 * Bukkit MapRenderer ever writes to. Without this a plugin can add a renderer and register
 * it against a MapView, but the client is still shown the terrain the server drew.
 *
 * Substitute the Bukkit-rendered pixels and cursors into the packet, the way CraftBukkit does.
 * Both maps held by a player and maps hanging in item frames go through this method.
 */
@Mixin(targets = "net.minecraft.world.level.saveddata.maps.MapItemSavedData$HoldingPlayer")
public class MapItemSavedData_HoldingPlayerMixin {

    @Shadow @Final public Player player;

    @Shadow private boolean dirtyData;
    @Shadow private int minDirtyX;
    @Shadow private int minDirtyY;
    @Shadow private int maxDirtyX;
    @Shadow private int maxDirtyY;
    @Shadow private boolean dirtyDecorations;
    @Shadow private int tick;

    @Inject(method = "nextUpdatePacket", at = @At("HEAD"), cancellable = true)
    private void cardboard$renderWithBukkitRenderers(MapId id, CallbackInfoReturnable<Packet<?>> cir) {
        MapItemSavedData worldmap = this.player.level().getMapData(id);
        if (worldmap == null) return;

        MapViewImpl view = ((MapItemSavedDataBridge) worldmap).getMapViewBF();
        if (view == null) return;

        // An ordinary map only carries Cardboard's own renderer, which just copies the
        // vanilla colours back out again. Leave those to vanilla, it is a lot cheaper.
        List<MapRenderer> renderers = view.getRenderers();
        if (renderers.size() == 1 && renderers.get(0) instanceof MapRendererImpl) return;

        if (!this.dirtyData && this.tick % 5 != 0) {
            // Nothing would be sent this tick, so do not pay for a render.
            this.tick++;
            cir.setReturnValue(null);
            return;
        }

        RenderData render = view.render((CraftPlayer) ((EntityBridge) this.player).getBukkitEntity());

        MapItemSavedData.MapPatch patch = null;
        if (this.dirtyData) {
            this.dirtyData = false;

            int startX = this.minDirtyX;
            int startY = this.minDirtyY;
            int width = this.maxDirtyX + 1 - startX;
            int height = this.maxDirtyY + 1 - startY;
            byte[] colors = new byte[width * height];

            for (int x = 0; x < width; ++x)
                for (int y = 0; y < height; ++y)
                    colors[x + y * width] = render.buffer[startX + x + (startY + y) * 128];

            patch = new MapItemSavedData.MapPatch(startX, startY, width, height, colors);
        }

        // Cursors come from the renderers rather than from the world, so they have to be
        // resent on the same five tick cadence vanilla uses for its own decorations.
        Collection<MapDecoration> icons = null;
        if (this.tick++ % 5 == 0) {
            this.dirtyDecorations = false;
            icons = new ArrayList<>();

            for (MapCursor cursor : render.cursors) {
                if (cursor.isVisible()) {
                    icons.add(new MapDecoration(CraftMapCursor.CraftType.bukkitToMinecraftHolder(cursor.getType()),
                            cursor.getX(), cursor.getY(), cursor.getDirection(),
                            CraftChatMessage.fromStringOrOptional(cursor.getCaption())));
                }
            }
        }

        cir.setReturnValue((icons == null && patch == null) ? null
                : new ClientboundMapItemDataPacket(id, worldmap.scale, worldmap.locked, icons, patch));
    }

}
