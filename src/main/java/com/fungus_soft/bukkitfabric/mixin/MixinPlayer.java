package com.fungus_soft.bukkitfabric.mixin;

import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.fungus_soft.bukkitfabric.interfaces.IMixinCommandOutput;
import com.fungus_soft.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.mojang.authlib.GameProfile;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(ServerPlayerEntity.class)
public class MixinPlayer extends MixinEntity implements IMixinCommandOutput, IMixinServerEntityPlayer  {

    private CraftPlayer bukkit;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(MinecraftServer server, ServerWorld world, GameProfile profile, ServerPlayerInteractionManager interactionManager, CallbackInfo ci) {
        this.bukkit = new CraftPlayer((ServerPlayerEntity) (Object) this);
    }

    @Override
    public CommandSender getBukkitSender(ServerCommandSource serverCommandSource) {
        return bukkit;
    }

    @Override
    public CraftPlayer getBukkitEntity() {
        return bukkit;
    }

	@Override
	public void reset() {
		// TODO Bukkit4Fabric: Auto-generated method stub
	}

	@Override
	public BlockPos getSpawnPoint(World world) {
		return ((ServerWorld)world).getSpawnPos();
	}

}