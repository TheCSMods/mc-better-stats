package io.github.thecsdev.betterstats.api.util;

import org.jetbrains.annotations.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;

public final class BSUtils
{
	private BSUtils() {}
	
	/**
	 * Searches all possible {@link ItemGroup}s while looking for the given {@link Item}.
	 * Returns the first {@link ItemGroup} the {@link Item} is found in, or {@code null} if the
	 * item is not part of an {@link ItemGroup}.
	 * @param item The {@link Item} in question.
	 * @apiNote An {@link Item} can be part of multiple {@link ItemGroup}s. Keep that in mind.
	 */
	public static @Nullable ItemGroup getItemGroup(Item item)
	{
		//null check
		if(item == null) return null;
		
		//iterate all currently defined item groups,
		//look for a group that contains the said item
		final var searchGroup = ItemGroups.getSearchGroup();
		for(ItemGroup group : ItemGroups.getGroups())
		{
			//ignore the search group, as it is used for the
			//creative menu item search tab
			if(group == searchGroup) continue;
			
			//map out all items in the current group
			var items = group.getDisplayStacks().stream().map(stack -> stack.getItem()).toList();
			
			//check if the current group has the given item
			if(items.contains(item)) return group;
		}
		
		//default outcome: un-grouped
		return null;
	}
}