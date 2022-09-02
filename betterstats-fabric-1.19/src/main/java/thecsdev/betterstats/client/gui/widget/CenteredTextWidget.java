package thecsdev.betterstats.client.gui.widget;

import java.awt.Color;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class CenteredTextWidget extends DrawableHelper implements Element, Drawable
{
	// ==================================================
	public TextRenderer textRenderer;
	public Text text;
	public int x, y, color;
	// ==================================================
	public CenteredTextWidget(TextRenderer tr, Text text, int x, int y, Color color)
	{
		this(tr, text, x, y, color.getRGB());
	}
	
	public CenteredTextWidget(TextRenderer tr, Text text, int x, int y, int color)
	{
		this.textRenderer = tr;
		this.text = text;
		
		this.x = x;
		this.y = y;
		this.color = color;
	}
	
	public int getWidth() { return textRenderer.getWidth(text.getString()); }
	public int getHeight() { return textRenderer.fontHeight; }
	// --------------------------------------------------
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		drawCenteredText(matrices, textRenderer, text, x, y, color);
	}
	// ==================================================
}