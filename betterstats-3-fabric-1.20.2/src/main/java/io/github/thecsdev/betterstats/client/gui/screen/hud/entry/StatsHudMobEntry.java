package io.github.thecsdev.betterstats.client.gui.screen.hud.entry;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.client.gui.stats.widget.CustomStatElement;
import io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget;
import io.github.thecsdev.betterstats.api.client.gui.stats.widget.MobStatWidget;
import io.github.thecsdev.betterstats.api.util.enumerations.MobStatType;
import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.api.util.stats.SUMobStat;
import io.github.thecsdev.tcdcommons.api.client.gui.TElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TWidgetHudScreen;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;

public final class StatsHudMobEntry extends TWidgetHudScreen.WidgetEntry<TElement>
{
	// ==================================================
	static final int WIDTH = StatsHudItemEntry.WIDTH;
	// --------------------------------------------------
	protected final IStatsProvider statsProvider;
	protected final EntityType<?> entityType;
	protected MobStatType mode = MobStatType.KILLED;
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
		final var el = new Element();
		el.eContextMenu.register((__, cMenu) ->
		{
			for(final var ist : MobStatType.values())
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
	private final CustomStatElement createCustomStatElement(SUMobStat stat)
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
