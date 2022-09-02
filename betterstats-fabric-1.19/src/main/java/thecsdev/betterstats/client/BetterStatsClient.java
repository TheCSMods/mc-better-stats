package thecsdev.betterstats.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import thecsdev.betterstats.BetterStats;

public final class BetterStatsClient extends BetterStats implements ClientModInitializer
{
	public static MinecraftClient MCClient;
	
	@Override
	public void onInitializeClient()
	{
		MCClient = MinecraftClient.getInstance();
	}
}