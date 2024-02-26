package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget.SIZE;
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
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget;
import io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils;
import io.github.thecsdev.betterstats.api.util.enumerations.FilterGroupBy;
import io.github.thecsdev.betterstats.api.util.enumerations.FilterSortItemsBy;
import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.api.util.stats.SUItemStat;
import io.github.thecsdev.betterstats.client.gui.screen.hud.BetterStatsHudScreen;
import io.github.thecsdev.betterstats.client.gui.screen.hud.entry.StatsHudItemEntry;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import net.minecraft.text.Text;

public @Internal @Virtual class ItemStatsTab extends BSStatsTab<SUItemStat>
{
	// ==================================================
	public @Virtual @Override Text getName() { return translatable("stat.itemsButton"); }
	// --------------------------------------------------
	public @Virtual @Override void initStats(StatsInitContext initContext)
	{
		//gather initialization info and filter info
		final var panel = initContext.getStatsPanel();
		final var stats = initContext.getStatsProvider();
		
		final var filters = initContext.getFilterSettings();
		final var predicate = getPredicate(initContext.getFilterSettings());
		
		final var filter_group = filters.getPropertyOrDefault(StatsTabUtils.FILTER_ID_GROUP, FilterGroupBy.DEFAULT);
		final var filter_sort = filters.getPropertyOrDefault(StatsTabUtils.FILTER_ID_SORT_ITEMS, FilterSortItemsBy.DEFAULT);
		
		//obtain stats and group/sort them
		Map<Text, List<SUItemStat>> statGroups = null;
		switch(filter_group)
		{
			case ALL:
				statGroups = new LinkedHashMap<>();
				statGroups.put(literal("*"), SUItemStat.getItemStats(stats, predicate));
				break;
			case MOD:
				statGroups = SUItemStat.getItemStatsByModGroupsB(stats, predicate);
				break;
			default:
				statGroups = getStatsDefault(stats, predicate);
				break;
		}
		filter_sort.sortItemStats(statGroups);
		
		//initialize stats GUI
		for(final var statGroup : statGroups.entrySet())
		{
			final var group = statGroup.getKey();
			StatsTabUtils.initGroupLabel(panel, group != null ? group : literal("*"));
			initStats(panel, statGroup.getValue(), widget -> processWidget(widget));
		}
		
		final var summary = initStatsSummary(panel);
		if(summary != null)
			summary.summarizeItemStats(statGroups.values().stream()
				.flatMap(Collection::stream)
				.collect(Collectors.toList()));
	}
	// --------------------------------------------------
	protected @Virtual @Override void initExtraFilters(FiltersInitContext initContext)
	{
		StatsTabUtils.initGroupByFilter(initContext);
		StatsTabUtils.initSortItemsByFilter(initContext);
	}
	// ==================================================
	protected @Virtual Map<Text, List<SUItemStat>> getStatsDefault(
			IStatsProvider stats,
			@Nullable Predicate<SUItemStat> predicate)
	{
		return SUItemStat.getItemStatsByItemGroupsB(stats, predicate);
	}
	// --------------------------------------------------
	protected @Virtual void processWidget(ItemStatWidget widget)
	{
		widget.eContextMenu.register((__, cMenu) ->
		{
			cMenu.addButton(BST.hud_pinStat(), ___ ->
			{
				final var hud = BetterStatsHudScreen.getInstance();
				hud.setParentScreen(MC_CLIENT.currentScreen);
				hud.addEntry(new StatsHudItemEntry(widget.getStat()));
				MC_CLIENT.setScreen(hud.getAsScreen());
			});
			cMenu.addButton(translatable("mco.selectServer.close"), ___ -> {});
		});
	}
	// --------------------------------------------------
	protected static void initStats(
			TPanelElement panel,
			Collection<SUItemStat> stats,
			Consumer<ItemStatWidget> processWidget)
	{
		final int wmp = panel.getWidth() - (panel.getScrollPadding() * 2); //width minus padding
		int nextX = panel.getScrollPadding();
		int nextY = nextPanelBottomY(panel) - panel.getY();
		
		for(final SUItemStat stat : stats)
		{
			final var statElement = new ItemStatWidget(nextX, nextY, stat);
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