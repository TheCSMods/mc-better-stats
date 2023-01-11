package io.github.thecsdev.betterstats.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.tcdcommons.api.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class BSButtonWidget extends Button
{
	// ==================================================
	protected static final Component TXT = TextUtils.fTranslatable("gui.stats");
	protected static final Button.OnPress ON_PRESS = btn ->
	{
		final Minecraft client = Minecraft.getInstance();
		client.setScreen(new BetterStatsScreen(client.screen));
	};
	// ==================================================
	public BSButtonWidget(int x, int y, int width, int height)
	{
		super(x, y, width, height, TXT, ON_PRESS, Button.NO_TOOLTIP);
	}
	// ==================================================
	public Button btn_backToGame = null;
	// --------------------------------------------------
	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta)
	{
		super.render(matrices, mouseX, mouseY, delta);
		if(btn_backToGame != null)
			this.y = btn_backToGame.y + btn_backToGame.getHeight() + 4;
	}
	// ==================================================
}