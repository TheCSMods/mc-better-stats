package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils.GAP;
import static io.github.thecsdev.tcdcommons.api.client.gui.config.TConfigPanelBuilder.nextPanelVerticalRect;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;

import java.util.function.Predicate;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.api.client.gui.stats.panel.StatsSummaryPanel;
import io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.betterstats.api.client.util.StatFilterSettings;
import io.github.thecsdev.betterstats.api.util.stats.SUStat;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;

/**
 * Contains {@link StatsTab}s that belong to {@link BetterStats}.
 */
@Internal abstract class BSStatsTab<S extends SUStat<?>> extends StatsTab
{
	// ==================================================
	@Internal BSStatsTab() {}
	// ==================================================
	public final @Override void initFilters(FiltersInitContext initContext)
	{
		StatsTabUtils.initDefaultFilters(initContext);
		StatsTabUtils.initShowEmptyStatsFilter(initContext);
		initExtraFilters(initContext);
	}
	protected @Virtual void initExtraFilters(FiltersInitContext initContext) {}
	// --------------------------------------------------
	protected @Virtual Predicate<S> getPredicate(StatFilterSettings filterSettings)
	{
		final String sq = filterSettings.getPropertyOrDefault(StatsTabUtils.FILTER_ID_SEARCH, "");
		final boolean se = filterSettings.getPropertyOrDefault(StatsTabUtils.FILTER_ID_SHOWEMPTY, false);
		return stat -> stat.matchesSearchQuery(sq) && (se || !stat.isEmpty());
	}
	// ==================================================
	@Internal static @Nullable StatsSummaryPanel initStatsSummary(TPanelElement panel)
	{
		//do not summarize if no children are present
		if(panel.getChildren().size() < 1) return null;
		
		//summary group label
		final var lbl = StatsTabUtils.initGroupLabel(panel, literal("\u2190 \u2022 \u2192"));
		lbl.setTextHorizontalAlignment(HorizontalAlignment.CENTER);
		
		//init summary panel
		final var n1 = nextPanelVerticalRect(panel);
		final var summary = new StatsSummaryPanel(n1.x, n1.y + GAP, n1.width);
		panel.addChild(summary, false);
		return summary;
	}
	// ==================================================
}