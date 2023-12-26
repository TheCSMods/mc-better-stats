package io.github.thecsdev.betterstats.api.client.gui.stats.widget;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;

import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.registry.BSRegistries;
import io.github.thecsdev.betterstats.api.util.enumerations.ItemStatType;
import io.github.thecsdev.betterstats.api.util.stats.SUItemStat;
import io.github.thecsdev.tcdcommons.api.client.gui.util.GuiUtils;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TInputContext;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TInputContext.InputType;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.stat.StatType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public @Virtual class ItemStatWidget extends AbstractStatWidget<SUItemStat>
{
	// ==================================================
	public static final int SIZE = 21;
	//
	public static final Text TEXT_STAT_MINED     = ItemStatType.MINED.getText();
	public static final Text TEXT_STAT_CRAFTED   = ItemStatType.CRAFTED.getText();
	public static final Text TEXT_STAT_PICKED_UP = ItemStatType.PICKED_UP.getText();
	public static final Text TEXT_STAT_DROPPED   = ItemStatType.DROPPED.getText();
	public static final Text TEXT_STAT_USED      = ItemStatType.USED.getText();
	public static final Text TEXT_STAT_BROKEN    = ItemStatType.BROKEN.getText();
	// --------------------------------------------------
	protected final ItemStack itemStack;
	protected final Tooltip defaultTooltip;
	// ==================================================
	public ItemStatWidget(int x, int y, SUItemStat stat) throws NullPointerException { this(x, y, SIZE, stat); }
	public ItemStatWidget(int x, int y, int size, SUItemStat stat) throws NullPointerException
	{
		super(x, y, size, size, stat);
		this.itemStack = stat.getItem().getDefaultStack();
		
		//prepare the String for the tooltip text
		final StringBuilder tttb = new StringBuilder();
		final boolean hasNoBlock = (stat.getBlock() == null || stat.getBlock() == Blocks.AIR);
		
		//iterate all registered stat types, to append their values to the tooltip text
		Registries.STAT_TYPE.forEach(st ->
		{
			//ignore all registries but ITEM and BLOCK where applicable
			final var stRegIsItem = (st.getRegistry() == Registries.ITEM);
			if (!stRegIsItem && (hasNoBlock || st.getRegistry() != Registries.BLOCK))
				return;
			
			//next up, obtain the StatType's Stat
			@SuppressWarnings({ "unchecked", "rawtypes" })
			final var stStat = ((StatType)st).getOrCreateStat(stRegIsItem ? stat.getItem() : stat.getBlock());
			
			//use the Stat instance to obtain the Stat value
			final int val = stat.getStatsProvider().getStatValue(stStat);
			
			//append the stat value to the final Tooltip text
			tttb.append("§e-§r ");
			tttb.append(Optional.ofNullable(st.getName()).orElse(literal("" + st.getTranslationKey())).getString());
			tttb.append(": " + val + "\n");
		});
		final Text ttt = literal("")
				.append(literal("").append(stat.getStatLabel()).formatted(Formatting.YELLOW)).append("\n")
				.append(literal(Objects.toString(stat.getStatID())).formatted(Formatting.GRAY))
				.append("\n\n§r")
				.append(tttb.toString().trim());
		
		//finally, construct the tooltip, and set the tooltip
		setTooltip(this.defaultTooltip = Tooltip.of(ttt));
	}
	// ==================================================
	public @Virtual @Override boolean input(TInputContext inputContext)
	{
		//only handle mouse presses
		if(inputContext.getInputType() != InputType.MOUSE_PRESS)
			return false;
		
		//handle the mouse press
		final int btn = inputContext.getMouseButton();
		
		// ---------- REI integration
		if((btn == 0 || btn == 1) && this.itemStack != null && !Screen.hasShiftDown())
		try
		{
			//create a new ViewSearchBuilder
			me.shedaniel.rei.api.client.view.ViewSearchBuilder builder =
					me.shedaniel.rei.api.client.view.ViewSearchBuilder.builder();
			
			//get entry stack
			me.shedaniel.rei.api.common.entry.EntryStack<?> entryStack =
					me.shedaniel.rei.api.common.util.EntryStacks.of(this.itemStack);
			
			//add recipes and usages
			if(btn == 0) builder.addRecipesFor(entryStack);
			else if(btn == 1) builder.addUsagesFor(entryStack);
			
			//open view and return
			boolean opened = me.shedaniel.rei.api.client.ClientHelper.getInstance().openView(builder);
			
			//if successful, block the focus by returning false
			if(opened) return false;
		}
		catch(NoClassDefFoundError exc) {}
		
		// ---------- Wiki integration
		else if(btn == 2)
		{
			final @Nullable var url = BSRegistries.getItemWikiURL(this.stat.getStatID());
			if(url != null)
			{
				GuiUtils.showUrlPrompt(url, false);
				return false; //if successful, block the focus by returning false
			}
		}
		
		//return super
		return super.input(inputContext);
	}
	// --------------------------------------------------
	public @Virtual @Override void render(TDrawContext pencil)
	{
		super.render(pencil);
		pencil.drawItem(this.itemStack, getX() + 3, getY() + 3);
	}
	// ==================================================
}