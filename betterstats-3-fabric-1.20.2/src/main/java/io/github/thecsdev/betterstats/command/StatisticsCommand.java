package io.github.thecsdev.betterstats.command;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;
import static io.github.thecsdev.tcdcommons.command.PlayerBadgeCommand.handleError;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.thecsdev.tcdcommons.api.command.argument.StatArgumentType;
import io.github.thecsdev.tcdcommons.api.util.TextUtils;
import io.github.thecsdev.tcdcommons.mixin.hooks.AccessorStatHandler;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.stat.StatType;
import net.minecraft.text.Text;

public final class StatisticsCommand
{
	// ==================================================
	public static final String TEXT_EDIT_OUTPUT = "commands.statistics.edit.output";
	public static final Text TEXT_CLEAR_KICK = translatable("commands.statistics.clear.kick");
	public static final String TEXT_CLEAR_OUTPUT = "commands.statistics.clear.output";
	// ==================================================
	private StatisticsCommand() {}
	// ==================================================
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess cra)
	{
		//define the command and its alt.
		final var statistics = literal("statistics").requires(scs -> scs.hasPermissionLevel(2))
				.then(statistics_edit(cra))
				.then(statistics_clear());
		final var stats = literal("stats").requires(scs -> scs.hasPermissionLevel(2))
				.then(statistics_edit(cra))
				.then(statistics_clear());
		
		//register the command
		dispatcher.register(statistics);
		dispatcher.register(stats);
	}
	// --------------------------------------------------
	private static ArgumentBuilder<ServerCommandSource, ?> statistics_edit(CommandRegistryAccess cra)
	{
		return literal("edit")
				.then(argument("targets", EntityArgumentType.players())
						.then(argument("stat_type", RegistryEntryArgumentType.registryEntry(cra, RegistryKeys.STAT_TYPE))
								.then(argument("stat", StatArgumentType.stat())
										.then(literal("set")
												.then(argument("value", IntegerArgumentType.integer(0))
														.executes(ctx -> execute_edit(ctx, true))
														)
												)
										.then(literal("increase")
												.then(argument("value", IntegerArgumentType.integer())
														.executes(ctx -> execute_edit(ctx, false))
														)
												)
										)
								)
						);
	}
	private static ArgumentBuilder<ServerCommandSource, ?> statistics_clear()
	{
		return literal("clear")
				.then(argument("targets", EntityArgumentType.players())
						.executes(ctx -> execute_clear(ctx)));
	}
	// --------------------------------------------------
	/*private static ArgumentBuilder<ServerCommandSource, ?> arg_stat()
	{
		final Function<CommandContext<?>, Iterable<Identifier>> supplier = context ->
		{
			@SuppressWarnings("unchecked")
			final var ctx = (CommandContext<ServerCommandSource>)context;
			try
			{
				@SuppressWarnings("unchecked")
				final var arg_stat_type = (StatType<Object>)RegistryEntryArgumentType.getRegistryEntry(ctx, "stat_type", RegistryKeys.STAT_TYPE).value();
				return arg_stat_type.getRegistry().getKeys().stream().map(key -> key.getValue()).toList();
			}
			catch (CommandSyntaxException | IllegalStateException e) { return Collections.emptyList(); }
		};
		return argument("stat", SuggestiveIdentifierArgumentType.suggestiveId(supplier));
	}*/
	// ==================================================
	@SuppressWarnings("unchecked")
	private static int execute_edit(CommandContext<ServerCommandSource> context, boolean setOrIncrease)
	{
		try
		{
			//get parameter values
			final var arg_targets = EntityArgumentType.getPlayers(context, "targets");
			final var arg_stat_type = (StatType<Object>)RegistryEntryArgumentType.getRegistryEntry(context, "stat_type", RegistryKeys.STAT_TYPE).value();
			final var arg_stat = IdentifierArgumentType.getIdentifier(context, "stat");
			final int arg_value = IntegerArgumentType.getInteger(context, "value");
			
			final var stat_object = arg_stat_type.getRegistry().getOrEmpty(arg_stat).orElse(null);
			Objects.requireNonNull(stat_object, "Registry entry '" + arg_stat + "' does not exist for registry '" + arg_stat_type.getRegistry() + "'.");
			final var stat = arg_stat_type.getOrCreateStat(stat_object);
			
			//execute
			final AtomicInteger affected = new AtomicInteger();
			for(final var target : arg_targets)
			{
				//null check
				if(target == null) continue;
				
				//set stat value
				if(setOrIncrease) target.getStatHandler().setStat(target, stat, arg_value);
				else target.getStatHandler().increaseStat(target, stat, arg_value);
				affected.incrementAndGet();
				
				//update the client
				target.getStatHandler().sendStats(target);
			}
			
			//send feedback
			context.getSource().sendFeedback(() -> translatable(
					TEXT_EDIT_OUTPUT,
					"[" + Registries.STAT_TYPE.getId(arg_stat_type) + " / " + arg_stat + "]",
					Integer.toString(affected.get())
				), false);
		}
		catch(CommandException | CommandSyntaxException | IllegalStateException | NullPointerException e) { handleError(context, e); }
		return 1;
	}
	private static int execute_clear(CommandContext<ServerCommandSource> context)
	{
		try
		{
			//get parameter values
			final var targets = EntityArgumentType.getPlayers(context, "targets");

			//execute
			final AtomicInteger affected = new AtomicInteger();
			for(final var target : targets)
			{
				//null check
				if(target == null) continue;
				
				//clear statistics
				((AccessorStatHandler)target.getStatHandler()).getStatMap().clear();
				affected.incrementAndGet();
				
				//disconnect the player because that's the only way to update the client
				target.networkHandler.disconnect(TextUtils.literal("")
						.append(TEXT_CLEAR_KICK)
						.append("\n\n[EN]: Your statistics were cleared, which requires you to disconnect and re-join."));
			}
			
			//send feedback
			context.getSource().sendFeedback(() -> translatable(TEXT_CLEAR_OUTPUT, Integer.toString(affected.get())), false);
		}
		catch(CommandException | CommandSyntaxException e) { handleError(context, e); }
		return 1;
	}
	// ==================================================
}