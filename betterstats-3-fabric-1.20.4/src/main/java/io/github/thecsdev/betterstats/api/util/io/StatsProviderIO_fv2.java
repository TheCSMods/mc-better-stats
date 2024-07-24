package io.github.thecsdev.betterstats.api.util.io;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;

import java.util.UUID;
import java.util.stream.Collectors;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;

import io.github.thecsdev.betterstats.api.util.stats.SUItemStat;
import io.github.thecsdev.betterstats.api.util.stats.SUMobStat;
import io.github.thecsdev.betterstats.api.util.stats.SUPlayerBadgeStat;
import io.github.thecsdev.tcdcommons.api.util.exceptions.UnsupportedFileVersionException;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

/**
 * A class containing {@link StatsProviderIO} logic for file version 2.
 * @apiNote Used for backwards-compatibility. Internal use only!
 */
@Internal
public final class StatsProviderIO_fv2
{
	// ==================================================
	static final void write_fileChunks(PacketByteBuf buffer_file, IStatsProvider statsProvider)
	{
		//write chunks
		write_fileChunk("metadata", buffer_file, statsProvider);
		write_fileChunk("general", buffer_file, statsProvider);
		write_fileChunk("item", buffer_file, statsProvider);
		write_fileChunk("mob", buffer_file, statsProvider);
		write_fileChunk("player_badge", buffer_file, statsProvider);
	}
	// --------------------------------------------------
	private static final void write_fileChunk(String chunkId, PacketByteBuf buffer_file, IStatsProvider statsProvider)
	{
		//create a buffer for the chunk, and write the chunk ID to it
		PacketByteBuf buffer_chunk  = new PacketByteBuf(Unpooled.buffer());
		buffer_chunk.writeString(chunkId);
		
		//obtain and write chunk data to the chunk buffer
		switch(chunkId)
		{
			case "metadata":     write_fileChunk_meta(buffer_chunk, statsProvider); break;
			case "general":      write_fileChunk_general(buffer_chunk, statsProvider); break;
			case "item":         write_fileChunk_item(buffer_chunk, statsProvider); break;
			case "mob":          write_fileChunk_mob(buffer_chunk, statsProvider); break;
			case "player_badge": write_fileChunk_playerBadge(buffer_chunk, statsProvider); break;
			default: break;
		}
		
		//write the chunk data buffer to the file buffer
		buffer_file.writeIntLE(buffer_chunk.readableBytes());
		buffer_file.writeBytes(buffer_chunk);
		buffer_chunk.release();
	}
	
	@SuppressWarnings("deprecation")
	private static final void write_fileChunk_meta(PacketByteBuf buffer_chunk, IStatsProvider statsProvider)
	{
		//write display name
		@Nullable Text displayName = statsProvider.getDisplayName();
		if(displayName == null) displayName = literal("-");
		buffer_chunk.encode(NbtOps.INSTANCE, TextCodecs.CODEC, displayName);
		
		//write game profile
		writeGameProfile(buffer_chunk, statsProvider.getGameProfile());
	}
	
