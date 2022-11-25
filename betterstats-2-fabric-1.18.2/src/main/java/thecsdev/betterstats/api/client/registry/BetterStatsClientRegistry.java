package thecsdev.betterstats.api.client.registry;

import java.util.AbstractMap;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.text.Text;
import thecsdev.betterstats.api.registry.BetterStatsRegistry;

public class BetterStatsClientRegistry extends BetterStatsRegistry
{
	// ==================================================
	protected BetterStatsClientRegistry() { super(); }
	// ==================================================
	/**
	 * A {@link Multimap} holding custom items for the "Mods"
	 * section in the menu bar. This section is for when a mod
	 * wants to add a custom button to the statistics screen.<br/>
	 * <br/>
	 * <b>The key</b> is a {@link String} representing a Mod ID.<br/>
	 * <b>The value</b> is a map entry (tuple) holding the {@link Text} label and the
	 * {@link Runnable} action that will be executed when the entry is pressed.
	 */
	public static final Multimap<String, AbstractMap.SimpleEntry<Text, Runnable>> MOD_MENU_ITEMS;
	// --------------------------------------------------
	static
	{
		//define the registries
		MOD_MENU_ITEMS = ArrayListMultimap.create();
	}
	// ==================================================
}