package thecsdev.betterstats.client.gui.widget.stats;

import static thecsdev.betterstats.BetterStats.lt;
import static thecsdev.betterstats.BetterStats.tt;
import static thecsdev.betterstats.client.BetterStatsClient.MCClient;
import static thecsdev.betterstats.config.BSConfig.FILTER_SHOW_ITEM_NAMES;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.util.math.MatrixStack;
import thecsdev.betterstats.client.BetterStatsClient;
import thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import thecsdev.betterstats.client.gui.screen.ScreenWithScissors;
import thecsdev.betterstats.client.gui.util.StatUtils.SUItemStat;
import thecsdev.betterstats.config.BSConfig;
import thecsdev.betterstats.config.BSWikiLinkConfig;

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
	// --------------------------------------------------
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		boolean b0 = super.mouseClicked(mouseX, mouseY, button);
		boolean b1 = hovered && BSConfig.ALLOW_CHEATS && Screen.hasShiftDown() && button == 0 && slashGiveItem();
		boolean b2 = hovered && button == 2 && openWikiArticle();
		boolean b3 = hovered && !b1 && (button == 0 || button == 1) && openREICraftingInfo(button);
		
		return b0 || b1 || b2 || b3;
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		boolean b0 = super.keyPressed(keyCode, scanCode, modifiers);
		
		boolean b1a = ScreenWithScissors.Key_Enter.matchesKey(keyCode, scanCode) ||
				ScreenWithScissors.Key_KpEnter.matchesKey(keyCode, scanCode);
		boolean b1 = BSConfig.ALLOW_CHEATS && isFocused() && b1a && Screen.hasShiftDown() && slashGiveItem();
		
		return b0 || b1;
	}
	// ==================================================
	public boolean slashGiveItem()
	{
		//check if the player has the permission to /give
		ClientPlayNetworkHandler net = MCClient.getNetworkHandler();
		if(net.getCommandSource().hasPermissionLevel(2)/* && MCClient.player.isCreative() eh, it's whatever ig*/)
		{
			//execute /give for the item
			int count = Screen.hasControlDown() ? 1 : itemStat.item.getMaxCount();
			String id = itemStat.itemId.getNamespace() + ":" + itemStat.itemId.getPath();
			String cmd = "/give @s " + id + " " + count;
			BetterStatsClient.sendChat(cmd);
			BetterStatsClient.beepItem();
			return true;
		}
		return false;
	}
	
	public boolean openWikiArticle() { return BSWikiLinkConfig.openUrl(itemStat.itemId, BSWikiLinkConfig.WikiType.ItemWiki); }
	
	public boolean openREICraftingInfo(int mouseButton)
	{
		if(!BSConfig.ENABLE_REI_LINKS) return false;
		//idk if including packages causes NoClassDefFoundError, so i will avoid doing that.
		//i am trying to make REI an optional mod here...
		try
		{
			//create a new ViewSearchBuilder
			me.shedaniel.rei.api.client.view.ViewSearchBuilder builder =
					me.shedaniel.rei.api.client.view.ViewSearchBuilder.builder();
			
			//get entry stack
			me.shedaniel.rei.api.common.entry.EntryStack<?> entryStack =
					me.shedaniel.rei.api.common.util.EntryStacks.of(itemStat.itemStack);
			
			//add recipes and usages
			if(mouseButton == 0) builder.addRecipesFor(entryStack);
			if(mouseButton == 1) builder.addUsagesFor(entryStack);
			
			//open view and return
			me.shedaniel.rei.api.client.ClientHelper.getInstance().openView(builder);
			return true;
		}
		catch(NoClassDefFoundError exc) { return false; }
	}
	// ==================================================
}