package io.github.thecsdev.betterstats.client.gui.panel.stats;

import java.util.function.Predicate;

import io.github.thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.betterstats.util.StatUtils.StatUtilsItemStat;
import io.github.thecsdev.betterstats.util.StatUtils.StatUtilsStat;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.stat.StatHandler;

public class BSStatPanel_BalancedDiet extends BSStatPanel_Items
{
	// ==================================================
	public BSStatPanel_BalancedDiet(int x, int y, int width, int height) { super(x, y, width, height); }
	public BSStatPanel_BalancedDiet(TPanelElement parentToFill) { super(parentToFill); }
	// ==================================================
	@Override
	public void init(BetterStatsScreen bss, StatHandler statHandler, Predicate<StatUtilsStat> statFilter)
	{
		//as for balanced diet stats, items are grouped by mod groups by default
		switch(getFilterGroupBy())
		{
			case None: initByNoGroups(bss, statHandler, statFilter); break;
			default: initByModGroups(bss, statHandler, statFilter); break;
		}
	}
	// --------------------------------------------------
	@Override
	public Predicate<StatUtilsStat> getStatPredicate()
	{
		//make sure the item stat is for a food item
		return stat -> (stat instanceof StatUtilsItemStat) && ((StatUtilsItemStat)stat).item.isFood();
	}
	// ==================================================
	protected @Override BSStatWidget_Item createStatWidget(StatUtilsItemStat stat, int x, int y)
	{
		return new BSStatWidget_BalancedDiet(stat, x, y);
	}
	// ==================================================
	protected class BSStatWidget_BalancedDiet extends BSStatWidget_Item
	{
		public BSStatWidget_BalancedDiet(StatUtilsItemStat stat, int x, int y) { super(stat, x, y); }
		public @Override void postRender(DrawContext pencil, int mouseX, int mouseY, float deltaTime)
		{
			if(stat.sUsed > 0) drawOutline(pencil, COLOR_GOLD_FOCUSED);
			else if(isFocused())
				drawOutline(pencil, COLOR_NORMAL_FOCUSED);
			else if(isHovered() || (!stat.isEmpty() && stat.sUsed < 1))
				drawOutline(pencil, COLOR_NORMAL_HOVERED);
		}
	}
	// ==================================================
}