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
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class StatUtils
{
	// ==================================================
	private StatUtils() {}
	// ==================================================
	/**
	 * Returns a list of all {@link StatUtilsGeneralStat}s and their values.
	 * @param statHandler The {@link StatHandler} that contains all the {@link Stat} data.
	 * @param filter (optional) Make it return false to exclude an {@link StatUtilsGeneralStat}.
	 */
	public static ArrayList<StatUtilsGeneralStat> getGeneralStats(StatHandler statHandler, Predicate<StatUtilsStat> filter)
	{
		//create a new list
		ArrayList<StatUtilsGeneralStat> result = Lists.newArrayList();
		Objects.requireNonNull(statHandler, "statHandler must not be null.");
		
		//obtain and sort all general (aka custom) stats
		ObjectArrayList<Stat<Identifier>> statiList = new ObjectArrayList<Stat<Identifier>>(Stats.CUSTOM.iterator());
	    statiList.sort(Comparator.comparing(stat -> TextUtils.translatable(getStatTranslationKey(stat)).getString()));
	    
	    //iterate and add all general stats
	    for (ObjectListIterator<Stat<Identifier>> statListIterator = statiList.iterator(); statListIterator.hasNext();)
	    	result.add(new StatUtilsGeneralStat(statHandler, statListIterator.next()));
	    
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
	 * @param statHandler The {@link StatHandler} that contains all the {@link Stat} data.
	 * @param filter (optional) Make it return false to exclude an {@link StatUtilsItemStat}.
	 */
	public static LinkedHashMap<ItemGroup, ArrayList<StatUtilsItemStat>> getItemStats(StatHandler statHandler, Predicate<StatUtilsStat> filter)
	{
		//create new map
		LinkedHashMap<ItemGroup, ArrayList<StatUtilsItemStat>> result = Maps.newLinkedHashMap();
		result.put(null, new ArrayList<>()); //the null category goes first
		
		//iterate and group all items
		for(Item itemReg : Registry.ITEM)
		{
			//create item stat
			StatUtilsItemStat itemStat = new StatUtilsItemStat(statHandler, itemReg);
			
			//filter search
			if(filter != null && !filter.test(itemStat)) continue;
					
			//group item
			if(!result.containsKey(itemReg.getGroup()))
				result.put(itemReg.getGroup(), Lists.newArrayList());
			result.get(itemReg.getGroup()).add(itemStat);
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
	 * @param statHandler The {@link StatHandler} that contains all the {@link Stat} data.
	 * @param filter (optional) Make it return false to exclude an {@link StatUtilsItemStat}. 
	 */
	public static LinkedHashMap<String, ArrayList<StatUtilsMobStat>> getMobStats(StatHandler statHandler, Predicate<StatUtilsStat> filter)
	{
		//create a new list
		LinkedHashMap<String, ArrayList<StatUtilsMobStat>> result = Maps.newLinkedHashMap();
		
		//'minecraft' goes first
		String mcModId = new Identifier("air").getNamespace();
		result.put(mcModId, Lists.newArrayList());
		
		//iterate all entities
		for(EntityType<?> entityType : Registry.ENTITY_TYPE)
		{
			//(system) filter
			if(entityType != EntityType.PLAYER &&
					(!entityType.isSummonable() || entityType.getSpawnGroup() == SpawnGroup.MISC))
				continue;
			
			//create the mob stat
			StatUtilsMobStat mobStat = new StatUtilsMobStat(statHandler, entityType);
			
			//filter
			if(filter != null && !filter.test(mobStat))
				continue;
			
			//obtain entity's mod id,
			//obtain the result list, and
			//put the entity in the result list
			String entityModId = EntityType.getId(entityType).getNamespace();
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
	 * Same as {@link #getItemStats(StatHandler, Predicate)}, but
	 * the items are grouped by mod IDs, and not by {@link ItemGroup}s.<br/>
	 * <br/>
	 * <b>Note:</b><br/>
	 * The {@link LinkedHashMap} keys are IDs of mods. To convert a mod id
	 * to mod name, use {@link #getModName(String)}.
	 * @param statHandler The {@link StatHandler} that contains all the {@link Stat} data.
	 * @param filter (optional) Make it return false to exclude an {@link StatUtilsItemStat}.
	 */
	public static LinkedHashMap<String, ArrayList<StatUtilsItemStat>> getItemStatsByMods(StatHandler statHandler, Predicate<StatUtilsStat> filter)
	{
		//create a new list
		LinkedHashMap<String, ArrayList<StatUtilsItemStat>> result = Maps.newLinkedHashMap();
		
		//'minecraft' goes first
		String mcModId = new Identifier("air").getNamespace();
		result.put(mcModId, Lists.newArrayList());
		
		//iterate all items and add them to the map
		for(Item itemReg : Registry.ITEM)
		{
			//create item stat
			StatUtilsItemStat itemStat = new StatUtilsItemStat(statHandler, itemReg);
			
			//filter search
			if(filter != null && !filter.test(itemStat)) continue;
			
			//---------- group the item
			//obtain mod id
			String itemModId = Registry.ITEM.getId(itemReg).getNamespace();
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
	public static String getStatTranslationKey(Stat<Identifier> stat)
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
		var container = FabricLoader.getInstance().getModContainer(modId);
		if(container.isPresent()) return container.get().getMetadata().getName();
		else return modId;
	}
	// ==================================================
	public static abstract class StatUtilsStat extends Object
	{
		// ------------------------------
		public final StatHandler statHandler;
		public final Text label;
		private final String sqLabel;
		// ------------------------------
		public StatUtilsStat(StatHandler statHandler, Text label)
		{
			this.statHandler = statHandler;
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
		public final Stat<Identifier> stat;
		public final int intValue;
		public final MutableText value;
		public final boolean isEmpty;
		
		public StatUtilsGeneralStat(StatHandler statHandler, Stat<Identifier> generalStat)
		{
			super(Objects.requireNonNull(statHandler, "statHandler must not be null."),
					TextUtils.fTranslatable(
					getStatTranslationKey(
							Objects.requireNonNull(generalStat, "generalStat must not be null."))));
			this.stat = generalStat;
			this.intValue = statHandler.getStat(stat);
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
		
		public StatUtilsItemStat(StatHandler statHandler, Item item)
		{
			super(Objects.requireNonNull(statHandler, "statHandler must not be null."),
					TextUtils.fTranslatable(
					Objects.requireNonNull(item, "item must not be null.")
					.getTranslationKey()));
			//define required stuff
			this.item = item;
			this.block = Block.getBlockFromItem(item);
			//handle sMined
			if(block == null || block.getDefaultState().isAir()) { sMined = 0; }
			else { this.sMined = statHandler.getStat(Stats.MINED, block); }
			//handle other stats
			this.sCrafted = statHandler.getStat(Stats.CRAFTED, item);
			this.sUsed = statHandler.getStat(Stats.USED, item);
			this.sBroken = statHandler.getStat(Stats.BROKEN, item);
			this.sPickedUp = statHandler.getStat(Stats.PICKED_UP, item);
			this.sDropped = statHandler.getStat(Stats.DROPPED, item);
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
		
		public StatUtilsMobStat(StatHandler statHandler, EntityType<?> entityType)
		{
			super(Objects.requireNonNull(statHandler, "statHandler must not be null."),
					TextUtils.fTranslatable(
							Objects.requireNonNull(entityType, "entityType must not be null.")
							.getTranslationKey()));
			//define required stuff
			this.entityType = Objects.requireNonNull(entityType, "entityType must not be null");
			this.killed = statHandler.getStat(Stats.KILLED, entityType);
			this.killedBy = statHandler.getStat(Stats.KILLED_BY, entityType);
			//define isEmpty
			this.isEmpty = (killed == 0 && killedBy == 0);
		}
		public @Override boolean isEmpty() { return this.isEmpty; }
	}
	// ==================================================
}