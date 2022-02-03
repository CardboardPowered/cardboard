package org.cardboardpowered.mixin.item;

import org.bukkit.Bukkit;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;

@MixinInfo(events = {"SheepDyeWoolEvent"})
@Mixin(value = DyeItem.class, priority = 900)
public class MixinDyeItem {

    @Shadow
    public DyeColor color;

    /**
     * @reason .
     * @author .
     */
    @SuppressWarnings("deprecation")
    @Overwrite
    public ActionResult useOnEntity(ItemStack itemstack, PlayerEntity entityhuman, LivingEntity entityliving, Hand enumhand) {
        if (!(entityliving instanceof SheepEntity)) return ActionResult.PASS;

        SheepEntity entitysheep = (SheepEntity) entityliving;
        if (entitysheep.isAlive() && !entitysheep.isSheared() && entitysheep.getColor() != this.color) {
            if (!entityhuman.world.isClient) {
                byte bColor = (byte) this.color.getId();
                SheepDyeWoolEvent event = new SheepDyeWoolEvent((org.bukkit.entity.Sheep) ((IMixinEntity)entitysheep).getBukkitEntity(), org.bukkit.DyeColor.getByWoolData(bColor));
                Bukkit.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) return ActionResult.PASS;

                entitysheep.setColor(DyeColor.byId((byte) event.getColor().getWoolData()));
                itemstack.decrement(1);
            }
            return ActionResult.success(entityhuman.world.isClient);
        }
        return ActionResult.PASS;
    }

}
