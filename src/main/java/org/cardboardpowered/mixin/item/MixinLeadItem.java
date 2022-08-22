package org.cardboardpowered.mixin.item;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.LeadItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Iterator;
import java.util.List;

@MixinInfo(events = {"HangingPlaceEvent"})
@Mixin(value = LeadItem.class, priority = 900)
public class MixinLeadItem extends Item {

    public MixinLeadItem(Settings settings) {
        super(settings);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public static ActionResult attachHeldMobsToBlock(PlayerEntity player, World world, BlockPos pos) {
        LeashKnotEntity leashKnotEntity = null;
        boolean bl = false;
        double d = 7.0;
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        List<MobEntity> list = world.getNonSpectatingEntities(MobEntity.class, new Box((double)i - 7.0, (double)j - 7.0, (double)k - 7.0, (double)i + 7.0, (double)j + 7.0, (double)k + 7.0));
        Iterator var11 = list.iterator();

        while(var11.hasNext()) {
            MobEntity mobEntity = (MobEntity)var11.next();
            if (mobEntity.getHoldingEntity() == player) {
                if (leashKnotEntity == null) {
                    leashKnotEntity = LeashKnotEntity.getOrCreate(world, pos);

                    HangingPlaceEvent event = new HangingPlaceEvent((Hanging) ((IMixinEntity) leashKnotEntity).getBukkitEntity(), player != null ? (Player) ((IMixinServerEntityPlayer) player).getBukkit() : null, CraftBlock.at((ServerWorld) world, pos), BlockFace.SELF);
                    Bukkit.getPluginManager().callEvent(event);

                    if (event.isCancelled()) {
                        leashKnotEntity.discard();
                        return ActionResult.PASS;
                    }
                    leashKnotEntity.onPlace();
                }
                if (BukkitEventFactory.callPlayerLeashEntityEvent(mobEntity, leashKnotEntity, player).isCancelled()) {
                    continue;
                }
                mobEntity.attachLeash(leashKnotEntity, true);
                bl = true;
            }
        }

        return bl ? ActionResult.SUCCESS : ActionResult.PASS;
    }
}
