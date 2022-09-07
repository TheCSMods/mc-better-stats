package thecsdev.betterstats.client.gui.widget.stats;

import static thecsdev.betterstats.config.BSConfig.COLOR_STAT_OUTLINE;

import java.awt.Dimension;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import thecsdev.betterstats.client.gui.util.GuiUtils;
import thecsdev.betterstats.client.gui.widget.FillWidget;
import thecsdev.betterstats.config.BSConfig;
import thecsdev.betterstats.util.math.PointAndSize;

public abstract class BSStatWidget extends FillWidget
{
	// ==================================================
	public final BetterStatsScreen parent;
	protected Text tooltip;
	private int tooltipLines, tooltipZ;
	// --------------------------------------------------
	//a special flag for when an error takes place while handling a stat
	protected int errored = 0;
	// ==================================================
	public BSStatWidget(BetterStatsScreen parent, int x, int y, int width, int height, int color)
	{
		super(x, y, width, height, color);
		this.parent = parent;
		withBorder(FWBorderMode.Hover, COLOR_STAT_OUTLINE);
		//updateTooltip(); -- bad idea, update manually instead
	}
	// --------------------------------------------------
	public BSStatWidget setTooltipZOffset(int z) { this.tooltipZ = Math.abs(z) % 100; return this; }
	
	public final void updateTooltip()
	{
		onUpdateTooltip();
		
		if(tooltip != null)
			this.tooltipLines = tooltip.getString().split("\\r?\\n").length;
		else this.tooltipLines = 0;
	}
	protected void onUpdateTooltip() {}
	
	protected void setErrored(int errorFlag)
	{
		if(this.errored == errorFlag) return;
		this.errored = errorFlag;
		this.color = (errored == 0 || ignoreErrorMessages()) ?
				BSConfig.COLOR_STAT_BG : BSConfig.COLOR_STAT_BG_ERRORED;
		updateTooltip();
	}
	
	protected boolean ignoreErrorMessages()
	{
		return !BSConfig.BS_OPTIONS_GUI ||
				BSConfig.COLOR_STAT_BG == BSConfig.COLOR_STAT_BG_ERRORED;
	}
	// ==================================================
	@Override
	public final void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		//check if there is a stat content pane
		if(parent.statContentPane == null)
			return;
		
		//avoid rendering off-screen
		else if(this.y > parent.statContentPane.y + parent.statContentPane.getHeight())
			return;
		else if(this.y + this.height < parent.statContentPane.y)
			return;
		
		//render
		super.render(matrices, mouseX, mouseY, delta);
		onRenderStat(matrices, mouseX, mouseY, delta);
	}
	
	public abstract void onRenderStat(MatrixStack matrices, int mouseX, int mouseY, float delta);
	
	@Override
	public boolean isHovered() { return super.isHovered(); }
		
	@Override
	public final void renderTooltip(MatrixStack matrices, int mouseX, int mouseY)
	{
		//ignore null stat content pane and tooltip
		if(parent.statContentPane == null || this.tooltip == null)
			return;
		
		//z-index-offset
		matrices.push();
		matrices.translate(0, 0, tooltipZ);
		
		//calculate Y and H
		Dimension tTextSize = GuiUtils.getTextSize(parent.getTextRenderer(), tooltip);
		int tY = this.y + this.height;
		int tWidth = tTextSize.width + 10;
		int tHeight = tTextSize.height + 10;
		tHeight += tooltipLines;
		
		//calculate offset
		PointAndSize tOffset = new PointAndSize();
		int parentEndX = parent.statContentPane.x + parent.statContentPane.getWidth();
		int parentEndY = parent.statContentPane.y + parent.statContentPane.getHeight();
		
		if(parent.statContentPane.scroll.hasScrollBar())
			parentEndX -= (parent.statContentPane.scroll.barTransform.width + 2);
		
		if(this.x + tWidth > parentEndX)
			tOffset.x = -Math.abs(this.x + tWidth - parentEndX);
		if(tY + tHeight > parentEndY)
			tOffset.y = -(tHeight + this.height);
		
		//render tooltip
		if(parent.getFocused() == this || (isHovered() && !(parent.getFocused() instanceof BSStatWidget)))
			GuiUtils.drawTooltip(matrices, this.x, tY, this.tooltip, tOffset);
		
		//don't forget to pop after pushing
		matrices.pop();
	}
	// --------------------------------------------------
	@Override
	public void onFocusedChanged(boolean newFocused)
	{
		super.onFocusedChanged(newFocused);
		if(newFocused && parent.getFocused() instanceof BSStatWidget)
		{
			//adjust the scroll so the element is visible when pressing Tab
			double val = 0;
			int top = parent.statContentPane.y;
			int bottom = top + parent.statContentPane.getHeight();
			
			if(this.y + this.height > bottom)
				val = this.y - bottom + this.getHeight() + 5;
			else if(this.y < top)
				val -= Math.abs(top - this.y + 5);
			else return;
			
			val = - val;
			parent.statContentPane.scroll.setValue(parent.statContentPane.scroll.getValue() + val);
			parent.statContentPane.onScroll_apply(parent.statContentPane.x, parent.statContentPane.y);
		}
	}
	// ==================================================
}