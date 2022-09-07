package thecsdev.betterstats.client.gui.widget;

import java.awt.Color;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class StringWidget extends CenteredTextWidget
{
	// ==================================================
	public StringWidget(TextRenderer tr, Text text, int x, int y, Color color)
	{
		super(tr, text, x, y, color);
	}
	
	public StringWidget(TextRenderer tr, Text text, int x, int y, int color)
	{
		super(tr, text, x, y, color);
	}
	// ==================================================
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		updateHovered(mouseX, mouseY);
		drawStringWithShadow(matrices, textRenderer, text.getString(), x, y, color);
	}
	// ==================================================
}