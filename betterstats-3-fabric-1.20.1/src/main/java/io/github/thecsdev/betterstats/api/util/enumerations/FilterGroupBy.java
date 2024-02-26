package io.github.thecsdev.betterstats.api.util.enumerations;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Objects;

import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab.FiltersInitContext;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.util.interfaces.ITextProvider;
import net.minecraft.text.Text;

/**
 * A statistics filter {@link Enum} that dictates how statistics entries are to be grouped.
 * 
 * @see StatsTab
 * @see StatsTab#initFilters(FiltersInitContext)
 */
public enum FilterGroupBy implements ITextProvider
{
	/**
	 * The default grouping method, decided by the {@link StatsTab}.
	 */
	DEFAULT(BST.filter_groupBy_default()),
	
	/**
	 * Group all statistics entries into one single group.
	 */
	ALL(translatable("gui.all")),
	
	/**
	 * Group statistics entries based on the mod the entry's item belongs to.
	 */
	MOD(BST.filter_groupBy_mod());
	
	private final Text text;
	private FilterGroupBy(Text text) { this.text = Objects.requireNonNull(text); }
	public final @Override Text getText() { return this.text; }
}