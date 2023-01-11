package io.github.thecsdev.betterstats.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.components.Button;

@Mixin(Button.class)
public interface MixinButtonWidget
{
	@Mutable
	@Accessor("onPress")
	public abstract void setOnPress(Button.OnPress onPress);
}