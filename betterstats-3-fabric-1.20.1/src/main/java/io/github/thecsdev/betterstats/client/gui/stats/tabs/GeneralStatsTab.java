package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.betterstats.client.gui.stats.panel.StatsTabPanel.TXT_NO_STATS_YET;
import static io.github.thecsdev.betterstats.api.util.stats.SUGeneralStat.getGeneralStats;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.api.client.gui.stats.panel.GameProfilePanel;
import io.github.thecsdev.betterstats.api.client.gui.stats.widget.GeneralStatWidget;
import io.github.thecsdev.betterstats.api.util.stats.SUGeneralStat;
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
	public final @Override void initStats(final StatsInitContext initContext)
	{
		final var panel = initContext.getStatsPanel();
		final int sp = panel.getScrollPadding();
		final var statsProvider = initContext.getStatsProvider();
		final var stats = getGeneralStats(statsProvider, getPredicate(initContext.getFilterSettings()));
		
		final var panel_gp = new GameProfilePanel(sp, sp, panel.getWidth() - (sp*2), statsProvider);
		panel.addChild(panel_gp, true);
		
		int nextX = sp, nextY = panel_gp.getHeight() + (sp * 2), nextW = panel.getWidth() - (sp * 2);
		if(stats.size() > 0)
		{
			for(final SUGeneralStat stat : stats)
			{
				final var statWidget = new GeneralStatWidget(nextX, nextY, nextW, stat);
				panel.addChild(statWidget, true);
				nextY += statWidget.getHeight() + GAP;
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