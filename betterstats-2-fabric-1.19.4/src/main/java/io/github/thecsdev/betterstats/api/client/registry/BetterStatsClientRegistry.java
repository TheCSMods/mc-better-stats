package io.github.thecsdev.betterstats.api.client.registry;

import java.util.AbstractMap.SimpleEntry;
import java.util.TreeMap;

import com.google.common.collect.Maps;

import io.github.thecsdev.betterstats.api.registry.BetterStatsRegistry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BetterStatsClientRegistry extends BetterStatsRegistry
{
	// ==================================================
	protected BetterStatsClientRegistry() { super(); }
	// ==================================================
	/**
	 * A {@link TreeMap} holding custom items for the "Other (mods)"
	 * section in the menu bar. This section is for when a mod
	 * wants to add a custom button to the statistics screen.<br/>
	 * <br/>
	 * <b>The key</b> is a {@link String} representing a Mod ID.<br/>
	 * <b>The value</b> is a map entry (tuple) holding the {@link Text} label and the
	 * {@link Runnable} action that will be executed when the entry is pressed.
	 */
	public static final TreeMap<Identifier, SimpleEntry<Text, Runnable>> MenuBar_ModItems;
	// --------------------------------------------------
	static
	{
		//define the registries
		MenuBar_ModItems = Maps.newTreeMap();
	}
	// ==================================================
}