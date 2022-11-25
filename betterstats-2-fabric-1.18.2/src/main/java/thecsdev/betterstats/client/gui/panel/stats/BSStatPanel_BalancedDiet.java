package thecsdev.betterstats.client.gui.panel.stats;

import static thecsdev.betterstats.util.StatUtils.getModName;
import static thecsdev.tcdcommons.api.util.TextUtils.literal;

import java.util.function.Predicate;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.stat.StatHandler;
import thecsdev.betterstats.util.StatUtils;
import thecsdev.betterstats.util.StatUtils.StatUtilsItemStat;
import thecsdev.betterstats.util.StatUtils.StatUtilsStat;
import thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;

public class BSStatPanel_BalancedDiet extends BSStatPanel_Items
{
	// ==================================================
	public BSStatPanel_BalancedDiet(int x, int y, int width, int height) { super(x, y, width, height); }
	public BSStatPanel_BalancedDiet(TPanelElement parentToFill) { super(parentToFill); }
	// ==================================================
	@Override
	public void init(StatHandler statHandler, Predicate<StatUtilsStat> statFilter)
	{
		var itemStats = StatUtils.getItemStatsByMods(statHandler, statFilter.and(getStatPredicate()));
		for(String iGroup : itemStats.keySet())
		{
			init_groupLabel(literal(getModName(iGroup)));
			init_itemStats(itemStats.get(iGroup));
		}
		//if there are no stats...
		if(itemStats.size() == 0) init_noResults();
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