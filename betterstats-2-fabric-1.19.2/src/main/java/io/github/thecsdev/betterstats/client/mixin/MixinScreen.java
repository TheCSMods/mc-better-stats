package io.github.thecsdev.betterstats.client.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;

@Mixin(Screen.class)
public interface MixinScreen
{
	@Accessor("selected")
	public abstract Selectable getSelected();
	
	@Accessor("selected")
	public abstract void setSelected(Selectable value);
	
	@Accessor("children")
	public abstract List<Element> getChildren();
	
	@Accessor("drawables")
	public abstract List<Drawable> getDrawables();
	
	@Accessor("selectables")
	public abstract List<Selectable> getSelectables();
}