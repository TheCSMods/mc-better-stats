package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;
import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import net.minecraft.text.Text;

/**
 * A {@link StatsTab} that displays information about advancements.
 */
public final @Internal class AdvancementsTab extends StatsTab
{
	// ==================================================
	public final @Override Text getName() { return translatable("gui.advancements"); }
	// ==================================================
	public final @Override boolean isAvailable() { return false; } //FIXME - Remove later
	public final @Override void initStats(StatsInitContext initContext)
	{
		//FIXME - Implement
	}
	// ==================================================
	
}