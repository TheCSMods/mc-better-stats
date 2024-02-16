package io.github.thecsdev.betterstats.client.gui.screen.hud.entry;

import static io.github.thecsdev.betterstats.api.registry.BSRegistries.getEntityStatTypePhrase;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.client.gui.stats.widget.CustomStatElement;
import io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget;
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
	// --------------------------------------------------
	private final CustomStatElement createCustomStatElement(SUMobStat stat)
	{
		//prepare variables
		if(this.mode == null) this.mode = Stats.KILLED;
		@Nullable Text left = getEntityStatTypePhrase(this.mode);
		@Nullable Text right = literal(Integer.toString(this.statsProvider.getStatValue(this.mode, this.entityType)));
		
		//create and return
		return new CustomStatElement(ItemStatWidget.SIZE, 0, WIDTH - ItemStatWidget.SIZE, left, right);
	}
	// ==================================================
	private final class Element extends TElement
	{
		public Element()
		{
			super(0, 0, WIDTH, CustomStatElement.HEIGHT);
			
			final var stat = new SUMobStat(StatsHudMobEntry.this.statsProvider, StatsHudMobEntry.this.entityType);
			final var ms = new MobStatWidget(0, 0, stat);
			ms.setSize(this.height, this.height);
			
			addChild(ms, true);
			addChild(createCustomStatElement(stat), true);
		}
		public @Override void render(TDrawContext pencil) { pencil.drawTFill(TPanelElement.COLOR_BACKGROUND); }
	}
	// ==================================================
}
