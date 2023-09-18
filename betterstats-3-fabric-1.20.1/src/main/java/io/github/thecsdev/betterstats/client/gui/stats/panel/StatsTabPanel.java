package io.github.thecsdev.betterstats.client.gui.stats.panel;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.api.client.gui.panel.BSComponentPanel;
import io.github.thecsdev.betterstats.api.client.gui.widget.ScrollBarWidget;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab.StatsInitContext;
import io.github.thecsdev.betterstats.api.client.util.StatFilterSettings;
import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.client.gui.stats.panel.StatFiltersPanel.StatFiltersPanelProxy;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;
import net.minecraft.text.Text;

public final class StatsTabPanel extends BSComponentPanel
{
	// ==================================================
	public static final Text TXT_NO_STATS_YET = translatable("betterstats.client.gui.stats.panel.statstabpanel.no_stats_yet");
	// --------------------------------------------------
	protected final StatsTabPanelProxy proxy;
	// ==================================================
	public StatsTabPanel(int x, int y, int width, int height, StatsTabPanelProxy proxy) throws NullPointerException
	{
		super(x, y, width, height);
		this.proxy = Objects.requireNonNull(proxy);
	}
	// ==================================================
	/**
	 * Returns the {@link StatsTabPanelProxy} associated
	 * with this {@link StatsTabPanel}.
	 */
	public final StatsTabPanelProxy getProxy() { return this.proxy; }
	// --------------------------------------------------
	protected final @Override void init()
	{
		//create and add the panel where the stats will be initialized
		final var panel = new TPanelElement(0, 0, getWidth() - 8, getHeight());
		panel.setScrollFlags(TPanelElement.SCROLL_VERTICAL);
		panel.setScrollPadding(10);
		panel.setSmoothScroll(true);
		panel.setBackgroundColor(0);
		panel.setOutlineColor(0);
		addChild(panel, true);
		
		//create a scroll-bar for the stats panel
		final var scroll_panel = new ScrollBarWidget(panel.getWidth(), 0, 8, panel.getHeight(), panel);
		addChild(scroll_panel, true);
		
		//initialize the panel
		final StatsTab selTab = this.proxy.getSelectedStatsTab();
		if(selTab == null) return;
		final IStatsProvider statsProvider = this.proxy.getStatsProvider();
		final StatFilterSettings filterSettings = this.proxy.getFilterSettings();
		
		selTab.initStats(new StatsInitContext()
		{
			public IStatsProvider getStatsProvider() { return statsProvider; }
			public TPanelElement getStatsPanel() { return panel; }
			public StatFilterSettings getFilterSettings() { return filterSettings; }
		});
		
		//handle cases where no GUI elements get added (no stats to show yet)
		if(panel.getChildren().size() == 0)
			init_noStatsLabel(panel);
	}
	// --------------------------------------------------
	private final @Internal TLabelElement init_noStatsLabel(TPanelElement panel)
	{
		final int sp = panel.getScrollPadding();
		final var lbl = new TLabelElement(sp, sp, panel.getWidth() - (sp*2), panel.getHeight() - (sp*2));
		lbl.setText(TXT_NO_STATS_YET);
		lbl.setTextHorizontalAlignment(HorizontalAlignment.CENTER);
		panel.addChild(lbl, true);
		return lbl;
	}
	// ==================================================
	/**
	 * A component that provides the {@link StatsTabPanel} with
	 * the necessary information to operate properly.
	 */
	public static interface StatsTabPanelProxy
	{
		/**
		 * Returns the {@link IStatsProvider} that will be used
		 * to visually display the player statistics.
		 */
		public IStatsProvider getStatsProvider();
		
		/**
		 * Returns the currently selected {@link StatsTab} that
		 * will be used to display player statistics.
		 */
		public StatsTab getSelectedStatsTab();
		
		/**
		 * @see StatFiltersPanelProxy#getFilterSettings()
		 */
		public StatFilterSettings getFilterSettings();
	}
	// ==================================================
}