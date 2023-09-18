package io.github.thecsdev.betterstats.api.client.gui.screen;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.api.client.util.io.LocalPlayerStatsProvider;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreenWrapper;
import io.github.thecsdev.tcdcommons.api.client.network.PlayerBadgeNetworkListener;
import net.minecraft.client.gui.screen.StatsListener;

@Internal class BetterStatsScreenWrapper extends TScreenWrapper<BetterStatsScreen> implements StatsListener, PlayerBadgeNetworkListener
{
	// ==================================================
	public BetterStatsScreenWrapper(BetterStatsScreen target) { super(target); }
	// ==================================================
	public final @Override void onStatsReady()
	{
		//if the user is viewing their own statistics, and they receive a statistics packet...
		if(this.target.getStatsProvider() == LocalPlayerStatsProvider.getInstance())
			//...refresh the statistics screen
			this.target.refresh();
	}
	// --------------------------------------------------
	public @Override void onPlayerBadgesReady() { onStatsReady(); }
	// ==================================================
}