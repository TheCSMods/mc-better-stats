package io.github.thecsdev.betterstats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.thecsdev.tcdcommons.TCDCommons;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(BetterStats.ModID)
public class BetterStats extends Object
{
	// ==================================================
	public static final Logger LOGGER = LoggerFactory.getLogger(getModID());
	// --------------------------------------------------
	public static final String ModID = "betterstats";
	private static BetterStats Instance;
	// --------------------------------------------------
	public final ModContainer ModInfo;
	// ==================================================
	/**
	 * Initializes this mod. This action may only be performed by the fabric-loader.
	 */
	public BetterStats()
	{
		ModInfo = ModList.get().getModContainerById(ModID).get();
		
		//validate instance first
		if(isModInitialized())
			throw new IllegalStateException(getModID() + " has already been initialized.");
		
		//on initialize
		if(getClass().equals(BetterStats.class)) //check if not a subclass
		{
			//depending on the side, initialize NoUnusedChunks
			if(FMLEnvironment.dist.isClient())
				new io.github.thecsdev.betterstats.client.BetterStatsClient();
			else if(FMLEnvironment.dist.isDedicatedServer())
				new io.github.thecsdev.betterstats.server.BetterStatsServer();
			else
				TCDCommons.crash("Attempting to initialize " + ModID, new RuntimeException("Invalid FMLEnvironment.dist()"));
			
			//do not proceed, return
			return;
		}
		
		//assign instance
		Instance = this;
		
		//log stuff
		LOGGER.info("Initializing '" + getModName() + "' as '" + getClass().getSimpleName() + "'.");
	}
	// --------------------------------------------------
	/** Returns the Fabric {@link ModContainer} containing information about this mod. */
	public ModContainer getModInfo() { return ModInfo; }
	// ==================================================
	/** Returns the instance of this mod. */
	public static BetterStats getInstance() { return Instance; }
	// --------------------------------------------------
	public static String getModName() { return getInstance().getModInfo().getModInfo().getDisplayName(); }
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