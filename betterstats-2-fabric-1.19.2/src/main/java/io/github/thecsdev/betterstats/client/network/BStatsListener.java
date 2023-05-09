package io.github.thecsdev.betterstats.client.network;

import com.mojang.authlib.GameProfile;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.network.BSNetworkProfile;
import net.minecraft.client.gui.screen.StatsListener;

/**
 * {@link BetterStats}'s version of {@link StatsListener}.
 */
public interface BStatsListener
{
	/**
	 * Returns the {@link GameProfile} this
	 * {@link BStatsListener} is listening for.
	 */
	public abstract GameProfile getListenerTargetGameProfile();
	
	/**
	 * Called by {@link BetterStatsClientNetworkHandler}
	 * when the stats for the given {@link #getListenerTargetGameProfile()} arrive.
	 * @param profile The {@link BSNetworkProfile} containing the profile info and stats.
	 */
	public abstract void onStatsReady(BSNetworkProfile profile);
}