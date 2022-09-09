package thecsdev.betterstats.client.gui.util;

import static thecsdev.betterstats.config.BSConfig.IGNORE_ENTITY_RENDER_ERRORS;
import static thecsdev.betterstats.BetterStats.lt;
import static thecsdev.betterstats.BetterStats.tt;
import static thecsdev.betterstats.client.BetterStatsClient.MCClient;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.StatType;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import thecsdev.betterstats.config.BSConfig;

public class StatUtils
{
	// ==================================================
	@SuppressWarnings("unchecked")
	public static final ArrayList<StatType<Item>> ItemStatTypes = Lists.newArrayList(new StatType[] { Stats.BROKEN, Stats.CRAFTED, Stats.USED, Stats.PICKED_UP, Stats.DROPPED });
	// ==================================================
	public static final Function<SUItemStat, Boolean> ITEM_STAT_EMPTY_FILTER = arg0 ->
			BSConfig.DEBUG_SHOW_EVERYTHING ||
			arg0.crafted != 0 || arg0.used != 0 || arg0.broken != 0 ||
			arg0.pickedUp != 0 || arg0.dropped != 0;
	
	public static final Function<SUMobStat, Boolean> MOB_STAT_EMPTY_FILTER = arg0 ->
			BSConfig.DEBUG_SHOW_EVERYTHING ||
			arg0.killed != 0 || arg0.killedBy != 0;
	// ==================================================
	public static ArrayList<SUGeneralStat> getGeneralStats(StatHandler statHandler)
	{
		//create a new list
		ArrayList<SUGeneralStat> result = Lists.newArrayList();
		
		//iterate all stats
		ObjectArrayList<Stat<Identifier>> statiList = new ObjectArrayList<Stat<Identifier>>(Stats.CUSTOM.iterator());
	    statiList.sort(Comparator.comparing(stat -> I18n.translate(getStatTranslationKey(stat), new Object[0])));
	    for (ObjectListIterator<Stat<Identifier>> statListIterator = statiList.iterator(); statListIterator.hasNext();)
	    {
	    	//get next stat
			Stat<Identifier> stat = statListIterator.next();
			int val = statHandler.getStat(stat);
			
			//put text
			result.add(new SUGeneralStat(
					tt(getStatTranslationKey(stat)),
					lt(stat.format(val)),
					val));
	    }
		
		//return result
		return result;
	}
	// --------------------------------------------------
	public static LinkedHashMap<ItemGroup, ArrayList<SUItemStat>> getItemStats(StatHandler statHandler, String searchQuery)
	{
		return getItemStats(statHandler, arg0 ->
		{
			//filter search
			boolean b0 = StringUtils.isAllBlank(searchQuery) ||
					StringUtils.containsIgnoreCase(tt(arg0.item.getTranslationKey()).getString(), searchQuery);
			
			//return
			return b0 && ITEM_STAT_EMPTY_FILTER.apply(arg0);
		});
	}
	
