package io.github.thecsdev.betterstats.client;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.time.Month;
import java.time.ZonedDateTime;
import java.util.Random;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.api.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.betterstats.api.client.registry.BSClientPlayerBadges;
import io.github.thecsdev.betterstats.api.client.registry.BSStatsTabs;
import io.github.thecsdev.betterstats.api.util.BSUtils;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.client.gui.util.GuiUtils;
import io.github.thecsdev.tcdcommons.api.events.client.MinecraftClientEvent;
import io.github.thecsdev.tcdcommons.api.events.client.gui.screen.GameMenuScreenEvent;
import io.github.thecsdev.tcdcommons.api.events.item.ItemGroupEvent;
import io.github.thecsdev.tcdcommons.api.hooks.client.gui.widget.ButtonWidgetHooks;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public final class BetterStatsClient extends BetterStats
{
	// ==================================================
	public static final MinecraftClient MC_CLIENT = MinecraftClient.getInstance();
	// --------------------------------------------------
	public static final KeyBinding KEYBIND_TOGGLE_HUD;
	// ==================================================
	static
	{
		//register key-bindings
		KEYBIND_TOGGLE_HUD = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				BST.keybind_toggleHud(),
				InputUtil.UNKNOWN_KEY.getCode(),
				getModID()));
	}
	// --------------------------------------------------
	public BetterStatsClient()
	{
		//initialize and register stuff
		BSStatsTabs.register();
		BSClientPlayerBadges.register();
		
		// ---------- modding the "Statistics" button
		//an event handler that will handle the game menu screen (the "pause" screen)
		GameMenuScreenEvent.INIT_WIDGETS_POST.register(gmScreen ->
		{
			//executing separately to really make sure the game menu screen finished initializing
			MC_CLIENT.execute(() ->
			{
				//easter egg - check the current date
				final var now = ZonedDateTime.now();
				if(now.getDayOfMonth() == 1 && now.getMonth() == Month.APRIL)
				{
					final var rn = new Random().nextInt(0, 101); //random number 0 to 100
					if(rn == 1) return; //randomly prevent `Better Statistics Screen` from opening
				}
				
				//locate the original stats button
				final ButtonWidget ogStatsBtn = GuiUtils.findButtonWidgetOnScreen(gmScreen, translatable("gui.stats"));
				if(ogStatsBtn == null) return;
				
				//replace its function
				final var ogStatsBtn_onPress = ButtonWidgetHooks.getOnPress(ogStatsBtn);
				ButtonWidgetHooks.setOnPress(
					ogStatsBtn,
					btn ->
					{
						if(Screen.hasShiftDown()) ogStatsBtn_onPress.onPress(ogStatsBtn);
						else MC_CLIENT.setScreen(new BetterStatsScreen(MC_CLIENT.currentScreen).getAsScreen());
					});
			});
		});
		
		// ---------- Performance optimizations
		//update the "Item to Group" map whenever item groups update
		ItemGroupEvent.UPDATE_DISPLAY_CONTEXT.register((a, b, c) -> BSUtils.updateITG());
		
		//pre-load dynamic content when joining worlds
		MinecraftClientEvent.JOINED_WORLD.register((client, world) ->
		{
			//when the client joins a world, update the item group display context
			//right away, so as to avoid lag spikes when opening inventory later
			final var useOp = //Important: Must copy the exact values used by CreativeInventoryScreen
					client.player.isCreativeLevelTwoOp() &&
					MC_CLIENT.options.getOperatorItemsTab().getValue();
			
			//create an instance of the creative inventory, as it is the one that updates
			//item groups and the search item group (in other words, let the game do it)
			new CreativeInventoryScreen(client.player, world.getEnabledFeatures(), useOp);
		});
	}
	// ==================================================
}