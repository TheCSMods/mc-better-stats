package io.github.thecsdev.betterstats.client.gui.screen.hud.entry;

import static io.github.thecsdev.betterstats.api.registry.BSRegistries.getEntityStatTypePhrase;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Objects;

import io.github.thecsdev.betterstats.api.client.gui.stats.widget.CustomStatElement;
import io.github.thecsdev.betterstats.api.client.gui.stats.widget.MobStatWidget;
import io.github.thecsdev.betterstats.api.client.util.io.LocalPlayerStatsProvider;
import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.api.util.stats.SUMobStat;
import io.github.thecsdev.tcdcommons.api.client.gui.TElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TWidgetHudScreen;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.stat.StatType;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

public final class StatsHudMobEntry extends TWidgetHudScreen.WidgetEntry<TElement>
{
	// ==================================================
	static final int WIDTH = StatsHudItemEntry.WIDTH;
	// --------------------------------------------------
	protected IStatsProvider statsProvider;
	protected final EntityType<?> entityType;
	protected StatType<EntityType<?>> mode = Stats.KILLED;
	// ==================================================
	public StatsHudMobEntry(SUMobStat stat) throws NullPointerException { this(stat.getStatsProvider(), stat.getEntityType()); }
	public StatsHudMobEntry(IStatsProvider statsProvider, EntityType<?> entityType) throws NullPointerException
	{
		super(0.5, 0.25);
		this.statsProvider = Objects.requireNonNull(statsProvider);
		this.entityType = Objects.requireNonNull(entityType);
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
			//entity stat type entries
			for(final var statType : Registries.STAT_TYPE)
			{
				//check the stat type and if it's compatible
				if(statType.getRegistry() != Registries.ENTITY_TYPE)
					continue;
				final @SuppressWarnings("unchecked") var statTypeE = (StatType<EntityType<?>>)statType;
				
				//create a button that will switch to the given stat type, and refresh
				cMenu.addButton(getEntityStatTypePhrase(statTypeE), ___ ->
				{
					this.mode = statTypeE;
					refreshEntry();
				});
			}
			cMenu.addSeparator();
			cMenu.addButton(translatable("selectWorld.delete"), ___ -> removeEntry());
		});
		return el;
	}
	// ==================================================
	private final class Element extends TElement
	{
		public Element()
		{
			//super
			super(0, 0, WIDTH, CustomStatElement.HEIGHT);
			
			//mob stat widget
			final var stat = new SUMobStat(StatsHudMobEntry.this.statsProvider, StatsHudMobEntry.this.entityType);
			final var ms = new MobStatWidget(0, 0, stat);
			ms.setSize(this.height, this.height);
			addChild(ms, true);
			
			//custom stat element
			{
				//prepare variables
				if(StatsHudMobEntry.this.mode == null) StatsHudMobEntry.this.mode = Stats.KILLED;
				final Text left = getEntityStatTypePhrase(StatsHudMobEntry.this.mode);
				final Text right = literal(Integer.toString(stat.getStatsProvider().getStatValue(
						StatsHudMobEntry.this.mode,
						StatsHudMobEntry.this.entityType
					)));
				
				//update width
				this.setSize(this.width + getTextRenderer().getWidth(left), this.height);
				
				//create and return
				final var cse = new CustomStatElement(ms.getWidth(), 0, this.width - ms.getWidth(), left, right);
				addChild(cse, true);
			}
		}
		public @Override void render(TDrawContext pencil) { pencil.drawTFill(TPanelElement.COLOR_BACKGROUND); }
	}
	// ==================================================
}
