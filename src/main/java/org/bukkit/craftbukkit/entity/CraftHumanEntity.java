package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinScreenHandler;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.Component;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.BlockPos;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.CraftServer;

import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.memory.CraftMemoryMapper;
import org.cardboardpowered.impl.entity.LivingEntityImpl;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.cardboardpowered.impl.inventory.CardboardDoubleChestInventory;
import org.cardboardpowered.impl.inventory.CardboardInventoryView;
import org.cardboardpowered.impl.inventory.CardboardPlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.bukkit.craftbukkit.inventory.CraftContainer;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.cardboardpowered.impl.entity.LivingEntityImpl;
import org.cardboardpowered.impl.inventory.CardboardDoubleChestInventory;
import org.cardboardpowered.impl.inventory.CardboardInventoryView;
import org.cardboardpowered.impl.inventory.CardboardPlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinScreenHandler;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;

import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.Component;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.BlockPos;


public class CraftHumanEntity extends LivingEntityImpl implements HumanEntity {

    private CardboardPlayerInventory inventory;
    protected GameMode gm;
    protected final PermissibleBase perm = new PermissibleBase(this);
    private boolean op;

    public CraftHumanEntity(PlayerEntity entity) {
        super(entity);
        this.nms = entity;
        this.gm = CraftServer.INSTANCE.getDefaultGameMode();
        this.inventory = new CardboardPlayerInventory(entity.inventory);
    }

    @Override
    public PlayerEntity getHandle() {
        return (PlayerEntity) nms;
    }

    public IMixinServerEntityPlayer getInterface() {
        return (IMixinServerEntityPlayer)(ServerPlayerEntity) nms;
    }

    @Override
    public void closeInventory() {
        getInterface().closeHandledScreen();
    }

    @Override
    public boolean discoverRecipe(NamespacedKey arg0) {
        return discoverRecipes(Arrays.asList(arg0)) != 0;
    }

    @Override
    public int discoverRecipes(Collection<NamespacedKey> recipes) {
        return getHandle().unlockRecipes(bukkitKeysToMinecraftRecipes(recipes));
    }

    private Collection<RecipeEntry<?>> bukkitKeysToMinecraftRecipes(Collection<NamespacedKey> recipeKeys) {
        Collection<RecipeEntry<?>> recipes = new ArrayList<>();
        RecipeManager manager = getHandle().getWorld().getServer().getRecipeManager();

        for (NamespacedKey recipeKey : recipeKeys) {
            Optional<RecipeEntry<?>> recipe = manager.get(CraftNamespacedKey.toMinecraft(recipeKey));
	        recipe.ifPresent(recipes::add);
        }

        return recipes;
    }

    @Override
    public float getAttackCooldown() {
        return getHandle().getAttackCooldownProgress(0.5f);
    }

    @Override
    public Location getBedLocation() {
        BlockPos bed = getHandle().getSleepingPosition().get();
        return new Location(getWorld(), bed.getX(), bed.getY(), bed.getZ());
    }

