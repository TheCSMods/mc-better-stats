package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.client.gui.util.StatsTabUtils;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.tcdcommons.api.client.gui.config.TConfigPanelBuilder;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TFillColorElement;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TTextureElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.util.GuiUtils;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.client.gui.util.UIExternalTexture;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TButtonWidget;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;
import io.github.thecsdev.tcdcommons.api.util.io.repo.RepositoryUserInfo;
import io.github.thecsdev.tcdcommons.api.util.io.repo.github.GitHubUserInfo;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
		lbl_title.setSize(lbl_title.getWidth(), lbl_title.getHeight() * 2);
		lbl_title.setTextColor(TDrawContext.DEFAULT_TEXT_COLOR);
		lbl_title.setTextScale(1.8f);
		lbl_title.setTextHorizontalAlignment(HorizontalAlignment.CENTER);
		
		//group label for translators
		initGroupLabel(panel, translatable("betterstats.translators.title").formatted(Formatting.YELLOW));
		{
			var translators = getTranslatorEntries();
			if(translators.length == 0) translators = new String[] { "-" };
			
			boolean highlight = true;
			int iteration = 0;
			for(final String translator : translators)
			{
				highlight = !highlight;
				iteration++;
				
				final var n1 = TConfigPanelBuilder.nextPanelVerticalRect(panel);
				final var panel_t = new TFillColorElement(n1.x, n1.y, n1.width, 20);
				panel_t.setColor(highlight ? 0x44000000 : 0x22000000);
				panel.addChild(panel_t, false);
				
				final var icon = new TTextureElement(2, 2, 16, 16);
				panel_t.addChild(icon, true);
				
				final var nameLabel = new TLabelElement(25, 0, panel_t.getWidth(), 20, literal(translator));
				nameLabel.setTextScale(0.8f);
				panel_t.addChild(nameLabel, true);
				
				final @Nullable String visitUrl = translatorEntryToUrl(translator);
				final var visitBtn = new TButtonWidget(
						panel_t.getEndX() - 102, panel_t.getY() + 2,
						100, 16);
				{
					final var visitBtnLbl = new TLabelElement(0, 0, visitBtn.getWidth(), visitBtn.getHeight());
					visitBtnLbl.setText(TEXT_OPEN_LINK);
					visitBtnLbl.setTextHorizontalAlignment(HorizontalAlignment.CENTER);
					visitBtnLbl.setTextScale(0.7f);
					visitBtn.addChild(visitBtnLbl, true);
				}
				visitBtn.setEnabled(visitUrl != null);
				visitBtn.setOnClick(btn -> GuiUtils.showUrlPrompt(visitUrl, false));
				panel_t.addChild(visitBtn, false);
				
				// ----- Handle GitHub info
				//FIXME - If `init()` is spammed, so will the API. Pre-load instead?
				if(!(visitUrl != null && iteration <= 3)) continue;
				@Nullable URL url = null;
				try { url = new URL(visitUrl); } catch(MalformedURLException mue) { continue; }
				
				//process the url
				final var host = url.getHost();
				if(!(Objects.equals(host, "github.com") || Objects.equals(host, "www.github.com")))
					continue;
				final var urlPathParts = url.getPath().split("/");
				if(urlPathParts.length < 2) continue;
				
				//fetch GitHub user info
				GitHubUserInfo.getUserInfoAsync(
						urlPathParts[1], MC_CLIENT,
						userInfo ->
						{
							//handle display name
							nameLabel.setText(getDisplayNameFromGHUser(userInfo));
							
							//handle tooltip
							final var ttt = literal("")
									.append(literal(nameLabel.getText().getString()).formatted(Formatting.YELLOW));
							final @Nullable Text descr = userInfo.getDescription();
							if(descr != null) ttt
								.append("\n")
								.append(literal(descr.getString()).formatted(Formatting.GRAY));
							panel_t.setTooltip(Tooltip.of(ttt));
							
							//process pfp url
							final @Nullable String pfpUrlStr = userInfo.getAvatarURL();
							@Nullable URL pfpUrl = null;
							try { pfpUrl = pfpUrlStr != null ? new URL(pfpUrlStr) : null; }
							catch(MalformedURLException mue) { return; }
							
							//fetch pfp url
							UIExternalTexture.loadTextureAsync(
									pfpUrl, MC_CLIENT,
									pfp -> icon.setTexture(pfp),
									err -> {});
						},
						err -> {});
			}
		}
	}
	// --------------------------------------------------
	public final @Override void initFilters(FiltersInitContext initContext)
	{
		StatsTabUtils.initDefaultFilters(initContext);
	}
	// --------------------------------------------------
	private static final void initGroupLabel(TPanelElement panel, Text text)
	{
		final var n1 = TConfigPanelBuilder.nextPanelVerticalRect(panel);
		final var panel_tGroup = new TPanelElement(n1.x, n1.y, n1.width, 20);
		panel_tGroup.setBackgroundColor(0xff353535);
		panel_tGroup.setOutlineColor(0xff000000);
		panel.addChild(panel_tGroup, false);
		panel_tGroup.addChild(new TFillColorElement(2, 2, 16, 16, panel_tGroup.getOutlineColor()));
		final var lbl = new TLabelElement(
				panel_tGroup.getHeight() + 5, 0,
				panel_tGroup.getWidth(), panel_tGroup.getHeight(),
				text);
		lbl.setTextHorizontalAlignment(HorizontalAlignment.LEFT);
		lbl.setTextScale(0.8f);
		panel_tGroup.addChild(lbl);
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
				.sorted(String.CASE_INSENSITIVE_ORDER)
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
	// --------------------------------------------------
	private static final Text getDisplayNameFromGHUser(RepositoryUserInfo userInfo)
	{
		final @Nullable var name = userInfo.getName();
		final @Nullable var dName = userInfo.getDisplayName();
		final var finalDName = literal("");
		if(name == null && dName == null) finalDName.append("-");
		else if(name != null && dName != null)
		{
			if(Objects.equals(name, dName.getString())) finalDName.append("@" + name);
			else finalDName.append(dName).append(" (@").append(name).append(")");
		}
		else if(name != null && dName == null) finalDName.append(name);
		else if(name == null && dName != null) finalDName.append("@").append(dName);
		return finalDName;
	}
	// ==================================================
}