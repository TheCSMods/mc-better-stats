package io.github.thecsdev.betterstats.client.gui.stats.panel;

import java.awt.Rectangle;
import java.util.Objects;

import io.github.thecsdev.betterstats.api.client.gui.panel.BSComponentPanel;
import io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils;
import io.github.thecsdev.betterstats.api.client.gui.widget.ScrollBarWidget;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab.FiltersInitContext;
import io.github.thecsdev.betterstats.api.client.util.StatFilterSettings;
import io.github.thecsdev.betterstats.client.gui.stats.panel.impl.BetterStatsPanel.BetterStatsPanelProxy;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TTextureElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.util.UITexture;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;
import net.minecraft.text.Text;

public final class StatFiltersPanel extends BSComponentPanel
{
	// ==================================================
	public static final Text TXT_FILTERS    = BST.filters();
	public static final Text TXT_NO_FILTERS = BST.filter_noFiltersQuestion();
	public static final UITexture TEX_NO_FILTERS = new UITexture(BS_WIDGETS_TEXTURE, new Rectangle(180, 180, 64, 64));
	// --------------------------------------------------
	private final StatFiltersPanelProxy proxy;
	// ==================================================
	public StatFiltersPanel(int x, int y, int width, int height, StatFiltersPanelProxy proxy) throws NullPointerException
	{
		super(x, y, width, height);
		this.proxy = Objects.requireNonNull(proxy);
	}
	// ==================================================
	/**
	 * Returns the {@link StatFiltersPanelProxy} associated
	 * with this {@link StatFiltersPanel}.
	 */
	public final StatFiltersPanelProxy getProxy() { return this.proxy; }
	// --------------------------------------------------
	protected final @Override void init()
	{
		//create a panel for the filters
		final var panel = new TPanelElement(0, 0, getWidth() - 8, getHeight());
		panel.setScrollFlags(TPanelElement.SCROLL_VERTICAL);
		panel.setScrollPadding(10);
		panel.setSmoothScroll(true);
		panel.setBackgroundColor(0);
		panel.setOutlineColor(0);
		addChild(panel, true);
		
		//create a scroll bar for the panel
		final var scroll_panel = new ScrollBarWidget(panel.getWidth(), 0, 8, panel.getHeight(), panel);
		addChild(scroll_panel, true);
		
		//init filter gui
		final var statsTab = this.proxy.getSelectedStatsTab();
		if(statsTab == null) return; //safety check; should not be null tho
		final var filterSettings = this.proxy.getFilterSettings();
		
		statsTab.initFilters(new FiltersInitContext()
		{
			public TPanelElement getFiltersPanel() { return panel; }
			public StatFilterSettings getFilterSettings() { return filterSettings; }
			public void refreshStatsTab() { StatFiltersPanel.this.proxy.refreshStatsTab(); }
			public StatsTab getSelectedStatsTab() { return statsTab; }
			public void setSelectedStatsTab(StatsTab statsTab) { StatFiltersPanel.this.proxy.setSelectedStatsTab(statsTab); }
		});
		
		//an indicator that there's no filters for the current stats tab
		if(panel.getChildren().size() == 0)
			init_noFilters(panel);
	}
	// --------------------------------------------------
	private final void init_noFilters(TPanelElement panel)
	{
		//calculate image coordinates and size
		final int size = (int)(Math.min(getWidth(), getHeight()) * 0.3f);
		final int x = (getWidth() / 2) - (size / 2);
		final int y = (getHeight() / 2) - (size / 2);
		
		//add image
		final var img = new TTextureElement(x, y - 10, size, size, TEX_NO_FILTERS);
		panel.addChild(img, true);
		
		//add text
		final var label = StatsTabUtils.initGroupLabel(panel, TXT_NO_FILTERS);
		label.setTextHorizontalAlignment(HorizontalAlignment.CENTER);
		label.setTextColor(0xFFFFFFFF);
	}
	// ==================================================
	/**
	 * A component that provides the {@link StatFiltersPanel} with
	 * the necessary information to operate properly.
	 */
	public static interface StatFiltersPanelProxy
	{
		/**
		 * Stores the "filter settings" aka user's stat filter
		 * preferences that apply for the current session.
		 * @apiNote Must not be {@code null}.
		 */
		public StatFilterSettings getFilterSettings();
		
		/**
		 * @see BetterStatsPanelProxy#getSelectedStatsTab()
		 */
		public StatsTab getSelectedStatsTab();
		
		/**
		 * @see BetterStatsPanelProxy#setSelectedStatsTab(StatsTab)
		 */
		public void setSelectedStatsTab(StatsTab statsTab);
		
		/**
		 * Refreshes the {@link StatsTabPanel}.
		 */
		public void refreshStatsTab();
	}
	// ==================================================
}