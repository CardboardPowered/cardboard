package org.bukkit.craftbukkit.block.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.cardboardpowered.BlockImplUtil;
import org.cardboardpowered.impl.world.WorldImpl;
import org.jetbrains.annotations.NotNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundGroup;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockSupport;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractBlock.AbstractBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.registry.Registries;

public class CraftBlockData implements BlockData {

    private BlockState state;
    private Map<Property<?>, Comparable<?>> parsedStates;
    private static Map<String, CraftBlockData> stringDataCache;

    protected CraftBlockData() {
        throw new AssertionError("Template Constructor");
    }

    protected CraftBlockData(BlockState state) {
        this.state = state;
    }

    @Override
    public Material getMaterial() {
        return BlockImplUtil.MN.IgetMaterial(state.getBlock());
    }

    public BlockState getState() {
        return state;
    }

    /**
     * Get a given EnumProperty's value as its Bukkit counterpart.
     *
     * @param nms the NMS state to convert
     * @param bukkit the Bukkit class
     * @param <B> the type
     * @return the matching Bukkit type
     */
    protected <B extends Enum<B>> B get(EnumProperty<?> nms, Class<B> bukkit) {
        return toBukkit(state.get(nms), bukkit);
    }

    /**
     * Convert all values from the given EnumProperty to their appropriate
     * Bukkit counterpart.
     *
     * @param nms the NMS state to get values from
     * @param bukkit the bukkit class to convert the values to
     * @param <B> the bukkit class type
     * @return an immutable Set of values in their appropriate Bukkit type
     */
    protected <B extends Enum<B>> Set<B> getValues(EnumProperty<?> nms, Class<B> bukkit) {
        ImmutableSet.Builder<B> values = ImmutableSet.builder();

        for (Enum<?> e : nms.getValues())
            values.add(toBukkit(e, bukkit));

        return values.build();
    }

