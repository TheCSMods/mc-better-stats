package io.github.thecsdev.betterstats.client.gui.panel;

import java.awt.Color;
import java.util.Objects;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.tcdcommons.api.util.TextUtils;
import net.minecraft.network.chat.Component;

public class BSPanel_Downloading extends BSPanel
{
	// ==================================================
	private static final Component DOWNLOADING_STATS_TEXT = TextUtils.fTranslatable("multiplayer.downloadingStats");
	private static final int TEXT_COLOR = Color.yellow.getRGB(); //was 16777215
	// --------------------------------------------------
	protected final BetterStatsScreen betterStats;
	// ==================================================
	public BSPanel_Downloading(BetterStatsScreen bss)
	{
		super(bss.getTpeX(), bss.getTpeY(), bss.getTpeWidth(), bss.getTpeHeight());
		this.betterStats = Objects.requireNonNull(bss, "bss must not be null.");
		setBlitOffset(70);
		setVisible(false);
	}
	// ==================================================
	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float deltaTime)
	{
		super.render(matrices, mouseX, mouseY, deltaTime);
		int cX = (getTpeX() + getTpeEndX()) / 2;
		int cY = (getTpeY() + getTpeEndY()) / 2;
		drawCenteredText(matrices, getTextRenderer(), DOWNLOADING_STATS_TEXT, cX, cY, TEXT_COLOR);
	}
	// ==================================================
}