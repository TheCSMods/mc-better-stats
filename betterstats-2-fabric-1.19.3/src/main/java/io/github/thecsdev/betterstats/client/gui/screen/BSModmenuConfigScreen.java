package io.github.thecsdev.betterstats.client.gui.screen;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * An optional {@link ModMenuApi} integration for the config GUI.<br/>
 * <b>Danger:</b> Interacting with this class could crash the game, as it depends on 'modmenu'.
 */
public class BSModmenuConfigScreen implements ModMenuApi
{
	public @Override ConfigScreenFactory<?> getModConfigScreenFactory()
	{
		return parent -> new BetterStatsConfigScreen(parent);
	}
}