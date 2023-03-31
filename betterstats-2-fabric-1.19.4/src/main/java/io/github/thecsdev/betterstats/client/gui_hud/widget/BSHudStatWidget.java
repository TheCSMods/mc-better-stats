package io.github.thecsdev.betterstats.client.gui_hud.widget;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Objects;

import org.apache.logging.log4j.util.TriConsumer;

import io.github.thecsdev.tcdcommons.api.client.gui.TElement;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TEntityRendererElement;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TContextMenuPanel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.stat.StatHandler;
import net.minecraft.text.Text;

public abstract class BSHudStatWidget extends TElement
{
	// ==================================================
	public static final int SIZE = 21;
	public static final int DRAG_STEP = 5;
	// --------------------------------------------------
	/**
	 * The anchor points of this {@link BSHudStatWidget}
	 * that are used to dynamically reposition this widget
	 * whenever the game window resizes.
	 */
	protected double anchorX, anchorY;
	// --------------------------------------------------
	protected final StatHandler statHandler;
	protected final TriConsumer<TElement, Boolean, Boolean> ehChildAr_reposition;
	// ==================================================
	public BSHudStatWidget(int x, int y, StatHandler statHandler)
	{
		super(x, y, SIZE, SIZE);
		this.statHandler = Objects.requireNonNull(statHandler, "statHandler must not be null.");
		ehChildAr_reposition = getEvents().CHILD_AR.addWeakEventHandler((child, added, repositioned) ->
		{
			//only when added
			if(!added) return;
			//get last child
			var lastChild = child.previous();
			//reposition this new child based on the last child
			if(!getTChildren().contains(lastChild))
				child.setPosition(0, 0, true);
			else child.setPosition(lastChild.getTpeEndX(), getTpeY(), false);
		});
	}
	// --------------------------------------------------
	public @Override void updateRenderingBoundingBox()
	{
		RENDER_RECT.setLocation(x, y);
		RENDER_RECT.setSize(width, height);
	}
	// --------------------------------------------------
	public @Override void onParentChanged() { if(getTParent() != null) init(); }
	// ==================================================
	/**
	 * Recalculates the {@link #anchorX} and {@link #anchorY}
	 * points used for dynamically repositioning this element.
	 * @return False when this fails, which only happens if {@link #screen} is null.
	 */
	public boolean reCalculateAnchor()
	{
		//if there is no screen, can not re-calculate
		if(this.screen == null) return false;
		
		//get screen size
		int sW = this.screen.getTpeWidth();
		int sH = this.screen.getTpeHeight();
		
		//subtract the size of this element
		sW -= getTpeWidth();
		sH -= getTpeHeight();
		
		//calculate anchor points XY, and also
		//make sure this element is always smaller
		if(sW > 0) this.anchorX = ((double)getTpeX()) / sW; else this.anchorX = 0;
		if(sH > 0) this.anchorY = ((double)getTpeY()) / sH; else this.anchorY = 0;
		
		//return
		return true;
	}
	
