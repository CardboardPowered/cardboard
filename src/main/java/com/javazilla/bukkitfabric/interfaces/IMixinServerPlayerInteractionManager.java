package com.javazilla.bukkitfabric.interfaces;

public interface IMixinServerPlayerInteractionManager {

    public boolean getInteractResultBF();
    public void setInteractResultBF(boolean b);

    public void setFiredInteractBF(boolean b);
    public boolean getFiredInteractBF();

}