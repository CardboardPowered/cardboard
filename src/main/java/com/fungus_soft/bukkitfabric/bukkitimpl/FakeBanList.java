package com.fungus_soft.bukkitfabric.bukkitimpl;

import java.util.Date;
import java.util.Set;

import org.bukkit.BanEntry;
import org.bukkit.BanList;

public class FakeBanList implements BanList {

    public Type type;
    public FakeBanList(Type type) {
        this.type = type;
    }

    @Override
    public BanEntry addBan(String arg0, String arg1, Date arg2, String arg3) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<BanEntry> getBanEntries() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BanEntry getBanEntry(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isBanned(String name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void pardon(String name) {
        // TODO Auto-generated method stub

    }

}
