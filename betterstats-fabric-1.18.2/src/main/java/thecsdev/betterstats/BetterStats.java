package thecsdev.betterstats;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.EnvType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import thecsdev.betterstats.config.BSConfig;
import thecsdev.betterstats.config.BSMobStatRenderConfig;
import thecsdev.betterstats.config.BSWikiLinkConfig;
import thecsdev.betterstats.server.BetterStatsServer;

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
			throw new CrashException(new CrashReport(crashMsg, exc));
		}
		Instance = this;
		
		//log stuff
		LOGGER.info("Initializing '" + getModName() + "' as '" + getClass().getSimpleName() + "'.");
		
		//load properties
		BSConfig.loadProperties();
		BSMobStatRenderConfig.loadProperties();
		BSWikiLinkConfig.loadProperties();
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
	public static Text tt(String translationKey, Object... params) { return new TranslatableText(translationKey, params); }
	public static Text lt(Object text) { return new LiteralText(Objects.toString(text)); }
	// ==================================================
	public static String getModName() { return ModName; }
	public static String getModID() { return ModID; }
	// ==================================================
}