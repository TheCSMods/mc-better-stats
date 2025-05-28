package io.github.thecsdev.betterstats.api.util.stats;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * A "Stat utils stat".<p>
 * Represents a statistic about something, whether it be about {@link Item}s,
 * or {@link EntityType}s, or anything else. This is a utility class for
 * easier reading of statistics about given things.
 * @apiNote Not intended to be {@link Override}n outside of this API.
 */
public abstract @Internal class SUStat<T> extends Object
{
	// ==================================================
	public static final Identifier ID_NULL = Identifier.of("null");
	// --------------------------------------------------
	protected final IStatsProvider statsProvider;
	protected final Identifier statId;
	protected final Text statLabel;
	protected final String statLabelSQ, statIdSQ; //"search query" helpers
	// ==================================================
	protected SUStat(IStatsProvider statsProvider, Identifier statId, Text statLabel) throws NullPointerException
	{
		this.statsProvider = Objects.requireNonNull(statsProvider);
		this.statId = Objects.requireNonNull(statId);
		this.statLabel = Objects.requireNonNull(statLabel);
		
		this.statLabelSQ = this.statLabel.getString().toLowerCase().replaceAll("\\s+","");
		this.statIdSQ = Objects.toString(statId);
	}
	// ==================================================
	/**
	 * Returns the {@link Text}ual label that represents the {@link Stat}.
	 */
	public final Text getStatLabel() { return this.statLabel; }
	
	/**
	 * Returns the unique {@link Identifier} associated with this {@link SUStat}.
	 * <p>
	 * For {@link SUGeneralStat}, refers to {@link Stat#getValue()},<br/>
	 * for {@link SUItemStat}, refers to {@link Item}'s {@link Identifier},<br/>
	 * for {@link SUMobStat}, refers to {@link EntityType}'s {@link Identifier}.
	 */
	public final Identifier getStatID() { return this.statId; }
	
	/**
	 * Returns the {@link IStatsProvider} the {@link Stats} data was obtained from.
	 */
	public final IStatsProvider getStatsProvider() { return this.statsProvider; }
	// --------------------------------------------------
	/**
	 * Checks if this {@link SUStat}'s {@link #statLabel} matches a given "search query".
	 * @param search The search query being performed.
	 * @see #getStatLabel()
	 */
	public @Virtual boolean matchesSearchQuery(String search)
	{
		search = StringUtils.defaultString(search).toLowerCase().replaceAll("\\s+","");
		return this.statLabelSQ.contains(search) || this.statIdSQ.contains(search);
	}
	// ==================================================
	/**
	 * Returns {@code true} if the sum of all the {@link Stat}
	 * values associated with this {@link SUStat} is {@code 0}.
	 */
	public abstract boolean isEmpty();
	// ==================================================
}