package thingxII.pokemonswarming;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(PokemonSwarming.MODID)
@Mod.EventBusSubscriber(modid = PokemonSwarming.MODID)
public class PokemonSwarming {

	private static SwarmCoordinator swarmCoordinator;

	public static final Logger SWARM_LOGGER = LogManager.getLogger();
	// Define mod id in a common place for everything to reference
	public static final String MODID = "pokemonswarming";

	public PokemonSwarming() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);

		modEventBus.register(SwarmCommandRegistry.class);

		// Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

	}

	@SubscribeEvent
	public static void onServerStarted(FMLServerStartedEvent event) {
		swarmCoordinator = new SwarmCoordinator();

		CommandDispatcher<CommandSource> dispatcher = event.getServer().getCommands().getDispatcher();
		SwarmCommandRegistry.SwarmCommand.register(dispatcher);

		MinecraftForge.EVENT_BUS.register(swarmCoordinator);
	}
}
