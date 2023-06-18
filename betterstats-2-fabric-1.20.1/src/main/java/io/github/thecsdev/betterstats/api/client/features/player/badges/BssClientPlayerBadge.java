package io.github.thecsdev.betterstats.api.client.features.player.badges;

import static io.github.thecsdev.betterstats.client.gui.screen.BetterStatsScreen.BSS_WIDGETS_TEXTURE;

import java.awt.Rectangle;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.tcdcommons.api.client.features.player.badges.ClientPlayerBadge;
import io.github.thecsdev.tcdcommons.api.client.gui.TDrawContext;

/**
 * {@link BetterStats}'s implementation of the {@link ClientPlayerBadge}.<br/>
 * This badge implementation draws textures from {@link BetterStatsScreen#BSS_WIDGETS_TEXTURE}.
 */
public abstract class BssClientPlayerBadge extends ClientPlayerBadge
{
	// ==================================================
	protected final Rectangle uv_coords;
	// ==================================================
	protected BssClientPlayerBadge() { this.uv_coords = new Rectangle(220, 0, 20, 20); }
	// --------------------------------------------------
	protected BssClientPlayerBadge setUVCoords(int x, int y, int w, int h)
	{
		this.uv_coords.x = x;
		this.uv_coords.y = y;
		this.uv_coords.width = w;
		this.uv_coords.height = h;
		return this;
	}
	// ==================================================
	public @Override void renderOnClientScreen(TDrawContext pencil, int x, int y, int w, int h, float deltaTime)
	{
		pencil.setShaderColor(1,1,1,1);
		pencil.drawTexture(
				BSS_WIDGETS_TEXTURE,
				x, y, w, h,
				this.uv_coords.x, this.uv_coords.y,
				this.uv_coords.width, this.uv_coords.height,
				256, 256);
	}
	// ==================================================
}