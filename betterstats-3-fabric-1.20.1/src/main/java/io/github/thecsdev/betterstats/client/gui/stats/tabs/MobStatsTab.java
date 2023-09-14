package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.betterstats.api.client.gui.stats.widget.MobStatWidget.SIZE;
import static io.github.thecsdev.betterstats.api.util.stats.SUMobStat.getMobStatsByModGroups;
import static io.github.thecsdev.tcdcommons.api.client.gui.config.TConfigPanelBuilder.nextPanelBottomY;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.api.client.gui.stats.widget.MobStatWidget;
import io.github.thecsdev.betterstats.api.util.stats.SUMobStat;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.util.TUtils;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import net.minecraft.text.Text;

@Internal @Virtual class MobStatsTab extends BSStatsTabs<SUMobStat>
{
	// ==================================================
	public @Virtual @Override Text getName() { return translatable("stat.mobsButton"); }
	// --------------------------------------------------
	public @Virtual @Override void initStats(StatsInitContext initContext)
	{
		final var panel = initContext.getStatsPanel();
		final var stats = initContext.getStatsProvider();
		final var statGroups = getMobStatsByModGroups(stats, getPredicate(initContext.getFilterSettings()));
		
		for(final var statGroup : statGroups.entrySet())
		{
			BSStatsTabs.init_groupLabel(panel, literal(TUtils.getModName(statGroup.getKey())));
			init_stats(panel, statGroup.getValue(), null);
		}
		
		final var summary = init_summary(panel);
		if(summary != null)
		{
			summary.summarizeMobStats(statGroups.values().stream()
				.flatMap(Collection::stream)
				.collect(Collectors.toList()));
			summary.autoHeight();
		}
	}
	// --------------------------------------------------
	protected static void init_stats
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
	// ==================================================
}