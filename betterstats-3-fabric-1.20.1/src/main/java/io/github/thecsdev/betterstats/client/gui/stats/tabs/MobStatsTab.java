package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.betterstats.api.client.gui.stats.widget.MobStatWidget.SIZE;
import static io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils.GAP;
import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.tcdcommons.api.client.gui.config.TConfigPanelBuilder.nextPanelBottomY;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.api.client.gui.stats.widget.MobStatWidget;
import io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils;
import io.github.thecsdev.betterstats.api.client.util.StatFilterSettings;
import io.github.thecsdev.betterstats.api.util.enumerations.FilterGroupBy;
import io.github.thecsdev.betterstats.api.util.enumerations.FilterSortMobsBy;
import io.github.thecsdev.betterstats.api.util.stats.SUMobStat;
import io.github.thecsdev.betterstats.client.gui.screen.hud.BetterStatsHudScreen;
import io.github.thecsdev.betterstats.client.gui.screen.hud.entry.StatsHudMobEntry;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.text.Text;

public @Internal @Virtual class MobStatsTab extends BSStatsTab<SUMobStat>
{
	// ==================================================
	public @Virtual @Override Text getName() { return translatable("stat.mobsButton"); }
	// --------------------------------------------------
	public @Virtual @Override void initStats(StatsInitContext initContext)
	{
		//gather initialization info and filter info
		final var panel = initContext.getStatsPanel();
		final var stats = initContext.getStatsProvider();
		
		final var filters = initContext.getFilterSettings();
		final var predicate = getPredicate(initContext.getFilterSettings());
		
		final var filter_group = filters.getPropertyOrDefault(StatsTabUtils.FILTER_ID_GROUP, FilterGroupBy.DEFAULT);
		final var filter_sort = filters.getPropertyOrDefault(StatsTabUtils.FILTER_ID_SORT_MOBS, FilterSortMobsBy.DEFAULT);
		
		
		//obtain stats and group/sort them
		Map<Text, List<SUMobStat>> statGroups = null;
		switch(filter_group)
		{
			case ALL:
				statGroups = new LinkedHashMap<>();
				statGroups.put(literal("*"), SUMobStat.getMobStats(stats, predicate));
				break;
			default:
				statGroups = SUMobStat.getMobStatsByModGroupsB(stats, predicate);
				break;
		}
		filter_sort.sortMobStats(statGroups);
		
		//initialize stats GUI
		for(final var statGroup : statGroups.entrySet())
		{
			final var group = statGroup.getKey();
			StatsTabUtils.initGroupLabel(panel, group != null ? group : literal("*"));
			initStats(panel, statGroup.getValue(), widget -> processWidget(widget));
		}
		
		final var summary = initStatsSummary(panel);
		if(summary != null)
		{
			summary.summarizeMobStats(statGroups.values().stream()
				.flatMap(Collection::stream)
				.collect(Collectors.toList()));
			summary.autoHeight();
		}
	}
	// --------------------------------------------------
	protected @Virtual @Override void initExtraFilters(FiltersInitContext initContext)
	{
		StatsTabUtils.initGroupByFilter(initContext);
		StatsTabUtils.initSortMobsByFilter(initContext);
	}
	
	protected @Virtual @Override Predicate<SUMobStat> getPredicate(StatFilterSettings filterSettings)
	{
		//super predicate, and, hide misc entities unless they aren't empty
		return super.getPredicate(filterSettings)
				.and(stat -> stat.getEntityType().getSpawnGroup() != SpawnGroup.MISC || !stat.isEmpty());
	}
	// ==================================================
	protected @Virtual void processWidget(MobStatWidget widget)
	{
		widget.eContextMenu.register((__, cMenu) ->
		{
			cMenu.addButton(translatable("betterstats.client.gui.screen.hud.betterstatshudscreen.pin_stat"), ___ ->
			{
				final var hud = BetterStatsHudScreen.getInstance();
				hud.setParentScreen(MC_CLIENT.currentScreen);
				hud.addEntry(new StatsHudMobEntry(widget.getStat()));
				MC_CLIENT.setScreen(hud.getAsScreen());
			});
			cMenu.addButton(translatable("mco.selectServer.close"), ___ -> {});
		});
	}
	// --------------------------------------------------
	protected static void initStats
	(TPanelElement panel, Collection<SUMobStat> stats, Consumer<MobStatWidget> processWidget)
	{
		final int wmp = panel.getWidth() - (panel.getScrollPadding() * 2); //width minus padding
		int nextX = panel.getScrollPadding();
		int nextY = nextPanelBottomY(panel) - panel.getY();
		
		for(final SUMobStat stat : stats)
		{
			final var statElement = new MobStatWidget(nextX, nextY, stat);
			panel.addChild(statElement, true);
			if(processWidget != null)
				processWidget.accept(statElement);
			
			nextX += SIZE + GAP;
			if(nextX + SIZE >= wmp)
			{
				nextX = panel.getScrollPadding();
				nextY = (nextPanelBottomY(panel) - panel.getY()) + GAP;
			}
		}
	}
	// ==================================================
}