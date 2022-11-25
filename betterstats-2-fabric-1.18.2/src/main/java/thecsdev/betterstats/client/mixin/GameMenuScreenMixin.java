package thecsdev.betterstats.client.mixin;

import static thecsdev.tcdcommons.api.util.TextUtils.translatable;
import static thecsdev.tcdcommons.api.util.TextUtils.fTranslatable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import thecsdev.betterstats.client.gui.screen.BetterStatsScreen;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen
{
	// --------------------------------------------------
	public ButtonWidget betterstats_button = null;
	// --------------------------------------------------
	protected GameMenuScreenMixin(Text title) { super(title); }
	// --------------------------------------------------
	@Inject(method = "initWidgets", at = @At("RETURN"))
	public void onInitWidgets(CallbackInfo callback)
	{
		//locate the original stats button
		ButtonWidget ogStatsBtn = betterstats_snipeButton(translatable("gui.stats"), true);
		if(ogStatsBtn == null) return;
		
		//create a better stats button
		betterstats_button = new ButtonWidget(
				ogStatsBtn.x, ogStatsBtn.y,
				ogStatsBtn.getWidth(), ogStatsBtn.getHeight(),
				fTranslatable("gui.stats"),
				__ -> client.setScreen(new BetterStatsScreen(this)));
		
		//adding at index 2 for keyboard navigation purposes
		((ScreenMixin)(Object)this).getChildren().add(2, betterstats_button);
		((ScreenMixin)(Object)this).getDrawables().add(2, betterstats_button);
		((ScreenMixin)(Object)this).getSelectables().add(2, betterstats_button);
	}
	// --------------------------------------------------
	private ButtonWidget betterstats_snipeButton(Text buttonText, boolean kill)
	{
		String btnTxtStr = buttonText.getString();
		ButtonWidget foundBtn = null;
		
		//iterate all drawables
		for(Drawable drawable : ((ScreenMixin)(Object)this).getDrawables())
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