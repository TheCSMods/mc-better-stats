package io.github.thecsdev.betterstats.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.github.thecsdev.tcdcommons.api.util.TextUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public final class StatUtils
{
	// ==================================================
	private StatUtils() {}
	// ==================================================
	/**
	 * Returns a list of all {@link StatUtilsGeneralStat}s and their values.
	 * @param StatsCounter The {@link StatsCounter} that contains all the {@link Stat} data.
	 * @param filter (optional) Make it return false to exclude an {@link StatUtilsGeneralStat}.
	 */
	public static ArrayList<StatUtilsGeneralStat> getGeneralStats(StatsCounter StatsCounter, Predicate<StatUtilsStat> filter)
	{
		//create a new list
		ArrayList<StatUtilsGeneralStat> result = Lists.newArrayList();
		Objects.requireNonNull(StatsCounter, "StatsCounter must not be null.");
		
		//obtain and sort all general (aka custom) stats
		ObjectArrayList<Stat<ResourceLocation>> statiList = new ObjectArrayList<Stat<ResourceLocation>>(Stats.CUSTOM.iterator());
	    statiList.sort(Comparator.comparing(stat -> TextUtils.translatable(getStatTranslationKey(stat)).getString()));
	    
	    //iterate and add all general stats
	    for (ObjectListIterator<Stat<ResourceLocation>> statListIterator = statiList.iterator(); statListIterator.hasNext();)
	    	result.add(new StatUtilsGeneralStat(StatsCounter, statListIterator.next()));
	    
	    //filter general stats if needed
	    if(filter != null) result.removeIf(filter.negate());
	    
		//return the result list
		return result;
	}
	// --------------------------------------------------
	/**
	 * Returns a list of all {@link StatUtilsItemStat}s and their values.<br/>
	 * <br/>
	 * Note: Will return a null {@link ItemGroup} for items that don't have an item group.
	 * @param StatsCounter The {@link StatsCounter} that contains all the {@link Stat} data.
	 * @param filter (optional) Make it return false to exclude an {@link StatUtilsItemStat}.
	 */
	public static LinkedHashMap<CreativeModeTab, ArrayList<StatUtilsItemStat>> getItemStats(StatsCounter StatsCounter, Predicate<StatUtilsStat> filter)
	{
		//create new map
		LinkedHashMap<CreativeModeTab, ArrayList<StatUtilsItemStat>> result = Maps.newLinkedHashMap();
		result.put(null, new ArrayList<>()); //the null category goes first
		
		//iterate and group all items
		for(Item itemReg : ForgeRegistries.ITEMS)
		{
			//create item stat
			StatUtilsItemStat itemStat = new StatUtilsItemStat(StatsCounter, itemReg);
			
			//filter search
			if(filter != null && !filter.test(itemStat)) continue;
			
			//get item group
			var tabs = itemReg.getCreativeTabs();
			var tab = tabs.size() > 0 ? tabs.iterator().next() : null;
			
			//group item
			if(!result.containsKey(tab))
				result.put(tab, Lists.newArrayList());
			result.get(tab).add(itemStat);
		}
		
		//check the null category in case no items got listed there
		if(result.get(null).size() == 0)
			result.remove(null);
		
		//return the result
		return result;
	}
	// --------------------------------------------------
	/**
	 * Returns a list of all {@link StatUtilsMobStat}s and their values.<br/>
	 * <br/>
	 * <b>Note:</b><br/>
	 * The {@link LinkedHashMap} keys are IDs of mods. To convert a mod id
	 * to mod name, use {@link #getModName(String)}.
	 * @param StatsCounter The {@link StatsCounter} that contains all the {@link Stat} data.
	 * @param filter (optional) Make it return false to exclude an {@link StatUtilsItemStat}. 
	 */
	public static LinkedHashMap<String, ArrayList<StatUtilsMobStat>> getMobStats(StatsCounter StatsCounter, Predicate<StatUtilsStat> filter)
	{
		//create a new list
		LinkedHashMap<String, ArrayList<StatUtilsMobStat>> result = Maps.newLinkedHashMap();
		
		//'minecraft' goes first
		String mcModId = new ResourceLocation("air").getNamespace();
		result.put(mcModId, Lists.newArrayList());
		
		//iterate all entities
		for(EntityType<?> entityType : ForgeRegistries.ENTITIES)
		{
			//(system) filter
			if(entityType != EntityType.PLAYER &&
					(!entityType.canSummon() || entityType.getCategory() == MobCategory.MISC))
				continue;
			
			//create the mob stat
			StatUtilsMobStat mobStat = new StatUtilsMobStat(StatsCounter, entityType);
			
			//filter
			if(filter != null && !filter.test(mobStat))
				continue;
			
			//obtain entity's mod id,
			//obtain the result list, and
			//put the entity in the result list
			String entityModId = EntityType.getKey(entityType).getNamespace();
			if(!result.containsKey(entityModId))
				result.put(entityModId, Lists.newArrayList());
			ArrayList<StatUtilsMobStat> resultList = result.get(entityModId);
			resultList.add(mobStat);
		}
		
		//make sure 'minecraft' actually has entries
		if(result.get(mcModId).size() == 0)
			result.remove(mcModId);
		
		//return the result
		return result;
	}
	// --------------------------------------------------
	/**
	 * Same as {@link #getItemStats(StatsCounter, Predicate)}, but
	 * the items are grouped by mod IDs, and not by {@link ItemGroup}s.<br/>
	 * <br/>
	 * <b>Note:</b><br/>
	 * The {@link LinkedHashMap} keys are IDs of mods. To convert a mod id
	 * to mod name, use {@link #getModName(String)}.
	 * @param StatsCounter The {@link StatsCounter} that contains all the {@link Stat} data.
	 * @param filter (optional) Make it return false to exclude an {@link StatUtilsItemStat}.
	 */
	public static LinkedHashMap<String, ArrayList<StatUtilsItemStat>> getItemStatsByMods(StatsCounter StatsCounter, Predicate<StatUtilsStat> filter)
	{
		//create a new list
		LinkedHashMap<String, ArrayList<StatUtilsItemStat>> result = Maps.newLinkedHashMap();
		
		//'minecraft' goes first
		String mcModId = new ResourceLocation("air").getNamespace();
		result.put(mcModId, Lists.newArrayList());
		
		//iterate all items and add them to the map
		for(Item itemReg : ForgeRegistries.ITEMS)
		{
			//create item stat
			StatUtilsItemStat itemStat = new StatUtilsItemStat(StatsCounter, itemReg);
			
			//filter search
			if(filter != null && !filter.test(itemStat)) continue;
			
			//---------- group the item
			//obtain mod id
			String itemModId = ForgeRegistries.ITEMS.getResourceKey(itemReg).get().location().getNamespace();
			//get/create a group array
			if(!result.containsKey(itemModId))
				result.put(itemModId, Lists.newArrayList());
			ArrayList<StatUtilsItemStat> resultList = result.get(itemModId);
			//add the stat to the array
			resultList.add(itemStat);
		}
		
		//make sure 'minecraft' actually has entries
		if(result.get(mcModId).size() == 0)
			result.remove(mcModId);
		
		//return the new list
		return result;
	}
	// ==================================================
	/**
	 * Returns the translation key for a given {@link Stat}.
	 * @param stat The statistic in question.
	 */
	public static String getStatTranslationKey(Stat<ResourceLocation> stat)
	{
		return "stat." + stat.getValue().toString().replace(':', '.');
	}
	
	/**
	 * Returns the name of a given mod by it's mod id.
	 * @param modId The unique ID of the mod.
	 * @return The name of the mod, or "*" if the argument is null.
	 */
	public static String getModName(String modId)
	{
		if(StringUtils.isAllBlank(modId)) return "*";
		var container = ModList.get().getModContainerById(modId);
		if(container.isPresent()) return container.get().getModInfo().getDisplayName();
		else return modId;
	}
	// ==================================================
	public static abstract class StatUtilsStat extends Object
	{
		// ------------------------------
		public final StatsCounter statsCounter;
		public final Component label;
		private final String sqLabel;
		// ------------------------------
		public StatUtilsStat(StatsCounter StatsCounter, Component label)
		{
			this.statsCounter = StatsCounter;
			this.label = Objects.requireNonNull(label, "label must not be null.");
			this.sqLabel = this.label.getString().toLowerCase().replaceAll("\\s+","");
		}
		
		public abstract boolean isEmpty();
		// ------------------------------
		public final boolean matchesSearchQuery(String search)
		{
			search = search.toLowerCase().replaceAll("\\s+","");
			return search.startsWith(this.sqLabel);
		}
		// ------------------------------
	}
	// --------------------------------------------------
	public static class StatUtilsGeneralStat extends StatUtilsStat
	{
		public final Stat<ResourceLocation> stat;
		public final int intValue;
		public final MutableComponent value;
		public final boolean isEmpty;
		
		public StatUtilsGeneralStat(StatsCounter statsCounter, Stat<ResourceLocation> generalStat)
		{
			super(Objects.requireNonNull(statsCounter, "StatsCounter must not be null."),
					TextUtils.fTranslatable(
					getStatTranslationKey(
							Objects.requireNonNull(generalStat, "generalStat must not be null."))));
			this.stat = generalStat;
			this.intValue = statsCounter.getValue(stat);
			this.value = TextUtils.literal(stat.format(this.intValue));
			//define isEmpty
			this.isEmpty = (intValue == 0);
		}
		public @Override boolean isEmpty() { return this.isEmpty; }
	}
	// --------------------------------------------------
	public static class StatUtilsItemStat extends StatUtilsStat
	{
		public final Item item;
		public final Block block;
		public final int sMined, sCrafted, sUsed, sBroken, sPickedUp, sDropped;
		public final boolean isEmpty;
		
		public StatUtilsItemStat(StatsCounter statsCounter, Item item)
		{
			super(Objects.requireNonNull(statsCounter, "StatsCounter must not be null."),
					TextUtils.fTranslatable(
					Objects.requireNonNull(item, "item must not be null.")
					.getDescriptionId()));
			//define required stuff
			this.item = item;
			this.block = Block.byItem(item);
			//handle sMined
			if(block == null || block.defaultBlockState().isAir()) { sMined = 0; }
			else { this.sMined = statsCounter.getValue(Stats.BLOCK_MINED, block); }
			//handle other stats
			this.sCrafted = statsCounter.getValue(Stats.ITEM_CRAFTED, item);
			this.sUsed = statsCounter.getValue(Stats.ITEM_USED, item);
			this.sBroken = statsCounter.getValue(Stats.ITEM_BROKEN, item);
			this.sPickedUp = statsCounter.getValue(Stats.ITEM_PICKED_UP, item);
			this.sDropped = statsCounter.getValue(Stats.ITEM_DROPPED, item);
			//define isEmpty
			this.isEmpty = (sMined == 0 && sCrafted == 0 &&
					sUsed == 0 && sBroken == 0 &&
					sPickedUp == 0 && sDropped == 0);
		}
		public @Override boolean isEmpty() { return this.isEmpty; }
	}
	// --------------------------------------------------
	public static class StatUtilsMobStat extends StatUtilsStat
	{
		public final EntityType<?> entityType;
		public final int killed, killedBy;
		public final boolean isEmpty;
		
		public StatUtilsMobStat(StatsCounter StatsCounter, EntityType<?> entityType)
		{
			super(Objects.requireNonNull(StatsCounter, "StatsCounter must not be null."),
					TextUtils.fTranslatable(
							Objects.requireNonNull(entityType, "entityType must not be null.")
							.getDescriptionId()));
			//define required stuff
			this.entityType = Objects.requireNonNull(entityType, "entityType must not be null");
			this.killed = StatsCounter.getValue(Stats.ENTITY_KILLED, entityType);
			this.killedBy = StatsCounter.getValue(Stats.ENTITY_KILLED_BY, entityType);
			//define isEmpty
			this.isEmpty = (killed == 0 && killedBy == 0);
		}
		public @Override boolean isEmpty() { return this.isEmpty; }
	}
	// ==================================================
}