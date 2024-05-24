package io.github.thecsdev.betterstats.api.client.util.io;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import io.github.thecsdev.betterstats.api.util.io.RAMStatsProvider;
import io.github.thecsdev.tcdcommons.api.util.thread.TaskScheduler;

/**
 * A {@link RAMStatsProvider} specifically used for storing statistics from
 * other players that are present on the currently-connected-to server.
 * @see LocalThirdPartyStatsProvider#isValid()
 * @apiNote Holds a "snapshot" of the stats. Does not update "live".
 * @apiNote It is recommended not to manually set stat values yourself. 
 */
@Experimental
public final class LocalThirdPartyStatsProvider extends RAMStatsProvider
{
	// ==================================================
	/**
	 * Holds {@link LocalThirdPartyStatsProvider} references.
	 */
	private static final Cache<String, LocalThirdPartyStatsProvider> SESSION_STORAGE = CacheBuilder.newBuilder()
			.expireAfterAccess(1, TimeUnit.HOURS)
			.build();
	static { TaskScheduler.schedulePeriodicCacheCleanup(SESSION_STORAGE); }
	// --------------------------------------------------
	private final String playerName;
	// ==================================================
	private LocalThirdPartyStatsProvider(String playerName) throws NullPointerException
	{
		super();
		this.playerName = Objects.requireNonNull(playerName);
	}
	// --------------------------------------------------
	/**
	 * Returns {@code true} if this instance of {@link LocalThirdPartyStatsProvider}
	 * is still "valid" aka not "expired" and/or "invalidated".
	 * @apiNote You should read data from this object only if this method returns {@code true}.
	 */
	public final boolean isValid() { return SESSION_STORAGE.asMap().containsValue(this); }
	
	/**
	 * Returns the name of the player whose stats are held in
	 * this {@link LocalThirdPartyStatsProvider}.
	 */
	public final String getPlayerName() { return this.playerName; }
	// ==================================================
	/**
	 * An {@link Internal} method for cleaning up and "invalidating" all
	 * {@link LocalThirdPartyStatsProvider}s present in the "session storage".
	 * @apiNote Called {@link Internal}ly when the client disconnects.
	 */
	public static final @Internal void clearSessionStorage() { SESSION_STORAGE.invalidateAll(); }
	
	/**
	 * Returns a {@link LocalThirdPartyStatsProvider} instance for a
	 * given player that is present on the server that this client
	 * is currently connected to.
	 * @apiNote The stats provider will be empty if said player's stats
	 * were not downloaded earlier during the current session.
	 * @apiNote It is always recommended to obtain an instance of
	 * {@link LocalThirdPartyStatsProvider} via this method, as they can
	 * "expire" and become invalidated at any time.
	 */
	@Experimental
	public static final LocalThirdPartyStatsProvider ofSessionPlayer(String playerName)
	{
		@Nullable var ltpsp = SESSION_STORAGE.getIfPresent(Objects.requireNonNull(playerName));
		if(ltpsp == null)
		{
			ltpsp = new LocalThirdPartyStatsProvider(playerName);
			SESSION_STORAGE.put(playerName, ltpsp);
		}
		return ltpsp;
	}
	// ==================================================
}