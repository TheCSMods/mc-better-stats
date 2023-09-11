package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.betterstats.api.util.stats.SUMobStat.getMobStatsByModGroups;
import static io.github.thecsdev.betterstats.client.gui.stats.panel.StatFiltersPanel.FILTER_ID_SEARCH;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.function.Predicate;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.api.client.util.StatFilterSettings;
import io.github.thecsdev.betterstats.api.util.stats.SUMobStat;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.util.TUtils;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.text.Text;

final @Internal class MonstersHuntedStatsTab extends MobStatsTab
{
	// ==================================================
	public final @Override Text getName() { return translatable("advancements.adventure.kill_all_mobs.title"); }
	// --------------------------------------------------
	public @Virtual @Override void initStats(StatsInitContext initContext)
	{
		final var panel = initContext.getStatsPanel();
		final var stats = initContext.getStatsProvider();
		
		for(final var statGroup : getMobStatsByModGroups(stats, getPredicate(initContext.getFilterSettings())).entrySet())
		{
			BSStatsTabs.init_groupLabel(panel, literal(TUtils.getModName(statGroup.getKey())));
			init_stats(panel, statGroup.getValue(), widget ->
			{
				if(widget.getStat().kills > 0) widget.setOutlineColor(COLOR_SPECIAL);
				else if(!widget.getStat().isEmpty()) widget.setOutlineColor(TPanelElement.COLOR_OUTLINE);
			});
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