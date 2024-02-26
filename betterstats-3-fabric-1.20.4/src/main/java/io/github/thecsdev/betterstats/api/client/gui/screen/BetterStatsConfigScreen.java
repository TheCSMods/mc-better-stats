package io.github.thecsdev.betterstats.api.client.gui.screen;

import static io.github.thecsdev.betterstats.BetterStats.getModID;
import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.BetterStatsConfig;
import io.github.thecsdev.betterstats.api.client.gui.widget.ScrollBarWidget;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.client.gui.config.TConfigPanelBuilder;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TFillColorElement;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreen;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreenPlus;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TButtonWidget;
import io.github.thecsdev.tcdcommons.api.client.util.interfaces.IParentScreenProvider;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;
import io.github.thecsdev.tcdcommons.client.TCDCommonsClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;

/**
 * A config {@link TScreen} for the {@link BetterStats} mod.
 */
public final class BetterStatsConfigScreen extends TScreenPlus implements IParentScreenProvider
{
	// ==================================================
	private final @Nullable Screen parent;
	// ==================================================
	public BetterStatsConfigScreen(@Nullable Screen parent)
	{
		super(translatable(getModID()).append(" - ").append(translatable("options.title")));
		this.parent = parent;
	}
	// --------------------------------------------------
	public final @Override Screen getParentScreen() { return this.parent; }
	// --------------------------------------------------
	public final @Override void close() { MC_CLIENT.setScreen(this.parent); }
	public final @Override void renderBackground(TDrawContext pencil)
	{
		if(this.parent != null) this.parent.render(pencil, pencil.mouseX, pencil.mouseY, pencil.deltaTime);
		else super.renderBackground(pencil);
	}
	public final @Override boolean shouldRenderInGameHud() { return false; }
	// ==================================================
	protected final @Override void init()
	{
		final var contentPane = new TFillColorElement(0, 0, getWidth(), getHeight());
		contentPane.setColor(0x44FFFFFF);
		contentPane.setZOffset(TCDCommonsClient.MAGIC_ITEM_Z_OFFSET);
		addChild(contentPane, false);
		
		final int panelW = (int) ((float)getWidth() / 2f);
		final int panelX = (getWidth() / 2) - (panelW / 2);
		final int panelH = getHeight() - 40;
		final int panelY = (getHeight() / 2) - (panelH / 2);
		
		final var panel = new TPanelElement(panelX, panelY, panelW, panelH);
		panel.setScrollFlags(0);
		panel.setScrollPadding(0);
		panel.setBackgroundColor(0xFF555555);
		panel.setOutlineColor(-16777216);
		contentPane.addChild(panel, false);
		
		final var panel_title = new TPanelElement(0, 0, panelW, 20);
		panel_title.setScrollFlags(0);
		panel_title.setScrollPadding(0);
		panel_title.setOutlineColor(panel.getOutlineColor());
		panel.addChild(panel_title, true);
		
		final var lbl_title = new TLabelElement(0, 0, panel_title.getWidth(), panel_title.getHeight());
		lbl_title.setText(translatable(getModID()));
		lbl_title.setTextHorizontalAlignment(HorizontalAlignment.CENTER);
		panel_title.addChild(lbl_title, true);
		
		final var panel_config = new TPanelElement(0, 20, panelW - 8, panel.getHeight() - (20 + 30));
		panel_config.setScrollFlags(TPanelElement.SCROLL_VERTICAL);
		panel_config.setScrollPadding(10);
		panel_config.setBackgroundColor(0);
		panel_config.setOutlineColor(0);
		panel.addChild(panel_config, true);
		
		final var scroll_config = new ScrollBarWidget(
				panel_config.getEndX(), panel_config.getY(),
				8, panel_config.getHeight(),
				panel_config);
		panel.addChild(scroll_config, false);
		
		final var panel_action = new TPanelElement(panel_config.getX(), panel_config.getEndY(), panelW, 30);
		panel_action.setScrollFlags(0);
		panel_action.setScrollPadding(0);
		panel_action.setBackgroundColor(panel_title.getBackgroundColor());
		panel_action.setOutlineColor(panel.getOutlineColor());
		panel.addChild(panel_action, false);
		
		final var config = BetterStats.getInstance().getConfig();
		final var config_builder = TConfigPanelBuilder.builder(panel_config);
		{
			//configs for client-sided features
			config_builder.addLabelB(translatable("tcdcommons.client_side")).setTextColor(0xFFFFFF00);
			{
				//debug mode
				config_builder.addCheckbox(
						BST.config_debugMode(),
						BetterStatsConfig.DEBUG_MODE,
						checkbox -> BetterStatsConfig.DEBUG_MODE = checkbox.getChecked());
				
				//gui smooth scroll
				config_builder.addCheckbox(
							BST.config_guiSmoothScroll(),
							config.guiSmoothScroll,
							checkbox -> config.guiSmoothScroll = checkbox.getChecked());
				config_builder.getLastAddedElement().setTooltip(Tooltip.of(BST.config_guiSmoothScroll_tooltip()));
				
				//gui mobs follow cursor
				config_builder.addCheckbox(
							BST.config_guiMobsFollowCursor(),
							config.guiMobsFollowCursor,
							checkbox -> config.guiMobsFollowCursor = checkbox.getChecked());
				
				//trust all servers bss network
				config_builder.addCheckbox(
							BST.config_trustAllServersBssNet(),
							config.trustAllServersBssNet,
							checkbox -> config.trustAllServersBssNet = checkbox.getChecked());
				config_builder.getLastAddedElement().setTooltip(Tooltip.of(BST.config_trustAllServersBssNet_tooltip()));
			}
			
			//configs for server-sided features
			config_builder.addLabelB(translatable("tcdcommons.server_side")).setTextColor(0xFFFFFF00);
			{
				//register commands
				config_builder.addCheckbox(
						BST.config_registerCommands(),
						config.registerCommands,
						checkbox -> config.registerCommands = checkbox.getChecked());
				
				//enable stat announcement system
				config_builder.addCheckbox(
						BST.config_enableSas(),
						config.enableServerSAS,
						checkbox -> config.enableServerSAS = checkbox.getChecked());
			}
			
			//finally, build the config gui
			config_builder.build(() -> { try { config.saveToFile(true); } catch (Exception e) { throw new RuntimeException(e); } });
		}
		
		final var btn_actionCancel = new TButtonWidget(
				5, 5, (panelW / 2) - 7, 20,
				translatable("gui.cancel"),
				__ -> close());
		final var btn_actionDone = new TButtonWidget(
				panel_action.getEndX() - btn_actionCancel.getWidth() - 5, panel_action.getY() + 5,
				btn_actionCancel.getWidth(), 20,
				translatable("gui.done"),
				__ -> { config_builder.saveChanges(); close(); });
		panel_action.addChild(btn_actionCancel, true);
		panel_action.addChild(btn_actionDone, false);
	}
	// ==================================================
}