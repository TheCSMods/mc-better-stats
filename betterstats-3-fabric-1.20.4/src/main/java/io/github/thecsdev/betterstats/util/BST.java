package io.github.thecsdev.betterstats.util;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;
import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.BetterStats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

/**
 * {@link BetterStats} {@link Text}s.
 * @apiNote Used to keep track of all of {@link BetterStats}'s {@link Text}s in one place.
 */
public final @Internal class BST
{
	// ==================================================
	private BST() {}
	// ==================================================
	// --------------------------------------------------
	public static final MutableText sTab_noStatsYet() { return translatable("betterstats.client.gui.stats.panel.statstabpanel.no_stats_yet"); }
	public static final MutableText sTab_hashedSeed() { return translatable("betterstats.client.gui.stats.panel.statstabpanel.seed_sha256"); }
	// --------------------------------------------------
	public static final MutableText config_debugMode()                     { return translatable("betterstats.api.client.gui.screen.betterstatsconfigscreen.debug_mode"); }
	public static final MutableText config_guiSmoothScroll()               { return translatable("betterstats.api.client.gui.screen.betterstatsconfigscreen.gui_smooth_scroll"); }
	public static final MutableText config_guiSmoothScroll_tooltip()       { return translatable("betterstats.api.client.gui.screen.betterstatsconfigscreen.gui_smooth_scroll.tooltip"); }
	public static final MutableText config_guiMobsFollowCursor()           { return translatable("betterstats.api.client.gui.screen.betterstatsconfigscreen.gui_mob_follow_cursor"); }
	public static final MutableText config_trustAllServersBssNet()         { return translatable("betterstats.api.client.gui.screen.betterstatsconfigscreen.trust_all_servers_bss_net"); }
	public static final MutableText config_trustAllServersBssNet_tooltip() { return translatable("betterstats.api.client.gui.screen.betterstatsconfigscreen.trust_all_servers_bss_net.tooltip"); }
	public static final MutableText config_registerCommands()              { return translatable("betterstats.api.client.gui.screen.betterstatsconfigscreen.register_commands"); }
	public static final MutableText config_enableSas()                     { return translatable("betterstats.api.client.gui.screen.betterstatsconfigscreen.enable_sas"); }
	// --------------------------------------------------
	public static final MutableText pbadge_dedication_title()       { return translatable("betterstats.api.client.registry.bsclientplayerbadges.dedication.title"); }
	public static final MutableText pbadge_dedication_description() { return translatable("betterstats.api.client.registry.bsclientplayerbadges.dedication.description"); }
	public static final MutableText pbadge_loyalty_title()          { return translatable("betterstats.api.client.registry.bsclientplayerbadges.loyalty.title"); }
	public static final MutableText pbadge_loyalty_description()    { return translatable("betterstats.api.client.registry.bsclientplayerbadges.loyalty.description"); }
	public static final MutableText pbadge_nextgen_title()          { return translatable("betterstats.api.client.registry.bsclientplayerbadges.the_next_generation.title"); }
	public static final MutableText pbadge_nextgen_description()    { return translatable("betterstats.api.client.registry.bsclientplayerbadges.the_next_generation.description"); }
	public static final MutableText pbadge_advtravel_title()        { return translatable("betterstats.api.client.registry.bsclientplayerbadges.adventurous_traveler.title"); }
	public static final MutableText pbadge_advtravel_description()  { return translatable("betterstats.api.client.registry.bsclientplayerbadges.adventurous_traveler.description"); }
	public static final MutableText pbadge_psurvivor_title()        { return translatable("betterstats.api.client.registry.bsclientplayerbadges.perennial_survivor.title"); }
	public static final MutableText pbadge_psurvivor_description()  { return translatable("betterstats.api.client.registry.bsclientplayerbadges.perennial_survivor.description"); }
	// --------------------------------------------------
	public static final MutableText hud_title()           { return translatable("betterstats.client.gui.screen.hud.betterstatshudscreen"); }
	public static final MutableText hud_pinStat()         { return translatable("betterstats.client.gui.screen.hud.betterstatshudscreen.pin_stat"); }
	public static final MutableText hud_liveStatsToggle() { return translatable("betterstats.client.gui.screen.hud.betterstatshudscreen.live_stats_toggle"); }
	public static final MutableText hud_tutorial1()       { return translatable("betterstats.client.gui.screen.hud.betterstatshudscreen.tutorial_1"); }
	public static final MutableText hud_tutorial2()       { return translatable("betterstats.client.gui.screen.hud.betterstatshudscreen.tutorial_2"); }
	public static final MutableText hud_tutorial3()       { return translatable("betterstats.client.gui.screen.hud.betterstatshudscreen.tutorial_3"); }
	// --------------------------------------------------
	public static final MutableText net_toggleTooltip()  { return translatable("betterstats.network.betterstatsnetworkhandler.toggle_tooltip"); }
	public static final MutableText net_consentWarning() { return translatable("betterstats.network.betterstatsnetworkhandler.consent_warning"); }
	// --------------------------------------------------
	public static final MutableText sas_firstMine(Text player, Text block)         { return translatable("betterstats.util.stats.statannouncementsystem.first_mine", player, block); }
	public static final MutableText sas_firstCraft(Text player, Text item)         { return translatable("betterstats.util.stats.statannouncementsystem.first_craft", player, item); }
	public static final MutableText sas_firstDeath(Text player)                    { return translatable("betterstats.util.stats.statannouncementsystem.first_death", player); }
	public static final MutableText sas_firstDeath_hc1(Text player)                { return translatable("betterstats.util.stats.statannouncementsystem.first_death.hc1", player); }
	public static final MutableText sas_firstKill(Text player, Text entity)        { return translatable("betterstats.util.stats.statannouncementsystem.first_kill", player, entity); }
	public static final MutableText sas_firstDeathTo(Text player, Text entity)     { return translatable("betterstats.util.stats.statannouncementsystem.first_death_to", player, entity); }
	public static final MutableText sas_custom(Text player, Text stat, Text value) { return translatable("betterstats.util.stats.statannouncementsystem.custom", player, stat, value); }
	// ==================================================
}