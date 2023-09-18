package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.betterstats.api.util.stats.SUMobStat.getMobStatsByModGroups;
import static io.github.thecsdev.betterstats.client.gui.stats.panel.StatFiltersPanel.FILTER_ID_SEARCH;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.api.client.registry.BSStatsTabs;
import io.github.thecsdev.betterstats.api.client.util.StatFilterSettings;
import io.github.thecsdev.betterstats.api.util.stats.SUMobStat;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.util.TUtils;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.text.Text;

public final @Internal class MonstersHuntedStatsTab extends MobStatsTab
{
	// ==================================================
	public final @Override Text getName() { return translatable("advancements.adventure.kill_all_mobs.title"); }
	// --------------------------------------------------
	public @Virtual @Override void initStats(StatsInitContext initContext)
	{
		final var panel = initContext.getStatsPanel();
		final var stats = initContext.getStatsProvider();
		final var statGroups = getMobStatsByModGroups(stats, getPredicate(initContext.getFilterSettings()));
		
		for(final var statGroup : statGroups.entrySet())
		{
			BSStatsTab.init_groupLabel(panel, literal(TUtils.getModName(statGroup.getKey())));
			init_stats(panel, statGroup.getValue(), widget ->
			{
				if(widget.getStat().kills > 0) widget.setOutlineColor(BSStatsTabs.COLOR_SPECIAL);
				else if(!widget.getStat().isEmpty()) widget.setOutlineColor(TPanelElement.COLOR_OUTLINE);
			});
		}
		
		final var summary = init_summary(panel);
		if(summary != null)
		{
			summary.summarizeMobStats(statGroups.values().stream()
				.flatMap(Collection::stream)
				.collect(Collectors.toList()));
			summary.autoHeight();
		}
	}
	// --------------------------------------------------
	protected @Virtual Predicate<SUMobStat> getPredicate(StatFilterSettings filterSettings)
	{
		final String sq = filterSettings.getPropertyOrDefault(FILTER_ID_SEARCH, "");
		final Predicate<SUMobStat> sqPred = stat -> stat.matchesSearchQuery(sq);
		return sqPred.and(stat -> 
				stat.getEntityType().getSpawnGroup() == SpawnGroup.MONSTER &&
				stat.getEntityType() != EntityType.GIANT &&
				stat.getEntityType() != EntityType.ILLUSIONER);
	}
	// ==================================================
}