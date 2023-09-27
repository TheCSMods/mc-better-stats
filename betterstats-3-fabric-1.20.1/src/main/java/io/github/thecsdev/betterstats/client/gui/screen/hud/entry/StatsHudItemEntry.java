package io.github.thecsdev.betterstats.client.gui.screen.hud.entry;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.client.gui.stats.widget.CustomStatElement;
import io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget;
import io.github.thecsdev.betterstats.api.util.enumerations.ItemStatType;
import io.github.thecsdev.betterstats.api.util.stats.SUItemStat;
import io.github.thecsdev.tcdcommons.api.client.gui.TElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TWidgetHudScreen;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import net.minecraft.text.Text;

public final class StatsHudItemEntry extends TWidgetHudScreen.WidgetEntry<TElement>
{
	// ==================================================
	static final int WIDTH = 150;
	// --------------------------------------------------
	protected final SUItemStat stat;
	protected ItemStatType mode = ItemStatType.MINED;
	// ==================================================
	public StatsHudItemEntry(SUItemStat stat) throws NullPointerException
	{
		super(0.5, 0.25);
		this.stat = Objects.requireNonNull(stat);
	}
	// ==================================================
	public final @Override TElement createWidget()
	{
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
	private final CustomStatElement createCustomStatElement()
	{
		//collect info
		final int i = ItemStatWidget.SIZE;
		@Nullable Text left = this.mode.getText();
		@Nullable Text right = literal(Integer.toString(this.mode.getStatValue(this.stat)));
		//create and return
		return new CustomStatElement(i, 0, WIDTH - i, left, right);
	}
	// ==================================================
	private final class Element extends TElement
	{
		public Element()
		{
			super(0, 0, WIDTH, CustomStatElement.HEIGHT);
			addChild(new ItemStatWidget(0, 0, stat), true);
			addChild(createCustomStatElement(), true);
		}
		public @Override void render(TDrawContext pencil) { pencil.drawTFill(TPanelElement.COLOR_BACKGROUND); }
	}
	// ==================================================
}