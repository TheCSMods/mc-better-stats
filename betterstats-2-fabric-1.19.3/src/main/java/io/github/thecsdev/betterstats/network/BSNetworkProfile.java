package io.github.thecsdev.betterstats.network;

import java.util.Objects;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import io.github.thecsdev.tcdcommons.api.hooks.TCommonHooks;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;

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
	// ==================================================
	protected BSNetworkProfile(GameProfile profile, StatHandler stats)
	{
		this.gameProfile = Objects.requireNonNull(profile);
		this.stats = Objects.requireNonNull(stats);
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
	 * @throws NullPointerException
	 */
	public static BSNetworkProfile ofServerPlayer(ServerPlayerEntity player)
	{
		return new BSNetworkProfile(player.getGameProfile(), player.getStatHandler());
	}
	// --------------------------------------------------
	public @Override int hashCode() { return this.gameProfile.hashCode() + this.stats.hashCode(); }
	public @Override boolean equals(Object obj)
	{
		if(!(obj instanceof BSNetworkProfile))
			return false;
		var o = (BSNetworkProfile)obj;
		return (Objects.equals(this.gameProfile, o.gameProfile)/* && Objects.equals(this.stats, o.stats)*/);
	}
	// ==================================================
	/**
	 * Writes this entire {@link BSNetworkProfile} to a {@link PacketByteBuf}
	 * where it can then be read from using {@link #readPacket(PacketByteBuf)}.
	 * @param pbb The {@link PacketByteBuf} to write to.
	 */
	public void writePacket(PacketByteBuf pbb)
	{
		//write profile uuid
		if(this.gameProfile.getId() != null)
		{
			pbb.writeBoolean(true);
			pbb.writeUuid(this.gameProfile.getId());
		}
		else pbb.writeBoolean(false);
		
		//write profile name
		if(this.gameProfile.getName() != null)
		{
			pbb.writeBoolean(true);
			pbb.writeString(this.gameProfile.getName());
		}
		else pbb.writeBoolean(false);
		
		//write stats
		var stats = TCommonHooks.getStatHandlerStatMap(this.stats);
		new StatisticsS2CPacket(stats).write(pbb);
	}
	
	/**
	 * Reads a {@link BSNetworkProfile} from a {@link PacketByteBuf}.
	 * @param pbb The {@link PacketByteBuf} to read from.
	 */
	public static BSNetworkProfile readPacket(PacketByteBuf pbb)
	{
		//prepare
		UUID profileId = null;
		String profileName = null;
		
		//read game profile uuid
		var hasDefinedUUID = pbb.readBoolean();
		if(hasDefinedUUID) profileId = pbb.readUuid();
		
		//read game profile name
		var hasDefinedName = pbb.readBoolean();
		if(hasDefinedName) profileName = pbb.readString();
		
		//create game profile
		if(profileId == null && profileName == null)
			profileId = new UUID(0, 0);
		var gameProfile = new GameProfile(profileId, profileName);
		
		//read statistics
		var stats = new StatHandler();
		var statsMap = TCommonHooks.getStatHandlerStatMap(stats);
		for(var sEntry : ((Object2IntMap<Stat<?>>) new StatisticsS2CPacket(pbb).getStatMap()).object2IntEntrySet())
			statsMap.put(sEntry.getKey(), sEntry.getIntValue());
		
		//create and return
		return new BSNetworkProfile(gameProfile, stats);
	}
	// ==================================================
	/**
	 * Returns the {@link String} that should be used as the
	 * GUI "display name" for a given player based on the
	 * data present in their {@link #gameProfile}.
	 */
	public String getProfileDisplayName()
	{
		if(this.gameProfile.getName() != null)
			return this.gameProfile.getName();
		else return this.gameProfile.getId().toString();
	}
	// ==================================================
}