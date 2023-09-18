package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.betterstats.api.util.stats.SUItemStat.getItemStatsByModGroups;
import static io.github.thecsdev.betterstats.client.gui.stats.panel.StatFiltersPanel.FILTER_ID_SEARCH;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.api.client.registry.BSStatsTabs;
import io.github.thecsdev.betterstats.api.client.util.StatFilterSettings;
import io.github.thecsdev.betterstats.api.util.stats.SUItemStat;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.util.TUtils;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import net.minecraft.text.Text;

public final @Internal class FoodStuffsStatsTab extends ItemStatsTab
{
	// ==================================================
	public final @Override Text getName() { return translatable("advancements.husbandry.balanced_diet.title"); }
	// --------------------------------------------------
	public @Virtual @Override void initStats(StatsInitContext initContext)
	{
		final var panel = initContext.getStatsPanel();
		final var stats = initContext.getStatsProvider();
		final var statGroups = getItemStatsByModGroups(stats, getPredicate(initContext.getFilterSettings()));
		
		for(final var statGroup : statGroups.entrySet())
		{
			BSStatsTab.init_groupLabel(panel, literal(TUtils.getModName(statGroup.getKey())));
			init_stats(panel, statGroup.getValue(), widget ->
			{
				if(widget.getStat().used > 0) widget.setOutlineColor(BSStatsTabs.COLOR_SPECIAL);
				else if(!widget.getStat().isEmpty()) widget.setOutlineColor(TPanelElement.COLOR_OUTLINE);
			});
		}
		
		final var summary = init_summary(panel);
		if(summary != null)
			summary.summarizeItemStats(statGroups.values().stream()
				.flatMap(Collection::stream)
				.collect(Collectors.toList()));
	}
	// --------------------------------------------------
	protected @Virtual @Override Predicate<SUItemStat> getPredicate(StatFilterSettings filterSettings)
	{
		final String sq = filterSettings.getPropertyOrDefault(FILTER_ID_SEARCH, "");
		final Predicate<SUItemStat> sqPred = stat -> stat.matchesSearchQuery(sq);
		return sqPred.and(stat -> stat.getItem().isFood());
	}
	// ==================================================
}