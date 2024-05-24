package io.github.thecsdev.betterstats.network;

import static io.github.thecsdev.betterstats.BetterStats.getModID;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.api.client.util.io.LocalThirdPartyStatsProvider;
import io.github.thecsdev.betterstats.client.network.BetterStatsClientPlayNetworkHandler;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.events.client.MinecraftClientEvent;
import io.github.thecsdev.tcdcommons.api.events.server.PlayerManagerEvent;
import io.github.thecsdev.tcdcommons.api.network.CustomPayloadNetwork;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.NetworkSide;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Represents the server-side network handler for {@link BetterStats}.
 */
public final @Internal class BetterStatsNetwork
{
	// ==================================================
	private BetterStatsNetwork() {}
	// --------------------------------------------------
	public static final Text TXT_TOGGLE_TOOLTIP  = BST.net_toggleTooltip();
	public static final Text TXT_CONSENT_WARNING = BST.net_consentWarning();
	public static final Text TXT_S3PS_TOOLTIP    = BST.net_s3psTooltip();
	//
	public static final int NETWORK_VERSION = 3;
	//
	public static final Identifier S2C_I_HAVE_BSS  = new Identifier(getModID(), "s2c_bss");
	public static final Identifier C2S_I_HAVE_BSS  = new Identifier(getModID(), "c2s_bss");
	public static final Identifier C2S_PREFERENCES = new Identifier(getModID(), "c2s_prf");
	// ==================================================
	public static void init() {}
	static
	{
		// ---------- SINGLEPLAYER/DEDICATED SERVER HANDLERS
		//init event handlers
		PlayerManagerEvent.PLAYER_CONNECTED.register(player ->
			BetterStatsServerPlayNetworkHandler.of(player).onPlayerConnected());
		
		//init network handlers
		CustomPayloadNetwork.registerReceiver(NetworkSide.SERVERBOUND, C2S_I_HAVE_BSS, ctx ->
			BetterStatsServerPlayNetworkHandler.of((ServerPlayerEntity)ctx.getPlayer()).onIHaveBss(ctx));
		
		CustomPayloadNetwork.registerReceiver(NetworkSide.SERVERBOUND, C2S_PREFERENCES, ctx ->
			BetterStatsServerPlayNetworkHandler.of((ServerPlayerEntity)ctx.getPlayer()).onPreferences(ctx));
		
		// ---------- PURE CLIENT-SIDE HANDLERS
		if(BetterStats.isClient())
		{
			//init event handlers
			MinecraftClientEvent.JOINED_WORLD.register((client, clientWorld) ->
				LocalThirdPartyStatsProvider.clearSessionStorage());
			/*MinecraftClientEvent.DISCONNECTED.register(client -> -- NULL POINTER EXCEPTION - LOGIC MOVED!!!
				BetterStatsClientPlayNetworkHandler.of(client.player).onDisconnected());*/
			
			//init network handlers
			CustomPayloadNetwork.registerReceiver(NetworkSide.CLIENTBOUND, S2C_I_HAVE_BSS, ctx ->
				BetterStatsClientPlayNetworkHandler.of((ClientPlayerEntity)ctx.getPlayer()).onIHaveBss(ctx));
		}
	}
	// ==================================================
}