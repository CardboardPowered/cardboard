package org.bukkit.craftbukkit.inventory;

//<<<<<<< HEAD
import com.destroystokyo.paper.Namespaced;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.gson.JsonParseException;
import com.javazilla.bukkitfabric.Utils;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.BlockItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtTagSizeTracker;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
//import org.apache.commons.codec.binary.Base64;
//=======
import static org.spigotmc.ValidateUtils.limit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

// import org.apache.commons.codec.binary.Base64;
//>>>>>>> upstream/ver/1.20
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.inventory.CraftMetaItem.ItemMetaKey.Specific;
import org.bukkit.craftbukkit.inventory.tags.DeprecatedCustomTagContainer;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftNBTTagConfigSerializer;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.persistence.PersistentDataContainer;
import org.cardboardpowered.impl.CardboardAttributable;
import org.cardboardpowered.impl.CardboardAttributeInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.spigotmc.ValidateUtils.limit;

@SuppressWarnings({"deprecation", "rawtypes"})
@DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
class CraftMetaItem implements ItemMeta, Damageable, Repairable, BlockDataMeta {

	 private Set<Namespaced> destroyableKeys = Sets.newHashSet();
	
    static class ItemMetaKey {

        @Retention(RetentionPolicy.SOURCE)
        @Target(ElementType.FIELD)
        @interface Specific {
            enum To { BUKKIT, NBT }
            To value();
        }

        final String BUKKIT;
        final String NBT;

        ItemMetaKey(final String both) {
            this(both, both);
        }

        ItemMetaKey(final String nbt, final String bukkit) {
            this.NBT = nbt;
            this.BUKKIT = bukkit;
        }
    }

    @SerializableAs("ItemMeta")
    public static final class SerializableMeta implements ConfigurationSerializable {
        static final String TYPE_FIELD = "meta-type";

        static final ImmutableMap<Class<? extends CraftMetaItem>, String> classMap;
        static final ImmutableMap<String, Constructor<? extends CraftMetaItem>> constructorMap;

        static {
            classMap = ImmutableMap.<Class<? extends CraftMetaItem>, String>builder()
                    .put(CraftMetaArmorStand.class, "ARMOR_STAND")
                    .put(CraftMetaBanner.class, "BANNER")
                    .put(CraftMetaBlockState.class, "TILE_ENTITY")
                    .put(CraftMetaBook.class, "BOOK")
                    .put(CraftMetaBookSigned.class, "BOOK_SIGNED")
                    .put(CraftMetaSkull.class, "SKULL")
                    .put(CraftMetaLeatherArmor.class, "LEATHER_ARMOR")
                    .put(CraftMetaMap.class, "MAP")
                    .put(CraftMetaPotion.class, "POTION")
                    .put(CraftMetaSpawnEgg.class, "SPAWN_EGG")
                    .put(CraftMetaEnchantedBook.class, "ENCHANTED")
                    .put(CraftMetaFirework.class, "FIREWORK")
                    .put(CraftMetaCharge.class, "FIREWORK_EFFECT")
                    .put(CraftMetaKnowledgeBook.class, "KNOWLEDGE_BOOK")
                    // TODO .put(CraftMetaTropicalFishBucket.class, "TROPICAL_FISH_BUCKET")
                    .put(CraftMetaCrossbow.class, "CROSSBOW")
                    .put(CraftMetaSuspiciousStew.class, "SUSPICIOUS_STEW")
                    .put(CraftMetaItem.class, "UNSPECIFIC")
                    .build();

            final ImmutableMap.Builder<String, Constructor<? extends CraftMetaItem>> classConstructorBuilder = ImmutableMap.builder();
            for (Map.Entry<Class<? extends CraftMetaItem>, String> mapping : classMap.entrySet()) {
                try {
                    classConstructorBuilder.put(mapping.getValue(), mapping.getKey().getDeclaredConstructor(Map.class));
                } catch (NoSuchMethodException e) {throw new AssertionError(e);}
            }
            constructorMap = classConstructorBuilder.build();
        }

        private SerializableMeta() {
        }

        public static ItemMeta deserialize(Map<String, Object> map) throws Throwable {
            Validate.notNull(map, "Cannot deserialize null map");

            String type = getString(map, TYPE_FIELD, false);
            Constructor<? extends CraftMetaItem> constructor = constructorMap.get(type);

            if (constructor == null)
                throw new IllegalArgumentException(type + " is not a valid " + TYPE_FIELD);

            try {
                return constructor.newInstance(map);
            } catch (final InstantiationException | IllegalAccessException e) {
                throw new AssertionError(e);
            } catch (final InvocationTargetException e) {
                throw e.getCause();
            }
        }

        @Override
        public Map<String, Object> serialize() {
            throw new AssertionError();
        }

        static String getString(Map<?, ?> map, Object field, boolean nullable) {
            return getObject(String.class, map, field, nullable);
        }

        static boolean getBoolean(Map<?, ?> map, Object field) {
            Boolean value = getObject(Boolean.class, map, field, true);
            return value != null && value;
        }

        static <T> T getObject(Class<T> clazz, Map<?, ?> map, Object field, boolean nullable) {
            final Object object = map.get(field);

            if (clazz.isInstance(object))
                return clazz.cast(object);

            if (object == null) {
                if (!nullable)
                    throw new NoSuchElementException(map + " does not contain " + field);
                return null;
            }
            throw new IllegalArgumentException(field + "(" + object + ") is not a valid " + clazz);
        }
    }

