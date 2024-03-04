package io.github.thecsdev.betterstats.client.gui.screen.hud.entry;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Objects;

import io.github.thecsdev.betterstats.api.client.gui.stats.widget.CustomStatElement;
import io.github.thecsdev.betterstats.api.client.util.io.LocalPlayerStatsProvider;
import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.api.util.stats.SUGeneralStat;
import io.github.thecsdev.tcdcommons.api.client.gui.TElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TWidgetHudScreen;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import net.minecraft.stat.StatType;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;

public class StatsHudGeneralEntry extends TWidgetHudScreen.WidgetEntry<TElement>
{
	// ==================================================
	static final int WIDTH = 100;
	// --------------------------------------------------
	protected IStatsProvider statsProvider;
	protected final Identifier generalStat;
	protected StatType<Identifier> mode = Stats.CUSTOM;
	// ==================================================
	public StatsHudGeneralEntry(SUGeneralStat stat) throws NullPointerException {
		this(stat.getStatsProvider(), stat.getGeneralStat().getValue());
	}
	public StatsHudGeneralEntry(IStatsProvider statsProvider, Identifier generalStat) throws NullPointerException
	{
		super(0.5, 0.25);
		this.statsProvider = Objects.requireNonNull(statsProvider);
		this.generalStat = Objects.requireNonNull(generalStat);
	}
	// ==================================================
	public final @Override TElement createWidget()
	{
		//ensure local stat providers are up-to-date
		if(this.statsProvider instanceof LocalPlayerStatsProvider)
			this.statsProvider = Objects.requireNonNull(LocalPlayerStatsProvider.getInstance());
		
		//create the element, and add the context menu to it
		if(this.mode == null) this.mode = Stats.CUSTOM;
		final var el = new Element();
		el.eContextMenu.register((__, cMenu) ->
		{
			cMenu.addButton(translatable("selectWorld.delete"), ___ -> removeEntry());
		});
		
		//return the new element
		return el;
	}
	// ==================================================
	private final class Element extends TElement
	{
		public Element()
		{
			//init super
			super(0, 0, WIDTH, CustomStatElement.HEIGHT);
			
			//obtain values and texts
			int rightVar = statsProvider.getStatValue(mode, generalStat);
			var left = (mode == Stats.CUSTOM) ?
					SUGeneralStat.getGeneralStatText(mode.getOrCreateStat(generalStat)) :
					literal("<Unsupported stat>");
			var right = literal(mode.getOrCreateStat(generalStat).format(rightVar));
			
			//update width
			this.setSize(this.width + getTextRenderer().getWidth(left), this.height);
			
			//add custom stat element child
			addChild(new CustomStatElement(0, 0, this.width, left, right));
		}
		public @Override void render(TDrawContext pencil) { pencil.drawTFill(TPanelElement.COLOR_BACKGROUND); }
	}
	// ==================================================
}