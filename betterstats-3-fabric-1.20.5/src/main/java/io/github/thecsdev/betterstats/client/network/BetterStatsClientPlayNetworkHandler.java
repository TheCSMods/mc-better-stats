package io.github.thecsdev.betterstats.client.network;

import static io.github.thecsdev.betterstats.BetterStats.LOGGER;
import static io.github.thecsdev.betterstats.BetterStats.getModID;
import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.betterstats.network.BetterStatsNetwork.C2S_I_HAVE_BSS;
import static io.github.thecsdev.betterstats.network.BetterStatsNetwork.C2S_MCBS_REQUEST;
import static io.github.thecsdev.betterstats.network.BetterStatsNetwork.C2S_PREFERENCES;
import static io.github.thecsdev.betterstats.network.BetterStatsNetwork.NETWORK_VERSION;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.BetterStatsConfig;
import io.github.thecsdev.betterstats.api.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.betterstats.api.util.io.StatsProviderIO;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreenWrapper;
import io.github.thecsdev.tcdcommons.api.events.client.MinecraftClientEvent;
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
	// ==================================================
	private final ClientPlayerEntity player;
	private final BetterStatsConfig  config;
	// --------------------------------------------------
	public boolean serverHasBss            = false;
	public boolean bssNetworkConsent       = false;
	public boolean netPref_enableLiveStats = false;
	// --------------------------------------------------
	private final HashMap<String, OtherClientPlayerStatsProvider> sessionPlayerStatStorage = new HashMap<>();
	// ==================================================
	private BetterStatsClientPlayNetworkHandler(ClientPlayerEntity player) throws NullPointerException
	{
		//assign values
		this.player = Objects.requireNonNull(player);
		this.config = Objects.requireNonNull(BetterStats.getInstance().getConfig());
		
		//disconnection handler
		//note: must execute only once. must unregister itself after execution
		final AtomicReference<MinecraftClientEvent.ClientDisconnect> evhCd = new AtomicReference<>(null);
		evhCd.set(client ->
		{
			MinecraftClientEvent.DISCONNECTED.unregister(Objects.requireNonNull(evhCd.get()));
			onDisconnected();
		});
		MinecraftClientEvent.DISCONNECTED.register(Objects.requireNonNull(evhCd.get()));
	}
	// --------------------------------------------------
	public final ClientPlayerEntity getPlayer() { return this.player; }
	// ==================================================
	/**
	 * Handles the {@link #player} disconnecting from the server.
	 */
	public final void onDisconnected()
	{
		//clear flags from the no-longer-valid session
		this.serverHasBss            = false;
		this.netPref_enableLiveStats = false;
		this.bssNetworkConsent       = false;
		this.sessionPlayerStatStorage.clear();
	}
	
	/**
	 * Handles the server telling the {@link #player} it has {@link BetterStats} installed.
	 */
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
		LOGGER.info("[Client] The server has '" + getModID() + "' installed.");
		
		//if the client is in single player, handle live stats updates
		//in case the statistics hud had entries in it
		if(MC_CLIENT.isInSingleplayer() || BetterStats.getInstance().getConfig().trustAllServersBssNet)
			TaskScheduler.executeOnce(MC_CLIENT, () -> this.player.networkHandler != null, () ->
			{
				sendIHaveBss(true);
				sendPreferences();
			});
	}
	
	/**
	 * Handles the server sending the {@link #player} an MCBS file.
	 */
	public final void onMcbs(PacketContext ctx)
	{
		//obtain the buffer
		final var buffer = ctx.getPacketBuffer();
		
		//read the packet type
		final int type = buffer.readInt();
		if(type != 1) return; //ignore non-1s for now
		
		//read player name
		final var playerName = buffer.readString();
		
		//read MCBS
		final var statsProvider = new OtherClientPlayerStatsProvider(playerName);
		try { StatsProviderIO.read(buffer, statsProvider); }
		catch(Exception exc) {/*ignore failures to process the MCBS file*/}
		
		//store MCBS
		if(!this.sessionPlayerStatStorage.containsKey(playerName))
			this.sessionPlayerStatStorage.put(playerName, new OtherClientPlayerStatsProvider(playerName));
		
		final var spss = this.sessionPlayerStatStorage.get(playerName);
		spss.setAll(statsProvider);
		
		//the following must be done on Minecraft's main thread
		MC_CLIENT.executeSync(() ->
		{
			//if BSS is currently open, broadcast the info to it if applicable
			if(MC_CLIENT.currentScreen instanceof TScreenWrapper<?> tsw &&
					tsw.getTargetTScreen() instanceof BetterStatsScreen bss)
			{
				//check if the screen is related to the target player's stats provier,
				//and if it is, refresh the screen
				if(bss.getStatsProvider() == spss)
					bss.refresh();
			}
		});
	}
	// --------------------------------------------------
	/**
	 * Returns {@code true} if {@link BetterStatsClientPlayNetworkHandler}
	 * is allowed to communicate with the server.
	 */
	public final boolean comms() { return MC_CLIENT.isInSingleplayer() || (this.serverHasBss && this.bssNetworkConsent); }
	
	/**
	 * Sends the server a message letting the server know the
	 * {@link #player} has {@link BetterStats} installed.
	 */
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
	
	/**
	 * Sends the player's preferences to the server, such as for
	 * example the "enable live stats" preference.
	 */
	public final boolean sendPreferences()
	{
		//if communications are off, don't send
		if(!comms()) return false;
		
		//construct and send
		final var data = new PacketByteBuf(Unpooled.buffer());
		data.writeBoolean(this.       netPref_enableLiveStats);   //write live stats preference
		data.writeBoolean(this.config.netPref_allowStatsSharing); //write stats sharing consent preference
		CustomPayloadNetwork.sendC2S(C2S_PREFERENCES, data);
		
		//return true to indicate success
		return true;
	}
	
	/**
	 * Similar to {@link #sendPreferences()}, except the sent preferences disable
	 * everything, and the {@link #bssNetworkConsent} is fully revoked.
	 */
	public final boolean sendAndRevokePreferences()
	{
		//if communications are off, don't send
		if(!comms()) return false;
		
		//construct and send
		final var data = new PacketByteBuf(Unpooled.buffer());
		data.writeBoolean(false); //write live stats preference
		data.writeBoolean(false); //write stats sharing consent preference
		CustomPayloadNetwork.sendC2S(C2S_PREFERENCES, data);
		
		//assign new state value, and return true to indicate success
		this.netPref_enableLiveStats   = false;
		this.bssNetworkConsent = false;
		return true;
	}
	
	/**
	 * Sends a third-party player statistics request to the server.
	 * The server should respond by sending back the given player's
	 * statistics, if said player consents to this.
	 * @param The name of the other player whose statistics this client wishes to see.
	 */
	public final boolean sendMcbsRequest(String playerName)
	{
		//requirements
		Objects.requireNonNull(playerName);
		
		//if communications are off, don't send
		if(!comms()) return false;
		
		//construct and send
		final var data = new PacketByteBuf(Unpooled.buffer());
		data.writeInt(1);             //write request type (1 = player)
		data.writeString(playerName); //write player name
		CustomPayloadNetwork.sendC2S(C2S_MCBS_REQUEST, data);
		
		//return true to indicate success
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