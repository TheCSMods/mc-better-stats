package thecsdev.betterstats.client.gui.util;

import static net.minecraft.client.gui.DrawableHelper.drawTextWithShadow;
import static net.minecraft.client.gui.DrawableHelper.fill;
import static thecsdev.betterstats.BetterStats.lt;
import static thecsdev.betterstats.client.BetterStatsClient.MCClient;
import static thecsdev.betterstats.config.BSConfig.COLOR_TOOLTIP_BG;
import static thecsdev.betterstats.config.BSConfig.COLOR_TOOLTIP_OUTLINE;
import static thecsdev.betterstats.config.BSConfig.COLOR_TOOLTIP_TEXT;

import java.awt.Dimension;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import thecsdev.betterstats.BetterStats;
import thecsdev.betterstats.client.BetterStatsClient;
import thecsdev.betterstats.util.math.PointAndSize;

public class GuiUtils
{
	// ==================================================
	public static final Identifier PING_TEXTURE = new Identifier(BetterStats.ModID, "textures/gui/ping.png");
	// --------------------------------------------------
	private static final PointAndSize DEFAULT_TOOLTIP_OFFSET = new PointAndSize(5, 0, 0, 0);
	// ==================================================
	public static void drawTooltip(MatrixStack matrices, int mouseX, int mouseY, Text text)
	{
		drawTooltip(matrices, mouseX, mouseY, text, DEFAULT_TOOLTIP_OFFSET);
	}
	
	public static Dimension drawTooltip(MatrixStack matrices, int mouseX, int mouseY, Text text, PointAndSize offsets)
	{
		//get important stuff
		MinecraftClient MC = BetterStatsClient.MCClient;
		TextRenderer tr = MC.textRenderer;
		
		if(text == null) return new Dimension();
		String textStr = text.getString();
		if(textStr.length() == 0) return new Dimension();
		String[] lines = textStr.split("(\\r?\\n)|(\\\\n)");
		
		//get text size so we can know how big the tooltip will be
		Dimension textSize = getTextSize(tr, text);
		textSize = new Dimension(
				textSize.width + 10 + offsets.width,
				textSize.height + 10 + offsets.height + lines.length - 1);
		
		//offset the XY for the tooltip
		mouseX += offsets.x;
		mouseY += offsets.y;
		
		if(MC.currentScreen != null)
		{
			int aX = mouseX + textSize.width;
			if(aX > MC.currentScreen.width)
				mouseX -= (textSize.width + offsets.x);
			
			int aY = mouseY + textSize.height;
			if(aY > MC.currentScreen.height)
				mouseY -= (textSize.height + offsets.y);
		}
		
		//fill in a tooltip square background
		fill(matrices, mouseX, mouseY, mouseX + textSize.width, mouseY + textSize.height, COLOR_TOOLTIP_OUTLINE);
		fill(matrices, mouseX + 2, mouseY + 2, mouseX + textSize.width - 2, mouseY + textSize.height - 2, COLOR_TOOLTIP_BG);
		
		//draw text
		int lineY = 0;
		for (String line : lines)
		{
			drawTextWithShadow(matrices, tr, lt(line), mouseX + 5, mouseY + 5 + lineY, COLOR_TOOLTIP_TEXT);
			lineY += /*tr.getWrappedLinesHeight(line, textSize.width)*/ tr.fontHeight;
			lineY++;
		}
		
		//return the tooltip size
		return textSize;
	}
	// ==================================================
	public static void drawTexture(Identifier id, int x, int y, int w, int h)
	{
		Screen cs = BetterStatsClient.MCClient.currentScreen;
		if(cs == null) return;
		
		Tessellator tessellator = Tessellator.getInstance();
	    BufferBuilder bufferBuilder = tessellator.getBuffer();
	    RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
	    RenderSystem.setShaderTexture(0, id);
	    RenderSystem.setShaderColor(1,1,1,1);
	    
	    bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
	    bufferBuilder.vertex(x, y + h, 0).texture(0, 1).color(255, 255, 255, 255).next();
	    bufferBuilder.vertex(x + w, y + h, 0).texture(1, 1).color(255, 255, 255, 255).next();
	    bufferBuilder.vertex(x + w, y, 0).texture(1, 0).color(255, 255, 255, 255).next();
	    bufferBuilder.vertex(x, y, 0).texture(0, 0).color(255, 255, 255, 255).next();
	    tessellator.draw();
	}
	// --------------------------------------------------
	public static void drawButtonPing(ClickableWidget widget) { drawButtonPing(widget, 6, 6); }
	public static void drawButtonPing(ClickableWidget widget, int w, int h) { drawButtonPing(widget, w, h, -4, -2); }
	public static void drawButtonPing(ClickableWidget widget, int w, int h, int offsetX, int offsetY)
	{
		GuiUtils.drawTexture(PING_TEXTURE,
				widget.x + widget.getWidth() + offsetX,
				widget.y + offsetY,
				w, h);
	}
	// --------------------------------------------------
	public static void drawCenteredTextLines(MatrixStack matrices, TextRenderer tr, int x, int y, String[] textLines, int color)
	{
		int tlH = (tr.fontHeight + 2) * textLines.length;
		int nextY = y - (tlH / 2) + (tr.fontHeight / 2);
		
		for (String line : textLines)
		{
			DrawableHelper.drawCenteredText(matrices, tr, line, x, nextY, color);
			nextY += tr.fontHeight + 1;
		}
	}
	// ==================================================
	//credit: Sodium Extra - code block taken from there
	public static void applyScissor(int x, int y, int width, int height, Runnable renderingAction)
	{
		//calculate and enable scissor
		double scale = MCClient.getWindow().getScaleFactor();
        RenderSystem.enableScissor(
        		(int) (x * scale),
        		(int) (MCClient.getWindow().getFramebufferHeight() - (y + height) * scale),
                (int) (width * scale),
                (int) (height * scale));
        
        //render whatever
        renderingAction.run();
        
        //disable scissor
        RenderSystem.disableScissor();
	}
	// --------------------------------------------------
	public static Dimension getTextSize(TextRenderer tr, Text text)
	{
		String textStr = text.getString();
		String[] lines = textStr.split("(\\r?\\n)|(\\\\n)");
		int maxWidth = 0;
		for (String line : lines)
		{
			int lineWidth = tr.getWidth(line);
			if(lineWidth > maxWidth)
				maxWidth = lineWidth;
		}
		return new Dimension(maxWidth, tr.getWrappedLinesHeight(textStr, maxWidth));
	}
	// ==================================================
}