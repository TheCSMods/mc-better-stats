package io.github.thecsdev.betterstats.client.gui.screen;

import static io.github.thecsdev.betterstats.BetterStats.getModID;
import static io.github.thecsdev.betterstats.api.util.io.StatsProviderIO.FILE_EXTENSION;
import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement.COLOR_BACKGROUND;

import java.util.Locale;
import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreenPlus;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.client.util.interfaces.IParentScreenProvider;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import io.github.thecsdev.tcdcommons.api.util.io.mod.ModInfoProvider;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Base for {@link QuickShareUploadScreen} and {@link QuickShareDownloadScreen}.
 */
abstract class QuickShareScreen extends TScreenPlus implements IParentScreenProvider
{
	// ==================================================
	protected static final String QSC_SUFFIX = ("." + FILE_EXTENSION).toLowerCase(Locale.ENGLISH);
	// --------------------------------------------------
	private @Nullable Screen parent;
	// ==================================================
	public QuickShareScreen(@Nullable Screen parent, Text title)
	{
		super(title);
		this.parent = parent;
	}
	// --------------------------------------------------
	public final @Override Screen getParentScreen() { return this.parent; }
	public @Virtual @Override void close() { MC_CLIENT.setScreen(getParentScreen()); }
	// ==================================================
	protected final void refresh() { MC_CLIENT.executeSync(() -> { if(!isOpen()) return; clearChildren(); init(); }); }
	// --------------------------------------------------
	public @Virtual @Override void renderBackground(TDrawContext pencil)
	{
		super.renderBackground(pencil);
		pencil.drawTFill(COLOR_BACKGROUND);
	}
	// ==================================================
	/**
	 * Adds additional information to {@link JsonObject}s that act
	 * as HTTP request bodies for requests send to BSS APIs.<br>
	 * This information helps the BSS servers respond accordingly, as well
	 * helping prevent abuse and enforce limitations.<br>
	 * Please see the Privacy Policy document for more info.
	 */
	protected static final @Internal void addTelemetryData(JsonObject httpRequestBody)
	{
		final var mi            = Objects.requireNonNull(ModInfoProvider.getInstance());
		final var miMinecraft   = mi.getModInfo("minecraft");
		final var miBetterStats = mi.getModInfo(getModID());
		
		httpRequestBody.addProperty("mod_info.minecraft.version",   miMinecraft.getVersion());
		httpRequestBody.addProperty("mod_info.betterstats.version", miBetterStats.getVersion());
	}
	// ==================================================
}