package io.github.thecsdev.betterstats.client.gui.panel;

import java.awt.Color;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.util.GuiUtils;

public class BSPanel extends TPanelElement
{
	// ==================================================
	public static final int COLOR_OUTLINE = Color.black.getRGB();
	// ==================================================
	public BSPanel(int x, int y, int width, int height) { super(x, y, width, height); }
	// ==================================================
	@Override
	public void postRender(PoseStack matrices, int mouseX, int mouseY, float deltaTime)
	{
		drawOutline(matrices, GuiUtils.applyAlpha(isFocused() ? COLOR_OUTLINE_FOCUSED : BSPanel.COLOR_OUTLINE, getAlpha()));
	}
	// ==================================================
}