package org.bukkit.craftbukkit.block;

import net.minecraft.world.level.block.entity.CreakingHeartBlockEntity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.CreakingHeart;

public class CraftCreakingHeart extends CraftBlockEntityState<CreakingHeartBlockEntity> implements CreakingHeart {

    public CraftCreakingHeart(World world, CreakingHeartBlockEntity blockEntity) {
        super(world, blockEntity);
    }

    protected CraftCreakingHeart(CraftCreakingHeart state, Location location) {
        super(state, location);
    }

    @Override
    public CraftCreakingHeart copy() {
        return new CraftCreakingHeart(this, null);
    }

    @Override
    public CraftCreakingHeart copy(Location location) {
        return new CraftCreakingHeart(this, location);
    }

    // 26.2: new on CreakingHeart
    @Override
    public org.bukkit.Location spreadResin() {
        // Paper implements resin spreading inside its patched server; there is no
        // equivalent hook on the vanilla block entity to delegate to.
        throw new UnsupportedOperationException("spreadResin is not supported on Cardboard");
    }


    // 26.2: CreakingHeart gained creaking accessors
    @Override
    public org.bukkit.entity.Creaking getCreaking() {
        final com.mojang.datafixers.util.Either<net.minecraft.world.entity.monster.creaking.Creaking, java.util.UUID>
                info = this.getSnapshot().creakingInfo;
        if (info == null) return null;
        return info.left()
                .map(c -> (org.bukkit.entity.Creaking) c.getBukkitEntity())
                .orElse(null);
    }

    @Override
    public void setCreaking(org.bukkit.entity.Creaking creaking) {
        if (creaking == null) {
            this.getSnapshot().creakingInfo = null;
            return;
        }
        this.getSnapshot().setCreakingInfo(
                (net.minecraft.world.entity.monster.creaking.Creaking)
                        ((org.bukkit.craftbukkit.entity.CraftEntity) creaking).getHandle());
    }

    @Override
    public org.bukkit.entity.Creaking spawnCreaking() {
        // Paper spawns the creaking from its patched block-entity tick; not reachable here.
        throw new UnsupportedOperationException("spawnCreaking is not supported on Cardboard");
    }

}
