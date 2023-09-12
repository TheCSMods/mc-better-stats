package io.github.thecsdev.betterstats.api.client.gui.screen;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.api.client.util.io.LocalPlayerStatsProvider;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreenWrapper;
import net.minecraft.client.gui.screen.StatsListener;

@Internal class BetterStatsScreenWrapper extends TScreenWrapper<BetterStatsScreen> implements StatsListener
{
	// ==================================================
	public BetterStatsScreenWrapper(BetterStatsScreen target) { super(target); }
	// ==================================================
	public final @Override void onStatsReady() //TODO - Temporary; Make a network handler for these
	{
		if(this.target.getStatsProvider() == LocalPlayerStatsProvider.getInstance())
			this.target.refresh();
	}
	// ==================================================
}