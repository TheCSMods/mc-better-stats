package thecsdev.betterstats.client.gui.widget.stats;

import static thecsdev.betterstats.BetterStats.lt;
import static thecsdev.betterstats.config.BSConfig.COLOR_STAT_GENERAL_TEXT;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import thecsdev.betterstats.client.gui.util.GuiUtils;
import thecsdev.betterstats.client.gui.util.StatUtils.SUMobStat;
import thecsdev.betterstats.config.BSConfig;

//was gonna take a non-visual approach for
//"no spoilers" and for "rendering performance"
//reasons, but eh.
@Deprecated
public class BSHuntStatWidget extends BSStatWidget
{
	// ==================================================
	public final TextRenderer textRenderer;
	public final SUMobStat mobStat;
	// ==================================================
	public BSHuntStatWidget(BetterStatsScreen parent, SUMobStat mobStat, int x, int y, int width)
	{
		super(parent, x, y, width, GuiUtils.getTextSize(parent.getTextRenderer(), lt(mobStat.entityName)).height + 6, BSConfig.COLOR_STAT_BG);
		this.textRenderer = parent.getTextRenderer();
		this.mobStat = mobStat;
		updateTooltip();
	}
	// --------------------------------------------------
	@Override
	protected void onUpdateTooltip() { tooltip = BSMobStatWidget.onUpdateTooltip(mobStat); }
	// --------------------------------------------------
	@Override
	public void onRenderStat(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		drawStringWithShadow(matrices, textRenderer, mobStat.entityName, x + 3, y + 3, COLOR_STAT_GENERAL_TEXT);
	}
	// ==================================================
}