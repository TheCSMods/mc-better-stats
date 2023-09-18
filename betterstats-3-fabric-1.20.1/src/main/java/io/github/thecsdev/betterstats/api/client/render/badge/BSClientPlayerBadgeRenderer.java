package io.github.thecsdev.betterstats.api.client.render.badge;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.client.badge.BSClientPlayerBadge;
import io.github.thecsdev.tcdcommons.api.client.gui.util.UITexture;
import io.github.thecsdev.tcdcommons.api.client.render.badge.PlayerBadgeRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * A {@link PlayerBadgeRenderer} for {@link BSClientPlayerBadge}s.
 */
public final class BSClientPlayerBadgeRenderer extends PlayerBadgeRenderer<BSClientPlayerBadge>
{
	// ==================================================
	private final BSClientPlayerBadge playerBadge;
	private final @Nullable UITexture icon;
	// ==================================================
	public BSClientPlayerBadgeRenderer(BSClientPlayerBadge playerBadge)
	{
		super(BSClientPlayerBadge.class);
		this.playerBadge = Objects.requireNonNull(playerBadge);
		this.icon = playerBadge.getIcon();
	}
	// ==================================================
	/**
	 * Returns the {@link BSClientPlayerBadge} this
	 * {@link BSClientPlayerBadgeRenderer} corresponds to.
	 */
	public final BSClientPlayerBadge getPlayerBadge() { return this.playerBadge; }
	// ==================================================
	public final @Override void render(
			DrawContext pencil,
			int x, int y, int width, int height,
			int mouseX, int mouseY, float deltaTime)
	{
		if(this.icon != null)
			this.icon.drawTexture(pencil, x, y, width, height);
	}
	// ==================================================
}