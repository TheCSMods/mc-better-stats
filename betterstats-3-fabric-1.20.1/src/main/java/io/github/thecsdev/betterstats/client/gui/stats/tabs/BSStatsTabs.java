package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.betterstats.BetterStats.getModID;
import static io.github.thecsdev.betterstats.client.gui.stats.panel.StatFiltersPanel.FILTER_ID_SEARCH;
import static io.github.thecsdev.betterstats.client.gui.stats.panel.StatFiltersPanel.FILTER_ID_SHOWEMPTY;
import static io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext.DEFAULT_TEXT_COLOR;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.function.Predicate;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.api.client.gui.stats.widget.GeneralStatWidget;
import io.github.thecsdev.betterstats.api.client.gui.widget.SelectStatsTabWidget;
import io.github.thecsdev.betterstats.api.client.registry.BSClientRegistries;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.betterstats.api.client.util.StatFilterSettings;
import io.github.thecsdev.betterstats.api.util.stats.SUStat;
import io.github.thecsdev.betterstats.client.gui.stats.panel.StatFiltersPanel;
import io.github.thecsdev.tcdcommons.api.client.gui.TElement;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TCheckboxWidget;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TTextFieldWidget;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Contains {@link StatsTab}s that belong to {@link BetterStats}.
 */
//TODO - The API looks nice, but the internal code is a mess. Sort all of these classes's code.
public @Internal abstract class BSStatsTabs<S extends SUStat<?>> extends StatsTab
{
	// ==================================================
	@Internal BSStatsTabs() {}
	// ==================================================
	//the spacing between GUI stat elements
	@Internal static final int GAP = 3;
	@Internal public static final int COLOR_SPECIAL = Color.YELLOW.getRGB();
	// --------------------------------------------------
	public static final StatsTab GENERAL         = new GeneralStatsTab();
	public static final StatsTab ITEMS           = new ItemStatsTab();
	public static final StatsTab ENTITIES        = new MobStatsTab();
	public static final StatsTab FOOD_STUFFS     = new FoodStuffsStatsTab();
	public static final StatsTab MONSTERS_HUNTED = new MonstersHuntedStatsTab();
	// ==================================================
	/**
	 * Registers the {@link BSStatsTabs} to the {@link BSClientRegistries#STATS_TAB} registry.
	 * @apiNote May only be called once.
	 */
	public static void register() {}
	static
	{
		final String modId = getModID();
		BSClientRegistries.STATS_TAB.register(new Identifier(modId, "general"), GENERAL);
		BSClientRegistries.STATS_TAB.register(new Identifier(modId, "items"), ITEMS);
		BSClientRegistries.STATS_TAB.register(new Identifier(modId, "entities"), ENTITIES);
		BSClientRegistries.STATS_TAB.register(new Identifier(modId, "food_stuffs"), FOOD_STUFFS);
		BSClientRegistries.STATS_TAB.register(new Identifier(modId, "monsters_hunted"), MONSTERS_HUNTED);
	}
	// ==================================================s
	public @Virtual @Override void initFilters(FiltersInitContext initContext)
	{
		//obtain important stuff
		final var filterSettings = initContext.getFilterSettings();
		final var panel = initContext.getFiltersPanel();
		
		//init the filters label
		final var lbl_filters = init_groupLabel(panel, StatFiltersPanel.TXT_FILTERS);
		lbl_filters.setTextHorizontalAlignment(HorizontalAlignment.CENTER);
		lbl_filters.setTextColor(DEFAULT_TEXT_COLOR);
		
		//init the stats tab select widget
		final var n1 = nextVerticalRect(panel);
		final var select_tab = new SelectStatsTabWidget(n1.x, n1.y + panel.getScrollPadding(), n1.width, n1.height);
		select_tab.setSelected(initContext.getSelectedStatsTab());
		select_tab.eSelectionChanged.register((__,sel) ->
		{
			initContext.setSelectedStatsTab(sel.getStatsTab());
			initContext.refreshStatsTab();
		});
		panel.addChild(select_tab, false);
		
		//init the search bar
		final var n2 = nextVerticalRect(panel);
		final var input_search = new TTextFieldWidget(n2.x, n2.y + GAP, n2.width, n2.height);
		input_search.setInput(filterSettings.getPropertyOrDefault(FILTER_ID_SEARCH, ""));
		input_search.eTextChanged.register((__, txt) ->
		{
			filterSettings.setProperty(FILTER_ID_SEARCH, (String)txt);
			initContext.refreshStatsTab();
		});
		panel.addChild(input_search, false);
		
		//init the "show empty stats" checkbox
		final var n3 = nextVerticalRect(panel);
		final var check_showEmpty = new TCheckboxWidget(n3.x, n3.y + GAP, n3.width, n3.height);
		check_showEmpty.setText(translatable("betterstats.api.client.gui.stats.panel.statfilterspanel.show_empty_stats"));
		check_showEmpty.setChecked(filterSettings.getPropertyOrDefault(FILTER_ID_SHOWEMPTY, false));
		check_showEmpty.eClicked.register(__ ->
		{
			filterSettings.setProperty(FILTER_ID_SHOWEMPTY, check_showEmpty.getChecked());
			initContext.refreshStatsTab();
		});
		panel.addChild(check_showEmpty, false);
	}
	// --------------------------------------------------
	protected @Virtual Predicate<S> getPredicate(StatFilterSettings filterSettings)
	{
		final String sq = filterSettings.getPropertyOrDefault(FILTER_ID_SEARCH, "");
		final boolean se = filterSettings.getPropertyOrDefault(FILTER_ID_SHOWEMPTY, false);
		return stat -> stat.matchesSearchQuery(sq) && (se || !stat.isEmpty());
	}
	// ==================================================
	/**
	 * Initializes the GUI for a "group label".
	 */
	@Internal static TLabelElement init_groupLabel(TPanelElement panel, Text text)
	{
		final int nextX = panel.getScrollPadding();
		final int nextY = (nextBottomY(panel) - panel.getY()) + (panel.getChildren().size() != 0 ? 10 : 0);
		final int nextW = panel.getWidth() - (nextX * 2);
		
		final var label = new TLabelElement(nextX, nextY, nextW, GeneralStatWidget.HEIGHT, text);
		label.setTextColor(COLOR_SPECIAL);
		label.setTextSideOffset(0);
		panel.addChild(label, true);
		return label;
	}
	// --------------------------------------------------
	/**
	 * Returns the next free (global) Y coordinate at which to
	 * place the next {@link TElement} in a given {@link TPanelElement}.
	 */
	@Internal static int nextBottomY(TPanelElement panel)
	{
		final TElement bottom = panel.getChildren().getTopmostElements().Item2;
		return (bottom != null) ? bottom.getEndY() : panel.getY() + panel.getScrollPadding();
	}
	
	/**
	 * Returns the next free (global-coordinate) space in the vertical direction for the next GUI element.
	 */
	@Internal static Rectangle nextVerticalRect(TPanelElement panel)
	{
		final int sp = panel.getScrollPadding();
		return new Rectangle(panel.getX() + sp, nextBottomY(panel), panel.getWidth() - (sp*2), 20);
	}
	// ==================================================
}