package io.github.thecsdev.betterstats.client.gui.screen.hud;

import static io.github.thecsdev.betterstats.BetterStats.getModID;
import static io.github.thecsdev.betterstats.api.client.gui.panel.BSComponentPanel.BS_WIDGETS_TEXTURE;
import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.betterstats.client.network.BetterStatsClientNetworkHandler.getLiveStatsEnabled;
import static io.github.thecsdev.betterstats.client.network.BetterStatsClientNetworkHandler.getServerHasBSS;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.awt.Rectangle;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.BetterStatsConfig;
import io.github.thecsdev.betterstats.client.network.BetterStatsClientNetworkHandler;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreenWrapper;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TWidgetHudScreen;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.client.gui.util.UITexture;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TButtonWidget;
import io.github.thecsdev.tcdcommons.api.client.util.interfaces.IParentScreenProvider;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket.Mode;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * {@link BetterStats}'s {@link TWidgetHudScreen}.
 */
public final class BetterStatsHudScreen extends TWidgetHudScreen implements IParentScreenProvider
{
	// ==================================================
	public static final Text TEXT_TITLE       = BST.hud_title();
	public static final Text TEXT_TUTORIAL_1  = BST.hud_tutorial1();
	public static final Text TEXT_TUTORIAL_2  = BST.hud_tutorial2();
	public static final Text TEXT_TUTORIAL_3  = BST.hud_tutorial3();
	public static final Text TEXT_LIVE_TOGGLE = BST.hud_liveStatsToggle();
	//
	public static final Identifier HUD_SCREEN_ID = new Identifier(getModID(), "stats_hud");
	// --------------------------------------------------
	private static final BetterStatsHudScreen INSTANCE = new BetterStatsHudScreen();
	// --------------------------------------------------
	private @Nullable Screen parent;
	//
	private float requestTimer = 0;
	private final int requestDelay = 20 * 10;
	// ==================================================
	private BetterStatsHudScreen() { super(TEXT_TITLE, HUD_SCREEN_ID); }
	// --------------------------------------------------
	protected final @Override TScreenWrapper<?> createScreenWrapper() { return new BetterStatsHudScreenWrapper(this); }
	// ==================================================
	protected final @Override void init()
	{
		//if the hud screen is opened, add some extra widgets to it
		if(isOpen())
		{
			//add the done button, that closes the screen, and shows the tutorial
			final var btn_done = new TButtonWidget(
					(getWidth() / 2) - 50, (getHeight() / 2) - 10,
					100, 20,
					translatable("gui.done"));
			btn_done.setTooltip(Tooltip.of(literal("") //must create new Text instance
					.append(TEXT_TUTORIAL_1).append("\n")
					.append(TEXT_TUTORIAL_2).append("\n")
					.append(TEXT_TUTORIAL_3)
				));
			btn_done.setOnClick(__ -> close());
			addChild(btn_done, false);
			
			//add a "realtime stats" toggle button
			if(BetterStatsConfig.CLIENT_NET_CONSENT && getServerHasBSS())
			{
				final var btn_toggleRealtime = new TButtonWidget(
						btn_done.getEndX() + 5, btn_done.getY(),
						20, 20);
				btn_toggleRealtime.setTooltip(Tooltip.of(TEXT_LIVE_TOGGLE));
				btn_toggleRealtime.setIcon(getLiveStatsEnabled() ?
						new UITexture(BS_WIDGETS_TEXTURE, new Rectangle(20, 80, 20, 20)) :
						new UITexture(BS_WIDGETS_TEXTURE, new Rectangle(0, 80, 20, 20)));
				btn_toggleRealtime.setOnClick(__ ->
				{
					BetterStatsClientNetworkHandler.c2s_liveStats(!getLiveStatsEnabled());
					refresh();
				});
				addChild(btn_toggleRealtime, false);
			}
		}
		
		//initialize the 'super' gui afterwards
		super.init();
	}
	// --------------------------------------------------
	public final @Override void render(TDrawContext pencil)
	{
		//render super
		super.render(pencil); //super must be called here
		
		// ---------- handle auto-requesting
		//don't auto-request when live stats are on and during setup
		if(this.client == null || isOpen())
			return;
		
		this.requestTimer += pencil.deltaTime;
		if(this.requestTimer > this.requestDelay)
		{
			this.requestTimer = 0;
			
			//network optimization;
			//- do not send packets when a screen is opened
			//- do not send packets when an overlay is present
			if(MC_CLIENT.currentScreen == null && MC_CLIENT.getOverlay() == null)
				MC_CLIENT.getNetworkHandler().sendPacket(new ClientStatusC2SPacket(Mode.REQUEST_STATS));
		}
	}
	// ==================================================
	/**
	 * Returns the current instance of {@link BetterStatsHudScreen}.
	 */
	public static BetterStatsHudScreen getInstance() { return INSTANCE; }
	// ==================================================
}