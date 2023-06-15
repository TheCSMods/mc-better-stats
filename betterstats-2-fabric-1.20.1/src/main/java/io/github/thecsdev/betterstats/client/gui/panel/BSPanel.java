package io.github.thecsdev.betterstats.client.gui.panel;

import java.awt.Color;

import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;

public class BSPanel extends TPanelElement
{
	// ==================================================
	public static final int COLOR_OUTLINE = Color.black.getRGB();
	// ==================================================
	public BSPanel(int x, int y, int width, int height)
	{
		super(x, y, width, height);
		setOutlineColor(COLOR_OUTLINE);
	}
	// ==================================================
}