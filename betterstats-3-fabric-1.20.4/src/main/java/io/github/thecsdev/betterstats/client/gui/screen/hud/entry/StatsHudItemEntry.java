package io.github.thecsdev.betterstats.client.gui.screen.hud.entry;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.client.gui.stats.widget.CustomStatElement;
import io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget;
import io.github.thecsdev.betterstats.api.client.util.io.LocalPlayerStatsProvider;
import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.api.util.stats.SUItemStat;
import io.github.thecsdev.tcdcommons.api.client.gui.TElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TWidgetHudScreen;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.stat.StatType;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

public final class StatsHudItemEntry extends TWidgetHudScreen.WidgetEntry<TElement>
{
	// ==================================================
	static final int WIDTH = 150;
	// --------------------------------------------------
	protected IStatsProvider statsProvider;
	protected final Item item;
	protected final @Nullable Block block;
	protected StatType<?> mode = Stats.USED;
	// ==================================================
	public StatsHudItemEntry(SUItemStat stat) throws NullPointerException { this(stat.getStatsProvider(), stat.getItem()); }
	public StatsHudItemEntry(IStatsProvider statsProvider, Item item) throws NullPointerException
	{
		super(0.5, 0.25);
		this.statsProvider = Objects.requireNonNull(statsProvider);
		this.item = Objects.requireNonNull(item);
		final var block = Block.getBlockFromItem(item);
		this.block = (block != Blocks.AIR) ? block : null;
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
			//item and block stat type entries
			for(final var statType : Registries.STAT_TYPE)
			{
				//check the stat type and if it's compatible
				final var isItem = (statType.getRegistry() == Registries.ITEM);
				final var isBlock = (statType.getRegistry() == Registries.BLOCK);
				if((!isItem && !isBlock) || (isBlock && this.block == null)) continue;
				
				//create a button that will switch to the given stat type, and refresh
				cMenu.addButton(statType.getName(), ___ ->
				{
					this.mode = statType;
					refreshEntry();
				});
			}
			
			//remove entry button
			cMenu.addSeparator();
			cMenu.addButton(translatable("selectWorld.delete"), ___ -> removeEntry());
		});
		return el;
	}
	// --------------------------------------------------
	private final CustomStatElement createCustomStatElement(SUItemStat stat)
	{
		//mode info
		if(this.mode == null) this.mode = Stats.USED;
		final var isItem = (this.mode.getRegistry() == Registries.ITEM);
		final var isBlock = (this.mode.getRegistry() == Registries.BLOCK);
		
		//collect info
		final Object valObj = (isBlock) ? this.block : (isItem ? this.item : null);
		@SuppressWarnings("unchecked")
		final var val = (valObj != null) ?
			this.statsProvider.getStatValue((StatType<Object>)this.mode, valObj) :
			-1; //-1 indicating an error, and this generally shouldn't happen
		
		//create and return
		@Nullable Text left = this.mode.getName();
		@Nullable Text right = literal(Integer.toString(val));
		return new CustomStatElement(ItemStatWidget.SIZE, 0, WIDTH - ItemStatWidget.SIZE, left, right);
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