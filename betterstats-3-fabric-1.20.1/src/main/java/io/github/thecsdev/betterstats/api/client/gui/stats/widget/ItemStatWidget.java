package io.github.thecsdev.betterstats.api.client.gui.stats.widget;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.fLiteral;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import io.github.thecsdev.betterstats.api.util.stats.SUItemStat;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public @Virtual class ItemStatWidget extends AbstractStatWidget<SUItemStat>
{
	// ==================================================
	public static final int SIZE = 21;
	//
	public static final Text TXT_STAT_MINED     = translatable("stat_type.minecraft.mined");
	public static final Text TXT_STAT_CRAFTED   = translatable("stat_type.minecraft.crafted");
	public static final Text TXT_STAT_PICKED_UP = translatable("stat_type.minecraft.picked_up");
	public static final Text TXT_STAT_DROPPED   = translatable("stat_type.minecraft.dropped");
	public static final Text TXT_STAT_USED      = translatable("stat_type.minecraft.used");
	public static final Text TXT_STAT_BROKEN    = translatable("stat_type.minecraft.broken");
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
				.append(TXT_STAT_MINED)    .append(" - " + stat.mined + "\n")
				.append(TXT_STAT_CRAFTED)  .append(" - " + stat.crafted + "\n")
				.append(TXT_STAT_PICKED_UP).append(" - " + stat.pickedUp + "\n")
				.append(TXT_STAT_DROPPED)  .append(" - " + stat.dropped + "\n")
				.append(TXT_STAT_USED)     .append(" - " + stat.used + "\n")
				.append(TXT_STAT_BROKEN)   .append(" - " + stat.broken);
		setTooltip(this.defaultTooltip = Tooltip.of(ttt));
	}
	// ==================================================
	public @Virtual @Override void render(TDrawContext pencil)
	{
		super.render(pencil);
		pencil.drawItem(this.itemStack, getX() + 3, getY() + 3);
	}
	// ==================================================
}