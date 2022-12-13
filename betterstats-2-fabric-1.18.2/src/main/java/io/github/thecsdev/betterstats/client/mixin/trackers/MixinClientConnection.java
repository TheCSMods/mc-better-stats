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
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;

/**
 * This {@link Mixin} tracks all {@link Packet}s sent from
 * <b>this</b> client to the server. It then uses those packets
 * to try and track things that are taking place in an attempt
 * to track statistics client-side.
 */
@Mixin(value = ClientConnection.class, priority = 501)
public abstract class MixinClientConnection
{
	// ==================================================
	protected @Shadow NetworkSide side;
	protected abstract @Shadow boolean isOpen();
	// ==================================================
	/**
	 * Client-side listener for C2S packets.
	 */
	@Inject(method = "sendImmediately", at = @At("HEAD"))
	public void onPreSendImmediately(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> callback, CallbackInfo ci)
	{
		//ensure client-side for both physical and logical side
		if(!BetterStats.isClient() || this.side != NetworkSide.CLIENTBOUND)
			return;
		
		//---------- track packets sent to the server (C2S)
		//BshsAutoRequest flag handling
		if(packet instanceof LookAndOnGround)
			BshsAutoRequest.flag_moved = true;
		else if (packet instanceof HandSwingC2SPacket)
			BshsAutoRequest.flag_handSwung = true;
	}
	
	/**
	 * Client-side listener for S2C packets.
	 */
	@Inject(method = "handlePacket", at = @At(value = "HEAD"))
	private static void onPreHandlePacket(Packet<?> packet, PacketListener listener, CallbackInfo callback)
	{
		//ensure client-side for both physical and logical side
		if(!BetterStats.isClient() || listener.getConnection().getSide() != NetworkSide.CLIENTBOUND)
			return;
		
		//---------- track the packets sent here from the server (S2C)
		//update stats hud when receiving statistics
		if(packet instanceof StatisticsS2CPacket)
		{
			var bshs = BetterStatsHudScreen.getInstance(); //do not create new instances
			if(bshs != null && bshs.flag_tickChildren < 1)
				bshs.flag_tickChildren = 5; //schedule the widget update
		}
	}
	// ==================================================
}