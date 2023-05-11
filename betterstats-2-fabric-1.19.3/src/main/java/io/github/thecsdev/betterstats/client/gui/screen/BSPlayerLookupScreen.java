package io.github.thecsdev.betterstats.client.gui.screen;

import static io.github.thecsdev.betterstats.client.gui.screen.BetterStatsScreen.BSS_WIDGETS_TEXTURE;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;

import io.github.thecsdev.tcdcommons.api.client.gui.other.TFillColorElement;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TTextureElement;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreen;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TButtonWidget;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TTextFieldWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

public class BSPlayerLookupScreen extends TScreen
{
	// ==================================================
	public final Screen parent;
	// --------------------------------------------------
	protected TFillColorElement panel_contentPane;
	protected BetterStatsConfigScreen.BSCSPanel panel_search;
	protected TTextFieldWidget input_playerName;
	// ==================================================
	public BSPlayerLookupScreen(Screen parent)
	{
		super(translatable("betterstats.gui.network.btn_pstat.tooltip"));
		this.parent = parent;
	}
	public @Override boolean shouldRenderInGameHud() { return false; }
	protected @Override void onClosed() { getClient().setScreen(this.parent); }
	// --------------------------------------------------
	public @Override void renderBackground(MatrixStack matrices)
	{
		if(this.parent != null)
			this.parent.render(matrices, 0, 0, 0);
	}
	// --------------------------------------------------
	protected @Override void init()
	{
		//create and add the main panel
		panel_contentPane = new TFillColorElement(0, 0, getTpeWidth(), getTpeHeight());
		panel_contentPane.setColor(-1771805596);
		panel_contentPane.setZOffset(10);
		//panel_contentPane.setScrollPadding(0);
		//panel_contentPane.setScrollFlags(0);
		addTChild(panel_contentPane, false);
		
		//calc
		int h = 30;
		int y = (getTpeHeight() / 2) - (h / 2);
		int w = getTpeWidth();
		int x = w;
		w = Math.min(Math.max((int)(w / 2.25), 300), w);
		x -= w;
		x /= 2;
		
		//create the panel
		panel_search = new BetterStatsConfigScreen.BSCSPanel(x, y, w, 30);
		panel_search.setScrollPadding(0);
		panel_contentPane.addTChild(panel_search, false);
		
		//player stat lookup field
		input_playerName = new TTextFieldWidget(
				panel_search.getTpeX() + 5, panel_search.getTpeY() + 5,
				panel_search.getTpeWidth() - 60, 20);
		panel_contentPane.addTChild(input_playerName, false);
		
		//player stat lookup button
		var btn_pstat = new TButtonWidget(
				input_playerName.getTpeEndX() + 5, input_playerName.getTpeY(),
				20, 20, null, null);
		btn_pstat.setDrawsVanillaButton(true);
		btn_pstat.setTooltip(translatable("betterstats.gui.network.btn_pstat.tooltip"));
		btn_pstat.setOnClick(btn ->
		{
			var bssParent = getBSSParentScreen();
			var targetProfile = getInputTargetProfile();
			getClient().setScreen(new BetterStatsScreen(bssParent, targetProfile));
		});
		panel_contentPane.addTChild(btn_pstat, false);
		
		var img_pstat = new TTextureElement(2, 2, 16, 16);
		img_pstat.setTexture(BSS_WIDGETS_TEXTURE, 256, 256);
		img_pstat.setTextureUVs(20, 60, 20, 20);
		btn_pstat.addTChild(img_pstat, true);
		
		//exit
		var btn_exit = new TButtonWidget(btn_pstat.getTpeEndX() + 5, btn_pstat.getTpeY(), 20, 20, null, null);
		btn_exit.setDrawsVanillaButton(true);
		btn_exit.setTooltip(translatable("gui.cancel"));
		btn_exit.setOnClick(btn -> getClient().setScreen(this.parent));
		panel_contentPane.addTChild(btn_exit, false);
		
		var img_exit = new TTextureElement(2, 2, 16, 16);
		img_exit.setTexture(BSS_WIDGETS_TEXTURE, 256, 256);
		img_exit.setTextureUVs(0, 60, 20, 20);
		btn_exit.addTChild(img_exit, true);
		
		//focus input field initially
		this.setFocusedTChild(input_playerName);
	}
	// ==================================================
	/**
	 * If the {@link #parent} {@link Screen} is a {@link BetterStatsScreen},
	 * returns the parent of that {@link BetterStatsScreen}.
	 * @see BetterStatsScreen#parent
	 */
	public Screen getBSSParentScreen()
	{
		return (this.parent instanceof BetterStatsScreen) ?
				((BetterStatsScreen)this.parent).parent : null;
	}
	// --------------------------------------------------
	public @Nullable GameProfile getInputTargetProfile()
	{
		if(this.input_playerName == null)
			return null;
		var input = this.input_playerName.getText().trim();
		if(StringUtils.isBlank(input))
			return null;
		return new GameProfile(null, input);
	}
	// ==================================================
}