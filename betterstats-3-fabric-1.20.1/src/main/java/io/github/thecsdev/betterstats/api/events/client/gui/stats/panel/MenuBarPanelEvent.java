package io.github.thecsdev.betterstats.api.events.client.gui.stats.panel;

import io.github.thecsdev.betterstats.client.gui.stats.panel.MenuBarPanel;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.menu.TMenuBarPanel;
import io.github.thecsdev.tcdcommons.api.event.TEvent;
import io.github.thecsdev.tcdcommons.api.event.TEventFactory;

/**
 * {@link TEvent}s related to the {@link MenuBarPanel}.
 */
public interface MenuBarPanelEvent
{
	/**
	 * @see MenuBarInitialized#invoke(TMenuBarPanel)
	 */
	TEvent<MenuBarInitialized> MENU_INITIALIZED = TEventFactory.createLoop();
	
	public static interface MenuBarInitialized
	{
		/**
		 * A {@link TEvent} that is invoked after the {@link MenuBarPanel}
		 * component initializes its menu items in the {@link TMenuBarPanel}.
		 * <p>
		 * Use this to add your own menu items to the {@link TMenuBarPanel}.
		 */
		public void invoke(TMenuBarPanel menuBar);
	}
}