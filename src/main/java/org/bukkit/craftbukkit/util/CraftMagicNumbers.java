package org.bukkit.craftbukkit.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Fluid;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.UnsafeValues;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.cardboardpowered.impl.CardboardModdedBlock;
import org.cardboardpowered.impl.CardboardModdedItem;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.javazilla.bukkitfabric.BukkitFabricMod;
import com.javazilla.bukkitfabric.BukkitLogger;
import com.javazilla.bukkitfabric.interfaces.IMixinMaterial;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Dynamic;

import io.izzel.arclight.api.EnumHelper;
import io.izzel.arclight.api.Unsafe;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.datafixer.Schemas;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@SuppressWarnings("deprecation")
public final class CraftMagicNumbers implements UnsafeValues {

    public static final UnsafeValues INSTANCE = new CraftMagicNumbers();

    private CraftMagicNumbers() {}

    public static BlockState getBlock(MaterialData material) {
        return getBlock(material.getItemType(), material.getData());
    }

    public static BlockState getBlock(Material material, byte data) {
        return CraftLegacyMaterials.fromLegacyData(CraftLegacyMaterials.toLegacy(material), data);
    }

    public static MaterialData getMaterial(BlockState data) {
        return CraftLegacyMaterials.toLegacy(getMaterial(data.getBlock())).getNewData(toLegacyData(data));
    }

    public static Item getItem(Material material, short data) {
        if (material.isLegacy())
            return CraftLegacyMaterials.fromLegacyData(CraftLegacyMaterials.toLegacy(material), data);

        return getItem(material);
    }

    public static MaterialData getMaterialData(Item item) {
        return CraftLegacyMaterials.toLegacyData(getMaterial(item));
    }

    // ========================================================================
    private static final Map<Block, Material> BLOCK_MATERIAL = new HashMap<>();
    private static final Map<Item, Material> ITEM_MATERIAL = new HashMap<>();
    private static final Map<Material, Item> MATERIAL_ITEM = new HashMap<>();
    private static final Map<Material, Block> MATERIAL_BLOCK = new HashMap<>();
    private static final Map<net.minecraft.fluid.Fluid, org.bukkit.Fluid> FLUID_MATERIAL = new HashMap<>();
    private static final Map<Material, net.minecraft.fluid.Fluid> MATERIAL_FLUID = new HashMap<>();

    private static boolean dev = false;

    static {
        for (Block block : Registry.BLOCK)
            BLOCK_MATERIAL.put(block, Material.getMaterial(Registry.BLOCK.getId(block).getPath().toUpperCase(Locale.ROOT)));

        for (Item item : Registry.ITEM)
            ITEM_MATERIAL.put(item, Material.getMaterial(Registry.ITEM.getId(item).getPath().toUpperCase(Locale.ROOT)));

        for (net.minecraft.fluid.Fluid fluid : Registry.FLUID)
            FLUID_MATERIAL.put(fluid, org.bukkit.Registry.FLUID.get(CraftNamespacedKey.fromMinecraft(Registry.FLUID.getId(fluid))));

        for (Material material : Material.values()) {
            if (material.isLegacy()) continue;

            Identifier key = key(material);
            Registry.ITEM.getOrEmpty(key).ifPresent((item) -> MATERIAL_ITEM.put(material, item));
            Registry.BLOCK.getOrEmpty(key).ifPresent((block) -> MATERIAL_BLOCK.put(material, block));
            Registry.FLUID.getOrEmpty(key).ifPresent((fluid) -> MATERIAL_FLUID.put(material, fluid));
        }
    }

    public static final Map<String, Material> BY_NAME = Unsafe.getStatic(Material.class, "BY_NAME");
    private static final List<Class<?>> MAT_CTOR = ImmutableList.of(int.class);
    public static final HashMap<String, Material> MODDED_MATERIALS = new HashMap<>();

    public static final HashMap<Item, Material> MODDED_ITEM_MATERIAL = new HashMap<>();
    public static final HashMap<Material, Item> MODDED_MATERIAL_ITEM = new HashMap<>();

    @Deprecated
    public static void setupUnknownModdedMaterials() {
        for (Material material : Material.values()) {
            if (material.isLegacy()) continue;
            Identifier key = key(material);
            Registry.ITEM.getOrEmpty(key).ifPresent((item) -> MATERIAL_ITEM.put(material, item));
            Registry.BLOCK.getOrEmpty(key).ifPresent((block) -> MATERIAL_BLOCK.put(material, block));
            Registry.FLUID.getOrEmpty(key).ifPresent((fluid) -> MATERIAL_FLUID.put(material, fluid));
        }
    }

