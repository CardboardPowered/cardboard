/**
 * Cardboard - Spigot/Paper for Fabric.
 * Copyright (C) 2020-2021 Cardboard contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.bridge.server.level;

import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition.PostTeleportTransition;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.cardboardpowered.extras.ServerPlayer_RespawnPosAngle;
import org.cardboardpowered.extras.ServerPlayer_RespawnResult;

import org.cardboardpowered.bridge.world.entity.EntityBridge;

import java.util.Optional;

public interface ServerPlayerBridge extends EntityBridge {

    void reset();

    BlockPos getSpawnPoint(Level world);

    void closeHandledScreen();

    int cardboard$nextContainerCounter();

    void setConnectionBF(Connection connection);

    Connection getConnectionBF();

	void spawnIn(ServerLevel worldserver1);

	void copyFrom_unused(ServerPlayer entityplayer, boolean flag);

	void spigot$forceSetPositionRotation(double x, double y, double z, float yaw, float pitch);

    @org.jspecify.annotations.Nullable ServerPlayer_RespawnResult cardboard$findRespawnPositionAndUseSpawnBlock0(boolean useCharge, PostTeleportTransition postTeleportTransition, PlayerRespawnEvent.RespawnReason respawnReason);

    boolean cardboard$drop(boolean dropStack);

    boolean cardboard$setRespawnPosition(ServerPlayer.@org.jspecify.annotations.Nullable RespawnConfig respawnConfig, boolean displayInChat, com.destroystokyo.paper.event.player.PlayerSetSpawnEvent.Cause cause);

    static Optional<ServerPlayer_RespawnPosAngle> cardboard$findRespawnAndUseSpawnBlock(
            ServerLevel level, ServerPlayer.RespawnConfig respawnConfig, boolean useCharge
    ) {
        LevelData.RespawnData respawnData = respawnConfig.respawnData();
        BlockPos blockPos = respawnData.pos();
        float yaw = respawnData.yaw();
        float pitch = respawnData.pitch();
        boolean flag = respawnConfig.forced();
        BlockState blockState = level.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block instanceof RespawnAnchorBlock
                && (flag || blockState.getValue(RespawnAnchorBlock.CHARGE) > 0)
                && RespawnAnchorBlock.canSetSpawn(level, blockPos)) {
            Optional<Vec3> optional = RespawnAnchorBlock.findStandUpPosition(EntityTypes.PLAYER, level, blockPos);
            Runnable consumeAnchorCharge = null; // Paper - Fix SPIGOT-5989 (don't use charge until after respawn event)
            if (!flag && useCharge && optional.isPresent()) {
                consumeAnchorCharge = () -> level.setBlock(blockPos, blockState.setValue(RespawnAnchorBlock.CHARGE, blockState.getValue(RespawnAnchorBlock.CHARGE) - 1), Block.UPDATE_ALL); // Paper - Fix SPIGOT-5989 (don't use charge until after respawn event)
            }
            final Runnable finalConsumeAnchorCharge = consumeAnchorCharge; // Paper - Fix SPIGOT-5989

            return optional.map(pos -> ServerPlayer_RespawnPosAngle.of(pos, blockPos, 0.0F, false, true, finalConsumeAnchorCharge)); // Paper - Fix SPIGOT-5989 (don't use charge until after respawn event)
        } else if (block instanceof BedBlock && level.environmentAttributes().getValue(EnvironmentAttributes.BED_RULE, blockPos).canSetSpawn(level)) {
            return BedBlock.findStandUpPosition(EntityTypes.PLAYER, level, blockPos, blockState.getValue(BedBlock.FACING), yaw)
                    .map(pos -> ServerPlayer_RespawnPosAngle.of(pos, blockPos, 0.0F, true, false, null)); // Paper - Fix SPIGOT-5989
        } else if (!flag) {
            return Optional.empty();
        } else {
            boolean isPossibleToRespawnInThis = block.isPossibleToRespawnInThis(blockState);
            BlockState blockState1 = level.getBlockState(blockPos.above());
            boolean isPossibleToRespawnInThis1 = blockState1.getBlock().isPossibleToRespawnInThis(blockState1);
            return isPossibleToRespawnInThis && isPossibleToRespawnInThis1
                    ? Optional.of(new ServerPlayer_RespawnPosAngle(new Vec3(blockPos.getX() + 0.5, blockPos.getY() + 0.1, blockPos.getZ() + 0.5), yaw, pitch, false, false, null)) // Paper - Fix SPIGOT-5989
                    : Optional.empty();
        }
    }
}