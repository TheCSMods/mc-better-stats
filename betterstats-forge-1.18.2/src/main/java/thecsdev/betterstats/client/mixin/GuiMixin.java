package thecsdev.betterstats.client.mixin;

import java.io.IOException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.Gui;
import thecsdev.betterstats.config.BSConfig;
import thecsdev.fabric2forge.bss_p758.util.crash.CrashException;

@Mixin(Gui.class)
public class GuiMixin
{
	@Inject(method = "onDisconnected", at = @At("RETURN"))
	public void onOnDisconnected(CallbackInfo callback)
	{
		try { BSConfig.saveProperties(); }
		catch(CrashException exc)
		{
			//ignore catching the exception if something
			//other than IO stuff triggered it
			if(!(exc.getCause() instanceof IOException))
				throw exc;
		}
	}
}