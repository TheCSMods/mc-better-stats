package io.github.thecsdev.betterstats.api.client.util.io;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;

import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.client.BetterStatsClient;
import io.github.thecsdev.tcdcommons.api.badge.PlayerBadgeHandler;
import io.github.thecsdev.tcdcommons.api.client.badge.ClientPlayerBadge;
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
	protected final Text displayName;
	protected final GameProfile playerProfile;
	//
	protected final StatHandler statsHandler;
	protected final PlayerBadgeHandler badgeHandler;
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
		this.playerProfile = localPlayer.getGameProfile();
		
		this.statsHandler = Objects.requireNonNull(localPlayer.getStatHandler());
		this.badgeHandler = ClientPlayerBadge.getClientPlayerBadgeHandler(localPlayer);
	}
	// ==================================================
	public final @Override Text getDisplayName() { return this.displayName; }
	public final @Override GameProfile getGameProfile() { return this.playerProfile; }
	// --------------------------------------------------
	public final @Override int getStatValue(Stat<?> stat) { return this.statsHandler.getStat(stat); }
	public final @Override <T> int getStatValue(StatType<T> type, T stat) { return this.statsHandler.getStat(type, stat); }
	public final @Override int getPlayerBadgeValue(Identifier badgeId) { return this.badgeHandler.getValue(badgeId); }
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