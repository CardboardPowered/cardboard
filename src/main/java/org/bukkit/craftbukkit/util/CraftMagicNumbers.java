package org.bukkit.craftbukkit.util;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.FeatureFlag;
import org.bukkit.Fluid;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.RegionAccessor;
import org.bukkit.UnsafeValues;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftFeatureFlag;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.CreativeCategory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.cardboardpowered.adventure.CardboardAdventure;
import org.cardboardpowered.impl.CardboardModdedBlock;
import org.cardboardpowered.impl.CardboardModdedItem;
import org.cardboardpowered.util.GameVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.javazilla.bukkitfabric.BukkitFabricMod;
import com.javazilla.bukkitfabric.BukkitLogger;
import com.javazilla.bukkitfabric.interfaces.IMixinMaterial;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Dynamic;
import io.izzel.arclight.api.EnumHelper;
import io.izzel.arclight.api.Unsafe;
import io.papermc.paper.inventory.ItemRarity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.bukkit.Bukkit;
import org.bukkit.Fluid;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.UnsafeValues;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.block.data.IMagicNumbers;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.CreativeCategory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.cardboardpowered.BlockImplUtil;
import org.cardboardpowered.adventure.CardboardAdventure;
import org.cardboardpowered.impl.CardboardModdedBlock;
import org.cardboardpowered.impl.CardboardModdedItem;
import org.cardboardpowered.util.GameVersion;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

@SuppressWarnings("deprecation")
public final class CraftMagicNumbers implements UnsafeValues, IMagicNumbers {
    
    public Material IgetMaterial(Block b) {return CraftMagicNumbers.getMaterial(b);}
    public Block IgetBlock(Material m) {return CraftMagicNumbers.getBlock(m);}

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
    private static final Map<org.bukkit.entity.EntityType, net.minecraft.entity.EntityType<?>> ENTITY_TYPE_ENTITY_TYPES = new HashMap();
    private static final Map<net.minecraft.entity.EntityType<?>, org.bukkit.entity.EntityType> ENTITY_TYPES_ENTITY_TYPE = new HashMap();

    static {
        BlockImplUtil.setMN((IMagicNumbers)INSTANCE);
        
        for (org.bukkit.entity.EntityType type : org.bukkit.entity.EntityType.values()) {
            if (type == org.bukkit.entity.EntityType.UNKNOWN) continue;
            ENTITY_TYPE_ENTITY_TYPES.put(type, Registries.ENTITY_TYPE.get(CraftNamespacedKey.toMinecraft(type.getKey())));
            ENTITY_TYPES_ENTITY_TYPE.put(Registries.ENTITY_TYPE.get(CraftNamespacedKey.toMinecraft(type.getKey())), type);
        }
        
        for (Block block : Registries.BLOCK)
            BLOCK_MATERIAL.put(block, Material.getMaterial(Registries.BLOCK.getId(block).getPath().toUpperCase(Locale.ROOT)));

        for (Item item : Registries.ITEM)
            ITEM_MATERIAL.put(item, Material.getMaterial(Registries.ITEM.getId(item).getPath().toUpperCase(Locale.ROOT)));

        //for (net.minecraft.fluid.Fluid fluid : Registries.FLUID)
        //    FLUID_MATERIAL.put(fluid, org.bukkit.Registries.FLUID.get(CraftNamespacedKey.fromMinecraft(Registries.FLUID.getId(fluid))));

        for (net.minecraft.fluid.Fluid fluidType : Registries.FLUID) {
            if (Registries.FLUID.getId(fluidType).getNamespace().equals(NamespacedKey.MINECRAFT)) {
                //Fluid fluid = org.bukkit.Registries.FLUID.get(CraftNamespacedKey.fromMinecraft(Registries.FLUID.getId(fluidType)));
               // if (fluid != null) {
               // 	FLUID_MATERIAL.put(fluidType, fluid);
               // }
            }
        }
        
        for (Material material : Material.values()) {
            if (material.isLegacy()) continue;

            Identifier key = key(material);
            Registries.ITEM.getOrEmpty(key).ifPresent((item) -> MATERIAL_ITEM.put(material, item));
            Registries.BLOCK.getOrEmpty(key).ifPresent((block) -> MATERIAL_BLOCK.put(material, block));
            Registries.FLUID.getOrEmpty(key).ifPresent((fluid) -> MATERIAL_FLUID.put(material, fluid));
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
            Registries.ITEM.getOrEmpty(key).ifPresent((item) -> MATERIAL_ITEM.put(material, item));
            Registries.BLOCK.getOrEmpty(key).ifPresent((block) -> MATERIAL_BLOCK.put(material, block));
            Registries.FLUID.getOrEmpty(key).ifPresent((fluid) -> MATERIAL_FLUID.put(material, fluid));
        }
    }

