package thecsdev.betterstats.client;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import thecsdev.betterstats.BetterStats;
import thecsdev.fabric2forge.bss_p758.fabricapi.ClientModInitializer;

public final class BetterStatsClient extends BetterStats implements ClientModInitializer
{
	public static Minecraft MCClient;
	
	@Override
	public void onInitializeClient() { MCClient = Minecraft.getInstance(); }
	public static void beepItem() { MCClient.player.playSound(SoundEvents.NOTE_BLOCK_HARP, 2, 2); }
	
	//handle sending player chat messages depending on the game version
	public static void sendChat(String msg) { MCClient.player.chat(msg); }
}