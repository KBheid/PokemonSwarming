package thingxII.pokemonswarming;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.*;
import net.minecraft.entity.player.ServerPlayerEntity;

public class SwarmCommandRegistry {
	private static IFormattableTextComponent ignoringTimeComponent = new StringTextComponent("Ignores Time: ");
	private static IFormattableTextComponent ignoringWeatherComponent = new StringTextComponent("Ignores Weather: ");

	private static IFormattableTextComponent trueComponent  = new StringTextComponent("true").withStyle(Style.EMPTY.withColor(Color.fromRgb(0x4a6f28)).withBold(true));
	private static IFormattableTextComponent falseComponent = new StringTextComponent("false").withStyle(Style.EMPTY.withColor(Color.fromRgb(0xb02e26)).withBold(true));

	public static class SwarmCommand {

		public static void register(CommandDispatcher<CommandSource> dispatcher) {
			LiteralArgumentBuilder<CommandSource> swarmCommand =
					Commands.literal("swarm")
							.executes(SwarmCommand::getCurrentSwarm);

			dispatcher.register(swarmCommand);
		}

		static int getCurrentSwarm(CommandContext<CommandSource> commandContext) {

			try {
				ServerPlayerEntity p = commandContext.getSource().getPlayerOrException();
				int secondsLeft = SwarmCoordinator.coordinator.GetSecondsRemaining();
				String timeRemaining = String.format("%d hours, %02d minutes, %02d seconds", secondsLeft / 3600, (secondsLeft % 3600) / 60, (secondsLeft % 60));
				String swarmName = SwarmCoordinator.coordinator.GetCurrentSwarmName();

				p.sendMessage(new StringTextComponent(swarmName), p.getUUID());

				if (!swarmName.equals("Currently none.")) {
					p.sendMessage(new StringTextComponent("It will remain for: " + timeRemaining), p.getUUID());

					IFormattableTextComponent timeComponent = (SwarmCoordinator.coordinator.GetCurrentSwarmIgnoresTime()) ?
							ignoringTimeComponent.copy().append(trueComponent) : ignoringTimeComponent.copy().append(falseComponent);

					IFormattableTextComponent weatherComponent = (SwarmCoordinator.coordinator.GetCurrentSwarmIgnoresWeather()) ?
							ignoringWeatherComponent.copy().append(trueComponent) : ignoringWeatherComponent.copy().append(falseComponent);

					p.sendMessage(timeComponent, p.getUUID());
					p.sendMessage(weatherComponent, p.getUUID());
				}
			}
			catch (CommandSyntaxException e) { }

			return 1;
		}
	}
}
