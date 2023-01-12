package io.github.thecsdev.betterstats.client.mixin.trackers;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.client.gui_hud.screen.BetterStatsHudScreen;
import io.github.thecsdev.betterstats.client.gui_hud.screen.BshsAutoRequest;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;

/**
 * This {@link Mixin} tracks all {@link Packet}s sent from
 * <b>this</b> client to the server. It then uses those packets
 * to try and track things that are taking place in an attempt
 * to track statistics client-side.
 */
@Mixin(value = Connection.class, priority = 501, remap = true)
public abstract class MixinClientConnection
{
	// ==================================================
	protected @Shadow PacketFlow receiving;
	protected abstract @Shadow boolean isConnected();
	// ==================================================
	/**
	 * Client-side listener for C2S packets.
	 */
	@Inject(method = "doSendPacket", at = @At("HEAD"))
	public void onPreSendImmediately(
			Packet<?> packet,
			@Nullable PacketSendListener p_243290_,
			ConnectionProtocol p_243203_,
			ConnectionProtocol p_243307_,
			CallbackInfo ci)
	{
		//ensure client-side for both physical and logical side
		if(!BetterStats.isClient() || this.receiving != PacketFlow.CLIENTBOUND)
			return;
		
		//---------- track packets sent to the server (C2S)
		//BshsAutoRequest flag handling
		if(packet instanceof ServerboundMovePlayerPacket.Rot)
			BshsAutoRequest.flag_moved = true;
		else if (packet instanceof ServerboundSwingPacket)
			BshsAutoRequest.flag_handSwung = true;
	}
	
	/**
	 * Client-side listener for S2C packets.
	 */
	@Inject(method = "genericsFtw", at = @At(value = "HEAD"))
	private static void onPreHandlePacket(Packet<?> packet, PacketListener listener, CallbackInfo callback)
	{
		//ensure client-side for both physical and logical side
		if(!BetterStats.isClient() || listener.getConnection().getDirection() != PacketFlow.CLIENTBOUND)
			return;
		
		//---------- track the packets sent here from the server (S2C)
		//update stats hud when receiving statistics
		if(packet instanceof ClientboundAwardStatsPacket)
		{
			var bshs = BetterStatsHudScreen.getInstance(); //do not create new instances
			if(bshs != null && bshs.flag_tickChildren < 1)
				bshs.flag_tickChildren = 5; //schedule the widget update
		}
	}
	// ==================================================
}