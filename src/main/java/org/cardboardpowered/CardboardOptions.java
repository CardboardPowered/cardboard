package org.cardboardpowered;

import static java.util.Arrays.asList;

import java.io.File;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.util.PathConverter;

/**
 * Command line options accepted by the server, mirroring CraftBukkit's {@code Main}.
 *
 * <p>This lives outside of the mixin on purpose: anonymous classes declared inside a mixin get
 * relocated into the target class as {@code Main$Anonymous$<hash>}, which makes Mixin log a
 * "Error loading class" warning while resolving the class hierarchy.
 *
 * @implSpec https://github.com/PaperMC/Paper/blob/main/paper-server/patches/sources/net/minecraft/server/Main.java.patch
 */
public final class CardboardOptions {

	private CardboardOptions() {
	}

	public static OptionSet parse(String[] args) {
		return createParser().parse(args);
	}

	public static OptionParser createParser() {
		OptionParser parser = new OptionParser();

		parser.acceptsAll(asList("?", "help"), "Show the help");

		parser.acceptsAll(asList("c", "config"), "Properties file to use")
				.withRequiredArg()
				.ofType(File.class)
				.defaultsTo(new File("server.properties"))
				.describedAs("Properties file");

		parser.acceptsAll(asList("P", "plugins"), "Plugin directory to use")
				.withRequiredArg()
				.ofType(File.class)
				.defaultsTo(new File("plugins"))
				.describedAs("Plugin directory");

		parser.acceptsAll(asList("h", "host", "server-ip"), "Host to listen on")
				.withRequiredArg()
				.ofType(String.class)
				.describedAs("Hostname or IP");

		parser.acceptsAll(asList("W", "world-dir", "universe", "world-container"), "World container")
				.withRequiredArg()
				.ofType(File.class)
				.defaultsTo(new File("."))
				.describedAs("Directory containing worlds");

		parser.acceptsAll(asList("w", "world", "level-name"), "World name")
				.withRequiredArg()
				.ofType(String.class)
				.describedAs("World name");

		parser.acceptsAll(asList("p", "port", "server-port"), "Port to listen on")
				.withRequiredArg()
				.ofType(Integer.class)
				.describedAs("Port");

		parser.accepts("serverId", "Server ID")
				.withRequiredArg();

		parser.accepts("jfrProfile", "Enable JFR profiling");

		parser.accepts("pidFile", "pid File")
				.withRequiredArg()
				.withValuesConvertedBy(new PathConverter());

		parser.acceptsAll(asList("o", "online-mode"), "Whether to use online authentication")
				.withRequiredArg()
				.ofType(Boolean.class)
				.describedAs("Authentication");

		parser.acceptsAll(asList("s", "size", "max-players"), "Maximum amount of players")
				.withRequiredArg()
				.ofType(Integer.class)
				.describedAs("Server size");

		parser.acceptsAll(asList("b", "bukkit-settings"), "File for bukkit settings")
				.withRequiredArg()
				.ofType(File.class)
				.defaultsTo(new File("bukkit.yml"))
				.describedAs("Yml file");

		parser.acceptsAll(asList("C", "commands-settings"), "File for command settings")
				.withRequiredArg()
				.ofType(File.class)
				.defaultsTo(new File("commands.yml"))
				.describedAs("Yml file");

		parser.accepts("forceUpgrade", "Whether to force a world upgrade");
		parser.accepts("eraseCache", "Whether to force cache erase during world upgrade");
		parser.accepts("recreateRegionFiles", "Whether to recreate region files during world upgrade");
		parser.accepts("safeMode", "Loads level with vanilla datapack only"); // Paper
		parser.accepts("nogui", "Disables the graphical console");

		parser.accepts("nojline", "Disables jline and emulates the vanilla console");

		parser.accepts("noconsole", "Disables the console");

		parser.acceptsAll(asList("v", "version"), "Show the CraftBukkit Version");

		parser.accepts("demo", "Demo mode");

		parser.accepts("bonusChest", "Enable the bonus chest");

		parser.accepts("initSettings", "Only create configuration files and then exit"); // SPIGOT-5761: Add initSettings option

		parser.acceptsAll(asList("S", "spigot-settings"), "File for spigot settings")
				.withRequiredArg()
				.ofType(File.class)
				.defaultsTo(new File("spigot.yml"))
				.describedAs("Yml file");

		parser.acceptsAll(asList("paper-dir", "paper-settings-directory"), "Directory for Paper settings")
				.withRequiredArg()
				.ofType(File.class)
				.defaultsTo(new File(/*io.papermc.paper.configuration.PaperConfigurations.CONFIG_DIR*/ "config"))
				.describedAs("Config directory");

		parser.acceptsAll(asList("paper", "paper-settings"), "File for Paper settings")
				.withRequiredArg()
				.ofType(File.class)
				.defaultsTo(new File("paper.yml"))
				.describedAs("Yml file");

		parser.acceptsAll(asList("add-plugin", "add-extra-plugin-jar"), "Specify paths to extra plugin jars to be loaded in addition to those in the plugins folder. This argument can be specified multiple times, once for each extra plugin jar path.")
				.withRequiredArg()
				.ofType(File.class)
				.defaultsTo(new File[] {})
				.describedAs("Jar file");

		parser.acceptsAll(asList("add-plugin-dir", "add-extra-plugin-dir"), "Specify paths to extra plugin directories to be loaded in addition to the plugins folder. This argument can be specified multiple times, once for each extra plugin dir path.")
				.withRequiredArg()
				.ofType(File.class)
				.defaultsTo(new File[] {})
				.describedAs("Plugin directory");

		parser.accepts("server-name", "Name of the server")
				.withRequiredArg()
				.ofType(String.class)
				.defaultsTo("Unknown Server")
				.describedAs("Name");

		return parser;
	}
}
