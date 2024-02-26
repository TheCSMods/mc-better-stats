package io.github.thecsdev.betterstats.api.util.enumerations;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Objects;
import java.util.function.Function;

import io.github.thecsdev.betterstats.api.util.stats.SUMobStat;
import io.github.thecsdev.tcdcommons.api.util.interfaces.ITextProvider;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

public enum MobStatType implements ITextProvider
{
	// ==================================================
	KILLED(Stats.KILLED, translatable("betterstats.stattype_phrase.minecraft.killed"), s -> s.kills),
	KILLED_BY(Stats.KILLED_BY, translatable("betterstats.stattype_phrase.minecraft.killed_by"), s -> s.deaths);
	// ==================================================
	private final StatType<?> statType;
	private final Text text;
	private final Function<SUMobStat, Integer> statValueSupplier;
	// ==================================================
	private MobStatType(StatType<?> statType, Text text, Function<SUMobStat, Integer> statValueSupplier)
	{
		this.statType = Objects.requireNonNull(statType);
		this.text = Objects.requireNonNull(text);
		this.statValueSupplier = Objects.requireNonNull(statValueSupplier);
	}
	// ==================================================
	public final StatType<?> getStatType() { return this.statType; }
	public final @Override Text getText() { return this.text; }
	public final int getStatValue(SUMobStat stat) throws NullPointerException
	{
		return this.statValueSupplier.apply(Objects.requireNonNull(stat));
	}
	// --------------------------------------------------
	public static final boolean isMobStat(Stat<?> stat)
	{
		final var statType = stat.getType();
		for(final var val : MobStatType.values())
			if(Objects.equals(statType, val.statType))
				return true;
		return false;
	}
	// ==================================================
}