	private static final void write_fileChunk_general(PacketByteBuf buffer_chunk, IStatsProvider statsProvider)
	{
		//NOTE: Generally, this part is highly confusing, not only because "general stats" are referred to as
		//      "custom stats" internally, but also because here the stats are being stored in accordance
		//      to their REGISTRY Identifier-s, and NOT the Stat<Identifier> Identifier-s.
		//
		//      Aka, the Registry holding custom stats is a Map of Identifier-s, whose keys are also Identifier-s.
		//      Here, the stats are stored in accordance to the Registry aka Map KEYS, NOT VALUES!
		
		//obtain a map of the general stats, that maps the stats based on their identifier's namespaces,
		//aka group based on the "mod id" of the mod they belong to
		final var customStats = Lists.newArrayList(Registries.CUSTOM_STAT.iterator());
		final var customStatsMap = customStats.stream()
				.filter(stat -> Registries.CUSTOM_STAT.getId(stat) != null) //ignore incompatibilities and unregistered stats
				.filter(stat -> statsProvider.getStatValue(Stats.CUSTOM.getOrCreateStat(stat)) != 0)
				.collect(Collectors.groupingBy(stat -> Registries.CUSTOM_STAT.getId(stat).getNamespace()));
		
		//iterate groups, obtain and write their data
		for(final var entry : customStatsMap.entrySet())
		{
			//obtain the group id and its stats
			final var groupModId = entry.getKey();
			final var groupStats = entry.getValue();
			
			//write the group id and its length
			buffer_chunk.writeString(groupModId); //write group id
			buffer_chunk.writeVarInt(groupStats.size()); //write group length
			
			//write group entries [Identifier-path, VarInt-value]
			for(final Identifier customStatAsIdentifier : groupStats)
			{
				//obtain the custom stat and its value
				final Stat<Identifier> customStat = Stats.CUSTOM.getOrCreateStat(customStatAsIdentifier);
				final int customStatValue = statsProvider.getStatValue(customStat);
				
				//write the custom stat registry id path and its value
				buffer_chunk.writeString(customStatAsIdentifier.getPath());
				buffer_chunk.writeVarInt(customStatValue);
			}
		}
	}
	
	private static final void write_fileChunk_item(PacketByteBuf buffer_chunk, IStatsProvider statsProvider)
	{
		//obtain a map of item stats
		final var stats = SUItemStat.getItemStatsByModGroups(statsProvider, stat -> !stat.isEmpty());
		
		//iterate groups, and write their data
		for(final var entry : stats.entrySet())
		{
			//obtain the group id and its stats
			final var groupModId = entry.getKey();
			final var groupStats = entry.getValue();
			
			//write the group id and its length
			buffer_chunk.writeString(groupModId);
			buffer_chunk.writeVarInt(groupStats.size());
			
			//write group entries
			for(final var stat : groupStats)
			{
				buffer_chunk.writeString(stat.getStatID().getPath());
				buffer_chunk.writeVarInt(stat.mined);
				buffer_chunk.writeVarInt(stat.crafted);
				buffer_chunk.writeVarInt(stat.used);
				buffer_chunk.writeVarInt(stat.broken);
				buffer_chunk.writeVarInt(stat.pickedUp);
				buffer_chunk.writeVarInt(stat.dropped);
			}
		}
	}
	
	private static final void write_fileChunk_mob(PacketByteBuf buffer_chunk, IStatsProvider statsProvider)
	{
		//obtain a map of mod stats
		final var stats = SUMobStat.getMobStatsByModGroups(statsProvider, stat -> !stat.isEmpty());
		
		//iterate groups, and write their data
		for(final var entry : stats.entrySet())
		{
			//obtain the group id and its stats
			final var groupModId = entry.getKey();
			final var groupStats = entry.getValue();
			
			//write the group id and its length
			buffer_chunk.writeString(groupModId);
			buffer_chunk.writeVarInt(groupStats.size());
			
			//write group entries
			for(final var stat : groupStats)
			{
				buffer_chunk.writeString(stat.getStatID().getPath());
				buffer_chunk.writeVarInt(stat.kills);
				buffer_chunk.writeVarInt(stat.deaths);
			}
		}
	}
	
