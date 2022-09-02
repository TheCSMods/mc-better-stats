package thecsdev.betterstats.client.mixin;

import java.util.HashMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import thecsdev.betterstats.client.gui.screen.ScreenWithScissors;
import thecsdev.betterstats.client.gui.widget.FillWidget;

@Mixin(ScreenWithScissors.class)
public abstract class ScreenWithScissorsMixin extends Screen
{
	protected ScreenWithScissorsMixin(Text title) { super(title); }
	
	@Inject(method = "swsOn_render", at = @At("HEAD"), cancellable = true, remap = false)
	public void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo callback)
	{
		callback.cancel();
		
		//get drawables for cutting
		HashMap<Drawable, FillWidget> dfc = ((ScreenWithScissors)(Object)this).drawablesForCutting;
		
		//render elements
		for(Drawable child : ((ScreenMixin)(Object)this).getDrawables())
		{
			if(dfc.containsKey(child))
				dfc.get(child).applyScissor(child, matrices, mouseX, mouseY, delta);
			else child.render(matrices, mouseX, mouseY, delta);
		}
		
		//---------- render element tooltips
		//z-index-offset (items render at 100)
		matrices.push();
		matrices.translate(0, 0, 200 + Math.abs(this.itemRenderer.zOffset));
		for(Selectable child : ((ScreenMixin)(Object)this).getSelectables())
		{
			if(child instanceof ClickableWidget)
			{
				ClickableWidget cwChild = (ClickableWidget)child;
				if(cwChild.isHovered())
					cwChild.renderTooltip(matrices, mouseX, mouseY);
			}
		}
		matrices.pop();
	}
	
	@Inject(method = "swsOn_removeDrawable", at = @At("HEAD"), cancellable = true, remap = false)
	public void onRemoveDrawable(Drawable drawableChild, CallbackInfo callback)
	{
		callback.cancel();
		
		//remove Element if it is an element
		if(drawableChild instanceof Element)
			remove((Element) drawableChild);
		
		//remove from the cutting list
		((ScreenWithScissors)(Object)this).drawablesForCutting.remove(drawableChild);
		
		//remove drawable
		((ScreenMixin)(Object)this).getDrawables().remove(drawableChild);
	}
	
	@Inject(method = "swsOn_mouseClicked", at = @At("RETURN"), remap = false)
	public void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> callback)
	{
		//clear selection when click hits no elements
		boolean b0 = callback.getReturnValue();
		if(!b0 && getFocused() != null && !getFocused().changeFocus(true))
		{
			focusOn(null);
			((ScreenMixin)(Object)this).setSelected(null);;
		}
	}
}