package io.github.thecsdev.betterstats.api.registry;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.api.util.stats.SUMobStat;
import io.github.thecsdev.tcdcommons.api.registry.TRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.stat.StatType;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * {@link BetterStats} registries that are present on both the client and the server side.
 */
public final class BSRegistries
{
	// ==================================================
	private BSRegistries() {}
	// ==================================================
	/**
	 * A {@link Map} of mod IDs and {@link Function}s that
	 * act as suppliers for item Wiki URLs for the given mods.<br/>
	 * <br/>
	 * The keys represent mod IDs.<br/>
	 * The values represent {@link Function}s that return the
	 * Wiki URL for a given item from that mod.
	 * 
	 * @apiNote I did not use a {@link TRegistry} because it uses {@link Identifier}s as "keys".
	 */
	public static final Map<String, Function<Identifier, String>> ITEM_WIKIS;
	
	/**
	 * A {@link Map} of mod IDs and {@link Function}s that
	 * act as suppliers for mob/entity Wiki URLs for the given mods.<br/>
	 * <br/>
	 * The keys represent mod IDs.<br/>
	 * The values represent {@link Function}s that return the
	 * Wiki URL for a given mob/entity from that mod.
	 * 
	 * @apiNote I did not use a {@link TRegistry} because it uses {@link Identifier}s as "keys".
	 */
	public static final Map<String, Function<Identifier, String>> MOB_WIKIS;
	
	/**
	 * A {@link Map} of {@link Function}s whose purpose is to return
	 * formatted {@link Text}s for corresponding {@link SUMobStat}s.<br/>
	 * The way it works is, the {@link Function} is given an {@link SUMobStat} that
	 * holds information about the stats provider and the stat itself, and the
	 * {@link Function}'s job is to obtain the stat value for the corresponding
	 * {@link StatType}, and then format it into a user-friendly {@link Text}.
	 * @apiNote The {@link Function} must not return {@code null}!
	 */
	public static final Map<StatType<EntityType<?>>, Function<SUMobStat, Text>> ENTITY_STAT_TEXT_FORMATTER;
	// --------------------------------------------------
	static
	{
		//define the registries
		ITEM_WIKIS = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		MOB_WIKIS = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		ENTITY_STAT_TEXT_FORMATTER = new HashMap<>();
		
		//the default Wiki for 'minecraft' is now 'minecraft.wiki'
		final String mc = new Identifier("air").getNamespace();
		ITEM_WIKIS.put(mc, id -> "https://minecraft.wiki/" + id.getPath());
		MOB_WIKIS.put(mc, id -> "https://minecraft.wiki/" + id.getPath());
		
		//the default entity stat text formatters for vanilla stat types
		ENTITY_STAT_TEXT_FORMATTER.put(Stats.KILLED, stat ->
		{
			final Text entityName = stat.getStatLabel();
			return (stat.kills == 0) ?
					translatable("stat_type.minecraft.killed.none", entityName) :
					translatable("stat_type.minecraft.killed", Integer.toString(stat.kills), entityName);
		});
		ENTITY_STAT_TEXT_FORMATTER.put(Stats.KILLED_BY, stat ->
		{
			final Text entityName = stat.getStatLabel();
			return (stat.deaths == 0) ?
					translatable("stat_type.minecraft.killed_by.none", entityName) :
					translatable("stat_type.minecraft.killed_by", entityName, Integer.toString(stat.deaths));
		});
	}
	// ==================================================
	/**
	 * Obtains the "item Wiki" web URL for a given {@link Item}, using {@link #ITEM_WIKIS}.
	 * @param itemId The unique {@link Identifier} of the given {@link Item}.
	 * @return {@code null} if the URL is not found.
	 * @throws NullPointerException If the argument is null.
	 */
	public static @Nullable String getItemWikiURL(Identifier itemId) throws NullPointerException
	{
		Objects.requireNonNull(itemId);
		var supplier = ITEM_WIKIS.get(itemId.getNamespace());
		if(supplier == null) return null;
		else return supplier.apply(itemId);
	}
	
	/**
	 * Obtains the "mob Wiki" web URL for a given {@link Entity}, thanks to {@link #MOB_WIKIS}.
	 * @param entityId The unique {@link Identifier} of the given {@link Entity}.
	 * @return {@code null} if the URL is not found.
	 * @throws NullPointerException If the argument is null.
	 */
	public static @Nullable String getMobWikiURL(Identifier entityId) throws NullPointerException
	{
		Objects.requireNonNull(entityId);
		var supplier = MOB_WIKIS.get(entityId.getNamespace());
		if(supplier == null) return null;
		else return supplier.apply(entityId);
	}
	// ==================================================
}