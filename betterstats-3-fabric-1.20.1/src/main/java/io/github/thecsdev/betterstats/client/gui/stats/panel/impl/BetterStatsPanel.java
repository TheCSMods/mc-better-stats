package io.github.thecsdev.betterstats.client.gui.stats.panel.impl;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.api.client.gui.panel.BSComponentPanel;
import io.github.thecsdev.betterstats.api.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.betterstats.api.client.util.StatFilterSettings;
import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.client.gui.stats.panel.ActionBarPanel;
import io.github.thecsdev.betterstats.client.gui.stats.panel.MenuBarPanel;
import io.github.thecsdev.betterstats.client.gui.stats.panel.StatFiltersPanel;
import io.github.thecsdev.betterstats.client.gui.stats.panel.StatsTabPanel;
import io.github.thecsdev.betterstats.client.gui.stats.panel.ActionBarPanel.ActionBarPanelProxy;
import io.github.thecsdev.betterstats.client.gui.stats.panel.StatFiltersPanel.StatFiltersPanelProxy;

/**
 * A {@link BSComponentPanel} that usually goes on the {@link BetterStatsScreen}.<p>
 * It displays player statistics, alongside other components such as the
 * {@link StatFiltersPanel} and {@link MenuBarPanel}.
 */
public final class BetterStatsPanel extends BSComponentPanel
{
	// ==================================================
	protected final BetterStatsPanelProxy proxy;
	// --------------------------------------------------
	protected @Nullable MenuBarPanel panel_menu;
	protected @Nullable StatFiltersPanel panel_filters;
	protected @Nullable ActionBarPanel panel_actionBar;
	protected @Nullable StatsTabPanel panel_stats;
	// ==================================================
	public BetterStatsPanel(int x, int y, int width, int height, BetterStatsPanelProxy proxy)
	{
		super(x, y, Math.max(width, 100), Math.max(height, 100));
		this.outlineColor = 0;
		
		this.proxy = Objects.requireNonNull(proxy);
	}
	// ==================================================
	/**
	 * Refreshes the {@link StatsTabPanel}.
	 * @apiNote If this panel is not yet initialized, nothing will happen.
	 */
	public final void refreshStatsTab() { if(this.panel_stats != null) this.panel_stats.refresh(); }
	
	/**
	 * Returns {@link StatsTabPanel#getVerticalScrollBarValue()}.
	 */
	@Internal
	public final double getStatsTabVerticalScrollAmount()
	{
		return (this.panel_stats != null) ? this.panel_stats.getVerticalScrollBarValue() : 0;
	}
	// ==================================================
	protected final @Override void init()
	{
		//calculate dimensions and stuff
		final int gap = 5;
		final int panelX = (getWidth() / 18), panelW = (getWidth() - (panelX * 2));
		final int panelY = (MenuBarPanel.HEIGHT + gap), panelH = (getHeight() - (panelY + gap));
		
		//add panels
		this.panel_menu = new MenuBarPanel(
				panelX, 0,
				panelW,
				new MenuBarPanelProxyImpl(this));
		addChild(this.panel_menu, true);
		
		this.panel_filters = new StatFiltersPanel(
				panelX, panelY,
				(panelW / 3) - gap, panelH - (ActionBarPanel.HEIGHT + gap),
				new StatFiltersPanelProxyImpl(this));
		addChild(this.panel_filters, true);
		
		this.panel_actionBar = new ActionBarPanel(
				panelX, panelY + panelH - ActionBarPanel.HEIGHT,
				this.panel_filters.getWidth(),
				new ActionBarPanelProxy() {});
		addChild(this.panel_actionBar, true);
		
		this.panel_stats = new StatsTabPanel(
				panelX + this.panel_filters.getWidth() + gap, panelY,
				panelW - (this.panel_filters.getWidth() + gap), panelH,
				new StatsTabPanelProxyImpl(this));
		addChild(this.panel_stats, true);
	}
	// ==================================================
	/**
	 * A component that provides the {@link BetterStatsPanel} with
	 * the necessary information to operate properly.
	 */
	public static interface BetterStatsPanelProxy
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
		 * Sets the currently selected {@link StatsTab} that
		 * will be used to display player statistics.
		 * @param statsTab The new {@link StatsTab}.
		 * 
		 * @apiNote Do not call {@link BetterStatsPanel#refresh()} here,
		 * as it will be done automatically.
		 */
		public void setSelectedStatsTab(StatsTab statsTab);
		
		/**
		 * @see StatFiltersPanelProxy#getFilterSettings()
		 * @apiNote Must not be {@code null}.
		 */
		public StatFilterSettings getFilterSettings();
	}
	// ==================================================
}