    /**
     * Set a given {@link EnumProperty} with the matching enum from Bukkit.
     *
     * @param nms the NMS EnumProperty to set
     * @param bukkit the matching Bukkit Enum
     * @param <B> the Bukkit type
     * @param <N> the NMS type
     */
    protected <B extends Enum<B>, N extends Enum<N> & StringIdentifiable> void set(EnumProperty<N> nms, Enum<B> bukkit) {
        this.parsedStates = null;
        this.state = this.state.with(nms, toNMS(bukkit, nms.getType()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public BlockData merge(BlockData data) {
        CraftBlockData craft = (CraftBlockData) data;
        Preconditions.checkArgument(craft.parsedStates != null, "Data not created via string parsing");
        Preconditions.checkArgument(this.state.getBlock() == craft.state.getBlock(), "States have different types (got %s, expected %s)", data, this);

        CraftBlockData clone = (CraftBlockData) this.clone();
        clone.parsedStates = null;

        for (Property parsed : craft.parsedStates.keySet()) clone.state = clone.state.with(parsed, craft.state.get(parsed));

        return clone;
    }

    @Override
    public boolean matches(BlockData data) {
        if (data == null) return false;
        if (!(data instanceof CraftBlockData)) return false;

        CraftBlockData craft = (CraftBlockData) data;
        if (this.state.getBlock() != craft.state.getBlock()) return false;

        // Fastpath an exact match
        boolean exactMatch = this.equals(data);

        // If that failed, do a merge and check
        if (!exactMatch && craft.parsedStates != null)
            return this.merge(data).equals(this);

        return exactMatch;
    }

    private static final Map<Class, BiMap<Enum<?>, Enum<?>>> classMappings = new HashMap<>();

    /**
     * Convert an NMS Enum (usually a EnumProperty) to its appropriate Bukkit
     * enum from the given class.
     *
     * @throws IllegalStateException if the Enum could not be converted
     */
    @SuppressWarnings("unchecked")
    private static <B extends Enum<B>> B toBukkit(Enum<?> nms, Class<B> bukkit) {
        Enum<?> converted;
        BiMap<Enum<?>, Enum<?>> nmsToBukkit = classMappings.get(nms.getClass());

        if (nmsToBukkit != null) {
            converted = nmsToBukkit.get(nms);
            if (converted != null)
                return (B) converted;
        }

        converted = (nms instanceof Direction) ? BlockImplUtil.notchToBlockFace((Direction) nms) : bukkit.getEnumConstants()[nms.ordinal()];

        Preconditions.checkState(converted != null, "Could not convert enum %s->%s", nms, bukkit);

        if (nmsToBukkit == null) {
            nmsToBukkit = HashBiMap.create();
            classMappings.put(nms.getClass(), nmsToBukkit);
        }

        nmsToBukkit.put(nms, converted);

        return (B) converted;
    }

    /**
     * Convert a given Bukkit enum to its matching NMS enum type.
     *
     * @param bukkit the Bukkit enum to convert
     * @param nms the NMS class
     * @return the matching NMS type
     * @throws IllegalStateException if the Enum could not be converted
     */
    @SuppressWarnings("unchecked")
    private static <N extends Enum<N> & StringIdentifiable> N toNMS(Enum<?> bukkit, Class<N> nms) {
        Enum<?> converted;
        BiMap<Enum<?>, Enum<?>> nmsToBukkit = classMappings.get(nms);

        if (nmsToBukkit != null) {
            converted = nmsToBukkit.inverse().get(bukkit);
            if (converted != null)
                return (N) converted;
        }

        converted = (bukkit instanceof BlockFace) ? BlockImplUtil.blockFaceToNotch((BlockFace) bukkit) : nms.getEnumConstants()[bukkit.ordinal()];;

        Preconditions.checkState(converted != null, "Could not convert enum %s->%s", nms, bukkit);

        if (nmsToBukkit == null) {
            nmsToBukkit = HashBiMap.create();
            classMappings.put(nms, nmsToBukkit);
        }

        nmsToBukkit.put(converted, bukkit);

        return (N) converted;
    }

    /**
     * Get the current value of a given state.
     *
     * @param ibs the state to check
     * @param <T> the type
     * @return the current value of the given state
     */
    protected <T extends Comparable<T>> T get(Property<T> ibs) {
        // Straight integer or boolean getter
        return this.state.get(ibs);
    }

    /**
     * Set the specified state's value.
     *
     * @param ibs the state to set
     * @param v the new value
     * @param <T> the state's type
     * @param <V> the value's type. Must match the state's type.
     */
    public <T extends Comparable<T>, V extends T> void set(Property<T> ibs, V v) {
        // Straight integer or boolean setter
        this.parsedStates = null;
        this.state = this.state.with(ibs, v);
    }

    @Override
    public String getAsString() {
        return toString(state.getEntries());
    }

    @Override
    public String getAsString(boolean hideUnspecified) {
        return (hideUnspecified && parsedStates != null) ? toString(parsedStates) : getAsString();
    }

    @Override
    public BlockData clone() {
        try {
            return (BlockData) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError("Clone not supported", ex);
        }
    }

    @Override
    public String toString() {
        return "CardboardBlockData{" + getAsString() + "}";
    }

    // Mimicked from BlockDataAbstract#toString()
    public String toString(Map<Property<?>, Comparable<?>> states) {
        StringBuilder stateString = new StringBuilder(Registries.BLOCK.getId(state.getBlock()).toString());

        if (!states.isEmpty()) {
            stateString.append('[');
            stateString.append(states.entrySet().stream().map(AbstractBlockState.PROPERTY_MAP_PRINTER).collect(Collectors.joining(",")));
            stateString.append(']');
        }

        return stateString.toString();
    }

    @SuppressWarnings("rawtypes")
    public NbtCompound toStates() {
        NbtCompound compound = new NbtCompound();

        for (Map.Entry<Property<?>, Comparable<?>> entry : state.getEntries().entrySet()) {
            Property Property = (Property) entry.getKey();
            compound.putString(Property.getName(), Property.name(entry.getValue()));
        }
        return compound;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CraftBlockData && state.equals(((CraftBlockData) obj).state);
    }

    @Override
    public int hashCode() {
        return state.hashCode();
    }

    protected static BooleanProperty getBoolean(String name) {
        throw new AssertionError("Template Method");
    }

    protected static BooleanProperty getBoolean(String name, boolean optional) {
        throw new AssertionError("Template Method");
    }

    protected static EnumProperty<?> getEnum(String name) {
        throw new AssertionError("Template Method");
    }

    protected static IntProperty getInteger(String name) {
        throw new AssertionError("Template Method");
    }

    protected static BooleanProperty getBoolean(Class<? extends Block> block, String name) {
        return (BooleanProperty) getState(block, name, false);
    }

    protected static BooleanProperty getBoolean(Class<? extends Block> block, String name, boolean optional) {
        return (BooleanProperty) getState(block, name, optional);
    }

    protected static EnumProperty<?> getEnum(Class<? extends Block> block, String name) {
        return (EnumProperty<?>) getState(block, name, false);
    }

    protected static IntProperty getInteger(Class<? extends Block> block, String name) {
        return (IntProperty) getState(block, name, false);
    }

    /**
     * Get a specified {@link Property} from a given block's class with a
     * given name
     *
     * @param block the class to retrieve the state from
     * @param name the name of the state to retrieve
     * @param optional if the state can be null
     * @return the specified state or null
     * @throws IllegalStateException if the state is null and {@code optional}
     * is false.
     */
    private static Property<?> getState(Class<? extends Block> block, String name, boolean optional) {
        Property<?> state = null;

        for (Block instance : Registries.BLOCK) {
            if (instance.getClass() == block) {
                if (state == null) {
                    state = instance.getStateManager().getProperty(name);
                } else {
                    Property<?> newState = instance.getStateManager().getProperty(name);
                    Preconditions.checkState(state == newState, "State mistmatch %s,%s", state, newState);
                }
            }
        }

        Preconditions.checkState(optional || state != null, "Null state for %s,%s", block, name);

        return state;
    }

    protected static int getMin(IntProperty state) {
        return 0; // TODO auto-generated method stub
    }

    protected static int getMax(IntProperty state) {
        return Integer.MAX_VALUE; // TODO auto-generated method stub
    }

    private static final Map<Class<? extends Block>, Function<BlockState, CraftBlockData>> MAP = new HashMap<>();

    static {
        // Keep synchronized with Spigot
        register(net.minecraft.block.AnvilBlock.class, org.bukkit.craftbukkit.block.impl.CraftAnvil::new);
        register(net.minecraft.block.BambooBlock.class, org.bukkit.craftbukkit.block.impl.CraftBamboo::new);
        register(net.minecraft.block.BannerBlock.class, org.bukkit.craftbukkit.block.impl.CraftBanner::new);
        register(net.minecraft.block.WallBannerBlock.class, org.bukkit.craftbukkit.block.impl.CraftBannerWall::new);
        register(net.minecraft.block.BarrelBlock.class, org.bukkit.craftbukkit.block.impl.CraftBarrel::new);
        register(net.minecraft.block.BedBlock.class, org.bukkit.craftbukkit.block.impl.CraftBed::new);
        register(net.minecraft.block.BeehiveBlock.class, org.bukkit.craftbukkit.block.impl.CraftBeehive::new);
        register(net.minecraft.block.BeetrootsBlock.class, org.bukkit.craftbukkit.block.impl.CraftBeetroot::new);
        register(net.minecraft.block.BellBlock.class, org.bukkit.craftbukkit.block.impl.CraftBell::new);
        register(net.minecraft.block.BlastFurnaceBlock.class, org.bukkit.craftbukkit.block.impl.CraftBlastFurnace::new);
        register(net.minecraft.block.BrewingStandBlock.class, org.bukkit.craftbukkit.block.impl.CraftBrewingStand::new);
        register(net.minecraft.block.BubbleColumnBlock.class, org.bukkit.craftbukkit.block.impl.CraftBubbleColumn::new);
        register(net.minecraft.block.CactusBlock.class, org.bukkit.craftbukkit.block.impl.CraftCactus::new);
        register(net.minecraft.block.CakeBlock.class, org.bukkit.craftbukkit.block.impl.CraftCake::new);
        register(net.minecraft.block.CampfireBlock.class, org.bukkit.craftbukkit.block.impl.CraftCampfire::new);
        register(net.minecraft.block.CarrotsBlock.class, org.bukkit.craftbukkit.block.impl.CraftCarrots::new);
        register(net.minecraft.block.AbstractCauldronBlock.class, org.bukkit.craftbukkit.block.impl.CraftCauldron::new);
        register(net.minecraft.block.ChainBlock.class, org.bukkit.craftbukkit.block.impl.CraftChain::new);
        register(net.minecraft.block.ChestBlock.class, org.bukkit.craftbukkit.block.impl.CraftChest::new);
        register(net.minecraft.block.TrappedChestBlock.class, org.bukkit.craftbukkit.block.impl.CraftChestTrapped::new);
        register(net.minecraft.block.ChorusFlowerBlock.class, org.bukkit.craftbukkit.block.impl.CraftChorusFlower::new);
        register(net.minecraft.block.ChorusPlantBlock.class, org.bukkit.craftbukkit.block.impl.CraftChorusFruit::new);
        register(net.minecraft.block.WallBlock.class, org.bukkit.craftbukkit.block.impl.CraftCobbleWall::new);
        register(net.minecraft.block.CocoaBlock.class, org.bukkit.craftbukkit.block.impl.CraftCocoa::new);
        register(net.minecraft.block.CommandBlock.class, org.bukkit.craftbukkit.block.impl.CraftCommand::new);
        register(net.minecraft.block.ComposterBlock.class, org.bukkit.craftbukkit.block.impl.CraftComposter::new);
        register(net.minecraft.block.ConduitBlock.class, org.bukkit.craftbukkit.block.impl.CraftConduit::new);
        register(net.minecraft.block.DeadCoralBlock.class, org.bukkit.craftbukkit.block.impl.CraftCoralDead::new);
        register(net.minecraft.block.CoralFanBlock.class, org.bukkit.craftbukkit.block.impl.CraftCoralFan::new);
        register(net.minecraft.block.DeadCoralFanBlock.class, org.bukkit.craftbukkit.block.impl.CraftCoralFanAbstract::new);
        register(net.minecraft.block.CoralWallFanBlock.class, org.bukkit.craftbukkit.block.impl.CraftCoralFanWall::new);
        register(net.minecraft.block.DeadCoralWallFanBlock.class, org.bukkit.craftbukkit.block.impl.CraftCoralFanWallAbstract::new);
        register(net.minecraft.block.CoralBlock.class, org.bukkit.craftbukkit.block.impl.CraftCoralPlant::new);
        register(net.minecraft.block.CropBlock.class, org.bukkit.craftbukkit.block.impl.CraftCrops::new);
        register(net.minecraft.block.DaylightDetectorBlock.class, org.bukkit.craftbukkit.block.impl.CraftDaylightDetector::new);
        register(net.minecraft.block.SnowyBlock.class, org.bukkit.craftbukkit.block.impl.CraftDirtSnow::new);
        register(net.minecraft.block.DispenserBlock.class, org.bukkit.craftbukkit.block.impl.CraftDispenser::new);
        register(net.minecraft.block.DoorBlock.class, org.bukkit.craftbukkit.block.impl.CraftDoor::new);
        register(net.minecraft.block.DropperBlock.class, org.bukkit.craftbukkit.block.impl.CraftDropper::new);
        register(net.minecraft.block.RodBlock.class, org.bukkit.craftbukkit.block.impl.CraftEndRod::new);
        register(net.minecraft.block.EnderChestBlock.class, org.bukkit.craftbukkit.block.impl.CraftEnderChest::new);
        register(net.minecraft.block.EndPortalFrameBlock.class, org.bukkit.craftbukkit.block.impl.CraftEnderPortalFrame::new);
        register(net.minecraft.block.FenceBlock.class, org.bukkit.craftbukkit.block.impl.CraftFence::new);
        register(net.minecraft.block.FenceGateBlock.class, org.bukkit.craftbukkit.block.impl.CraftFenceGate::new);
        register(net.minecraft.block.FireBlock.class, org.bukkit.craftbukkit.block.impl.CraftFire::new);
        register(net.minecraft.block.SignBlock.class, org.bukkit.craftbukkit.block.impl.CraftFloorSign::new);
        register(net.minecraft.block.FluidBlock.class, org.bukkit.craftbukkit.block.impl.CraftFluids::new);
        register(net.minecraft.block.FurnaceBlock.class, org.bukkit.craftbukkit.block.impl.CraftFurnaceFurace::new);
        register(net.minecraft.block.GlazedTerracottaBlock.class, org.bukkit.craftbukkit.block.impl.CraftGlazedTerracotta::new);
        register(net.minecraft.block.GrassBlock.class, org.bukkit.craftbukkit.block.impl.CraftGrass::new);
        register(net.minecraft.block.GrindstoneBlock.class, org.bukkit.craftbukkit.block.impl.CraftGrindstone::new);
        register(net.minecraft.block.HayBlock.class, org.bukkit.craftbukkit.block.impl.CraftHay::new);
        register(net.minecraft.block.HopperBlock.class, org.bukkit.craftbukkit.block.impl.CraftHopper::new);
        register(net.minecraft.block.MushroomBlock.class, org.bukkit.craftbukkit.block.impl.CraftHugeMushroom::new);
        register(net.minecraft.block.FrostedIceBlock.class, org.bukkit.craftbukkit.block.impl.CraftIceFrost::new);
        register(net.minecraft.block.PaneBlock.class, org.bukkit.craftbukkit.block.impl.CraftIronBars::new);
        register(net.minecraft.block.JigsawBlock.class, org.bukkit.craftbukkit.block.impl.CraftJigsaw::new);
        register(net.minecraft.block.JukeboxBlock.class, org.bukkit.craftbukkit.block.impl.CraftJukeBox::new);
        register(net.minecraft.block.KelpBlock.class, org.bukkit.craftbukkit.block.impl.CraftKelp::new);
        register(net.minecraft.block.LadderBlock.class, org.bukkit.craftbukkit.block.impl.CraftLadder::new);
        register(net.minecraft.block.LanternBlock.class, org.bukkit.craftbukkit.block.impl.CraftLantern::new);
        register(net.minecraft.block.LeavesBlock.class, org.bukkit.craftbukkit.block.impl.CraftLeaves::new);
        register(net.minecraft.block.LecternBlock.class, org.bukkit.craftbukkit.block.impl.CraftLectern::new);
        register(net.minecraft.block.LeverBlock.class, org.bukkit.craftbukkit.block.impl.CraftLever::new);
        register(net.minecraft.block.LoomBlock.class, org.bukkit.craftbukkit.block.impl.CraftLoom::new);
        register(net.minecraft.block.DetectorRailBlock.class, org.bukkit.craftbukkit.block.impl.CraftMinecartDetector::new);
        register(net.minecraft.block.RailBlock.class, org.bukkit.craftbukkit.block.impl.CraftMinecartTrack::new);
        register(net.minecraft.block.MyceliumBlock.class, org.bukkit.craftbukkit.block.impl.CraftMycel::new);
        register(net.minecraft.block.NetherWartBlock.class, org.bukkit.craftbukkit.block.impl.CraftNetherWart::new);
        register(net.minecraft.block.NoteBlock.class, org.bukkit.craftbukkit.block.impl.CraftNote::new);
        register(net.minecraft.block.ObserverBlock.class, org.bukkit.craftbukkit.block.impl.CraftObserver::new);
        register(net.minecraft.block.PistonBlock.class, org.bukkit.craftbukkit.block.impl.CraftPiston::new);
        register(net.minecraft.block.PistonHeadBlock.class, org.bukkit.craftbukkit.block.impl.CraftPistonExtension::new);
        register(net.minecraft.block.PistonExtensionBlock.class, org.bukkit.craftbukkit.block.impl.CraftPistonMoving::new);
        register(net.minecraft.block.NetherPortalBlock.class, org.bukkit.craftbukkit.block.impl.CraftPortal::new);
        register(net.minecraft.block.PotatoesBlock.class, org.bukkit.craftbukkit.block.impl.CraftPotatoes::new);
        register(net.minecraft.block.PoweredRailBlock.class, org.bukkit.craftbukkit.block.impl.CraftPoweredRail::new);
        register(net.minecraft.block.PressurePlateBlock.class, org.bukkit.craftbukkit.block.impl.CraftPressurePlateBinary::new);
        register(net.minecraft.block.WeightedPressurePlateBlock.class, org.bukkit.craftbukkit.block.impl.CraftPressurePlateWeighted::new);
        register(net.minecraft.block.CarvedPumpkinBlock.class, org.bukkit.craftbukkit.block.impl.CraftPumpkinCarved::new);
        register(net.minecraft.block.ComparatorBlock.class, org.bukkit.craftbukkit.block.impl.CraftRedstoneComparator::new);
        register(net.minecraft.block.RedstoneLampBlock.class, org.bukkit.craftbukkit.block.impl.CraftRedstoneLamp::new);
        register(net.minecraft.block.RedstoneOreBlock.class, org.bukkit.craftbukkit.block.impl.CraftRedstoneOre::new);
        register(net.minecraft.block.RedstoneTorchBlock.class, org.bukkit.craftbukkit.block.impl.CraftRedstoneTorch::new);
        register(net.minecraft.block.WallRedstoneTorchBlock.class, org.bukkit.craftbukkit.block.impl.CraftRedstoneTorchWall::new);
        register(net.minecraft.block.RedstoneWireBlock.class, org.bukkit.craftbukkit.block.impl.CraftRedstoneWire::new);
        register(net.minecraft.block.SugarCaneBlock.class, org.bukkit.craftbukkit.block.impl.CraftReed::new);
        register(net.minecraft.block.RepeaterBlock.class, org.bukkit.craftbukkit.block.impl.CraftRepeater::new);
        register(net.minecraft.block.RespawnAnchorBlock.class, org.bukkit.craftbukkit.block.impl.CraftRespawnAnchor::new);
        register(net.minecraft.block.PillarBlock.class, org.bukkit.craftbukkit.block.impl.CraftRotatable::new);
        register(net.minecraft.block.SaplingBlock.class, org.bukkit.craftbukkit.block.impl.CraftSapling::new);
        register(net.minecraft.block.ScaffoldingBlock.class, org.bukkit.craftbukkit.block.impl.CraftScaffolding::new);
        register(net.minecraft.block.SeaPickleBlock.class, org.bukkit.craftbukkit.block.impl.CraftSeaPickle::new);
        register(net.minecraft.block.ShulkerBoxBlock.class, org.bukkit.craftbukkit.block.impl.CraftShulkerBox::new);
        register(net.minecraft.block.SkullBlock.class, org.bukkit.craftbukkit.block.impl.CraftSkull::new);
        register(net.minecraft.block.PlayerSkullBlock.class, org.bukkit.craftbukkit.block.impl.CraftSkullPlayer::new);
        register(net.minecraft.block.WallPlayerSkullBlock.class, org.bukkit.craftbukkit.block.impl.CraftSkullPlayerWall::new);
        register(net.minecraft.block.WallSkullBlock.class, org.bukkit.craftbukkit.block.impl.CraftSkullWall::new);
        register(net.minecraft.block.SmokerBlock.class, org.bukkit.craftbukkit.block.impl.CraftSmoker::new);
        register(net.minecraft.block.SnowBlock.class, org.bukkit.craftbukkit.block.impl.CraftSnow::new);
        register(net.minecraft.block.FarmlandBlock.class, org.bukkit.craftbukkit.block.impl.CraftSoil::new);
        register(net.minecraft.block.StainedGlassPaneBlock.class, org.bukkit.craftbukkit.block.impl.CraftStainedGlassPane::new);
        register(net.minecraft.block.StairsBlock.class, org.bukkit.craftbukkit.block.impl.CraftStairs::new);
        register(net.minecraft.block.StemBlock.class, org.bukkit.craftbukkit.block.impl.CraftStem::new);
        register(net.minecraft.block.AttachedStemBlock.class, org.bukkit.craftbukkit.block.impl.CraftStemAttached::new);
        register(net.minecraft.block.SlabBlock.class, org.bukkit.craftbukkit.block.impl.CraftStepAbstract::new);
        // TODO: 1.19.4: register(net.minecraft.block.StoneButtonBlock.class, org.bukkit.craftbukkit.block.impl.CraftStoneButton::new);
        register(net.minecraft.block.StonecutterBlock.class, org.bukkit.craftbukkit.block.impl.CraftStonecutter::new);
        register(net.minecraft.block.StructureBlock.class, org.bukkit.craftbukkit.block.impl.CraftStructure::new);
        register(net.minecraft.block.SweetBerryBushBlock.class, org.bukkit.craftbukkit.block.impl.CraftSweetBerryBush::new);
        register(net.minecraft.block.TntBlock.class, org.bukkit.craftbukkit.block.impl.CraftTNT::new);
        register(net.minecraft.block.TallPlantBlock.class, org.bukkit.craftbukkit.block.impl.CraftTallPlant::new);
        register(net.minecraft.block.TallFlowerBlock.class, org.bukkit.craftbukkit.block.impl.CraftTallPlantFlower::new);
        register(net.minecraft.block.TallSeagrassBlock.class, org.bukkit.craftbukkit.block.impl.CraftTallSeaGrass::new);
        register(net.minecraft.block.TargetBlock.class, org.bukkit.craftbukkit.block.impl.CraftTarget::new);
        register(net.minecraft.block.WallTorchBlock.class, org.bukkit.craftbukkit.block.impl.CraftTorchWall::new);
        register(net.minecraft.block.TrapdoorBlock.class, org.bukkit.craftbukkit.block.impl.CraftTrapdoor::new);
        register(net.minecraft.block.TripwireBlock.class, org.bukkit.craftbukkit.block.impl.CraftTripwire::new);
        register(net.minecraft.block.TripwireHookBlock.class, org.bukkit.craftbukkit.block.impl.CraftTripwireHook::new);
        register(net.minecraft.block.TurtleEggBlock.class, org.bukkit.craftbukkit.block.impl.CraftTurtleEgg::new);
        register(net.minecraft.block.TwistingVinesBlock.class, org.bukkit.craftbukkit.block.impl.CraftTwistingVines::new);
        register(net.minecraft.block.VineBlock.class, org.bukkit.craftbukkit.block.impl.CraftVine::new);
        register(net.minecraft.block.WallSignBlock.class, org.bukkit.craftbukkit.block.impl.CraftWallSign::new);
        register(net.minecraft.block.WeepingVinesBlock.class, org.bukkit.craftbukkit.block.impl.CraftWeepingVines::new);
        register(net.minecraft.block.WitherSkullBlock.class, org.bukkit.craftbukkit.block.impl.CraftWitherSkull::new);
        register(net.minecraft.block.WallWitherSkullBlock.class, org.bukkit.craftbukkit.block.impl.CraftWitherSkullWall::new);
        // TODO: 1.19.4: register(net.minecraft.block.WoodenButtonBlock.class, org.bukkit.craftbukkit.block.impl.CraftWoodButton::new);
        
        stringDataCache = new ConcurrentHashMap<String, CraftBlockData>();
        CraftBlockData.reloadCache();
        // TOODO 1.19: net.minecraft.block.Block.STATE_IDS.iterator().forEachRemaining(AbstractBlock.AbstractBlockState::createCraftBlockData);
    }
    
    public static void reloadCache() {
        stringDataCache.clear();
        // TODO: 1.19 net.minecraft.block.Block.STATE_IDS.forEach(blockData -> stringDataCache.put(blockData.toString(), blockData.createCraftBlockData()));
    }

    private static void register(Class<? extends Block> nms, Function<BlockState, CraftBlockData> bukkit) {
        Preconditions.checkState(MAP.put(nms, bukkit) == null, "Duplicate mapping %s->%s", nms, bukkit);
    }
    
    public static CraftBlockData newData(Material material, String data) {
        net.minecraft.block.Block block;
        Preconditions.checkArgument((material == null || material.isBlock() ? 1 : 0) != 0, (String)"Cannot get data for not block %s", (Object)material);
        if (material != null && (block = CraftMagicNumbers.getBlock(material)) != null) {
            Identifier key = Registries.BLOCK.getId(block);
            data = data == null ? key.toString() : key + (String)data;
        }
        CraftBlockData cached = stringDataCache.computeIfAbsent((String)data, s2 -> CraftBlockData.createNewData(null, s2));
        return (CraftBlockData)cached.clone();
    }
    
    private static CraftBlockData createNewData(Material material, String data) {
        BlockState blockData;
        net.minecraft.block.Block block = CraftMagicNumbers.getBlock(material);
        Map<Property<?>, Comparable<?>> parsed = null;
        if (data != null) {
            try {
                if (block != null) {
                    data = Registries.BLOCK.getId(block) + (String)data;
                }
                StringReader reader = new StringReader((String)data);
                BlockArgumentParser.BlockResult arg = BlockArgumentParser.block(Registries.BLOCK.getReadOnlyWrapper(), reader, false);
                Preconditions.checkArgument((!reader.canRead() ? 1 : 0) != 0, (Object)("Spurious trailing data: " + (String)data));
                blockData = arg.blockState();
                parsed = arg.properties();
            }
            catch (CommandSyntaxException ex) {
                throw new IllegalArgumentException("Could not parse data: " + (String)data, ex);
            }
        } else {
            blockData = block.getDefaultState();
        }
        CraftBlockData craft = CraftBlockData.fromData(blockData);
        craft.parsedStates = parsed;
        return craft;
    }

    /*public static CraftBlockData newData_old(Material material, String data) {
        //Preconditions.checkArgument(material == null || material.isBlock(), "Cannot get data for not block %s", material);

        BlockState blockData;
        Block block = BlockImplUtil.MN.IgetBlock(material);

        Map<Property<?>, Comparable<?>> parsed = null;

        // Data provided, use it
        if (data != null) {
            try {
                // Material provided, force that material in
                if (block != null) data = Registry.BLOCK.getId(block) + data;

                StringReader reader = new StringReader(data);
                BlockArgumentParser arg = new BlockArgumentParser(reader, false).parse(false);
                Preconditions.checkArgument(!reader.canRead(), "Spurious trailing data: " + data);

                blockData = arg.getBlockState();
                parsed = arg.getBlockProperties();
            } catch (CommandSyntaxException ex) {
                throw new IllegalArgumentException("Could not parse data: " + data, ex);
            }
        } else blockData = block.getDefaultState();

        CraftBlockData craft = fromData(blockData);
        craft.parsedStates = parsed;
        return craft;
    }*/

    public static CraftBlockData fromData(BlockState data) {
        return MAP.getOrDefault(data.getBlock().getClass(), CraftBlockData::new).apply(data);
    }

    @Override
    public SoundGroup getSoundGroup() {
        return null;
    }

	@Override
	public boolean isFaceSturdy(@NotNull BlockFace arg0, @NotNull BlockSupport arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPreferredTool(@NotNull ItemStack arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRandomlyTicked() {
		// TODO Auto-generated method stub
		return false;
	}

	  @Override
	    public boolean isSupported(org.bukkit.block.Block block) {
	        Preconditions.checkArgument(block != null, "block must not be null");

	        CraftBlock craftBlock = (CraftBlock) block;
	        return state.canPlaceAt(craftBlock.getCraftWorld().getHandle(), craftBlock.getPosition());
	    }

	    @Override
	    public boolean isSupported(Location location) {
	        Preconditions.checkArgument(location != null, "location must not be null");

	        WorldImpl world = (WorldImpl) location.getWorld();
	        Preconditions.checkArgument(world != null, "location must not have a null world");

	        
	        
	        BlockPos position = CraftLocation.toBlockPosition(location);
	        return state.canPlaceAt(world.getHandle(), position);
	    }

}