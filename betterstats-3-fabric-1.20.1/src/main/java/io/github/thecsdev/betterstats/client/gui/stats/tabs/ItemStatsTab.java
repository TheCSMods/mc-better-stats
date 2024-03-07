package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.betterstats.BetterStats.getModID;
import static io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget.SIZE;
import static io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils.GAP;
import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.tcdcommons.api.client.gui.config.TConfigPanelBuilder.nextPanelBottomY;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget;
import io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils;
import io.github.thecsdev.betterstats.api.util.enumerations.FilterGroupBy;
import io.github.thecsdev.betterstats.api.util.enumerations.FilterSortItemsBy;
import io.github.thecsdev.betterstats.api.util.stats.SUItemStat;
import io.github.thecsdev.betterstats.client.gui.panel.PageChooserPanel;
import io.github.thecsdev.betterstats.client.gui.panel.PageChooserPanel.PageChooserPanelProxy;
import io.github.thecsdev.betterstats.client.gui.screen.hud.BetterStatsHudScreen;
import io.github.thecsdev.betterstats.client.gui.screen.hud.entry.StatsHudItemEntry;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.client.gui.config.TConfigPanelBuilder;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public @Internal @Virtual class ItemStatsTab extends BSStatsTab<SUItemStat>
{
	// ==================================================
	private static final Identifier FILTER_PAGE = new Identifier(getModID(), "item_stats_page");
	private static final int ITEMS_PER_PAGE = 500;
	// ==================================================
	public @Virtual @Override Text getName() { return translatable("stat.itemsButton"); }
	// --------------------------------------------------
	public @Virtual @Override void initStats(StatsInitContext initContext)
	{
		//gather initialization info and filter info
		final var panel = initContext.getStatsPanel();
		final var stats = initContext.getStatsProvider();
		
		final var filters      = initContext.getFilterSettings();
		final var filter_group = filters.getPropertyOrDefault(StatsTabUtils.FILTER_ID_GROUP, FilterGroupBy.DEFAULT);
		final var filter_sort  = filters.getPropertyOrDefault(StatsTabUtils.FILTER_ID_SORT_ITEMS, FilterSortItemsBy.DEFAULT);
		
		//obtain stats and group/sort them
		final var itemStats = SUItemStat.getItemStats(stats, getPredicate(filters));
		final int itemStatsSize = itemStats.size();
		final Map<Text, List<SUItemStat>> statGroups = (filter_group == FilterGroupBy.DEFAULT) ?
			getDefaultGroupFilter().apply(itemStats) : filter_group.apply(itemStats);
		filter_sort.sortItemStats(statGroups);
		
		//initialize stats GUI
		if(statGroups.size() > 0) initPageChooser(initContext, itemStatsSize);
		for(final var statGroup : statGroups.entrySet())
		{
			final var group = statGroup.getKey();
			StatsTabUtils.initGroupLabel(panel, group != null ? group : literal("*"));
			initStats(panel, statGroup.getValue(), widget -> processWidget(widget));
		}
		
		final var summary = initStatsSummary(panel);
		if(summary != null)
		{
			summary.summarizeItemStats(statGroups.values().stream()
				.flatMap(Collection::stream)
				.collect(Collectors.toList()));
			initPageChooser(initContext, itemStatsSize);
		}
	}
	// --------------------------------------------------
	protected @Virtual @Override void initExtraFilters(FiltersInitContext initContext)
	{
		StatsTabUtils.initGroupByFilter(initContext);
		StatsTabUtils.initSortItemsByFilter(initContext);
	}
	// ==================================================
	/**
	 * Returns the {@link FilterGroupBy} that'll be used by "default".
	 * @apiNote Must not return {@code null}.
	 */
	protected @Virtual FilterGroupBy getDefaultGroupFilter() { return FilterGroupBy.DEFAULT; }
	// --------------------------------------------------
	/**
	 * Initializes a {@link Collection} of {@link SUItemStat} onto a {@link TPanelElement}.
	 * @param panel The {@link TPanelElement}.
	 * @param stats The {@link SUItemStat}s to initialize.
	 * @param processWidget Optional {@link Consumer} that allows you to make changes to widgets as they are created.
	 */
	protected @Virtual void initStats(
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
	//
	/**
	 * Default {@link ItemStatWidget} processing logic.<br/>
	 * Primarily used for {@link #initStats(TPanelElement, Collection, Consumer)}.
	 * @param widget The {@link ItemStatWidget}.
	 */
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
	protected @Virtual void initPageChooser(StatsInitContext initContext, int totalItemCount)
	{
		//obtain page filter
		final var filters = initContext.getFilterSettings();
		final var filter_page = filters.getPropertyOrDefault(FILTER_PAGE, new AtomicInteger(1));
		final var filter_maxPages = Math.max(totalItemCount / ITEMS_PER_PAGE, 1);
		filters.setProperty(FILTER_PAGE, filter_page); //set the value if absent, which it will be initially
		
		//obtain the panel, and calculate the next XYWH
		final var panel = initContext.getStatsPanel();
		final var n1 = TConfigPanelBuilder.nextPanelVerticalRect(panel);
		if(panel.getChildren().size() != 0) n1.y += GAP;
		
		//define logic for triggering a manual refresh
		final Runnable triggerRefresh = () -> { panel.clearChildren(); initStats(initContext); };
		
		//create and add the page chooser panel
		final var pc = new PageChooserPanel(n1.x, n1.y, n1.width, new PageChooserPanelProxy()
		{
			public int getPage() { return filter_page.get(); }
			public int getPageCount() { return filter_maxPages; }
			public void onNavigateRight() { filter_page.incrementAndGet(); triggerRefresh.run(); }
			public void onNavigateLeft() { filter_page.decrementAndGet(); triggerRefresh.run(); }
		});
		panel.addChild(pc, false);
	}
	// ==================================================
}