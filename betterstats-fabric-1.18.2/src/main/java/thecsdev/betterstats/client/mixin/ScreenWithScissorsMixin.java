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
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import thecsdev.betterstats.client.gui.screen.ScreenWithScissors;
import thecsdev.betterstats.client.gui.widget.FillWidget;
import thecsdev.betterstats.client.gui.widget.stats.BSStatWidget;

@Mixin(ScreenWithScissors.class)
public abstract class ScreenWithScissorsMixin extends Screen
{
	// ==================================================
	protected ScreenWithScissorsMixin(Text title) { super(title); }
	// ==================================================
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
	// --------------------------------------------------
	@Inject(method = "swsOn_swsFocusOn", at = @At("HEAD"), cancellable = true, remap = false)
	public void onSwsFocusOn(Element element, boolean lookForwards, CallbackInfoReturnable<Boolean> callback)
	{
		//unfocus the focused element
		boolean b0 = getFocused() == null;
		if(!b0)
		{
			//change focus
			b0 = getFocused().changeFocus(lookForwards);
			if(b0) //try again
			{
				b0 = getFocused().changeFocus(lookForwards);
				if(b0) //give up
					return;
			}
			
			//let the next if statement pass
			b0 = true;
		}
		
		//if there was no focused element or the focused one got unfocused
		if(b0)
		{
			//change focus to null for now
			setFocused(null);
			((ScreenMixin)(Object)this).setSelected(null);
			
			//change focus for the new element
			boolean b1 = element == null || element.changeFocus(lookForwards);
			if(b1)
			{
				//focus on the new element
				setFocused(element);
				Selectable s = (element instanceof Selectable) ? (Selectable)element : null;
				((ScreenMixin)(Object)this).setSelected(s);
				
				if(element instanceof BSStatWidget)
					((BSStatWidget)element).onFocusedChanged(((BSStatWidget)element).isFocused());
			}
		}
		
		//return
		callback.setReturnValue(b0);
		callback.cancel();
	}
	// ==================================================
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
	// --------------------------------------------------
	@Inject(method = "swsOn_mouseClicked", at = @At("RETURN"), remap = false)
	public void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> callback)
	{
		//clear selection when click hits no elements
		boolean b0 = callback.getReturnValue();
		if(!b0)
			((ScreenWithScissors)(Object)this).swsFocusOn(null, true);
	}
	// --------------------------------------------------
	@Inject(method = "swsOn_keyPressed", at = @At("RETURN"), cancellable = true, remap = false)
	public void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> callback)
	{
		//check if an element is focused at all
		if(getFocused() == null)
		{
			boolean bl = !hasShiftDown();
			if (!changeFocus(bl))
				changeFocus(bl); 
		}
		
		//check for current selected element
		if(!(getFocused() instanceof ClickableWidget) ||
				(getFocused() instanceof TextFieldWidget))
			return;
		
		ClickableWidget focused = (ClickableWidget)getFocused();
		Direction raycastDir;
		boolean lookForwards = true;
		
		//pressing up to select element above
		if(ScreenWithScissors.Key_Up.matchesKey(keyCode, scanCode))
		{
			raycastDir = Direction.NORTH;
			lookForwards = false;
		}
		else if(ScreenWithScissors.Key_Down.matchesKey(keyCode, scanCode))
		{
			raycastDir = Direction.SOUTH;
			lookForwards = true;
		}
		else if(ScreenWithScissors.Key_Left.matchesKey(keyCode, scanCode))
		{
			raycastDir = Direction.WEST;
			lookForwards = false;
		}
		else if(ScreenWithScissors.Key_Right.matchesKey(keyCode, scanCode))
		{
			raycastDir = Direction.EAST;
			lookForwards = true;
		}
		else raycastDir = null;
		
		//check raycast direction
		if(raycastDir == null) return;
		
		//perform raycast and focus on casted element
		ClickableWidget hit = ((ScreenWithScissors)(Object)this).raycast(focused, raycastDir);
		if(hit != null && ((ScreenWithScissors)(Object)this).swsFocusOn(hit, lookForwards))
			callback.setReturnValue(true);
	}
	// ==================================================
}