package io.github.thecsdev.betterstats.api.client.gui.stats.widget;

import static io.github.thecsdev.tcdcommons.client.TCDCommonsClient.MC_CLIENT;

import io.github.thecsdev.betterstats.api.util.stats.SUGeneralStat;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;
import net.minecraft.text.Text;

public @Virtual class GeneralStatWidget extends AbstractStatWidget<SUGeneralStat>
{
	// ==================================================
	public static final int HEIGHT = MC_CLIENT.textRenderer.fontHeight + 8;
	// --------------------------------------------------
	protected final Text txt_label, txt_value;
	// ==================================================
	public GeneralStatWidget(int x, int y, int width, SUGeneralStat stat) throws NullPointerException
	{
		super(x, y, width, HEIGHT, stat);
		this.txt_label = stat.getStatLabel();
		this.txt_value = stat.valueText;
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