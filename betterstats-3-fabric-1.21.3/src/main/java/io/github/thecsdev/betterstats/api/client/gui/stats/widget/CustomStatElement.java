package io.github.thecsdev.betterstats.api.client.gui.stats.widget;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.util.stats.SUGeneralStat;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TBlankElement;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;
import net.minecraft.text.Text;

/**
 * A {@link TBlankElement} that only renders {@link Text}s on the
 * left and right side of the {@link CustomStatElement}.<br/>
 * Does not handle any user input or have a background color.
 */
public @Virtual class CustomStatElement extends TBlankElement
{
	// ==================================================
	public static final int HEIGHT = Math.max(GeneralStatWidget.HEIGHT, ItemStatWidget.SIZE);
	// --------------------------------------------------
	protected @Nullable Text txtLeft, txtRight;
	// ==================================================
	public CustomStatElement(int x, int y, int width, SUGeneralStat generalStat)
	{
		this(x, y, width, generalStat.getStatLabel(), generalStat.valueText);
	}
	
	public CustomStatElement(int x, int y, int width, @Nullable Text left, @Nullable Text right)
	{
		super(x, y, width, HEIGHT);
		this.txtLeft = left;
		this.txtRight = right;
	}
	// ==================================================
	public final @Nullable Text getLeftText() { return this.txtLeft; }
	public final @Nullable Text getRightText() { return this.txtLeft; }
	public @Virtual void setLeftText(@Nullable Text left) { this.txtLeft = left; }
	public @Virtual void setRightText(@Nullable Text right) { this.txtRight = right; }
	// ==================================================
	public @Virtual @Override void render(TDrawContext pencil)
	{
		pencil.drawTElementTextTH(this.txtLeft, HorizontalAlignment.LEFT);
		pencil.drawTElementTextTH(this.txtRight, HorizontalAlignment.RIGHT);
	}
	// ==================================================
}