package io.github.thecsdev.betterstats.client;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.api.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.betterstats.api.client.registry.BSClientPlayerBadges;
import io.github.thecsdev.betterstats.api.client.registry.BSStatsTabs;
import io.github.thecsdev.betterstats.api.util.BSUtils;
import io.github.thecsdev.betterstats.client.network.BetterStatsClientNetworkHandler;
import io.github.thecsdev.tcdcommons.api.client.gui.util.GuiUtils;
import io.github.thecsdev.tcdcommons.api.events.client.MinecraftClientEvent;
import io.github.thecsdev.tcdcommons.api.events.client.gui.screen.GameMenuScreenEvent;
import io.github.thecsdev.tcdcommons.api.events.item.ItemGroupEvent;
import io.github.thecsdev.tcdcommons.api.hooks.client.gui.widget.ButtonWidgetHooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemGroups;

public final class BetterStatsClient extends BetterStats
{
	// ==================================================
	public static final MinecraftClient MC_CLIENT = MinecraftClient.getInstance();
	// ==================================================
	public BetterStatsClient()
	{
		//initialize and register stuff
		BSStatsTabs.register();
		BSClientPlayerBadges.register();
		BetterStatsClientNetworkHandler.init();
		
		//an event handler that will handle the game menu screen (the "pause" screen)
		GameMenuScreenEvent.INIT_WIDGETS_POST.register(gmScreen ->
		{
			//executing separately to really make sure the game menu screen finished initializing
			MC_CLIENT.execute(() ->
			{
				//locate the original stats button
				final ButtonWidget ogStatsBtn = GuiUtils.findButtonWidgetOnScreen(gmScreen, translatable("gui.stats"));
				if(ogStatsBtn == null) return;
				
				//replace its function
				ButtonWidgetHooks.setOnPress(
						ogStatsBtn,
						btn -> MC_CLIENT.setScreen(new BetterStatsScreen(MC_CLIENT.currentScreen).getAsScreen()));
			});
		});
		
		//update the "Item to Group" map whenever item groups update
		ItemGroupEvent.UPDATE_DISPLAY_CONTEXT.register((a, b, c) -> BSUtils.updateITG());
		
		//when the client joins a world, update the item group display context
		//right away, so as to avoid lag spikes when opening inventory later
		MinecraftClientEvent.JOINED_WORLD.register((client, world) ->
		{
			final var useOp = //Important: Must copy the exact values used by CreativeInventoryScreen
					client.player.isCreativeLevelTwoOp() &&
					MC_CLIENT.options.getOperatorItemsTab().getValue();
			ItemGroups.updateDisplayContext(world.getEnabledFeatures(), useOp, world.getRegistryManager());
		});
	}
	// ==================================================
}