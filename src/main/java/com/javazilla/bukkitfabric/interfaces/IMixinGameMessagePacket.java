package com.javazilla.bukkitfabric.interfaces;

import net.md_5.bungee.api.chat.BaseComponent;

public interface IMixinGameMessagePacket {

    public BaseComponent[] getBungeeComponents();

    public void setBungeeComponents(BaseComponent[] components);

}