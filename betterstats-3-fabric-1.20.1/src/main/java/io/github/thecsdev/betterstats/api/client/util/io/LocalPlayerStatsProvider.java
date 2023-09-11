package io.github.thecsdev.betterstats.api.client.util.io;

import static io.github.thecsdev.tcdcommons.api.badge.PlayerBadgeHandler.PBH_CUSTOM_DATA_ID;
import static io.github.thecsdev.tcdcommons.api.hooks.entity.EntityHooks.getCustomDataEntryG;
import static io.github.thecsdev.tcdcommons.api.hooks.entity.EntityHooks.setCustomDataEntryG;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;

import java.util.Iterator;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.client.BetterStatsClient;
import io.github.thecsdev.tcdcommons.api.badge.PlayerBadgeHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.StatType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * An {@link IStatsProvider} for the {@link MinecraftClient#player}.
 */
public final class LocalPlayerStatsProvider implements IStatsProvider
{
	// ==================================================
	protected static LocalPlayerStatsProvider INSTANCE = null;
	// --------------------------------------------------
	protected final StatHandler statsHandler;
	protected final PlayerBadgeHandler badgeHandler;
	protected final Text displayName;
	// ==================================================
	protected LocalPlayerStatsProvider() throws IllegalStateException
	{
		//define some local variables needed for construction
		final var client = BetterStatsClient.MC_CLIENT;
		final var localPlayer = client.player;
		if(localPlayer == null)
			throw new IllegalStateException("Unable to obtain local player stats while not in-game.");
		
		//define final variables
		this.displayName = literal(localPlayer.getDisplayName().getString()); //using `literal` to clear Text metadata
		this.statsHandler = Objects.requireNonNull(localPlayer.getStatHandler());
		PlayerBadgeHandler badgeHandler = getCustomDataEntryG(localPlayer, PBH_CUSTOM_DATA_ID);
		if(badgeHandler == null)
			badgeHandler = setCustomDataEntryG(localPlayer, PBH_CUSTOM_DATA_ID, new PlayerBadgeHandler());
		this.badgeHandler = badgeHandler;
	}
	// ==================================================
	public final @Override Text getDisplayName() { return this.displayName; }
	public final @Override int getStatValue(Stat<?> stat) { return this.statsHandler.getStat(stat); }
	public final @Override <T> int getStatValue(StatType<T> type, T stat) { return this.statsHandler.getStat(type, stat); }
	public final @Override boolean containsPlayerBadge(Identifier badgeId) { return this.badgeHandler.containsBadge(badgeId); }
	public final @Override Iterator<Identifier> getPlayerBadgeIterator() { return this.badgeHandler.iterator(); }
	// ==================================================
	/**
	 * Returns the current {@link LocalPlayerStatsProvider} instance,
	 * or {@code null} if the {@link MinecraftClient} is not "in-game".
	 */
	public static @Nullable LocalPlayerStatsProvider getInstance()
	{
		//obtain the Minecraft client instance
		final var player = BetterStatsClient.MC_CLIENT.player;
		//if the local player is null, return null...
		if(player == null) return (INSTANCE = null);
		//...else get or create instance
		else
		{
			//if the instance is null, or its stat handler no longer matches player stat handler,
			//create a new instance
			if(INSTANCE == null || INSTANCE.statsHandler != player.getStatHandler())
				INSTANCE = new LocalPlayerStatsProvider();
			//finally return the instance
			return INSTANCE;
		}
	}
	// ==================================================
}