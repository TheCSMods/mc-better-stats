package io.github.thecsdev.betterstats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.thecsdev.betterstats.command.StatisticsCommand;
import io.github.thecsdev.betterstats.network.BetterStatsNetworkHandler;
import io.github.thecsdev.tcdcommons.api.events.server.command.CommandManagerEvent;
import net.fabricmc.loader.api.FabricLoader;

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
	//protected final ModContainer modInfo; -- avoid platform-specific APIs
	//TODO - When porting to Forge, just replace Fabric API calls with Forge ones, or directly declare URLs if impossible
	public static final String URL_SOURCES, URL_ISSUES, URL_CURSEFORGE, URL_MODRINTH;
	public static final String URL_YOUTUBE, URL_KOFI, URL_DISCORD;
	static
	{
		final var modInfo = FabricLoader.getInstance().getModContainer(ModID).get();
		URL_SOURCES = modInfo.getMetadata().getContact().get("sources").get();
		URL_ISSUES  = modInfo.getMetadata().getContact().get("issues").get();
		final var modMenuLinks = modInfo.getMetadata().getCustomValue("modmenu").getAsObject().get("links").getAsObject();
		URL_CURSEFORGE = modMenuLinks.get("modmenu.curseforge").getAsString();
		URL_MODRINTH   = modMenuLinks.get("modmenu.modrinth").getAsString();
		URL_YOUTUBE    = modMenuLinks.get("modmenu.youtube").getAsString();
		URL_KOFI       = modMenuLinks.get("modmenu.kofi").getAsString();
		URL_DISCORD    = modMenuLinks.get("modmenu.discord").getAsString();
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
		/*LOGGER.info("Initializing '" + getModName() + "' " + modInfo.getMetadata().getVersion() +
				" as '" + getClass().getSimpleName() + "'.");*/
		LOGGER.info("Initializing '" + getModName() + "' as '" + getClass().getSimpleName() + "'.");
		
		//try to load config (IOException-s should be ignored)
		this.config = new BetterStatsConfig(getModID());
		this.config.tryLoadFromFile(true);
		
		//init stuff
		BetterStatsNetworkHandler.init();
		
		//register common-side commands
		CommandManagerEvent.COMMAND_REGISTRATION_CALLBACK.register((dispatcher, commandRegAccess, regEnv) ->
		{
			StatisticsCommand.register(dispatcher, commandRegAccess);
		});
	}
	// ==================================================
	public static BetterStats getInstance() { return Instance; }
	//public ModContainer getModInfo() { return modInfo; }
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