package io.github.thecsdev.betterstats.client.gui.widget;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.client.BetterStatsClient;
import io.github.thecsdev.betterstats.client.gui.stats.tabs.BSCreditsTab;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TTextureElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TRefreshablePanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.util.GuiUtils;
import io.github.thecsdev.tcdcommons.api.client.gui.util.UIExternalTexture;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TButtonWidget;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;
import io.github.thecsdev.tcdcommons.api.util.io.repo.RepositoryInfoProvider;
import io.github.thecsdev.tcdcommons.api.util.io.repo.RepositoryUserInfo;
import io.github.thecsdev.tcdcommons.util.io.http.TcdWebApiPerson;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * A widget that displays information about a credited {@link Person} in the {@link BSCreditsTab}.
 */
@SuppressWarnings("removal")
public final class CreditsTabPersonWidget extends TRefreshablePanelElement
{
	// ==================================================
	public static final Text TEXT_OPEN_LINK = translatable("mco.notification.visitUrl.buttonText.default");
	// --------------------------------------------------
	private final TcdWebApiPerson person;
	private final boolean fetchGitHubInfo;
	// ==================================================
	public CreditsTabPersonWidget(int x, int y, int width, TcdWebApiPerson person, boolean fetchGitHubInfo) throws NullPointerException
	{
		super(x, y, width, 20);
		setBackgroundColor(0x22000000);
		setOutlineColor(0x00000000);
		this.person = Objects.requireNonNull(person);
		this.fetchGitHubInfo = fetchGitHubInfo;
	}
	// ==================================================
	protected final @Override void init()
	{
		final var icon = new TTextureElement(2, 2, 16, 16);
		addChild(icon, true);
		
		final var nameLabel = new TLabelElement(25, 0, getWidth(), 20, literal(this.person.getName()));
		nameLabel.setTextScale(0.9f);
		addChild(nameLabel, true);
		
		final @Nullable URL visitUrl = this.person.getContact().getHomepageUrl();
		final var visitBtn = new TButtonWidget(getEndX() - 102, getY() + 2, 100, 16);
		{
			final var visitBtnLbl = new TLabelElement(0, 0, visitBtn.getWidth(), visitBtn.getHeight());
			visitBtnLbl.setText(TEXT_OPEN_LINK);
			visitBtnLbl.setTextHorizontalAlignment(HorizontalAlignment.CENTER);
			visitBtnLbl.setTextScale(0.9f);
			visitBtn.addChild(visitBtnLbl, true);
		}
		visitBtn.setEnabled(visitUrl != null);
		visitBtn.setOnClick(btn -> GuiUtils.showUrlPrompt(visitUrl.toString(), false));
		addChild(visitBtn, false);

		//process the url
		if(!this.fetchGitHubInfo) return;
		final @Nullable var host = visitUrl.getHost();
		if(!(Objects.equals(host, "github.com") || Objects.equals(host, "www.github.com")))
			return;
		@Nullable URI visitUri = null;
		try { visitUri = visitUrl.toURI(); } catch(URISyntaxException use) { return; }
		
		//fetch GitHub user info
		RepositoryInfoProvider.getUserInfoAsync(
			visitUri,
			BetterStatsClient.MC_CLIENT,
			userInfo ->
			{
				//handle display name
				nameLabel.setText(CreditsTabPersonWidget.getDisplayNameFromGHUser(userInfo));
				
				//handle tooltip
				final var ttt = literal("")
						.append(literal(nameLabel.getText().getString()).formatted(Formatting.YELLOW));
				final @Nullable Text descr = userInfo.getBiography() != null ?
						literal(userInfo.getBiography()) : null;
				if(descr != null) ttt
					.append("\n")
					.append(literal(descr.getString()).formatted(Formatting.GRAY));
				setTooltip(Tooltip.of(ttt));
				
				//fetch pfp url
				userInfo.getAvatarImageAsync(
					BetterStatsClient.MC_CLIENT,
					avatarBytes ->
					{
						UIExternalTexture.loadTextureAsync(
							avatarBytes,
							avatarImg -> icon.setTexture(avatarImg),
							err ->
							{
								System.err.println("Failed to load image for " + userInfo.getAccountName());
								err.printStackTrace();
							});
					},
					err -> {});
			},
			err -> {});
	}
	// ==================================================
	/**
	 * Constructs a "display name" {@link Text} from a {@link RepositoryUserInfo}.
	 * @param userInfo The {@link RepositoryUserInfo}.
	 */
	public static final Text getDisplayNameFromGHUser(RepositoryUserInfo userInfo) throws NullPointerException
	{
		final @Nullable var name = userInfo.getAccountName();
		final @Nullable var dName = userInfo.getDisplayName();
		final var finalDName = literal("");
		if(name == null && dName == null) finalDName.append("-");
		else if(name != null && dName != null)
		{
			if(Objects.equals(name, dName)) finalDName.append("@" + name);
			else finalDName.append(dName).append(" (@").append(name).append(")");
		}
		else if(name != null && dName == null) finalDName.append(name);
		else if(name == null && dName != null) finalDName.append("@").append(dName);
		return finalDName;
	}
	// ==================================================
	
}