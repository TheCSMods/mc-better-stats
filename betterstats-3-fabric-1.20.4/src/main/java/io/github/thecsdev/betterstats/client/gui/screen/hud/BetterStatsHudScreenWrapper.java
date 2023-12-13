package io.github.thecsdev.betterstats.client.gui.screen.hud;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreenWrapper;
import net.minecraft.client.gui.screen.StatsListener;

@Internal class BetterStatsHudScreenWrapper extends TScreenWrapper<BetterStatsHudScreen> implements StatsListener
{
	// ==================================================
	public BetterStatsHudScreenWrapper(BetterStatsHudScreen target) { super(target); }
	// --------------------------------------------------
	public final @Override void onStatsReady() { this.target.refresh(); }
	// ==================================================
}