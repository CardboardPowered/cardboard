package org.cardboardpowered.bridge.world.level.saveddata.maps;

import org.cardboardpowered.impl.map.MapViewImpl;

public interface MapItemSavedDataBridge {

    MapViewImpl getMapViewBF();

    /**
     * The numeric map id this saved data is stored under, or -1 if it is not known.
     * Vanilla keeps the id in the storage key rather than on the data itself, so it
     * has to be stamped on whenever the data is created or looked up.
     */
    int getMapIdBF();

    void setMapIdBF(int id);

}