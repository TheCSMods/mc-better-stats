package thecsdev.betterstats.client.gui.widget;

import static thecsdev.betterstats.BetterStats.lt;

import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import thecsdev.betterstats.client.gui.screen.ScreenWithScissors;

public class ContextMenuWidget extends ClickableWidget
{
	public ContextMenuWidget(ScreenWithScissors parent, int x, int y)
	{
		super(x, y, parent.width, parent.height, lt(""));
	}
	@Override
	public void appendNarrations(NarrationMessageBuilder builder) {}
}