package io.github.thecsdev.betterstats.client;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.tcdcommons.api.client.events.screen.TGameMenuScreenEvent;
import io.github.thecsdev.tcdcommons.api.client.hooks.TGuiHooks;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class BetterStatsClient extends BetterStats implements ClientModInitializer
{
	// ==================================================
	@Override
	public void onInitializeClient()
	{
		//MixinGameMenuScreen - Override the "Statistics" button functionality.
		TGameMenuScreenEvent.INIT_WIDGETS_POST.register(gmScreen ->
		{
			MinecraftClient.getInstance().execute(() ->
			{
				//locate the original stats button
				ButtonWidget ogStatsBtn = betterstats_snipeButton(gmScreen, translatable("gui.stats"));
				if(ogStatsBtn == null) return;
				
				//replace it's function
				//((MixinButtonWidget)ogStatsBtn).setOnPress(btn ->
				TGuiHooks.setButtonPressAction(ogStatsBtn, btn ->
				{
					final MinecraftClient client = MinecraftClient.getInstance();
					client.setScreen(new BetterStatsScreen(client.currentScreen));
				});
			});
		});
	}
	// ==================================================
	private ButtonWidget betterstats_snipeButton(Screen screen, Text buttonText)
	{
		String btnTxtStr = buttonText.getString();
		ButtonWidget foundBtn = null;
		
		//iterate all drawables
		//for(Drawable drawable : ((MixinScreen)(Object)this).getDrawables())
		for(Drawable drawable : dev.architectury.hooks.client.screen.ScreenHooks.getRenderables(screen))
		{
			//ignore non-buttons
			if(!(drawable instanceof ButtonWidget))
				continue;
			ButtonWidget btn = (ButtonWidget)drawable;
			
			//compare texts
			if(!btnTxtStr.equals(btn.getMessage().getString()))
				continue;
			
			//return the button
			foundBtn = btn;
		}
		
		return foundBtn;
	}
	// ==================================================
}