package io.github.thecsdev.betterstats.client.gui.panel.stats;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;

import java.util.Collections;
import java.util.function.Predicate;

import io.github.thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.betterstats.util.StatUtils;
import io.github.thecsdev.betterstats.util.StatUtils.StatUtilsGeneralStat;
import io.github.thecsdev.betterstats.util.StatUtils.StatUtilsStat;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.util.HorizontalAlignment;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TSelectEnumWidget;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TSelectWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.stat.StatHandler;
import net.minecraft.text.MutableText;

public class BSStatPanel_General extends BSStatPanel
{
	// ==================================================
	public static enum BSStatPanelGeneral_SortBy
	{
		Default(literal("A-Z")),
		Reverse(literal("Z-A")),
		Incremental(literal("0-9")),
		Decremental(literal("9-0"));
		
		private final MutableText text;
		BSStatPanelGeneral_SortBy(MutableText text) { this.text = text; }
		public MutableText asText() { return text; }
	}
	// ==================================================
	public BSStatPanel_General(TPanelElement parentToFill) { super(parentToFill); }
	public BSStatPanel_General(int x, int y, int width, int height) { super(x, y, width, height); }
	// --------------------------------------------------
	public @Override TSelectWidget createFilterSortByWidget(BetterStatsScreen bss, int x, int y, int width, int height)
	{
		var sw = new TSelectEnumWidget<BSStatPanelGeneral_SortBy>(x, y, width, height, BSStatPanelGeneral_SortBy.class);
		sw.setSelected(bss.cache.getAs("BSStatPanelGeneral_SortBy", BSStatPanelGeneral_SortBy.class, BSStatPanelGeneral_SortBy.Default), false);
		sw.setEnumValueToLabel(newVal -> ((BSStatPanelGeneral_SortBy)newVal).asText());
		sw.setOnSelectionChange(newVal ->
		{
			bss.cache.set("BSStatPanelGeneral_SortBy", newVal);
			bss.getStatPanel().init_stats();
		});
		return sw;
	}
	// ==================================================
	@Override
	public void init(BetterStatsScreen bss, StatHandler statHandler, Predicate<StatUtilsStat> statFilter)
	{
		//first, obtain all stats
		var stats = StatUtils.getGeneralStats(statHandler, statFilter.and(getStatPredicate()));
		//then sort the stats
		switch(bss.cache.getAs("BSStatPanelGeneral_SortBy", BSStatPanelGeneral_SortBy.class, BSStatPanelGeneral_SortBy.Default))
		{
			case Reverse: Collections.reverse(stats); break;
			case Incremental:
				Collections.sort(stats, (o1, o2) -> Integer.compare(o1.intValue, o2.intValue)); break;
			case Decremental:
				Collections.sort(stats, (o1, o2) -> Integer.compare(o2.intValue, o1.intValue)); break;
			default: break;
		}
		//iterate all stats and create widgets
		int height = getTextRenderer().fontHeight + 8;
		for(StatUtilsGeneralStat stat : stats) new BSStatWidget_General(stat, height);
		//if there are no stats...
		if(stats.size() == 0) init_noResults();
	}
	// --------------------------------------------------
	public int getChildBottomY()
	{
		if(getTChildren().size() == 0) return getTpeY() + getScrollPadding();
		return getTChildren().getTopmostElements().Item2.getTpeEndY() + 2;
	}
	// ==================================================
	protected class BSStatWidget_General extends BSStatWidget
	{
		// ----------------------------------------------
		public final StatUtilsGeneralStat stat;
		// ----------------------------------------------
		public BSStatWidget_General(StatUtilsGeneralStat stat, int height)
		{
			//initialize and add
			super(BSStatPanel_General.this.getTpeX() + BSStatPanel_General.this.getScrollPadding(),
					BSStatPanel_General.this.getChildBottomY(),
					BSStatPanel_General.this.getTpeWidth() - (BSStatPanel_General.this.getScrollPadding() * 2),
					height);
			BSStatPanel_General.this.addTChild(this, false);
			
			//declare fields
			this.stat = stat;
			
			//update tooltip
			updateTooltip();
		}
		@Override public void updateTooltip() { setTooltip(null); }
		// ----------------------------------------------
		@Override
		public void render(MatrixStack matrices, int mouseX, int mouseY, float deltaTime)
		{
			super.render(matrices, mouseX, mouseY, deltaTime);
			drawTElementText(matrices, this.stat.label, HorizontalAlignment.LEFT, deltaTime);
			drawTElementText(matrices, this.stat.value, HorizontalAlignment.RIGHT, deltaTime);
		}
		// ----------------------------------------------
	}
	// ==================================================
}