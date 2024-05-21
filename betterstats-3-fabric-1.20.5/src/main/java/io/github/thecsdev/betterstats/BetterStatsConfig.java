package io.github.thecsdev.betterstats;

import io.github.thecsdev.betterstats.util.stats.SASConfig;
import io.github.thecsdev.tcdcommons.api.config.AutoConfig;
import io.github.thecsdev.tcdcommons.api.config.annotation.NonSerialized;
import io.github.thecsdev.tcdcommons.api.config.annotation.SerializedAs;

public class BetterStatsConfig extends AutoConfig
{
	// ==================================================
	public static @NonSerialized boolean FULL_VERSION = (BetterStats.class.getResource("betterstats.full.txt") != null);
	// ==================================================
	public static @NonSerialized boolean DEBUG_MODE = false;
	// --------------------------------------------------
	public @SerializedAs("common-forceFullVersion")      boolean forceFullVersion      = false;
	public @SerializedAs("client-guiSmoothScroll")       boolean guiSmoothScroll       = true;
	public @SerializedAs("client-guiMobsFollowCursor")   boolean guiMobsFollowCursor   = true;
	public @SerializedAs("client-trustAllServersBssNet") boolean trustAllServersBssNet = true;
	public @SerializedAs("server-registerCommands")      boolean registerCommands      = true;
	public @SerializedAs("server-enableSAS")             boolean enableServerSAS       = true;
	public @SerializedAs("server-sasConfig")             SASConfig sasConfig = new SASConfig();
	// ==================================================
	public BetterStatsConfig(String name) { super(name); }
	// ==================================================
	/**
	 * Returns {@code true} if all features should always be available.
	 */
	public final boolean isFullVersion() { return FULL_VERSION || this.forceFullVersion; }
	// ==================================================
}