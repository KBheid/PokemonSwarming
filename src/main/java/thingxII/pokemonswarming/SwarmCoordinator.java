package thingxII.pokemonswarming;

import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.spawning.*;
import com.pixelmonmod.pixelmon.api.spawning.archetypes.entities.pokemon.SpawnInfoPokemon;
import com.pixelmonmod.pixelmon.spawning.PixelmonSpawning;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import thingxII.pokemonswarming.mixin.Patch_WeatherAccessor;

import java.util.*;

import static thingxII.pokemonswarming.PokemonSwarming.SWARM_LOGGER;

public class SwarmCoordinator {
	private static final int RETRY_TICKS = 30 * 20;

	public static SwarmCoordinator coordinator;
	private SwarmInfo currentSwarm;
	private SpawnSet currentSwarmSet;
	private int ticksRemaining = 0;
	private String currentSwarmString;

	public SwarmCoordinator() {
		coordinator = this;
		CreateSwarm();
	}

	@SubscribeEvent
	public void Tick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			return;
		}

		ticksRemaining--;
		if (ticksRemaining <= 0) {
			currentSwarm = null;
			CreateSwarm();
		}
	}

	public String GetCurrentSwarmName() {
		return currentSwarmString;
	}
	public int GetSecondsRemaining() { return ticksRemaining/20; }
	public boolean GetCurrentSwarmIgnoresTime() { return currentSwarm.ignoringTime; }
	public boolean GetCurrentSwarmIgnoresWeather() { return currentSwarm.ignoringWeather; }

	public void CreateSwarm() {
		// Coordinator is not present or inactive, just retry at a later time
		if (PixelmonSpawning.coordinator == null || !PixelmonSpawning.coordinator.getActive()) {
			currentSwarmSet = null;
			currentSwarmString = "Currently none.";
			ticksRemaining = RETRY_TICKS;
			return;
		}

		// Remove old if necessary
		if (currentSwarmSet != null) {
			PixelmonSpawning.standard.remove(currentSwarmSet);
		}

		currentSwarm = CreateNewSwarmSpawnInfo();

		if (currentSwarm == null) {
			currentSwarmSet = null;
			currentSwarmString = "Currently none.";
			ticksRemaining = RETRY_TICKS;
			return;
		}
		Species s = currentSwarm.info.getSpecies();

		String specName = s.getName();

		SpawnSet newSet = new SpawnSet();
		newSet.spawnInfos = new ArrayList<>();
		newSet.spawnInfos.add(currentSwarm.info);

		currentSwarmSet = newSet;
		PixelmonSpawning.standard.add(currentSwarmSet);

		// Re-initialize tracking spawners
		PixelmonSpawning.trackingSpawnerPreset.setSpawnSets(PixelmonSpawning.standard).addSpawnSets(PixelmonSpawning.npcs).setupCache();
		if (PixelmonSpawning.coordinator != null)
			PixelmonSpawning.coordinator.activate();

		// Broadcast to players
		String output = specName + " is swarming in " + currentSwarm.spawnBiome + " biomes!";
		currentSwarmString = output;

		if (Config.sendMessageInConsole)
			SWARM_LOGGER.info(output);

		if (Config.sendMessageToPlayers) {
			for (ServerPlayerEntity player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
				player.sendMessage(new StringTextComponent(output), player.getUUID());
			}
		}

		ticksRemaining = Config.swarmDuration*20;
	}

	private SwarmInfo CreateNewSwarmSpawnInfo() {
		int maxAttempts = 10;
		int attempts = 0;
		Random r = new Random();
		List<SpawnSet> sets = PixelmonSpawning.standard;

		SwarmInfo swarmInfo = null;

		boolean ignoringWeather = false;
		boolean ignoringTime = false;
		while (swarmInfo == null && attempts < maxAttempts) {
			attempts++;

			// Get random set and random info in the set
			SpawnSet set = sets.get(r.nextInt(sets.size()));
			SpawnInfo info = set.spawnInfos.get(r.nextInt(set.spawnInfos.size()));

			// Only include Pokemon
			if (!info.typeID.equals("pokemon"))
				continue;

			// Cast to Pokemon
			SpawnInfoPokemon sip = (SpawnInfoPokemon) info;
			PokemonSpecification spec = sip.getPokemonSpec().clone();

			if (sip.tags.contains("legendary") || sip.tags.contains("mythical"))
				continue;

			// If no valid locations, try again
			if (sip.locationTypes.isEmpty())
				continue;

			// Get random valid location
			Object[] biomesObjs = sip.condition.biomes.toArray();
			if (biomesObjs.length == 0)
				continue;

			int randBiome = r.nextInt(biomesObjs.length);
			ResourceLocation biome = (ResourceLocation) biomesObjs[randBiome];

			// Selectively ignore biomes
			boolean ignoredBiome = false;
			for (String ignored : Config.ignoredPrefixes) {
				if (biome.toString().startsWith(ignored)) {
					ignoredBiome = true;
					break;
				}
			}
			for (String ignored : Config.ignoredBiomes) {
				if (biome.toString().equals(ignored) || ignoredBiome) {
					ignoredBiome = true;
					break;
				}
			}
			if (ignoredBiome)
				continue;


			// Copy generic data from old to new
			SpawnInfoPokemon clonedSpawnInfo = new SpawnInfoPokemon();

			clonedSpawnInfo.condition 			= sip.condition;
			clonedSpawnInfo.condition.biomes    = new HashSet<>();

			clonedSpawnInfo.species 			= sip.getSpecies();
			clonedSpawnInfo.form				= sip.getForm();
			clonedSpawnInfo.typeID 				= sip.typeID;
			clonedSpawnInfo.tags 				= sip.tags;
			clonedSpawnInfo.anticondition 		= sip.anticondition;
			clonedSpawnInfo.compositeCondition 	= sip.compositeCondition;
			clonedSpawnInfo.rarityMultipliers 	= sip.rarityMultipliers;

			// Copy pokemon specific data from old to new
			clonedSpawnInfo.minLevel 				= sip.minLevel;
			clonedSpawnInfo.maxLevel 				= sip.maxLevel;
			clonedSpawnInfo.spawnSpecificBossRate 	= 0f;
			clonedSpawnInfo.locationTypes 			= sip.locationTypes;
			clonedSpawnInfo.heldItems 				= sip.heldItems;

			// Update values from config
			clonedSpawnInfo.rarity 						= sip.rarity * (float) Config.rarityMultiplier;
			clonedSpawnInfo.spawnSpecificPokerusRate 	= (sip.spawnSpecificPokerusRate == null) ? (float) Config.pokerusDefault : sip.spawnSpecificPokerusRate / (float) Config.pokerusMultiplier;
			clonedSpawnInfo.spawnSpecificShinyRate 		= (sip.spawnSpecificShinyRate == null)   ? (float) Config.shinyDefault   : sip.spawnSpecificShinyRate   / (float) Config.shinyMultiplier;

			// Using the percentage will overwrite rarity changes
			if (Config.usePercentage) {
				clonedSpawnInfo.percentage = (float) Config.spawnPercentage;
			}

			ignoringTime = r.nextFloat() < Config.chanceToIgnoreTime;
			ignoringWeather = r.nextFloat() < Config.chanceToIgnoreWeather;
			if (ignoringTime)
				clonedSpawnInfo.condition.times 	= new ArrayList<>();
			if (ignoringWeather)
				((Patch_WeatherAccessor) clonedSpawnInfo.condition).setWeathers(new ArrayList<>());

			// Add our location in - only one
			clonedSpawnInfo.condition.biomes.add(biome);
			clonedSpawnInfo.setPokemon(spec);

			// Set our info as valid
			swarmInfo = new SwarmInfo();
			swarmInfo.spawnBiome = biome.toString();
			swarmInfo.info = clonedSpawnInfo;
			swarmInfo.ignoringTime = ignoringTime;
			swarmInfo.ignoringWeather = ignoringWeather;
		}

		return swarmInfo;
	}

	private static class SwarmInfo {
		public SpawnInfoPokemon info;
		public String spawnBiome;
		public boolean ignoringTime;
		public boolean ignoringWeather;
	}
}