    static final ItemMetaKey NAME = new ItemMetaKey("Name", "display-name");
    static final ItemMetaKey LOCNAME = new ItemMetaKey("LocName", "loc-name");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey DISPLAY = new ItemMetaKey("display");
    static final ItemMetaKey LORE = new ItemMetaKey("Lore", "lore");
    static final ItemMetaKey CUSTOM_MODEL_DATA = new ItemMetaKey("CustomModelData", "custom-model-data");
    static final ItemMetaKey ENCHANTMENTS = new ItemMetaKey("Enchantments", "enchants");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey ENCHANTMENTS_ID = new ItemMetaKey("id");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey ENCHANTMENTS_LVL = new ItemMetaKey("lvl");
    static final ItemMetaKey REPAIR = new ItemMetaKey("RepairCost", "repair-cost");
    static final ItemMetaKey ATTRIBUTES = new ItemMetaKey("AttributeModifiers", "attribute-modifiers");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey ATTRIBUTES_IDENTIFIER = new ItemMetaKey("AttributeName");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey ATTRIBUTES_NAME = new ItemMetaKey("Name");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey ATTRIBUTES_VALUE = new ItemMetaKey("Amount");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey ATTRIBUTES_TYPE = new ItemMetaKey("Operation");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey ATTRIBUTES_UUID_HIGH = new ItemMetaKey("UUIDMost");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey ATTRIBUTES_UUID_LOW = new ItemMetaKey("UUIDLeast");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey ATTRIBUTES_SLOT = new ItemMetaKey("Slot");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey HIDEFLAGS = new ItemMetaKey("HideFlags", "ItemFlags");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey UNBREAKABLE = new ItemMetaKey("Unbreakable");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey DAMAGE = new ItemMetaKey("Damage");
    @Specific(Specific.To.NBT)
    static final ItemMetaKey BLOCK_DATA = new ItemMetaKey("BlockStateTag");
    static final ItemMetaKey BUKKIT_CUSTOM_TAG = new ItemMetaKey("PublicBukkitValues");

    private Text displayName;
    private Text locName;
    private List<Text> lore;
    private Integer customModelData;
    private NbtCompound blockData;
    private Map<Enchantment, Integer> enchantments;
    private Multimap<Attribute, AttributeModifier> attributeModifiers;
    private int repairCost;
    private int hideFlag;
    private boolean unbreakable;
    private int damage;

    private static final Set<String> HANDLED_TAGS = Sets.newHashSet();
    private static final CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY = new CraftPersistentDataTypeRegistry();

    private NbtCompound internalTag;
    private final Map<String, NbtElement> unhandledTags = new HashMap<String, NbtElement>();
    private CraftPersistentDataContainer persistentDataContainer = new CraftPersistentDataContainer(DATA_TYPE_REGISTRY);

    private int version = CraftMagicNumbers.INSTANCE.getDataVersion(); // Internal use only

    CraftMetaItem(CraftMetaItem meta) {
        if (meta == null)
            return;

        this.displayName = meta.displayName;
        this.locName = meta.locName;

        if (meta.hasLore())
            this.lore = new ArrayList<Text>(meta.lore);

        this.customModelData = meta.customModelData;
        this.blockData = meta.blockData;

        if (meta.enchantments != null)
            this.enchantments = new LinkedHashMap<Enchantment, Integer>(meta.enchantments); // Spigot

        if (meta.hasAttributeModifiers())
            this.attributeModifiers = LinkedHashMultimap.create(meta.attributeModifiers);

        this.repairCost = meta.repairCost;
        this.hideFlag = meta.hideFlag;
        this.unbreakable = meta.unbreakable;
        this.damage = meta.damage;
        this.unhandledTags.putAll(meta.unhandledTags);
        this.persistentDataContainer.putAll(meta.persistentDataContainer.getRaw());

        this.internalTag = meta.internalTag;
        if (this.internalTag != null)
            deserializeInternal(internalTag, meta);

        this.version = meta.version;
    }

    CraftMetaItem(NbtCompound tag) {
        if (tag.contains(DISPLAY.NBT)) {
            NbtCompound display = tag.getCompound(DISPLAY.NBT);

            if (display.contains(NAME.NBT)) {
                try {
                    displayName = Text.Serialization.fromJson( limit( display.getString(NAME.NBT), 1024 ) ); // Spigot
                } catch (JsonParseException ignore) {}
            }

            if (display.contains(LOCNAME.NBT)) {
                try {
                    locName = Text.Serialization.fromJson( limit( display.getString(LOCNAME.NBT), 1024 ) ); // Spigot
                } catch (JsonParseException ignore) {}
            }

            if (display.contains(LORE.NBT)) {
                NbtList list = display.getList(LORE.NBT, CraftMagicNumbers.NBT.TAG_STRING);
                lore = new ArrayList<Text>(list.size());

                for (int index = 0; index < list.size(); index++) {
                    String line = limit( list.getString(index), 8192 ); // Spigot
                    try {
                        lore.add(Text.Serialization.fromJson(line));
                    } catch (JsonParseException ignore) {}
                }
            }
        }

        if (tag.contains(CUSTOM_MODEL_DATA.NBT, CraftMagicNumbers.NBT.TAG_INT))
            customModelData = tag.getInt(CUSTOM_MODEL_DATA.NBT);

        if (tag.contains(BLOCK_DATA.NBT, CraftMagicNumbers.NBT.TAG_COMPOUND))
            blockData = tag.getCompound(BLOCK_DATA.NBT);

        this.enchantments = buildEnchantments(tag, ENCHANTMENTS);
        this.attributeModifiers = buildModifiers(tag, ATTRIBUTES);

        if (tag.contains(REPAIR.NBT))
            repairCost = tag.getInt(REPAIR.NBT);

        if (tag.contains(HIDEFLAGS.NBT))
            hideFlag = tag.getInt(HIDEFLAGS.NBT);

        if (tag.contains(UNBREAKABLE.NBT))
            unbreakable = tag.getBoolean(UNBREAKABLE.NBT);

        if (tag.contains(DAMAGE.NBT))
            damage = tag.getInt(DAMAGE.NBT);

        if (tag.contains(BUKKIT_CUSTOM_TAG.NBT)) {
            NbtCompound compound = tag.getCompound(BUKKIT_CUSTOM_TAG.NBT);
            Set<String> keys = compound.getKeys();
            for (String key : keys)
                persistentDataContainer.put(key, compound.get(key));
        }

        Set<String> keys = tag.getKeys();
        for (String key : keys)
            if (!getHandledTags().contains(key)) unhandledTags.put(key, tag.get(key));
    }

