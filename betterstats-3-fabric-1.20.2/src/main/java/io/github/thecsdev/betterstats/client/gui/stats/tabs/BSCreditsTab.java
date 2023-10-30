package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.tcdcommons.api.client.gui.config.TConfigPanelBuilder;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.util.GuiUtils;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TButtonWidget;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class BSCreditsTab extends StatsTab
{
	// ==================================================
	public static final Text TEXT_TITLE = translatable("credits_and_attribution.button.credits");
	public static final Text TEXT_OPEN_LINK = translatable("mco.notification.visitUrl.buttonText.default");
	// ==================================================
	public final @Override Text getName() { return TEXT_TITLE; }
	// --------------------------------------------------
	public final @Override boolean isAvailable() { return false; }
	// ==================================================
	public final @Override void initStats(StatsInitContext initContext)
	{
		//"mco.notification.visitUrl.buttonText.default": "Open link",
		final var panel = initContext.getStatsPanel();
		
		final var lbl_title = StatsTabUtils.initGroupLabel(panel, TEXT_TITLE);
		lbl_title.setTextColor(TDrawContext.DEFAULT_TEXT_COLOR);
		lbl_title.setTextHorizontalAlignment(HorizontalAlignment.CENTER);
		
		StatsTabUtils.initGroupLabel(panel, translatable("betterstats.translators.title"));
		{
			var translators = getTranslatorEntries();
			if(translators.length == 0) translators = new String[] { "-" };
			for(final var translator : translators)
			{
				final var n1 = TConfigPanelBuilder.nextPanelVerticalRect(panel);
				final var lbl_t = new TLabelElement(n1.x, n1.y, n1.width, n1.height, literal(translator));
				panel.addChild(lbl_t, false);
				
				final @Nullable var visit_url = translatorEntryToUrl(translator);
				final var btn_visit = new TButtonWidget(n1.x + n1.width - 100, n1.y, 100, n1.height);
				btn_visit.setText(TEXT_OPEN_LINK);
				btn_visit.setEnabled(visit_url != null);
				btn_visit.setOnClick(btn -> GuiUtils.showUrlPrompt(visit_url, false));
				panel.addChild(btn_visit, false);
			}
		}
	}
	// --------------------------------------------------
	public final @Override void initFilters(FiltersInitContext initContext)
	{
		StatsTabUtils.initDefaultFilters(initContext);
	}
	// ==================================================
	public static final String[] getTranslatorEntries()
	{
		final String translators = translatable("betterstats.translators").getString();
		if(StringUtils.isBlank(translators))
			return new String[] {};
		
		var names = new ArrayList<String>(Arrays.asList(translators.split(",")));
		names.removeIf(name -> StringUtils.isBlank(name));
		names = names.stream()
				.map(name -> name.trim())
				.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
		
		return names.toArray(new String[] {});
	}
	// --------------------------------------------------
	public static final @Nullable String translatorEntryToUrl(String translator)
	{
		if(StringUtils.isBlank(translator) || Objects.equals(translator, "-"))
			return null;
		else if(translator.startsWith("http://") || translator.startsWith("https://"))
			return translator;
		else if(translator.contains(Character.toString(Identifier.NAMESPACE_SEPARATOR)))
			return ("https://github.com/" + new Identifier(translator).getNamespace());
		else
			return ("https://github.com/" + translator);
	}
	// ==================================================
}