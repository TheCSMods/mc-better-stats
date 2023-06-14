package io.github.thecsdev.betterstats;

import io.github.thecsdev.tcdcommons.api.config.AutoConfig;
import io.github.thecsdev.tcdcommons.api.config.annotation.SerializedAs;

public class BetterStatsConfig extends AutoConfig
{
	// ==================================================
	@SerializedAs("guiMobsFollowCursor") //mitigate side-effects of field renaming
	public boolean guiMobsFollowCursor;
	// ==================================================
	public BetterStatsConfig(String name)
	{
		super(name);
		this.guiMobsFollowCursor = true;
	}
	// ==================================================
}