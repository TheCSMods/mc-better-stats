package io.github.thecsdev.betterstats.api.util.io;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.tcdcommons.api.util.exceptions.UnsupportedFileVersionException;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.stat.Stat;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * An {@link IEditableStatsProvider} whose statistics are loaded into and
 * held in the memory (aka RAM), hence the name {@link RAMStatsProvider}.
 */
public final class RAMStatsProvider implements IEditableStatsProvider
{
	// ==================================================
	protected @Nullable Text displayName;
	// --------------------------------------------------
	protected final Object2IntMap<Stat<?>> statMap = Object2IntMaps.synchronize(new Object2IntOpenHashMap<>());
	protected final Set<Identifier> playerBadges = Collections.synchronizedSet(new HashSet<>());
	// ==================================================
	public RAMStatsProvider() { this.statMap.defaultReturnValue(0); }
	
	/**
	 * Creates a {@link RAMStatsProvider} instance, after which
	 * {@link StatsProviderIO#read(PacketByteBuf, IEditableStatsProvider)} is called.
	 * @param buffer The {@link PacketByteBuf} to read from.
	 * @param releaseBuffer After reading, call {@link PacketByteBuf#release()}?
	 * @apiNote {@link PacketByteBuf#release()} will get called when requested, even when an {@link Exception} is raised.
	 */
	public RAMStatsProvider(PacketByteBuf buffer, boolean releaseBuffer)
			throws NullPointerException, IllegalHeaderException, UnsupportedFileVersionException
	{
		this();
		try { StatsProviderIO.read(Objects.requireNonNull(buffer), this); }
		finally { if(releaseBuffer && buffer.refCnt() > 0) buffer.release(); }
	}
	// ==================================================
	public final @Override Text getDisplayName() { return this.displayName; }
	public final @Override void setDisplayName(Text displayName)
	{
		if(displayName == null) displayName = literal("-");
		this.displayName = displayName;
	}
	// --------------------------------------------------
	public final @Override int getStatValue(Stat<?> stat) { return this.statMap.getInt(stat); }
	public final @Override void setStatValue(Stat<?> stat, int value) { this.statMap.put(stat, value); }
	// --------------------------------------------------
	public final @Override Iterator<Identifier> getPlayerBadgeIterator() { return this.playerBadges.iterator(); }
	public final @Override boolean addPlayerBadge(Identifier badgeId) { return this.playerBadges.add(badgeId); }
	public final @Override boolean removePlayerBadge(Identifier badgeId) { return this.playerBadges.remove(badgeId); }
	// ==================================================
	public final Object2IntMap<Stat<?>> getStatMap() { return this.statMap; }
	public final Set<Identifier> getPlayerBadges() { return this.playerBadges; }
	// ==================================================
}