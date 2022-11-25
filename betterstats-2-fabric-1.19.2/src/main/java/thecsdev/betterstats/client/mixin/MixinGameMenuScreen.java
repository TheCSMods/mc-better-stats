package thecsdev.betterstats.client.mixin;

import static thecsdev.tcdcommons.api.util.TextUtils.translatable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import thecsdev.betterstats.client.gui.BSButtonWidget;
import thecsdev.betterstats.client.gui.screen.BetterStatsScreen;

@Mixin(GameMenuScreen.class)
public abstract class MixinGameMenuScreen extends Screen
{
	// --------------------------------------------------
	public BSButtonWidget betterstats_button = null;
	// --------------------------------------------------
	protected MixinGameMenuScreen(Text title) { super(title); }
	// --------------------------------------------------
	@Inject(method = "initWidgets", at = @At("RETURN"))
	public void onInitWidgets(CallbackInfo callback)
	{
		MinecraftClient.getInstance().execute(() ->
		{
			//locate the original stats button
			ButtonWidget ogStatsBtn = betterstats_snipeButton(translatable("gui.stats"), false);
			if(ogStatsBtn == null) return;
			
			//replace it's function
			((MixinButtonWidget)ogStatsBtn).setOnPress(btn ->
			{
				final MinecraftClient client = MinecraftClient.getInstance();
				client.setScreen(new BetterStatsScreen(client.currentScreen));
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
	private ButtonWidget betterstats_snipeButton(Text buttonText, boolean kill)
	{
		String btnTxtStr = buttonText.getString();
		ButtonWidget foundBtn = null;
		
		//iterate all drawables
		for(Drawable drawable : ((MixinScreen)(Object)this).getDrawables())
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
		
		//kill the found button if needed, and return the button if found
		if(foundBtn != null && kill)
			this.remove(foundBtn);
		return foundBtn;
	}
	// --------------------------------------------------
}