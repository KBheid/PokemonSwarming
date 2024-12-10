package thingxII.pokemonswarming;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import java.util.Arrays;
import java.util.List;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = PokemonSwarming.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

	private static final ForgeConfigSpec.IntValue SWARM_DURATION;

	private static final ForgeConfigSpec.DoubleValue RARITY_MULTIPLIER;
	private static final ForgeConfigSpec.DoubleValue POKERUS_MULTIPLIER;
	private static final ForgeConfigSpec.DoubleValue POKERUS_DEFAULT;
	private static final ForgeConfigSpec.DoubleValue SHINY_MULTIPLIER;
	private static final ForgeConfigSpec.DoubleValue SHINY_DEFAULT;

	private static final ForgeConfigSpec.BooleanValue USE_PERCENTAGE;
	private static final ForgeConfigSpec.DoubleValue SPAWN_PERCENTAGE;

	private static final ForgeConfigSpec.DoubleValue CHANCE_TO_IGNORE_TIME;
	private static final ForgeConfigSpec.DoubleValue CHANCE_TO_IGNORE_WEATHER;

	private static final ForgeConfigSpec.ConfigValue<List<? extends String>> IGNORED_PREFIXES;
	private static final ForgeConfigSpec.ConfigValue<List<? extends String>> IGNORED_BIOMES;

	private static final ForgeConfigSpec.BooleanValue SEND_MESSAGE_IN_CONSOLE;
	private static final ForgeConfigSpec.BooleanValue SEND_MESSAGE_TO_PLAYERS;

	static {
		SWARM_DURATION = BUILDER
				.comment("Swarm duration in seconds")
				.defineInRange("swarmDuration", 60*30, 0, Integer.MAX_VALUE/20);

		RARITY_MULTIPLIER = BUILDER
				.comment("The amount to multiply the rarity by. Higher value = more likely")
				.defineInRange("rarityMultiplier", 10f, 0, Double.MAX_VALUE);

		POKERUS_MULTIPLIER = BUILDER
				.comment("The amount to multiply the Pokerus rate by. Higher value = more likely")
				.defineInRange("pokerusMultiplier", 10f, 0, Double.MAX_VALUE);
		POKERUS_DEFAULT = BUILDER
				.comment("The amount to set Pokerus rate to, assuming that the Pokemon does not have a specific Pokerus rate. 1/pokerusDefault will have Pokerus.")
				.defineInRange("pokerusDefault", 250f, 0, Double.MAX_VALUE);

		SHINY_MULTIPLIER = BUILDER
				.comment("The amount to multiply Shiny rate by. Higher value = more likely")
				.defineInRange("shinyMultiplier", 100f, 0, Double.MAX_VALUE);
		SHINY_DEFAULT = BUILDER
				.comment("The amount to set Shiny rate to, assuming that the Pokemon does not have a specific Shiny rate. 1/shinyDefault will be Shiny.")
				.defineInRange("shinyDefault", 250f, 0, Double.MAX_VALUE);

		USE_PERCENTAGE = BUILDER
				.comment("If true, spawnPercentage will be used instead of the rarityMultiplier.")
				.define("usePercentage", false);
		SPAWN_PERCENTAGE = BUILDER
				.comment("The percent chance to overwrite any other spawn in valid locations. E.g. if set to 10, 10% Pokemon will be the swarming Pokemon, and 9/10 will be normal spawns (including swarming Pokemon with the multipliers above).")
				.defineInRange("spawnPercentage", 10f, 0, 99.9f);

		CHANCE_TO_IGNORE_TIME = BUILDER
				.comment("The percent chance to ignore the time condition.")
				.defineInRange("chanceToIgnoreTime", 0.5f, 0, 1f);

		CHANCE_TO_IGNORE_WEATHER = BUILDER
				.comment("The percent chance to ignore the weather condition.")
				.defineInRange("chanceToIgnoreWeather", 0.5f, 0, 1f);

		IGNORED_PREFIXES = BUILDER
				.comment("A list of prefixes to ignore when picking a Pokemon - useful if certain biome mods are not installed.")
				.defineList("ignoredPrefixes", Arrays.asList("byg", "biomesoplenty", "teralith", "terralith"), entry -> true);

		IGNORED_BIOMES = BUILDER
				.comment("A list of biomes to ignore when picking a Pokemon.")
				.defineList("ignoredBiomes", Arrays.asList(""), entry -> true);


		BUILDER.push("Messages");
			SEND_MESSAGE_IN_CONSOLE = BUILDER
					.comment("Should we send messages in the console about the current swarm?")
					.define("sendMessageInConsole", false);

			SEND_MESSAGE_TO_PLAYERS = BUILDER
					.comment("Should we send messages to each player about the current swarm?")
					.define("sendMessageToPlayers", true);
		BUILDER.pop();
	}

	static final ForgeConfigSpec SPEC = BUILDER.build();

	public static int swarmDuration;

	public static double rarityMultiplier;
	public static double pokerusMultiplier;
	public static double pokerusDefault;
	public static double shinyMultiplier;
	public static double shinyDefault;

	public static boolean usePercentage;
	public static double spawnPercentage;

	public static double chanceToIgnoreTime;
	public static double chanceToIgnoreWeather;

	public static List<? extends String> ignoredPrefixes;
	public static List<? extends String> ignoredBiomes;

	public static boolean sendMessageInConsole;
	public static boolean sendMessageToPlayers;

	@SubscribeEvent
	static void onLoad(final ModConfig.ModConfigEvent event) {
		swarmDuration = SWARM_DURATION.get();
		rarityMultiplier = RARITY_MULTIPLIER.get();
		pokerusMultiplier = POKERUS_MULTIPLIER.get();
		pokerusDefault = POKERUS_DEFAULT.get();
		shinyMultiplier = SHINY_MULTIPLIER.get();
		shinyDefault = SHINY_DEFAULT.get();
		usePercentage = USE_PERCENTAGE.get();
		spawnPercentage = SPAWN_PERCENTAGE.get();
		chanceToIgnoreTime = CHANCE_TO_IGNORE_TIME.get();
		chanceToIgnoreWeather = CHANCE_TO_IGNORE_WEATHER.get();
		ignoredPrefixes = IGNORED_PREFIXES.get();
		ignoredBiomes = IGNORED_BIOMES.get();
		sendMessageInConsole = SEND_MESSAGE_IN_CONSOLE.get();
		sendMessageToPlayers = SEND_MESSAGE_TO_PLAYERS.get();
	}
}
