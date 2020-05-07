package com.fungus_soft.bukkitfabric.bukkitimpl;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.UnsafeValues;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;

public class FakeUnsafe implements UnsafeValues {

    @Override
    public void checkSupported(PluginDescriptionFile arg0) throws InvalidPluginException {
        // TODO Auto-generated method stub
    }

    @Override
    public Material fromLegacy(Material arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Material fromLegacy(MaterialData arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Material fromLegacy(MaterialData arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BlockData fromLegacy(Material arg0, byte arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getDataVersion() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Material getMaterial(String arg0, int arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Advancement loadAdvancement(NamespacedKey arg0, String arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ItemStack modifyItemStack(ItemStack arg0, String arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] processClass(PluginDescriptionFile arg0, String arg1, byte[] arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean removeAdvancement(NamespacedKey arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Material toLegacy(Material arg0) {
        // TODO Auto-generated method stub
        return null;
    }

}