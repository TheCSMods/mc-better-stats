package io.github.thecsdev.betterstats.api.client.gui.stats.widget;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.fLiteral;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.registry.BSRegistries;
import io.github.thecsdev.betterstats.api.util.stats.SUItemStat;
import io.github.thecsdev.tcdcommons.api.client.gui.util.GuiUtils;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TInputContext;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TInputContext.InputType;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public @Virtual class ItemStatWidget extends AbstractStatWidget<SUItemStat>
{
	// ==================================================
	public static final int SIZE = 21;
	//
	public static final Text TEXT_STAT_MINED     = translatable("stat_type.minecraft.mined");
	public static final Text TEXT_STAT_CRAFTED   = translatable("stat_type.minecraft.crafted");
	public static final Text TEXT_STAT_PICKED_UP = translatable("stat_type.minecraft.picked_up");
	public static final Text TEXT_STAT_DROPPED   = translatable("stat_type.minecraft.dropped");
	public static final Text TEXT_STAT_USED      = translatable("stat_type.minecraft.used");
	public static final Text TEXT_STAT_BROKEN    = translatable("stat_type.minecraft.broken");
	// --------------------------------------------------
	protected final ItemStack itemStack;
	protected final Tooltip defaultTooltip;
	// ==================================================
	public ItemStatWidget(int x, int y, SUItemStat stat) throws NullPointerException { this(x, y, SIZE, stat); }
	public ItemStatWidget(int x, int y, int size, SUItemStat stat) throws NullPointerException
	{
		super(x, y, size, size, stat);
		this.itemStack = stat.getItem().getDefaultStack();
		
		final Text ttt = literal("") //MUST create new text instance
				.append(stat.getStatLabel())
				.append(fLiteral("\n§7" + stat.getStatID()))
				.append("\n\n§r")
				.append(TEXT_STAT_MINED)    .append(" - " + stat.mined + "\n")
				.append(TEXT_STAT_CRAFTED)  .append(" - " + stat.crafted + "\n")
				.append(TEXT_STAT_PICKED_UP).append(" - " + stat.pickedUp + "\n")
				.append(TEXT_STAT_DROPPED)  .append(" - " + stat.dropped + "\n")
				.append(TEXT_STAT_USED)     .append(" - " + stat.used + "\n")
				.append(TEXT_STAT_BROKEN)   .append(" - " + stat.broken);
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