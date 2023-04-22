package io.github.thecsdev.betterstats.client.network;

import static io.github.thecsdev.betterstats.BetterStats.LOGGER;
import static io.github.thecsdev.betterstats.network.BetterStatsNetworkHandler.C2S_PREFS;
import static io.github.thecsdev.betterstats.network.BetterStatsNetworkHandler.S2C_REQ_PREFS;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.Side;
import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.client.gui_hud.screen.BetterStatsHudScreen;
import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;

/**
 * Client-side network handler for {@link BetterStats}.
 */
public final class BetterStatsClientNetworkHandler
{
	// ==================================================
	protected BetterStatsClientNetworkHandler() {}
	public static void init() {/*calls static*/}
	// ==================================================
	static { initNetworkReceivers(); }
	private static void initNetworkReceivers()
	{
		NetworkManager.registerReceiver(Side.S2C, S2C_REQ_PREFS, (payload, context) -> c2s_sendPrefs());
	}
	// ==================================================
	public static void c2s_sendPrefs()
	{
		//TODO - Remove later when the feature is fully done;
		//# There are two reasons why this feature is hard-coded for single-player only for now:
		//1. Bandwidth - There is no rate limiting mechanism to prevent the server from spamming stat packets,
		//   like for example when the player is using an Eff. 5 shovel on sand, which causes packet spam.
		//2. Security  - There is no mechanism to allow the player Not to tell the server about
		//   'Better Stats' being installed on their side, which could be a privacy/security issue.
		if(!MinecraftClient.getInstance().isInSingleplayer()) return;
		
		//create prefs. packet
		var data = new PacketByteBuf(Unpooled.buffer());
		data.writeBoolean(BetterStatsHudScreen.getInstance() != null); //boolean - enabled
		var packet = new CustomPayloadC2SPacket(C2S_PREFS, data);
		//send packet
		try { MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet); }
		catch(Exception e) { LOGGER.debug("Failed to send '" + C2S_PREFS + "' packet; " + e.getMessage()); }
	}
	// ==================================================
}