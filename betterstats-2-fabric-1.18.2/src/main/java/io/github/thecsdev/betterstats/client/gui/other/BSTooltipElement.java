package io.github.thecsdev.betterstats.client.gui.other;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.tcdcommons.api.client.gui.TElement;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TTooltipElement;
import io.github.thecsdev.tcdcommons.api.util.SubjectToChange;

@SubjectToChange
public class BSTooltipElement extends TTooltipElement
{
	// ==================================================
	public BSTooltipElement(int maxWidth) { super(maxWidth); }
	// ==================================================
	public @Override void refreshPosition(@Nullable TElement target, int mouseX, int mouseY)
	{
		if(target != null) refreshPosition_toElement(target);
		else refreshPosition_toCursor(mouseX, mouseY);
	}
	// ==================================================
}