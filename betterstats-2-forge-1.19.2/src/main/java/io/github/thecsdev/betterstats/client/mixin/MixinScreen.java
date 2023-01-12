package io.github.thecsdev.betterstats.client.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.gui.screens.Screen;

@Mixin(Screen.class)
public interface MixinScreen
{
	/*@Accessor("selected")
	public abstract Selectable getSelected();
	
	@Accessor("selected")
	public abstract void setSelected(Selectable value);
	
	@Accessor("children")
	public abstract List<Element> getChildren();
	
	@Accessor("drawables")
	public abstract List<Drawable> getDrawables();
	
	@Accessor("selectables")
	public abstract List<Selectable> getSelectables();*/
}