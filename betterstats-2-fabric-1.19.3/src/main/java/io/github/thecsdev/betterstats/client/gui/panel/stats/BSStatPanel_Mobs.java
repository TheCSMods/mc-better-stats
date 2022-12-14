package io.github.thecsdev.betterstats.client.gui.panel.stats;

import static io.github.thecsdev.betterstats.util.StatUtils.getModName;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

import io.github.thecsdev.betterstats.api.registry.BetterStatsRegistry;
import io.github.thecsdev.betterstats.client.gui.panel.BSPanel;
import io.github.thecsdev.betterstats.client.gui_hud.screen.BetterStatsHudScreen;
import io.github.thecsdev.betterstats.client.gui_hud.widget.BSHudStatWidget_Entity;
import io.github.thecsdev.betterstats.util.StatUtils;
import io.github.thecsdev.betterstats.util.StatUtils.StatUtilsMobStat;
import io.github.thecsdev.betterstats.util.StatUtils.StatUtilsStat;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TEntityRendererElement;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TContextMenuPanel;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.util.GuiUtils;
import io.github.thecsdev.tcdcommons.api.client.gui.util.HorizontalAlignment;
import io.github.thecsdev.tcdcommons.api.util.TextUtils;
import net.minecraft.registry.Registries;
import net.minecraft.stat.StatHandler;

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
		//else init total stats as well
		else init_totalStats(mobStats.values());
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
	
	protected void init_totalStats(Collection<ArrayList<StatUtilsMobStat>> mobStats)
	{
		//define KD
		int kills = 0, deaths = 0;
		
		//iterate all stats
		for(var group : mobStats)
		{
			//and count the kills and deaths
			for(var groupItem : group)
			{
				//ignore empty stats
				if(groupItem == null || groupItem.isEmpty())
					continue;
				//count
				kills += groupItem.killed;
				deaths += groupItem.killedBy;
			}
		}
		
		//init a new group
		var glSb = new StringBuilder();
		glSb.append(new char[] { 8592, 32, 8226, 32, 8594 });
		var groupLabel = init_groupLabel(TextUtils.literal(glSb.toString()));
		groupLabel.setHorizontalAlignment(HorizontalAlignment.CENTER);
		
		//declare the starting XY
		int nextX = getTpeX() + getScrollPadding();
		int nextY = getTpeY() + getScrollPadding();
		
		//calculate nextY based on the last child
		{
			var lastChild = getLastTChild(false);
			if(lastChild != null) nextY = lastChild.getTpeEndY() + 2;
		}
		
		//create the panel
		var panel = new BSPanel(nextX, nextY, getTpeWidth() - (getScrollPadding() * 2), 20);
		panel.setScrollPadding(0);
		addTChild(panel, false);
		
		//create the labels
		int leftX = 5, leftW = (panel.getTpeWidth() / 2) - 10;
		int rightX = (panel.getTpeWidth() / 2) + 5, rightW = (panel.getTpeWidth() / 2) - 10;
		
		var lbl_kills_a = new TLabelElement(leftX, 0, leftW, 20, translatable("betterstats.hud.entity.kills"));
		var lbl_kills_b = new TLabelElement(leftX, 0, leftW, 20, literal(Integer.toString(kills)));
		lbl_kills_b.setHorizontalAlignment(HorizontalAlignment.RIGHT);
		
		var lbl_deaths_a = new TLabelElement(rightX, 0, rightW, 20, translatable("betterstats.hud.entity.deaths"));
		var lbl_deaths_b = new TLabelElement(rightX, 0, rightW, 20, literal(Integer.toString(deaths)));
		lbl_deaths_b.setHorizontalAlignment(HorizontalAlignment.RIGHT);
		
		panel.addTChild(lbl_kills_a, true);
		panel.addTChild(lbl_kills_b, true);
		panel.addTChild(lbl_deaths_a, true);
		panel.addTChild(lbl_deaths_b, true);
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
				String url = BetterStatsRegistry.getMobWikiURL(Registries.ENTITY_TYPE.getId(this.stat.entityType));
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
		@Override
		protected void onContextMenu(TContextMenuPanel contextMenu)
		{
			super.onContextMenu(contextMenu);
			contextMenu.addButton(translatable("betterstats.gui.ctx_menu.pin_to_hud"), btn ->
			{
				var bshs = BetterStatsHudScreen.getOrCreateInstance(this.screen);
				getClient().setScreen(bshs);
				bshs.addHudStatWidget(new BSHudStatWidget_Entity(0, 0, stat.statHandler, stat.entityType));
			});
			contextMenu.addButton(translatable("betterstats.gui.ctx_menu.close"), btn -> {});
		}
		// ----------------------------------------------
	}
	// ==================================================
}