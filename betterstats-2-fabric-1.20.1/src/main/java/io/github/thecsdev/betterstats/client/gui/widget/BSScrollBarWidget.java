package io.github.thecsdev.betterstats.client.gui.widget;

import java.awt.Color;

import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TScrollBarWidget;
import net.minecraft.client.gui.DrawContext;

public class BSScrollBarWidget extends TScrollBarWidget
{
	// ==================================================
	private static final int COLOR_BLACK = Color.black.getRGB();
	private static final int COLOR_NORMAL = new Color(255,255,255,50).getRGB();
	private static final int COLOR_HOVERED = new Color(255,255,255,110).getRGB();
	// ==================================================
	public BSScrollBarWidget(int x, int y, int width, int height, TPanelElement target)
	{
		super(x, y, width, height, target);
		setZOffset(getZOffset() + 1);
	}
	// ==================================================
	@Override
	public void render(DrawContext pencil, int mouseX, int mouseY, float deltaTime)
	{
		pencil.fill(this.x, this.y, this.x + this.width, this.y + this.height, 1342177280);
		drawOutline(pencil, COLOR_BLACK);
		drawSliderKnob(pencil, mouseX, mouseY, deltaTime);
	}
	
	@Override
	protected void drawSliderKnob(DrawContext pencil, int mouseX, int mouseY, float deltaTime,
			int x, int y, int width, int height)
	{
		pencil.fill(x + 1, y + 1, x + width - 1, y + height - 1,
				isFocusedOrHovered() ? COLOR_HOVERED : COLOR_NORMAL);
	}
	// ==================================================
}