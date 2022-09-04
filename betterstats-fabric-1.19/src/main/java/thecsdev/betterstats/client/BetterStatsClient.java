package thecsdev.betterstats.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import thecsdev.betterstats.BetterStats;

public final class BetterStatsClient extends BetterStats implements ClientModInitializer
{
	public static MinecraftClient MCClient;
	
	@Override
	public void onInitializeClient() { MCClient = MinecraftClient.getInstance(); }
	public static void beepItem() { MCClient.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP, 2, 2); }
}