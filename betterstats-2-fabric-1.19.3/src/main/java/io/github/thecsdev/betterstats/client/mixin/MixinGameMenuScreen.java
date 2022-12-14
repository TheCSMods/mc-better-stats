package io.github.thecsdev.betterstats.client.mixin;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;

@Mixin(GameMenuScreen.class)
public abstract class MixinGameMenuScreen extends Screen
{
	// --------------------------------------------------
	protected MixinGameMenuScreen(Text title) { super(title); }
	// --------------------------------------------------
	@Inject(method = "initWidgets", at = @At("RETURN"))
	public void onInitWidgets(CallbackInfo callback)
	{
		MinecraftClient.getInstance().execute(() ->
		{
			//locate the original stats button
			ButtonWidget ogStatsBtn = betterstats_snipeButton(translatable("gui.stats"));
			if(ogStatsBtn == null) return;
			
			//replace it's function
			((MixinButtonWidget)ogStatsBtn).setOnPress(btn ->
			{
				final MinecraftClient client = MinecraftClient.getInstance();
				client.setScreen(new BetterStatsScreen(client.currentScreen));
			});
		});
	}
	// --------------------------------------------------
	private ButtonWidget betterstats_snipeButton(Text buttonText)
	{
		return betterstats_snipeButton(buttonText,
				((MixinScreen)(Object)this).getSelectables()
				.stream()
				.map(e -> (Element)e)
				.toList());
	}
	
	@SuppressWarnings("unchecked") //list casting
	private ButtonWidget betterstats_snipeButton(Text buttonText, List<Element> elements)
	{
		String btnTxtStr = buttonText.getString();
		ButtonWidget foundBtn = null;
		
		//iterate all drawables
		for(Element selectable : elements)
		{
			//check grids
			if(selectable instanceof GridWidget)
			{
				GridWidget grid = (GridWidget)selectable;
				return betterstats_snipeButton(buttonText, (List<Element>)grid.children());
			}
			
			//ignore non-buttons
			if(!(selectable instanceof ButtonWidget))
				continue;
			ButtonWidget btn = (ButtonWidget)selectable;
			
			//compare texts
			if(!btnTxtStr.equals(btn.getMessage().getString()))
				continue;
			
			//return the button
			foundBtn = btn;
		}
		
		//return the button if found
		return foundBtn;
	}
	// --------------------------------------------------
}