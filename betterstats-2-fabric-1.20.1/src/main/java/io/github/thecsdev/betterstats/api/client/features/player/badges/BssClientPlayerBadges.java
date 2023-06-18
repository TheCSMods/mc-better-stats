package io.github.thecsdev.betterstats.api.client.features.player.badges;

import static io.github.thecsdev.tcdcommons.api.registry.TCDCommonsRegistry.PlayerBadges;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import io.github.thecsdev.betterstats.BetterStats;
import net.minecraft.util.Identifier;

public final class BssClientPlayerBadges
{
	// ==================================================
	protected BssClientPlayerBadges() {}
	// ==================================================
	//public static final BssPlayerBadge_Test TEST;
	
	public static final BssClientPlayerBadge_Badgeless BADGELESS;
	public static final BssClientPlayerBadge_Custom DEDICATION;
	public static final BssClientPlayerBadge_Custom LOYALTY;
	public static final BssClientPlayerBadge_Custom THE_NEXT_GEN;
	
	/*public static final BssClientPlayerBadge_Custom BSS_SUPPORTER;
	public static final BssClientPlayerBadge_Custom BSS_DEBUGGER;
	public static final BssClientPlayerBadge_Custom BSS_TRANSLATOR;*/
	// ==================================================
	public static void register() {/*calls static*/}
	static
	{
		final var mId = BetterStats.getModID();
		//PlayerBadges.put(BssPlayerBadge_Test.BADGE_ID, TEST = BssPlayerBadge_Test.instance);
		
		PlayerBadges.put(BssClientPlayerBadge_Badgeless.BADGE_ID, BADGELESS = BssClientPlayerBadge_Badgeless.instance);
		PlayerBadges.put(new Identifier(mId, "dedication"), DEDICATION = new BssClientPlayerBadge_Custom("dedication")
				.setUVCoords(220, 20, 20, 20));
		PlayerBadges.put(new Identifier(mId, "loyalty"), LOYALTY = new BssClientPlayerBadge_Custom("loyalty")
				.setUVCoords(200, 20, 20, 20));
		PlayerBadges.put(new Identifier(mId, "the_next_generation"), THE_NEXT_GEN = new BssClientPlayerBadge_Custom("the_next_generation")
				.setUVCoords(220, 40, 20, 20));
		THE_NEXT_GEN.setName(translatable("advancements.end.dragon_egg.title"));
		
		/*PlayerBadges.put(new Identifier(mId, "bss_supporter"), BSS_SUPPORTER = new BssClientPlayerBadge_Custom("bss_supporter")
				.setUVCoords(220, 60, 20, 20));
		PlayerBadges.put(new Identifier(mId, "bss_debugger"), BSS_DEBUGGER = new BssClientPlayerBadge_Custom("bss_debugger")
				.setUVCoords(200, 60, 20, 20));
		PlayerBadges.put(new Identifier(mId, "bss_translator"), BSS_TRANSLATOR = new BssClientPlayerBadge_Custom("bss_translator")
				.setUVCoords(180, 60, 20, 20));*/
	}
	// ==================================================
}