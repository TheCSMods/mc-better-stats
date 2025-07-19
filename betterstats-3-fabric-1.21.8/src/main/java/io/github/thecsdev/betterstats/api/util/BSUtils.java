package io.github.thecsdev.betterstats.api.util;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;

public final class BSUtils
{
	// ==================================================
	/**
	 * An {@link Item} to {@link ItemGroup} map.
	 */
	private static final HashMap<Item, ItemGroup> ITG = new HashMap<Item, ItemGroup>();
	// ==================================================
	private BSUtils() {}
	// ==================================================
	/**
	 * Updates the {@link #ITG} {@link Map} that is used by {@link #getItemGroup(Item)}.
	 */
	public static final @Internal void updateITG()
	{
		//clear the ITG, and then update it
		ITG.clear();
		final var searchGroup = ItemGroups.getSearchGroup();
		final var air = Items.AIR;
		for(final ItemGroup group : ItemGroups.getGroups())
		{
			//ignore the search group, as it is used for the
			//creative menu item search tab
			if(group == searchGroup) continue;
			
			//add group's items to ITG
			group.getDisplayStacks().forEach(stack ->
			{
				//obtain the stack's item, and ensure an item is present
				//(in Minecraft's "language", AIR usually refers to "null")
				final var item = stack.getItem();
				if(item == null || item == air) return;
				
				//put the item and its group to the ITG map
				ITG.put(item, group);
			});
		}
	}
	// --------------------------------------------------
	/**
	 * Uses {@link #ITG} to find the {@link ItemGroup} for the given {@link Item}.
	 * @param item The {@link Item} in question.
	 * @apiNote An {@link Item} can be part of multiple {@link ItemGroup}s.
	 * This method will return the first or last found {@link ItemGroup}. Keep that in mind.
	 */
	public static @Nullable ItemGroup getItemGroup(Item item) { return ITG.getOrDefault(item, null); }
	// ==================================================
}