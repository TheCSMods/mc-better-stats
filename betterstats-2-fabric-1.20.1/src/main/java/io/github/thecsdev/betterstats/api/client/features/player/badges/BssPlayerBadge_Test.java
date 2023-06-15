package io.github.thecsdev.betterstats.api.client.features.player.badges;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.tcdcommons.api.features.player.badges.PlayerBadge;
import io.github.thecsdev.tcdcommons.api.util.TextUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * A test {@link PlayerBadge} used for testing the
 * entire {@link PlayerBadge} system.
 */
public final class BssPlayerBadge_Test extends PlayerBadge
{
	// ==================================================
	public static final Identifier BADGE_ID = new Identifier(BetterStats.getModID(), "test");
	public static final BssPlayerBadge_Test instance = new BssPlayerBadge_Test();
	// ==================================================
	protected BssPlayerBadge_Test() { super(); }
	public @Override boolean shouldSave() { return true; }
	// ==================================================
	public @Override Text getName() { return TextUtils.literal("Test badge"); }
	public @Override Text getDescription() { return TextUtils.literal("Just testing..."); }
	// ==================================================
	public @Override void renderOnClientScreen(DrawContext pencil, int x, int y, int w, int h, float deltaTime)
	{
		pencil.fill(x, y, x + w, y + h, -1);
	}
	// ==================================================
}