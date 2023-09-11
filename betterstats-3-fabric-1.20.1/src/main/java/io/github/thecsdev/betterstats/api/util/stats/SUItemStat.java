package io.github.thecsdev.betterstats.api.util.stats;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.fTranslatable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import io.github.thecsdev.betterstats.api.util.BSUtils;
import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class SUItemStat extends SUStat<Item>
{
	// ==================================================
	protected final Item item;
	protected final @Nullable Block block;
	protected final boolean isEmpty; //cached value to avoid re-calculations
	protected final String itemIdSQ; //"search query" helper - edit: was
	// --------------------------------------------------
	public final int mined, crafted, used, broken, pickedUp, dropped;
	// ==================================================
	protected SUItemStat(IStatsProvider statsProvider, Item item)
	{
		super(statsProvider, Registries.ITEM.getId(Objects.requireNonNull(item)), getItemStatText(item));
		this.item = item;
		this.block = Block.getBlockFromItem(item);
		this.itemIdSQ = Objects.toString(this.statId);
		
		//handle sMined
		if(this.block == null) mined = 0;
		else this.mined = statsProvider.getStatValue(Stats.MINED, this.block);
		
		//handle other stats
		this.crafted  = statsProvider.getStatValue(Stats.CRAFTED, item);
		this.used     = statsProvider.getStatValue(Stats.USED, item);
		this.broken   = statsProvider.getStatValue(Stats.BROKEN, item);
		this.pickedUp = statsProvider.getStatValue(Stats.PICKED_UP, item);
		this.dropped  = statsProvider.getStatValue(Stats.DROPPED, item);
		
		//define isEmpty
		this.isEmpty = (this.mined == 0 && this.crafted == 0 &&
				this.used == 0 && this.broken == 0 &&
				this.pickedUp == 0 && this.dropped == 0);
	}
	// ==================================================
	/**
	 * Returns the {@link Item} corresponding with this {@link SUItemStat}.
	 */
	public final Item getItem() { return this.item; }
	
	/**
	 * Returns the {@link Block} that corresponds with {@link #getItem()},
	 * or {@code null} if the {@link Item} does not have a corresponding {@link Block}.
	 */
	public final @Nullable Block getBlock() { return this.block; }
	
	/**
	 * Returns the {@link Item}'s {@link Identifier}, as a {@link String}.
	 */
	public final String getItemIDString() { return this.itemIdSQ; }
	// --------------------------------------------------
	public final @Override boolean isEmpty() { return this.isEmpty; }
	// --------------------------------------------------
	public @Override boolean matchesSearchQuery(String search)
	{
		return super.matchesSearchQuery(search) ||
				StringUtils.defaultString(search).contains(this.itemIdSQ);
	}
	// ==================================================
	/**
	 * Returns the {@link Text} that should correspond to a given {@link SUItemStat}.
	 */
	public static Text getItemStatText(Item item) { return fTranslatable(item.getTranslationKey()); }
	// ==================================================
	/**
	 * Obtains all {@link Item} {@link Stat}s in form of {@link SUItemStat}.
	 * @param statsProvider The {@link IStatsProvider}.
	 * @param filter Optional. A {@link Predicate} used to filter out any unwanted {@link SUItemStat}s.
	 */
	public static Collection<SUItemStat> getItemStats
	(IStatsProvider statsProvider, @Nullable Predicate<SUItemStat> filter)
	{
		//create the result list
		final var result = new ArrayList<SUItemStat>();
		
		//iterate and group all items
		for(final Item itemReg : Registries.ITEM)
		{
			//create item stat
			final var itemStat = new SUItemStat(statsProvider, itemReg);
			
			//filter
			if(filter != null && !filter.test(itemStat))
				continue;
			
			//add to the list
			result.add(itemStat);
		}
		
		//return the result
		return result;
	}
	
	/**
	 * Obtains all {@link Item} {@link Stat}s in form of {@link SUItemStat}, grouped
	 * into {@link ItemGroup}s using a {@link Map}.
	 * @param statsProvider The {@link IStatsProvider}.
	 * @param filter Optional. A {@link Predicate} used to filter out any unwanted {@link SUItemStat}s.
	 */
	public static Map<ItemGroup, Collection<SUItemStat>> getItemStatsByItemGroups
	(IStatsProvider statsProvider, @Nullable Predicate<SUItemStat> filter)
	{
		//create new map
		final var result = new LinkedHashMap<ItemGroup, Collection<SUItemStat>>();
		result.put(null, new ArrayList<>()); //the null category goes first
		
		//iterate and group all items
		for(final SUItemStat itemStat : getItemStats(statsProvider, filter))
		{
			//group item
			var itemRegGroup = BSUtils.getItemGroup(itemStat.item);
			if(!result.containsKey(itemRegGroup)) //ensure a corresponding list exists
				result.put(itemRegGroup, new ArrayList<>());
			result.get(itemRegGroup).add(itemStat);
		}
		
		//check the null category in case no items got listed there
		if(result.get(null).size() == 0)
			result.remove(null);
		
		//return the result
		return result;
	}
	
	/**
	 * Obtains all {@link Item} {@link Stat}s in form of {@link SUItemStat}, grouped
	 * into "mod groups" using a {@link Map}. The {@link Map} keys represent "mod IDs".
	 * @param statsProvider The {@link IStatsProvider}.
	 * @param filter Optional. A {@link Predicate} used to filter out any unwanted {@link SUItemStat}s.
	 */
	public static Map<String, Collection<SUItemStat>> getItemStatsByModGroups
	(IStatsProvider statsProvider, @Nullable Predicate<SUItemStat> filter)
	{
		//create the result map
		final var result = new LinkedHashMap<String, Collection<SUItemStat>>();
		
		//add the 'minecraft' category first
		String mcModId = new Identifier("air").getNamespace();
		result.put(mcModId, new ArrayList<>());
		
		//iterate all items and add them to the map
		for(final SUItemStat itemStat : getItemStats(statsProvider, filter))
		{
			//---------- group the item
			//obtain mod id
			final String itemModId = Registries.ITEM.getId(itemStat.item).getNamespace();
			
			//get or create a group array
			if(!result.containsKey(itemModId))
				result.put(itemModId, Lists.newArrayList());
			final var resultList = result.get(itemModId);
			
			//add the stat to the array
			resultList.add(itemStat);
		}
		
		//make sure 'minecraft' actually has entries
		//(aka handle cases where the filter filters out all 'minecraft' entries)
		if(result.get(mcModId).size() == 0)
			result.remove(mcModId);
		
		//return the result
		return result;
	}
	// ==================================================
}