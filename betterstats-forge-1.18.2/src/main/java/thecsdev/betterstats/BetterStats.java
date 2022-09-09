package thecsdev.betterstats;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.CrashReport;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import thecsdev.betterstats.client.BetterStatsClient;
import thecsdev.betterstats.config.BSConfig;
import thecsdev.betterstats.config.BSMobStatRenderConfig;
import thecsdev.betterstats.server.BetterStatsServer;
import thecsdev.fabric2forge.bss_p758.fabricapi.EnvType;
import thecsdev.fabric2forge.bss_p758.text.LiteralText;
import thecsdev.fabric2forge.bss_p758.text.TranslatableText;
import thecsdev.fabric2forge.bss_p758.util.crash.CrashException;

@Mod(BetterStats.ModID)
public abstract class BetterStats
{
	// ==================================================
	private static BetterStats Instance;
	// --------------------------------------------------
	public static final Logger LOGGER = LoggerFactory.getLogger(getModID());
	// --------------------------------------------------
	public static final String ModName = "Better Statistics Screen";
	public static final String ModID   = "betterstats";
	public static final String FeedbackSite = "https://github.com/TheCSDev/mc-better-stats";
	// ==================================================
	public BetterStats()
	{
		//validate and assign instance
		if(validateInstance())
		{
			String crashMsg = "Attempting to initialize " + ModID;
			RuntimeException exc = new RuntimeException(ModID + " has already been initialized.");
			crash(crashMsg, exc);
		}
		
		//on initialize
		if(getClass().equals(BetterStats.class)) //check if not a subclass
		{
			//depending on the side, initialize NoUnusedChunks
			if(FMLEnvironment.dist.isClient()) new BetterStatsClient().onInitializeClient();
			else if(FMLEnvironment.dist.isDedicatedServer()) new BetterStatsServer().onInitializeServer();
			else crash("Attempting to initialize " + ModID, new RuntimeException("Invalid FMLEnvironment.dist()"));
			
			//do not proceed, return
			return;
		}
		
		Instance = this;
		
		//log stuff
		LOGGER.info("Initializing '" + getModName() + "' as '" + getClass().getSimpleName() + "'.");
		
		//load properties
		BSConfig.loadProperties();
		BSMobStatRenderConfig.loadProperties();
	}
	
	public static boolean validateInstance()
	{
		if(Instance != null &&
				(Instance instanceof thecsdev.betterstats.client.BetterStatsClient ||
						Instance instanceof BetterStatsServer))
			return true;
		else return false;
	}
	// --------------------------------------------------
	public static void crash(String crashMessage, Throwable exception)
	{
		CrashReport crashReport = CrashReport.forThrowable(exception, crashMessage);
		throw new CrashException(crashReport);
	}
	// --------------------------------------------------
	public static EnvType getEnviroment()
	{
		//validate
		if(!validateInstance())
			throw new RuntimeException(ModID + " is unitialized.");
		//return based on instance type
		if(Instance instanceof thecsdev.betterstats.client.BetterStatsClient) return EnvType.CLIENT;
		else return EnvType.SERVER;
	}
	public static boolean isClient() { return getEnviroment() == EnvType.CLIENT; }
	public static boolean isServer() { return getEnviroment() == EnvType.SERVER; }
	// ==================================================
	public static Component tt(String translationKey, Object... params) { return new TranslatableText(translationKey, params); }
	public static Component lt(Object text) { return new LiteralText(Objects.toString(text)); }
	// ==================================================
	public static String getModName() { return ModName; }
	public static String getModID() { return ModID; }
	// ==================================================
}