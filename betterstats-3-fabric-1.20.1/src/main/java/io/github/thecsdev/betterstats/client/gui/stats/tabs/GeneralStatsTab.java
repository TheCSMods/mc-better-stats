package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.betterstats.api.util.stats.SUGeneralStat.getGeneralStats;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.api.client.gui.stats.widget.GeneralStatWidget;
import io.github.thecsdev.betterstats.api.util.stats.SUGeneralStat;
import net.minecraft.text.Text;

final @Internal class GeneralStatsTab extends BSStatsTabs<SUGeneralStat>
{
	// ==================================================
	public final @Override Text getName() { return translatable("stat.generalButton"); }
	// --------------------------------------------------
	public final @Override void initStats(final StatsInitContext initContext)
	{
		final var panel = initContext.getStatsPanel();
		final var stats = initContext.getStatsProvider();
		int nextX = panel.getScrollPadding(), nextY = nextX, nextW = panel.getWidth() - (nextY * 2);
		
		for(final SUGeneralStat stat : getGeneralStats(stats, getPredicate(initContext.getFilterSettings())))
		{
			final var statWidget = new GeneralStatWidget(nextX, nextY, nextW, stat);
			panel.addChild(statWidget, true);
			nextY += statWidget.getHeight() + GAP;
		}
	}
	// ==================================================
}