	private static final void write_fileChunk_playerBadge(PacketByteBuf buffer_chunk, IStatsProvider statsProvider)
	{
		//obtain a map of mod stats
		final var stats = SUPlayerBadgeStat.getPlayerBadgeStatsByModGroups(statsProvider, stat -> !stat.isEmpty());
		
		//iterate groups, and write their data
		for(final var entry : stats.entrySet())
		{
			//obtain the group id and its stats
			final var groupModId = entry.getKey();
			final var groupStats = entry.getValue();
			
			//write the group id and its length
			buffer_chunk.writeString(groupModId);
			buffer_chunk.writeVarInt(groupStats.size());
			
			//write group entries
			for(final var stat : groupStats)
			{
				buffer_chunk.writeString(stat.getStatID().getPath());
				buffer_chunk.writeVarInt(stat.value);
			}
		}
	}
	// ==================================================
	static final void read_fileChunks(PacketByteBuf buffer_file, IEditableStatsProvider statsProvider)
			throws IllegalHeaderException, UnsupportedFileVersionException
	{
		//read chunks
		while(buffer_file.readableBytes() > 0)
		{
			//read next chunk's size, and check it
			final int chunkSize = buffer_file.readIntLE();
			if(buffer_file.readableBytes() < chunkSize)
				throw new IllegalHeaderException(
						"chunk size >= " + chunkSize,
						"chunk size == " + buffer_file.readableBytes());
			
			//read the chunk data
			//(creates a view of the original buffer, so it doesn't have to be released separately)
			final var buffer_chunk = new PacketByteBuf(buffer_file.readSlice(chunkSize));
			final var chunkId = buffer_chunk.readString();
			switch(chunkId)
			{
				case "metadata":     read_fileChunk_meta(buffer_chunk, statsProvider); break;
				case "general":      read_fileChunk_general(buffer_chunk, statsProvider); break;
				case "item":         read_fileChunk_item(buffer_chunk, statsProvider); break;
				case "mob":          read_fileChunk_mob(buffer_chunk, statsProvider); break;
				case "player_badge": read_fileChunk_playerBadge(buffer_chunk, statsProvider); break;
				default: break;
			}
		}
	}
	// --------------------------------------------------
	@SuppressWarnings("deprecation")
	private static final void read_fileChunk_meta(PacketByteBuf buffer_chunk, IEditableStatsProvider statsProvider)
	{
		//read display name
		final Text displayName = buffer_chunk.readText();
		statsProvider.setDisplayName(displayName);
		
		//read game profile
		if(buffer_chunk.readableBytes() < 2) return; //compatibility with alpha files
		final @Nullable GameProfile gameProfile = readGameProfile(buffer_chunk);
		statsProvider.setGameProfile(gameProfile);
	}
	
	private static final void read_fileChunk_general(PacketByteBuf buffer_chunk, IEditableStatsProvider statsProvider)
	{
		while(buffer_chunk.readableBytes() > 0)
		{
			//read the next mod id and how many entries it has
			final String modId = buffer_chunk.readString();
			final int entryCount = buffer_chunk.readVarInt();
			
			//read all entries for the corresponding mod id
			for(int i = 0; i < entryCount; i++)
			{
				//read custom stat data
				final String customStatIdPath = buffer_chunk.readString();
				final int customStatValue = buffer_chunk.readVarInt();
				
				//obtain custom stat, and sore its value
				//comment: the fact that the stat itself and its key are both Identifier-s always confuses me
				final Identifier customStatId = new Identifier(modId, customStatIdPath);
				final Identifier customStat = Registries.CUSTOM_STAT.get(customStatId);
				if(customStat == null) continue; //for now, unknown modded stats are ignored
				
				//set stat value
				statsProvider.setStatValue(Stats.CUSTOM.getOrCreateStat(customStat), customStatValue);
			}
		}
	}
	
