package io.github.thecsdev.betterstats.api.util.enumerations;

import static io.github.thecsdev.betterstats.api.client.gui.stats.widget.MobStatWidget.TEXT_STAT_DEATHS;
import static io.github.thecsdev.betterstats.api.client.gui.stats.widget.MobStatWidget.TEXT_STAT_KILLS;

import java.util.Objects;
import java.util.function.Function;

import io.github.thecsdev.betterstats.api.util.stats.SUMobStat;
import io.github.thecsdev.tcdcommons.api.util.interfaces.ITextProvider;
import net.minecraft.text.Text;

public enum MobStatType implements ITextProvider
{
	// ==================================================
	KILLED(TEXT_STAT_KILLS, s -> s.kills),
	KILLED_BY(TEXT_STAT_DEATHS, s -> s.deaths);
	// ==================================================
	private final Text text;
	private final Function<SUMobStat, Integer> statValueSupplier;
	// ==================================================
	private MobStatType(Text text, Function<SUMobStat, Integer> statValueSupplier)
	{
		this.text = Objects.requireNonNull(text);
		this.statValueSupplier = Objects.requireNonNull(statValueSupplier);
	}
	// ==================================================
	public final @Override Text getText() { return this.text; }
	public int getStatValue(SUMobStat stat) throws NullPointerException
	{
		return this.statValueSupplier.apply(Objects.requireNonNull(stat));
	}
	// ==================================================
}