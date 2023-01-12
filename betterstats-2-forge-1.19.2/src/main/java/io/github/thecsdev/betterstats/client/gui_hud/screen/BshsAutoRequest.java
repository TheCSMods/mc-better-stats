package io.github.thecsdev.betterstats.client.gui_hud.screen;

import io.github.thecsdev.betterstats.client.gui.screen.BetterStatsScreen;

public final class BshsAutoRequest
{
	// ==================================================
	private BshsAutoRequest() {}
	static { resetFlags(); }
	// ==================================================
	public static int flag_cooldown; //ticks
	public static boolean flag_moved;
	public static boolean flag_handSwung;
	// ==================================================
	public static void tick()
	{
		//check if can tick
		if(!canTick()) return;
		//decrease the cooldown timer
		else if(flag_cooldown > 0) flag_cooldown--;
		//send the request when it is ready to be sent
		if(isReady()) sendRequest();
	}
	
	private static void sendRequest()
	{
		BetterStatsScreen.sendStatsRequestPacket();
		resetFlags();
	}
	// --------------------------------------------------
	public static boolean canTick() { return flag_moved && flag_handSwung; }
	public static boolean isReady() { return flag_cooldown < 1 && flag_moved && flag_handSwung; }
	public static void resetFlags()
	{
		flag_cooldown = 300;
		flag_moved = false;
		flag_handSwung = false;
	}
	// ==================================================
}