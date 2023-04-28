package io.github.thecsdev.betterstats.client.network;

import static io.github.thecsdev.betterstats.BetterStats.LOGGER;
import static io.github.thecsdev.betterstats.client.gui_hud.screen.BetterStatsHudScreen.HUD_ID;
import static io.github.thecsdev.betterstats.network.BetterStatsNetworkHandler.C2S_PREFS;
import static io.github.thecsdev.betterstats.network.BetterStatsNetworkHandler.S2C_REQ_PREFS;
import static io.github.thecsdev.tcdcommons.api.client.registry.TCDCommonsClientRegistry.InGameHud_Screens;

import dev.architectury.event.events.client.ClientPlayerEvent;
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
	public static boolean respondToPrefs = false;
	// ==================================================
	protected BetterStatsClientNetworkHandler() {}
	public static void init() {/*calls static*/}
	// ==================================================
	static { initNetworkReceivers(); }
	private static void initNetworkReceivers()
	{
		//by default, do not respond to S2C_REQ_PREFS
		respondToPrefs = false;
		ClientPlayerEvent.CLIENT_PLAYER_QUIT.register((cp) ->
		{
			respondToPrefs = false;
			InGameHud_Screens.remove(HUD_ID); //TODO - temporary bug fix for switching worlds/servers
		});
		//handle S2C_REQ_PREFS
		NetworkManager.registerReceiver(Side.S2C, S2C_REQ_PREFS, (payload, context) -> c2s_sendPrefs());
	}
	// ==================================================
	public static void c2s_sendPrefs()
	{
		//only send C2S_PREFS when allowed to
		if(!respondToPrefs && !MinecraftClient.getInstance().isInSingleplayer())
			return;
		
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