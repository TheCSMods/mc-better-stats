package io.github.thecsdev.betterstats.api.util.enumerations;

import static io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget.TEXT_STAT_BROKEN;
import static io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget.TEXT_STAT_CRAFTED;
import static io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget.TEXT_STAT_DROPPED;
import static io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget.TEXT_STAT_MINED;
import static io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget.TEXT_STAT_PICKED_UP;
import static io.github.thecsdev.betterstats.api.client.gui.stats.widget.ItemStatWidget.TEXT_STAT_USED;

import java.util.Objects;
import java.util.function.Function;

import io.github.thecsdev.betterstats.api.util.stats.SUItemStat;
import io.github.thecsdev.tcdcommons.api.util.interfaces.ITextProvider;
import net.minecraft.text.Text;

public enum ItemStatType implements ITextProvider
{
	// ==================================================
	MINED(TEXT_STAT_MINED, s -> s.mined),
	CRAFTED(TEXT_STAT_CRAFTED, s -> s.crafted),
	PICKED_UP(TEXT_STAT_PICKED_UP, s -> s.pickedUp),
	DROPPED(TEXT_STAT_DROPPED, s -> s.dropped),
	USED(TEXT_STAT_USED, s -> s.used),
	BROKEN(TEXT_STAT_BROKEN, s -> s.broken);
	// ==================================================
	private final Text text;
	private final Function<SUItemStat, Integer> statValueSupplier;
	// ==================================================
	private ItemStatType(Text text, Function<SUItemStat, Integer> statValueSupplier)
	{
		this.text = Objects.requireNonNull(text);
		this.statValueSupplier = Objects.requireNonNull(statValueSupplier);
	}
	// ==================================================
	public final @Override Text getText() { return this.text; }
	public int getStatValue(SUItemStat stat) throws NullPointerException
	{
		return this.statValueSupplier.apply(Objects.requireNonNull(stat));
	}
	// ==================================================
}