package thecsdev.betterstats.server;

import thecsdev.betterstats.BetterStats;
import thecsdev.fabric2forge.bss_p758.fabricapi.DedicatedServerModInitializer;

public class BetterStatsServer extends BetterStats implements DedicatedServerModInitializer
{
	@Override
	public void onInitializeServer()
	{
		//nothing to see here, this is a client mod.
	}
}