package io.github.thecsdev.betterstats.client.network;

import static io.github.thecsdev.betterstats.BetterStats.LOGGER;
import static io.github.thecsdev.betterstats.BetterStatsConfig.CLIENT_NET_CONSENT;
import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.betterstats.network.BetterStatsNetworkHandler.C2S_I_HAVE_BSS;
import static io.github.thecsdev.betterstats.network.BetterStatsNetworkHandler.C2S_LIVE_STATS;
import static io.github.thecsdev.betterstats.network.BetterStatsNetworkHandler.NETWORK_VERSION;
import static io.github.thecsdev.betterstats.network.BetterStatsNetworkHandler.S2C_I_HAVE_BSS;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.client.gui.screen.hud.BetterStatsHudScreen;
import io.github.thecsdev.tcdcommons.api.events.client.MinecraftClientEvent;
import io.github.thecsdev.tcdcommons.api.network.CustomPayloadNetwork;
import io.github.thecsdev.tcdcommons.api.network.packet.TCustomPayload;
import io.github.thecsdev.tcdcommons.api.util.thread.TaskScheduler;
import io.netty.buffer.Unpooled;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketByteBuf;

public final @Internal class BetterStatsClientNetworkHandler
{
	// ==================================================
	private BetterStatsClientNetworkHandler() {}
	// --------------------------------------------------
	private static boolean serverHasBSS = false;
	// ==================================================
	public static void init() {}
	static
	{
		MinecraftClientEvent.DISCONNECTED.register(client ->
		{
			//when the client disconnects, clear all flags, including user consent
			LOGGER.info("Clearing '" + BetterStatsClientNetworkHandler.class.getSimpleName() + "' flags.");
			serverHasBSS = false;
			CLIENT_NET_CONSENT = false;
		});
		
		//initialize network handlers
		CustomPayloadNetwork.registerReceiver(NetworkSide.CLIENTBOUND, S2C_I_HAVE_BSS, ctx ->
		{
			//ignore duplicate packets
			if(serverHasBSS) return;
			
			//obtain data buffer and make sure data is present
			final var buffer = ctx.getPacketBuffer();
			if(buffer.readableBytes() == 0) return;
			
			//obtain network version and compare it
			final int netVer = buffer.readIntLE();
			if(netVer != NETWORK_VERSION) return;
			
			//server has BSS
			LOGGER.info("Server has '" + BetterStats.getModID() + "' installed.");
			serverHasBSS = true;
			
			//if the client is in single player, handle live stats updates
			//in case the statistics hud had entries in it
			if(MC_CLIENT.isInSingleplayer() || BetterStats.getInstance().getConfig().trustAllServersBssNet)
				TaskScheduler.executeOnce(MC_CLIENT, () -> MC_CLIENT.getNetworkHandler() != null, () -> c2s_iHaveBSS(true));
		});
	}
	// ==================================================
	public static boolean serverHasBSS() { return serverHasBSS; }
	// --------------------------------------------------
	/**
	 * Returns {@code true} if {@link BetterStatsClientNetworkHandler}
	 * is allowed to communicate with the server.
	 */
	public static boolean comms() { return MC_CLIENT.isInSingleplayer() || (serverHasBSS && CLIENT_NET_CONSENT); }
	// --------------------------------------------------
	public static final void c2s_iHaveBSS(boolean forceSend)
	{
		if(!forceSend && !comms()) return;
		CLIENT_NET_CONSENT = true;
		
		//construct and send
		final var data = new PacketByteBuf(Unpooled.buffer());
		data.writeIntLE(NETWORK_VERSION);
		new TCustomPayload(C2S_I_HAVE_BSS, data).sendC2S();
	}
	
	public static final void c2s_liveStats() { c2s_liveStats(BetterStatsHudScreen.getInstance().entryCount() > 0); }
	public static final void c2s_liveStats(boolean receiveLiveUpdates)
	{
		//if communications are off, don't send
		if(!comms()) return;
		
		//construct and send
		final var data = new PacketByteBuf(Unpooled.buffer());
		data.writeBoolean(receiveLiveUpdates);
		new TCustomPayload(C2S_LIVE_STATS, data).sendC2S();
	}
	// ==================================================
}