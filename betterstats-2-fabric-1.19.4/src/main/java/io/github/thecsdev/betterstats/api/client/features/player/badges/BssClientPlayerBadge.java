package io.github.thecsdev.betterstats.api.client.features.player.badges;

import static io.github.thecsdev.betterstats.client.gui.screen.BetterStatsScreen.BSS_WIDGETS_TEXTURE;

import java.awt.Rectangle;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.tcdcommons.api.client.features.player.badges.ClientPlayerBadge;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;

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
	public @Override void renderOnClientScreen(MatrixStack matrices, int x, int y, int w, int h, float deltaTime)
	{
		//apply shader stuff
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderTexture(0, BSS_WIDGETS_TEXTURE);
		RenderSystem.setShaderColor(1,1,1,1);
		
		//draw the texture
		DrawableHelper.drawTexture(matrices,
				x, y, w, h,
				this.uv_coords.x, this.uv_coords.y,
				this.uv_coords.width, this.uv_coords.height,
				256, 256);
	}
	// ==================================================
}