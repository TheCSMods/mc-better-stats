package io.github.thecsdev.betterstats.api.util.io;

import java.util.Objects;

import com.mojang.authlib.GameProfile;

import io.github.thecsdev.tcdcommons.api.badge.ServerPlayerBadgeHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * An {@link IStatsProvider} that provides statistics
 * about a given {@link ServerPlayerEntity}.
 */
public final class ServerPlayerStatsProvider implements IStatsProvider
{
	// ==================================================
	private final ServerPlayerEntity player;
	// --------------------------------------------------
	private final Text                     displayName;
	private final GameProfile              gameProfile;
	private final ServerStatHandler        statHandler;
	private final ServerPlayerBadgeHandler badgeHandler;
	// ==================================================
	private ServerPlayerStatsProvider(ServerPlayerEntity player) throws NullPointerException
	{
		this.player       = Objects.requireNonNull(player);
		this.displayName  = player.getDisplayName();
		this.gameProfile  = player.getGameProfile();
		this.statHandler  = player.getStatHandler();
		this.badgeHandler = ServerPlayerBadgeHandler.getServerBadgeHandler(player);
	}
	// --------------------------------------------------
	public final ServerPlayerEntity getPlayer() { return this.player; }
	// ==================================================
	public final @Override Text getDisplayName() { return this.displayName; }
	public final @Override GameProfile getGameProfile() { return this.gameProfile; }
	// --------------------------------------------------
	public final @Override int getStatValue(Stat<?> stat) { return this.statHandler.getStat(stat); }
	public final @Override <T> int getStatValue(StatType<T> type, T stat) { return this.statHandler.getStat(type, stat); }
	public final @Override int getPlayerBadgeValue(Identifier badgeId) { return this.badgeHandler.getValue(badgeId); }
	// ==================================================
	public final @Override int hashCode() { return this.player.hashCode(); }
	public final @Override boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		final var spsp = (ServerPlayerStatsProvider)obj;
		return (this.player == spsp.player);
	}
	// ==================================================
	/**
	 * Creates a {@link ServerPlayerStatsProvider} instance based on a {@link ServerPlayerEntity}.
	 * @param player The {@link ClientPlayerEntity}.
	 */
	public static final ServerPlayerStatsProvider of(ServerPlayerEntity player) throws NullPointerException
	{
		return new ServerPlayerStatsProvider(player);
	}
	// ==================================================
}