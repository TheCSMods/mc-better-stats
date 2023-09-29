package io.github.thecsdev.betterstats.network;

import org.jetbrains.annotations.ApiStatus.Internal;

public final @Internal class PlayerPreferences
{
	/**
	 * When set to true, the {@link BetterStatsNetworkHandler} will
	 * automatically update the client on their stats changes, live.
	 */
	public boolean liveStats = false;
}