	/**
	 * Repositions this element according to the
	 * {@link #anchorX} and {@link #anchorY} points.
	 * @return False when this fails, which only happens if {@link #screen} is null.
	 */
	public boolean rePositionToAnchor()
	{
		//if there is no screen, can not re-position
		if(this.screen == null) return false;
		
		//get screen size
		int sW = this.screen.getTpeWidth();
		int sH = this.screen.getTpeHeight();
		
		//subtract the size of this element
		sW -= getTpeWidth();
		sH -= getTpeHeight();
		
		//calculate XY
		int x = (int) (this.anchorX * sW);
		int y = (int) (this.anchorY * sH);
		
		//reposition and return
		setPosition(x, y, false);
		return true;
	}
	// ==================================================
	@SuppressWarnings("deprecation")
	public final void init()
	{
		//clear and re-initialize
		clearTChildren();
		onInit();
		//reAlign and update
		reAlignElements();
		//update rectangles
		updateRenderingBoundingBox();
		forEachChild(child -> { child.updateRenderingBoundingBox(); return false; }, true);
		//tick
		tick();
	}
	public abstract void onInit();
	// --------------------------------------------------
	public void reAlignElements()
	{
		//realign children
		TElement last = null;
		for(TElement child : getTChildren())
		{
			if(last == null) child.setPosition(0, 0, true);
			else child.setPosition(last.getTpeEndX(), getTpeY(), false);
			last = child;
		}
		//update size (width) to fit children
		if(last == null) this.width = SIZE;
		else this.width = Math.abs(last.getTpeEndX() - getTpeX());
		//update the bounding box
		updateRenderingBoundingBox(); 
	}
	// --------------------------------------------------
	public ItemEntry addItemEntry(Item item) { return new ItemEntry(item); }
	public EntityEntry addEntityEntry(EntityType<?> entityType) { return new EntityEntry(entityType); }
	// --------------------------------------------------
	@Override
	protected void onContextMenu(TContextMenuPanel contextMenu)
	{
		//call super
		super.onContextMenu(contextMenu);
		//add separator if needed
		if(contextMenu.getTChildren().size() > 0)
			contextMenu.addSeparator();
		//add the remove/delete option
		contextMenu.addButton(translatable("selectWorld.deleteButton"), btn ->
		{
			if(getTParent() != null)
				getTParent().removeTChild(this);
		});
	}
	// ==================================================
	protected int dragOffMouseX, dragOffMouseY;
	public @Override boolean mousePressed(int mouseX, int mouseY, int button)
	{
		//handle right clicks
		if(button == 1)
		{
			showContextMenu(mouseX, mouseY);
			return true;
		}
		
		//prepare for drag
		dragOffMouseX = mouseX - getTpeX();
		dragOffMouseY = mouseY - getTpeY();
		
		//true blocks dragging, so return false
		return false;
	}
	public @Override boolean mouseDragged(double mouseX, double mouseY, double deltaX, double deltaY, int button)
	{
		int targetX = ((int)mouseX) - dragOffMouseX;
		int targetY = ((int)mouseY) - dragOffMouseY;
		targetX -= (targetX % DRAG_STEP);
		targetY -= (targetY % DRAG_STEP);
		setPosition(targetX, targetY, false);
		reCalculateAnchor();
		return true;
	}
	// ==================================================
	public @Override void render(MatrixStack matrices, int mouseX, int mouseY, float deltaTime)
	{
		fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, 1342177280);
		fill(matrices, this.x, this.y, this.x + SIZE, this.y + this.height, 1342177280);
		if(isFocusedOrHovered()) drawOutline(matrices, -1);
	}
	// ==================================================
	/**
	 * An entry that renders an {@link Item}
	 * on the {@link BSHudStatWidget}.
	 */
	protected class ItemEntry extends TElement
	{
		// ----------------------------------------------
		protected ItemStack stack;
		// ----------------------------------------------
		public ItemEntry(Item item)
		{
			super(0, 0, SIZE, SIZE);
			if(item == null) item = Items.AIR;
			this.stack = item.getDefaultStack();
			BSHudStatWidget.this.addTChild(this);
			
		}
		public @Override boolean isClickThrough() { return true; }
		public @Override int getTpeY() { return BSHudStatWidget.this.getTpeY(); }
		// ----------------------------------------------
		public @Override void render(MatrixStack matrices, int mouseX, int mouseY, float deltaTime)
		{
			getItemRenderer().renderGuiItemIcon(matrices, this.stack, getTpeX() + 3, getTpeY() + 3);
		}
		// ----------------------------------------------
	}
	/**
	 * An entry that renders an {@link EntityType}
	 * on the {@link BSHudStatWidget}.
	 */
	protected class EntityEntry extends TEntityRendererElement
	{
		// ----------------------------------------------
		public EntityEntry(EntityType<?> entityType)
		{
			super(0, 0, SIZE, SIZE, entityType);
			BSHudStatWidget.this.addTChild(this);
		}
		public @Override boolean isClickThrough() { return true; }
		public @Override int getTpeY() { return BSHudStatWidget.this.getTpeY(); }
		// ----------------------------------------------
		public @Override void render(MatrixStack matrices, int mouseX, int mouseY, float deltaTime)
		{
			super.render(matrices, getTpeX() + 150, getTpeY() + 55, deltaTime);
		}
		// ----------------------------------------------
	}
	/**
	 * An entry that renders a text label
	 * on the {@link BSHudStatWidget}.
	 */
	protected class LabelEntry extends TLabelElement
	{
		// ----------------------------------------------
		protected int txt_width;
		// ----------------------------------------------
		public LabelEntry(Text text)
		{
			super(0, 0, SIZE, SIZE, text);
			BSHudStatWidget.this.addTChild(this);
		}
		public @Override boolean isClickThrough() { return true; }
		public @Override int getTpeY() { return BSHudStatWidget.this.getTpeY() + 1; }
		
		protected void setTpeWidth(int width)
		{
			this.width = width;
			updateRenderingBoundingBox();
			reAlignElements();
		}
		// ----------------------------------------------
		@Override
		public void setText(Text label)
		{
			//set the text
			super.setText(label);
			//update the width
			var w = (label != null) ? getTextRenderer().getWidth(label) : 0;
			if(w != this.txt_width) setTpeWidth(w + 10);
			this.txt_width = w;
		}
		// ----------------------------------------------
		@Override
		public void render(MatrixStack matrices, int mouseX, int mouseY, float deltaTime)
		{
			drawTElementText(matrices, getText(), getHorizontalAlignment(), getColor(), 5, deltaTime);
		}
		// ----------------------------------------------
	}
	// ==================================================
}