package io.github.thecsdev.betterstats.client.gui.panel.stats;

import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.thecsdev.betterstats.util.StatUtils;
import io.github.thecsdev.betterstats.util.StatUtils.StatUtilsGeneralStat;
import io.github.thecsdev.betterstats.util.StatUtils.StatUtilsStat;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.util.HorizontalAlignment;
import net.minecraft.stats.StatsCounter;

public class BSStatPanel_General extends BSStatPanel
{
	// ==================================================
	public BSStatPanel_General(TPanelElement parentToFill) { super(parentToFill); }
	public BSStatPanel_General(int x, int y, int width, int height) { super(x, y, width, height); }
	// ==================================================
	@Override
	public void init(StatsCounter statHandler, Predicate<StatUtilsStat> statFilter)
	{
		//first, obtain all stats
		var stats = StatUtils.getGeneralStats(statHandler, statFilter.and(getStatPredicate()));
		//iterate all stats and create widgets
		int height = getTextRenderer().lineHeight + 8;
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
		public void render(PoseStack matrices, int mouseX, int mouseY, float deltaTime)
		{
			super.render(matrices, mouseX, mouseY, deltaTime);
			drawTElementText(matrices, this.stat.label, HorizontalAlignment.LEFT, deltaTime);
			drawTElementText(matrices, this.stat.value, HorizontalAlignment.RIGHT, deltaTime);
		}
		// ----------------------------------------------
	}
	// ==================================================
}