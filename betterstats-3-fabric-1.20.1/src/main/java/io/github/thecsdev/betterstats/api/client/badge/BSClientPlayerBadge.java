package io.github.thecsdev.betterstats.api.client.badge;

import java.util.Objects;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.api.util.stats.SUPlayerBadgeStat;
import io.github.thecsdev.tcdcommons.api.client.badge.ClientPlayerBadge;
import io.github.thecsdev.tcdcommons.api.client.gui.util.UITexture;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import net.minecraft.text.Text;

/**
 * {@link BetterStats}'s implementation of {@link ClientPlayerBadge}.
 */
public @Virtual class BSClientPlayerBadge extends ClientPlayerBadge
{
	// ==================================================
	private static final Function<IStatsProvider, Integer> EMPTY_CRITERIA = __ -> 0;
	// --------------------------------------------------
	protected final Text name, description;
	protected final @Nullable UITexture icon;
	// --------------------------------------------------
	protected @Nullable Function<IStatsProvider, Integer> statCriteria = EMPTY_CRITERIA;
	// ==================================================
	public BSClientPlayerBadge(Text title, Text description, @Nullable UITexture icon) throws NullPointerException
	{
		this.name = Objects.requireNonNull(title);
		this.description = Objects.requireNonNull(description);
		this.icon = icon;
	}
	// ==================================================
	public @Virtual @Override Text getName() { return this.name; }
	public @Virtual @Override Text getDescription() { return this.description; }
	// --------------------------------------------------
	public final @Nullable UITexture getIcon() { return this.icon; }
	// ==================================================
	/**
	 * <b>About the returned function:</b>
	 * <p>
	 * The {@link Function} returns an {@link Integer} if a given {@link IStatsProvider}
	 * contains statistics that meet the criteria for this {@link BSClientPlayerBadge}
	 * to be "awarded" to said {@link IStatsProvider}.
	 * <p>
	 * The returned {@link Integer} indicates the "value" aka "quantity"
	 * of the {@link BSClientPlayerBadge} that should be "awarded".
	 * 
	 * @see SUPlayerBadgeStat#value
	 */
	public final @Nullable Function<IStatsProvider, Integer> getStatCriteria() { return this.statCriteria; }
	public @Virtual void setStatCriteria(@Nullable Function<IStatsProvider, Integer> statCriteria) { this.statCriteria = statCriteria; }
	// ==================================================
}