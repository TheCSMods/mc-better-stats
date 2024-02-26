package io.github.thecsdev.betterstats;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import io.github.thecsdev.betterstats.command.StatisticsCommand;
import io.github.thecsdev.betterstats.network.BetterStatsNetworkHandler;
import io.github.thecsdev.tcdcommons.TCDCommons;
import io.github.thecsdev.tcdcommons.api.events.server.command.CommandManagerEvent;
import io.github.thecsdev.tcdcommons.command.PlayerBadgeCommand;
import io.github.thecsdev.tcdcommons.util.io.http.TcdWebApi;

public class BetterStats extends Object
{
	// ==================================================
	public static final Logger LOGGER = LoggerFactory.getLogger(getModID());
	// --------------------------------------------------
	private static final String ModName = "Better Statistics Screen";
	private static final String ModID = "betterstats";
	private static BetterStats Instance;
	// --------------------------------------------------
	protected final BetterStatsConfig config;
	// --------------------------------------------------
	private static final JsonObject MOD_PROPERTIES;
	public static final String URL_SOURCES, URL_ISSUES, URL_CURSEFORGE, URL_MODRINTH;
	public static final String URL_YOUTUBE, URL_KOFI, URL_DISCORD;
	static
	{
		//read the mod properties resource file
		try
		{
			final var propertiesStream = BetterStats.class.getResourceAsStream("/properties.json");
			final var propertiesJsonStr = new String(propertiesStream.readAllBytes());
			propertiesStream.close();
			MOD_PROPERTIES = TcdWebApi.GSON.fromJson(propertiesJsonStr, JsonObject.class);
		}
		catch(Exception e) { throw new ExceptionInInitializerError(e); }
		
		//read links
		final var links = MOD_PROPERTIES.get("links").getAsJsonObject();
		URL_SOURCES    = links.get("sources").getAsString();
		URL_ISSUES     = links.get("issues").getAsString();
		URL_CURSEFORGE = links.get("curseforge").getAsString();
		URL_MODRINTH   = links.get("modrinth").getAsString();
		URL_YOUTUBE    = links.get("youtube").getAsString();
		URL_KOFI       = links.get("kofi").getAsString();
		URL_DISCORD    = links.get("discord").getAsString();
	}
	// ==================================================
	public BetterStats()
	{
		//validate instance first
		if(isModInitialized())
			throw new IllegalStateException(getModID() + " has already been initialized.");
		else if(!isInstanceValid(this))
			throw new UnsupportedOperationException("Invalid " + getModID() + " type: " + this.getClass().getName());
		
		//assign instance
		Instance = this;
		//modInfo = FabricLoader.getInstance().getModContainer(getModID()).get();
		
		//log stuff
		LOGGER.info("Initializing '" + getModName() + "' as '" + getClass().getSimpleName() + "'.");
		
		//load config
		this.config = new BetterStatsConfig(getModID());
		this.config.loadFromFileOrCrash(true);
		
		//init stuff
		BetterStatsNetworkHandler.init();
		
		//register commands
		CommandManagerEvent.COMMAND_REGISTRATION_CALLBACK.register((dispatcher, commandRegAccess, regEnv) ->
		{
			//check the config property 
			if(!getConfig().registerCommands) return;
			
			//register commands
			StatisticsCommand.register(dispatcher, commandRegAccess);
			if(TCDCommons.getInstance().getConfig().enablePlayerBadges)
				PlayerBadgeCommand.register(dispatcher);
		});
	}
	// ==================================================
	public static BetterStats getInstance() { return Instance; }
	public static @Internal JsonObject getModProperties() { return MOD_PROPERTIES; }
	public BetterStatsConfig getConfig() { return this.config; }
	// --------------------------------------------------
	public static String getModName() { return ModName; }
	public static String getModID() { return ModID; }
	// --------------------------------------------------
	public static boolean isModInitialized() { return isInstanceValid(Instance); }
	private static boolean isInstanceValid(BetterStats instance) { return isServer(instance) || isClient(instance); }
	// --------------------------------------------------
	public static boolean isServer() { return isServer(Instance); }
	public static boolean isClient() { return isClient(Instance); }
	
	private static boolean isServer(BetterStats arg0) { return arg0 instanceof io.github.thecsdev.betterstats.server.BetterStatsServer; }
	private static boolean isClient(BetterStats arg0) { return arg0 instanceof io.github.thecsdev.betterstats.client.BetterStatsClient; }
	// ==================================================
}