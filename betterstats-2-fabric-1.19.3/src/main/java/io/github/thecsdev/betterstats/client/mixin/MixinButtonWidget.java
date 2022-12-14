package io.github.thecsdev.betterstats.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;

@Mixin(ButtonWidget.class)
public interface MixinButtonWidget
{
	@Mutable
	@Accessor("onPress")
	public abstract void setOnPress(PressAction onPress);
}