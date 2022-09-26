package thecsdev.betterstats.client.gui.widget;

import net.minecraft.client.util.math.MatrixStack;

public interface TooltipProvider
{
	public boolean tp_isHovered();
	public void tp_renderTooltip(MatrixStack matrices, int mouseX, int mouseY);
}