    public static void test() {
        // TODO: This needs to be kept updated when Spigot updates
        // It is the value of Material.values().length
        int MATERIAL_LENGTH = 1525;
        int i = MATERIAL_LENGTH - 1;

        List<String> names = new ArrayList<>();
        List<Material> list = new ArrayList<>();

        String lastMod = "";
        for (Block block : Registry.BLOCK) {
            Identifier id = Registry.BLOCK.getId(block);
            String name = standardize(id);
            if (id.getNamespace().startsWith("minecraft")) continue;

            Material material = BY_NAME.get(name);
            if (null == material && !names.contains(name)) {
                material = EnumHelper.makeEnum(Material.class, name, i, MAT_CTOR, ImmutableList.of(i));
                ((IMixinMaterial)(Object)material).setModdedData(new CardboardModdedBlock(id.toString()));
                MATERIAL_BLOCK.put(material, block);
                BY_NAME.put(name, material);
                list.add(material);
                MODDED_MATERIALS.put(name, material);
                
                if (!(lastMod.equalsIgnoreCase(id.namespace)))
                    BukkitFabricMod.LOGGER.info("Registering modded blocks from mod '" + (lastMod = id.namespace) + "'..");
            }
            Material m = Material.getMaterial(id.getNamespace().toUpperCase(Locale.ROOT) + "_" + id.getPath().toUpperCase(Locale.ROOT));
            BLOCK_MATERIAL.put(block, m);
            MATERIAL_BLOCK.put(m, block);
        }

        for (Item item : Registry.ITEM) {
            Identifier id = Registry.ITEM.getId(item);
            String name = standardize(id);
            if (id.getNamespace().startsWith("minecraft")) continue;

            Material material = BY_NAME.get(name);
            if (null == material && !names.contains(name)) {
                material = EnumHelper.makeEnum(Material.class, name, i, MAT_CTOR, ImmutableList.of(i));
                ((IMixinMaterial)(Object)material).setModdedData(new CardboardModdedItem(id.toString()));
                MATERIAL_ITEM.put(material, item);
                BY_NAME.put(name, material);
                list.add(material);
                MODDED_MATERIALS.put(name, material);

                if (!(lastMod.equalsIgnoreCase(id.namespace)))
                    BukkitFabricMod.LOGGER.info("Registering modded items from mod '" + (lastMod = id.namespace) + "'..");
            }
            Material m = Material.getMaterial(id.getNamespace().toUpperCase(Locale.ROOT) + "_" + id.getPath().toUpperCase(Locale.ROOT));
            ITEM_MATERIAL.put(item, m);
            MATERIAL_ITEM.put(m, item);
        }

        for (net.minecraft.fluid.Fluid fluid : Registry.FLUID)
            FLUID_MATERIAL.put(fluid, org.bukkit.Registry.FLUID.get(CraftNamespacedKey.fromMinecraft(Registry.FLUID.getId(fluid))));

        EnumHelper.addEnums(Material.class, list);

        for (Material material : list) {
            Identifier key = key(material);
            Registry.ITEM.getOrEmpty(key).ifPresent((item) -> MATERIAL_ITEM.put(material, item));
            Registry.BLOCK.getOrEmpty(key).ifPresent((block) -> MATERIAL_BLOCK.put(material, block));
            Registry.FLUID.getOrEmpty(key).ifPresent((fluid) -> MATERIAL_FLUID.put(material, fluid));
        }
    }

    public static HashMap<String, Material> getModdedMaterials() {
        HashMap<String, Material> map = new HashMap<>();
        for (Block block : Registry.BLOCK) {
            Identifier id = Registry.BLOCK.getId(block);
            String name = standardize(id);
            if (id.getNamespace().startsWith("minecraft")) continue;

            map.put(name, Material.getMaterial(id.getNamespace().toUpperCase(Locale.ROOT) + "_" + id.getPath().toUpperCase(Locale.ROOT)));
        }

        for (Item item : Registry.ITEM) {
            Identifier id = Registry.ITEM.getId(item);
            String name = standardize(id);
            if (id.getNamespace().startsWith("minecraft")) continue;

            map.put(name, Material.getMaterial(id.getNamespace().toUpperCase(Locale.ROOT) + "_" + id.getPath().toUpperCase(Locale.ROOT)));
        }
        return map;
    }

    public static String standardize(Identifier location) {
        Preconditions.checkNotNull(location, "location");
        return (location.getNamespace().equals(NamespacedKey.MINECRAFT) ? location.getPath() : location.toString())
            .replace(':', '_')
            .replaceAll("\\s+", "_")
            .replaceAll("\\W", "")
            .toUpperCase(Locale.ENGLISH);
    }

