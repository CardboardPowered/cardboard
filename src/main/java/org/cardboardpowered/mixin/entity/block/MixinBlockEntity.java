package org.cardboardpowered.mixin.entity.block;

import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.javazilla.bukkitfabric.interfaces.IMixinBlockEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(BlockEntity.class)
public class MixinBlockEntity implements IMixinBlockEntity {

    private static final CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY = new CraftPersistentDataTypeRegistry();
    public CraftPersistentDataContainer persistentDataContainer;

    @Shadow public World world;
    @Shadow public BlockPos pos;

    @Override
    public CraftPersistentDataContainer getPersistentDataContainer() {
        return persistentDataContainer;
    }

    @Inject(at = @At("RETURN"), method = "writeNbt")
    public void saveEnd(NbtCompound tag, @SuppressWarnings("rawtypes") CallbackInfoReturnable callback) {
        if (this.persistentDataContainer != null && !this.persistentDataContainer.isEmpty())
            tag.put("PublicBukkitValues", this.persistentDataContainer.toTagCompound());
    }

    @Override
    public InventoryHolder getOwner_() {
        return getOwner(true);
    }

    public InventoryHolder getOwner(boolean useSnapshot) {
        if (world == null) return null;

        org.bukkit.block.Block block = ((IMixinWorld)this.world).getWorldImpl().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        if (block == null) {
            org.bukkit.Bukkit.getLogger().warning("No block for owner at " + world + ", pos: " + pos);
            return null;
        }
        org.bukkit.block.BlockState state = block.getState(useSnapshot); // Paper: useSnapshot
        if (state instanceof InventoryHolder) return (InventoryHolder) state;
        System.out.println("STATE NOT INSTANCEOF INVENTORYHOLDER!!");
        return null;
    }

    @Override
    public void setCardboardPersistentDataContainer(CraftPersistentDataContainer c) {
        this.persistentDataContainer = c;
    }

    @Override
    public CraftPersistentDataTypeRegistry getCardboardDTR() {
        return DATA_TYPE_REGISTRY;
    }

}