    static Map<Enchantment, Integer> buildEnchantments(NbtCompound tag, ItemMetaKey key) {
        if (!tag.contains(key.NBT)) return null;

        NbtList ench = tag.getList(key.NBT, CraftMagicNumbers.NBT.TAG_COMPOUND);
        Map<Enchantment, Integer> enchantments = new LinkedHashMap<Enchantment, Integer>(ench.size());

        for (int i = 0; i < ench.size(); i++) {
            String id = ((NbtCompound) ench.get(i)).getString(ENCHANTMENTS_ID.NBT);
            int level = 0xffff & ((NbtCompound) ench.get(i)).getShort(ENCHANTMENTS_LVL.NBT);

            Enchantment enchant = Enchantment.getByKey(CraftNamespacedKey.fromStringOrNull(id));
            if (enchant != null) enchantments.put(enchant, level);
        }
        return enchantments;
    }

    static Multimap<Attribute, AttributeModifier> buildModifiers(NbtCompound tag, ItemMetaKey key) {
        Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create();
        if (!tag.contains(key.NBT, CraftMagicNumbers.NBT.TAG_LIST))
            return modifiers;

        NbtList mods = tag.getList(key.NBT, CraftMagicNumbers.NBT.TAG_COMPOUND);
        int size = mods.size();

        for (int i = 0; i < size; i++) {
            NbtCompound entry = mods.getCompound(i);
            if (entry.isEmpty()) continue; // entry is not an actual CompoundTag. getCompound returns empty CompoundTag in that case

            EntityAttributeModifier nmsModifier = EntityAttributeModifier.fromNbt(entry);
            if (nmsModifier == null) continue;

            AttributeModifier attribMod = CardboardAttributeInstance.convert(nmsModifier);

            String attributeName = entry.getString(ATTRIBUTES_IDENTIFIER.NBT);
            if (attributeName == null || attributeName.isEmpty()) continue;

            Attribute attribute = CardboardAttributable.fromMinecraft(attributeName);
            if (attribute == null) continue;

            if (entry.contains(ATTRIBUTES_SLOT.NBT, CraftMagicNumbers.NBT.TAG_STRING)) {
                String slotName = entry.getString(ATTRIBUTES_SLOT.NBT);
                if (slotName == null || slotName.isEmpty()) {
                    modifiers.put(attribute, attribMod);
                    continue;
                }

                org.bukkit.inventory.EquipmentSlot slot = null;
                try {
                    slot = Utils.getSlot(EquipmentSlot.byName(slotName.toLowerCase(Locale.ROOT)));
                } catch (IllegalArgumentException ex) {
                    // SPIGOT-4551 - Slot is invalid, should really match nothing but this is undefined behavior anyway
                }

                if (slot == null) {
                    modifiers.put(attribute, attribMod);
                    continue;
                }

                attribMod = new AttributeModifier(attribMod.getUniqueId(), attribMod.getName(), attribMod.getAmount(), attribMod.getOperation(), slot);
            }
            modifiers.put(attribute, attribMod);
        }
        return modifiers;
    }

