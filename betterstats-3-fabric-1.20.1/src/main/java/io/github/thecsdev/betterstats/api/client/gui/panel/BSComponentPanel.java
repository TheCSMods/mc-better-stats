package io.github.thecsdev.betterstats.api.client.gui.panel;

import static io.github.thecsdev.betterstats.BetterStats.getModID;
import io.github.thecsdev.betterstats.api.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TRefreshablePanelElement;
import net.minecraft.util.Identifier;

/**
 * A {@link TRefreshablePanelElement} representing a {@link BetterStatsScreen} GUI component.
 */
public abstract class BSComponentPanel extends TRefreshablePanelElement
{
	public static final Identifier BS_WIDGETS_TEXTURE = new Identifier(getModID(), "textures/gui/widgets.png");
	
	public BSComponentPanel(int x, int y, int width, int height)
	{
		super(x, y, width, height);
		this.scrollFlags = 0;
		this.scrollPadding = 0;
		this.outlineColor = -16777216; //black
	}
}