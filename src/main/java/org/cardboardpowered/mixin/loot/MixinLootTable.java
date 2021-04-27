package org.cardboardpowered.mixin.loot;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.world.LootGenerateEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;

@Mixin(LootTable.class)
public class MixinLootTable {

    public void supplyInventory(Inventory iinventory, LootContext loottableinfo) {
        // CraftBukkit start
        this.fillInventory(iinventory, loottableinfo, false);
    }

    public void fillInventory(Inventory iinventory, LootContext loottableinfo, boolean plugin) {
        List<ItemStack> list = this.generateLoot(loottableinfo);
        Random random = loottableinfo.getRandom();
        LootGenerateEvent event = BukkitEventFactory.callLootGenerateEvent(iinventory, (LootTable)(Object)this, loottableinfo, list, plugin);
        if (event.isCancelled()) return;
        list = event.getLoot().stream().map(CraftItemStack::asNMSCopy).collect(Collectors.toList());

        List<Integer> list1 = this.getFreeSlots(iinventory, random);

        this.shuffle(list, list1.size(), random);
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            ItemStack itemstack = (ItemStack) iterator.next();

            if (list1.isEmpty())
                return;
            iinventory.setStack((Integer) list1.remove(list1.size() - 1), itemstack.isEmpty() ? ItemStack.EMPTY : itemstack);
        }

    }

    @Shadow
    public void shuffle(List<ItemStack> list, int i, Random random) {
    }

    @Shadow
    public List<Integer> getFreeSlots(Inventory iinventory, Random random) {
        return null;
    }

    @Shadow
    public List<ItemStack> generateLoot(LootContext loottableinfo) {
        return null;
    }

}