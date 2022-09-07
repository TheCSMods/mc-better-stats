package thecsdev.betterstats.client.gui.screen;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;

import com.google.common.collect.Maps;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.chat.Component;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import thecsdev.betterstats.client.gui.widget.FillWidget;
import thecsdev.betterstats.client.gui.widget.stats.BSStatWidget;

public class ScreenWithScissors extends Screen
{
	// ==================================================
	public final HashMap<Widget, FillWidget> drawablesForCutting = Maps.newHashMap();
	// --------------------------------------------------
	//WARNING: FOR KEY/SCAN-CODE COMPARING ONLY, DO NOT REGISTER THE FOLLOWING KeyBinding-S
	public static final KeyBinding Key_Up = new KeyBinding("null", InputUtil.GLFW_KEY_UP, "null");
	public static final KeyBinding Key_Down = new KeyBinding("null", InputUtil.GLFW_KEY_DOWN, "null");
	public static final KeyBinding Key_Left = new KeyBinding("null", InputUtil.GLFW_KEY_LEFT, "null");
	public static final KeyBinding Key_Right = new KeyBinding("null", InputUtil.GLFW_KEY_RIGHT, "null");
	public static final KeyBinding Key_Enter = new KeyBinding("null", InputUtil.GLFW_KEY_ENTER, "null");
	public static final KeyBinding Key_KpEnter = new KeyBinding("null", InputUtil.GLFW_KEY_KP_ENTER, "null");
	// ==================================================
	protected ScreenWithScissors(Component title) { super(title); }
	
	@Override
	public void tick()
	{
		super.tick();
		for(Element child : this.children())
		{
			if(child instanceof TextFieldWidget)
			{
				TextFieldWidget tfw = (TextFieldWidget)child; 
				if(getFocused() == tfw)
					tfw.tick();
				else tfw.setTextFieldFocused(false);
			}
		}
	}
	// --------------------------------------------------
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
	public ClickableWidget raycast(ClickableWidget centerChild, Direction direction)
	{
		//define the ray and other variables that will be used for raycasting
		Rectangle center = new Rectangle(centerChild.x, centerChild.y, centerChild.getWidth(), centerChild.getHeight());
		Rectangle ray;
		Point centerPos = center.getLocation();
		
		if(direction == Direction.NORTH)
		{
			ray = new Rectangle(0, 0, width, center.y);
			ray.y -=500; ray.height += 500;
		}
		else if(direction == Direction.SOUTH)
		{
			ray = new Rectangle(0, center.y + center.height, width, height - center.y + center.height);
			ray.height += 500;
		}
		else if(direction == Direction.WEST)
		{
			ray = new Rectangle(0, 0, center.x, height);
			ray.x -= 500; ray.width += 500;
		}
		else if(direction == Direction.EAST)
		{
			ray = new Rectangle(center.x + center.width, 0, width - center.x + center.width, height);
			ray.width += 500;
		}
		else return null;
		
		//iterate all children, find the ones that collide with the ray,
		//and then find the closest child that collides with the ray
		ClickableWidget hit = null;
		for(Element child : children())
		{
			//check if clickable
			if(!(child instanceof ClickableWidget) || child == centerChild)
				continue;
			ClickableWidget cwChild = (ClickableWidget)child;
			
			//check if cwChild (top-left corner) is inside of ray
			//Rectangle cwChildRect = new Rectangle(cwChild.x, cwChild.y, cwChild.getWidth(), cwChild.getHeight());
			Point cwChildPos = new Point(cwChild.x, cwChild.y);
			if(!ray.contains(cwChildPos))
				continue;
			
			//check if cwChild accepts focus
			if(!cwChild.active || !cwChild.visible)
				continue;
			
			if(!(cwChild instanceof BSStatWidget))
			{
				if(cwChild.isFocused() == cwChild.changeFocus(true))
					continue;
				cwChild.changeFocus(true); //change it back
			}
			
			//assign the first non-null element
			if(hit == null)
			{
				hit = cwChild;
				continue;
			}
			//check for closest element
			else
			{
				double closePos = centerPos.distance(new Point(hit.x, hit.y));
				double childPos = centerPos.distance(cwChildPos);
				
				if(childPos < closePos)
					hit = cwChild;
			}
		}
		
		//return the hit element
		return hit;
	}
	// ==================================================
	public void removeDrawable(Drawable drawableChild) { swsOn_removeDrawable(drawableChild); }
	public boolean swsFocusOn(Element element, boolean lookForwards) { return swsOn_swsFocusOn(element, lookForwards); }
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) { swsOn_render(matrices, mouseX, mouseY, delta); }
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) { return swsOn_mouseClicked(mouseX, mouseY, button); }
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return swsOn_keyPressed(keyCode, scanCode, modifiers); }
	
	//The following methods's functionalities are defined
	//in the ScreenWithScissorsMixin.java class.
	//Applying Mixins to the methods above for some reason failed.
	
	//@Inject(method = "swsOn_removeDrawable", at = @At("HEAD"), cancellable = true)
	private void swsOn_removeDrawable(Drawable drawableChild) {}
	
	//@Inject(method = "swsOn_swsFocusOn", at = @At("HEAD"), cancellable = true)
	private boolean swsOn_swsFocusOn(Element element, boolean lookForwards) { throw new RuntimeException("This should not happen."); }
	
	//@Inject(method = "swsOn_render", at = @At("HEAD"), cancellable = true)
	private void swsOn_render(MatrixStack matrices, int mouseX, int mouseY, float delta) {}
	
	//@Inject(method = "swsOn_mouseClicked", at = @At("RETURN"))
	private boolean swsOn_mouseClicked(double mouseX, double mouseY, int button) { return super.mouseClicked(mouseX, mouseY, button); }
	
	//@Inject(method = "swsOn_keyPressed", at = @At("RETURN"))
	private boolean swsOn_keyPressed(int keyCode, int scanCode, int modifiers) { return super.keyPressed(keyCode, scanCode, modifiers); }
	// ==================================================
}