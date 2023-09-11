package io.github.thecsdev.betterstats.api.registry;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.tcdcommons.api.registry.TRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
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
	// --------------------------------------------------
	static
	{
		//define the registries
		ITEM_WIKIS = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		MOB_WIKIS = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		
		//the default Wiki for 'minecraft' was the Fandom wiki
		/*final String mc = new Identifier("air").getNamespace();
		ITEM_WIKIS.put(mc, id -> "https://minecraft.fandom.com/wiki/" + id.getPath());
		MOB_WIKIS.put(mc, id -> "https://minecraft.fandom.com/wiki/" + id.getPath());*/
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