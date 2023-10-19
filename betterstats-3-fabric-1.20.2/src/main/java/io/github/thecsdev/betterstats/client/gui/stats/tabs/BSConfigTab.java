package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import blue.endless.jankson.annotation.Nullable;
import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.BetterStatsConfig;
import io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils;
import io.github.thecsdev.betterstats.api.client.registry.BSStatsTabs;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.tcdcommons.api.client.gui.config.TConfigPanelBuilder;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TButtonWidget;
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
		this.config_builder = TConfigPanelBuilder.builder(panel)
			.addCheckbox(
					translatable("betterstats.api.client.gui.screen.betterstatsconfigscreen.debug_mode"),
					BetterStatsConfig.DEBUG_MODE,
					checkbox -> BetterStatsConfig.DEBUG_MODE = checkbox.getChecked())
			.addCheckbox(
					translatable("betterstats.api.client.gui.screen.betterstatsconfigscreen.gui_mob_follow_cursor"),
					config.guiMobsFollowCursor,
					checkbox -> config.guiMobsFollowCursor = checkbox.getChecked())
			.build(() -> config.trySaveToFile(true));
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