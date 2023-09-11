package io.github.thecsdev.betterstats.api.util.io;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.tcdcommons.api.badge.PlayerBadge;
import io.github.thecsdev.tcdcommons.api.registry.TRegistries;
import net.minecraft.client.MinecraftClient;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.StatType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * An {@link IStatsProvider} whose statistics can be edited.
 * @apiNote Under no circumstance, make edits/changes to the {@link MinecraftClient#player}'s
 * {@link StatHandler}, as that would result in a client/server de-sync.
 */
public interface IEditableStatsProvider extends IStatsProvider
{
	// ==================================================
	/**
	 * Sets the value of {@link IStatsProvider#getDisplayName()}
	 * @param displayName The new "display name" for this {@link IStatsProvider}.
	 */
	public void setDisplayName(@Nullable Text displayName);
	// ==================================================
	/**
	 * Sets the value of a given {@link Stat}.
	 * @param stat The {@link Stat} whose value is to be changed.
	 * @param value The new {@link Stat} value.
	 * @apiNote This cannot be undone.
	 * @apiNote When overriding, make sure to enforce the legal range between `0` and `{@link Integer#MAX_VALUE}`.
	 */
	public void setStatValue(Stat<?> stat, int value);
	
	/**
	 * Sets the value of a given {@link Stat}.
	 * @param type The {@link StatType}.
	 * @param stat The specific {@link Stat} associated with the {@link StatType}.
	 * @param value The new {@link Stat} value.
	 * @apiNote This cannot be undone.
	 * @apiNote You should not override this, as it calls {@link #setStatValue(Stat, int)} by default.
	 */
	default <T> void setStatValue(StatType<T> type, T stat, int value) { setStatValue(type.getOrCreateStat(stat), value); }
	
	/**
	 * Increases the value of a given {@link Stat}.
	 * @param stat The {@link Stat} whose value is to be changed.
	 * @param value The {@link Stat} value increment.
	 * @apiNote This cannot be undone.
	 * @apiNote You should not override this, as it calls {@link #setStatValue(Stat, int)} by default.
	 */
	default void increaseStatValue(Stat<?> stat, int value)
	{
		final int i = (int)Math.min(/*must cast to long*/(long)getStatValue(stat) + value, Integer.MAX_VALUE);
		setStatValue(stat, i);
	}
	
	/**
	 * Increases the value of a given {@link Stat}.
	 * @param type The {@link StatType}.
	 * @param stat The specific {@link Stat} associated with the {@link StatType}.
	 * @param value The {@link Stat} value increment.
	 * @apiNote This cannot be undone.
	 * @apiNote You should not override this, as it calls {@link #increaseStatValue(Stat, int)} by default.
	 */
	default <T> void increaseStatValue(StatType<T> type, T stat, int value) { increaseStatValue(type.getOrCreateStat(stat), value); }
	// ==================================================
	/**
	 * Adds a {@link PlayerBadge} to the list of {@link PlayerBadge}s, using its unique {@link Identifier}.
	 * @param badgeId The {@link PlayerBadge}'s unique {@link Identifier}.
	 * @return {@code true} if the {@link PlayerBadge} was successfully added, aka not present prior to being added.
	 * @see TRegistries#PLAYER_BADGE
	 */
	public boolean addPlayerBadge(Identifier badgeId);
	
	/**
	 * Removes a {@link PlayerBadge} from the list of {@link PlayerBadge}s, using its unique {@link Identifier}.
	 * @param badgeId The {@link PlayerBadge}'s unique {@link Identifier}.
	 * @return {@code true} if the {@link PlayerBadge} was present prior to being removed.
	 * @see TRegistries#PLAYER_BADGE
	 */
	public boolean removePlayerBadge(Identifier badgeId);
	// ==================================================
}