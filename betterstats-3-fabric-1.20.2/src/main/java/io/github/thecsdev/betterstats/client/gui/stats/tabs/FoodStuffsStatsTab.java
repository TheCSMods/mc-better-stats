package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget;
import io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils;
import io.github.thecsdev.betterstats.api.client.registry.BSStatsTabs;
import io.github.thecsdev.betterstats.api.client.util.StatFilterSettings;
import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.api.util.stats.SUItemStat;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import net.minecraft.text.Text;

public final @Internal class FoodStuffsStatsTab extends ItemStatsTab
{
	// ==================================================
	public final @Override Text getName() { return translatable("advancements.husbandry.balanced_diet.title"); }
	// --------------------------------------------------
	protected final @Override Map<Text, List<SUItemStat>> getStatsDefault(
			IStatsProvider stats,
			@Nullable Predicate<SUItemStat> predicate)
	{
		return SUItemStat.getItemStatsByModGroupsB(stats, predicate);
	}
	// --------------------------------------------------
	protected final @Override void processWidget(ItemStatWidget widget)
	{
		super.processWidget(widget);
		if(widget.getStat().used > 0) widget.setOutlineColor(BSStatsTabs.COLOR_SPECIAL);
		else if(!widget.getStat().isEmpty()) widget.setOutlineColor(TPanelElement.COLOR_OUTLINE);
	}
	// --------------------------------------------------
	protected @Virtual @Override Predicate<SUItemStat> getPredicate(StatFilterSettings filterSettings)
	{
		final String sq = filterSettings.getPropertyOrDefault(StatsTabUtils.FILTER_ID_SEARCH, "");
		final Predicate<SUItemStat> sqPred = stat -> stat.matchesSearchQuery(sq);
		return sqPred.and(stat -> stat.getItem().isFood());
	}
	// ==================================================
}