/**
 * CardboardPowered - Bukkit/Spigot for Fabric
 * Copyright (C) CardboardPowered.org and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.mixin.client;

import java.io.File;
import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.java.JavaPluginLoader;
import io.papermc.paper.plugin.PluginInitializerManager;
import io.papermc.paper.plugin.entrypoint.LaunchEntryPointHandler;
import org.cardboardpowered.CardboardLoadHolder;
import org.cardboardpowered.CardboardMod;
import org.cardboardpowered.bridge.server.MinecraftServerBridge;
import org.cardboardpowered.impl.util.CardboardMagicNumbers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;

/**
 * Singleplayer/LAN counterpart of DedicatedServerMixin: boots the Bukkit layer on the
 * integrated server, and tears it down on world close so the next world can boot it again.
 */
@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin {

	@Unique
	private static final Logger cardboard$LOGGER = LoggerFactory.getLogger("Cardboard|Integrated");

	@Unique
	private static boolean cardboard$materialsSetUp;

	@Unique
	private static boolean cardboard$bootedBefore;

	// The client constructs its integrated server on the render thread.
	@Inject(at = @At("TAIL"), method = "<init>")
	private void cardboard$rememberClientThread(CallbackInfo ci) {
		CraftServer.clientThread = Thread.currentThread();
	}

	@Inject(at = @At("HEAD"), method = "initServer()Z")
	private void cardboard$captureLoadContext(CallbackInfoReturnable<Boolean> cir) {
		((MinecraftServerBridge) (Object) this).cardboard$worldLoaderContext(CardboardLoadHolder.worldLoader.get());
		CraftServer.server = (MinecraftServer) (Object) this;
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/server/IntegratedServer;loadLevel()V"), method = "initServer()Z")
	private void cardboard$bootBukkit(CallbackInfoReturnable<Boolean> cir) {
		// Appends Material enum constants, so only once per JVM even though worlds open repeatedly.
		if (!cardboard$materialsSetUp) {
			cardboard$materialsSetUp = true;
			CardboardMagicNumbers.test();
			CardboardMagicNumbers.setupUnknownModdedMaterials();
		}

		// Plugin jars were opened by discovery at game start and closed when the last world shut down.
		if (cardboard$bootedBefore) {
			LaunchEntryPointHandler.INSTANCE.populateProviderStorage();
			try {
				PluginInitializerManager.load(CardboardMod.options);
			} catch (Exception e) {
				cardboard$LOGGER.error("Could not rediscover plugins for this world", e);
			}
		}
		cardboard$bootedBefore = true;

		MinecraftServer self = (MinecraftServer) (Object) this;
		Bukkit.setServer(new CraftServer(self));
		org.spigotmc.SpigotConfig.init(new File("spigot.yml"));

		Bukkit.getLogger().info("Loading Bukkit plugins...");
		new File("plugins").mkdir();
		Bukkit.getPluginManager().registerInterface(JavaPluginLoader.class);

		CraftServer s = (CraftServer) Bukkit.getServer();
		s.loadPlugins();
		s.enablePlugins(PluginLoadOrder.STARTUP);
	}

	// MinecraftServerMixin.doStop has already disabled plugins by the time super.stopServer() returns.
	@Inject(at = @At("TAIL"), method = "stopServer")
	private void cardboard$resetBukkit(CallbackInfo ci) {
		CraftServer.INSTANCE = null;
		CraftServer.console = null;
		CraftServer.server = null;
		try {
			// Bukkit.setServer refuses to replace a non-null singleton, and Paper API has no reset.
			Field field = Bukkit.class.getDeclaredField("server");
			field.setAccessible(true);
			field.set(null, null);
		} catch (ReflectiveOperationException e) {
			cardboard$LOGGER.error("Could not reset the Bukkit server singleton; plugins will not load in the next world", e);
		}
	}

}
