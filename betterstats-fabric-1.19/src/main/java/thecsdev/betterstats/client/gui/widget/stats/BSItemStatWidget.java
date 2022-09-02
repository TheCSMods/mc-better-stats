package thecsdev.betterstats.client.gui.widget.stats;

import static thecsdev.betterstats.BetterStats.lt;
import static thecsdev.betterstats.BetterStats.tt;
import static thecsdev.betterstats.config.BSConfig.FILTER_SHOW_ITEM_NAMES;

import net.minecraft.client.util.math.MatrixStack;
import thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import thecsdev.betterstats.client.gui.util.StatUtils.SUItemStat;
import thecsdev.betterstats.config.BSConfig;

public class BSItemStatWidget extends BSStatWidget
{
	// ==================================================
	public final SUItemStat itemStat;
	// ==================================================
	public BSItemStatWidget(BetterStatsScreen parent, SUItemStat itemStat, int x, int y)
	{
		//construct stuff
		super(parent, x, y, 21, 21, BSConfig.COLOR_STAT_BG);
		this.itemStat = itemStat;
		updateTooltip();
	}
	
	@Override
	protected void onUpdateTooltip()
	{
		String sMined = tt("stat_type.minecraft.mined").getString();
		String sCrafted = tt("stat_type.minecraft.crafted").getString();
		String sPicked = tt("stat_type.minecraft.picked_up").getString();
		String sDroppp = tt("stat_type.minecraft.dropped").getString();
		String sUsed = tt("stat_type.minecraft.used").getString();
		String sBroken = tt("stat_type.minecraft.broken").getString();
		
		tooltip = lt(
				(FILTER_SHOW_ITEM_NAMES ? tt(itemStat.item.getTranslationKey()).getString() + "\n\n" : "") +
				(itemStat.mined != null ? (sMined + " - " + itemStat.mined + "\n") : "") +
				sCrafted + " - " + itemStat.crafted + "\n" +
				sPicked  + " - " + itemStat.pickedUp + "\n" +
				sDroppp  + " - " + itemStat.dropped + "\n" +
				sUsed    + " - " + itemStat.used + "\n" +
				sBroken  + " - " + itemStat.broken
			);
	}
	// --------------------------------------------------
	@Override
	public void onRenderStat(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		//render the item
		parent.getItemRenderer().renderGuiItemIcon(itemStat.itemStack, this.x + 3, this.y + 3);
	}
	// ==================================================
}