package io.github.thecsdev.betterstats.api.client.util.io;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;

import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.client.BetterStatsClient;
import io.github.thecsdev.tcdcommons.api.badge.PlayerBadgeHandler;
import io.github.thecsdev.tcdcommons.api.client.badge.ClientPlayerBadge;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.StatType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * An {@link IStatsProvider} for {@link ClientPlayerEntity}s.
 * @see MinecraftClient#player
 */
public final class LocalPlayerStatsProvider implements IStatsProvider
{
	// ==================================================
	private static LocalPlayerStatsProvider INSTANCE = null;
	// ==================================================
	private final ClientPlayerEntity player;
	// --------------------------------------------------
	private final Text               displayName;
	private final GameProfile        gameProfile;
	private final StatHandler        statsHandler;
	private final PlayerBadgeHandler badgeHandler;
	// ==================================================
	private LocalPlayerStatsProvider(ClientPlayerEntity player) throws NullPointerException
	{
		this.player = Objects.requireNonNull(player);
		this.displayName  = player.getDisplayName();
		this.gameProfile  = player.getGameProfile();
		this.statsHandler = player.getStatHandler();
		this.badgeHandler = ClientPlayerBadge.getClientPlayerBadgeHandler(player);
	}
	// --------------------------------------------------
	public final ClientPlayerEntity getPlayer() { return this.player; }
	// ==================================================
	public final @Override Text getDisplayName() { return this.displayName; }
	public final @Override GameProfile getGameProfile() { return this.gameProfile; }
	// --------------------------------------------------
	public final @Override int getStatValue(Stat<?> stat) { return this.statsHandler.getStat(stat); }
	public final @Override <T> int getStatValue(StatType<T> type, T stat) { return this.statsHandler.getStat(type, stat); }
	public final @Override int getPlayerBadgeValue(Identifier badgeId) { return this.badgeHandler.getValue(badgeId); }
	// ==================================================
	public final @Override int hashCode() { return this.player.hashCode(); }
	public final @Override boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		final var lpsp = (LocalPlayerStatsProvider)obj;
		return (this.player == lpsp.player);
	}
	// ==================================================
	/**
	 * Returns the current {@link LocalPlayerStatsProvider} instance,
	 * or {@code null} if the {@link MinecraftClient} is not "in-game".
	 */
	public static final @Nullable LocalPlayerStatsProvider getInstance()
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
			if(INSTANCE == null || INSTANCE.player != player)
				INSTANCE = new LocalPlayerStatsProvider(player);
			//finally return the instance
			return INSTANCE;
		}
	}
	
	/**
	 * Creates a {@link LocalPlayerStatsProvider} instance based on a {@link ClientPlayerEntity}.
	 * @param player The {@link ClientPlayerEntity}.
	 */
	public static final LocalPlayerStatsProvider of(ClientPlayerEntity player) throws NullPointerException
	{
		//null-check
		Objects.requireNonNull(player);
		//return INSTANCE if the player is the same. create and return new stats provider otherwise
		return (INSTANCE != null && INSTANCE.player == player) ?
				INSTANCE : new LocalPlayerStatsProvider(player);
	}
	// ==================================================
}