package io.github.thecsdev.betterstats.api.util.enumerations;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

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
	MINED(Stats.MINED, translatable("stat_type.minecraft.mined"), s -> s.mined),
	CRAFTED(Stats.CRAFTED, translatable("stat_type.minecraft.crafted"), s -> s.crafted),
	PICKED_UP(Stats.PICKED_UP, translatable("stat_type.minecraft.picked_up"), s -> s.pickedUp),
	DROPPED(Stats.DROPPED, translatable("stat_type.minecraft.dropped"), s -> s.dropped),
	USED(Stats.USED, translatable("stat_type.minecraft.used"), s -> s.used),
	BROKEN(Stats.BROKEN, translatable("stat_type.minecraft.broken"), s -> s.broken);
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