    @Override
    public int getCooldown(Material arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Inventory getEnderChest() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getExpToLevel() {
        return getHandle().getNextLevelExperience();
    }

    @Override
    public GameMode getGameMode() {
        return gm;
    }

    @Override
    public PlayerInventory getInventory() {
        return inventory;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ItemStack getItemInHand() {
        return getInventory().getItemInHand();
    }

    @Override
    public ItemStack getItemOnCursor() {
      return null; // return CraftItemStack.asCraftMirror(getHandle().getInventory().getCursorStack());
    }

    @Override
    public MainHand getMainHand() {
        return nms.getMainArm() == Arm.LEFT ? MainHand.LEFT : MainHand.RIGHT;
    }

    @Override
    public InventoryView getOpenInventory() {
        return ((IMixinScreenHandler)getHandle().currentScreenHandler).getBukkitView();
    }

    @Override
    public org.bukkit.entity.Entity getShoulderEntityLeft() {
        if (!getHandle().getShoulderEntityLeft().isEmpty()) {
            Optional<net.minecraft.entity.Entity> shoulder = net.minecraft.entity.EntityType.getEntityFromNbt(getHandle().getShoulderEntityLeft(), getHandle().getWorld());
            return (!shoulder.isPresent()) ? null : ((IMixinEntity)shoulder.get()).getBukkitEntity();
        }
        return null;
    }

    @Override
    public org.bukkit.entity.Entity getShoulderEntityRight() {
        if (!getHandle().getShoulderEntityRight().isEmpty()) {
            Optional<net.minecraft.entity.Entity> shoulder = net.minecraft.entity.EntityType.getEntityFromNbt(getHandle().getShoulderEntityRight(), getHandle().getWorld());
            return (!shoulder.isPresent()) ? null : ((IMixinEntity)shoulder.get()).getBukkitEntity();
        }
        return null;
    }

    @Override
    public int getSleepTicks() {
        return getHandle().sleepTimer;
    }

    @Override
    public boolean hasCooldown(Material arg0) {
        return getHandle().getItemCooldownManager().isCoolingDown(CraftMagicNumbers.getItem(arg0));
    }

    @Override
    public boolean isBlocking() {
        return getHandle().isBlocking();
    }

    @Override
    public boolean isHandRaised() {
        return getHandle().isUsingItem();
    }

    @Override
    public InventoryView openEnchanting(Location location, boolean force) {
        if (!force) {
            Block block = location.getBlock();
            if (block.getType() != Material.ENCHANTING_TABLE)
                return null;
        }
        if (location == null)
            location = getLocation();

        // If there isn't an enchant table we can force create one, won't be very useful though.
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        getHandle().openHandledScreen(((EnchantingTableBlock) Blocks.ENCHANTING_TABLE).createScreenHandlerFactory(null, getHandle().getWorld(), pos));

        if (force)
            ((IMixinScreenHandler)getHandle().currentScreenHandler).setCheckReachable(false);
        return ((IMixinScreenHandler)getHandle().currentScreenHandler).getBukkitView();
    }


    @Override
    public InventoryView openInventory(Inventory inventory) {
        if (!(getHandle() instanceof ServerPlayerEntity)) return null;
        ServerPlayerEntity player = (ServerPlayerEntity) getHandle();
        ScreenHandler formerContainer = getHandle().currentScreenHandler;

        NamedScreenHandlerFactory iinventory = null;
        if (inventory instanceof CardboardDoubleChestInventory) {
            iinventory = ((CardboardDoubleChestInventory) inventory).tile;
        } else if (inventory instanceof CraftInventory) {
            CraftInventory craft = (CraftInventory) inventory;
            if (craft.getInventory() instanceof NamedScreenHandlerFactory)
                iinventory = (NamedScreenHandlerFactory) craft.getInventory();
        }

        if (iinventory instanceof NamedScreenHandlerFactory) {
            if (iinventory instanceof BlockEntity) {
                BlockEntity te = (BlockEntity) iinventory;
                if (!te.hasWorld()) {
                   // te.setLocation(getHandle().world, getHandle().getBlockPos());
                    te.setWorld(getHandle().getWorld());
                    te.pos = getHandle().getBlockPos();
                }
            }
        }

        ScreenHandlerType<?> container = CraftContainer.getNotchInventoryType(inventory);
        if (iinventory instanceof LockableContainerBlockEntity) {
            getHandle().openHandledScreen(iinventory);
        } else openCustomInventory(inventory, player, container);
        if (getHandle().currentScreenHandler == formerContainer)
            return null;

        ((IMixinScreenHandler)getHandle().currentScreenHandler).setCheckReachable(false);
        return ((IMixinScreenHandler)getHandle().currentScreenHandler).getBukkitView();
    }

    public void openCustomInventory(Inventory inventory, ServerPlayerEntity player, ScreenHandlerType<?> windowType) {
        if (player.networkHandler == null) return;
        Preconditions.checkArgument(windowType != null, "Unknown windowType");
        ScreenHandler container = new CraftContainer(inventory, this.getHandle(), ((IMixinServerEntityPlayer)player).nextContainerCounter());

        container = BukkitEventFactory.callInventoryOpenEvent(player, container);
        if (container == null) return;

        String title = ((IMixinScreenHandler)container).getBukkitView().getTitle();

        player.networkHandler.sendPacket(new OpenScreenS2CPacket(container.syncId, windowType, CraftChatMessage.fromString(title)[0]));
        getHandle().currentScreenHandler = container;

        if ( CraftServer.INSTANCE.getMinecraftVersion().contains("1.16") ) {
            // 1.16.5
            container.addListener((ScreenHandlerListener) ((ServerPlayerEntity)(Object)this));
        } else {
            // 1.17+
            ((ServerPlayerEntity)getHandle()).onScreenHandlerOpened(container);
        }
    }

    @Override
    public void openInventory(InventoryView inventory) {
        if (!(getHandle() instanceof ServerPlayerEntity)) return;
        if (((ServerPlayerEntity) getHandle()).networkHandler == null) return;
        if (getHandle().currentScreenHandler != getHandle().playerScreenHandler) {
            // fire INVENTORY_CLOSE if one already open
            ((ServerPlayerEntity) getHandle()).networkHandler.onCloseHandledScreen(new CloseHandledScreenC2SPacket(getHandle().currentScreenHandler.syncId));
        }
        ServerPlayerEntity player = (ServerPlayerEntity) getHandle();
        ScreenHandler container;
        if (inventory instanceof CardboardInventoryView) {
            container = ((CardboardInventoryView) inventory).getHandle();
        } else container = new CraftContainer(inventory, this.getHandle(), ((IMixinServerEntityPlayer)player).nextContainerCounter());

        // Trigger an INVENTORY_OPEN event
        container = BukkitEventFactory.callInventoryOpenEvent(player, container);
        if (container == null)
            return;

        // Now open the window
        ScreenHandlerType<?> windowType = CraftContainer.getNotchInventoryType(inventory.getTopInventory());
        String title = inventory.getTitle();
        player.networkHandler.sendPacket(new OpenScreenS2CPacket(container.syncId, windowType, CraftChatMessage.fromString(title)[0]));
        player.currentScreenHandler = container;
        if ( CraftServer.INSTANCE.getMinecraftVersion().contains("1.16") ) {
            // 1.16.5
            container.addListener((ScreenHandlerListener) ((ServerPlayerEntity)(Object)this));
        } else {
            // 1.17+
            ((ServerPlayerEntity)getHandle()).onScreenHandlerOpened(container);
        }
    }

    @Override
    public InventoryView openMerchant(Villager villager, boolean force) {
        Preconditions.checkNotNull(villager, "villager cannot be null");
        return this.openMerchant((Merchant) villager, force);
    }

    @Override
    public InventoryView openMerchant(Merchant arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InventoryView openWorkbench(Location location, boolean force) {
        if (!force) {
            Block block = location.getBlock();
            if (block.getType() != Material.CRAFTING_TABLE)
                return null;
        }
        if (location == null)
            location = getLocation();
        getHandle().openHandledScreen(((CraftingTableBlock) Blocks.CRAFTING_TABLE).createScreenHandlerFactory(null, getHandle().getWorld(), new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ())));
        if (force)
            ((IMixinScreenHandler)getHandle().currentScreenHandler).setCheckReachable(false);
        return ((IMixinScreenHandler)getHandle().currentScreenHandler).getBukkitView();
    }

    @Override
    public void setCooldown(Material arg0, int arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setGameMode(GameMode arg0) {
        this.gm = arg0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setItemInHand(ItemStack arg0) {
        getInventory().setItemInHand(arg0);
    }

    @Override
    public void setItemOnCursor(ItemStack item) {
        net.minecraft.item.ItemStack stack = CraftItemStack.asNMSCopy(item);
      //  getHandle().getInventory().setCursorStack(stack);
      //  if (this instanceof PlayerImpl)
     //       ((ServerPlayerEntity) getHandle()).updateCursorStack();
    }

    @Override
    public void setShoulderEntityLeft(Entity entity) {
       // getHandle().setShoulderEntityLeft(entity == null ? new NbtCompound() : ((CraftEntity) entity).save());
        if (entity != null) entity.remove();
    }

    @Override
    public void setShoulderEntityRight(Entity entity) {
      //  getHandle().setShoulderEntityRight(entity == null ? new NbtCompound() : ((CraftEntity) entity).save());
        if (entity != null) entity.remove();
    }

    @Override
    public boolean setWindowProperty(Property arg0, int arg1) {
        return false;
    }

    @Override
    public boolean sleep(Location location, boolean force) {
        BlockPos blockposition = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        BlockState iblockdata = getHandle().getWorld().getBlockState(blockposition);
        if (!(iblockdata.getBlock() instanceof BedBlock))
            return false;

        if (getHandle().trySleep(blockposition).left().isPresent())
            return false;

        // From BlockBed
        iblockdata = (BlockState) iblockdata.with(BedBlock.OCCUPIED, true);
        getHandle().getWorld().setBlockState(blockposition, iblockdata, 4);

        return true;
    }

    @Override
    public boolean undiscoverRecipe(NamespacedKey recipe) {
        return undiscoverRecipes(Arrays.asList(recipe)) != 0;
    }

    @Override
    public int undiscoverRecipes(Collection<NamespacedKey> recipes) {
        return getHandle().lockRecipes(bukkitKeysToMinecraftRecipes(recipes));
    }

    @Override
    public void wakeup(boolean arg0) {
        getHandle().wakeUp(true, arg0);
    }

    @Override
    public boolean isOp() {
        return op;
    }

    @Override
    public boolean isPermissionSet(String name) {
        return perm.isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        return this.perm.isPermissionSet(perm);
    }

    @Override
    public boolean hasPermission(String name) {
        boolean b = perm.hasPermission(name);
        if (!b && FabricLoader.getInstance().isModLoaded("cyber-permissions")) {
            if (nms instanceof ServerPlayerEntity)
                return org.cardboardpowered.impl.hooks.PermissionHook.hasPermission((ServerPlayerEntity) nms, name);
        }
        return b;
    }

    @Override
    public boolean hasPermission(Permission perm) {
        boolean b = this.perm.hasPermission(perm);
        if (!b && FabricLoader.getInstance().isModLoaded("cyber-permissions")) {
            if (nms instanceof ServerPlayerEntity)
                return org.cardboardpowered.impl.hooks.PermissionHook.hasPermission((ServerPlayerEntity) nms, perm.getName());
        }
        return b;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        return perm.addAttachment(plugin, name, value);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return perm.addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        return perm.addAttachment(plugin, name, value, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        return perm.addAttachment(plugin, ticks);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        perm.removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        perm.recalculatePermissions();
    }

    @Override
    public void setOp(boolean value) {
        this.op = value;
        perm.recalculatePermissions();
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return perm.getEffectivePermissions();
    }

    @Override
    public Set<NamespacedKey> getDiscoveredRecipes() {
        return ImmutableSet.of();
    }

    @Override
    public boolean hasDiscoveredRecipe(NamespacedKey arg0) {
        return false;
    }

    @Override
    public boolean dropItem(boolean dropAll) {
        return ((ServerPlayerEntity)getHandle()).dropSelectedItem(dropAll);
    }

    @Override
    public EntityType getType() {
        return EntityType.PLAYER;
    }

    @Override
    public void closeInventory(Reason arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public Location getPotentialBedLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InventoryView openAnvil(Location arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InventoryView openCartographyTable(Location arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InventoryView openGrindstone(Location arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InventoryView openLoom(Location arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void openSign(Sign arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public InventoryView openSmithingTable(Location arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InventoryView openStonecutter(Location arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Entity releaseLeftShoulderEntity() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Entity releaseRightShoulderEntity() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @NotNull EquipmentSlot getHandRaised() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasLineOfSight(@NotNull Location arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void registerAttribute(@NotNull Attribute arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public @Nullable Component customName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void customName(@Nullable Component arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public float getExhaustion() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getFoodLevel() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getSaturatedRegenRate() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getSaturation() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getStarvationRate() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getUnsaturatedRegenRate() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isDeeplySleeping() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setExhaustion(float arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setFoodLevel(int arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setSaturatedRegenRate(int arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setSaturation(float arg0) {
        this.getHandle().getHungerManager().setSaturationLevel(arg0);
    }

    @Override
    public void setStarvationRate(int arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setUnsaturatedRegenRate(int arg0) {
        // TODO Auto-generated method stub
    }
    
    // 1.17 API Start

    @Override
    public @Nullable ItemStack getItemInUse() {
        return CraftItemStack.asBukkitCopy(nms.getActiveItem());
    }

	@Override
    public Firework fireworkBoost(ItemStack fireworkItemStack) {
        Preconditions.checkArgument((fireworkItemStack != null ? 1 : 0) != 0, (Object)"fireworkItemStack must not be null");
        Preconditions.checkArgument((fireworkItemStack.getType() == Material.FIREWORK_ROCKET ? 1 : 0) != 0, (String)"fireworkItemStack must be of type %s", (Object)Material.FIREWORK_ROCKET);
        FireworkRocketEntity fireworks = new FireworkRocketEntity(this.getHandle().getWorld(), CraftItemStack.asNMSCopy(fireworkItemStack), this.getHandle());
        boolean success = this.getHandle().getWorld().spawnEntity(fireworks);
        return success ? (Firework)(((IMixinEntity) fireworks).getBukkitEntity()) : null;
    }

	@Override
    public FishHook getFishHook() {
        if (this.getHandle().fishHook == null) {
            return null;
        }
        return (FishHook)((IMixinEntity)this.getHandle().fishHook).getBukkitEntity();
    }

	@Override
	public @Nullable Location getLastDeathLocation() {
		// TODO Auto-generated method stub
        return this.getHandle().getLastDeathPos().map(CraftMemoryMapper::fromNms).orElse(null);

	}

	@Override
    public void setLastDeathLocation(Location location) {
        if (location == null) {
            this.getHandle().setLastDeathPos(Optional.empty());
        } else {
           //  this.getHandle().setLastDeathPos(Optional.of(CraftMemoryMapper.toNms(location)));
        }
    }

	@Override
	public int getEnchantmentSeed() {
        return this.getHandle().getEnchantmentTableSeed();
	}

	@Override
	public void setEnchantmentSeed(int i2) {
        this.getHandle().enchantmentTableSeed = i2;
	}

}

