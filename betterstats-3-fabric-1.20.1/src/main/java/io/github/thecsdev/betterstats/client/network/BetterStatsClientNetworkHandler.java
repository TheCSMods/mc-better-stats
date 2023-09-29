package io.github.thecsdev.betterstats.client.network;

import static io.github.thecsdev.betterstats.BetterStatsConfig.LEGAL_NET_CONSENT;
import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.betterstats.network.BetterStatsNetworkHandler.C2S_LIVE_STATS;
import static io.github.thecsdev.betterstats.network.BetterStatsNetworkHandler.NETWORK_VERSION;
import static io.github.thecsdev.betterstats.network.BetterStatsNetworkHandler.S2C_I_HAVE_BSS;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.tcdcommons.api.events.client.MinecraftClientEvent;
import io.github.thecsdev.tcdcommons.api.network.CustomPayloadNetwork;
import io.netty.buffer.Unpooled;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;

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
		//initialize event handlers
		MinecraftClientEvent.DISCONNECTED.register(client ->
		{
			//when the client disconnects, clear all flags, including user consent
			serverHasBSS = false;
			LEGAL_NET_CONSENT = false;
		});
		
		//initialize network handlers
		CustomPayloadNetwork.registerReceiver(NetworkSide.CLIENTBOUND, S2C_I_HAVE_BSS, ctx ->
		{
			//obtain data buffer and make sure data is present
			final var buffer = ctx.getPacketBuffer();
			if(buffer.readableBytes() == 0) return;
			
			//obtain network version and compare it
			final int netVer = buffer.readIntLE();
			if(netVer != NETWORK_VERSION) return;
			
			//server has BSS
			serverHasBSS = true;
		});
	}
	// ==================================================
	/**
	 * Returns {@code true} if {@link BetterStatsClientNetworkHandler}
	 * is allowed to communicate with the server.
	 */
	public static boolean comms() { return MC_CLIENT.isInSingleplayer() || (serverHasBSS && LEGAL_NET_CONSENT); }
	// --------------------------------------------------
	public static final void c2s_liveStats(boolean receiveLiveUpdates)
	{
		//if communications are off, don't send
		if(!comms()) return;
		
		//construct and send
		final var data = new PacketByteBuf(Unpooled.buffer());
		data.writeBoolean(receiveLiveUpdates);
		MC_CLIENT.getNetworkHandler().sendPacket(new CustomPayloadS2CPacket(C2S_LIVE_STATS, data));
	}
	// ==================================================
}