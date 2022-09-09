package thecsdev.betterstats.client.gui.widget.stats;

import static thecsdev.betterstats.BetterStats.lt;

import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import thecsdev.betterstats.client.gui.screen.ScreenWithScissors;

public class ContextMenuWidget extends ClickableWidget
{
	public final ScreenWithScissors parent;
	public ContextMenuWidget(ScreenWithScissors parent, int x, int y)
	{
		super(x, y, parent.width, parent.height, lt(""));
		this.parent = parent;
	}
	@Override
	public void appendNarrations(NarrationMessageBuilder builder) {}
}
