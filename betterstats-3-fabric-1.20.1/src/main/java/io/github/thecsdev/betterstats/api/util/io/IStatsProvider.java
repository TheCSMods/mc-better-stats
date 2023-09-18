package io.github.thecsdev.betterstats.api.util.io;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;

import io.github.thecsdev.tcdcommons.api.badge.PlayerBadge;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.StatType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * A component that contains loaded statistics about a given player.
 */
public interface IStatsProvider
{
	// ==================================================
	/**
	 * Returns a "visual"/"user friendly" display {@link Text} that will
	 * be shown on the GUI screen as an indicator as to who the stats belong to.
	 * @apiNote Does not have to follow any Minecraft account naming rules or even correspond to one.
	 */
	public @Nullable Text getDisplayName();
	
	/**
	 * Returns the {@link GameProfile} of the player these stats belong to,
	 * or {@code null} if these stats are not associated with a player.
	 */
	public @Nullable GameProfile getGameProfile();
	// ==================================================
	/**
	 * Returns the {@link Integer} value of a given {@link Stat}.
	 * @param stat The {@link Stat} whose value is to be obtained.
	 * @see StatHandler
	 */
	public int getStatValue(Stat<?> stat);
	
	/**
	 * Returns the {@link Integer} value of a given {@link StatType} and its corresponding {@link Stat}.
	 * @param type The {@link StatType}.
	 * @param stat The {@link Stat} whose value is to be obtained.
	 * @see StatHandler
	 * @apiNote You should not override this, as it calls {@link #getStatValue(Stat)} by default.
	 */
	default <T> int getStatValue(StatType<T> type, T stat) { return type.hasStat(stat) ? getStatValue(type.getOrCreateStat(stat)) : 0; }
	// --------------------------------------------------
	/**
	 * Returns the {@link Integer} value of a given {@link PlayerBadge} stat.
	 * @param badgeId The unique {@link Identifier} of the {@link PlayerBadge}.
	 */
	public int getPlayerBadgeValue(Identifier badgeId);
	
	/**
	 * Returns the {@link Integer} value of a given {@link PlayerBadge} stat.
	 * @param playerBadge The given {@link PlayerBadge}. Must be registered.
	 * @throws NullPointerException If the argument is {@code null}, or the {@link PlayerBadge} is not registered.
	 */
	default int getPlayerBadgeValue(PlayerBadge playerBadge) throws NullPointerException
	{
		return getPlayerBadgeValue(Objects.requireNonNull(playerBadge.getId()));
	}
	// ==================================================
}