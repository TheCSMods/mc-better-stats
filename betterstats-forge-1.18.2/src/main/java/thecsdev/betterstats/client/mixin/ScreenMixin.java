package thecsdev.betterstats.client.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;

@Mixin(Screen.class)
public abstract class ScreenMixin
{
	@Accessor("lastNarratable") public abstract NarratableEntry getSelected();
	@Accessor("lastNarratable") public abstract void setSelected(NarratableEntry value);
	
	@Accessor("children") public abstract List<GuiEventListener> getChildren();
	@Accessor("narratables") public abstract List<NarratableEntry> getNarratables();
	@Accessor("renderables") public abstract List<Widget> getRenderables();
}