package io.github.thecsdev.betterstats.network;

import static io.github.thecsdev.tcdcommons.api.registry.TCDCommonsRegistry.PlayerBadges;

import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;

import io.github.thecsdev.betterstats.api.client.features.player.badges.BssClientPlayerBadge_Badgeless;
import io.github.thecsdev.tcdcommons.api.client.features.player.badges.ClientPlayerBadge;
import io.github.thecsdev.tcdcommons.api.features.player.badges.PlayerBadge;
import io.github.thecsdev.tcdcommons.api.features.player.badges.ServerPlayerBadgeHandler;
import io.github.thecsdev.tcdcommons.api.hooks.TCommonHooks;
import io.github.thecsdev.tcdcommons.api.registry.TCDCommonsRegistry;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.Identifier;

/**
 * A "player profile" that holds information about a given
 * player and their statistics.
 */
public final class BSNetworkProfile
{
	// ==================================================
	/**
	 * The {@link GameProfile} associated with the given player.
	 */
	public final GameProfile gameProfile;
	
	/**
	 * The statistics associated with the given player.
	 */
	public final StatHandler stats;
	
	/**
	 * Stores a list of {@link PlayerBadge} {@link Identifier}s
	 * associated with this {@link BSNetworkProfile}.
	 */
	public final HashSet<Identifier> playerBadgeIds;
	// ==================================================
	protected BSNetworkProfile(ServerPlayerEntity player)
	{
		this(player.getGameProfile(), player.getStatHandler());
		this.playerBadgeIds.addAll(ServerPlayerBadgeHandler.getBadgeHandler(player).getBadges());
	}
	
	protected BSNetworkProfile(GameProfile profile, StatHandler stats)
	{
		this.gameProfile = Objects.requireNonNull(profile);
		this.stats = Objects.requireNonNull(stats);
		this.playerBadgeIds = Sets.newHashSet();
	}
	// --------------------------------------------------
	/**
	 * Returns the {@link BSNetworkProfile} of the local client on the Minecraft client side.<br/>
	 * <b>Warning: </b> Works only on the client, do NOT use this on the server side.
	 * @throws IllegalStateException When the client player is not connected to a world or is null,
	 * or when attempting to call this on the server side.
	 */
	@Environment(EnvType.CLIENT)
	public static BSNetworkProfile ofLocalClient()
	{
		try
		{
			var c = net.minecraft.client.MinecraftClient.getInstance();
			if(c.player == null) throw new IllegalStateException("MinecraftClient.player is null");
			return new BSNetworkProfile(c.player.getGameProfile(), c.player.getStatHandler());
		}
		catch(Throwable t)
		{
			if (t instanceof ClassNotFoundException || t instanceof NoClassDefFoundError)
				throw new IllegalStateException("Are you executing this on the server side?; " + t.getMessage());
			else throw t;
		}
	}
	
	/**
	 * Returns the {@link BSNetworkProfile} of a {@link ServerPlayerEntity}.
	 * @param player The player based on who the {@link BSNetworkProfile} will be created.
	 */
	public static BSNetworkProfile ofServerPlayer(@Nullable ServerPlayerEntity player)
	{
		if(player == null) return ofNull();
		else return new BSNetworkProfile(player);
	}
	
	/**
	 * Retruns a {@link BSNetworkProfile} will a null
	 * {@link GameProfile} and an empty {@link StatHandler}.
	 */
	public static BSNetworkProfile ofNull()
	{
		var gameProfile = new GameProfile(new UUID(0, 0), null);
		return new BSNetworkProfile(gameProfile, new StatHandler());
	}
	
	/**
	 * Returns a {@link BSNetworkProfile} with a non-null
	 * {@link GameProfile} and an empty {@link StatHandler}.
	 */
	public static BSNetworkProfile ofGameProfile(GameProfile gameProfile)
	{
		return new BSNetworkProfile(gameProfile, new StatHandler());
	}
	// --------------------------------------------------
	public @Override int hashCode() { return this.gameProfile.hashCode(); }
	public @Override boolean equals(Object obj)
	{
		//basic checks
		if(obj == this) return true;
		else if(!(obj instanceof BSNetworkProfile)) return false;
		//cast and compare
		var o = (BSNetworkProfile)obj;
		return compareGameProfiles(this.gameProfile, o.gameProfile);
	}
	public static boolean compareGameProfiles(GameProfile gpA, GameProfile gpB)
	{
		//gather info for profile comparing
		var idA = gpA.getId();
		var idB = gpB.getId();
		var nameA = gpA.getName();
		var nameB = gpB.getName();
		//compare profiles and return
		var a = (idA != null && idB != null) && Objects.equals(idA, idB);
		var b = (nameA != null && nameB != null) && Objects.equals(nameA, nameB);
		return (a || b);
	}
	// ==================================================
	/**
	 * Writes this entire {@link BSNetworkProfile} to a {@link PacketByteBuf}
	 * where it can then be read from using {@link #readPacket(PacketByteBuf)}.
	 * @param pbb The {@link PacketByteBuf} to write to.
	 */
	public void writePacket(PacketByteBuf pbb)
	{
		//write game profile
		writeGameProfile(pbb, this.gameProfile);
		
		//write stats
		var statsPbb = new PacketByteBuf(Unpooled.buffer());
		new StatisticsS2CPacket(TCommonHooks.getStatHandlerStatMap(this.stats)).write(statsPbb);
		
		pbb.writeInt(statsPbb.readableBytes());
		pbb.writeBytes(statsPbb);
		statsPbb.release(); //avoid memory leaks
		
		//write badges
		this.playerBadgeIds.remove(null); //null not allowed
		final var badgeArr = this.playerBadgeIds.toArray(new Identifier[0]);
		
		pbb.writeInt(badgeArr.length);
		for(Identifier badgeId : badgeArr)
			pbb.writeIdentifier(badgeId);
	}
	
