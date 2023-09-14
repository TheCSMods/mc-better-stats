package io.github.thecsdev.betterstats.api.util.io;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static java.nio.charset.StandardCharsets.US_ASCII;

import java.util.Objects;
import java.util.stream.Collectors;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import io.github.thecsdev.betterstats.api.util.stats.SUItemStat;
import io.github.thecsdev.betterstats.api.util.stats.SUMobStat;
import io.github.thecsdev.tcdcommons.api.badge.PlayerBadgeHandler;
import io.github.thecsdev.tcdcommons.api.util.exceptions.UnsupportedFileVersionException;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * A utility class for reading and writing {@link IStatsProvider} data.
 */
public final class StatsProviderIO extends Object
{
	// ==================================================
	private StatsProviderIO() {}
	// --------------------------------------------------
	private static final @Internal String MC_ID = new Identifier("air").getNamespace();
	// --------------------------------------------------
	public static final String FILE_EXTENSION = "mcbs"; //Note: as per RIFF rules, length MUST BE 4!
	
	/**
	 * The current {@link #FILE_EXTENSION}'s "RIFF" data format version.
	 * @apiNote Changing the {@link #FILE_VERSION} is more of a "last resort"-type thing,
	 * where the overall structure of the RIFF format has to change and/or for when the game's
	 * stats system is modified by the game developers. When possible, instead of increasing the
	 * version number to change a chunk's structure, define a new chunks with its new ID, while
	 * still maintaining support for the older chunks.
	 * @apiNote TLDR; Only increase if backwards compatibility is impossible.
	 */
	public static final int FILE_VERSION = 1;
	// ==================================================
	/**
	 * Writes an {@link IStatsProvider}'s statistics data to a given {@link PacketByteBuf}.
	 * @param statsProvider The data to write.
	 * @param buffer The buffer to write the data to.
	 * @apiNote Uses the "RIFF" file format.
	 */
	public static final void write(IStatsProvider statsProvider, PacketByteBuf buffer)
	throws NullPointerException
	{
		//null checks
		Objects.requireNonNull(statsProvider);
		Objects.requireNonNull(buffer);
		
		//write data
		buffer.writeBytes("RIFF".getBytes(US_ASCII));
		write_file(statsProvider, buffer);
	}
	// --------------------------------------------------
	private static final void write_file(IStatsProvider statsProvider, PacketByteBuf buffer)
	{
		//create the buffer
		final var buffer_file = new PacketByteBuf(Unpooled.buffer());
		
		//write the file extension
		if(FILE_EXTENSION.length() != 4) //RIFF specification requires length of 4
			throw new IllegalStateException("Illegal file extension length, must be 4! Current value: " + FILE_EXTENSION);
		buffer_file.writeBytes(FILE_EXTENSION.toUpperCase().getBytes(US_ASCII));
		
		//write the file version
		buffer_file.writeIntLE(FILE_VERSION);
		
		//write chunks
		write_fileChunk("metadata", buffer_file, statsProvider);
		write_fileChunk("general", buffer_file, statsProvider);
		write_fileChunk("item", buffer_file, statsProvider);
		write_fileChunk("mob", buffer_file, statsProvider);
		write_fileChunk("player_badge", buffer_file, statsProvider);
		
		//finally, write the file chunk and release the file buffer
		buffer.writeIntLE(buffer_file.readableBytes());
		buffer.writeBytes(buffer_file);
		buffer_file.release();
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
	
	private static final void write_fileChunk_meta(PacketByteBuf buffer_chunk, IStatsProvider statsProvider)
	{
		@Nullable Text displayName = statsProvider.getDisplayName();
		if(displayName == null) displayName = literal("-");
		buffer_chunk.writeText(displayName);
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
		final var customStatIDs = Lists.newArrayList(Registries.CUSTOM_STAT.iterator());
		final var customStatsMap = customStatIDs.stream()
				.filter(statId -> statsProvider.getStatValue(Stats.CUSTOM.getOrCreateStat(Registries.CUSTOM_STAT.get(statId))) != 0)
				.collect(Collectors.groupingBy(statId -> statId.getNamespace()));
		
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
			for(final Identifier customStatId : groupStats)
			{
				//obtain the custom stat and its value
				final Stat<Identifier> customStat = Stats.CUSTOM.getOrCreateStat(Registries.CUSTOM_STAT.get(customStatId));
				final int customStatValue = statsProvider.getStatValue(customStat);
				
				//write the custom stat registry id path and its value
				buffer_chunk.writeString(customStatId.getPath());
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
		//obtain a map of player badge stats
		final var stats = Lists.newArrayList(statsProvider.getPlayerBadgeIterator());
		final var statsMap = PlayerBadgeHandler.toMapByModId(stats);
		
		//iterate groups, and write their data
		for(final var entry : statsMap.entrySet())
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
				buffer_chunk.writeString(stat.getPath());
				//quantity, in case i ever feel like implementing something like this:
				buffer_chunk.writeVarInt(1); //for now, a player can have just one of each
			}
		}
	}
	// ==================================================
	/**
	 * Reads an {@link IStatsProvider}'s data that has been written to a
	 * {@link PacketByteBuf}, and converts it to an {@link IStatsProvider}.
	 * @param buffer The buffer to read data from.
	 * @param statsProvider The {@link IEditableStatsProvider} to load the data into.
	 * @throws IllegalHeaderException If the "RIFF" header is missing.
	 */
	public static final void read(PacketByteBuf buffer, IEditableStatsProvider statsProvider)
	throws IllegalHeaderException, UnsupportedFileVersionException
	{
		//create a new editable stats provider
		if(buffer.readableBytes() < 8)
			throw new IllegalHeaderException("chunk size >= 8", "chunk size == " + buffer.readableBytes());
		
		//first, mark the reading index, for error correction purposes
		buffer.markReaderIndex();
		
		//begin reading
		try
		{
			//read RIFF
			final String RIFF = buffer.readSlice(4).toString(US_ASCII);
			if(!"RIFF".equalsIgnoreCase(RIFF))
				throw new IllegalHeaderException("RIFF", RIFF);
			
			//read file chunk length
			final int fileLength = buffer.readIntLE();
			if(buffer.readableBytes() < fileLength)
				throw new IllegalHeaderException("chunk size >= " + fileLength, "chunk size == " + buffer.readableBytes());
			
			//begin reading file
			//(creates a view of the original buffer, so it doesn't have to be released separately)
			final var buffer_file = new PacketByteBuf(buffer.readSlice(fileLength));
			read_file(buffer_file, statsProvider);
		}
		catch(IllegalHeaderException | UnsupportedFileVersionException exc) { buffer.resetReaderIndex(); throw exc; }
	}
	// --------------------------------------------------
	private static final void read_file(PacketByteBuf buffer_file, IEditableStatsProvider statsProvider)
	throws IllegalHeaderException, UnsupportedFileVersionException
	{		
		//read file extension
		final String FEXT = buffer_file.readSlice(4).toString(US_ASCII);
		if(!FILE_EXTENSION.equalsIgnoreCase(FEXT))
			throw new IllegalHeaderException(FILE_EXTENSION, FEXT);
		
		//read file version
		final int fileVersion = buffer_file.readIntLE();
		if(fileVersion != FILE_VERSION)
			throw new UnsupportedFileVersionException(Integer.toString(fileVersion));
		
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
	private static final void read_fileChunk_meta(PacketByteBuf buffer_chunk, IEditableStatsProvider statsProvider)
	{
		//read display name
		final Text displayName = buffer_chunk.readText();
		statsProvider.setDisplayName(displayName);
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
				//read badge ID path, and obtain the Identifier
				final var badgeId = new Identifier(modId, buffer_chunk.readString());
				statsProvider.addPlayerBadge(badgeId);
			}
		}
	}
	// ==================================================
}