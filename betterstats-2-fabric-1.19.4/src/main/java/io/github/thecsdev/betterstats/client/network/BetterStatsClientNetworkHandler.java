package io.github.thecsdev.betterstats.client.network;

import static io.github.thecsdev.betterstats.BetterStats.LOGGER;
import static io.github.thecsdev.betterstats.client.gui_hud.screen.BetterStatsHudScreen.HUD_ID;
import static io.github.thecsdev.betterstats.network.BetterStatsNetworkHandler.C2S_PREFS;
import static io.github.thecsdev.betterstats.network.BetterStatsNetworkHandler.S2C_I_HAVE_BSS;
import static io.github.thecsdev.betterstats.network.BetterStatsNetworkHandler.S2C_REQ_PREFS;
import static io.github.thecsdev.betterstats.network.BetterStatsNetworkHandler.S2C_STATS;
import static io.github.thecsdev.tcdcommons.api.client.registry.TCDCommonsClientRegistry.InGameHud_Screens;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.authlib.GameProfile;

import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.Side;
import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.betterstats.client.gui_hud.screen.BetterStatsHudScreen;
import io.github.thecsdev.betterstats.client.util.BSClientHttpUtils;
import io.github.thecsdev.betterstats.network.BSNetworkProfile;
import io.github.thecsdev.tcdcommons.api.events.TNetworkEvent;
import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;

/**
 * Client-side network handler for {@link BetterStats}.
 */
public final class BetterStatsClientNetworkHandler
{
	// ==================================================
	public static boolean serverHasBSS;
	public static boolean enableBSSProtocol;
	private static final Cache<String, BSNetworkProfile> ProfileCache;
	// ==================================================
	protected BetterStatsClientNetworkHandler() {}
	public static void init() {/*calls static*/}
	// --------------------------------------------------
	public static @Nullable BSNetworkProfile getCachedProfile(GameProfile gameProfile)
	{
		//null check the profile
		if(gameProfile == null) return null;
		
		//obtain name and id
		final var gpName = gameProfile.getName();
		final var gpUUID = Objects.toString(gameProfile.getId());
		
		//obtain entry (try both name and uuid)
		BSNetworkProfile profile = null;
		if(gpName != null)
			profile = ProfileCache.getIfPresent(gpName);
		if(profile == null && gpUUID != null)
			profile = ProfileCache.getIfPresent(gpUUID);
		
		//return entry
		/*if(profile != null && profile.isLocalClient()) return null;
		else */return profile;
	}
	// ==================================================
	static
	{
		//init variables
		ProfileCache = CacheBuilder.newBuilder()
				.expireAfterAccess(15, TimeUnit.MINUTES) //expire if not accessed frequently, but
				.expireAfterWrite(60, TimeUnit.MINUTES) //regardless of access frequency, eventually it must expire
				.build();
		//^ why so long to expire? because of HTTP rate limits for remote badges
		//^ (statistics caches do get updated when requesting new stats tho)
		
		//init network
		initNetworkReceivers();
	}
	
	private static void initNetworkReceivers()
	{
		//by default, do not respond to S2C_REQ_PREFS
		enableBSSProtocol = false;
		serverHasBSS = false;
		ClientPlayerEvent.CLIENT_PLAYER_QUIT.register((cp) ->
		{
			enableBSSProtocol = false;
			serverHasBSS = false;
			InGameHud_Screens.remove(HUD_ID); //TODO - temporary bug fix for switching worlds/servers
		});
		//handle S2C_REQ_PREFS
		NetworkManager.registerReceiver(Side.S2C, S2C_I_HAVE_BSS, (payload, context) -> serverHasBSS = true);
		NetworkManager.registerReceiver(Side.S2C, S2C_REQ_PREFS, (payload, context) -> c2s_sendPrefs());
		
		//handle receiving stats
		TNetworkEvent.RECEIVE_PACKET_POST.register((packet, side) ->
		{
			//handle BSNetworkProfile-s over the vanilla packet protocol
			if(!(packet instanceof StatisticsS2CPacket) || side != NetworkSide.CLIENTBOUND)
				return;
			try { onReceivedBSNetworkProfile(BSNetworkProfile.ofLocalClient()); }
			catch(IllegalStateException ise) { /*MinecraftClient.player is null. Ignore that.*/ }
		});
		NetworkManager.registerReceiver(Side.S2C, S2C_STATS, (payload, context) ->
			//handle BSNetworkProfile-s over the S2C_STATS protocol
			onReceivedBSNetworkProfile(BSNetworkProfile.readPacket(payload))
		);
	}
	