	/**
	 * Reads a {@link BSNetworkProfile} from a {@link PacketByteBuf}.
	 * @param pbb The {@link PacketByteBuf} to read from.
	 */
	public static BSNetworkProfile readPacket(PacketByteBuf pbb)
	{
		//read game profile
		GameProfile gameProfile = readGameProfile(pbb);
		if(gameProfile == null) gameProfile = new GameProfile(new UUID(0, 0), null);
		
		//read statistics
		int statsPbbSize = pbb.readInt();
		PacketByteBuf statsPbb = new PacketByteBuf(pbb.readBytes(statsPbbSize));
		
		var stats = new StatHandler();
		var statsMap = TCommonHooks.getStatHandlerStatMap(stats);
		for(var sEntry : ((Object2IntMap<Stat<?>>) new StatisticsS2CPacket(statsPbb).getStatMap()).object2IntEntrySet())
			statsMap.put(sEntry.getKey(), sEntry.getIntValue());
		statsPbb.release(); //avoid memory leaks
		
		//construct object
		final var newProfile = new BSNetworkProfile(gameProfile, stats);
		
		//read player badges
		final var badgeCount = pbb.readInt();
		for(int index = 0; index < badgeCount; index++)
		{
			//DoS attack prevention
			if(!pbb.isReadable()) break;
			
			//check if the badge is client-side - don't allow those
			final var playerBadgeId = pbb.readIdentifier();
			final var playerBadge = PlayerBadges.getOrDefault(playerBadgeId, null);
			if(playerBadge instanceof ClientPlayerBadge)
				continue; //skip client player badges
			
			//add the player badge to the set
			newProfile.playerBadgeIds.add(playerBadgeId);
		}
		
		//create and return
		return newProfile;
	}
	// --------------------------------------------------
	public static void writeGameProfile(PacketByteBuf buf, @Nullable GameProfile gameProfile)
	{
		//handle null
		if(gameProfile == null)
		{
			buf.writeBoolean(false);
			buf.writeBoolean(false);
			return;
		}
		//get uuid and name
		var uid = gameProfile.getId();
		var una = gameProfile.getName();
		//write uuid
		buf.writeBoolean(uid != null);
		if(uid != null) buf.writeUuid(uid);
		//write name
		buf.writeBoolean(una != null);
		if(una != null) buf.writeString(una);
	}
	
	public static @Nullable GameProfile readGameProfile(PacketByteBuf buf)
	{
		//define
		UUID uid = null;
		String una = null;
		//read
		if(buf.readBoolean()) uid = buf.readUuid();
		if(buf.readBoolean()) una = buf.readString();
		//handle null
		if(uid == null && una == null) return null;
		//construct
		else return new GameProfile(uid, una);
	}
	// ==================================================
	/**
	 * Returns true if this {@link #gameProfile} belongs
	 * to the local player from the {@link net.mincraft.client.MinecraftClient}.
	 */
	@SuppressWarnings("resource")
	public boolean isLocalClient()
	{
		try { return net.minecraft.client.MinecraftClient.getInstance().player.getGameProfile().equals(this.gameProfile); }
		catch(Throwable e) { return false; }
	}
	// --------------------------------------------------
	/**
	 * Returns the {@link String} that should be used as the
	 * GUI "display name" for a given player based on the
	 * data present in their {@link #gameProfile}.
	 */
	public String getProfileDisplayName() { return getGameProfileDisplayName(this.gameProfile); }
	public static String getGameProfileDisplayName(GameProfile gameProfile)
	{
		if(gameProfile.getName() != null)
			return gameProfile.getName();
		else return gameProfile.getId().toString();
	}
	// --------------------------------------------------
	/**
	 * Puts all stats from another {@link BSNetworkProfile}
	 * into the current {@link #stats} of this {@link BSNetworkProfile}.
	 * @param statHandler The stats to add to {@link #stats}.
	 */
	public void putAllStats(BSNetworkProfile other)
	{
		//put stats
		var shMap = TCommonHooks.getStatHandlerStatMap(other.stats);
		var sMap = TCommonHooks.getStatHandlerStatMap(this.stats);
		sMap.putAll(shMap);
		
		//put badges
		this.playerBadgeIds.addAll(other.playerBadgeIds);
	}
	// --------------------------------------------------
	public HashSet<PlayerBadge> collectPlayerBadges()
	{
		//define needed stuff
		final var reg = TCDCommonsRegistry.PlayerBadges;
		final var set = new HashSet<PlayerBadge>();
		//collect badges
		for(Identifier badgeId : this.playerBadgeIds)
		{
			final var badge = reg.getOrDefault(badgeId, null);
			if(badge != null) set.add(badge);
		}
		//badgeless badge
		if(set.size() == 0)
			set.add(BssClientPlayerBadge_Badgeless.instance);
		//return
		return set;
	}
	// ==================================================
}