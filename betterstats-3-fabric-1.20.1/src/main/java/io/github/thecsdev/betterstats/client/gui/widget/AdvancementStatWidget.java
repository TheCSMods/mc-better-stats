package io.github.thecsdev.betterstats.client.gui.widget;

import static io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget.SIZE;
import static io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement.COLOR_OUTLINE_FOCUSED;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;

import java.util.Objects;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TClickableWidget;
import net.minecraft.advancement.Advancement;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Formatting;

/**
 * A stat widget representing statistics about an {@link Advancement}.
 */
@Experimental
public final @Internal class AdvancementStatWidget extends TClickableWidget
{
	// ==================================================
	private final Advancement advancement;
	private final ItemStack   displayItem;
	// --------------------------------------------------
	private int backgroundColor = 0;
	private @Nullable Consumer<AdvancementStatWidget> onClick;
	// ==================================================
	public AdvancementStatWidget(int x, int y, Advancement advancement) throws NullPointerException
	{
		super(x, y, SIZE, SIZE);
		this.advancement = Objects.requireNonNull(advancement);
		
		final var tooltip = literal("");
		final @Nullable var display = advancement.getDisplay();
		if(display != null)
		{
			tooltip.append(literal("").append(display.getTitle()).formatted(Formatting.YELLOW));
			tooltip.append("\n");
			tooltip.append(literal("").append(display.getDescription()).formatted(Formatting.GRAY));
			this.displayItem = display.getIcon();
			
			this.backgroundColor = switch(display.getFrame())
			{
				case TASK      -> TPanelElement.COLOR_BACKGROUND;
				case GOAL      -> 0x50223333;
				case CHALLENGE -> 0x44330066;
				default        -> TPanelElement.COLOR_BACKGROUND;
			};
		}
		else
		{
			tooltip.append(literal(advancement.getId().toString()).formatted(Formatting.YELLOW));
			this.displayItem = Items.AIR.getDefaultStack();
			this.backgroundColor = TPanelElement.COLOR_BACKGROUND;
		}
		setTooltip(Tooltip.of(tooltip));
	}
	// ==================================================
	/**
	 * Retrieves the "on-click" action of this {@link AdvancementStatWidget}.
	 */
	public final @Nullable Consumer<AdvancementStatWidget> getOnClick() { return this.onClick; }
	
	/**
	 * Sets the action that will take place when this {@link AdvancementStatWidget} is clicked.
	 */
	public final void setOnClick(@Nullable Consumer<AdvancementStatWidget> onClick) { this.onClick = onClick; }
	// --------------------------------------------------
	/**
	 * Returns the associated {@link Advancement}.
	 */
	public final Advancement getAdvancement() { return this.advancement; }
	// ==================================================
	protected final @Override void onClick() { if(this.onClick != null) this.onClick.accept(this); }
	// --------------------------------------------------
	public final @Override void render(TDrawContext pencil)
	{
		//draw the solid background color, and then the display item
		pencil.drawTFill(this.backgroundColor);
		pencil.drawItem(this.displayItem, getX() + 3, getY() + 3);
	}
	
	public final @Override void postRender(TDrawContext pencil)
	{
		//draw an outline when the widget is hovered or focused
		if(isFocusedOrHovered())
			pencil.drawTBorder(COLOR_OUTLINE_FOCUSED);
	}
	// ==================================================
}