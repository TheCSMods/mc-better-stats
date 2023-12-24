package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.betterstats.BetterStats.getModID;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.betterstats.client.gui.widget.CreditsTabPersonWidget;
import io.github.thecsdev.tcdcommons.api.client.gui.config.TConfigPanelBuilder;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class BSCreditsTab extends StatsTab
{
	// ==================================================
	public static final Text TEXT_TITLE = translatable("credits_and_attribution.button.credits");
	// ==================================================
	public final @Override Text getName() { return TEXT_TITLE; }
	public final @Override boolean isAvailable() { return false; }
	// --------------------------------------------------
	public final @Override void initFilters(FiltersInitContext initContext) { StatsTabUtils.initDefaultFilters(initContext); }
	// ==================================================
	public final @Override void initStats(StatsInitContext initContext)
	{
		final var panel = initContext.getStatsPanel();
		
		final var lbl_title = StatsTabUtils.initGroupLabel(panel, TEXT_TITLE);
		lbl_title.setSize(lbl_title.getWidth(), lbl_title.getHeight() * 2);
		lbl_title.setTextColor(TDrawContext.DEFAULT_TEXT_COLOR);
		lbl_title.setTextScale(1.8f);
		lbl_title.setTextHorizontalAlignment(HorizontalAlignment.CENTER);
		
		//group label for translators
		initGroupLabel(panel, translatable("betterstats.contributors.title").formatted(Formatting.YELLOW));
		{
			final var contributors = getContributors();
			boolean highlight = true;
			int iteration = 1;
			
			for(final Person contributor : contributors)
			{
				//keep track of the loop iterations
				highlight = !highlight;
				iteration++;
				
				//create gui elements
				final var n1 = TConfigPanelBuilder.nextPanelVerticalRect(panel);
				final var ctpw = new CreditsTabPersonWidget(n1.x, n1.y, n1.width, contributor, iteration <= 10);
				ctpw.setBackgroundColor(highlight ? 0x44000000 : 0x22000000);
				panel.addChild(ctpw, false);
			}
		}
	}
	// --------------------------------------------------
	private static final void initGroupLabel(TPanelElement panel, Text text)
	{
		final var n1 = TConfigPanelBuilder.nextPanelVerticalRect(panel);
		final var panel_tGroup = new TPanelElement(n1.x, n1.y, n1.width, 20);
		panel_tGroup.setBackgroundColor(0xff353535);
		panel_tGroup.setOutlineColor(0xff000000);
		panel.addChild(panel_tGroup, false);
		//panel_tGroup.addChild(new TFillColorElement(2, 2, 16, 16, panel_tGroup.getOutlineColor()));
		final var lbl = new TLabelElement(
				panel_tGroup.getHeight() + 5, 0,
				panel_tGroup.getWidth(), panel_tGroup.getHeight(),
				text);
		lbl.setTextHorizontalAlignment(HorizontalAlignment.LEFT);
		lbl.setTextScale(0.8f);
		panel_tGroup.addChild(lbl);
	}
	// ==================================================
	/**
	 * Returns an array of {@link Person}s who are credited as contributors of this mod.
	 */
	public static final Person[] getContributors()
	{
		return FabricLoader.getInstance().getModContainer(getModID()).get()
				.getMetadata().getContributors().stream()
				.sorted((p1, p2) -> p1.getName().compareTo(p2.getName()))
				.toArray(Person[]::new);
	}
	// ==================================================
}