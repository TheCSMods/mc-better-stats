package thecsdev.betterstats.client.gui.widget;

import static java.lang.Math.abs;
import static thecsdev.betterstats.BetterStats.lt;

import java.awt.Color;
import java.awt.Point;
import java.util.HashSet;
import java.util.function.Consumer;

import com.google.common.collect.Sets;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import thecsdev.betterstats.client.gui.util.GuiUtils;
import thecsdev.betterstats.client.gui.widget.FillWidget.FWScrollOptions.FWScrollbarType;
import thecsdev.betterstats.util.math.PointAndSize;
import thecsdev.betterstats.util.math.Tuple;

//initially used only for the 'fill' function when rendering, but has since expanded
//it's behavior to feature 'scissors' and 'scrolling'. requires `ScreenWithScissors`.
public class FillWidget extends ClickableWidget implements Selectable, Drawable, Element
{
	// ==================================================
	public enum FWBorderMode { None, Always, Hover }
	// ==================================================
	public int color;
	protected boolean focusible, focused, hovered;
	
	public int borderColor;
	public FWBorderMode borderMode;
	public final FWScrollOptions scroll = new FWScrollOptions();
	// ==================================================
	public FillWidget(int x, int y, int width, int height, int color)
	{
		super(x, y, width, height, lt(""));
		
		this.color = color;
		
		this.hovered = false;
		this.focused = false;
		this.focusible = true;
		
		this.borderMode = FWBorderMode.Always;
		this.borderColor = Color.black.getRGB();
	}
	
	public FillWidget withBorder(FWBorderMode mode, int color)
	{
		this.borderMode = mode;
		this.borderColor = color;
		return this;
	}
	
	public FillWidget withoutFocus() { this.focusible = false; return this; }
	public FillWidget withScroll(FWScrollbarType scrollbar, int min, int max) { return withScroll(scrollbar, min, max, 20); }
	public FillWidget withScroll(FWScrollbarType scrollbar, int min, int max, int sensitivity)
	{
		this.scroll.barType = scrollbar;
		this.scroll.min = min;
		this.scroll.max = max;
		this.scroll.sensitivity = sensitivity;
		this.scroll.value = 0;
		return this;
	}
	
	// --------------------------------------------------
	@Override
	public void appendNarrations(NarrationMessageBuilder builder) {}

	@Override
	public SelectionType getType()
	{
		if(isFocused()) return SelectionType.FOCUSED;
		else if(isHovered()) return SelectionType.HOVERED;
		else return SelectionType.NONE;
	}
	
