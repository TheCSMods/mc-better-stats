package io.github.thecsdev.betterstats.client.gui.stats.panel;

import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.awt.Rectangle;
import java.util.Objects;

import io.github.thecsdev.betterstats.api.client.gui.panel.BSComponentPanel;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreenWrapper;
import io.github.thecsdev.tcdcommons.api.client.gui.util.UITexture;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TButtonWidget;
import io.github.thecsdev.tcdcommons.api.client.util.interfaces.IParentScreenProvider;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;

public final class ActionBarPanel extends BSComponentPanel
{
	// ==================================================
	public static final int HEIGHT = 20;
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
		final var btn_close = new TButtonWidget(getEndX() - 20, getY(), 20, getHeight());
		btn_close.setEnabled(this.proxy.canClose());
		btn_close.setOnClick(__ -> this.proxy.onClose());
		btn_close.setTooltip(Tooltip.of(translatable("gui.done")));
		btn_close.setIcon(new UITexture(BS_WIDGETS_TEXTURE, new Rectangle(0, 60, 20, 20)));
		addChild(btn_close, false);
	}
	// ==================================================
	public static interface ActionBarPanelProxy
	{
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