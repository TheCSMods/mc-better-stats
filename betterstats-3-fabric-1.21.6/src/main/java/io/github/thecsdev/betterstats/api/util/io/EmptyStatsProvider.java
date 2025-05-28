package io.github.thecsdev.betterstats.api.util.io;

import java.util.UUID;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;

import io.github.thecsdev.tcdcommons.api.util.TextUtils;
import net.minecraft.stat.Stat;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

final @Internal class EmptyStatsProvider implements IStatsProvider
{
	// ==================================================
	private static final Text        NULL_NAME = TextUtils.literal("null");
	private static final GameProfile NULL_GP   = new GameProfile(new UUID(0, 0), "null");
	// ==================================================
	public EmptyStatsProvider() {}
	// ==================================================
	public final @Override int getStatValue(Stat<?> stat) { return 0; }
	public final @Override int getPlayerBadgeValue(Identifier badgeId) { return 0; }
	public @Nullable GameProfile getGameProfile() { return NULL_GP; }
	public @Nullable Text getDisplayName() { return NULL_NAME; }
	// ==================================================
}