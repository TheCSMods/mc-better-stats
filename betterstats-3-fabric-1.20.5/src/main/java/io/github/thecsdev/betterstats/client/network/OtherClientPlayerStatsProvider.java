package io.github.thecsdev.betterstats.client.network;

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.api.util.io.RAMStatsProvider;

/**
 * A {@link RAMStatsProvider}, except in form of a {@link Class} that is specifically
 * used for storing {@link IStatsProvider} data about another player that isn't the client.
 */
public final @Internal class OtherClientPlayerStatsProvider extends RAMStatsProvider
{
	// ==================================================
	private final String playerName;
	// ==================================================
	OtherClientPlayerStatsProvider(String playerName) throws NullPointerException
	{
		this.playerName = Objects.requireNonNull(playerName);
	}
	// --------------------------------------------------
	public final String getPlayerName() { return this.playerName; }
	// ==================================================
}