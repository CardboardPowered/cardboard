package com.javazilla.bukkitfabric.interfaces;

public interface IMixinServerPlayerInteractionManager {

    boolean getInteractResultBF();
    void setInteractResultBF(boolean b);

    void setFiredInteractBF(boolean b);
    boolean getFiredInteractBF();

}