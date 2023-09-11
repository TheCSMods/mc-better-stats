package io.github.thecsdev.betterstats.api.client.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * An optional {@link ModMenuApi} integration for the config GUI.<br/>
 * @apiNote Interacting with this class could crash the game, as it depends on 'modmenu'.
 */
public final class ModMenuApiImpl implements ModMenuApi
{
	public final @Override ConfigScreenFactory<?> getModConfigScreenFactory()
	{
		return parent -> null;
	}
}