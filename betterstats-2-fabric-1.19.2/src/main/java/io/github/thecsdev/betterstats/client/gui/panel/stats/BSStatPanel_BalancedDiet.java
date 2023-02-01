package io.github.thecsdev.betterstats.client.gui.panel.stats;

import java.util.function.Predicate;

import io.github.thecsdev.betterstats.util.StatUtils.StatUtilsItemStat;
import io.github.thecsdev.betterstats.util.StatUtils.StatUtilsStat;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.stat.StatHandler;

public class BSStatPanel_BalancedDiet extends BSStatPanel_Items
{
	// ==================================================
	public BSStatPanel_BalancedDiet(int x, int y, int width, int height) { super(x, y, width, height); }
	public BSStatPanel_BalancedDiet(TPanelElement parentToFill) { super(parentToFill); }
	// ==================================================
	@Override
	public void init(StatHandler statHandler, Predicate<StatUtilsStat> statFilter)
	{
		//as for balanced diet stats, items are grouped by mod groups by default
		switch(getFilterGroupBy())
		{
			case None: initByNoGroups(statHandler, statFilter); break;
			default: initByModGroups(statHandler, statFilter); break;
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
		public @Override void postRender(MatrixStack matrices, int mouseX, int mouseY, float deltaTime)
		{
			if(stat.sUsed > 0) drawOutline(matrices, COLOR_GOLD_FOCUSED);
			else if(isFocused())
				drawOutline(matrices, COLOR_NORMAL_FOCUSED);
			else if(isHovered() || (!stat.isEmpty() && stat.sUsed < 1))
				drawOutline(matrices, COLOR_NORMAL_HOVERED);
		}
	}
	// ==================================================
}