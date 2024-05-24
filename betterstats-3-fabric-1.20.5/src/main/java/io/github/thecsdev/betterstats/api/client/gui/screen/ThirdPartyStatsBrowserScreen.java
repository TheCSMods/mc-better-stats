package io.github.thecsdev.betterstats.api.client.gui.screen;

import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;

import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreenPlus;
import io.github.thecsdev.tcdcommons.api.client.util.interfaces.IParentScreenProvider;
import net.minecraft.client.gui.screen.Screen;

/**
 * A {@link TScreenPlus} that allows the user to obtain statistics about
 * "third-party" sources, such as statistics about other players for example.
 */
public final class ThirdPartyStatsBrowserScreen extends TScreenPlus implements IParentScreenProvider
{
	// ==================================================
	private @Nullable Screen parent;
	// ==================================================
	public ThirdPartyStatsBrowserScreen(@Nullable Screen parent)
	{
		super(BST.bss());
		this.parent = parent;
	}
	// --------------------------------------------------
	public final @Nullable @Override Screen getParentScreen() { return this.parent; }
	public final @Override boolean shouldRenderInGameHud() { return false; }
	public final @Override void close() { MC_CLIENT.setScreen(this.parent); }
	// ==================================================
	protected final @Override void init()
	{
		//FIXME - Implement this section
		throw new NotImplementedException();
	}
	// ==================================================
}