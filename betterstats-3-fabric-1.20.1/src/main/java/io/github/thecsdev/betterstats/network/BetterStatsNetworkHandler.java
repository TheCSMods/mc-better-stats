package io.github.thecsdev.betterstats.network;

import java.util.WeakHashMap;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.tcdcommons.api.events.server.PlayerManagerEvent;
import io.github.thecsdev.tcdcommons.api.network.CustomPayloadNetwork;
import io.netty.buffer.Unpooled;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Represents the server-side network handler for {@link BetterStats}.
 */
public final @Internal class BetterStatsNetworkHandler
{
	// ==================================================
	private BetterStatsNetworkHandler() {}
	// --------------------------------------------------
	public static final int NETWORK_VERSION = 1;
	//
	public static final Identifier S2C_I_HAVE_BSS;
	public static final Identifier C2S_LIVE_STATS;
	// --------------------------------------------------
	public static final WeakHashMap<ServerPlayerEntity, PlayerPreferences> PlayerPrefs;
	// ==================================================
	public static void init() {}
	static
	{
		//init packet IDs
		final var modId = BetterStats.getModID();
		S2C_I_HAVE_BSS = new Identifier(modId, "s2c_bss");
		C2S_LIVE_STATS = new Identifier(modId, "c2s_live_stats");
		
		//init variables
		PlayerPrefs = new WeakHashMap<>();
		
		//init event handlers
		PlayerManagerEvent.PLAYER_CONNECTED.register(player ->
		{
			PlayerPrefs.put(player, new PlayerPreferences());
			s2c_iHaveBSS(player);
		});
		
		//init network handlers
		CustomPayloadNetwork.registerReceiver(NetworkSide.SERVERBOUND, C2S_LIVE_STATS, ctx ->
		{
			//obtain prefs
			final var prefs = PlayerPrefs.get(ctx.getPlayer());
			if(prefs == null) return; //shouldn't happen at all, but just in case
			
			//update prefs
			prefs.liveStats = ctx.getPacketBuffer().readBoolean();
		});
	}
	// ==================================================
	/**
	 * Sends the {@link #S2C_I_HAVE_BSS} packet to a given {@link ServerPlayerEntity}.
	 */
	public static void s2c_iHaveBSS(ServerPlayerEntity player)
	{
		final var data = new PacketByteBuf(Unpooled.buffer());
		data.writeIntLE(NETWORK_VERSION);
		player.networkHandler.sendPacket(new CustomPayloadS2CPacket(S2C_I_HAVE_BSS, data));
	}
	// ==================================================
}