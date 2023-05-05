package io.github.thecsdev.betterstats.client;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.List;

import dev.architectury.event.EventResult;
import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.betterstats.client.gui_hud.screen.BetterStatsHudScreen;
import io.github.thecsdev.betterstats.client.gui_hud.screen.BshsAutoRequest;
import io.github.thecsdev.betterstats.client.network.BetterStatsClientNetworkHandler;
import io.github.thecsdev.tcdcommons.api.client.events.screen.TGameMenuScreenEvent;
import io.github.thecsdev.tcdcommons.api.client.hooks.TGuiHooks;
import io.github.thecsdev.tcdcommons.api.events.TNetworkEvent;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import net.minecraft.text.Text;

public final class BetterStatsClient extends BetterStats implements ClientModInitializer
{
	// ==================================================
	public static boolean DEBUG_MODE = false;
	// ==================================================
	@Override
	public void onInitializeClient()
	{
		//MixinGameMenuScreen - Override the "Statistics" button functionality.
		TGameMenuScreenEvent.INIT_WIDGETS_POST.register(gmScreen ->
		{
			MinecraftClient.getInstance().execute(() ->
			{
				//locate the original stats button
				ButtonWidget ogStatsBtn = betterstats_snipeButton(gmScreen, translatable("gui.stats"));
				if(ogStatsBtn == null) return;
				
				//replace it's function
				TGuiHooks.setButtonPressAction(ogStatsBtn, btn ->
				{
					final MinecraftClient client = MinecraftClient.getInstance();
					client.setScreen(new BetterStatsScreen(client.currentScreen));
				});
			});
		});
		
		//MixinClientConnection - Tracking packets
		TNetworkEvent.SEND_PACKET_PRE.register((packet, netSide) ->
		{
			//ensure client-side for both physical and logical side
			if(!BetterStats.isClient() || netSide != NetworkSide.CLIENTBOUND)
				return EventResult.pass();
			
			//---------- track packets sent to the server (C2S)
			//BshsAutoRequest flag handling
			if(packet instanceof LookAndOnGround)
				BshsAutoRequest.flag_moved = true;
			else if (packet instanceof HandSwingC2SPacket)
				BshsAutoRequest.flag_handSwung = true;
			
			//return
			return EventResult.pass();
		});
		TNetworkEvent.RECEIVE_PACKET_PRE.register((packet, netSide) ->
		{
			//ensure client-side for both physical and logical side
			if(!BetterStats.isClient() || netSide != NetworkSide.CLIENTBOUND)
				return EventResult.pass();
			
			//---------- track the packets sent here from the server (S2C)
			//update stats hud when receiving statistics
			if(packet instanceof StatisticsS2CPacket)
			{
				var bshs = BetterStatsHudScreen.getInstance(); //do not create new instances
				if(bshs != null && bshs.flag_tickChildren < 1)
					bshs.flag_tickChildren = 3; //schedule the widget update
			}
			
			//return
			return EventResult.pass();
		});

		//init stuff
		BetterStatsClientNetworkHandler.init();
	}
	// ==================================================
	private ButtonWidget betterstats_snipeButton(Screen screen, Text buttonText)
	{
		return betterstats_snipeButton(buttonText,
				dev.architectury.hooks.client.screen.ScreenHooks.getNarratables(screen)
				.stream()
				.map(e -> (Element)e)
				.toList());
	}
	
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
				var gridCh = TGuiHooks.getGridWidgetChildren(grid).stream().map(i -> (Element)i).toList();
				return betterstats_snipeButton(buttonText, gridCh);
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
	// ==================================================
}