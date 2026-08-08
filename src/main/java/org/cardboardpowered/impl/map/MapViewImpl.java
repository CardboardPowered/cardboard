package org.cardboardpowered.impl.map;

import java.util.*;
import java.util.logging.Level;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import org.cardboardpowered.bridge.world.level.LevelBridge;
import org.cardboardpowered.bridge.world.level.saveddata.maps.MapItemSavedDataBridge;

public final class MapViewImpl implements MapView {

    private final Map<CraftPlayer, RenderData> renderCache = new HashMap<>();
    private final List<MapRenderer> renderers = new ArrayList<>();
    private final Map<MapRenderer, Map<CraftPlayer, MapCanvasImpl>> canvases = new HashMap<>();
    protected final MapItemSavedData worldMap;

    public MapViewImpl(MapItemSavedData worldMap) {
        this.worldMap = worldMap;
        addRenderer(new MapRendererImpl(this, worldMap));
    }

    @Override
    public int getId() {
        int id = ((MapItemSavedDataBridge) worldMap).getMapIdBF();
        if (id < 0) throw new IllegalStateException("Map has no ID assigned");
        return id;
    }

    @Override
    public boolean isVirtual() {
        return renderers.size() > 0 && !(renderers.get(0) instanceof MapRendererImpl);
    }

    @Override
    @Deprecated
    public Scale getScale() {
        return Objects.requireNonNull(Scale.valueOf(worldMap.scale));
    }

    @Override
    @Deprecated
    public void setScale(Scale scale) {
       // worldMap.scale = scale.getValue();
    }

    @Override
    public World getWorld() {
        ResourceKey<net.minecraft.world.level.Level> dimension = worldMap.dimension;
        ServerLevel world = CraftServer.server.getLevel(dimension);
        return (world == null) ? null : ((LevelBridge)world).cardboard$getWorld();
    }

    @Override
    public void setWorld(World world) {
        //worldMap.dimension = ((CraftWorld) world).getHandle().getRegistryKey();
    }

    @Override
    public int getCenterX() {
        return worldMap.centerX;
    }

    @Override
    public int getCenterZ() {
        return worldMap.centerZ;
    }

    @Override
    public void setCenterX(int x) {
        //worldMap.xCenter = x;
    }

    @Override
    public void setCenterZ(int z) {
        //worldMap.zCenter = z;
    }

    @Override
    public List<MapRenderer> getRenderers() {
        return new ArrayList<>(renderers);
    }

    @Override
    public void addRenderer(MapRenderer renderer) {
        if (!renderers.contains(renderer)) {
            renderers.add(renderer);
            canvases.put(renderer, new HashMap<>());
            renderer.initialize(this);
        }
    }

    @Override
    public boolean removeRenderer(MapRenderer renderer) {
        if (renderers.contains(renderer)) {
            renderers.remove(renderer);
            for (Map.Entry<CraftPlayer, MapCanvasImpl> entry : canvases.get(renderer).entrySet())
                for (int x = 0; x < 128; ++x)
                    for (int y = 0; y < 128; ++y)
                        entry.getValue().setPixel(x, y, (byte) -1);
            canvases.remove(renderer);
            return true;
        } else return false;
    }

    private boolean isContextual() {
        for (MapRenderer renderer : renderers)
            if (renderer.isContextual()) return true;
        return false;
    }

    public RenderData render(CraftPlayer player) {
        boolean context = isContextual();
        RenderData render = renderCache.get(context ? player : null);

        if (render == null) {
            render = new RenderData();
            renderCache.put(context ? player : null, render);
        }

        // Can try instead of 2 condition this? renderCache.isEmpty()
        if (context && renderCache.containsKey(null))
            renderCache.remove(null);

        Arrays.fill(render.buffer, (byte) 0);
        render.cursors.clear();

        for (MapRenderer renderer : renderers) {
            MapCanvasImpl canvas = canvases.get(renderer).get(renderer.isContextual() ? player : null);
            if (canvas == null) {
                canvas = new MapCanvasImpl(this);
                canvases.get(renderer).put(renderer.isContextual() ? player : null, canvas);
            }

            canvas.setBase(render.buffer);
            try {
                renderer.render(this, canvas, player);
            } catch (Throwable ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Could not render map using renderer " + renderer.getClass().getName(), ex);
            }

            byte[] buf = canvas.getBuffer();
            for (int i = 0; i < buf.length; ++i) {
                byte color = buf[i];
                // There are 208 valid color id's, 0 -> 127 and -128 -> -49
                if (color >= 0 || color <= -21) render.buffer[i] = color;
            }

            for (int i = 0; i < canvas.getCursors().size(); ++i)
                render.cursors.add(canvas.getCursors().getCursor(i));
        }

        return render;
    }

    @Override
    public boolean isTrackingPosition() {
        return false;// TODO: 1.17ify worldMap.showIcons;
    }

    @Override
    public void setTrackingPosition(boolean trackingPosition) {
        //worldMap.showIcons = trackingPosition;
    }

    @Override
    public boolean isUnlimitedTracking() {
       return false;// return worldMap.unlimitedTracking;
    }

    @Override
    public void setUnlimitedTracking(boolean unlimited) {
       // worldMap.unlimitedTracking = unlimited;
    }

    @Override
    public boolean isLocked() {
        return worldMap.locked;
    }

    @Override
    public void setLocked(boolean locked) {
       // worldMap.locked = locked;
    }

}