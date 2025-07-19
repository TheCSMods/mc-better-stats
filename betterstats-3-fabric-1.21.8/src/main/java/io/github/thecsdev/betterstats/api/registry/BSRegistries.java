package io.github.thecsdev.betterstats.api.registry;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.api.util.formatters.StatValueFormatter;
import io.github.thecsdev.betterstats.api.util.stats.SUMobStat;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.registry.TMutableRegistry;
import io.github.thecsdev.tcdcommons.api.registry.TRegistry;
import io.github.thecsdev.tcdcommons.api.util.TextUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
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
	@Deprecated(since = "3.9.1", forRemoval = true)
	public static final Map<StatType<EntityType<?>>, Function<SUMobStat, Text>> ENTITY_STAT_TEXT_FORMATTER;
	
	/**
	 * A {@link Map} of {@link Text}s representing "phrases" for each entity stat type.<br/>
	 * For example:<br/>
	 * - {@link Stats#KILLED} becomes "Killed"<br>
	 * - {@link Stats#KILLED_BY} becomes "Died to"<br/>
	 * - and so on...
	 */
	public static final Map<StatType<EntityType<?>>, Text> ENTITY_STAT_PHRASE;
	// --------------------------------------------------
	/**
	 * Formatters for formatting time-based statistic values.
	 * @since 3.13
	 */
	@Experimental
	public static final TMutableRegistry<StatValueFormatter> STAT_TIME_FORMATTER = new TMutableRegistry<>();
	
	/**
	 * Formatters for formatting distance-based statistic values.
	 * @since 3.13
	 */
	@Experimental
	public static final TMutableRegistry<StatValueFormatter> STAT_DISTANCE_FORMATTER = new TMutableRegistry<>();
	// --------------------------------------------------
	static
	{
		//define the registries
		ITEM_WIKIS = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		MOB_WIKIS = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		ENTITY_STAT_TEXT_FORMATTER = new HashMap<>();
		ENTITY_STAT_PHRASE = new HashMap<>();
		
		//the default Wiki for 'minecraft' is now 'minecraft.wiki'
		final String mc = Identifier.of("air").getNamespace();
		ITEM_WIKIS.put(mc, id -> "https://minecraft.wiki/" + id.getPath());
		MOB_WIKIS.put(mc, id -> "https://minecraft.wiki/" + id.getPath());
		
		//the default entity stat text formatters for vanilla stat types
		ENTITY_STAT_TEXT_FORMATTER.put(Stats.KILLED, stat ->
		{
			return literal("")
					.append(getEntityStatTypePhrase(Stats.KILLED))
					.append(": " + stat.kills);
			/*final Text entityName = stat.getStatLabel();
			return (stat.kills == 0) ?
					translatable("stat_type.minecraft.killed.none", entityName) :
					translatable("stat_type.minecraft.killed", Integer.toString(stat.kills), entityName);*/
		});
		ENTITY_STAT_TEXT_FORMATTER.put(Stats.KILLED_BY, stat ->
		{
			return literal("")
					.append(getEntityStatTypePhrase(Stats.KILLED_BY))
					.append(": " + stat.deaths);
			/*final Text entityName = stat.getStatLabel();
			return (stat.deaths == 0) ?
					translatable("stat_type.minecraft.killed_by.none", entityName) :
					translatable("stat_type.minecraft.killed_by", entityName, Integer.toString(stat.deaths));*/
		});
		
		ENTITY_STAT_PHRASE.put(Stats.KILLED, BST.stp_mc_killed());
		ENTITY_STAT_PHRASE.put(Stats.KILLED_BY, BST.stp_mc_killedBy());
	}
	// ==================================================
	/**
	 * Obtains the "item Wiki" web URL for a given {@link Item}, using {@link #ITEM_WIKIS}.
	 * @param itemId The unique {@link Identifier} of the given {@link Item}.
	 * @return {@code null} if the URL is not found.
	 * @throws NullPointerException If the argument is null.
	 */
	public static final @Nullable String getItemWikiURL(Identifier itemId) throws NullPointerException
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
	public static final @Nullable String getMobWikiURL(Identifier entityId) throws NullPointerException
	{
		Objects.requireNonNull(entityId);
		var supplier = MOB_WIKIS.get(entityId.getNamespace());
		if(supplier == null) return null;
		else return supplier.apply(entityId);
	}
	
	/**
	 * Obtains the {@link Text} representing the "phrase" for a given entity {@link StatType}.
	 * @param statType The {@link StatType}.
	 */
	public static final Text getEntityStatTypePhrase(StatType<EntityType<?>> statType)
	{
		//null-check
		if(statType == null) return TextUtils.literal("null");
		
		//first approach: look at the better-stats registry
		{
			final @Nullable var esp = ENTITY_STAT_PHRASE.get(statType);
			if(esp != null) return esp;
		}
		
		//alternative approaches: using the id
		do
		{
			final @Nullable var statTypeId = Registries.STAT_TYPE.getId(statType);
			if(statTypeId == null) break;
			
			//look in the translation keys
			{
				final var stKey = statTypeId.toString().replace(':', '.');
				final var tKey = "betterstats.stattype_phrase." + stKey;
				final var phrase = Text.translatable(tKey);
				if(!Objects.equals(tKey, phrase.getString())) return phrase;
			}
			
			//return the id as a literal if nothing is found
			return TextUtils.literal(statTypeId.toString());
		}
		while(false);
		
		//last resort: null
		return TextUtils.literal("null");
	}
	// ==================================================
}