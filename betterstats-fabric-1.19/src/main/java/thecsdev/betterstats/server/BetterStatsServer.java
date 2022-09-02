package thecsdev.betterstats.server;

import net.fabricmc.api.DedicatedServerModInitializer;
import thecsdev.betterstats.BetterStats;

public class BetterStatsServer extends BetterStats implements DedicatedServerModInitializer
{
	@Override
	public void onInitializeServer()
	{
		//nothing to see here, this is a client mod.
	}
}