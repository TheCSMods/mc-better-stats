package thecsdev.betterstats.client.gui.widget.stats;

import static thecsdev.betterstats.BetterStats.lt;
import static thecsdev.betterstats.config.BSConfig.COLOR_STAT_GENERAL_TEXT;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.util.math.MatrixStack;
import thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import thecsdev.betterstats.client.gui.util.GuiUtils;
import thecsdev.betterstats.client.gui.util.StatUtils.SUGeneralStat;
import thecsdev.betterstats.config.BSConfig;

public class BSGenetalStatWidget extends BSStatWidget
{
	// ==================================================
	public final TextRenderer textRenderer;
	public final SUGeneralStat stat;
	// ==================================================
	public BSGenetalStatWidget(BetterStatsScreen parent, int x, int y, int width, SUGeneralStat stat)
	{
		super(parent, x, y, width, GuiUtils.getTextSize(parent.getTextRenderer(), stat.title).height + 6, BSConfig.COLOR_STAT_BG);
		this.textRenderer = parent.getTextRenderer();
		this.stat = stat;
		
		setMessage(lt(stat.title.getString() + ", " + stat.txtValue.getString() + "."));
	}
	
	@Override
	public void appendNarrations(NarrationMessageBuilder builder) { builder.put(NarrationPart.TITLE, getNarrationMessage()); }
	
	@Override
	public void onRenderStat(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		//render text
		String s0 = stat.title.getString();
		String s1 = stat.txtValue.getString();
		int s1w = textRenderer.getWidth(s1);
		
		drawStringWithShadow(matrices, textRenderer, s0, x + 3, y + 3, COLOR_STAT_GENERAL_TEXT);
		drawStringWithShadow(matrices, textRenderer, s1, x + width - s1w - 3, y + 3, COLOR_STAT_GENERAL_TEXT);
	}
	// ==================================================
}