	/**
	 * Caches the passed {@link BSNetworkProfile}, and/or merges it with
	 * an existing cache entry. The returned value will not be the
	 * {@link BSNetworkProfile} that was passed, if there already is a cached entry.
	 * @param of The {@link BSNetworkProfile} to cache or merge with existing cache.
	 */
	public static BSNetworkProfile handleCachingAndMerging(BSNetworkProfile of)
	{
		var existing = getCachedProfile(of.gameProfile);
		//put if doesn't exist
		if(existing == null)
		{
			//put cache
			ProfileCache.put(of.getProfileDisplayName(), of);
			existing = of;
			
			//check for remote badges, but only when this entry is cached for the first time,
			//aka if it didn't exist before
			final var pUID = of.gameProfile.getId();
			if(pUID != null)
				BSClientHttpUtils.getRemotePlayerBadgesAsync(pUID, (success, remote_badges) ->
				{
					//make sure the request was successful
					//(most of the time it probably won't be)
					if(!success) return;
					//add all badges that were received
					of.playerBadgeIds.addAll(remote_badges);
					//notify BSS
					@SuppressWarnings("resource")
					final var currentScreen = MinecraftClient.getInstance().currentScreen;
					if(currentScreen instanceof BetterStatsScreen)
						//TODO - Maybe implement a listener interface for this? Tho it's complicated
						//as a similar interface technically already exists, but cannot be used here
						((BetterStatsScreen)currentScreen).reInit_BSNetworkProfilePanel();
				});
		}
		//merge if it does exist
		else existing.putAllStats(of);
		//finally, return
		return existing;
	}
	
	private static boolean onReceivedBSNetworkProfile(BSNetworkProfile receivedProfile)
	{
		//null check, return false to indicate failure
		if(receivedProfile == null) return false;
		
		//obtain profile info
		/*var pDisplayName = profile.getProfileDisplayName();
		var existingProfile = ProfileCache.getIfPresent(pDisplayName);
		//var notifyCurrentScreen = (existingProfile == null) || profile.isLocalClient();
		
		//cache...
		if(existingProfile != null)
			//if one exists, just add the updated stats on top of it
			existingProfile.putAllStats(profile);
		else
			//but if one doesn't exist, then put this new one in place
			ProfileCache.put(pDisplayName, (existingProfile = profile));*/
		final var cachedProfile = handleCachingAndMerging(receivedProfile);
		
		//...and notify
		var client = MinecraftClient.getInstance();
		var screen = client.currentScreen;
		if(screen instanceof BStatsListener)
		{
			var bsl = (BStatsListener)screen;
			if(Objects.equals(cachedProfile.gameProfile.getId(), new UUID(0, 0)))
				client.executeSync(() -> bsl.onStatsPlayerNotFound()); //TODO - Thread safety!
			else if(BSNetworkProfile.compareGameProfiles(bsl.getListenerTargetGameProfile(), cachedProfile.gameProfile))
				client.executeSync(() -> bsl.onBetterStatsReady(cachedProfile)); //TODO - Thread safety!
		}
		
		//return true to indicate everything was done
		return true;
	}
	// ==================================================
	public static boolean comms() { return (enableBSSProtocol || MinecraftClient.getInstance().isInSingleplayer()); }
	public static boolean c2s_sendPrefs()
	{
		//only send C2S_PREFS when allowed to
		if(!comms()) return false;
		
		//create prefs. packet
		var data = new PacketByteBuf(Unpooled.buffer());
		data.writeBoolean(enableBSSProtocol && BetterStatsHudScreen.getInstance() != null); //boolean - statsHudAccuracyMode
		var packet = new CustomPayloadC2SPacket(C2S_PREFS, data);
		//send packet
		try { MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet); }
		catch(Exception e) { LOGGER.debug("Failed to send '" + C2S_PREFS + "' packet; " + e.getMessage()); }
		//return
		return true;
	}
	// ==================================================
}