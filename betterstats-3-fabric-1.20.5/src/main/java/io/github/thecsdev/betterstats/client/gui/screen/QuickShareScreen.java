package io.github.thecsdev.betterstats.client.gui.screen;

import static io.github.thecsdev.betterstats.api.util.io.StatsProviderIO.FILE_EXTENSION;
import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement.COLOR_BACKGROUND;

import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreenPlus;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.client.util.interfaces.IParentScreenProvider;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
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
}