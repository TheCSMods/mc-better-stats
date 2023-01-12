package io.github.thecsdev.betterstats.client.mixin;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.thecsdev.betterstats.client.gui.BSButtonWidget;
import io.github.thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Mixin(value = PauseScreen.class, remap = true)
public abstract class MixinGameMenuScreen extends Screen
{
	// --------------------------------------------------
	public BSButtonWidget betterstats_button = null;
	// --------------------------------------------------
	protected MixinGameMenuScreen(Component title) { super(title); }
	// --------------------------------------------------
	@Inject(method = "createPauseMenu", at = @At("RETURN"))
	public void onInitWidgets(CallbackInfo callback)
	{
		Minecraft.getInstance().execute(() ->
		{
			//locate the original stats button
			Button ogStatsBtn = betterstats_snipeButton(translatable("gui.stats"), false);
			if(ogStatsBtn == null) return;
			
			//replace it's function
			((MixinButtonWidget)ogStatsBtn).setOnPress(btn ->
			{
				final var client = Minecraft.getInstance();
				client.setScreen(new BetterStatsScreen(client.screen));
			});
			
			//create a better stats button
			/*betterstats_button = new BSButtonWidget(
					ogStatsBtn.x, ogStatsBtn.y,
					ogStatsBtn.getWidth(), ogStatsBtn.getHeight());
			
			//adding at index 2 for keyboard navigation purposes
			((MixinScreen)(Object)this).getChildren().add(2, betterstats_button);
			((MixinScreen)(Object)this).getDrawables().add(2, betterstats_button);
			((MixinScreen)(Object)this).getSelectables().add(2, betterstats_button);
			
			//assign the helper widget
			betterstats_button.btn_backToGame = betterstats_snipeButton(translatable("menu.returnToGame"), false);*/
		});
	}
	// --------------------------------------------------
	private Button betterstats_snipeButton(Component buttonText, boolean kill)
	{
		String btnTxtStr = buttonText.getString();
		Button foundBtn = null;
		
		//iterate all drawables
		for(Widget drawable : ((Screen)(Object)this).renderables)
		{
			//ignore non-buttons
			if(!(drawable instanceof Button))
				continue;
			Button btn = (Button)drawable;
			
			//compare texts
			if(!btnTxtStr.equals(btn.getMessage().getString()))
				continue;
			
			//return the button
			foundBtn = btn;
		}
		
		//kill the found button if needed, and return the button if found
		if(foundBtn != null && kill)
			this.removeWidget(foundBtn);
		return foundBtn;
	}
	// --------------------------------------------------
}