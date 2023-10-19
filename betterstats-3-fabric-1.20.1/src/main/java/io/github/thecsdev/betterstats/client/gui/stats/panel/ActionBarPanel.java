package io.github.thecsdev.betterstats.client.gui.stats.panel;

import static io.github.thecsdev.betterstats.BetterStatsConfig.LEGAL_NET_CONSENT;
import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.betterstats.client.network.BetterStatsClientNetworkHandler.c2s_liveStats;
import static io.github.thecsdev.betterstats.client.network.BetterStatsClientNetworkHandler.serverHasBSS;
import static io.github.thecsdev.betterstats.network.BetterStatsNetworkHandler.TXT_CONSENT_WARNING;
import static io.github.thecsdev.betterstats.network.BetterStatsNetworkHandler.TXT_TOGGLE_TOOLTIP;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.fLiteral;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.awt.Rectangle;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import io.github.thecsdev.betterstats.api.client.gui.panel.BSComponentPanel;
import io.github.thecsdev.betterstats.api.client.registry.BSStatsTabs;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.betterstats.client.gui.stats.panel.impl.BetterStatsPanel.BetterStatsPanelProxy;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreenWrapper;
import io.github.thecsdev.tcdcommons.api.client.gui.util.UITexture;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TButtonWidget;
import io.github.thecsdev.tcdcommons.api.client.util.interfaces.IParentScreenProvider;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;

public final class ActionBarPanel extends BSComponentPanel
{
	// ==================================================
	public static final int HEIGHT = 22;
	// --------------------------------------------------
	protected final ActionBarPanelProxy proxy;
	// ==================================================
	public ActionBarPanel(int x, int y, int width, ActionBarPanelProxy proxy) throws NullPointerException
	{
		super(x, y, width, HEIGHT);
		this.proxy = Objects.requireNonNull(proxy);
	}
	// ==================================================
	/**
	 * Returns the {@link ActionBarPanelProxy} associated
	 * with this {@link ActionBarPanel}.
	 */
	public final ActionBarPanelProxy getProxy() { return this.proxy; }
	// ==================================================
	protected final @Override void init()
	{
		//close button
		final var btn_close = new TButtonWidget(getEndX() - 21, getY() + 1, 20, 20);
		btn_close.setEnabled(this.proxy.canClose());
		btn_close.setOnClick(__ -> this.proxy.onClose());
		btn_close.setTooltip(Tooltip.of(translatable("gui.done")));
		btn_close.setIcon(new UITexture(BS_WIDGETS_TEXTURE, new Rectangle(0, 60, 20, 20)));
		addChild(btn_close, false);
		
		//options button
		final var btn_options = new TButtonWidget(btn_close.getX() - 20, btn_close.getY(), 20, 20);
		//btn_options.setOnClick(__ -> MC_CLIENT.setScreen(new BetterStatsConfigScreen(MC_CLIENT.currentScreen).getAsScreen()));
		btn_options.setOnClick(__ -> this.proxy.setSelectedStatsTab(BSStatsTabs.BSS_CONFIG));
		btn_options.setTooltip(Tooltip.of(translatable("options.title")));
		btn_options.setIcon(new UITexture(BS_WIDGETS_TEXTURE, new Rectangle(0, 40, 20, 20)));
		addChild(btn_options, false);
		
		//bss network button
		final var btn_bssNet = new TButtonWidget(btn_options.getX() - 20, btn_options.getY(), 20, 20);
		btn_bssNet.setTooltip(Tooltip.of(TXT_TOGGLE_TOOLTIP));
		btn_bssNet.setIcon(LEGAL_NET_CONSENT ?
				new UITexture(BS_WIDGETS_TEXTURE, new Rectangle(20, 80, 20, 20)) :
				new UITexture(BS_WIDGETS_TEXTURE, new Rectangle(0, 80, 20, 20)));
		btn_bssNet.setOnClick(__ ->
		{
			//if turning off, send a packet indicating no more live updates
			if(LEGAL_NET_CONSENT)
			{
				c2s_liveStats(false);
				LEGAL_NET_CONSENT = false;
				refresh();
				return;
			}
			
			//else if turning on, go through a consent screen
			final var currentScreen = MC_CLIENT.currentScreen;
			final BooleanConsumer confirmScreenCallback = (accepted) ->
			{
				LEGAL_NET_CONSENT = accepted;
				MC_CLIENT.setScreen(currentScreen);
				if(LEGAL_NET_CONSENT) c2s_liveStats();
			};
			MC_CLIENT.setScreen(new ConfirmScreen(confirmScreenCallback, TXT_TOGGLE_TOOLTIP, TXT_CONSENT_WARNING));
		});
		btn_bssNet.setEnabled(serverHasBSS() && !MC_CLIENT.isInSingleplayer());
		addChild(btn_bssNet, false);
		
		//credits button
		final var btn_credits = new TButtonWidget(btn_bssNet.getX() - 20, btn_bssNet.getY(), 20, 20);
		final var tt_credits = fLiteral("§e" + translatable("credits_and_attribution.button.credits").getString())
				.append(fLiteral("§r\n\n§7# " + translatable("betterstats.translators.title").getString() + "§r\n"))
				.append(getCreditsTranslatorNames());
		btn_credits.setTooltip(Tooltip.of(tt_credits));
		btn_credits.setIcon(new UITexture(BS_WIDGETS_TEXTURE, new Rectangle(0, 100, 20, 20)));
		addChild(btn_credits, false);
	}
	// --------------------------------------------------
	private static final String getCreditsTranslatorNames()
	{
		final String translators = translatable("betterstats.translators").getString();
		if(StringUtils.isBlank(translators))
			return "-";
		
		String[] names = translators.split(",");
		StringBuilder output = new StringBuilder();
		
		for (String name : names)
		{
			//skip blank names
			if(StringUtils.isBlank(name)) continue;
			//append name
			output.append("- " + name.trim()).append("\n");
		}
		
		return output.toString().trim();
	}
	// ==================================================
	public static interface ActionBarPanelProxy
	{
		/**
		 * See {@link BetterStatsPanelProxy#setSelectedStatsTab(StatsTab)}
		 */
		public void setSelectedStatsTab(StatsTab statsTab);
		
		/**
		 * Returns {@code true} if the "close" operation is supported,
		 * aka if the current GUI {@link Screen} may be closed.
		 * @see #onClose()
		 */
		default boolean canClose() { return true; }
		
		/**
		 * Called when the "close" button is pressed.</br>
		 * Use this to close the current {@link Screen}.
		 */
		default void onClose()
		{
			//obtain the current screen
			final Screen curr = MC_CLIENT.currentScreen;
			
			//for parent screen providers, set the screen to their parent screen
			if(curr instanceof IParentScreenProvider)
				MC_CLIENT.setScreen(((IParentScreenProvider)curr).getParentScreen());
			
			//for TScreen-s, look for a parent screen provider and use it if possible
			else if(curr instanceof TScreenWrapper<?>)
			{
				//obtain current screen as TScreen
				final var tcurr = ((TScreenWrapper<?>)curr).getTargetTScreen();
				//if it's a parent screen provider, use it
				if(tcurr instanceof IParentScreenProvider)
					MC_CLIENT.setScreen(((IParentScreenProvider)tcurr).getParentScreen());
				//if not, just call `close()`
				else tcurr.close();
			}
			
			//for all other instances, just call `close()` on the current screen if it isn't null
			else if(curr != null) curr.close();
		}
	}
	// ==================================================
}