	private static final void read_fileChunk_item(PacketByteBuf buffer_chunk, IEditableStatsProvider statsProvider)
	{
		while(buffer_chunk.readableBytes() > 0)
		{
			//read the next mod id and how many entries it has
			final String modId = buffer_chunk.readString();
			final int entryCount = buffer_chunk.readVarInt();
			
			//read all entries for the corresponding mod id
			for(int i = 0; i < entryCount; i++)
			{
				//read item stat data
				final String itemIdPath = buffer_chunk.readString();
				final int mined  = buffer_chunk.readVarInt(),
						crafted  = buffer_chunk.readVarInt(),
						used     = buffer_chunk.readVarInt(),
						broken   = buffer_chunk.readVarInt(),
						pickedUp = buffer_chunk.readVarInt(),
						dropped  = buffer_chunk.readVarInt();
				
				//obtain item, and store its stats
				final Identifier itemId = new Identifier(modId, itemIdPath);
				final @Nullable Item item = Registries.ITEM.get(itemId);
				final @Nullable Block block = (item != null) ? Block.getBlockFromItem(item) : null;
				
				if(item == null) continue; //for now, unknown modded stats are ignored
				else
				{
					if(block != null) statsProvider.setStatValue(Stats.MINED, block, mined);
					statsProvider.setStatValue(Stats.CRAFTED,   item, crafted);
					statsProvider.setStatValue(Stats.USED,      item, used);
					statsProvider.setStatValue(Stats.BROKEN,    item, broken);
					statsProvider.setStatValue(Stats.PICKED_UP, item, pickedUp);
					statsProvider.setStatValue(Stats.DROPPED,   item, dropped);
				}
			}
		}
	}
	
	private static final void read_fileChunk_mob(PacketByteBuf buffer_chunk, IEditableStatsProvider statsProvider)
	{
		while(buffer_chunk.readableBytes() > 0)
		{
			//read the next mod id and how many entries it has
			final String modId = buffer_chunk.readString();
			final int entryCount = buffer_chunk.readVarInt();
			
			//read all entries for the corresponding mod id
			for(int i = 0; i < entryCount; i++)
			{
				//read mob stat data
				final String mobIdPath = buffer_chunk.readString();
				final int kills = buffer_chunk.readVarInt();
				final int deaths = buffer_chunk.readVarInt();
				
				//obtain mob, and store its stats
				final Identifier mobId = new Identifier(modId, mobIdPath);
				final @Nullable EntityType<?> entityType = Registries.ENTITY_TYPE.get(mobId);
				
				if(entityType == null) continue; //for now, unknown modded stats are ignored
				else
				{
					statsProvider.setStatValue(Stats.KILLED, entityType, kills);
					statsProvider.setStatValue(Stats.KILLED_BY, entityType, deaths);
				}
			}
		}
	}
	
	private static final void read_fileChunk_playerBadge(PacketByteBuf buffer_chunk, IEditableStatsProvider statsProvider)
	{
		while(buffer_chunk.readableBytes() > 0)
		{
			//read the next mod id and how many entries it has
			final String modId = buffer_chunk.readString();
			final int entryCount = buffer_chunk.readVarInt();
			
			//read all entries for the corresponding mod id
			for(int i = 0; i < entryCount; i++)
			{
				//read player badge stat data
				final String playerBadgeIdPath = buffer_chunk.readString();
				final int value = buffer_chunk.readVarInt();
				
				//obtain mob, and store its stats
				final Identifier playerBadgeId = new Identifier(modId, playerBadgeIdPath);
				statsProvider.setPlayerBadgeValue(playerBadgeId, value);
			}
		}
	}
	// ==================================================
	private static final void writeGameProfile(PacketByteBuf buffer, @Nullable GameProfile gameProfile)
	{
		//if game profile is null, write false for all fields
		if(gameProfile == null)
		{
			buffer.writeBoolean(false);
			buffer.writeBoolean(false);
			return;
		}
		
		//obtain game profile info
		final var uuid  = gameProfile.getId();
		final var name = gameProfile.getName();
		
		//write
		// - first UUID
		if(uuid != null) { buffer.writeBoolean(true); buffer.writeUuid(uuid); }
		else buffer.writeBoolean(false);
		// - then name
		if(name != null) { buffer.writeBoolean(true); buffer.writeString(name); }
		else buffer.writeBoolean(false);
	}
	
	private static final @Nullable GameProfile readGameProfile(PacketByteBuf buffer)
	{
		//first UUID
		final UUID uuid = buffer.readBoolean() ? buffer.readUuid() : null;
		//then name
		final String name = buffer.readBoolean() ? buffer.readString() : null;
		
		//construct the game profile
		if(name == null && uuid == null) return null;
		else return new GameProfile(uuid, name);
	}
	// ==================================================
}