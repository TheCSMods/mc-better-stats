package thecsdev.betterstats.client.gui.panel.stats;

import static thecsdev.tcdcommons.api.util.TextUtils.literal;
import static thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.ArrayList;
import java.util.function.Predicate;

import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.StatHandler;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;
import thecsdev.betterstats.api.registry.BetterStatsRegistry;
import thecsdev.betterstats.util.StatUtils;
import thecsdev.betterstats.util.StatUtils.StatUtilsItemStat;
import thecsdev.betterstats.util.StatUtils.StatUtilsStat;
import thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import thecsdev.tcdcommons.api.client.gui.util.GuiUtils;

public class BSStatPanel_Items extends BSStatPanel
{
	// ==================================================
	public BSStatPanel_Items(int x, int y, int width, int height) { super(x, y, width, height); }
	public BSStatPanel_Items(TPanelElement parentToFill) { super(parentToFill); }
	// ==================================================
	public @Override Predicate<StatUtilsStat> getStatPredicate()
	{
		return stat ->
				//for performance reasons, maybe it is better not to show 100s of items all at once
				//!stat.isEmpty() && //TODO - serious performance issues when rendering 100s of elements
				//some subclasses may assume the stat type, so ensure it is the right type
				(stat instanceof StatUtilsItemStat);
	}
	// ==================================================
	@Override
	public void init(StatHandler statHandler, Predicate<StatUtilsStat> statFilter)
	{
		var itemStats = StatUtils.getItemStats(statHandler, statFilter.and(getStatPredicate()));
		for(ItemGroup iGroup : itemStats.keySet())
		{
			Text gLabel = iGroup != null ? iGroup.getDisplayName() : null;
			init_groupLabel(gLabel);
			init_itemStats(itemStats.get(iGroup));
		}
		//if there are no stats...
		if(itemStats.size() == 0) init_noResults();
	}
	// --------------------------------------------------
	protected void init_itemStats(ArrayList<StatUtilsItemStat> itemStats)
	{
		//declare the starting XY
		int nextX = getTpeX() + getScrollPadding();
		int nextY = getTpeY() + getScrollPadding();
		
		//calculate nextY based on the last child
		{
			var lastChild = getLastTChild(false);
			if(lastChild != null) nextY = lastChild.getTpeEndY() + 2;
		}
		
		//iterate and add item stat elements
		for(StatUtilsItemStat stat : itemStats)
		{
			//create and add the widget for the stat
			addTChild(createStatWidget(stat, nextX, nextY), false);
			
			//increment next XY
			nextX += BSStatWidget_Item.SIZE + 1;
			if(nextX + BSStatWidget_Item.SIZE > getTpeEndX() - getScrollPadding())
			{
				nextX = getTpeX() + getScrollPadding();
				nextY += BSStatWidget_Item.SIZE + 1;
			}
		}
	}
	// ==================================================
	protected BSStatWidget_Item createStatWidget(StatUtilsItemStat stat, int x, int y)
	{
		return new BSStatWidget_Item(stat, x, y);
	}
	// ==================================================
	protected class BSStatWidget_Item extends BSStatWidget
	{
		// ----------------------------------------------
		public static final int SIZE = 21;
		public final StatUtilsItemStat stat;
		public final ItemStack stack;
		
		public static final boolean SHOW_ITEM_NAMES = true;
		// ----------------------------------------------
		public BSStatWidget_Item(StatUtilsItemStat stat, int x, int y)
		{
			super(x, y, SIZE, SIZE);
			this.stat = stat;
			this.stack = stat.item.getDefaultStack();
			
			updateTooltip();
		}
		
		@Override
		public void updateTooltip()
		{
			String sMined = translatable("stat_type.minecraft.mined").getString();
			String sCrafted = translatable("stat_type.minecraft.crafted").getString();
			String sPicked = translatable("stat_type.minecraft.picked_up").getString();
			String sDroppp = translatable("stat_type.minecraft.dropped").getString();
			String sUsed = translatable("stat_type.minecraft.used").getString();
			String sBroken = translatable("stat_type.minecraft.broken").getString();
			
			Text tooltip = literal(
					(SHOW_ITEM_NAMES ? translatable(stat.item.getTranslationKey()).getString() + "\n\n" : "") +
					sMined + " - " + stat.sMined + "\n" +
					sCrafted + " - " + stat.sCrafted + "\n" +
					sPicked  + " - " + stat.sPickedUp + "\n" +
					sDroppp  + " - " + stat.sDropped + "\n" +
					sUsed    + " - " + stat.sUsed + "\n" +
					sBroken  + " - " + stat.sBroken
				);
			setTooltip(tooltip);
		}
		// ----------------------------------------------
		public @Override boolean mousePressed(int mouseX, int mouseY, int button)
		{
			//handle REI
			if((button == 0 || button == 1) && this.stack != null)
			try
			{
				//create a new ViewSearchBuilder
				me.shedaniel.rei.api.client.view.ViewSearchBuilder builder =
						me.shedaniel.rei.api.client.view.ViewSearchBuilder.builder();
				
				//get entry stack
				me.shedaniel.rei.api.common.entry.EntryStack<?> entryStack =
						me.shedaniel.rei.api.common.util.EntryStacks.of(this.stack);
				
				//add recipes and usages
				if(button == 0) builder.addRecipesFor(entryStack);
				if(button == 1) builder.addUsagesFor(entryStack);
				
				//open view and return
				boolean opened = me.shedaniel.rei.api.client.ClientHelper.getInstance().openView(builder);
				
				//if successful, block the focus by returning false
				if(opened) return false;
			}
			catch(NoClassDefFoundError exc) { /*return super*/ }
			//handle Wikis
			else if(button == 2)
			{
				String url = BetterStatsRegistry.getItemWikiURL(Registry.ITEM.getId(this.stat.item));
				if(url != null)
				{
					GuiUtils.showUrlPrompt(url, false);
					//if successful, block the focus by returning false
					return false;
				}
			}
			//return super if all else fails
			return super.mousePressed(mouseX, mouseY, button);
		}
		// ----------------------------------------------
		@Override
		public void render(MatrixStack matrices, int mouseX, int mouseY, float deltaTime)
		{
			//render super
			super.render(matrices, mouseX, mouseY, deltaTime);
			//set Z to 0
			ItemRenderer ir = getItemRenderer();
			float z = ir.zOffset;
			ir.zOffset = 0;
			//render the item
			try { getItemRenderer().renderGuiItemIcon(this.stack, getTpeX() + 3, getTpeY() + 3); }
			//reset Z
			finally { ir.zOffset = z; }
		}
		// ----------------------------------------------
	}
	// ==================================================
}