	public static LinkedHashMap<ItemGroup, ArrayList<SUItemStat>> getItemStats(StatHandler statHandler, Function<SUItemStat, Boolean> filter)
	{
		//create new map
		LinkedHashMap<ItemGroup, ArrayList<SUItemStat>> result = Maps.newLinkedHashMap();
		result.put(null, new ArrayList<>()); //the null category goes first
		
		//iterate and group all items
		for(Item itemReg : Registry.ITEM)
		{
			//create item stat
			SUItemStat itemStat = new SUItemStat(statHandler, itemReg);
			
			//filter search
			if(!filter.apply(itemStat)) continue;
						
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
	public static LinkedHashMap<String, ArrayList<SUMobStat>> getMobStats(StatHandler statHandler, String searchQuery)
	{
		return getMobStats(statHandler, arg0 ->
		{
			//filter search
			String entityName = tt(arg0.entityType.getTranslationKey()).getString();
			boolean b0 = StringUtils.isAllBlank(searchQuery) ||
					StringUtils.containsIgnoreCase(entityName, searchQuery);
			
			//return
			return b0 && MOB_STAT_EMPTY_FILTER.apply(arg0);
		});
	}
	
	public static LinkedHashMap<String, ArrayList<SUMobStat>> getMobStats(StatHandler statHandler, Function<SUMobStat, Boolean> filter)
	{
		//create a new list
		LinkedHashMap<String, ArrayList<SUMobStat>> result = Maps.newLinkedHashMap();
		
		//'minecraft' goes first
		String mcModId = new Identifier("air").getNamespace();
		result.put(mcModId, Lists.newArrayList());
		
		//iterate all entities
		for(EntityType<?> entityType : Registry.ENTITY_TYPE)
		{
			//create the mob stat
			SUMobStat mobStat = new SUMobStat(statHandler, entityType);
			
			//filter
			if(!filter.apply(mobStat) || !(mobStat.entity instanceof LivingEntity))
				continue;
			
			//obtain entity's mod id,
			//obtain the result list, and
			//put the entity in the result list
			String entityModId = EntityType.getId(entityType).getNamespace();
			if(!result.containsKey(entityModId))
				result.put(entityModId, Lists.newArrayList());
			ArrayList<SUMobStat> resultList = result.get(entityModId);
			resultList.add(mobStat);
		}
		
		//make sure 'minecraft' actually has entries
		if(result.get(mcModId).size() < 1)
			result.remove(mcModId);
		
		//return the result
		return result;
	}
	// --------------------------------------------------
	public static LinkedHashMap<String, ArrayList<SUItemStat>> getFoodStats(StatHandler statHandler, Function<SUItemStat, Boolean> filter)
	{
		//create a new list
		LinkedHashMap<String, ArrayList<SUItemStat>> result = Maps.newLinkedHashMap();
		
		//'minecraft' goes first
		String mcModId = new Identifier("a").getNamespace();
		result.put(mcModId, Lists.newArrayList());
		
		//iterate all registered items and look for food items
		for(Item itemReg : Registry.ITEM)
		{
			//create item stat
			SUItemStat itemStat = new SUItemStat(statHandler, itemReg);
			
			//filter
			if(!itemReg.isFood() || !filter.apply(itemStat))
				continue;
			
			//group the item by it's mod id
			String itemModId = itemStat.itemId.getNamespace();
			if(!result.containsKey(itemModId))
				result.put(itemModId, Lists.newArrayList());
			ArrayList<SUItemStat> resultList = result.get(itemModId);
			resultList.add(itemStat);
		}
		
		//make sure 'minecraft' actually has entries
		if(result.get(mcModId).size() < 1)
			result.remove(mcModId);
		
		//return the result
		return result;
	}
	// ==================================================
	public static String getStatTranslationKey(Stat<Identifier> stat)
	{
		return "stat." + stat.getValue().toString().replace(':', '.');
	}
	// --------------------------------------------------
	public static String getModNameFromID(String modId)
	{
		//get the fabric loader
		FabricLoader fl = FabricLoader.getInstance();
		
		//check if the mod exists and return the mod display name
		if(fl.isModLoaded(modId))
			return fl.getModContainer(modId).get().getMetadata().getName();
		else return modId;
	}
	// ==================================================
	public static String getBSConfigPropertyName(Class<?> clazz, Field property)
	{
		String key = "betterstats.config." + clazz.getSimpleName() + "." + property.getName();
		String name = tt(key).getString();
		
		if(name.startsWith("ref="))
			name = tt(name.substring(4)).getString();
		
		if(StringUtils.isAllBlank(name) || name.equals(key))
			name = property.getName();
		
		return name;
	}
	
	public static String getBSConfigPropertyTooltip(Class<?> clazz, Field property)
	{
		String key = "betterstats.config." + clazz.getSimpleName() + "." + property.getName() + ".tooltip";
		String name = tt(key).getString();
		
		if(name.startsWith("ref="))
			name = tt(name.substring(4)).getString();
		
		if(StringUtils.isAllBlank(name) || name.equals(key))
			name = "";
		
		return property.getName() + (!StringUtils.isAllBlank(name) ? ("\n" + name) : "");
	}
	// ==================================================
	public static class SUGeneralStat
	{
		public final Text title;
		public final Text txtValue;
		public final int intValue;
		
		public SUGeneralStat(Text title, Text value, int intValue)
		{
			this.title = title;
			this.txtValue = value;
			this.intValue = intValue;
		}
	}
	
	public static class SUItemStat
	{
		public final Item item;
		public final ItemStack itemStack;
		public final Identifier itemId;
		
		public Integer mined;
		public final int crafted, used, broken, pickedUp, dropped;
		
		public SUItemStat(StatHandler statHandler, Item item)
		{
			Block block = Block.getBlockFromItem(item);
			this.item = item;
			this.itemStack = item.getDefaultStack();
			this.itemId = Registry.ITEM.getId(item);
			
			if(block != null && !block.getDefaultState().isAir())
				this.mined = Integer.valueOf(statHandler.getStat(Stats.MINED, block));
			else this.mined = null;
			
			this.crafted = statHandler.getStat(Stats.CRAFTED, item);
			this.used = statHandler.getStat(Stats.USED, item);
			this.broken = statHandler.getStat(Stats.BROKEN, item);
			this.pickedUp = statHandler.getStat(Stats.PICKED_UP, item);
			this.dropped = statHandler.getStat(Stats.DROPPED, item);
		}
	}
	
	public static class SUMobStat
	{
		public final Entity entity;
		public final String entityName;
		public final EntityType<?> entityType;
		
		public final int killed, killedBy;
		public boolean errored = false;
		
		public SUMobStat(StatHandler statHandler, EntityType<?> entityType)
		{
			if(entityType != EntityType.PLAYER)
			{
				//"summon" and assign the entity (entity may not be null!)
				this.entity = entityType.isSummonable() ?
						safelyCreateEntity(entityType) : //handle summonable entities
						EntityType.MARKER.create(MCClient.world); //handle non-summonable entities
				
				//handle dinnerbone mode
				try
				{
					if(BSConfig.DEBUG_DINNERBONE_MODE)
						this.entity.setCustomName(lt("Dinnerbone"));
				}
				catch(Exception e) {}
				
				//discard the entity so it doesn't take up ram for no reason
				safelyDiscardEntity(entity);
				
			}
			else this.entity = MCClient.player;
			
			this.entityType = entityType;
			this.entityName = tt(entityType.getTranslationKey()).getString();
			this.killed = statHandler.getStat(Stats.KILLED, entityType);
			this.killedBy = statHandler.getStat(Stats.KILLED_BY, entityType);
		}
		
		public Entity safelyCreateEntity(EntityType<?> et)
		{
			if(et == null) return EntityType.MARKER.create(MCClient.world);
			try { return et.create(MCClient.world); }
			catch(Exception exc)
			{
				if(!IGNORE_ENTITY_RENDER_ERRORS) throw exc;
				errored = true;
				return EntityType.MARKER.create(MCClient.world);
			}
		}
		
		public void safelyDiscardEntity(Entity entity)
		{
			if(entity == null) return;
			try { entity.discard(); }
			catch(Exception exc)
			{
				if(!IGNORE_ENTITY_RENDER_ERRORS) throw exc;
				errored = true;
			}
		}
	}
	// ==================================================
}