    public static void test() {
        // TODO: This needs to be kept updated when Spigot updates
        // It is the value of Material.values().length
    	BukkitFabricMod.LOGGER.info("DEB: " + Material.values().length);
        int MATERIAL_LENGTH = 1837; //1525;
        int i = MATERIAL_LENGTH - 1;

        List<String> names = new ArrayList<>();
        List<Material> list = new ArrayList<>();

        String lastMod = "";
        for (Block block : Registries.BLOCK) {
            Identifier id = Registries.BLOCK.getId(block);
            String name = standardize(id);
            String nam = id.getNamespace().toUpperCase(Locale.ROOT) + "_" + id.getPath().toUpperCase(Locale.ROOT);
            if (id.getNamespace().startsWith("minecraft")) {
            	boolean has = false;
            	try {
            		Material.valueOf(id.getPath().toUpperCase());
            		has = true;
            	} catch (IllegalArgumentException e) {
            		// Snapshot or API not updated.
            		has = false;
            		nam = id.getPath().toUpperCase(Locale.ROOT);
            	}
            	if (has) {
            		continue;
            	}
            }

            Material material = BY_NAME.get(name);
            if (null == material && !names.contains(name)) {
                material = EnumHelper.makeEnum(Material.class, name, i, MAT_CTOR, ImmutableList.of(i));
                if (!(material instanceof IMixinMaterial)) {
                    BukkitFabricMod.LOGGER.warning("Material not instanceof IMixinMaterial");
                    return;
                }

                ((IMixinMaterial)(Object)material).setModdedData(new CardboardModdedBlock(id.toString()));
                MATERIAL_BLOCK.put(material, block);
                BY_NAME.put(name, material);
                list.add(material);
                MODDED_MATERIALS.put(name, material);
                
                if (!(lastMod.equalsIgnoreCase(id.namespace)))
                    BukkitFabricMod.LOGGER.info("Registering modded blocks from mod '" + (lastMod = id.namespace) + "'..");
            }
            Material m = Material.getMaterial(nam);
            BLOCK_MATERIAL.put(block, m);
            MATERIAL_BLOCK.put(m, block);
        }

        for (Item item : Registries.ITEM) {
            Identifier id = Registries.ITEM.getId(item);
            String name = standardize(id);
            String nam = id.getNamespace().toUpperCase(Locale.ROOT) + "_" + id.getPath().toUpperCase(Locale.ROOT);
            if (id.getNamespace().startsWith("minecraft")) {
            	boolean has = false;
            	try {
            		Material.valueOf(id.getPath().toUpperCase());
            		has = true;
            	} catch (IllegalArgumentException e) {
            		// Snapshot or API not updated.
            		nam = id.getPath().toUpperCase(Locale.ROOT);
            		has = false;
            	}
            	if (has) {
            		continue;
            	}
            }

            Material material = BY_NAME.get(name);
            if (null == material && !names.contains(name)) {
                material = EnumHelper.makeEnum(Material.class, name, i, MAT_CTOR, ImmutableList.of(i));
                if (!(material instanceof IMixinMaterial)) {
                    BukkitFabricMod.LOGGER.warning("Material not instanceof IMixinMaterial");
                    return;
                }

                ((IMixinMaterial)(Object)material).setModdedData(new CardboardModdedItem(id.toString()));
                MATERIAL_ITEM.put(material, item);
                BY_NAME.put(name, material);
                list.add(material);
                MODDED_MATERIALS.put(name, material);

                if (!(lastMod.equalsIgnoreCase(id.namespace)))
                    BukkitFabricMod.LOGGER.info("Registering modded items from mod '" + (lastMod = id.namespace) + "'..");
            }
            Material m = Material.getMaterial(nam);
            ITEM_MATERIAL.put(item, m);
            MATERIAL_ITEM.put(m, item);
        }

        //for (net.minecraft.fluid.Fluid fluid : Registries.FLUID)
        //    FLUID_MATERIAL.put(fluid, org.bukkit.Registries.FLUID.get(CraftNamespacedKey.fromMinecraft(Registries.FLUID.getId(fluid))));

        EnumHelper.addEnums(Material.class, list);

        for (Material material : list) {
            Identifier key = key(material);
            Registries.ITEM.getOrEmpty(key).ifPresent((item) -> MATERIAL_ITEM.put(material, item));
            Registries.BLOCK.getOrEmpty(key).ifPresent((block) -> MATERIAL_BLOCK.put(material, block));
            Registries.FLUID.getOrEmpty(key).ifPresent((fluid) -> MATERIAL_FLUID.put(material, fluid));
        }
    }

