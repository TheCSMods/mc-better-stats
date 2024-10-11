package io.github.thecsdev.betterstats.api.client.gui.stats.widget;

import static io.github.thecsdev.betterstats.api.util.stats.SUGeneralStat.TEXT_VALUE;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.fLiteral;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.client.TCDCommonsClient.MC_CLIENT;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.util.formatters.StatValueFormatter;
import io.github.thecsdev.betterstats.api.util.stats.SUGeneralStat;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;

public @Virtual class GeneralStatWidget extends AbstractStatWidget<SUGeneralStat>
{
	// ==================================================
	public static final int HEIGHT = MC_CLIENT.textRenderer.fontHeight + 8;
	// --------------------------------------------------
	protected final         Text               txt_label;
	protected /*final*/     Text               txt_value;      //no longer "final" as of v3.13
	protected final         Tooltip            defaultTooltip;
	protected @Experimental StatValueFormatter formatter;      //experimental
	// ==================================================
	public GeneralStatWidget(int x, int y, int width, SUGeneralStat stat) throws NullPointerException
	{
		super(x, y, width, HEIGHT, stat);
		this.txt_label = stat.getStatLabel();
		//this.txt_value = stat.valueText; -- will assign on the following line
		setFormatter(null); //a formatter value must be assigned; null will be turned into default value
		
		final Text ttt = literal("") //MUST create new text instance
				.append(stat.getStatLabel())
				.append(fLiteral("\n§7K: " + stat.getStatID()))
				.append(fLiteral("\n§7V: " + stat.getGeneralStat().getValue()))
				.append("\n\n§r")
				.append(fLiteral("§e" + TEXT_VALUE.getString() + ": §r" + stat.value));
		setTooltip(this.defaultTooltip = Tooltip.of(ttt));
	}
	// --------------------------------------------------
	public final @Experimental StatValueFormatter getFormatter() { return this.formatter; }
	public final @Experimental void setFormatter(@Nullable StatValueFormatter formatter)
	{
		this.formatter = (formatter != null) ? formatter : new StatValueFormatter()
		{
			public final @Override Text getDisplayName() { return literal("-"); }
			public final @Override Text format(int number) { return GeneralStatWidget.this.stat.valueText; }
		};
		this.txt_value = this.formatter.format(this.stat.value);
	}
	// ==================================================
	public @Virtual @Override void render(TDrawContext pencil)
	{
		super.render(pencil);
		pencil.drawTElementTextTH(this.txt_label, HorizontalAlignment.LEFT);
		pencil.drawTElementTextTH(this.txt_value, HorizontalAlignment.RIGHT);
	}
	// ==================================================
}