package thecsdev.betterstats.client.mixin;

import static thecsdev.betterstats.BetterStats.tt;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.PauseScreen;
import thecsdev.betterstats.client.gui.widget.BetterStatsButtonWidget;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen
{
	// --------------------------------------------------
	public BetterStatsButtonWidget betterstats_button = null;
	// --------------------------------------------------
	protected PauseScreenMixin(Component title) { super(title); }
	// --------------------------------------------------
	@Inject(method = "initWidgets", at = @At("RETURN"))
	public void onInitWidgets(CallbackInfo callback)
	{
		//locate the original stats button
		Button ogStatsBtn = betterstats_snipeButton(tt("gui.stats"), true);
		if(ogStatsBtn == null) return;
		
		//create a better stats button
		betterstats_button = new BetterStatsButtonWidget(
				ogStatsBtn.x, ogStatsBtn.y,
				ogStatsBtn.getWidth(), ogStatsBtn.getHeight(),
				this);
		
		//adding at index 2 for keyboard navigation purposes
		((ScreenMixin)(Object)this).getChildren().add(2, betterstats_button);
		((ScreenMixin)(Object)this).getRenderables().add(2, betterstats_button);
		((ScreenMixin)(Object)this).getNarratables().add(2, betterstats_button);
	}
	// --------------------------------------------------
	private Button betterstats_snipeButton(Component buttonText, boolean kill)
	{
		String btnTxtStr = buttonText.getString();
		Button foundBtn = null;
		
		//iterate all drawables
		for(Widget drawable : ((ScreenMixin)(Object)this).getRenderables())
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