    public static HashMap<String, Material> getModdedMaterials() {
        HashMap<String, Material> map = new HashMap<>();
        for (Block block : Registries.BLOCK) {
            Identifier id = Registries.BLOCK.getId(block);
            String name = standardize(id);
            if (id.getNamespace().startsWith("minecraft")) continue;

            map.put(name, Material.getMaterial(id.getNamespace().toUpperCase(Locale.ROOT) + "_" + id.getPath().toUpperCase(Locale.ROOT)));
        }

        for (Item item : Registries.ITEM) {
            Identifier id = Registries.ITEM.getId(item);
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
        Identifier id = Registries.BLOCK.getId(block);
        Material m = BLOCK_MATERIAL.getOrDefault(block, Material.getMaterial(id.getNamespace().toUpperCase(Locale.ROOT) + "_" + id.getPath().toUpperCase(Locale.ROOT)));
        BLOCK_MATERIAL.put(block, m);
        MATERIAL_BLOCK.put(m, block);
        return m;
    }

    public static Material getMaterial(Item item) {
        return ITEM_MATERIAL.getOrDefault(item, Material.AIR);
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
        Item item = Registries.ITEM.get(id);
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
        Block block = Registries.BLOCK.get(id);
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
        return "60a2bb6bf2684dc61c56b90d7c41bddc";
    }

    @Override
    public int getDataVersion() {
        return GameVersion.create().world_version;
    }

    @Override
    public ItemStack modifyItemStack(ItemStack stack, String arguments) {
        net.minecraft.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);

        try {
            nmsStack.setNbt((NbtCompound) StringNbtReader.parse(arguments));
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

    private static final List<String> SUPPORTED_API = Arrays.asList("1.13", "1.14", "1.15", "1.16", "1.17", "1.18", "1.19", "1.20");

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
        return true;//apiVersion != null && SUPPORTED_API.contains(apiVersion);
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

    //@Override
    public String getTranslationKey(Material arg0) {
        return arg0.name();
    }

    //@Override
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

    @Override
    public @NotNull Multimap<Attribute, AttributeModifier> getItemAttributes(@NotNull Material arg0,
            @NotNull EquipmentSlot arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ItemRarity getItemRarity(Material arg0) {
        // TODO Auto-generated method stub
        return ItemRarity.COMMON;
    }

    @Override
    public ItemRarity getItemStackRarity(ItemStack arg0) {
        // TODO Auto-generated method stub
        return ItemRarity.COMMON;
    }

    @Override
    public int getProtocolVersion() {
        // TODO Auto-generated method stub
        return SharedConstants.getProtocolVersion();
    }

    @Override
    public String getTranslationKey(ItemStack arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isValidRepairItemStack(@NotNull ItemStack arg0, @NotNull ItemStack arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    // Paper start
    @Override
    public net.kyori.adventure.text.flattener.ComponentFlattener componentFlattener() {
        return CardboardAdventure.FLATTENER;
    }

    @Override
    public net.kyori.adventure.text.serializer.gson.GsonComponentSerializer colorDownsamplingGsonComponentSerializer() {
        return CardboardAdventure.COLOR_DOWNSAMPLING_GSON;
    }

    @Override
    public net.kyori.adventure.text.serializer.gson.GsonComponentSerializer gsonComponentSerializer() {
        return CardboardAdventure.GSON;
    }

    @Override
    public net.kyori.adventure.text.serializer.plain.PlainComponentSerializer plainComponentSerializer() {
        return CardboardAdventure.PLAIN;
    }

    @Override
    public net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer legacyComponentSerializer() {
        return CardboardAdventure.LEGACY_SECTION_UXRC;
    }
    // Paper end

    @Override
    public Entity deserializeEntity(byte[] bs, World world, boolean bl) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public @NotNull Attributable getDefaultEntityAttributes(@NotNull NamespacedKey arg0) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public boolean hasDefaultEntityAttributes(@NotNull NamespacedKey arg0) {
        // TODO Auto-generated method stub
        return false;
    }
    @Override
    public boolean isCollidable(@NotNull Material arg0) {
        // TODO Auto-generated method stub
        return true;
    }
    @Override
    public byte[] serializeEntity(Entity entity) {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    // 1.18.2 api:
    
	@Override
	public CreativeCategory getCreativeCategory(Material arg0) {
		return CreativeCategory.BUILDING_BLOCKS;
	}
	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(Material arg0, EquipmentSlot arg1) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public @NotNull String getMainLevelName() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public PlainTextComponentSerializer plainTextSerializer() {
		// TODO Auto-generated method stub
		return null;
	}

	//@Override
	public <T extends Keyed> org.bukkit.@NotNull Registry<T> registryFor(Class<T> arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	// 1.19.2
	
	@Override
	public @NotNull NamespacedKey getBiomeKey(RegionAccessor arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Component resolveWithContext(Component arg0, CommandSender arg1, Entity arg2, boolean arg3)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setBiomeKey(RegionAccessor arg0, int arg1, int arg2, int arg3, NamespacedKey arg4) {
		// TODO Auto-generated method stub
		
	}
	
	// 1.19.4:

	@Override
    public String getBlockTranslationKey(Material material) {
        Block block = CraftMagicNumbers.getBlock(material);
        return block != null ? block.getTranslationKey() : null;
    }

	@Override
	public @Nullable FeatureFlag getFeatureFlag(@NotNull NamespacedKey key) {
        Preconditions.checkArgument((key != null ? 1 : 0) != 0, "key cannot be null");
        return CraftFeatureFlag.getFromNMS(key);
	}

	@Override
    public String getItemTranslationKey(Material material) {
        Item item = CraftMagicNumbers.getItem(material);
        return item != null ? item.getTranslationKey() : null;
    }
	
    public static net.minecraft.entity.EntityType<?> getEntityTypes(org.bukkit.entity.EntityType type) {
        return ENTITY_TYPE_ENTITY_TYPES.get(type);
    }

    public static org.bukkit.entity.EntityType getEntityType(net.minecraft.entity.EntityType<?> entityTypes) {
        return ENTITY_TYPES_ENTITY_TYPE.get(entityTypes);
    }


}
