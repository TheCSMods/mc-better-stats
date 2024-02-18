package io.github.thecsdev.betterstats.client.gui.stats.panel;

import static io.github.thecsdev.betterstats.BetterStats.URL_CURSEFORGE;
import static io.github.thecsdev.betterstats.BetterStats.URL_DISCORD;
import static io.github.thecsdev.betterstats.BetterStats.URL_ISSUES;
import static io.github.thecsdev.betterstats.BetterStats.URL_KOFI;
import static io.github.thecsdev.betterstats.BetterStats.URL_MODRINTH;
import static io.github.thecsdev.betterstats.BetterStats.URL_SOURCES;
import static io.github.thecsdev.betterstats.BetterStats.URL_YOUTUBE;
import static io.github.thecsdev.betterstats.BetterStatsConfig.RESTRICTED_MODE;
import static io.github.thecsdev.betterstats.api.client.registry.BSClientRegistries.STATS_TAB;
import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.tcdcommons.api.client.gui.util.GuiUtils.showUrlPrompt;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Map.Entry;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.client.gui.panel.BSComponentPanel;
import io.github.thecsdev.betterstats.api.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.betterstats.api.client.registry.BSStatsTabs;
import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.betterstats.api.events.client.gui.BetterStatsGUIEvent;
import io.github.thecsdev.betterstats.api.util.io.IEditableStatsProvider;
import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.api.util.io.StatsProviderIO;
import io.github.thecsdev.betterstats.client.gui.screen.hud.BetterStatsHudScreen;
import io.github.thecsdev.betterstats.client.gui.stats.panel.impl.BetterStatsPanel.BetterStatsPanelProxy;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.menu.TContextMenuPanel;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.menu.TMenuBarPanel;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.explorer.TFileChooserResult.ReturnValue;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.explorer.TFileChooserScreen;
import io.github.thecsdev.tcdcommons.api.client.gui.util.GuiUtils;
import io.github.thecsdev.tcdcommons.api.util.TUtils;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Represents the "menu bar" GUI component that usually shows
 * up on the top of the {@link BetterStatsScreen}.
 */
