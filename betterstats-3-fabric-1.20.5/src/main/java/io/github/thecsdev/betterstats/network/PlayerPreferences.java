package io.github.thecsdev.betterstats.network;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.BetterStats;

public final @Internal class PlayerPreferences
{
	// ==================================================
	/**
	 * When set to {@code true}, this should never be switched back to {@code false}.<br/>
	 * Indicates whether or not the associated player has {@link BetterStats} installed.
	 */
	public boolean hasBss = false;
	// --------------------------------------------------
	/**
	 * The timestamp at which the last live stats update was performed.
	 * Used to avoid packet spam.
	 */
	long lastLiveStatsUpdate = 0;
	
	/**
	 * When set to true, the {@link BetterStatsNetworkHandler} will
	 * automatically update the client on their stats changes, live.
	 */
	public boolean liveStats = false;
	// ==================================================
}