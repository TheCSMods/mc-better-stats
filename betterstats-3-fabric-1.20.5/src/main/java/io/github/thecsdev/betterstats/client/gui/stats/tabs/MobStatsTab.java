package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.betterstats.api.client.gui.stats.widget.MobStatWidget.SIZE;
import static io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils.GAP;
import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.tcdcommons.api.client.gui.config.TConfigPanelBuilder.nextPanelBottomY;
import static io.github.thecsdev.tcdcommons.api.util.TUtils.safeSubList;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.api.client.gui.stats.widget.MobStatWidget;
import io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils;
import io.github.thecsdev.betterstats.api.client.util.StatFilterSettings;
import io.github.thecsdev.betterstats.api.util.enumerations.FilterGroupBy;
import io.github.thecsdev.betterstats.api.util.enumerations.FilterSortMobsBy;
import io.github.thecsdev.betterstats.api.util.stats.SUMobStat;
import io.github.thecsdev.betterstats.client.gui.screen.hud.BetterStatsHudScreen;
import io.github.thecsdev.betterstats.client.gui.screen.hud.entry.StatsHudMobEntry;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.text.Text;

public @Internal @Virtual class MobStatsTab extends BSStatsTab<SUMobStat>
{
	// ==================================================
	private static final int ITEMS_PER_PAGE = 100;
	// ==================================================
	public @Virtual @Override Text getName() { return translatable("stat.mobsButton"); }
	// --------------------------------------------------
	public @Virtual @Override void initStats(StatsInitContext initContext)
	{
		//gather initialization info and filter info
		final var panel = initContext.getStatsPanel();
		final var stats = initContext.getStatsProvider();
		
		final var filters = initContext.getFilterSettings();
		final var filter_group = filters.getPropertyOrDefault(StatsTabUtils.FILTER_ID_GROUP, FilterGroupBy.DEFAULT);
		final var filter_sort = filters.getPropertyOrDefault(StatsTabUtils.FILTER_ID_SORT_MOBS, FilterSortMobsBy.DEFAULT);
		
		//obtain stats and sort them
		final var mobStats = SUMobStat.getMobStats(stats, getPredicate(filters));
		final int mobStatsSize = mobStats.size();
		filter_sort.sortMobStats(mobStats);
		
		//initialize the GUI
		if(mobStatsSize > 0)
		{
			//top page chooser
			initPageChooser(initContext, mobStatsSize, ITEMS_PER_PAGE);
			
			//init stats, grouped
			{
				//paginate items
				final int maxPages = Math.max((int)Math.ceil((double)mobStatsSize / ITEMS_PER_PAGE), 1);
				final int page     = Math.min(getPageFilter(filters).get(), maxPages);
				final int from = Math.max(page - 1, 0) * ITEMS_PER_PAGE;
				final int to   = Math.max(Math.min(page * ITEMS_PER_PAGE, mobStatsSize), from);
				final var subl = safeSubList(mobStats, from, to);
				
				//group the paginated mobs
				final Map<Text, List<SUMobStat>> statGroups = (filter_group == FilterGroupBy.DEFAULT) ?
						getDefaultGroupFilter().apply(subl) : filter_group.apply(subl);
				
				//init gui for each group
				for(final var statGroup : statGroups.entrySet())
				{
					final var group = statGroup.getKey();
					StatsTabUtils.initGroupLabel(panel, group != null ? group : literal("*"));
					initStats(panel, statGroup.getValue(), widget -> processWidget(widget));
				}
			}
			
			//stats summary
			final var summary = initStatsSummary(panel);
			summary.summarizeMobStats(mobStats);
			
			//bottom page chooser
			initPageChooser(initContext, mobStatsSize, ITEMS_PER_PAGE);
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
	/**
	 * Returns the {@link FilterGroupBy} that'll be used by "default".
	 * @apiNote Must not return {@code null}.
	 */
	protected @Virtual FilterGroupBy getDefaultGroupFilter() { return FilterGroupBy.DEFAULT; }
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
	//
	protected @Virtual void processWidget(MobStatWidget widget)
	{
		widget.eContextMenu.register((__, cMenu) ->
		{
			cMenu.addButton(BST.hud_pinStat(), ___ ->
			{
				final var hud = BetterStatsHudScreen.getInstance();
				hud.setParentScreen(MC_CLIENT.currentScreen);
				hud.addEntry(new StatsHudMobEntry(widget.getStat()));
				MC_CLIENT.setScreen(hud.getAsScreen());
			});
			cMenu.addButton(translatable("mco.selectServer.close"), ___ -> {});
		});
	}
	// ==================================================
}