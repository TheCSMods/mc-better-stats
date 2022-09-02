package thecsdev.betterstats.client.gui.widget;

import java.util.function.Consumer;

import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import thecsdev.betterstats.client.gui.util.GuiUtils;

public class ActionCheckboxWidget extends CheckboxWidget
{
	// --------------------------------------------------
	protected Consumer<ActionCheckboxWidget> onChanged;
	public Text tooltip;
	// --------------------------------------------------
	public ActionCheckboxWidget(int x, int y, int width, int height, Text message, boolean checked, boolean showMessage) {
		super(x, y, width, height, message, checked, showMessage);
	}
	public ActionCheckboxWidget(int x, int y, int width, int height, Text message, boolean checked) {
		super(x, y, width, height, message, checked);
	}
	// --------------------------------------------------
	public ActionCheckboxWidget withTooltip(Text tooltip) { this.tooltip = tooltip; return this; }
	public void setChangedListener(Consumer<ActionCheckboxWidget> changedListener) { this.onChanged = changedListener; }
	// --------------------------------------------------
	@Override
	public void onPress()
	{
		super.onPress();
		if(onChanged != null)
			onChanged.accept(this);
	}
	
	@Override
	public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY)
	{
		if(tooltip != null && hovered)
			GuiUtils.drawTooltip(matrices, mouseX, mouseY, tooltip);
	}
	// --------------------------------------------------
}