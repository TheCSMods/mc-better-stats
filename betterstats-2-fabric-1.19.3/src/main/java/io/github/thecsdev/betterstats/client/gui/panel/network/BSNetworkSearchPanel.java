package io.github.thecsdev.betterstats.client.gui.panel.network;

import static io.github.thecsdev.betterstats.client.gui.screen.BetterStatsScreen.BSS_WIDGETS_TEXTURE;
import static io.github.thecsdev.betterstats.client.network.BetterStatsClientNetworkHandler.serverHasBSS;
import static io.github.thecsdev.betterstats.client.network.BetterStatsClientNetworkHandler.enableBSSProtocol;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import io.github.thecsdev.betterstats.client.gui.panel.BSPanel;
import io.github.thecsdev.betterstats.client.gui.screen.BSPlayerLookupScreen;
import io.github.thecsdev.betterstats.client.gui.screen.BetterStatsConfigScreen;
import io.github.thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TTextureElement;
import io.github.thecsdev.tcdcommons.api.client.gui.util.GuiUtils;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TButtonWidget;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.util.math.MatrixStack;

public class BSNetworkSearchPanel extends BSPanel
{
	// ==================================================
	public BSNetworkSearchPanel(int x, int y, int width, int height)
	{
		super(x, y, width, height);
		this.setScrollFlags(0);
		this.setScrollPadding(0);
	}
	// --------------------------------------------------
	public @Override void postRender(MatrixStack matrices, int mouseX, int mouseY, float deltaTime) {}
	protected @Override void renderBackground(MatrixStack matrices, int mouseX, int mouseY, float deltaTime)
	{
		super.renderBackground(matrices, mouseX, mouseY, deltaTime);
		drawOutline(matrices, GuiUtils.applyAlpha(isFocused() ? COLOR_OUTLINE_FOCUSED : BSPanel.COLOR_OUTLINE, getAlpha()));
	}
	// ==================================================
	public final void init(final BetterStatsScreen bss) { clearTChildren(); onInit(bss); }
	protected void onInit(final BetterStatsScreen bss)
	{
		//exit
		var btn_exit = new TButtonWidget(getTpeEndX() - 20, getTpeY(), 20, 20, null, null);
		btn_exit.setDrawsVanillaButton(true);
		btn_exit.setTooltip(translatable("gui.done"));
		btn_exit.setOnClick(btn -> getClient().setScreen(bss.parent));
		addTChild(btn_exit, false);
		
		var img_exit = new TTextureElement(2, 2, 16, 16);
		img_exit.setTexture(BSS_WIDGETS_TEXTURE, 256, 256);
		img_exit.setTextureUVs(0, 60, 20, 20);
		btn_exit.addTChild(img_exit, true);
		
		//options
		var btn_options = new TButtonWidget(btn_exit.getTpeX() - 20, getTpeY(), 20, 20, null, null);
		btn_options.setDrawsVanillaButton(true);
		btn_options.setTooltip(translatable("options.title"));
		btn_options.setOnClick(btn -> getClient().setScreen(new BetterStatsConfigScreen(bss)));
		addTChild(btn_options, false);
		
		var img_options = new TTextureElement(4, 4, 12, 12);
		img_options.setTexture(BSS_WIDGETS_TEXTURE, 256, 256);
		img_options.setTextureUVs(0, 40, 20, 20);
		btn_options.addTChild(img_options, true);
		
		//radio tower (bss network protocol)
		var btn_radio = new TButtonWidget(btn_options.getTpeX() - 20, getTpeY(), 20, 20, null, null);
		btn_radio.setDrawsVanillaButton(true);
		btn_radio.setTooltip(translatable("betterstats.gui.network.btn_radio.tooltip"));
		btn_radio.setOnClick(btn ->
		{
			//disabling
			if(enableBSSProtocol) enableBSSProtocol = false;
			//enabling
			else if(getClient().isInSingleplayer()) enableBSSProtocol = true;
			else
			{
				var confsc_title = translatable("betterstats.hud.accuracy_mode_warning.title");
				var confsc_msg = translatable("betterstats.gui.network.btn_radio.warning");
				BooleanConsumer confsc_callback = (accepted ->
				{
					enableBSSProtocol = accepted;
					getClient().setScreen(bss);
					this.init(bss);
				});
				var confsc = new ConfirmScreen(confsc_callback, confsc_title, confsc_msg);
				getClient().setScreen(confsc);
				return;
			}
			//refresh
			this.init(bss);
		});
		addTChild(btn_radio, false);
		btn_radio.setVisible(!getClient().isInSingleplayer() && serverHasBSS);
		
		var img_radio = new TTextureElement(4, 4, 12, 12);
		img_radio.setTexture(BSS_WIDGETS_TEXTURE, 256, 256);
		img_radio.setTextureUVs(enableBSSProtocol ? 20 : 0, 80, 20, 20);
		btn_radio.addTChild(img_radio, true);
		
		//player stat lookup
		var btn_pstat = new TButtonWidget(btn_radio.getTpeX() - 20, getTpeY(), 20, 20, null, null);
		btn_pstat.setDrawsVanillaButton(true);
		btn_pstat.setTooltip(translatable("betterstats.gui.network.btn_pstat.tooltip"));
		btn_pstat.setOnClick(btn -> getClient().setScreen(new BSPlayerLookupScreen(bss)));
		addTChild(btn_pstat, false);
		btn_pstat.setVisible(btn_radio.getVisible() && enableBSSProtocol);
		
		var img_pstat = new TTextureElement(2, 2, 16, 16);
		img_pstat.setTexture(BSS_WIDGETS_TEXTURE, 256, 256);
		img_pstat.setTextureUVs(20, 60, 20, 20);
		btn_pstat.addTChild(img_pstat, true);
	}
	// ==================================================
}