    public static String standardizeLower(Identifier location) {
        return (location.getNamespace().equals(NamespacedKey.MINECRAFT) ? location.getPath() : location.toString())
            .replace(':', '_')
            .replaceAll("\\s+", "_")
            .replaceAll("\\W", "")
            .toLowerCase(Locale.ENGLISH);
    }

    public static Material getMaterial(Block block) {
        Identifier id = Registry.BLOCK.getId(block);
        Material m = BLOCK_MATERIAL.getOrDefault(block, Material.getMaterial(id.getNamespace().toUpperCase(Locale.ROOT) + "_" + id.getPath().toUpperCase(Locale.ROOT)));
        BLOCK_MATERIAL.put(block, m);
        MATERIAL_BLOCK.put(m, block);
        return m;
    }

    public static Material getMaterial(Item item) {
        for (Item item1 : Registry.ITEM) {
            Identifier id = Registry.ITEM.getId(item1);
            if (!id.getNamespace().toLowerCase().contains("minecraft"))
            ITEM_MATERIAL.put(item1, Material.getMaterial(id.getNamespace().toUpperCase(Locale.ROOT) + "_" + id.getPath().toUpperCase(Locale.ROOT)));
        }

        Identifier id = Registry.ITEM.getId(item);
        Material m = ITEM_MATERIAL.getOrDefault(item, Material.getMaterial(id.getNamespace().toUpperCase(Locale.ROOT) + "_" + id.getPath().toUpperCase(Locale.ROOT)));
        ITEM_MATERIAL.put(item, m);
        MATERIAL_ITEM.put(m,item);
        return m;
    }

    public static Item getItem(Material material) {
        if (material != null && material.isLegacy()) material = CraftLegacyMaterials.fromLegacy(material);
        return MATERIAL_ITEM.getOrDefault(material, getModdedItem(material));
    }

    public static Block getBlock(Material material) {
        if (material != null && material.isLegacy()) material = CraftLegacyMaterials.fromLegacy(material);
        return MATERIAL_BLOCK.getOrDefault(material, getModdedBlock(material));
    }

    private static Item getModdedItem(Material mat) {
        if (!((Object)mat instanceof IMixinMaterial)) {
            // Dev env
            return null;
        }
        IMixinMaterial mm = (IMixinMaterial)(Object) mat;
        if (!mm.isModded()) return null;

        Identifier id = new Identifier(mm.getModdedData().getId());
        Item item = Registry.ITEM.get(id);
        MATERIAL_ITEM.put(mat, item);
        return item;
    }

    private static Block getModdedBlock(Material mat) {
        if (null == mat) return null;
        if (!((Object)mat instanceof IMixinMaterial)) {
            // Dev env
            return null;
        }
        IMixinMaterial mm = (IMixinMaterial)(Object) mat;
        if (!mm.isModded()) return null;

        Identifier id = new Identifier(mm.getModdedData().getId());
        Block block = Registry.BLOCK.get(id);
        MATERIAL_BLOCK.put(mat, block);
        return block;
    }

    public static Identifier key(Material mat) {
        return CraftNamespacedKey.toMinecraft(mat.getKey());
    }
    // ========================================================================

    public static byte toLegacyData(BlockState data) {
        return CraftLegacyMaterials.toLegacyData(data);
    }

    @Override
    public Material toLegacy(Material material) {
        return CraftLegacyMaterials.toLegacy(material);
    }

    @Override
    public Material fromLegacy(Material material) {
        return CraftLegacyMaterials.fromLegacy(material);
    }

    @Override
    public Material fromLegacy(MaterialData material) {
        return CraftLegacyMaterials.fromLegacy(material);
    }

    @Override
    public Material fromLegacy(MaterialData material, boolean itemPriority) {
        return CraftLegacyMaterials.fromLegacy(material, itemPriority);
    }

    @Override
    public BlockData fromLegacy(Material material, byte data) {
        return CraftBlockData.fromData(getBlock(material, data));
    }

    @Override
    public Material getMaterial(String material, int version) {
        setupUnknownModdedMaterials();
        Preconditions.checkArgument(material != null, "material == null");
        Preconditions.checkArgument(version <= this.getDataVersion(), "Newer version! Server downgrades are not supported!");

        // Fastpath up to date materials
        if (version == this.getDataVersion()) return Material.getMaterial(material);

        Dynamic<NbtElement> name = new Dynamic<>(NbtOps.INSTANCE, NbtString.of("minecraft:" + material.toLowerCase(Locale.ROOT)));
        Dynamic<NbtElement> converted = Schemas.getFixer().update(TypeReferences.ITEM_NAME, name, version, this.getDataVersion());

        if (name.equals(converted)) converted = Schemas.getFixer().update(TypeReferences.BLOCK_NAME, name, version, this.getDataVersion());
        return Material.matchMaterial(converted.asString(""));
    }

