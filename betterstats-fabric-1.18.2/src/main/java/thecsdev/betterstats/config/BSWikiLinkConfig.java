package thecsdev.betterstats.config;

import static thecsdev.betterstats.BetterStats.LOGGER;
import static thecsdev.betterstats.BetterStats.ModID;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import thecsdev.betterstats.BetterStats;

public class BSWikiLinkConfig
{
	// ==================================================
	public static final String FandomWikiSite = "https://minecraft.fandom.com/wiki/"/*+ item_id*/;
	public static final HashMap<String, Entry> REGISTRY = Maps.newHashMap();
	// --------------------------------------------------
	static
	{
		put(new Identifier("air").getNamespace(), new Entry(FandomWikiSite));
	}
	// --------------------------------------------------
	public static enum WikiType { MobWiki, ItemWiki }
	public static class Entry
	{
		public final String MobWikiHandler;
		public final String ItemWikiHandler;
		
		public Entry(String wikiHandlerUrl) { this(wikiHandlerUrl, wikiHandlerUrl); }
		public Entry(String mobWikiHandler, String itemWikiHandler)
		{
			if(!mobWikiHandler.endsWith("/")) mobWikiHandler += "/";
			if(!itemWikiHandler.endsWith("/")) itemWikiHandler += "/";
			this.MobWikiHandler = mobWikiHandler;
			this.ItemWikiHandler = itemWikiHandler;
		}
		
		public String getWikiUrl(WikiType type, String mobOrItemId)
		{
			switch (type)
			{
				case MobWiki: return MobWikiHandler + mobOrItemId;
				case ItemWiki: return ItemWikiHandler + mobOrItemId;
				default: return FandomWikiSite + mobOrItemId;
			}
		}
		
		/** Returns true if nothing goes wrong while opening the URL. */
		public boolean openWiki(WikiType type, String mobOrItemId)
		{
			try
			{
				Util.getOperatingSystem().open(getWikiUrl(type, mobOrItemId));
				return true;
			}
			catch(Exception e) { return false; }
		}
	}
	// --------------------------------------------------
	public static Entry put(String modId, Entry entry)
	{
		if(REGISTRY.containsKey(modId))
			REGISTRY.remove(modId);
		return REGISTRY.put(modId, entry);
	}
	// ==================================================
	public static File getPropertiesFile()
	{
		return new File(System.getProperty("user.dir") +
				"/config/" + BetterStats.ModID + "_wikiLinks.json");
	}
	// --------------------------------------------------
	public static void loadProperties()
	{
		String msr = BSWikiLinkConfig.class.getSimpleName();
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
				String mobWiki = FandomWikiSite;
				String itemWiki = FandomWikiSite;
				
				if(entryJson.has("WikiHandler"))
				{
					mobWiki = entryJson.get("WikiHandler").getAsString();
					itemWiki = entryJson.get("WikiHandler").getAsString();
				}
				if(entryJson.has("MobWikiHandler")) mobWiki = entryJson.get("MobWikiHandler").getAsString();
				if(entryJson.has("ItemWikiHandler")) mobWiki = entryJson.get("ItemWikiHandler").getAsString();
				
				//put entry
				put(jsonKey, new Entry(mobWiki, itemWiki));
			}
		}
		catch(Exception jsonExc)
		{
			LOGGER.error("Unable to load the '" + ModID + "' mod config " +
					"for the '" + msr + "'. Is the JSON syntax is invalid?");
		}
	}
	// ==================================================
	public static boolean canOpenUrl(Identifier mobOrItemId, WikiType wikiType)
	{
		boolean b0 = mobOrItemId != null && wikiType != null;
		boolean b1 = REGISTRY.get(mobOrItemId.getNamespace()) != null;
		boolean b2 = BSConfig.ENABLE_WIKI_LINKS;
		boolean b3 = BetterStats.isClient();
		return b0 && b1 && b2 && b3;
	}
	
	public static boolean openUrl(final Identifier mobOrItemId, final WikiType wikiType)
	{
		if(!canOpenUrl(mobOrItemId, wikiType)) return false;
		
		try
		{
			final Entry entry = REGISTRY.get(mobOrItemId.getNamespace());
			if(entry == null) return false;
			
			entry.openWiki(wikiType, mobOrItemId.getPath());
			return true;
		}
		catch(Exception exc) { return false; }
	}
	
	public static String getUrl(Identifier mobOrItemId, WikiType wikiType, String defaultValue)
	{
		Entry entry = REGISTRY.get(mobOrItemId.getNamespace());
		if(entry == null) return defaultValue;
		return entry.getWikiUrl(wikiType, mobOrItemId.getPath());
	}
	// ==================================================
}