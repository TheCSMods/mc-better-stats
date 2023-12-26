package io.github.thecsdev.betterstats.api.client.gui.stats.widget;

import static io.github.thecsdev.betterstats.api.util.stats.SUGeneralStat.TEXT_VALUE;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.fLiteral;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.client.TCDCommonsClient.MC_CLIENT;

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
	protected final Text txt_label, txt_value;
	protected final Tooltip defaultTooltip;
	// ==================================================
	public GeneralStatWidget(int x, int y, int width, SUGeneralStat stat) throws NullPointerException
	{
		super(x, y, width, HEIGHT, stat);
		this.txt_label = stat.getStatLabel();
		this.txt_value = stat.valueText;

		final Text ttt = literal("") //MUST create new text instance
				.append(stat.getStatLabel())
				.append(fLiteral("\n§7K: " + stat.getStatID()))
				.append(fLiteral("\n§7V: " + stat.getGeneralStat().getValue()))
				.append("\n\n§r")
				.append(fLiteral("§e" + TEXT_VALUE.getString() + ": §r" + stat.value));
		setTooltip(this.defaultTooltip = Tooltip.of(ttt));
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