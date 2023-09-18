package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.betterstats.client.gui.stats.panel.StatFiltersPanel.FILTER_ID_SEARCH;
import static io.github.thecsdev.betterstats.client.gui.stats.panel.StatFiltersPanel.FILTER_ID_SHOWEMPTY;
import static io.github.thecsdev.tcdcommons.api.client.gui.config.TConfigPanelBuilder.nextPanelBottomY;
import static io.github.thecsdev.tcdcommons.api.client.gui.config.TConfigPanelBuilder.nextPanelVerticalRect;
import static io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext.DEFAULT_TEXT_COLOR;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.function.Predicate;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.api.client.gui.stats.panel.StatsSummaryPanel;
import io.github.thecsdev.betterstats.api.client.gui.stats.widget.GeneralStatWidget;
import io.github.thecsdev.betterstats.api.client.gui.widget.SelectStatsTabWidget;
import io.github.thecsdev.betterstats.api.client.registry.BSStatsTabs;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.betterstats.api.client.util.StatFilterSettings;
import io.github.thecsdev.betterstats.api.util.stats.SUStat;
import io.github.thecsdev.betterstats.client.gui.stats.panel.StatFiltersPanel;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TCheckboxWidget;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TTextFieldWidget;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;
import net.minecraft.text.Text;

/**
 * Contains {@link StatsTab}s that belong to {@link BetterStats}.
 */
@Internal abstract class BSStatsTab<S extends SUStat<?>> extends StatsTab
{
	// ==================================================
	@Internal BSStatsTab() {}
	// ==================================================
	public static final @Internal int GAP = 3;
	// ==================================================
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
		final var n1 = nextPanelVerticalRect(panel);
		final var select_tab = new SelectStatsTabWidget(n1.x, n1.y + panel.getScrollPadding(), n1.width, n1.height);
		select_tab.setSelected(initContext.getSelectedStatsTab());
		select_tab.eSelectionChanged.register((__,sel) ->
		{
			initContext.setSelectedStatsTab(sel.getStatsTab());
			initContext.refreshStatsTab();
		});
		panel.addChild(select_tab, false);
		
		//init the search bar
		final var n2 = nextPanelVerticalRect(panel);
		final var input_search = new TTextFieldWidget(n2.x, n2.y + GAP, n2.width, n2.height);
		input_search.setInput(filterSettings.getPropertyOrDefault(FILTER_ID_SEARCH, ""));
		input_search.eTextChanged.register((__, txt) ->
		{
			filterSettings.setProperty(FILTER_ID_SEARCH, (String)txt);
			initContext.refreshStatsTab();
		});
		panel.addChild(input_search, false);
		
		//init the "show empty stats" checkbox
		final var n3 = nextPanelVerticalRect(panel);
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
		final int nextY = (nextPanelBottomY(panel) - panel.getY()) + (panel.getChildren().size() != 0 ? 10 : 0);
		final int nextW = panel.getWidth() - (nextX * 2);
		
		final var label = new TLabelElement(nextX, nextY, nextW, GeneralStatWidget.HEIGHT, text);
		label.setTextColor(BSStatsTabs.COLOR_SPECIAL);
		label.setTextSideOffset(0);
		panel.addChild(label, true);
		return label;
	}
	
	@Internal static @Nullable StatsSummaryPanel init_summary(TPanelElement panel)
	{
		//do not summarize if no children are present
		if(panel.getChildren().size() < 1) return null;
		
		//summary group label
		final var lbl = init_groupLabel(panel, literal("\u2190 \u2022 \u2192"));
		lbl.setTextHorizontalAlignment(HorizontalAlignment.CENTER);
		
		//init summary panel
		final var n1 = nextPanelVerticalRect(panel);
		final var summary = new StatsSummaryPanel(n1.x, n1.y + GAP, n1.width);
		panel.addChild(summary, false);
		return summary;
	}
	// ==================================================
}