    @Deprecated
    public String getMappingsVersion() {
        return "MinecraftMapping-spigot2intermediary.srg";
    }

    @Override
    public int getDataVersion() {
        return SharedConstants.getGameVersion().getWorldVersion();
    }

    @Override
    public ItemStack modifyItemStack(ItemStack stack, String arguments) {
        net.minecraft.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);

        try {
            nmsStack.setTag((NbtCompound) StringNbtReader.parse(arguments));
        } catch (CommandSyntaxException ex) {
            BukkitLogger.getLogger(CraftMagicNumbers.class.getName()).log(Level.SEVERE, null, ex);
        }

        stack.setItemMeta(CraftItemStack.getItemMeta(nmsStack));
        return stack;
    }

    @Override
    public Advancement loadAdvancement(NamespacedKey key, String advancement) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean removeAdvancement(NamespacedKey key) {
        // TODO Auto-generated method stub
        return false;
    }

    private static final List<String> SUPPORTED_API = Arrays.asList("1.13", "1.14", "1.15", "1.16");

    @Override
    public void checkSupported(PluginDescriptionFile pdf) throws InvalidPluginException {
        String minimumVersion = "1.12"; // TODO
        int minimumIndex = SUPPORTED_API.indexOf(minimumVersion);

        if (pdf.getAPIVersion() != null) {
            int pluginIndex = SUPPORTED_API.indexOf(pdf.getAPIVersion());
            if (pluginIndex == -1) throw new InvalidPluginException("Unsupported API version " + pdf.getAPIVersion());

            if (pluginIndex < minimumIndex)
                throw new InvalidPluginException("Plugin API version " + pdf.getAPIVersion() + " is lower than the minimum allowed version. Please update or replace it.");
        } else {
            if (minimumIndex == -1) {
                CraftLegacyMaterials.init();
                Bukkit.getLogger().log(Level.WARNING, "Legacy plugin " + pdf.getFullName() + " does not specify an api-version.");
            } else throw new InvalidPluginException("Plugin API version " + pdf.getAPIVersion() + " is lower than the minimum allowed version. Please update or replace it.");
        }
    }

    public static boolean isLegacy(PluginDescriptionFile pdf) {
        return pdf.getAPIVersion() == null;
    }

    @Override
    public byte[] processClass(PluginDescriptionFile pdf, String path, byte[] clazz) {
        try {
            clazz = Commodore.convert(clazz, !isLegacy(pdf), pdf.getName());
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Fatal error trying to convert " + pdf.getFullName() + ":" + path, ex);
        }
        return clazz;
    }

    // Paper start
    public boolean isSupportedApiVersion(String apiVersion) {
        return apiVersion != null && SUPPORTED_API.contains(apiVersion);
    }
    // Paper end

    /**
     * This helper class represents the different NBT Tags.
     * <p>
     * These should match NBTBase#getTypeId
     */
    public static class NBT {
        public static final int TAG_END = 0;
        public static final int TAG_BYTE = 1;
        public static final int TAG_SHORT = 2;
        public static final int TAG_INT = 3;
        public static final int TAG_LONG = 4;
        public static final int TAG_FLOAT = 5;
        public static final int TAG_DOUBLE = 6;
        public static final int TAG_BYTE_ARRAY = 7;
        public static final int TAG_STRING = 8;
        public static final int TAG_LIST = 9;
        public static final int TAG_COMPOUND = 10;
        public static final int TAG_INT_ARRAY = 11;
        public static final int TAG_ANY_NUMBER = 99;
    }

    public static Fluid getFluid(net.minecraft.fluid.Fluid fluid) {
        return FLUID_MATERIAL.get(fluid);
    }

    public static net.minecraft.fluid.Fluid getFluid(Fluid fluid) {
        return MATERIAL_FLUID.get(fluid);
    }

    @Override
    public ItemStack deserializeItem(byte[] arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTimingsServerName() {
        return "Fabric";
    }

    @Override
    public String getTranslationKey(Material arg0) {
        return arg0.name();
    }

    @Override
    public String getTranslationKey(org.bukkit.block.Block arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTranslationKey(EntityType arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int nextEntityId() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void reportTimings() {
    }

    @Override
    public byte[] serializeItem(ItemStack arg0) {
        // TODO Auto-generated method stub
        return null;
    }

}