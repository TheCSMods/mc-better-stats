package thecsdev.betterstats.client.gui.screen;

import java.util.HashMap;

import com.google.common.collect.Maps;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import thecsdev.betterstats.client.gui.widget.FillWidget;

public class ScreenWithScissors extends Screen
{
	// ==================================================
	public final HashMap<Drawable, FillWidget> drawablesForCutting = Maps.newHashMap();
	// ==================================================
	protected ScreenWithScissors(Text title) { super(title); }
	
	protected Drawable applyScissors(Drawable drawable, FillWidget scissors)
	{
		this.drawablesForCutting.put(drawable, scissors);
		return drawable;
	}
	// --------------------------------------------------
	protected <T extends Drawable> T addCutDrawable(T drawable, FillWidget scissors)
	{
		applyScissors(drawable, scissors);
		return super.addDrawable(drawable);
	}
	
	protected <T extends Element & Drawable & Selectable> T addCutDrawableChild(T drawableElement, FillWidget scissors)
	{
		applyScissors(drawableElement, scissors);
		return super.addDrawableChild(drawableElement);
	}
	
	@Override
	protected void remove(Element child)
	{
		if(child instanceof Drawable)
			this.drawablesForCutting.remove((Drawable)child);
		super.remove(child);
	}
	
	@Override
	protected void clearChildren()
	{
		super.clearChildren();
		this.drawablesForCutting.clear();
	}
	// ==================================================
	protected void removeDrawable(Drawable drawableChild) { swsOn_removeDrawable(drawableChild); }
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) { swsOn_render(matrices, mouseX, mouseY, delta); }
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) { return swsOn_mouseClicked(mouseX, mouseY, button); }
	
	//The following methods's functionalities are defined
	//in the ScreenWithScissorsMixin.java class.
	//Applying Mixins to the methods above for some reason failed.
	
	//@Inject(method = "swsOn_render", at = @At("HEAD"), cancellable = true)
	private void swsOn_render(MatrixStack matrices, int mouseX, int mouseY, float delta) {}
	
	//@Inject(method = "swsOn_removeDrawable", at = @At("HEAD"), cancellable = true)
	private void swsOn_removeDrawable(Drawable drawableChild) {}
	
	//@Inject(method = "swsOn_mouseClicked", at = @At("RETURN"))
	private boolean swsOn_mouseClicked(double mouseX, double mouseY, int button) { return super.mouseClicked(mouseX, mouseY, button); }
	// ==================================================
}