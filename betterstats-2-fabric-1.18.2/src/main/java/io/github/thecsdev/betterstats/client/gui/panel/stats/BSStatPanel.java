package io.github.thecsdev.betterstats.client.gui.panel.stats;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.awt.Color;
import java.util.function.Predicate;

import io.github.thecsdev.betterstats.client.gui.panel.BSPanel;
import io.github.thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.betterstats.client.gui.screen.BetterStatsScreen.GroupStatsBy;
import io.github.thecsdev.betterstats.client.gui.widget.BSScrollBarWidget;
import io.github.thecsdev.betterstats.util.StatUtils.StatUtilsStat;
import io.github.thecsdev.tcdcommons.api.client.gui.TElement;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.util.FocusOrigin;
import io.github.thecsdev.tcdcommons.api.client.gui.util.GuiUtils;
import io.github.thecsdev.tcdcommons.api.client.gui.util.HorizontalAlignment;
import io.github.thecsdev.tcdcommons.api.util.TextUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.stat.StatHandler;
import net.minecraft.text.Text;

public abstract class BSStatPanel extends BSPanel
{
	// ==================================================
	public static final int
			COLOR_NORMAL_HOVERED = 1358954495,
			COLOR_NORMAL_FOCUSED = -5570561,
			COLOR_GOLD_FOCUSED = Color.YELLOW.getRGB();
	// --------------------------------------------------
	protected final BSScrollBarWidget scroll_this;
	// ==================================================
	public BSStatPanel(TPanelElement parentToFill)
	{
		this(parentToFill.getTpeX(), parentToFill.getTpeY(), parentToFill.getTpeWidth(), parentToFill.getTpeHeight());
		parentToFill.addTChild(this, false);
	}
	
	public BSStatPanel(int x, int y, int width, int height)
	{
		super(x, y, width - 8, height);
		this.scroll_this = new BSScrollBarWidget(getTpeEndX(), getTpeY(), 8, getTpeHeight(), this);
		setScrollPadding(10);
		setSmoothScroll(true);
	}
	// ==================================================
	public final BSScrollBarWidget getVerticalScrollBar() { return this.scroll_this; }
	// ==================================================
	@Override
	public void setPosition(int x, int y, int flags)
	{
		super.setPosition(x, y, flags);
		//move the slider alongside this element when this element is moved
		this.scroll_this.setPosition(getTpeEndX(), getTpeY(), false);
	}
	// --------------------------------------------------
	@Override
	public void onParentChanged()
	{
		super.onParentChanged();
		//remove the slider from the previous parent
		if(this.scroll_this.getTParent() != null && this.scroll_this.getTParent() != getTParent())
			this.scroll_this.getTParent().removeTChild(this.scroll_this);
		//assign the slider to the new parent
		if(getTParent() != null)
		{
			getTParent().addTChild(this.scroll_this, false);
			this.scroll_this.refreshKnobSize();
		}
	}
	// ==================================================
	public int getStatOutlineColor() { return 1; }
	
	/**
	 * Returns the current {@link BetterStatsScreen#filter_groupBy}.
	 */
	public GroupStatsBy getFilterGroupBy()
	{
		if(!(this.screen instanceof BetterStatsScreen))
			return GroupStatsBy.Default;
		else return ((BetterStatsScreen)this.screen).filter_groupBy;
	}
	// ==================================================
	/**
	 * Use this to create and add all of the statistics
	 * related {@link TElement}s onto this {@link BSStatPanel}.
	 * @param statHandler The {@link StatHandler} containing all the stats.
	 * @param statFilter The {@link Predicate} that filters out certain stats.
	 */
	public abstract void init(StatHandler statHandler, Predicate<StatUtilsStat> statFilter);
	// --------------------------------------------------
	protected TLabelElement init_groupLabel(Text groupName)
	{
		//calculate initial XY
		int x = getTpeX() + getScrollPadding();
		int y = getTpeY() + getScrollPadding();
		
		//obtain the last child and calculate
		//the next Y if there is a last child
		var lastChild = getLastTChild(false);
		if(lastChild != null) y = lastChild.getTpeEndY() + 10;
		
		//obtain group name text and it's width 
		if(groupName == null) groupName = TextUtils.literal("*");
		int lblW = getTpeWidth() - (getScrollPadding() * 2) /*getTextRenderer().getWidth(groupName) + 15*/;
		
		//create and add the label
		TLabelElement label = new TLabelElement(x, y, lblW, 20);
		label.setText(groupName);
		label.setColor(Color.yellow.getRGB(), Color.orange.getRGB());
		addTChild(label, false);
		return label;
	}
	
	/**
	 * When there are no stats to show, call this to show the
	 * label indicating that there are no stats to show yet.
	 */
	protected TLabelElement init_noResults()
	{
		int sp = getScrollPadding();
		var lbl = new TLabelElement(sp, sp, getTpeWidth() - (sp*2), getTpeHeight() - (sp*2));
		lbl.setHorizontalAlignment(HorizontalAlignment.CENTER);
		lbl.setText(translatable("betterstats.gui.no_stats_yet"));
		addTChild(lbl);
		return lbl;
	}
	// ==================================================
	/**
	 * Returns the stat predicate that this {@link BSStatPanel} wishes
	 * to append to the {@link BetterStatsScreen#getStatPredicate()}.<br/>
	 * <br/>
	 * Use {@link Predicate#and(Predicate)} to merge two predicates.
	 */
	public Predicate<StatUtilsStat> getStatPredicate() { return stat -> true; }
	// ==================================================
	//do not render any backgrounds or anything
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float deltaTime)
	{
		renderSmoothScroll(deltaTime);
	}
	
	@Override
	public void postRender(MatrixStack matrices, int mouseX, int mouseY, float deltaTime) {}
	// ==================================================
	/**
	 * A {@link BetterStatsScreen} {@link BSStatPanel} widget.<br/>
	 * This widget is meant to display a certain statistic.
	 */
	protected abstract class BSStatWidget extends TElement
	{
		// ----------------------------------------------
		public BSStatWidget(int x, int y, int width, int height) { super(x, y, width, height); }
		public @Override boolean canChangeFocus(FocusOrigin focusOrigin, boolean gainingFocus) { return true; }
		public abstract void updateTooltip();
		// ----------------------------------------------
		protected void renderBackground(MatrixStack matrices, int mouseX, int mouseY, float deltaTime)
		{
			fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height,
					GuiUtils.applyAlpha(1342177280, getAlpha()));
		}
		// ----------------------------------------------
		public @Override void render(MatrixStack matrices, int mouseX, int mouseY, float deltaTime)
		{
			renderBackground(matrices, mouseX, mouseY, deltaTime);
		}
		
		public @Override void postRender(MatrixStack matrices, int mouseX, int mouseY, float deltaTime)
		{
			//focus first
			if(isFocused()) drawOutline(matrices, COLOR_NORMAL_FOCUSED);
			//then hover
			else if(isHovered()) drawOutline(matrices, COLOR_NORMAL_HOVERED);
		}
		// ----------------------------------------------
		public @Override boolean mousePressed(int mouseX, int mouseY, int button)
		{
			//handle context menu
			if(button == 1) { showContextMenu(mouseX, mouseY); return true; }
			
			//handle not focused
			if(!isFocused() || this.screen == null)
				//accept click so the screen will ask for focus
				return true;
			
			//remove focus when clicked while focused
			this.screen.setFocusedTChild(null);
			
			//return false to reject the next focus attempt
			return false;
		}
		// ----------------------------------------------
	}
	// ==================================================
}