public final class MenuBarPanel extends BSComponentPanel
{
	// ==================================================
	/**
	 * The default {@link MenuBarPanel}'s {@link #getHeight()}.
	 */
	public static final int HEIGHT = TMenuBarPanel.HEIGHT;
	// --------------------------------------------------
	private final MenuBarPanelProxy proxy;
	// ==================================================
	public MenuBarPanel(int x, int y, int width, MenuBarPanelProxy proxy) throws NullPointerException
	{
		super(x, y, width, HEIGHT);
		this.proxy = Objects.requireNonNull(proxy);
	}
	// --------------------------------------------------
	public final @Override void setSize(int width, int height, int flags)
	{
		//set size as usual
		super.setSize(width, height, flags);
		//if invoking event handlers, then also refresh this panel
		if(getParentTScreen() != null && (flags & SS_INVOKE_EVENT) == SS_INVOKE_EVENT)
			refresh();
	}
	// ==================================================
	/**
	 * Returns the {@link MenuBarPanelProxy} associated
	 * with this {@link MenuBarPanel}.
	 */
	public final MenuBarPanelProxy getProxy() { return this.proxy; }
	// ==================================================
	protected final @Override void init()
	{
		//pre-init stuff
		final var localPlayer = MC_CLIENT.player;
		final var tr = "betterstats.api.client.gui.stats.panel.menubarpanel.";
		
		//create the menu bar panel
		final var menu = new TMenuBarPanel(0, 0, getWidth());
		menu.setBackgroundColor(0); //clear colors because
		menu.setOutlineColor(0);    //this panel already has them
		
		//add menu item elements
		menu.addButton(translatable(tr + "menu_file"), btn ->
		{
			final var cMenu = new TContextMenuPanel(btn);
			//cMenu.addButton(translatable(tr + "menu_file.new"), null);
			cMenu.addButton(translatable(tr + "menu_file.open"), __ ->
			{
				TFileChooserScreen.showOpenFileDialog(StatsProviderIO.FILE_EXTENSION, result ->
				{
					if(result.getReturnValue() != ReturnValue.APPROVE_OPTION)
						return;
					try
					{
						//show the stats screen
						final var stats = StatsProviderIO.loadFromFile(result.getSelectedFile());
						final var parentScreen = GuiUtils.getCurrentScreenParent();
						MC_CLIENT.setScreen(new BetterStatsScreen(parentScreen, stats).getAsScreen());
					}
					catch(Exception exc) { TUtils.throwCrash("Failed to load stats.", exc); }
				});
			});
			//cMenu.addButton(translatable(tr + "menu_file.save"), null);
			cMenu.addButton(translatable(tr + "menu_file.save_as"), __ ->
			{
				TFileChooserScreen.showSaveFileDialog(StatsProviderIO.FILE_EXTENSION, result ->
				{
					if(result.getReturnValue() != ReturnValue.APPROVE_OPTION)
						return;
					try
					{
						final var file = result.getSelectedFile();
						final var stats = MenuBarPanel.this.proxy.getStatsProvider();
						StatsProviderIO.saveToFile(file, stats);
					}
					catch(Exception exc) { TUtils.throwCrash("Failed to save stats.", exc); }
				});
			});
			cMenu.open();
		});
		
		menu.addButton(translatable(tr + "menu_view"), btn ->
		{
			//create the context menu
			final var cMenu = new TContextMenuPanel(btn);
			
			//vanilla stats
			cMenu.addButton(translatable(tr + "menu_view.vanilla_stats"), __ ->
			{
				if(localPlayer == null) return;
				MC_CLIENT.setScreen(new StatsScreen(
						MC_CLIENT.currentScreen,
						localPlayer.getStatHandler()));
			});
			
			//statistics hud
			cMenu.addButton(BetterStatsHudScreen.TEXT_TITLE, __ ->
			{
				final var sc = BetterStatsHudScreen.getInstance();
				sc.setParentScreen(MC_CLIENT.currentScreen);
				MC_CLIENT.setScreen(sc.getAsScreen());
			});
			
			//stats tab entries
			cMenu.addSeparator();
			for(final Entry<Identifier, StatsTab> statsTabEntry : STATS_TAB)
			{
				final StatsTab statsTab = statsTabEntry.getValue();
				if(!statsTab.isAvailable()) continue; //only show it if available
				cMenu.addButton(statsTab.getName(), __ -> this.proxy.setSelectedStatsTab(statsTab));
			}
			
			//open the context menu
			cMenu.open();
		});
		
		menu.addButton(translatable(tr + "menu_about"), btn ->
		{
			//create the context menu
			final var cMenu = new TContextMenuPanel(btn);
			
			//url-s
			if(!RESTRICTED_MODE)
			{
				cMenu.addButton(translatable(tr + "menu_about.source"), __ -> showUrlPrompt(URL_SOURCES, false));
				cMenu.addButton(translatable("menu.reportBugs"), __ -> showUrlPrompt(URL_ISSUES, false));
				cMenu.addSeparator();
			}
			cMenu.addButton(translatable(tr + "menu_about.curseforge"), __ -> showUrlPrompt(URL_CURSEFORGE, false));
			cMenu.addButton(translatable(tr + "menu_about.modrinth"), __ -> showUrlPrompt(URL_MODRINTH, false));
			if(!RESTRICTED_MODE)
			{
				cMenu.addSeparator();
				cMenu.addButton(translatable(tr + "menu_about.youtube"), __ -> showUrlPrompt(URL_YOUTUBE, false));
				cMenu.addButton(translatable(tr + "menu_about.kofi"), __ -> showUrlPrompt(URL_KOFI, false));
				cMenu.addButton(translatable(tr + "menu_about.discord"), __ -> showUrlPrompt(URL_DISCORD, false));
			}
			
			//open the context menu
			cMenu.open();
		});
		
		//invoke the menu bar event here, so other mods can add their options
		BetterStatsGUIEvent.MENU_BAR_INITIALIZED.invoker().invoke(menu);
		
		//add the menu bar panel
		addChild(menu, true);
		
		//add the display name label
		final var statsProvider = this.proxy.getStatsProvider();
		final boolean isESP = (statsProvider instanceof IEditableStatsProvider);
		
		@Nullable Text displayName = (statsProvider != null) ? statsProvider.getDisplayName() : null;
		if(displayName == null) displayName = literal("-"); //both conditions above can return null
		
		final int displayNameW = getTextRenderer().getWidth(displayName);
		
		final int menuSP = menu.getScrollPadding();
		final var lbl_displayName = new TLabelElement(
				menu.getEndX() - (menuSP + displayNameW + getTextRenderer().fontHeight), menu.getY() + menuSP,
				displayNameW, menu.getHeight() - (menuSP*2),
				displayName);
		lbl_displayName.setTextColor(isESP ? 0xFFFFFFFF : BSStatsTabs.COLOR_SPECIAL);
		addChild(lbl_displayName, false);
	}
	// ==================================================
	/**
	 * A component that provides the {@link MenuBarPanel} with
	 * the necessary information to operate properly.
	 */
	public static interface MenuBarPanelProxy
	{
		/**
		 * @see BetterStatsPanelProxy#getStatsProvider()
		 */
		public IStatsProvider getStatsProvider();
		
		/**
		 * @see BetterStatsPanelProxy#setSelectedStatsTab(StatsTab)
		 */
		public void setSelectedStatsTab(StatsTab statsTab);
	}
	// ==================================================
}