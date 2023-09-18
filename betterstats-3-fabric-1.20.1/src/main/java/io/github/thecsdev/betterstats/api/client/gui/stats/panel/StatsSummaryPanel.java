package io.github.thecsdev.betterstats.api.client.gui.stats.panel;

import static io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget.TXT_STAT_BROKEN;
import static io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget.TXT_STAT_CRAFTED;
import static io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget.TXT_STAT_DROPPED;
import static io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget.TXT_STAT_MINED;
import static io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget.TXT_STAT_PICKED_UP;
import static io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget.TXT_STAT_USED;
import static io.github.thecsdev.betterstats.api.client.gui.stats.widget.MobStatWidget.TXT_STAT_DEATHS;
import static io.github.thecsdev.betterstats.api.client.gui.stats.widget.MobStatWidget.TXT_STAT_KILLS;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.client.gui.panel.BSComponentPanel;
import io.github.thecsdev.betterstats.api.client.gui.stats.widget.GeneralStatWidget;
import io.github.thecsdev.betterstats.api.util.stats.SUItemStat;
import io.github.thecsdev.betterstats.api.util.stats.SUMobStat;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

/**
 * A {@link BSComponentPanel} that summarizes given {@link Collection}s
 * of {@link SUItemStat}s and {@link SUMobStat}s.
 */
public final class StatsSummaryPanel extends BSComponentPanel
{
	// ==================================================
	public static final int ENTRY_HEIGHT = GeneralStatWidget.HEIGHT;
	// --------------------------------------------------
	/**
	 * {@link StatsSummaryPanel} entries.
	 * @apiNote Each entry <b>must</b> be a {@link Text} {@code array} with the length of 3.
	 */
	private final List<Text[]> entries = new ArrayList<>();
	// --------------------------------------------------
	protected int columnCount = 2;
	// ==================================================
	public StatsSummaryPanel(int x, int y, int width) { this(x, y, width, 10 + (ENTRY_HEIGHT * 3)); }
	public StatsSummaryPanel(int x, int y, int width, int height)
	{
		super(x, y, width, height);
		this.scrollFlags = TPanelElement.SCROLL_VERTICAL;
		this.scrollPadding = 5;
	}
	// ==================================================
	/**
	 * Sets the number of columns that will be used to summarize stats.
	 * @apiNote Argument is clamped to the range from 1 to 4.
	 */
	public final void setColumnCount(int columnCount)
	{
		this.columnCount = MathHelper.clamp(columnCount, 1, 5);
		if(getParentTScreen() != null) refresh();
	}
	// --------------------------------------------------
	public final void clearEntries() { this.entries.clear(); }
	public final void addEntry(@Nullable Text left, @Nullable Text right) { addEntry(left, null, right); }
	public final void addEntry(@Nullable Text left, @Nullable Text center, @Nullable Text right) { this.entries.add(new Text[] { left, center, right }); }
	// --------------------------------------------------
	/**
	 * Adds {@link Text}ual entries to this {@link StatsSummaryPanel} that
	 * summarize a collection of {@link SUItemStat}'s stats.
	 */
	public final void summarizeItemStats(Iterable<SUItemStat> itemStats)
	{
		//first clear any existing entries
		clearEntries();
		
		//define the stat integers
		long mined = 0, crafted = 0, used = 0, broken = 0, pickedUp = 0, dropped = 0;
		
		//sum them up
		for(final var itemStat : itemStats)
		{
			mined    += itemStat.mined;
			crafted  += itemStat.crafted;
			used     += itemStat.used;
			broken   += itemStat.broken;
			pickedUp += itemStat.pickedUp;
			dropped  += itemStat.dropped;
		}
		
		//add entries
		addEntry(TXT_STAT_MINED,     literal(Long.toString(mined)));
		addEntry(TXT_STAT_CRAFTED,   literal(Long.toString(crafted)));
		addEntry(TXT_STAT_USED,      literal(Long.toString(used)));
		addEntry(TXT_STAT_BROKEN,    literal(Long.toString(broken)));
		addEntry(TXT_STAT_PICKED_UP, literal(Long.toString(pickedUp)));
		addEntry(TXT_STAT_DROPPED,   literal(Long.toString(dropped)));
		
		//refresh if needed
		if(getParentTScreen() != null) refresh();
	}
	
	/**
	 * Adds {@link Text}ual entries to this {@link StatsSummaryPanel} that
	 * summarize a collection of {@link SUMobStat}'s stats.
	 */
	public final void summarizeMobStats(Iterable<SUMobStat> mobStats)
	{
		//first clear any existing entries
		clearEntries();
		
		//define the stat integers
		long kills = 0, deaths = 0;
		
		//sum them up
		for(final var mobStat : mobStats)
		{
			kills  += mobStat.kills;
			deaths += mobStat.deaths;
		}
		
		//add entries
		addEntry(TXT_STAT_KILLS,  literal(Long.toString(kills)));
		addEntry(TXT_STAT_DEATHS, literal(Long.toString(deaths)));
		
		//refresh if needed
		if(getParentTScreen() != null) refresh();
	}
	// ==================================================
	protected final @Override void init()
	{
		//calculate entry width and height
		final int sp = getScrollPadding();
		final int entryWidth = (getWidth() - (sp * 2)) / Math.max(this.columnCount, 1);
		
		//define the current "row/column" "cursor" position, starting from 0
		int row = 0, column = 0;
		
		//iterate all entries
		for(final Text[] entry : this.entries)
		{
			//calculate entry X and Y
			final int eX = sp + (column * entryWidth);
			final int eY = sp + (row * ENTRY_HEIGHT);
			
			//create entry labels
			int ha = 0; //keeps track of horizontal alignment
			for(final Text entryText : entry)
			{
				//keep track of horizontal alignment, and skip null texts
				ha++;
				if(entryText == null) continue;
				
				//create the label and assign its corresponding horizontal alignment
				final var lbl = new TLabelElement(eX, eY, entryWidth, ENTRY_HEIGHT, entryText);
				switch(ha)
				{
					case 1: lbl.setTextHorizontalAlignment(HorizontalAlignment.LEFT); break;
					case 2: lbl.setTextHorizontalAlignment(HorizontalAlignment.CENTER); break;
					case 3: lbl.setTextHorizontalAlignment(HorizontalAlignment.RIGHT); break;
					default: break;
				}
				
				//add the label
				addChild(lbl, true);
			}
			
			//increment row and column
			column++;
			if(column >= this.columnCount) { column = 0; row++; }
		}
	}
	// --------------------------------------------------
	/**
	 * Resizes this {@link StatsSummaryPanel} to fit the children labels on the Y axis.
	 */
	public final void autoHeight()
	{
		//old implementation that only works when initialized
		/*if(getChildren().size() < 1) return;
		final int startY = getY();
		final int endY = getChildren().getLastChild().getEndY();
		final int height = (endY - startY) + getScrollPadding();
		setSize(getWidth(), height);*/
		
		//new implementation that works even when not initialized
		final int rows = (int) Math.ceil(((double)this.entries.size() / Math.max(this.columnCount, 1)));
		final int height = (rows * ENTRY_HEIGHT) + (getScrollPadding() * 2);
		setSize(getWidth(), height);
	}
	// ==================================================
}