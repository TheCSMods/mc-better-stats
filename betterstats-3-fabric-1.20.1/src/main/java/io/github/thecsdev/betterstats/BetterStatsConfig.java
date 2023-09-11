package io.github.thecsdev.betterstats;

import io.github.thecsdev.tcdcommons.api.config.AutoConfig;
import io.github.thecsdev.tcdcommons.api.config.annotation.NonSerialized;
import io.github.thecsdev.tcdcommons.api.config.annotation.SerializedAs;

public class BetterStatsConfig extends AutoConfig
{
	// ==================================================
	public static @NonSerialized boolean DEBUG_MODE = false;
	// --------------------------------------------------
	public @SerializedAs("guiMobsFollowCursor") boolean guiMobsFollowCursor = true;
	// ==================================================
	public BetterStatsConfig(String name) { super(name); }
	// ==================================================
}