package io.github.thecsdev.betterstats.api.util.io;

import java.util.Collection;
import java.util.Iterator;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.tcdcommons.api.badge.PlayerBadge;
import io.github.thecsdev.tcdcommons.api.badge.PlayerBadgeHandler;
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
	/**
	 * Returns a "visual"/"user friendly" display {@link Text} that will
	 * be shown on the GUI screen as an indicator as to who the stats belong to.
	 * @apiNote Does not have to follow any Minecraft account naming rules or even correspond to one.
	 */
	public @Nullable Text getDisplayName();
	
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
	
	/**
	 * Returns an {@link Iterator} that allows easily iterating
	 * over every {@link PlayerBadge} {@link Identifier} in this {@link IStatsProvider}.
	 */
	public Iterator<Identifier> getPlayerBadgeIterator();
	
	/**
	 * Returns true if a given {@link PlayerBadge}'s {@link Identifier}
	 * is associated with this {@link IStatsProvider}.
	 * @param badgeId The {@link PlayerBadge}'s unique {@link Identifier}.
	 * @see PlayerBadgeHandler
	 * @apiNote It is recommended to {@link Override} this and use
	 * {@link Collection#contains(Object)} for better performance.
	 */
	default boolean containsPlayerBadge(final Identifier badgeId)
	{
		// Create an Iterator object by calling getPlayerBadgeIterator method
		Iterator<Identifier> iterator = getPlayerBadgeIterator();
		
		// Iterate through the Identifiers to check if badgeId exists
		while(iterator.hasNext())
			if(iterator.next().equals(badgeId))
				return true;  // Return true if badgeId is found
		
		// Return false if badgeId is not found
		return false;
	}
}