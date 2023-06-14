package io.github.thecsdev.betterstats.client.gui.screen;

import static io.github.thecsdev.betterstats.client.BetterStatsClient.DEBUG_MODE;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.fLiteral;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Objects;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.BetterStatsConfig;
import io.github.thecsdev.betterstats.client.gui.panel.BSPanel;
import io.github.thecsdev.betterstats.client.gui.widget.BSScrollBarWidget;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TFillColorElement;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreen;
import io.github.thecsdev.tcdcommons.api.client.gui.util.HorizontalAlignment;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TConfigGuiBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

public final class BetterStatsConfigScreen extends TScreen
{
	// ==================================================
	public final Screen parent;
	public final BetterStatsConfig config;
	// --------------------------------------------------
	protected TFillColorElement panel_contentPane;
	protected BSCSPanel panel_title;
	protected BSCSPanel panel_config;
	protected BSScrollBarWidget scroll_config;
	// ==================================================
	public BetterStatsConfigScreen(Screen parent)
	{
		super(translatable(BetterStats.getModID())
				.append(" - ").append(translatable("options.title")));
		this.parent = parent;
		this.config = Objects.requireNonNull(BetterStats.getInstance().getConfig());
	}
	public @Override boolean shouldRenderInGameHud() { return false; }
	protected @Override void onClosed() { getClient().setScreen(this.parent); }
	// --------------------------------------------------
	public @Override void renderBackground(MatrixStack matrices)
	{
		if(this.parent != null)
			this.parent.render(matrices, 0, 0, 0);
	}
	// ==================================================
	protected @Override void init()
	{
		//create and add the main panel
		panel_contentPane = new TFillColorElement(0, 0, getTpeWidth(), getTpeHeight());
		panel_contentPane.setColor(-1771805596);
		panel_contentPane.setZOffset(125);
		//panel_contentPane.setScrollPadding(0);
		//panel_contentPane.setScrollFlags(0);
		addTChild(panel_contentPane, false);
		
		//calc
		int h = getTpeHeight();
		int y = h / 8;
		h -= y;
		y /= 2;
		int w = getTpeWidth();
		int x = w;
		w = Math.min(Math.max((int)(w / 2.25), 300), w);
		x -= w;
		x /= 2;
		
		//create the title panel
		panel_title = new BSCSPanel(x, y, w, 20);
		panel_contentPane.addTChild(panel_title, false);
		
		var lbl_title = new TLabelElement(0, 0, panel_title.getTpeWidth(), panel_title.getTpeHeight());
		lbl_title.setText(fLiteral("§6").append(getTitle()));
		lbl_title.setHorizontalAlignment(HorizontalAlignment.CENTER);
		panel_title.addTChild(lbl_title, true);
		
		//create the config panel
		panel_config = new BSCSPanel(x, y + 25, w - 7, h - 25);
		panel_contentPane.addTChild(panel_config, false);
		
		scroll_config = new BSScrollBarWidget(
				panel_config.getTpeEndX() - 1, panel_config.getTpeY(),
				8, panel_config.getTpeHeight(),
				panel_config);
		panel_contentPane.addTChild(scroll_config, false);
		
		//create some config GUI
		var config_builder = new TConfigGuiBuilder(panel_config, null);
		config_builder.addBoolean(
				translatable("betterstats.gui.config.debug_mode"),
				DEBUG_MODE,
				newVal -> DEBUG_MODE = newVal);
		config_builder.addBoolean(translatable("betterstats.gui.config.gui_mob_follow_cursor"),
				this.config.guiMobsFollowCursor,
				newVal -> this.config.guiMobsFollowCursor = newVal);
		config_builder.addButton(TConfigGuiBuilder.TXT_SAVE, btn ->
		{
			config_builder.applyAllConfigChanges();
			this.config.trySaveToFile(true);
			this.close();
		});
	}
	// ==================================================
	public static class BSCSPanel extends BSPanel 
	{
		public BSCSPanel(int x, int y, int width, int height) { super(x, y, width, height); }
		protected @Override void renderBackground(MatrixStack matrices, int mouseX, int mouseY, float deltaTime) {
			fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, -6908266);
		}
	}
	// ==================================================
}