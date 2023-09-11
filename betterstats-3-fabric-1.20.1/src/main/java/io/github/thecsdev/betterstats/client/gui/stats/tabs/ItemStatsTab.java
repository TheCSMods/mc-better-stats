package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget.SIZE;
import static io.github.thecsdev.betterstats.api.util.stats.SUItemStat.getItemStatsByItemGroups;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Collection;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget;
import io.github.thecsdev.betterstats.api.util.stats.SUItemStat;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.Text;

@Internal @Virtual class ItemStatsTab extends BSStatsTabs<SUItemStat>
{
	// ==================================================
	public @Virtual @Override Text getName() { return translatable("stat.itemsButton"); }
	// --------------------------------------------------
	public @Virtual @Override void initStats(StatsInitContext initContext)
	{
		final var panel = initContext.getStatsPanel();
		final var stats = initContext.getStatsProvider();
		
		for(final var statGroup : getItemStatsByItemGroups(stats, getPredicate(initContext.getFilterSettings())).entrySet())
		{
			final ItemGroup group = statGroup.getKey();
			BSStatsTabs.init_groupLabel(panel, group != null ? group.getDisplayName() : literal("*"));
			init_stats(panel, statGroup.getValue(), null);
		}
	}
	// --------------------------------------------------
	/**
	 * Initializes the GUI for a {@link Collection} of {@link SUItemStat}s.
	 */
	protected static void init_stats
	(TPanelElement panel, Collection<SUItemStat> stats, Consumer<ItemStatWidget> processWidget)
	{
		final int wmp = panel.getWidth() - (panel.getScrollPadding() * 2); //width minus padding
		int nextX = panel.getScrollPadding();
		int nextY = BSStatsTabs.nextBottomY(panel) - panel.getY();
		
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
				nextY = (BSStatsTabs.nextBottomY(panel) - panel.getY()) + GAP;
			}
		}
	}
	// ==================================================
}