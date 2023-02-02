package io.github.thecsdev.betterstats.client.gui.panel.stats;

import static io.github.thecsdev.betterstats.util.StatUtils.getModName;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import io.github.thecsdev.betterstats.api.registry.BetterStatsRegistry;
import io.github.thecsdev.betterstats.client.gui.panel.BSPanel;
import io.github.thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.betterstats.client.gui_hud.screen.BetterStatsHudScreen;
import io.github.thecsdev.betterstats.client.gui_hud.widget.BSHudStatWidget_Item;
import io.github.thecsdev.betterstats.util.ItemStatEnum;
import io.github.thecsdev.betterstats.util.StatUtils;
import io.github.thecsdev.betterstats.util.StatUtils.StatUtilsItemStat;
import io.github.thecsdev.betterstats.util.StatUtils.StatUtilsStat;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TContextMenuPanel;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.util.GuiUtils;
import io.github.thecsdev.tcdcommons.api.client.gui.util.HorizontalAlignment;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TSelectEnumWidget;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TSelectWidget;
import io.github.thecsdev.tcdcommons.api.util.TextUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.stat.StatHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class BSStatPanel_Items extends BSStatPanel
{
	// ==================================================
	public static enum BSStatPanelItems_SortBy
	{
		Default(literal("-")),
		Mined(translatable("stat_type.minecraft.mined")),
		Crafted(translatable("stat_type.minecraft.crafted")),
		PickedUp(translatable("stat_type.minecraft.picked_up")),
		Dropped(translatable("stat_type.minecraft.dropped")),
		Used(translatable("stat_type.minecraft.used")),
		Broken(translatable("stat_type.minecraft.broken"));
		
		private final MutableText text;
		BSStatPanelItems_SortBy(MutableText text) { this.text = text; }
		public MutableText asText() { return text; }
	}
	// ==================================================
	public BSStatPanel_Items(int x, int y, int width, int height) { super(x, y, width, height); }
	public BSStatPanel_Items(TPanelElement parentToFill) { super(parentToFill); }
	// --------------------------------------------------
	public @Override Predicate<StatUtilsStat> getStatPredicate() { return stat -> (stat instanceof StatUtilsItemStat); }
	
	public @Override TSelectWidget createFilterSortByWidget(BetterStatsScreen bss, int x, int y, int width, int height)
	{
		var sw = new TSelectEnumWidget<BSStatPanelItems_SortBy>(x, y, width, height, BSStatPanelItems_SortBy.class);
		sw.setSelected(bss.cache.getAs("BSStatPanelItems_SortBy", BSStatPanelItems_SortBy.class, BSStatPanelItems_SortBy.Default), false);
		sw.setEnumValueToLabel(newVal -> ((BSStatPanelItems_SortBy)newVal).asText());
		sw.setOnSelectionChange(newVal ->
		{
			bss.cache.set("BSStatPanelItems_SortBy", newVal);
			bss.getStatPanel().init_stats();
		});
		return sw;
	}
	// ==================================================
	@Override
	public void init(BetterStatsScreen bss, StatHandler statHandler, Predicate<StatUtilsStat> statFilter)
	{
		//by default, item stats are grouped by item groups
		switch(getFilterGroupBy())
		{
			case Mod: initByModGroups(bss, statHandler, statFilter); break;
			case None: initByNoGroups(bss, statHandler, statFilter); break;
			default: initByItemGroups(bss, statHandler, statFilter); break;
		}
	}
	
	protected void initByNoGroups(BetterStatsScreen bss, StatHandler statHandler, Predicate<StatUtilsStat> statFilter)
	{
		//get stats
		var itemStats = StatUtils.getItemStatsByMods(statHandler, statFilter.and(getStatPredicate()));
		ArrayList<StatUtilsItemStat> allItems = Lists.newArrayList();
		//merge stats
		for(String iGroup : itemStats.keySet())
			allItems.addAll(itemStats.get(iGroup));
		//init all
		if(itemStats.size() > 0)
		{
			init_groupLabel(literal("*"));
			init_itemStats(bss, allItems);
			init_totalStats(itemStats.values());
		}
		//if there are no stats...
		else init_noResults();
	}
	
	protected void initByItemGroups(BetterStatsScreen bss, StatHandler statHandler, Predicate<StatUtilsStat> statFilter)
	{
		var itemStats = StatUtils.getItemStats(statHandler, statFilter.and(getStatPredicate()));
		for(ItemGroup iGroup : itemStats.keySet())
		{
			Text gLabel = iGroup != null ? iGroup.getDisplayName() : null;
			init_groupLabel(gLabel);
			init_itemStats(bss, itemStats.get(iGroup));
		}
		//if there are no stats...
		if(itemStats.size() == 0) init_noResults();
		else init_totalStats(itemStats.values());
	}
	
	protected void initByModGroups(BetterStatsScreen bss, StatHandler statHandler, Predicate<StatUtilsStat> statFilter)
	{
		var itemStats = StatUtils.getItemStatsByMods(statHandler, statFilter.and(getStatPredicate()));
		for(String iGroup : itemStats.keySet())
		{
			init_groupLabel(literal(getModName(iGroup)));
			init_itemStats(bss, itemStats.get(iGroup));
		}
		//if there are no stats...
		if(itemStats.size() == 0) init_noResults();
		else init_totalStats(itemStats.values());
	}
	// --------------------------------------------------
	protected void init_itemStats(BetterStatsScreen bss, ArrayList<StatUtilsItemStat> itemStats)
	{
		//sort the stats
		switch(bss.cache.getAs("BSStatPanelItems_SortBy", BSStatPanelItems_SortBy.class, BSStatPanelItems_SortBy.Default))
		{
			case Mined: Collections.sort(itemStats, (o1, o2) -> Integer.compare(o2.sMined, o1.sMined)); break;
			case Crafted: Collections.sort(itemStats, (o1, o2) -> Integer.compare(o2.sCrafted, o1.sCrafted)); break;
			case PickedUp: Collections.sort(itemStats, (o1, o2) -> Integer.compare(o2.sPickedUp, o1.sPickedUp)); break;
			case Dropped: Collections.sort(itemStats, (o1, o2) -> Integer.compare(o2.sDropped, o1.sDropped)); break;
			case Used: Collections.sort(itemStats, (o1, o2) -> Integer.compare(o2.sUsed, o1.sUsed)); break;
			case Broken: Collections.sort(itemStats, (o1, o2) -> Integer.compare(o2.sBroken, o1.sBroken)); break;
			default: break;
		}
		
		//declare the starting XY
		int nextX = getTpeX() + getScrollPadding();
		int nextY = getTpeY() + getScrollPadding();
		
		//calculate nextY based on the last child
		{
			var lastChild = getLastTChild(false);
			if(lastChild != null) nextY = lastChild.getTpeEndY() + 2;
		}
		
		//iterate and add item stat elements
		for(StatUtilsItemStat stat : itemStats)
		{
			//create and add the widget for the stat
			addTChild(createStatWidget(stat, nextX, nextY), false);
			
			//increment next XY
			nextX += BSStatWidget_Item.SIZE + 1;
			if(nextX + BSStatWidget_Item.SIZE > getTpeEndX() - getScrollPadding())
			{
				nextX = getTpeX() + getScrollPadding();
				nextY += BSStatWidget_Item.SIZE + 1;
			}
		}
	}
	
	protected void init_totalStats(Collection<ArrayList<StatUtilsItemStat>> itemStats)
	{
		//define totals
		int tMined = 0, tCrafted = 0, tUsed = 0 , tBroken = 0, tPickedUp = 0, tDropped = 0;
		
		//iterate all stats
		for(var group : itemStats)
		{
			//and count the kills and deaths
			for(var groupMob : group)
			{
				//ignore empty stats
				if(groupMob == null || groupMob.isEmpty())
					continue;
				//count
				tMined += groupMob.sMined;
				tCrafted += groupMob.sCrafted;
				tUsed += groupMob.sUsed;
				tBroken += groupMob.sBroken;
				tPickedUp += groupMob.sPickedUp;
				tDropped += groupMob.sDropped;
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
		var panel = new BSPanel(nextX, nextY, getTpeWidth() - (getScrollPadding() * 2), 60);
		panel.setScrollPadding(0);
		addTChild(panel, false);
		
		//create the labels
		int leftX = 5, leftW = (panel.getTpeWidth() / 2) - 10;
		int rightX = (panel.getTpeWidth() / 2) + 5, rightW = (panel.getTpeWidth() / 2) - 10;
		
		var lbl_tMined_a = new TLabelElement(leftX, 0, leftW, 20, ItemStatEnum.MINED.getIText());
		var lbl_tMined_b = new TLabelElement(leftX, 0, leftW, 20, literal(Integer.toString(tMined)));
		lbl_tMined_b.setHorizontalAlignment(HorizontalAlignment.RIGHT);
		panel.addTChild(lbl_tMined_a, true);
		panel.addTChild(lbl_tMined_b, true);
		var lbl_tCrafted_a = new TLabelElement(rightX, 0, rightW, 20, ItemStatEnum.CRAFTED.getIText());
		var lbl_tCrafted_b = new TLabelElement(rightX, 0, rightW, 20, literal(Integer.toString(tCrafted)));
		lbl_tCrafted_b.setHorizontalAlignment(HorizontalAlignment.RIGHT);
		panel.addTChild(lbl_tCrafted_a, true);
		panel.addTChild(lbl_tCrafted_b, true);
		
		var lbl_tUsed_a = new TLabelElement(leftX, 20, leftW, 20, ItemStatEnum.USED.getIText());
		var lbl_tUsed_b = new TLabelElement(leftX, 20, leftW, 20, literal(Integer.toString(tUsed)));
		lbl_tUsed_b.setHorizontalAlignment(HorizontalAlignment.RIGHT);
		panel.addTChild(lbl_tUsed_a, true);
		panel.addTChild(lbl_tUsed_b, true);
		var lbl_tBroken_a = new TLabelElement(rightX, 20, rightW, 20, ItemStatEnum.BROKEN.getIText());
		var lbl_tBroken_b = new TLabelElement(rightX, 20, rightW, 20, literal(Integer.toString(tBroken)));
		lbl_tBroken_b.setHorizontalAlignment(HorizontalAlignment.RIGHT);
		panel.addTChild(lbl_tBroken_a, true);
		panel.addTChild(lbl_tBroken_b, true);
		
		var lbl_tPickedUp_a = new TLabelElement(leftX, 40, leftW, 20, ItemStatEnum.PICKED_UP.getIText());
		var lbl_tPickedUp_b = new TLabelElement(leftX, 40, leftW, 20, literal(Integer.toString(tPickedUp)));
		lbl_tPickedUp_b.setHorizontalAlignment(HorizontalAlignment.RIGHT);
		panel.addTChild(lbl_tPickedUp_a, true);
		panel.addTChild(lbl_tPickedUp_b, true);
		var lbl_tDropped_a = new TLabelElement(rightX, 40, rightW, 20, ItemStatEnum.DROPPED.getIText());
		var lbl_tDropped_b = new TLabelElement(rightX, 40, rightW, 20, literal(Integer.toString(tDropped)));
		lbl_tDropped_b.setHorizontalAlignment(HorizontalAlignment.RIGHT);
		panel.addTChild(lbl_tDropped_a, true);
		panel.addTChild(lbl_tDropped_b, true);
	}
	// ==================================================
	protected BSStatWidget_Item createStatWidget(StatUtilsItemStat stat, int x, int y)
	{
		return new BSStatWidget_Item(stat, x, y);
	}
	// ==================================================
	protected class BSStatWidget_Item extends BSStatWidget
	{
		// ----------------------------------------------
		public static final int SIZE = 21;
		public final StatUtilsItemStat stat;
		public final ItemStack stack;
		
		public static final boolean SHOW_ITEM_NAMES = true;
		// ----------------------------------------------
		public BSStatWidget_Item(StatUtilsItemStat stat, int x, int y)
		{
			super(x, y, SIZE, SIZE);
			this.stat = stat;
			this.stack = stat.item.getDefaultStack();
			
			updateTooltip();
		}
		
		@Override
		public void updateTooltip()
		{
			String sMined = translatable("stat_type.minecraft.mined").getString();
			String sCrafted = translatable("stat_type.minecraft.crafted").getString();
			String sPicked = translatable("stat_type.minecraft.picked_up").getString();
			String sDroppp = translatable("stat_type.minecraft.dropped").getString();
			String sUsed = translatable("stat_type.minecraft.used").getString();
			String sBroken = translatable("stat_type.minecraft.broken").getString();
			
			Text tooltip = literal(
					(SHOW_ITEM_NAMES ? translatable(stat.item.getTranslationKey()).getString() + "\n\n" : "") +
					sMined + " - " + stat.sMined + "\n" +
					sCrafted + " - " + stat.sCrafted + "\n" +
					sPicked  + " - " + stat.sPickedUp + "\n" +
					sDroppp  + " - " + stat.sDropped + "\n" +
					sUsed    + " - " + stat.sUsed + "\n" +
					sBroken  + " - " + stat.sBroken
				);
			setTooltip(tooltip);
		}
		// ----------------------------------------------
		public @Override boolean mousePressed(int mouseX, int mouseY, int button)
		{
			//handle REI
			if((button == 0 || button == 1) && this.stack != null && !Screen.hasShiftDown())
			try
			{
				//create a new ViewSearchBuilder
				me.shedaniel.rei.api.client.view.ViewSearchBuilder builder =
						me.shedaniel.rei.api.client.view.ViewSearchBuilder.builder();
				
				//get entry stack
				me.shedaniel.rei.api.common.entry.EntryStack<?> entryStack =
						me.shedaniel.rei.api.common.util.EntryStacks.of(this.stack);
				
				//add recipes and usages
				if(button == 0) builder.addRecipesFor(entryStack);
				if(button == 1) builder.addUsagesFor(entryStack);
				
				//open view and return
				boolean opened = me.shedaniel.rei.api.client.ClientHelper.getInstance().openView(builder);
				
				//if successful, block the focus by returning false
				if(opened) return false;
			}
			catch(NoClassDefFoundError exc) { /*return super*/ }
			//handle Wikis
			else if(button == 2)
			{
				String url = BetterStatsRegistry.getItemWikiURL(Registries.ITEM.getId(this.stat.item));
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
		public void render(MatrixStack matrices, int mouseX, int mouseY, float deltaTime)
		{
			//render super
			super.render(matrices, mouseX, mouseY, deltaTime);
			//render the item
			getItemRenderer().renderGuiItemIcon(this.stack, getTpeX() + 3, getTpeY() + 3);
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
				bshs.addHudStatWidget(new BSHudStatWidget_Item(0, 0, stat.statHandler, stat.item));
			});
			contextMenu.addButton(translatable("betterstats.gui.ctx_menu.close"), btn -> {});
		}
		// ----------------------------------------------
	}
	// ==================================================
}