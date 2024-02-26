package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.BetterStatsConfig;
import io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils;
import io.github.thecsdev.betterstats.api.client.registry.BSStatsTabs;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.client.gui.config.TConfigPanelBuilder;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TButtonWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;

public final class BSConfigTab extends StatsTab
{
	// ==================================================
	public static final Text TEXT_TITLE = translatable("options.title");
	// --------------------------------------------------
	private @Nullable TConfigPanelBuilder<?> config_builder;
	// ==================================================
	public final @Override Text getName() { return TEXT_TITLE; }
	// --------------------------------------------------
	public final @Override boolean isAvailable() { return false; }
	// ==================================================
	public final @Override void initStats(StatsInitContext initContext)
	{
		//prepare to init
		final var panel = initContext.getStatsPanel();
		
		//init config gui
		final var config = BetterStats.getInstance().getConfig();
		this.config_builder = TConfigPanelBuilder.builder(panel);
		
		//configs for client-sided features
		this.config_builder.addLabelB(translatable("tcdcommons.client_side")).setTextColor(0xFFFFFF00);
		{
			//debug mode
			this.config_builder.addCheckbox(
					BST.config_debugMode(),
					BetterStatsConfig.DEBUG_MODE,
					checkbox -> BetterStatsConfig.DEBUG_MODE = checkbox.getChecked());
			
			//gui smooth scroll
			this.config_builder.addCheckbox(
						BST.config_guiSmoothScroll(),
						config.guiSmoothScroll,
						checkbox -> config.guiSmoothScroll = checkbox.getChecked());
			this.config_builder.getLastAddedElement().setTooltip(Tooltip.of(BST.config_guiSmoothScroll_tooltip()));
			
			//gui mobs follow cursor
			this.config_builder.addCheckbox(
						BST.config_guiMobsFollowCursor(),
						config.guiMobsFollowCursor,
						checkbox -> config.guiMobsFollowCursor = checkbox.getChecked());
			
			//trust all servers bss network
			this.config_builder.addCheckbox(
						BST.config_trustAllServersBssNet(),
						config.trustAllServersBssNet,
						checkbox -> config.trustAllServersBssNet = checkbox.getChecked());
			this.config_builder.getLastAddedElement().setTooltip(Tooltip.of(BST.config_trustAllServersBssNet_tooltip()));
		}
		
		//configs for server-sided features
		this.config_builder.addLabelB(translatable("tcdcommons.server_side")).setTextColor(0xFFFFFF00);
		{
			//register commands
			this.config_builder.addCheckbox(
					BST.config_registerCommands(),
					config.registerCommands,
					checkbox -> config.registerCommands = checkbox.getChecked());
			
			//enable stat announcement system
			this.config_builder.addCheckbox(
					BST.config_enableSas(),
					config.enableServerSAS,
					checkbox -> config.enableServerSAS = checkbox.getChecked());
		}
		
		//finally, build the config gui
		this.config_builder.build(() -> { try { config.saveToFile(true); } catch (Exception e) { throw new RuntimeException(e); } });
	}
	// --------------------------------------------------
	public final @Override void initFilters(FiltersInitContext initContext)
	{
		StatsTabUtils.initDefaultFilters(initContext);
		
		final var panel = initContext.getFiltersPanel();
		StatsTabUtils.initGroupLabel(panel, TEXT_TITLE);
		
		final var n1 = TConfigPanelBuilder.nextPanelVerticalRect(panel);
		n1.y += 3;
		final var btn_done = new TButtonWidget(n1.x, n1.y, n1.width, 20, translatable("gui.done"));
		btn_done.setOnClick(__ ->
		{
			//this shouldn't happen:
			if(this.config_builder == null) return;
			
			//save and close
			this.config_builder.saveChanges();
			initContext.setSelectedStatsTab(BSStatsTabs.GENERAL);
		});
		panel.addChild(btn_done, false);
		
		final var n2 = TConfigPanelBuilder.nextPanelVerticalRect(panel);
		n2.y += 3;
		final var btn_cancel = new TButtonWidget(n2.x, n2.y, n2.width, 20, translatable("gui.cancel"));
		btn_cancel.setOnClick(__ -> initContext.setSelectedStatsTab(BSStatsTabs.GENERAL));
		panel.addChild(btn_cancel, false);
	}
	// ==================================================
}