package io.github.thecsdev.betterstats.client.gui.screen.hud;

import static io.github.thecsdev.betterstats.BetterStats.getModID;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreenWrapper;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TWidgetHudScreen;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.client.util.interfaces.IParentScreenProvider;
import net.minecraft.client.gui.screen.Screen;
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
	public static final Text TEXT_TITLE = translatable("betterstats.client.gui.screen.hud.betterstatshudscreen");
	public static final Identifier HUD_SCREEN_ID = new Identifier(getModID(), "stats_hud");
	// --------------------------------------------------
	private static final BetterStatsHudScreen INSTANCE = new BetterStatsHudScreen();
	// --------------------------------------------------
	private @Nullable Screen parent;
	//
	private float requestTimer = 0;
	private final int requestDelay = 100;
	// ==================================================
	private BetterStatsHudScreen() { super(TEXT_TITLE, HUD_SCREEN_ID); }
	// --------------------------------------------------
	protected final @Override TScreenWrapper<?> createScreenWrapper() { return new BetterStatsHudScreenWrapper(this); }
	// ==================================================
	public final @Override void render(TDrawContext pencil)
	{
		//render super
		super.render(pencil);
		
		//handle auto-requesting
		if(this.client == null || isOpen()) return; //don't auto-request during user setup
		this.requestTimer += pencil.deltaTime;
		if(this.requestTimer > this.requestDelay)
		{
			this.requestTimer = 0;
			this.client.getNetworkHandler().sendPacket(new ClientStatusC2SPacket(Mode.REQUEST_STATS));
		}
	}
	// ==================================================
	/**
	 * Returns the current instance of {@link BetterStatsHudScreen}.
	 */
	public static BetterStatsHudScreen getInstance() { return INSTANCE; }
	// ==================================================
}