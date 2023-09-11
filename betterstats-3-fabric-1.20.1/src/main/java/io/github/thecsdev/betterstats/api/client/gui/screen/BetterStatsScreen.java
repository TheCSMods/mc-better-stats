package io.github.thecsdev.betterstats.api.client.gui.screen;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.client.registry.StatsTab;
import io.github.thecsdev.betterstats.api.client.util.StatFilterSettings;
import io.github.thecsdev.betterstats.api.client.util.io.LocalPlayerStatsProvider;
import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.client.gui.stats.panel.impl.BetterStatsPanel;
import io.github.thecsdev.betterstats.client.gui.stats.panel.impl.BetterStatsPanel.BetterStatsPanelProxy;
import io.github.thecsdev.betterstats.client.gui.stats.tabs.BSStatsTabs;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreenPlus;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreenWrapper;
import io.github.thecsdev.tcdcommons.api.client.util.interfaces.IParentScreenProvider;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket.Mode;

/**
 * The main focal point of this mod.<br/>
 * The screen where player statistics are shown, but better.
 */
public final class BetterStatsScreen extends TScreenPlus implements IParentScreenProvider
{
	// ==================================================
	private final @Nullable Screen parent;
	@Internal final IStatsProvider statsProvider;
	private @Nullable StatsTab selectedStatsTab = BSStatsTabs.GENERAL;
	private final StatFilterSettings filterSettings = new StatFilterSettings();
	// --------------------------------------------------
	private @Nullable BetterStatsPanel bsPanel;
	// ==================================================
	/**
	 * Constructs the {@link BetterStatsScreen} using the {@link LocalPlayerStatsProvider}.
	 * @param parent The {@link Screen} that should open when this one closes.
	 * @throws NullPointerException If The {@link LocalPlayerStatsProvider} is "unavailable" aka {@code null}.
	 * @see LocalPlayerStatsProvider#getInstance()
	 */
	public BetterStatsScreen(@Nullable Screen parent) throws NullPointerException { this(parent, LocalPlayerStatsProvider.getInstance()); }
	
	/**
	 * Constructs the {@link BetterStatsScreen} using a custom {@link IStatsProvider}.
	 * @param parent The {@link Screen} that should open when this one closes.
	 * @param statsProvider The {@link IStatsProvider}.
	 * @throws NullPointerException If the {@link IStatsProvider} is {@code null}.
	 */
	public BetterStatsScreen(@Nullable Screen parent, IStatsProvider statsProvider) throws NullPointerException
	{
		super(translatable("gui.stats"));
		this.parent = parent;
		this.statsProvider = Objects.requireNonNull(statsProvider);
	}
	
	protected final @Override TScreenWrapper<?> createScreenWrapper() { return new BetterStatsScreenWrapper(this); }
	public final @Override Screen getParentScreen() { return this.parent; }
	// ==================================================
	public final @Override boolean shouldPause() { return true; }
	public final @Override boolean shouldRenderInGameHud() { return false; }
	public final @Override void close()
	{
		//for non-pause-menu screens, set screen to parent
		if(!(this.parent instanceof GameMenuScreen))
			getClient().setScreen(this.parent);
		//for the pause-menu screen, set screen to null for consistency
		else super.close();
	}
	// --------------------------------------------------
	protected final @Override void onOpened() //TODO - Temporary; Make a network handler for these
	{
		super.onOpened();
		if(!(this.statsProvider == LocalPlayerStatsProvider.getInstance())) return;
		final var statReq = new ClientStatusC2SPacket(Mode.REQUEST_STATS);
		this.client.getNetworkHandler().sendPacket(statReq);
	}
	// ==================================================
	/**
	 * Returns the {@link BetterStatsPanel} that
	 * contains all the statistics GUI.<p>
	 * Will return {@code null} if this screen isn't initialized yet.
	 */
	public final @Nullable BetterStatsPanel getStatisticsPanel() { return this.bsPanel; }
	// --------------------------------------------------
	public final void setSelectedTab(@Nullable StatsTab statsTab) { this.selectedStatsTab = statsTab; refresh(); }
	// ==================================================
	/**
	 * Refreshes this screen by clearing and re-initializing its children.
	 */
	public final void refresh() { if(!isOpen()) return; clearChildren(); init(); }
	// --------------------------------------------------
	protected final @Override void init()
	{
		//initialize the content pane panel
		this.bsPanel = new BetterStatsPanel(0, 0, getWidth(), getHeight(), new BetterStatsPanelProxy()
		{
			public IStatsProvider getStatsProvider() { return BetterStatsScreen.this.statsProvider; }
			public StatsTab getSelectedStatsTab() { return BetterStatsScreen.this.selectedStatsTab; }
			public void setSelectedStatsTab(StatsTab statsTab) { BetterStatsScreen.this.selectedStatsTab = statsTab; }
			public final @Override StatFilterSettings getFilterSettings() { return BetterStatsScreen.this.filterSettings; }
		});
		addChild(this.bsPanel, false);
	}
	// ==================================================
}