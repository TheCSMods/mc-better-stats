package io.github.thecsdev.betterstats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.thecsdev.betterstats.network.BetterStatsNetworkHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public class BetterStats extends Object
{
	// ==================================================
	public static final Logger LOGGER = LoggerFactory.getLogger(getModID());
	// --------------------------------------------------
	private static final String ModID = "betterstats";
	private static BetterStats Instance;
	// --------------------------------------------------
	public final ModContainer modInfo;
	// ==================================================
	/**
	 * Initializes this mod. This action may only be performed by the fabric-loader.
	 */
	public BetterStats()
	{
		//validate instance first
		if(isModInitialized())
			throw new IllegalStateException(getModID() + " has already been initialized.");
		else if(!isInstanceValid(this))
			throw new UnsupportedOperationException("Invalid " + getModID() + " type: " + this.getClass().getName());
		
		//assign instance
		Instance = this;
		modInfo = FabricLoader.getInstance().getModContainer(getModID()).get();
		
		//log stuff
		LOGGER.info("Initializing '" + getModName() + "' as '" + getClass().getSimpleName() + "'.");

		//init stuff
		BetterStatsNetworkHandler.init();
	}
	// --------------------------------------------------
	/** Returns the Fabric {@link ModContainer} containing information about this mod. */
	public ModContainer getModInfo() { return modInfo; }
	// ==================================================
	/** Returns the instance of this mod. */
	public static BetterStats getInstance() { return Instance; }
	// --------------------------------------------------
	public static String getModName() { return getInstance().getModInfo().getMetadata().getName(); }
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