package thecsdev.betterstats.client.gui.widget;

import static thecsdev.betterstats.BetterStats.lt;

import java.awt.Color;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import thecsdev.betterstats.client.gui.util.GuiUtils;

public class CenteredTextWidget extends DrawableHelper implements Element, Drawable, TooltipProvider
{
	// ==================================================
	public TextRenderer textRenderer;
	protected Text text, tooltip = null;
	public int x, y, color;
	
	protected int cache_w, cache_h;
	protected boolean hovered;
	// ==================================================
	public CenteredTextWidget(TextRenderer tr, Text text, int x, int y, Color color)
	{
		this(tr, text, x, y, color.getRGB());
	}
	
	public CenteredTextWidget(TextRenderer tr, Text text, int x, int y, int color)
	{
		this.textRenderer = tr;
		setText(text);
		
		this.x = x;
		this.y = y;
		this.color = color;
	}
	
	public void setText(Text text)
	{
		this.text = text != null ? lt(text.getString().replaceAll("\\r?\\n", "")) : lt("");
		this.cache_w = getWidth();
		this.cache_h = getHeight();
	}
	
	public void setTooltip(Text tooltip)
	{
		this.tooltip = tooltip;
		if(this.tooltip != null && StringUtils.isAllBlank(this.tooltip.getString()))
			this.tooltip = null;
	}
	
	public int getWidth() { return textRenderer.getWidth(text.getString()); }
	public int getHeight() { return textRenderer.fontHeight; }
	// --------------------------------------------------
	protected void updateHovered(int mouseX, int mouseY)
	{
		this.hovered = (mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.cache_w && mouseY < this.y + this.cache_h);
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		updateHovered(mouseX, mouseY);
		drawCenteredText(matrices, textRenderer, text, x, y, color);
	}
	// ==================================================
	@Override
	public boolean tp_isHovered() { return this.hovered; }
	
	@Override
	public void tp_renderTooltip(MatrixStack matrices, int mouseX, int mouseY)
	{
		if(tp_isHovered() && tooltip != null)
			GuiUtils.drawTooltip(matrices, mouseX, mouseY, tooltip);
	}
	// ==================================================
}