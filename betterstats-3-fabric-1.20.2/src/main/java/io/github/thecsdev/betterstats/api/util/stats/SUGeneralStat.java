package io.github.thecsdev.betterstats.api.util.stats;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.fLiteral;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.fTranslatable;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.registry.Registries;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class SUGeneralStat extends SUStat<Identifier>
{
	// ==================================================
	private final Stat<Identifier> stat;
	private final boolean isEmpty; //cached value to avoid re-calculations
	
	/**
	 * The raw {@link Integer} value of this {@link Stat}.
	 */
	public final int value;
	
	/**
	 * The formatted {@link Text}ual user-friendly version of this {@link Stat}'s {@link #value}.
	 * @see Stat#format(int)
	 */
	public final Text valueText;
	// ==================================================
	public SUGeneralStat(IStatsProvider statsProvider, Stat<Identifier> stat)
	{
		super(statsProvider, id(stat), getGeneralStatText(stat));
		this.stat = Objects.requireNonNull(stat);
		this.value = statsProvider.getStatValue(stat);
		this.valueText = fLiteral(stat.format(this.value));
		this.isEmpty = this.value == 0;
	}
	// --------------------------------------------------
	private static final @Internal Identifier id(Stat<Identifier> stat)
	{
		//not the intended way of dealing with this, but has to be done to deal with incompatibilities,
		//and by incompatibilities i mean mod devs. not registering their stats because... idk either
		return Optional.ofNullable(Registries.CUSTOM_STAT.get(stat.getValue())).orElse(ID_NULL);
	}
	// ==================================================
	/**
	 * Returns the "general" {@link Stat} that corresponds with this {@link SUGeneralStat}.
	 */
	public final Stat<Identifier> getGeneralStat() { return this.stat; }
	// --------------------------------------------------
	public final @Override boolean isEmpty() { return this.isEmpty; }
	// ==================================================
	/**
	 * Returns the translation key for a given {@link Stat}.
	 * @param stat The statistic in question.
	 */
	public static String getGeneralStatTranslationKey(Stat<Identifier> stat)
	{
		//note: throw NullPointerException if stat is null
		return "stat." + stat.getValue().toString().replace(':', '.');
	}
	// --------------------------------------------------
	/**
	 * Returns the {@link Text} that should correspond to a
	 * given general {@link SUGeneralStat}, using {@link #getGeneralStatTranslationKey(Stat)}.
	 */
	public static Text getGeneralStatText(Stat<Identifier> stat) { return fTranslatable(getGeneralStatTranslationKey(stat)); }
	// ==================================================
	/**
	 * Obtains a list of all "general" {@link Stat}s in form of {@link SUGeneralStat}.
	 * @param statsProvider The {@link IStatsProvider}.
	 * @param filter Optional. A {@link Predicate} used to filter out any unwanted {@link SUGeneralStat}s.
	 */
	public static List<SUGeneralStat> getGeneralStats
	(IStatsProvider statsProvider, @Nullable Predicate<SUGeneralStat> filter)
	{
		//null checks
		Objects.requireNonNull(statsProvider);
		
		//create an array list
		final var result = new ArrayList<SUGeneralStat>();
		
		//obtain stats
		final var statsList = new ObjectArrayList<Stat<Identifier>>(Stats.CUSTOM.iterator());
		statsList.sort(Comparator.comparing(stat -> translatable(getGeneralStatTranslationKey(stat)).getString()));
		
		for(final var stat : statsList) result.add(new SUGeneralStat(statsProvider, stat));
		//filter out stats with NULL IDs and stats the filter filters out
		result.removeIf(stat -> Objects.equals(ID_NULL, stat.getStatID()) || (filter != null && !filter.test(stat)));
		
		//return the result list
		return result;
	}
	// ==================================================
}