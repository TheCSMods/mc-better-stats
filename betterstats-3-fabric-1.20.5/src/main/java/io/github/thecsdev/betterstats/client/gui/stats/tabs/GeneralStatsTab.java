package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils.FILTER_ID_SORT_CUSTOMS;
import static io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils.GAP;
import static io.github.thecsdev.betterstats.api.util.stats.SUGeneralStat.getGeneralStats;
import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.betterstats.client.gui.stats.panel.StatsTabPanel.TXT_NO_STATS_YET;
import static io.github.thecsdev.tcdcommons.api.client.gui.config.TConfigPanelBuilder.nextPanelVerticalRect;
import static io.github.thecsdev.tcdcommons.api.hooks.world.biome.source.BiomeAccessHooks.getBiomeAccessSeed;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.BetterStatsConfig;
import io.github.thecsdev.betterstats.api.client.gui.stats.panel.GameProfilePanel;
import io.github.thecsdev.betterstats.api.client.gui.stats.widget.CustomStatElement;
import io.github.thecsdev.betterstats.api.client.gui.stats.widget.GeneralStatWidget;
import io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils;
import io.github.thecsdev.betterstats.api.util.enumerations.FilterSortCustomsBy;
import io.github.thecsdev.betterstats.api.util.stats.SUGeneralStat;
import io.github.thecsdev.betterstats.client.gui.screen.hud.BetterStatsHudScreen;
import io.github.thecsdev.betterstats.client.gui.screen.hud.entry.StatsHudGeneralEntry;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TFillColorElement;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;
import net.minecraft.text.Text;

public final @Internal class GeneralStatsTab extends BSStatsTab<SUGeneralStat>
{
	// ==================================================
	public final @Override Text getName() { return translatable("stat.generalButton"); }
	// --------------------------------------------------
	protected final @Override void initExtraFilters(FiltersInitContext initContext)
	{
		StatsTabUtils.initSortCustomsByFilter(initContext);
	}
	// ==================================================
	public final @Override void initStats(final StatsInitContext initContext)
	{
		//obtain initialization info
		final var panel = initContext.getStatsPanel();
		final int sp = panel.getScrollPadding();
		
		final var statsProvider = initContext.getStatsProvider();
		final var filterSettings = initContext.getFilterSettings();
		
		//obtain statistics and sort them
		final var stats = getGeneralStats(statsProvider, getPredicate(filterSettings));
		final var sortBy = filterSettings.getProperty(FILTER_ID_SORT_CUSTOMS, FilterSortCustomsBy.DEFAULT);
		if(sortBy != null) sortBy.sortGeneralStats(stats);
		
		// ---------- initialize gui
		//game profile panel
		final var panel_gp = new GameProfilePanel(sp, sp, panel.getWidth() - (sp*2), statsProvider);
		panel.addChild(panel_gp, true);
		
		//world statistics
		if(BetterStatsConfig.DEBUG_MODE)
		{
			StatsTabUtils.initGroupLabel(panel, translatable("createWorld.tab.world.title"))
				.setTextHorizontalAlignment(HorizontalAlignment.CENTER);
			final var n1 = nextPanelVerticalRect(panel);
			final var wsp = new TFillColorElement(n1.x, n1.y, n1.width, (CustomStatElement.HEIGHT * 2) + GAP);
			wsp.setColor(TPanelElement.COLOR_BACKGROUND);
			panel.addChild(wsp, false);
			
			final var world = MC_CLIENT.world;
			final var ws1 = new CustomStatElement(
					wsp.getX(), wsp.getY(), wsp.getWidth(),
					translatable("selectWorld.enterName"),
					literal(Objects.toString((world != null) ? world.getRegistryKey().getValue() : "-"))
				);
			final var ws2 = new CustomStatElement(
					wsp.getX(), wsp.getEndY() - CustomStatElement.HEIGHT, wsp.getWidth(),
					BST.sTab_hashedSeed(),
					literal(Objects.toString(getBiomeAccessSeed(world.getBiomeAccess())))
				);
			wsp.addChild(ws1, false);
			wsp.addChild(ws2, false);
		}
		
		//general statistics
		StatsTabUtils.initGroupLabel(panel, translatable("entity.minecraft.player"))
			.setTextHorizontalAlignment(HorizontalAlignment.CENTER);
		int nextX = sp;
		int nextY = (nextPanelVerticalRect(panel).y - panel.getY()) + GAP;
		int nextW = panel.getWidth() - (sp * 2);
		if(stats.size() > 0)
		{
			for(final SUGeneralStat stat : stats)
			{
				final var statWidget = new GeneralStatWidget(nextX, nextY, nextW, stat);
				panel.addChild(statWidget, true);
				nextY += statWidget.getHeight() + GAP;
				
				statWidget.eContextMenu.register((__, cMenu) ->
				{
					cMenu.addButton(BST.hud_pinStat(), ___ ->
					{
						final var hud = BetterStatsHudScreen.getInstance();
						hud.setParentScreen(MC_CLIENT.currentScreen);
						hud.addEntry(new StatsHudGeneralEntry(statWidget.getStat()));
						MC_CLIENT.setScreen(hud.getAsScreen());
					});
					cMenu.addButton(translatable("mco.selectServer.close"), ___ -> {});
				});
			}
		}
		else
		{
			final var fill = new TFillColorElement(nextX, nextY, nextW, GeneralStatWidget.HEIGHT);
			fill.setColor(TPanelElement.COLOR_BACKGROUND);
			panel.addChild(fill, true);
			
			final var lbl_noStats = new TLabelElement(0, 0, fill.getWidth(), fill.getHeight());
			lbl_noStats.setText(TXT_NO_STATS_YET);
			lbl_noStats.setTextHorizontalAlignment(HorizontalAlignment.CENTER);
			fill.addChild(lbl_noStats, true);
		}
	}
	// ==================================================
}