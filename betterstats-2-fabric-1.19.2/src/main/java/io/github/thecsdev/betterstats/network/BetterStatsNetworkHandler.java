package io.github.thecsdev.betterstats.network;

import static io.github.thecsdev.betterstats.BetterStats.LOGGER;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.Side;
import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.util.StatUtils;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;

/**
 * Server-side network handler for {@link BetterStats}.<br/>
 * Works on both physical and logical sides.
 */
public final class BetterStatsNetworkHandler
{
	// ==================================================
	public static final Identifier S2C_I_HAVE_BSS; //server tells the client it has bss installed
	public static final Identifier S2C_REQ_PREFS; //server asks client for their preferences
	public static final Identifier C2S_PREFS; //client tells the server their preferences
	// --------------------------------------------------
	/**
	 * A Map of player UUIDs and {@link PlayerPreferences} for the given players.
	 */
	private static final Cache<String, PlayerPreferences> PlayerPrefs;
	// ==================================================
	protected BetterStatsNetworkHandler() {}
	public static void init() {/*calls static*/}
	// ==================================================
	static
	{
		//init packet IDs
		S2C_I_HAVE_BSS = new Identifier(BetterStats.getModID(), "s2c_bss");
		S2C_REQ_PREFS = new Identifier(BetterStats.getModID(), "s2c_rp");
		C2S_PREFS = new Identifier(BetterStats.getModID(), "c2s_p");
		
		//init the map that keeps track of privacy prefs.
		PlayerPrefs = CacheBuilder.newBuilder()
				.expireAfterWrite(15, TimeUnit.MINUTES)
				.build();
		
		//handle receiving packets
		initNetworkReceivers();
		
		//handle player prefs
		PlayerEvent.PLAYER_JOIN.register(player -> s2c_iHaveBSS(player));
		PlayerEvent.PLAYER_RESPAWN.register((player, endPortal) -> s2c_requestPrefs(player, false));
		PlayerEvent.PLAYER_QUIT.register(player -> PlayerPrefs.invalidate(player.getUuidAsString()));
		//update the player on entity stat changes
		EntityEvent.LIVING_DEATH.register((entity, deathSource) ->
		{
			//handle player killing other entities
			if(deathSource.getAttacker() instanceof ServerPlayerEntity)
				s2c_updatePlayerOnMobStat((ServerPlayerEntity)deathSource.getAttacker(), entity.getType(), 1, 0);
			//handle other entities killing players (do not use 'else')
			if(entity instanceof ServerPlayerEntity && deathSource.getAttacker() != null)
				s2c_updatePlayerOnMobStat((ServerPlayerEntity)entity, deathSource.getAttacker().getType(), 0, 1);
			//pass
			return EventResult.pass();
		});
		
		//update the player on block stat changes
		BlockEvent.BREAK.register((world, blockPos, blockState, player, exp) ->
		{
			s2c_updatePlayerOnItemStat(player, blockState.getBlock().asItem(), 1, 0, 0, 0, 0, 0);
			return EventResult.pass();
		});
		BlockEvent.PLACE.register((world, blockPos, blockState, entity) ->
		{
			//check if player did it
			if(!(entity instanceof ServerPlayerEntity))
				return EventResult.pass();
			//update and pass
			s2c_updatePlayerOnItemStat((ServerPlayerEntity)entity, blockState.getBlock().asItem(), 0, 0, 1, 0, 0, 0);
			return EventResult.pass();
		});
		//update player on crafting stats
		PlayerEvent.CRAFT_ITEM.register((player, itemStack, inventory) ->
		{
			//make sure a server player did it
			if(!(player instanceof ServerPlayerEntity))
				return;
			//update
			s2c_updatePlayerOnItemStat((ServerPlayerEntity)player, itemStack.getItem(), 0, 1, 0, 0, 0, 0);
		});
		//update player on pick-up and drop stats
		PlayerEvent.PICKUP_ITEM_POST.register((player, itemEntity, itemStack) ->
		{
			//make sure a server player did it
			if(!(player instanceof ServerPlayerEntity))
				return;
			//update
			var c = itemStack.getCount();
			s2c_updatePlayerOnItemStat((ServerPlayerEntity)player, itemStack.getItem(), 0, 0, 0, 0, c, 0);
		});
		PlayerEvent.DROP_ITEM.register((player, itemEntity) ->
		{
			//make sure a server player did it
			if(!(player instanceof ServerPlayerEntity))
				return EventResult.pass();
			//update
			var c = itemEntity.getStack().getCount();
			s2c_updatePlayerOnItemStat((ServerPlayerEntity)player, itemEntity.getStack().getItem(), 0, 0, 0, 0, 0, c);
			return EventResult.pass();
		});
		//TODO - Track "use" for items
	}
	// --------------------------------------------------
	private static void initNetworkReceivers()
	{
		//handle player prefs.
		NetworkManager.registerReceiver(Side.C2S, C2S_PREFS, (payload, context) ->
		{
			var player = (ServerPlayerEntity)context.getPlayer();
			var prefs = getOrCreatePlayerPrefs(player);
			prefs.betterStatsInstalled = true;
			try { prefs.enabled = payload.readBoolean(); }
			catch(Exception e) { LOGGER.debug("Failed to handle '" + C2S_PREFS + "' packet; " + e.getMessage()); }
		});
	}
	// ==================================================
	/**
	 * Gets the {@link PlayerPreferences} for a given player.<br/>
	 * Will create a new {@link PlayerPreferences} instance if one doesn't exist.
	 * @param player The player in question.
	 */
	private static PlayerPreferences getOrCreatePlayerPrefs(ServerPlayerEntity player)
	{
		var uuid = player.getUuidAsString();
		var get = PlayerPrefs.getIfPresent(uuid);
		if(get == null) PlayerPrefs.put(uuid, get = new PlayerPreferences());
		return get;
	}

