package thecsdev.betterstats.config;

import static thecsdev.betterstats.BetterStats.LOGGER;
import static thecsdev.betterstats.BetterStats.ModID;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

import org.apache.commons.lang3.NotImplementedException;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.registry.Registry;
import thecsdev.betterstats.BetterStats;

public class BSMobStatRenderConfig
{
	// ==================================================
	public static final HashMap<Identifier, Entry> REGISTRY = Maps.newHashMap();
	// --------------------------------------------------
	static
	{
		//internal registry entries
		put(EntityType.getId(EntityType.AXOLOTL), new Entry(new Point(10,-10)));
		put(EntityType.getId(EntityType.GHAST), new Entry(new Point(0,-25)));
		put(EntityType.getId(EntityType.SPIDER), new Entry(95));
		put(EntityType.getId(EntityType.CAVE_SPIDER), new Entry(80));
		put(EntityType.getId(EntityType.SQUID), new Entry(new Point(0,-25)));
		put(EntityType.getId(EntityType.GLOW_SQUID), new Entry(new Point(0,-25)));
		
		//some mobs appear a bit larger than they should be
		put(EntityType.getId(EntityType.PHANTOM), new Entry(60, new Point(0, -20)));
		put(EntityType.getId(EntityType.TURTLE), new Entry(70));
		
		//while some are so wide that they appear very small
		put(EntityType.getId(EntityType.ENDER_DRAGON), new Entry(300, new Point(0,-20)));
		
		//why does their model size return a large number while they appear tiny on the screen?
		//oh well, these entries will fix that.
		//oh wait, maybe it's because the following entities can expand in size? idk.
		put(EntityType.getId(EntityType.SLIME), new Entry(400));
		put(EntityType.getId(EntityType.MAGMA_CUBE), new Entry(400));
		put(EntityType.getId(EntityType.PUFFERFISH), new Entry(150));
	}
	// --------------------------------------------------
	public static class Entry
	{
		public final int MobStatGuiSize;
		public final Point MobStatGuiPosOffset;
		
		public Entry(Point msGuiPosOffset) { this(100, msGuiPosOffset); }
		public Entry(int msGuiSizeOffset) { this(msGuiSizeOffset, null); }
		public Entry(int msGuiSize, Point msGuiPosOffset)
		{
			if(msGuiPosOffset == null) msGuiPosOffset = new Point();
			this.MobStatGuiSize = msGuiSize;
			this.MobStatGuiPosOffset = msGuiPosOffset;
		}
	}
	// --------------------------------------------------
	public static Entry put(Identifier entityId, Entry entityEntry)
	{
		if(REGISTRY.containsKey(entityId))
			REGISTRY.remove(entityId);
		return REGISTRY.put(entityId, entityEntry);
	}
	// ==================================================
	public static File getPropertiesFile()
	{
		return new File(System.getProperty("user.dir") +
				"/config/" + BetterStats.ModID + "_mobStatRenderer.json");
	}
	// --------------------------------------------------
	public static void loadProperties()
	{
		String msr = BSMobStatRenderConfig.class.getSimpleName();
		try
		{
			//get and make sure the properties file exists
			File fProp = getPropertiesFile();
			if(!fProp.exists())
			{
				LOGGER.info("Could not load '" + ModID + "' config for the '" + msr + "'. File not found.");
				return;
			}
			
			//read the config
			String rawJson = Files.readString(fProp.toPath());
			loadProperties(rawJson);
		}
		catch(IOException ioExc)
		{
			String msg = "Unable to load the '" + ModID + "' mod config " + "for the '" + msr + "'.";
			throw new CrashException(new CrashReport(msg, ioExc));
		}
		catch(Exception jsonExc)
		{
			LOGGER.error("Unable to load the '" + ModID + "' mod config " +
					"for the '" + msr + "'. Is the JSON syntax is invalid?");
		}
	}
	
	public static void loadProperties(String rawJson)
	{
		String msr = BSMobStatRenderConfig.class.getSimpleName();
		try
		{
			//read the config
			JsonObject json = JsonHelper.deserialize(rawJson, true);
			
			//iterate all items
			for(String jsonKey : json.keySet())
			{
				//ignore non object entries
				JsonElement el = json.get(jsonKey);
				if(!(el instanceof JsonObject))
					continue;
				
				//read entry
				JsonObject entryJson = (JsonObject)el;
				int i0 = 100;
				Point p0 = new Point();
				
				if(entryJson.has("MobStatGuiSize"))
					i0 = entryJson.get("MobStatGuiSize").getAsInt();
				if(entryJson.has("MobStatGuiPosOffset"))
				{
					JsonArray xyArr = entryJson.get("MobStatGuiPosOffset").getAsJsonArray();
					int[] xy = { xyArr.get(0).getAsInt(), xyArr.get(1).getAsInt() };
					p0.x = xy[0];
					p0.y = xy[1];
				}
				
				//---------- implement entry
				Entry entityEntry = new Entry(i0, p0);
				boolean extendz = jsonKey.toLowerCase().startsWith("extends ");
				if(extendz) jsonKey = jsonKey.substring(8);
				Identifier entityId = new Identifier(jsonKey);
				
				if(!extendz) put(entityId, entityEntry);
				else
				{
					//obtain and create type
					EntityType<?> entityType = Registry.ENTITY_TYPE.get(entityId);
					if(!entityType.isSummonable()) continue; //has to be done
					//warning - this is global scope, avoid client-side stuff at all costs
					
					//TODO - FINISH
					throw new NotImplementedException("\"extends mod_id:mob_id\"is not implemented yet.");
				}
			}
		}
		catch(Exception jsonExc)
		{
			LOGGER.error("Unable to load the '" + ModID + "' mod config " +
					"for the '" + msr + "'. Is the JSON syntax is invalid?");
		}
	}
	// ==================================================
	public static int getLivingEntityGUISize(LivingEntity e, int viewportSize)
	{
		//null check
		final int maxVal = (int) (50 * ((float)viewportSize / 80));
		if(e == null) return maxVal;
		
		//calculate default gui size
		int result = maxVal;
		{
			//return size based on entity model size
			float f1 = e.getType().getDimensions().width, f2 = e.getType().getDimensions().height;
			double d0 = Math.sqrt((f1 * f1) + (f2 * f2));
			if(d0 == 0) d0 = 0.1;
			
			//calculate and return
			result = (int) (maxVal / d0);
		}
		
		//apply any offsets
		{
			//get entity id and entry
			Entry eEntry = REGISTRY.get(EntityType.getId(e.getType()));
			
			//apply config offsets to the result value
			if(eEntry != null)
				result *= ((float)eEntry.MobStatGuiSize / 100);
		}
		
		//return the result
		return result;
	}
	// --------------------------------------------------
	public static Point getLivingEntityGUIPos(LivingEntity e, int viewportSize)
	{
		//calculate the initial result
		Point result = new Point((viewportSize / 2), viewportSize - (int)((float)viewportSize * 0.14f));
		
		//obtain entity entry
		if(e != null)
		{
			//get entity id and entry
			Entry eEntry = REGISTRY.get(EntityType.getId(e.getType()));
			
			//offset the result
			if(eEntry != null)
			{
				result.x += (float)viewportSize * ((float)eEntry.MobStatGuiPosOffset.x / 100);
				result.y += (float)viewportSize * ((float)eEntry.MobStatGuiPosOffset.y / 100);
			}
		}
		
		//return the result
		return result;
	}
	// ==================================================
}