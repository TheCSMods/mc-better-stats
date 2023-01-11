package io.github.thecsdev.betterstats.api.registry;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;

public class BetterStatsRegistry
{
	// ==================================================
	protected BetterStatsRegistry() {}
	// ==================================================
	/**
	 * A {@link Map} of mod IDs and {@link Function}s that
	 * act as suppliers for item Wiki URLs for the given mods.<br/>
	 * <br/>
	 * The keys represent mod IDs.<br/>
	 * The values represent {@link Function}s that return the
	 * Wiki URL for a given item from that mod.
	 */
	public static final Map<String, Function<ResourceLocation, String>> ITEM_WIKIS;
	
	/**
	 * A {@link Map} of mod IDs and {@link Function}s that
	 * act as suppliers for mob/entity Wiki URLs for the given mods.<br/>
	 * <br/>
	 * The keys represent mod IDs.<br/>
	 * The values represent {@link Function}s that return the
	 * Wiki URL for a given mob/entity from that mod.
	 */
	public static final Map<String, Function<ResourceLocation, String>> MOB_WIKIS;
	// --------------------------------------------------
	static
	{
		//define the registries
		ITEM_WIKIS = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		MOB_WIKIS = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		
		//the default Wiki for 'minecraft' is the Fandom wiki
		final String mc = new ResourceLocation("air").getNamespace();
		ITEM_WIKIS.put(mc, id -> "https://minecraft.fandom.com/wiki/" + id.getPath());
		MOB_WIKIS.put(mc, id -> "https://minecraft.fandom.com/wiki/" + id.getPath());
	}
	// ==================================================
	public static @Nullable String getItemWikiURL(ResourceLocation itemId)
	{
		Objects.requireNonNull(itemId, "itemId must not be null.");
		var supplier = ITEM_WIKIS.get(itemId.getNamespace());
		if(supplier == null) return null;
		else return supplier.apply(itemId);
	}
	
	public static @Nullable String getMobWikiURL(ResourceLocation entityId)
	{
		Objects.requireNonNull(entityId, "entityId must not be null.");
		var supplier = MOB_WIKIS.get(entityId.getNamespace());
		if(supplier == null) return null;
		else return supplier.apply(entityId);
	}
	// ==================================================
}