package thecsdev.betterstats.client.gui.panel.stats;

import static thecsdev.betterstats.util.StatUtils.getModName;
import static thecsdev.tcdcommons.api.util.TextUtils.literal;
import static thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Predicate;

import net.minecraft.stat.StatHandler;
import net.minecraft.util.registry.Registry;
import thecsdev.betterstats.api.registry.BetterStatsRegistry;
import thecsdev.betterstats.util.StatUtils;
import thecsdev.betterstats.util.StatUtils.StatUtilsMobStat;
import thecsdev.betterstats.util.StatUtils.StatUtilsStat;
import thecsdev.tcdcommons.api.client.gui.other.TEntityRendererElement;
import thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import thecsdev.tcdcommons.api.client.gui.util.GuiUtils;

public class BSStatPanel_Mobs extends BSStatPanel
{
	// ==================================================
	public BSStatPanel_Mobs(int x, int y, int width, int height) { super(x, y, width, height); }
	public BSStatPanel_Mobs(TPanelElement parentToFill) { super(parentToFill); }
	// ==================================================
	@Override
	public Predicate<StatUtilsStat> getStatPredicate()
	{
		//make sure it is a mob stat, as some subclasses
		//assume the stat is a mob stat
		return stat -> (stat instanceof StatUtilsMobStat);
	}
	// ==================================================
	@Override
	public void init(StatHandler statHandler, Predicate<StatUtilsStat> statFilter)
	{
		var mobStats = StatUtils.getMobStats(statHandler, statFilter.and(getStatPredicate()));
		for(var mobGroup : mobStats.keySet())
		{
			init_groupLabel(literal(getModName(mobGroup)));
			init_mobStats(mobStats.get(mobGroup));
		}
		//if there are no stats...
		if(mobStats.size() == 0) init_noResults();
	}
	
	protected void init_mobStats(ArrayList<StatUtilsMobStat> mobStats)
	{
		//declare the starting XY
		int nextX = getTpeX() + getScrollPadding();
		int nextY = getTpeY() + getScrollPadding();
		
		//calculate nextY based on the last child
		{
			var lastChild = getLastTChild(false);
			if(lastChild != null) nextY = lastChild.getTpeEndY() + 2;
		}
		
		//iterate and add item stat elements
		final int SIZE = 50;
		for(var stat : mobStats)
		{
			//create and add the widget for the stat
			addTChild(createStatWidget(stat, nextX, nextY, SIZE), false);
			
			//increment next XY
			nextX += SIZE + 2;
			if(nextX + SIZE > getTpeEndX() - getScrollPadding())
			{
				nextX = getTpeX() + getScrollPadding();
				nextY += SIZE + 2;
			}
		}
	}
	// ==================================================
	protected BSStatWidget_Mob createStatWidget(StatUtilsMobStat stat, int x, int y, int size)
	{
		return new BSStatWidget_Mob(stat, x, y, size);
	}
	// ==================================================
	protected class BSStatWidget_Mob extends BSStatWidget
	{
		// ----------------------------------------------
		public final StatUtilsMobStat stat;
		// ----------------------------------------------
		public BSStatWidget_Mob(StatUtilsMobStat stat, int x, int y, int size)
		{
			super(x, y, size, size);
			this.stat = Objects.requireNonNull(stat, "stat must not be null.");
			addTChild(new TEntityRendererElement(x, y, size, size, stat.entityType), false);
			
			updateTooltip();
		}
		// ----------------------------------------------
		public @Override void updateTooltip()
		{
			String entityName = stat.label.getString();
			String s0 = translatable("stat_type.minecraft.killed.none", entityName).getString();
			String s1 = translatable("stat_type.minecraft.killed_by.none", entityName).getString();
			
			if(stat.killed != 0)
				s0 = translatable("stat_type.minecraft.killed", Integer.toString(stat.killed), entityName).getString();
			if(stat.killedBy != 0)
				s1 = translatable("stat_type.minecraft.killed_by", entityName, Integer.toString(stat.killedBy)).getString();
			
			setTooltip(literal(s0 + "\n" + s1));
		}
		// ----------------------------------------------
		public @Override boolean mousePressed(int mouseX, int mouseY, int button)
		{
			//handle Wikis
			if(button == 2)
			{
				String url = BetterStatsRegistry.getMobWikiURL(Registry.ENTITY_TYPE.getId(this.stat.entityType));
				if(url != null)
				{
					GuiUtils.showUrlPrompt(url, false);
					//if successful, block the focus by returning false
					return false;
				}
			}
			//return super if all else fails
			return super.mousePressed(mouseX, mouseY, button);
		}
		// ----------------------------------------------
	}
	// ==================================================
}