	/**
	 * Tell a client the server has BSS installed.
	 */
	public static void s2c_iHaveBSS(ServerPlayerEntity player)
	{
		var data = new PacketByteBuf(Unpooled.EMPTY_BUFFER);
		try { player.networkHandler.sendPacket(new CustomPayloadS2CPacket(S2C_I_HAVE_BSS, data)); }
		catch(Exception e) { LOGGER.debug("Failed to send '" + S2C_I_HAVE_BSS + "' packet; " + e.getMessage()); }
	}
	
	/**
	 * Tries to ask a {@link ServerPlayerEntity} if they have
	 * {@link BetterStats} installed. No response will be given if not.
	 * @param player The {@link ServerPlayerEntity} to ask.
	 * @param force Whether or not to ignore existing {@link #PlayerPrefs} entries.
	 */
	public static void s2c_requestPrefs(ServerPlayerEntity player, boolean force)
	{
		//check if one exists
		if(!force && PlayerPrefs.getIfPresent(player.getUuidAsString()) != null)
			return;
		//create request packet and send
		var data = new PacketByteBuf(Unpooled.EMPTY_BUFFER);
		try { player.networkHandler.sendPacket(new CustomPayloadS2CPacket(S2C_REQ_PREFS, data)); }
		catch(Exception e) { LOGGER.debug("Failed to send '" + S2C_REQ_PREFS + "' packet; " + e.getMessage()); }
	}
	// --------------------------------------------------
	public static void s2c_updatePlayerOnItemStat(ServerPlayerEntity player, Item item,
			int m, int c, int u, int b, int pu, int d)
	{
		//check for player prefs
		var prefs = PlayerPrefs.getIfPresent(player.getUuidAsString());
		if(prefs == null) { s2c_requestPrefs(player, false); return; }
		else if(!prefs.betterStatsInstalled || !prefs.enabled) return;
		//cooldown system to prevent spam
		var curr = System.currentTimeMillis();
		if(curr - prefs._lastItemUpdate < PlayerPreferences._updateCooldown) return;
		prefs._lastItemUpdate = curr;
		//obtain stats
		var iStats = new StatUtils.StatUtilsItemStat(player.getStatHandler(), item);
		//create packet
		var object2int = new Object2IntOpenHashMap<Stat<?>>();
		if(m > 0 && iStats.block != null) object2int.put(Stats.MINED.getOrCreateStat(iStats.block), iStats.sMined + m);
		if(c > 0) object2int.put(Stats.CRAFTED.getOrCreateStat(iStats.item), iStats.sCrafted + c);
		if(u > 0) object2int.put(Stats.USED.getOrCreateStat(iStats.item), iStats.sUsed + u);
		if(b > 0) object2int.put(Stats.BROKEN.getOrCreateStat(iStats.item), iStats.sBroken + b);
		if(pu > 0) object2int.put(Stats.PICKED_UP.getOrCreateStat(iStats.item), iStats.sPickedUp + pu);
		if(d > 0) object2int.put(Stats.DROPPED.getOrCreateStat(iStats.item), iStats.sDropped + d);
		var packet = new StatisticsS2CPacket(object2int);
		//send packet
		try { player.networkHandler.sendPacket(packet); }
		catch(Exception e) { LOGGER.debug("Failed to send 'StatisticsS2CPacket' packet; " + e.getMessage()); }
	}
	
	public static void s2c_updatePlayerOnMobStat(ServerPlayerEntity player, EntityType<?> entityType,
			int k, int d)
	{
		//check for player prefs
		var prefs = PlayerPrefs.getIfPresent(player.getUuidAsString());
		if(prefs == null) { s2c_requestPrefs(player, false); return; }
		else if(!prefs.betterStatsInstalled || !prefs.enabled) return;
		//cooldown system to prevent spam
		var curr = System.currentTimeMillis();
		if(curr - prefs._lastMobUpdate < PlayerPreferences._updateCooldown) return;
		prefs._lastMobUpdate = curr;
		//obtain stats
		var eStats = new StatUtils.StatUtilsMobStat(player.getStatHandler(), entityType);
		//create packet
		var obj2int = new Object2IntOpenHashMap<Stat<?>>();
		if(k > 0) obj2int.put(Stats.KILLED.getOrCreateStat(entityType), eStats.killed + k);
		if(d > 0) obj2int.put(Stats.KILLED_BY.getOrCreateStat(entityType), eStats.killedBy + d);
		var packet = new StatisticsS2CPacket(obj2int);
		//send packet
		try { player.networkHandler.sendPacket(packet); }
		catch(Exception e) { LOGGER.debug("Failed to send 'StatisticsS2CPacket' packet; " + e.getMessage()); }
	}
	// ==================================================
	/**
	 * Keeps track of any preferences a player may have about
	 * how the server should handle their statistics.
	 */
	//TODO - Implement feature for viewing other player's stats
	private static class PlayerPreferences
	{
		public static final short _updateCooldown = 500;
		public long _lastItemUpdate = 0;
		public long _lastMobUpdate = 0;
		public boolean betterStatsInstalled = false;
		public boolean enabled = false;
	}
	// ==================================================
}