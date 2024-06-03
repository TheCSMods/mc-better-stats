package io.github.thecsdev.betterstats;

import io.github.thecsdev.betterstats.util.stats.SASConfig;
import io.github.thecsdev.tcdcommons.api.config.AutoConfig;
import io.github.thecsdev.tcdcommons.api.config.annotation.NonSerialized;
import io.github.thecsdev.tcdcommons.api.config.annotation.SerializedAs;

public class BetterStatsConfig extends AutoConfig
{
	// ==================================================
	private static @NonSerialized boolean FULL_VERSION = false;
	// ==================================================
	public static @NonSerialized boolean DEBUG_MODE = false;
	// --------------------------------------------------
	public @SerializedAs("common-forceFullVersion")      boolean forceFullVersion          = false; //v3.11+
	public @SerializedAs("client-guiSmoothScroll")       boolean guiSmoothScroll           = true;
	public @SerializedAs("client-guiMobsFollowCursor")   boolean guiMobsFollowCursor       = true;
	public @SerializedAs("client-trustAllServersBssNet") boolean trustAllServersBssNet     = true;
	public @SerializedAs("client-allowStatsSharing")     boolean netPref_allowStatsSharing = true; //v3.11+
	public @SerializedAs("server-registerCommands")      boolean registerCommands          = true;
	public @SerializedAs("server-enableSAS")             boolean enableServerSAS           = true;
	public @SerializedAs("server-sasConfig")             SASConfig sasConfig = new SASConfig();
	// ==================================================
	public BetterStatsConfig(String name) { super(name); }
	static
	{
		//check for the "full version" file's presence
		try
		{
			final var s = BetterStats.class.getResourceAsStream("/betterstats.full.txt");
			if(s != null) { s.close(); FULL_VERSION = true; }
		}
		catch(Exception e) { FULL_VERSION = true; }
	}
	// ==================================================
	/**
	 * Returns {@code true} if all features should always be available.
	 */
	public final boolean isFullVersion() { return FULL_VERSION || this.forceFullVersion; }
	// ==================================================
}