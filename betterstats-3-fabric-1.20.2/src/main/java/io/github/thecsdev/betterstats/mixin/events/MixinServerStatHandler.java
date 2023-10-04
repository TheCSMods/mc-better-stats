package io.github.thecsdev.betterstats.mixin.events;

import static io.github.thecsdev.betterstats.api.util.enumerations.ItemStatType.isItemStat;
import static io.github.thecsdev.betterstats.api.util.enumerations.MobStatType.isMobStat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.thecsdev.betterstats.network.BetterStatsNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stat;

@Mixin(ServerStatHandler.class)
public abstract class MixinServerStatHandler
{
	public abstract @Shadow void sendStats(ServerPlayerEntity player);
	
	@Inject(method = "setStat", at = @At("RETURN"))
	public void onSetStat(PlayerEntity player, Stat<?> stat, int value, CallbackInfo ci)
	{
		//skip if one of the following applies:
		//- if not an item or mob stat
		//- if a server player entity is not supplied
		if(!(isItemStat(stat) || isMobStat(stat)) || !(player instanceof ServerPlayerEntity))
			return;
		
		//handle live stats updates
		final var p = (ServerPlayerEntity)player;
		if(p.getServer().isRunning())
			BetterStatsNetworkHandler.s2c_liveStats(p);
	}
}