    CraftMetaItem(Map<String, Object> map) {
        setDisplayName(SerializableMeta.getString(map, NAME.BUKKIT, true));
        setLocalizedName(SerializableMeta.getString(map, LOCNAME.BUKKIT, true));

        Iterable<?> lore = SerializableMeta.getObject(Iterable.class, map, LORE.BUKKIT, true);
        if (lore != null)
            safelyAdd(lore, this.lore = new ArrayList<Text>(), Integer.MAX_VALUE);

        Integer customModelData = SerializableMeta.getObject(Integer.class, map, CUSTOM_MODEL_DATA.BUKKIT, true);
        if (customModelData != null)
            setCustomModelData(customModelData);

        Map blockData = SerializableMeta.getObject(Map.class, map, BLOCK_DATA.BUKKIT, true);
        if (blockData != null)
            this.blockData = (NbtCompound) CraftNBTTagConfigSerializer.deserialize(blockData);

        enchantments = buildEnchantments(map, ENCHANTMENTS);
        attributeModifiers = buildModifiers(map, ATTRIBUTES);

        Integer repairCost = SerializableMeta.getObject(Integer.class, map, REPAIR.BUKKIT, true);
        if (repairCost != null)
            setRepairCost(repairCost);

        Iterable<?> hideFlags = SerializableMeta.getObject(Iterable.class, map, HIDEFLAGS.BUKKIT, true);
        if (hideFlags != null) {
            for (Object hideFlagObject : hideFlags) {
                String hideFlagString = (String) hideFlagObject;
                try {
                    ItemFlag hideFlatEnum = ItemFlag.valueOf(hideFlagString);
                    addItemFlags(hideFlatEnum);
                } catch (IllegalArgumentException ex) {
                    // Ignore when we got a old String which does not map to a Enum value anymore
                }
            }
        }

        Boolean unbreakable = SerializableMeta.getObject(Boolean.class, map, UNBREAKABLE.BUKKIT, true);
        if (unbreakable != null)
            setUnbreakable(unbreakable);

        Integer damage = SerializableMeta.getObject(Integer.class, map, DAMAGE.BUKKIT, true);
        if (damage != null)
            setDamage(damage);

        String internal = SerializableMeta.getString(map, "internal", true);
        if (internal != null) {
            ByteArrayInputStream buf = new ByteArrayInputStream(Base64.getDecoder().decode(internal));
        	try {
                internalTag = NbtIo.readCompressed(buf, NbtTagSizeTracker.ofUnlimitedBytes());
                deserializeInternal(internalTag, map);
                Set<String> keys = internalTag.getKeys();
                for (String key : keys)
                    if (!getHandledTags().contains(key))
                        unhandledTags.put(key, internalTag.get(key));
            } catch (IOException ex) {
                Logger.getLogger(CraftMetaItem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        Map nbtMap = SerializableMeta.getObject(Map.class, map, BUKKIT_CUSTOM_TAG.BUKKIT, true);
        if (nbtMap != null)
            this.persistentDataContainer.putAll((NbtCompound) CraftNBTTagConfigSerializer.deserialize(nbtMap));
    }

    void deserializeInternal(NbtCompound tag, Object context) {
        // SPIGOT-4576: Need to migrate from internal to proper data
        if (tag.contains(ATTRIBUTES.NBT, CraftMagicNumbers.NBT.TAG_LIST))
            this.attributeModifiers = buildModifiers(tag, ATTRIBUTES);
    }

    static Map<Enchantment, Integer> buildEnchantments(Map<String, Object> map, ItemMetaKey key) {
        Map<?, ?> ench = SerializableMeta.getObject(Map.class, map, key.BUKKIT, true);
        if (ench == null)
            return null;

        Map<Enchantment, Integer> enchantments = new LinkedHashMap<Enchantment, Integer>(ench.size());
        for (Map.Entry<?, ?> entry : ench.entrySet()) {
            // Doctor older enchants
            String enchantKey = entry.getKey().toString();
            if (enchantKey.equals("SWEEPING")) enchantKey = "SWEEPING_EDGE";

            Enchantment enchantment = Enchantment.getByName(enchantKey);
            if ((enchantment != null) && (entry.getValue() instanceof Integer))
                enchantments.put(enchantment, (Integer) entry.getValue());
        }

        return enchantments;
    }

    static Multimap<Attribute, AttributeModifier> buildModifiers(Map<String, Object> map, ItemMetaKey key) {
        Map<?, ?> mods = SerializableMeta.getObject(Map.class, map, key.BUKKIT, true);
        Multimap<Attribute, AttributeModifier> result = LinkedHashMultimap.create();
        if (mods == null)
            return result;

        for (Object obj : mods.keySet()) {
            if (!(obj instanceof String)) continue;

            String attributeName = (String) obj;
            if (Strings.isNullOrEmpty(attributeName)) continue;

            List<?> list = SerializableMeta.getObject(List.class, mods, attributeName, true);
            if (list == null || list.isEmpty())
                return result;

            for (Object o : list) {
                if (!(o instanceof AttributeModifier)) continue; // catches NPE

                AttributeModifier modifier = (AttributeModifier) o;
                Attribute attribute = EnumUtils.getEnum(Attribute.class, attributeName.toUpperCase(Locale.ROOT));
                if (attribute == null) continue;

                result.put(attribute, modifier);
            }
        }
        return result;
    }

    void applyToItem(NbtCompound itemTag) {
        if (hasDisplayName())
            setDisplayTag(itemTag, NAME.NBT, NbtString.of(CraftChatMessage.toJSON(displayName)));

        if (hasLocalizedName())
            setDisplayTag(itemTag, LOCNAME.NBT, NbtString.of(CraftChatMessage.toJSON(locName)));

        if (hasLore())
            setDisplayTag(itemTag, LORE.NBT, createStringList(lore));

        if (hasCustomModelData())
            itemTag.putInt(CUSTOM_MODEL_DATA.NBT, customModelData);

        if (hasBlockData())
            itemTag.put(BLOCK_DATA.NBT, blockData);

        if (hideFlag != 0)
            itemTag.putInt(HIDEFLAGS.NBT, hideFlag);

        applyEnchantments(enchantments, itemTag, ENCHANTMENTS);
        applyModifiers(attributeModifiers, itemTag, ATTRIBUTES);

        if (hasRepairCost())
            itemTag.putInt(REPAIR.NBT, repairCost);

        if (isUnbreakable())
            itemTag.putBoolean(UNBREAKABLE.NBT, unbreakable);

        if (hasDamage())
            itemTag.putInt(DAMAGE.NBT, damage);

        for (Map.Entry<String, NbtElement> e : unhandledTags.entrySet())
            itemTag.put(e.getKey(), e.getValue());

        if (!persistentDataContainer.isEmpty()) {
            NbtCompound bukkitCustomCompound = new NbtCompound();
            Map<String, NbtElement> rawPublicMap = persistentDataContainer.getRaw();

            for (Map.Entry<String, NbtElement> TagEntry : rawPublicMap.entrySet())
                bukkitCustomCompound.put(TagEntry.getKey(), TagEntry.getValue());

            itemTag.put(BUKKIT_CUSTOM_TAG.NBT, bukkitCustomCompound);
        }
    }

    NbtList createStringList(List<Text> list) {
        if (list == null || list.isEmpty()) return null;

        NbtList tagList = new NbtList();
        for (Text value : list)
            tagList.add(NbtString.of(version <= 0 || version >= 1803 ? CraftChatMessage.toJSON(value) : CraftChatMessage.fromComponent(value, Formatting.DARK_PURPLE))); // SPIGOT-4935

        return tagList;
    }

    static void applyEnchantments(Map<Enchantment, Integer> enchantments, NbtCompound tag, ItemMetaKey key) {
        if (enchantments == null) return;

        NbtList list = new NbtList();

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            NbtCompound subtag = new NbtCompound();

            subtag.putString(ENCHANTMENTS_ID.NBT, entry.getKey().getKey().toString());
            subtag.putShort(ENCHANTMENTS_LVL.NBT, entry.getValue().shortValue());

            list.add(subtag);
        }
        tag.put(key.NBT, list);
    }

    static void applyModifiers(Multimap<Attribute, AttributeModifier> modifiers, NbtCompound tag, ItemMetaKey key) {
        if (modifiers == null || modifiers.isEmpty())
            return;

        NbtList list = new NbtList();
        for (Map.Entry<Attribute, AttributeModifier> entry : modifiers.entries()) {
            if (entry.getKey() == null || entry.getValue() == null)
                continue;
            net.minecraft.entity.attribute.EntityAttributeModifier nmsModifier = CardboardAttributeInstance.convert(entry.getValue());
            NbtCompound sub = nmsModifier.toNbt();
            if (sub.isEmpty()) continue;

            String name = entry.getKey().getKey().toString();
            if (name == null || name.isEmpty()) continue;

            sub.putString(ATTRIBUTES_IDENTIFIER.NBT, name); // Attribute Name
            if (entry.getValue().getSlot() != null) {
                net.minecraft.entity.EquipmentSlot slot = Utils.getNMS(entry.getValue().getSlot());
                if (slot != null) sub.putString(ATTRIBUTES_SLOT.NBT, slot.getName());
            }
            list.add(sub);
        }
        tag.put(key.NBT, list);
    }

    void setDisplayTag(NbtCompound tag, String key, NbtElement value) {
        final NbtCompound display = tag.getCompound(DISPLAY.NBT);
        if (!tag.contains(DISPLAY.NBT)) tag.put(DISPLAY.NBT, display);
        display.put(key, value);
    }

    boolean applicableTo(Material type) {
        return type != Material.AIR;
    }

    boolean isEmpty() {
        return !(hasDisplayName() || hasLocalizedName() || hasEnchants() || hasLore() || hasCustomModelData() || hasBlockData() || hasRepairCost() || !unhandledTags.isEmpty() || !persistentDataContainer.isEmpty() || hideFlag != 0 || isUnbreakable() || hasDamage() || hasAttributeModifiers());
    }

    @Override
    public String getDisplayName() {
        return CraftChatMessage.fromComponent(displayName, Formatting.WHITE);
    }

    @Override
    public final void setDisplayName(String name) {
        this.displayName = CraftChatMessage.wrapOrNull(name);
    }

    @Override
    public boolean hasDisplayName() {
        return displayName != null;
    }

    @Override
    public String getLocalizedName() {
        return CraftChatMessage.fromComponent(locName, Formatting.WHITE);
    }

    @Override
    public void setLocalizedName(String name) {
        this.locName = CraftChatMessage.wrapOrNull(name);
    }

    @Override
    public boolean hasLocalizedName() {
        return locName != null;
    }

    @Override
    public boolean hasLore() {
        return this.lore != null && !this.lore.isEmpty();
    }

    @Override
    public boolean hasRepairCost() {
        return repairCost > 0;
    }

    @Override
    public boolean hasEnchant(Enchantment ench) {
        return hasEnchants() && enchantments.containsKey(ench);
    }

    @Override
    public int getEnchantLevel(Enchantment ench) {
        Validate.notNull(ench, "Enchantment cannot be null");
        Integer level = hasEnchants() ? enchantments.get(ench) : null;
        return (level == null) ? 0 : level;
    }

    @Override
    public Map<Enchantment, Integer> getEnchants() {
        return hasEnchants() ? ImmutableMap.copyOf(enchantments) : ImmutableMap.<Enchantment, Integer>of();
    }

    @Override
    public boolean addEnchant(Enchantment ench, int level, boolean ignoreRestrictions) {
        if (enchantments == null) enchantments = new LinkedHashMap<Enchantment, Integer>(4);

        if (ignoreRestrictions || level >= ench.getStartLevel() && level <= ench.getMaxLevel()) {
            Integer old = enchantments.put(ench, level);
            return old == null || old != level;
        }
        return false;
    }

    @Override
    public boolean removeEnchant(Enchantment ench) {
        boolean b = hasEnchants() && enchantments.remove(ench) != null;
        if (enchantments != null && enchantments.isEmpty()) this.enchantments = null;
        return b;
    }

    @Override
    public boolean hasEnchants() {
        return !(enchantments == null || enchantments.isEmpty());
    }

    @Override
    public boolean hasConflictingEnchant(Enchantment ench) {
        return checkConflictingEnchants(enchantments, ench);
    }

    @Override
    public void addItemFlags(ItemFlag... hideFlags) {
        for (ItemFlag f : hideFlags)
            this.hideFlag |= getBitModifier(f);
    }

    @Override
    public void removeItemFlags(ItemFlag... hideFlags) {
        for (ItemFlag f : hideFlags)
            this.hideFlag &= ~getBitModifier(f);
    }

    @Override
    public Set<ItemFlag> getItemFlags() {
        Set<ItemFlag> currentFlags = EnumSet.noneOf(ItemFlag.class);

        for (ItemFlag f : ItemFlag.values())
            if (hasItemFlag(f)) currentFlags.add(f);
        return currentFlags;
    }

    @Override
    public boolean hasItemFlag(ItemFlag flag) {
        int bitModifier = getBitModifier(flag);
        return (this.hideFlag & bitModifier) == bitModifier;
    }

    private byte getBitModifier(ItemFlag hideFlag) {
        return (byte) (1 << hideFlag.ordinal());
    }

    @Override
    public List<String> getLore() {
        return this.lore == null ? null : new ArrayList<String>(Lists.transform(this.lore, (line) -> CraftChatMessage.fromComponent(line, Formatting.DARK_PURPLE)));
    }

    @Override
    public void setLore(List<String> lore) { // too tired to think if .clone is better
        if (lore == null) {
            this.lore = null;
        } else {
            if (this.lore == null) {
                safelyAdd(lore, this.lore = new ArrayList<Text>(lore.size()), Integer.MAX_VALUE);
            } else {
                this.lore.clear();
                safelyAdd(lore, this.lore, Integer.MAX_VALUE);
            }
        }
    }

    @Override
    public boolean hasCustomModelData() {
        return customModelData != null;
    }

    @Override
    public int getCustomModelData() {
        Preconditions.checkState(hasCustomModelData(), "We don't have CustomModelData! Check hasCustomModelData first!");
        return customModelData;
    }

    @Override
    public void setCustomModelData(Integer data) {
        this.customModelData = data;
    }

    @Override
    public boolean hasBlockData() {
       return this.blockData != null;
    }

    @Override
    public BlockData getBlockData(Material material) {
        return  CraftBlockData.fromData(getBlockState(CraftMagicNumbers.getBlock(material).getDefaultState(), blockData));
    }

    /**
     * Ported functionality from ItemBlock.patch
     */
    public static BlockState getBlockState(BlockState iblockdata, NbtCompound nbttagcompound1) {
        BlockState iblockdata1 = iblockdata;
        {
            StateManager<Block, BlockState> blockstatelist = iblockdata.getBlock().getStateManager();
            Iterator<String> iterator = nbttagcompound1.getKeys().iterator();

            while (iterator.hasNext()) {
                String s = (String) iterator.next();
                Property<?> iblockstate = blockstatelist.getProperty(s);

                if (iblockstate != null) {
                    String s1 = nbttagcompound1.get(s).asString();
                    iblockdata1 = BlockItem.with(iblockdata1, iblockstate, s1);
                }
            }
        }
        return iblockdata1;
    }

    @Override
    public void setBlockData(BlockData blockData) {
        this.blockData = (blockData == null) ? null : ((CraftBlockData) blockData).toStates();
    }

    @Override
    public int getRepairCost() {
        return repairCost;
    }

    @Override
    public void setRepairCost(int cost) {
        repairCost = cost;
    }

    @Override
    public boolean isUnbreakable() {
        return unbreakable;
    }

    @Override
    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    @Override
    public boolean hasAttributeModifiers() {
        return attributeModifiers != null && !attributeModifiers.isEmpty();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers() {
        return hasAttributeModifiers() ? ImmutableMultimap.copyOf(attributeModifiers) : null;
    }

    private void checkAttributeList() {
        if (attributeModifiers == null)
            attributeModifiers = LinkedHashMultimap.create();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(org.bukkit.inventory.EquipmentSlot slot) {
        checkAttributeList();
        SetMultimap<Attribute, AttributeModifier> result = LinkedHashMultimap.create();
        for (Map.Entry<Attribute, AttributeModifier> entry : attributeModifiers.entries())
            if (entry.getValue().getSlot() == null || entry.getValue().getSlot() == slot)
                result.put(entry.getKey(), entry.getValue());
        return result;
    }

    @Override
    public Collection<AttributeModifier> getAttributeModifiers(org.bukkit.attribute.Attribute attribute) {
        Preconditions.checkNotNull(attribute, "Attribute cannot be null");
        return attributeModifiers.containsKey(attribute) ? ImmutableList.copyOf(attributeModifiers.get(attribute)) : null;
    }

    @Override
    public boolean addAttributeModifier(org.bukkit.attribute.Attribute attribute, AttributeModifier modifier) {
        Preconditions.checkNotNull(attribute, "Attribute cannot be null");
        Preconditions.checkNotNull(modifier, "AttributeModifier cannot be null");
        checkAttributeList();
        for (Map.Entry<Attribute, AttributeModifier> entry : attributeModifiers.entries())
            Preconditions.checkArgument(!entry.getValue().getUniqueId().equals(modifier.getUniqueId()), "Cannot register AttributeModifier. Modifier is already applied! %s", modifier);
        return attributeModifiers.put(attribute, modifier);
    }

    @Override
    public void setAttributeModifiers(Multimap<org.bukkit.attribute.Attribute, AttributeModifier> attributeModifiers) {
        if (attributeModifiers == null || attributeModifiers.isEmpty()) {
            this.attributeModifiers = LinkedHashMultimap.create();
            return;
        }

        checkAttributeList();
        this.attributeModifiers.clear();

        Iterator<Map.Entry<Attribute, AttributeModifier>> iterator = attributeModifiers.entries().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Attribute, AttributeModifier> next = iterator.next();
            if (next.getKey() == null || next.getValue() == null) {
                iterator.remove();
                continue;
            }
            this.attributeModifiers.put(next.getKey(), next.getValue());
        }
    }

    @Override
    public boolean removeAttributeModifier(org.bukkit.attribute.Attribute attribute) {
        Preconditions.checkNotNull(attribute, "Attribute cannot be null");
        checkAttributeList();
        return !attributeModifiers.removeAll(attribute).isEmpty();
    }

    @Override
    public boolean removeAttributeModifier(org.bukkit.inventory.EquipmentSlot slot) {
        checkAttributeList();
        int removed = 0;
        Iterator<Map.Entry<Attribute, AttributeModifier>> iter = attributeModifiers.entries().iterator();

        while (iter.hasNext()) {
            Map.Entry<Attribute, AttributeModifier> entry = iter.next();
            // Explicitly match against null because (as of MC 1.13) AttributeModifiers without a -
            // set slot are active in any slot.
            if (entry.getValue().getSlot() == null || entry.getValue().getSlot() == slot) {
                iter.remove();
                ++removed;
            }
        }
        return removed > 0;
    }

    @Override
    public boolean removeAttributeModifier(org.bukkit.attribute.Attribute attribute, AttributeModifier modifier) {
        Preconditions.checkNotNull(attribute, "Attribute cannot be null");
        Preconditions.checkNotNull(modifier, "AttributeModifier cannot be null");
        checkAttributeList();
        int removed = 0;
        Iterator<Map.Entry<Attribute, AttributeModifier>> iter = attributeModifiers.entries().iterator();

        while (iter.hasNext()) {
            Map.Entry<Attribute, AttributeModifier> entry = iter.next();
            if (entry.getKey() == null || entry.getValue() == null) {
                iter.remove();
                ++removed;
                continue; // remove all null values while we are here
            }

            if (entry.getKey() == attribute && entry.getValue().getUniqueId().equals(modifier.getUniqueId())) {
                iter.remove();
                ++removed;
            }
        }
        return removed > 0;
    }

    @Override
    public CustomItemTagContainer getCustomTagContainer() {
        return new DeprecatedCustomTagContainer(this.getPersistentDataContainer());
    }

    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        return this.persistentDataContainer;
    }

    private static boolean compareModifiers(Multimap<Attribute, AttributeModifier> first, Multimap<Attribute, AttributeModifier> second) {
        if (first == null || second == null) return false;

        for (Map.Entry<Attribute, AttributeModifier> entry : first.entries())
            if (!second.containsEntry(entry.getKey(), entry.getValue()))
                return false;

        for (Map.Entry<Attribute, AttributeModifier> entry : second.entries())
            if (!first.containsEntry(entry.getKey(), entry.getValue()))
                return false;
        return true;
    }

    @Override
    public boolean hasDamage() {
        return damage > 0;
    }

    @Override
    public int getDamage() {
        return damage;
    }

    @Override
    public void setDamage(int damage) {
        this.damage = damage;
    }

    @Override
    public final boolean equals(Object object) {
        if (object == null) return false;
        if (object == this) return true;

        if (!(object instanceof CraftMetaItem)) return false;
        return CraftItemFactory.instance().equals(this, (ItemMeta) object);
    }

    /**
     * This method is almost as weird as notUncommon.
     * Only return false if your common internals are unequal.
     * Checking your own internals is redundant if you are not common, as notUncommon is meant for checking those 'not common' variables.
     */
    boolean equalsCommon(CraftMetaItem that) {
        return ((this.hasDisplayName() ? that.hasDisplayName() && this.displayName.equals(that.displayName) : !that.hasDisplayName()))
                && (this.hasLocalizedName() ? that.hasLocalizedName() && this.locName.equals(that.locName) : !that.hasLocalizedName())
                && (this.hasEnchants() ? that.hasEnchants() && this.enchantments.equals(that.enchantments) : !that.hasEnchants())
                && (this.hasLore() ? that.hasLore() && this.lore.equals(that.lore) : !that.hasLore())
                && (this.hasCustomModelData() ? that.hasCustomModelData() && this.customModelData.equals(that.customModelData) : !that.hasCustomModelData())
                && (this.hasBlockData() ? that.hasBlockData() && this.blockData.equals(that.blockData) : !that.hasBlockData())
                && (this.hasRepairCost() ? that.hasRepairCost() && this.repairCost == that.repairCost : !that.hasRepairCost())
                && (this.hasAttributeModifiers() ? that.hasAttributeModifiers() && compareModifiers(this.attributeModifiers, that.attributeModifiers) : !that.hasAttributeModifiers())
                && (this.unhandledTags.equals(that.unhandledTags))
                && (this.persistentDataContainer.equals(that.persistentDataContainer))
                && (this.hideFlag == that.hideFlag)
                && (this.isUnbreakable() == that.isUnbreakable())
                && (this.hasDamage() ? that.hasDamage() && this.damage == that.damage : !that.hasDamage())
                && (this.version == that.version);
    }

    /**
     * This method is a bit weird...
     * Return true if you are a common class OR your uncommon parts are empty.
     * Empty uncommon parts implies the NBT data would be equivalent if both were applied to an item
     */
    boolean notUncommon(CraftMetaItem meta) {
        return true;
    }

    @Override
    public final int hashCode() {
        return applyHash();
    }

    int applyHash() {
        int hash = 3;
        hash = 61 * hash + (hasDisplayName() ? this.displayName.hashCode() : 0);
        hash = 61 * hash + (hasLocalizedName() ? this.locName.hashCode() : 0);
        hash = 61 * hash + (hasLore() ? this.lore.hashCode() : 0);
        hash = 61 * hash + (hasCustomModelData() ? this.customModelData.hashCode() : 0);
        hash = 61 * hash + (hasBlockData() ? this.blockData.hashCode() : 0);
        hash = 61 * hash + (hasEnchants() ? this.enchantments.hashCode() : 0);
        hash = 61 * hash + (hasRepairCost() ? this.repairCost : 0);
        hash = 61 * hash + unhandledTags.hashCode();
        hash = 61 * hash + (!persistentDataContainer.isEmpty() ? persistentDataContainer.hashCode() : 0);
        hash = 61 * hash + hideFlag;
        hash = 61 * hash + (isUnbreakable() ? 1231 : 1237);
        hash = 61 * hash + (hasDamage() ? this.damage : 0);
        hash = 61 * hash + (hasAttributeModifiers() ? this.attributeModifiers.hashCode() : 0);
        hash = 61 * hash + version;
        return hash;
    }

    @Override
    public CraftMetaItem clone() {
        try {
            CraftMetaItem clone = (CraftMetaItem) super.clone();
            if (this.lore != null) clone.lore = new ArrayList<Text>(this.lore);

            clone.customModelData = this.customModelData;
            clone.blockData = this.blockData;
            if (this.enchantments != null)
                clone.enchantments = new LinkedHashMap<Enchantment, Integer>(this.enchantments);

            if (this.hasAttributeModifiers())
                clone.attributeModifiers = LinkedHashMultimap.create(this.attributeModifiers);

            clone.persistentDataContainer = new CraftPersistentDataContainer(this.persistentDataContainer.getRaw(), DATA_TYPE_REGISTRY);
            clone.hideFlag = this.hideFlag;
            clone.unbreakable = this.unbreakable;
            clone.damage = this.damage;
            clone.version = this.version;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

    @Override
    public final Map<String, Object> serialize() {
        ImmutableMap.Builder<String, Object> map = ImmutableMap.builder();
        map.put(SerializableMeta.TYPE_FIELD, SerializableMeta.classMap.get(getClass()));
        serialize(map);
        return map.build();
    }

    ImmutableMap.Builder<String, Object> serialize(ImmutableMap.Builder<String, Object> builder) {
        if (hasDisplayName())
            builder.put(NAME.BUKKIT, CraftChatMessage.fromComponent(displayName));

        if (hasLocalizedName())
            builder.put(LOCNAME.BUKKIT, CraftChatMessage.fromComponent(locName));

        if (hasLore())
            builder.put(LORE.BUKKIT, ImmutableList.copyOf(Lists.transform(lore, CraftChatMessage::fromComponent)));

        if (hasCustomModelData())
            builder.put(CUSTOM_MODEL_DATA.BUKKIT, customModelData);

        if (hasBlockData())
            builder.put(BLOCK_DATA.BUKKIT, CraftNBTTagConfigSerializer.serialize(blockData));

        serializeEnchantments(enchantments, builder, ENCHANTMENTS);
        serializeModifiers(attributeModifiers, builder, ATTRIBUTES);

        if (hasRepairCost())
            builder.put(REPAIR.BUKKIT, repairCost);

        List<String> hideFlags = new ArrayList<String>();
        for (ItemFlag hideFlagEnum : getItemFlags())
            hideFlags.add(hideFlagEnum.name());

        if (!hideFlags.isEmpty())
            builder.put(HIDEFLAGS.BUKKIT, hideFlags);

        if (isUnbreakable())
            builder.put(UNBREAKABLE.BUKKIT, unbreakable);

        if (hasDamage())
            builder.put(DAMAGE.BUKKIT, damage);

        final Map<String, NbtElement> internalTags = new HashMap<String, NbtElement>(unhandledTags);
        serializeInternal(internalTags);
        if (!internalTags.isEmpty()) {
            NbtCompound internal = new NbtCompound();
            for (Map.Entry<String, NbtElement> e : internalTags.entrySet())
                internal.put(e.getKey(), e.getValue());

            try {
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                NbtIo.writeCompressed(internal, buf);
                
                builder.put("internal", Base64.getEncoder().encodeToString(buf.toByteArray()));
            } catch (IOException ex) {
                Logger.getLogger(CraftMetaItem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (!persistentDataContainer.isEmpty())
            builder.put(BUKKIT_CUSTOM_TAG.BUKKIT, persistentDataContainer.serialize()); // Store custom tags, wrapped in their compound

        return builder;
    }

    void serializeInternal(final Map<String, NbtElement> unhandledTags) {
    }

    public Material updateMaterial(Material material) {
        return material;
    }

    public static void serializeEnchantments(Map<Enchantment, Integer> enchantments, ImmutableMap.Builder<String, Object> builder, ItemMetaKey key) {
        if (enchantments == null || enchantments.isEmpty()) return;

        ImmutableMap.Builder<String, Integer> enchants = ImmutableMap.builder();
        for (Map.Entry<? extends Enchantment, Integer> enchant : enchantments.entrySet())
            enchants.put(enchant.getKey().getName(), enchant.getValue());

        builder.put(key.BUKKIT, enchants.build());
    }

    public static void serializeModifiers(Multimap<Attribute, AttributeModifier> modifiers, ImmutableMap.Builder<String, Object> builder, ItemMetaKey key) {
        if (modifiers == null || modifiers.isEmpty()) return;

        Map<String, List<Object>> mods = new LinkedHashMap<>();
        for (Map.Entry<Attribute, AttributeModifier> entry : modifiers.entries()) {
            if (entry.getKey() == null) continue;

            Collection<AttributeModifier> modCollection = modifiers.get(entry.getKey());
            if (modCollection == null || modCollection.isEmpty()) continue;
            mods.put(entry.getKey().name(), new ArrayList<>(modCollection));
        }
        builder.put(key.BUKKIT, mods);
    }

    public static void safelyAdd(Iterable<?> addFrom, Collection<Text> addTo, int maxItemLength) {
        if (addFrom == null) return;

        for (Object object : addFrom) {
            if (!(object instanceof String)) {
                if (object != null)
                    throw new IllegalArgumentException(addFrom + " cannot contain non-string " + object.getClass().getName());
                addTo.add(Text.of(""));
            } else {
                String page = object.toString();
                if (page.length() > maxItemLength) page = page.substring(0, maxItemLength);
                addTo.add(CraftChatMessage.wrapOrEmpty(page));
            }
        }
    }

    public static boolean checkConflictingEnchants(Map<Enchantment, Integer> enchantments, Enchantment ench) {
        if (enchantments == null || enchantments.isEmpty()) return false;

        for (Enchantment enchant : enchantments.keySet()) if (enchant.conflictsWith(ench)) return true;
        return false;
    }

    @Override
    public final String toString() {
        return SerializableMeta.classMap.get(getClass()) + "_META:" + serialize();
    }

    public int getVersion() {
        return version;
    }

    @Override
    public void setVersion(int version) {
        this.version = version;
    }

    public static Set<String> getHandledTags() {
        synchronized (HANDLED_TAGS) {
            if (HANDLED_TAGS.isEmpty()) {
                HANDLED_TAGS.addAll(Arrays.asList(
                        DISPLAY.NBT,
                        CUSTOM_MODEL_DATA.NBT,
                        BLOCK_DATA.NBT,
                        REPAIR.NBT,
                        ENCHANTMENTS.NBT,
                        HIDEFLAGS.NBT,
                        UNBREAKABLE.NBT,
                        DAMAGE.NBT,
                        BUKKIT_CUSTOM_TAG.NBT,
                        ATTRIBUTES.NBT,
                        ATTRIBUTES_IDENTIFIER.NBT,
                        ATTRIBUTES_NAME.NBT,
                        ATTRIBUTES_VALUE.NBT,
                        ATTRIBUTES_UUID_HIGH.NBT,
                        ATTRIBUTES_UUID_LOW.NBT,
                        ATTRIBUTES_SLOT.NBT,
                        CraftMetaMap.MAP_SCALING.NBT,
                        CraftMetaMap.MAP_ID.NBT,
                        CraftMetaPotion.POTION_EFFECTS.NBT,
                        CraftMetaPotion.DEFAULT_POTION.NBT,
                        CraftMetaPotion.POTION_COLOR.NBT,
                        CraftMetaSkull.SKULL_OWNER.NBT,
                        CraftMetaSkull.SKULL_PROFILE.NBT,
                        CraftMetaSpawnEgg.ENTITY_TAG.NBT,
                        CraftMetaBlockState.BLOCK_ENTITY_TAG.NBT,
                        CraftMetaBook.BOOK_TITLE.NBT,
                        CraftMetaBook.BOOK_AUTHOR.NBT,
                        CraftMetaBook.BOOK_PAGES.NBT,
                        CraftMetaBook.RESOLVED.NBT,
                        CraftMetaBook.GENERATION.NBT,
                        CraftMetaFirework.FIREWORKS.NBT,
                        CraftMetaEnchantedBook.STORED_ENCHANTMENTS.NBT,
                        CraftMetaCharge.EXPLOSION.NBT,
                        CraftMetaBlockState.BLOCK_ENTITY_TAG.NBT,
                        CraftMetaKnowledgeBook.BOOK_RECIPES.NBT,
                        // TODO CraftMetaTropicalFishBucket.VARIANT.NBT,
                        CraftMetaCrossbow.CHARGED.NBT,
                        CraftMetaCrossbow.CHARGED_PROJECTILES.NBT,
                        CraftMetaSuspiciousStew.EFFECTS.NBT
                ));
            }
            return HANDLED_TAGS;
        }
    }

    @Override
    public Set<Material> getCanDestroy() {
        return Collections.emptySet();//!this.hasDestroyableKeys() ? Collections.emptySet() : this.legacyGetMatsFromKeys(this.destroyableKeys);

    }

    @Override
    public Set<Material> getCanPlaceOn() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Namespaced> getDestroyableKeys() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BaseComponent[] getDisplayNameComponent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<BaseComponent[]> getLoreComponents() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Namespaced> getPlaceableKeys() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasDestroyableKeys() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasPlaceableKeys() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setCanDestroy(Set<Material> arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setCanPlaceOn(Set<Material> arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setDestroyableKeys(Collection<Namespaced> arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setDisplayNameComponent(BaseComponent[] arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setLoreComponents(List<BaseComponent[]> arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setPlaceableKeys(Collection<Namespaced> arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public @Nullable Component displayName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void displayName(@Nullable Component arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public @Nullable List<Component> lore() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void lore(@Nullable List<? extends Component> arg0) {
    // 1.19.2: public void lore(@Nullable List<Component> arg0) {
        // TODO Auto-generated method stub
        
    }
    
    // 1.18.2 api:

	@Override
	public @NotNull String getAsString() {
		// TODO Auto-generated method stub
		return null;
	}

}
