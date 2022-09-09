package thecsdev.betterstats.config;

import java.util.HashMap;

import com.google.common.collect.Maps;

import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

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
	public static boolean canOpenUrl(Identifier mobOrItemId, WikiType wikiType)
	{
		boolean b0 = mobOrItemId != null && wikiType != null;
		boolean b1 = REGISTRY.get(mobOrItemId.getNamespace()) != null;
		boolean b2 = BSConfig.ENABLE_WIKI_LINKS;
		return b0 && b1 && b2;
	}
	
	public static boolean openUrl(Identifier mobOrItemId, WikiType wikiType)
	{
		if(!canOpenUrl(mobOrItemId, wikiType)) return false;
		return REGISTRY.get(mobOrItemId.getNamespace()).openWiki(wikiType, mobOrItemId.getPath());
	}
	// ==================================================
}