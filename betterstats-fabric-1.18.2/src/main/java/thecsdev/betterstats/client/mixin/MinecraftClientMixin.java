package thecsdev.betterstats.client.mixin;

import java.io.IOException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.crash.CrashException;
import thecsdev.betterstats.config.BSConfig;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin
{
	@Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("RETURN"))
	public void onDisconnect(Screen screen, CallbackInfo callback)
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