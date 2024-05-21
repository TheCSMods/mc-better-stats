package io.github.thecsdev.betterstats.client.network;

import static io.github.thecsdev.betterstats.BetterStats.getModID;
import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.betterstats.network.BetterStatsNetwork.C2S_I_HAVE_BSS;
import static io.github.thecsdev.betterstats.network.BetterStatsNetwork.C2S_LIVE_STATS;
import static io.github.thecsdev.betterstats.network.BetterStatsNetwork.NETWORK_VERSION;

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.client.gui.screen.hud.BetterStatsHudScreen;
import io.github.thecsdev.tcdcommons.api.hooks.entity.EntityHooks;
import io.github.thecsdev.tcdcommons.api.network.CustomPayloadNetwork;
import io.github.thecsdev.tcdcommons.api.network.CustomPayloadNetworkReceiver.PacketContext;
import io.github.thecsdev.tcdcommons.api.util.thread.TaskScheduler;
import io.netty.buffer.Unpooled;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * Better statistics screen client play network handler.<br/>
 * Keeps track of {@link ClientPlayerEntity} data that is related to {@link BetterStats}.
 */
public final @Internal class BetterStatsClientPlayNetworkHandler
{
	// ==================================================
	public static final Identifier CUSTOM_DATA_ID = new Identifier(getModID(), "client_play_network_handler");
	// --------------------------------------------------
	public boolean serverHasBss      = false;
	public boolean bssNetworkConsent = false;
	public boolean enableLiveStats   = false;
	// ==================================================
	private final ClientPlayerEntity player;
	// ==================================================
	private BetterStatsClientPlayNetworkHandler(ClientPlayerEntity player) throws NullPointerException
	{
		this.player = Objects.requireNonNull(player);
	}
	// --------------------------------------------------
	public final ClientPlayerEntity getPlayer() { return this.player; }
	// ==================================================
	public final void onDisconnected()
	{
		this.serverHasBss      = false;
		this.enableLiveStats   = false;
		this.bssNetworkConsent = false;
	}
	
	public final void onIHaveBss(PacketContext ctx)
	{
		//ignore duplicate packets
		if(this.serverHasBss) return;
		
		//obtain data buffer and make sure data is present
		final var buffer = ctx.getPacketBuffer();
		if(buffer.readableBytes() == 0) return;
		
		//obtain network version and compare it
		final int netVer = buffer.readIntLE();
		if(netVer != NETWORK_VERSION) return; //ignore incompatible servers
		
		//server has BSS
		this.serverHasBss = true;
		
		//if the client is in single player, handle live stats updates
		//in case the statistics hud had entries in it
		if(MC_CLIENT.isInSingleplayer() || BetterStats.getInstance().getConfig().trustAllServersBssNet)
			TaskScheduler.executeOnce(MC_CLIENT, () -> this.player.networkHandler != null, () -> sendIHaveBss(true));
	}
	// --------------------------------------------------
	/**
	 * Returns {@code true} if {@link BetterStatsClientPlayNetworkHandler}
	 * is allowed to communicate with the server.
	 */
	public final boolean comms() { return MC_CLIENT.isInSingleplayer() || (this.serverHasBss && this.bssNetworkConsent); }
	
	public final boolean sendIHaveBss(boolean forceSend)
	{
		//check if can send
		if(!forceSend && !comms()) return false;
		this.bssNetworkConsent = true; //if force-sent, then we are likely in single-player
		
		//construct and send
		final var data = new PacketByteBuf(Unpooled.buffer());
		data.writeIntLE(NETWORK_VERSION);
		CustomPayloadNetwork.sendC2S(C2S_I_HAVE_BSS, data);
		return true;
	}
	
	public final boolean sendLiveStatsSetting() { return sendLiveStatsSetting(BetterStatsHudScreen.getInstance().entryCount() > 0); }
	public final boolean sendLiveStatsSetting(boolean recieveLiveUpdates_aka_theSetting)
	{
		//if communications are off, don't send
		if(!comms()) return false;
		
		//construct and send
		final var data = new PacketByteBuf(Unpooled.buffer());
		data.writeBoolean(recieveLiveUpdates_aka_theSetting);
		CustomPayloadNetwork.sendC2S(C2S_LIVE_STATS, data);
		
		//assign new state value, and return true to indicate success
		this.enableLiveStats = recieveLiveUpdates_aka_theSetting;
		return true;
	}
	// ==================================================
	/**
	 * Returns an instance of {@link BetterStatsClientPlayNetworkHandler} from a given
	 * {@link ClientPlayerEntity}. Creates one if it doesn't exist yet.
	 * @param player The {@link ClientPlayerEntity}.
	 */
	public static final BetterStatsClientPlayNetworkHandler of(ClientPlayerEntity player) throws NullPointerException
	{
		final var cd = EntityHooks.getCustomData(Objects.requireNonNull(player));
		@Nullable BetterStatsClientPlayNetworkHandler cpnh = cd.getProperty(CUSTOM_DATA_ID);
		if(cpnh == null)
		{
			cpnh = new BetterStatsClientPlayNetworkHandler(player);
			cd.setProperty(CUSTOM_DATA_ID, cpnh);
		}
		return cpnh;
	}
	// ==================================================
}