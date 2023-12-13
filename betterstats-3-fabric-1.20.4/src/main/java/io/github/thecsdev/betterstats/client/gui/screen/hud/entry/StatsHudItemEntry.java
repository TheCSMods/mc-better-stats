package io.github.thecsdev.betterstats.client.gui.screen.hud.entry;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.client.gui.stats.widget.CustomStatElement;
import io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget;
import io.github.thecsdev.betterstats.api.client.util.io.LocalPlayerStatsProvider;
import io.github.thecsdev.betterstats.api.util.enumerations.ItemStatType;
import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.api.util.stats.SUItemStat;
import io.github.thecsdev.tcdcommons.api.client.gui.TElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TWidgetHudScreen;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import net.minecraft.item.Item;
import net.minecraft.text.Text;

public final class StatsHudItemEntry extends TWidgetHudScreen.WidgetEntry<TElement>
{
	// ==================================================
	static final int WIDTH = 150;
	// --------------------------------------------------
	protected IStatsProvider statsProvider;
	protected final Item item;
	protected ItemStatType mode = ItemStatType.MINED;
	// ==================================================
	public StatsHudItemEntry(SUItemStat stat) throws NullPointerException { this(stat.getStatsProvider(), stat.getItem()); }
	public StatsHudItemEntry(IStatsProvider statsProvider, Item item) throws NullPointerException
	{
		super(0.5, 0.25);
		this.statsProvider = Objects.requireNonNull(statsProvider);
		this.item = Objects.requireNonNull(item);
	}
	// ==================================================
	public final @Override TElement createWidget()
	{
		//ensure local stat providers are up-to-date
		if(this.statsProvider instanceof LocalPlayerStatsProvider)
			this.statsProvider = Objects.requireNonNull(LocalPlayerStatsProvider.getInstance());
		
		//create and return element
		final var el = new Element();
		el.eContextMenu.register((__, cMenu) ->
		{
			for(final var ist : ItemStatType.values())
			{
				cMenu.addButton(ist.getText(), ___ ->
				{
					this.mode = ist;
					refreshEntry();
				});
			}
			cMenu.addSeparator();
			cMenu.addButton(translatable("selectWorld.delete"), ___ -> removeEntry());
		});
		return el;
	}
	// --------------------------------------------------
	private final CustomStatElement createCustomStatElement(SUItemStat stat)
	{
		//collect info
		final int i = ItemStatWidget.SIZE;
		@Nullable Text left = this.mode.getText();
		@Nullable Text right = literal(Integer.toString(this.mode.getStatValue(stat)));
		//create and return
		return new CustomStatElement(i, 0, WIDTH - i, left, right);
	}
	// ==================================================
	private final class Element extends TElement
	{
		public Element()
		{
			super(0, 0, WIDTH, CustomStatElement.HEIGHT);
			final var stat = new SUItemStat(StatsHudItemEntry.this.statsProvider, StatsHudItemEntry.this.item);
			addChild(new ItemStatWidget(0, 0, stat), true);
			addChild(createCustomStatElement(stat), true);
		}
		public @Override void render(TDrawContext pencil) { pencil.drawTFill(TPanelElement.COLOR_BACKGROUND); }
	}
	// ==================================================
}