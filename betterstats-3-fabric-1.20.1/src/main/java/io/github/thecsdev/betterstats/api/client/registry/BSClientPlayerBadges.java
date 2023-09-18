package io.github.thecsdev.betterstats.api.client.registry;

import static io.github.thecsdev.betterstats.api.client.gui.panel.BSComponentPanel.BS_WIDGETS_TEXTURE;
import static io.github.thecsdev.tcdcommons.api.client.registry.TClientRegistries.PLAYER_BADGE_RENDERER;
import static io.github.thecsdev.tcdcommons.api.registry.TRegistries.PLAYER_BADGE;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.awt.Rectangle;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.api.client.badge.BSClientPlayerBadge;
import io.github.thecsdev.betterstats.api.client.render.badge.BSClientPlayerBadgeRenderer;
import io.github.thecsdev.betterstats.api.util.stats.SUItemStat;
import io.github.thecsdev.tcdcommons.api.client.badge.ClientPlayerBadge;
import io.github.thecsdev.tcdcommons.api.client.gui.util.UITexture;
import io.github.thecsdev.tcdcommons.api.registry.TRegistries;
import net.minecraft.item.Items;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;

public final class BSClientPlayerBadges
{
	// ==================================================
	private BSClientPlayerBadges() {}
	// ==================================================
	//public static final ClientPlayerBadge BADGELESS; -- not a real badge
	public static final ClientPlayerBadge DEDICATION;
	public static final ClientPlayerBadge LOYALTY;
	public static final ClientPlayerBadge THE_NEXT_GENERATION;
	public static final ClientPlayerBadge ADVENTUROUS_TRAVELER;
	public static final ClientPlayerBadge PERENNIAL_SURVIVOR;
	// ==================================================
	/**
	 * Registers the {@link BSClientPlayerBadge}s to the {@link TRegistries#PLAYER_BADGE} registry.
	 * @apiNote May only be called once.
	 */
	public static final void register() {}
	static
	{
		//create badges
		final var dedication = new BSClientPlayerBadge(
				translatable("betterstats.api.client.registry.bsclientplayerbadges.dedication.title"),
				translatable("betterstats.api.client.registry.bsclientplayerbadges.dedication.description"),
				new UITexture(BS_WIDGETS_TEXTURE, new Rectangle(220, 20, 20, 20)));
		final var loyalty = new BSClientPlayerBadge(
				translatable("betterstats.api.client.registry.bsclientplayerbadges.loyalty.title"),
				translatable("betterstats.api.client.registry.bsclientplayerbadges.loyalty.description"),
				new UITexture(BS_WIDGETS_TEXTURE, new Rectangle(200, 20, 20, 20)));
		final var the_next_generation = new BSClientPlayerBadge(
				translatable("betterstats.api.client.registry.bsclientplayerbadges.the_next_generation.title"),
				translatable("betterstats.api.client.registry.bsclientplayerbadges.the_next_generation.description"),
				new UITexture(BS_WIDGETS_TEXTURE, new Rectangle(220, 40, 20, 20)));
		final var adventurous_traveler = new BSClientPlayerBadge(
				translatable("betterstats.api.client.registry.bsclientplayerbadges.adventurous_traveler.title"),
				translatable("betterstats.api.client.registry.bsclientplayerbadges.adventurous_traveler.description"),
				new UITexture(BS_WIDGETS_TEXTURE, new Rectangle(200, 40, 20, 20)));
		final var perennial_survivor = new BSClientPlayerBadge(
				translatable("betterstats.api.client.registry.bsclientplayerbadges.perennial_survivor.title"),
				translatable("betterstats.api.client.registry.bsclientplayerbadges.perennial_survivor.description"),
				new UITexture(BS_WIDGETS_TEXTURE, new Rectangle(180, 20, 20, 20)));
		
		DEDICATION           = dedication;
		LOYALTY              = loyalty;
		THE_NEXT_GENERATION  = the_next_generation;
		ADVENTUROUS_TRAVELER = adventurous_traveler;
		PERENNIAL_SURVIVOR   = perennial_survivor;
		
		//set badge criteria
		dedication.setStatCriteria(sp -> (sp.getStatValue(Stats.CUSTOM, Stats.PLAY_TIME) > 20736000) ? 1 : 0);
		loyalty.setStatCriteria(sp -> (sp.getStatValue(Stats.CUSTOM, Stats.PLAY_TIME) > 20736000 * 2) ? 1 : 0);
		the_next_generation.setStatCriteria(sp -> new SUItemStat(sp, Items.DRAGON_EGG).isEmpty() ? 0 : 1);
		adventurous_traveler.setStatCriteria(sp ->
		{
			boolean a = sp.getStatValue(Stats.CUSTOM, Stats.WALK_ONE_CM) +
						sp.getStatValue(Stats.CUSTOM, Stats.SPRINT_ONE_CM) +
						sp.getStatValue(Stats.CUSTOM, Stats.AVIATE_ONE_CM) > 72700000;
			return a ? 1 : 0;
		});
		perennial_survivor.setStatCriteria(sp -> sp.getStatValue(Stats.CUSTOM, Stats.TIME_SINCE_DEATH) > 864000 ? 1 : 0);
		
		//register badges
		final String modId = BetterStats.getModID();
		PLAYER_BADGE.register(new Identifier(modId, "dedication"), DEDICATION);
		PLAYER_BADGE.register(new Identifier(modId, "loyalty"), LOYALTY);
		PLAYER_BADGE.register(new Identifier(modId, "the_next_generation"), THE_NEXT_GENERATION);
		PLAYER_BADGE.register(new Identifier(modId, "adventurous_traveler"), ADVENTUROUS_TRAVELER);
		PLAYER_BADGE.register(new Identifier(modId, "perennial_survivor"), PERENNIAL_SURVIVOR);
		
		//register badge renderers (must come AFTER registering badges)
		PLAYER_BADGE_RENDERER.register(DEDICATION.getId(), new BSClientPlayerBadgeRenderer(dedication));
		PLAYER_BADGE_RENDERER.register(LOYALTY.getId(), new BSClientPlayerBadgeRenderer(loyalty));
		PLAYER_BADGE_RENDERER.register(THE_NEXT_GENERATION.getId(), new BSClientPlayerBadgeRenderer(the_next_generation));
		PLAYER_BADGE_RENDERER.register(ADVENTUROUS_TRAVELER.getId(), new BSClientPlayerBadgeRenderer(adventurous_traveler));
		PLAYER_BADGE_RENDERER.register(PERENNIAL_SURVIVOR.getId(), new BSClientPlayerBadgeRenderer(perennial_survivor));
	}
	// ==================================================
}