	@Override
	public boolean changeFocus(boolean lookForwards)
	{
		if(!focusible)
			return (this.focused = false);
		
		this.focused = !this.focused;
		return this.focused;
	}
	// ==================================================
	public boolean isFocused() { return this.focusible && this.focused; }
	public boolean isHovered() { return isFocused() || this.hovered; }
	@Override public boolean isMouseOver(double mouseX, double mouseY) { return this.hovered; }
	// --------------------------------------------------
	public void onScroll_apply(double mouseX, double mouseY)
	{
		scroll.entries.forEach(entry ->
		{
			entry.mouseX = mouseX;
			entry.mouseY = mouseY;
			entry.scroll = scroll.value;
			entry.applyScroll.accept(entry); 
		});
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		this.scroll.setValue(this.scroll.value + (amount * scroll.sensitivity));
		onScroll_apply(mouseX, mouseY);
		return true;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		//check if hovering
		if(!scroll.barTransform.isHovering(mouseX, mouseY))
			return false;
		
		//create points
		Point mousePos = new Point((int)mouseX, (int)mouseY);
		Point mouseRelPos = new Point(
				(int)(mouseX - scroll.barTransform.x),
				(int)(mouseY - scroll.barTransform.y));
		
		//assign point and return
		scroll.barDragging = new Tuple<Point, Point>(mousePos, mouseRelPos);
		return true;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
	{
		//check if can scroll
		if(!scroll.canScroll())
		{
			scroll.barDragging = null;
			return false;
		}
		
		if(scroll.barDragging != null)
		{
			int barWasAtY = scroll.barDragging.Item1.y - scroll.barDragging.Item2.y;
			int barIsAtY =  barWasAtY + ((int)mouseY - scroll.barDragging.Item1.y);
			
			int i0 = this.height - scroll.barTransform.height;
			double val = (double)(barIsAtY - this.y) / i0;
			
			//double val = (mouseY - this.y) / (this.height - this.y); -- old method
			val = 1d - val;
			val = MathHelper.lerp(val, scroll.min, scroll.max);
			
			scroll.setValue(val);
			onScroll_apply(mouseX, mouseY);
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		boolean b0 = scroll.barDragging != null;
		scroll.barDragging = null;
		return b0;
	}
	// --------------------------------------------------
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		this.hovered = (mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height);
		fill(matrices, x, y, x + width, y + height, color);
		
		if(canDrawBorder())
			drawBorder(matrices, x, y, width, height, borderColor);
		if(scroll.barType == FWScrollbarType.AlwaysVertical)
			drawScrollBar(matrices);
	}
	
	protected boolean canDrawBorder()
	{
		return borderMode == FWBorderMode.Always ||
				(borderMode == FWBorderMode.Hover && isHovered());
	}
	
	protected void drawBorder(MatrixStack matrices, int x, int y, int width, int height, int borderColor)
	{
		//horizontal - top and bottom
		drawHorizontalLine(matrices, x, x + width - 1, y, borderColor);
		drawHorizontalLine(matrices, x, x + width - 1, y + height - 1, borderColor);
		
		//vertical - left and right
		drawVerticalLine(matrices, x, y, y + height, borderColor);
		drawVerticalLine(matrices, x + width - 1, y, y + height, borderColor);
	}
	
	protected void drawScrollBar(MatrixStack matrices)
	{
		//get x, y, width, height
		int width = 6;
		int x = this.x + this.width - 2 - width;
		int y = this.y + 2;
		int height = abs(y - (y + this.height - 4));
		
		//draw border
		drawBorder(matrices, x, y, width, height, scroll.barBorderColor);
		if(!scroll.canScroll())
		{
			this.scroll.barTransform.width = width;
			this.scroll.barTransform.height = height;
			return;
		}
		
		//calculate bar position
		int minPlusMax = abs(scroll.min) + abs(scroll.max);
		
		int barH = (int) (((float)this.height / (float)(this.height + minPlusMax)) * this.height);
		int barY = height - barH;
		barY *= 1f - ((float)(scroll.value + abs(scroll.min)) / (float)minPlusMax);
		barY += y;
		
		barH++;
		
		//draw bar
		this.scroll.barTransform.x = x;
		this.scroll.barTransform.y = barY;
		this.scroll.barTransform.width = width;
		this.scroll.barTransform.height = barH;
		fill(matrices, x, barY, x + width, barY + barH, scroll.barColor);
	}
	// --------------------------------------------------
	public void applyScissor(Runnable renderingAction) { GuiUtils.applyScissor(x, y, width, height, renderingAction); }
	public void applyScissor(Drawable drawable, MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		applyScissor(() -> drawable.render(matrices, mouseX, mouseY, delta));
	}
	// ==================================================
	public final class FWScrollOptions
	{
		public enum FWScrollbarType { None, AlwaysVertical }
		
		public FWScrollbarType barType;
		public int barColor, barBorderColor;
		
		//barDragging contains the mouse position of when the mouse started dragging
		//as well as mouse position relative to the scroll bar.
		//used for calculations when dragging the scroll-bar.
		//set to null when the scroll-bar isn't being dragged.
		private Tuple<Point, Point> barDragging;
		public final PointAndSize barTransform = new PointAndSize(0, 0);
		
		private double value;
		public int min, max;
		public int sensitivity;
		
		public final HashSet<FWScrollEntry> entries = Sets.newHashSet();
		
		public FWScrollOptions()
		{
			this.min = 0;
			this.max = 0;
			this.value = 0;
			this.sensitivity = 20;
			
			this.barType = FWScrollbarType.None;
			this.barColor = Color.lightGray.getRGB();
			this.barBorderColor = Color.gray.getRGB();
			this.barDragging = null;
		}
		
		public void clearScrollData()
		{
			this.value = 0;
			this.min = 0;
			this.max = 0;
		}

		public FWScrollEntry makeScrollable(ClickableWidget target)
		{
			FWScrollEntry a = new FWScrollEntry(target);
			entries.add(a);
			return a;
		}

		public FWScrollEntry makeScrollable(Drawable target, int x, int y, Consumer<FWScrollEntry> applyScroll)
		{
			FWScrollEntry a = new FWScrollEntry(target, x, y, applyScroll);
			entries.add(a);
			return a;
		}
		
		public boolean hasScrollBar() { return this.barType != FWScrollbarType.None; }
		public boolean canScroll() { return this.min != 0 || this.max != 0; }
		public double getValue() { return this.value; }
		public double setValue(double value) { return this.value = MathHelper.clamp(value, min, max); }
	}
	
	public static final class FWScrollEntry
	{
		public static final Consumer<FWScrollEntry> DEFAULT_APPLY_SCROLL = arg0 ->
		{
			//check if clickable
			if(!(arg0.target instanceof ClickableWidget))
				return;
			
			//move on the Y axis
			ClickableWidget cw = (ClickableWidget) arg0.target;
			cw.y = (int) (arg0.y + arg0.scroll);
		};
		
		public final int x, y;
		public final Drawable target;
		
		public final Consumer<FWScrollEntry> applyScroll;
		public double mouseX, mouseY, scroll;
		
		public FWScrollEntry(ClickableWidget target) { this(target, target.x, target.y, null); }
		public FWScrollEntry(Drawable target, int x, int y, Consumer<FWScrollEntry> applyScroll)
		{
			if(applyScroll == null)
				applyScroll = DEFAULT_APPLY_SCROLL;
			
			this.x = x;
			this.y = y;
			this.target = target;
			this.applyScroll = applyScroll;
		}
	}
	// ==================================================
}