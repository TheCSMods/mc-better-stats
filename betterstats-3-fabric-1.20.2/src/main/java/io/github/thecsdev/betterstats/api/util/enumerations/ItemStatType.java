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
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

public enum ItemStatType implements ITextProvider
{
	// ==================================================
	MINED(    Stats.MINED,     TEXT_STAT_MINED,     s -> s.mined),
	CRAFTED(  Stats.CRAFTED,   TEXT_STAT_CRAFTED,   s -> s.crafted),
	PICKED_UP(Stats.PICKED_UP, TEXT_STAT_PICKED_UP, s -> s.pickedUp),
	DROPPED(  Stats.DROPPED,   TEXT_STAT_DROPPED,   s -> s.dropped),
	USED(     Stats.USED,      TEXT_STAT_USED,      s -> s.used),
	BROKEN(   Stats.BROKEN,    TEXT_STAT_BROKEN,    s -> s.broken);
	// ==================================================
	private final StatType<?> statType;
	private final Text text;
	private final Function<SUItemStat, Integer> statValueSupplier;
	// ==================================================
	private ItemStatType(StatType<?> statType, Text text, Function<SUItemStat, Integer> statValueSupplier)
	{
		this.statType = Objects.requireNonNull(statType);
		this.text = Objects.requireNonNull(text);
		this.statValueSupplier = Objects.requireNonNull(statValueSupplier);
	}
	// ==================================================
	public final StatType<?> getStatType() { return this.statType; }
	public final @Override Text getText() { return this.text; }
	public final int getStatValue(SUItemStat stat) throws NullPointerException
	{
		return this.statValueSupplier.apply(Objects.requireNonNull(stat));
	}
	// --------------------------------------------------
	public static final boolean isItemStat(Stat<?> stat)
	{
		final var statType = stat.getType();
		for(final var val : ItemStatType.values())
			if(Objects.equals(statType, val.statType))
				return true;
		return false;
	}
	// ==================================================
}