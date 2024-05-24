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
	public static final MutableText bss()                    { return translatable("betterstats"); }
	public static final MutableText bss_translators_title()  { return translatable("betterstats.translators.title"); }
	public static final MutableText bss_contributors_title() { return translatable("betterstats.contributors.title"); }
	// --------------------------------------------------
	public static final MutableText cmd_stats_edit_out(Text stat, Text affectedPlayerCount) { return translatable("commands.statistics.edit.output", stat, affectedPlayerCount); }
	public static final MutableText cmd_stats_clear_kick()                                  { return translatable("commands.statistics.clear.kick"); }
	public static final MutableText cmd_stats_clear_out(Text affectedPlayerCount)           { return translatable("commands.statistics.clear.output", affectedPlayerCount); }
	public static final MutableText cmd_stats_query_out(Text player, Text stat, Text value) { return translatable("commands.statistics.query.output", player, stat, value); }
	// --------------------------------------------------
	public static final MutableText stp_mc_killed()   { return translatable("betterstats.stattype_phrase.minecraft.killed"); }
	public static final MutableText stp_mc_killedBy() { return translatable("betterstats.stattype_phrase.minecraft.killed_by"); }
	// --------------------------------------------------
	public static final MutableText menu_file()         { return translatable("betterstats.api.client.gui.stats.panel.menubarpanel.menu_file"); }
	public static final MutableText menu_file_new()     { return translatable("betterstats.api.client.gui.stats.panel.menubarpanel.menu_file.new"); }
	public static final MutableText menu_file_open()    { return translatable("betterstats.api.client.gui.stats.panel.menubarpanel.menu_file.open"); }
	public static final MutableText menu_file_save()    { return translatable("betterstats.api.client.gui.stats.panel.menubarpanel.menu_file.save"); }
	public static final MutableText menu_file_saveAs()  { return translatable("betterstats.api.client.gui.stats.panel.menubarpanel.menu_file.save_as"); }
	public static final MutableText menu_view()         { return translatable("betterstats.api.client.gui.stats.panel.menubarpanel.menu_view"); }
	public static final MutableText menu_view_vStats()  { return translatable("betterstats.api.client.gui.stats.panel.menubarpanel.menu_view.vanilla_stats"); }
	public static final MutableText menu_about()        { return translatable("betterstats.api.client.gui.stats.panel.menubarpanel.menu_about"); }
	public static final MutableText menu_about_src()    { return translatable("betterstats.api.client.gui.stats.panel.menubarpanel.menu_about.source"); }
	public static final MutableText menu_about_cf()     { return translatable("betterstats.api.client.gui.stats.panel.menubarpanel.menu_about.curseforge"); }
	public static final MutableText menu_about_mr()     { return translatable("betterstats.api.client.gui.stats.panel.menubarpanel.menu_about.modrinth"); }
	public static final MutableText menu_about_yt()     { return translatable("betterstats.api.client.gui.stats.panel.menubarpanel.menu_about.youtube"); }
	public static final MutableText menu_about_kofi()   { return translatable("betterstats.api.client.gui.stats.panel.menubarpanel.menu_about.kofi"); }
	public static final MutableText menu_about_dc()     { return translatable("betterstats.api.client.gui.stats.panel.menubarpanel.menu_about.discord"); }
	// --------------------------------------------------
	public static final MutableText gpp_uuid() { return translatable("betterstats.api.client.gui.stats.panel.gameprofilepanel.uuid"); }
	// --------------------------------------------------
	public static final MutableText filters()                  { return translatable("betterstats.api.client.gui.stats.panel.statfilterspanel.filters"); }
	public static final MutableText filter_showEmptyStats()    { return translatable("betterstats.api.client.gui.stats.panel.statfilterspanel.show_empty_stats"); }
	public static final MutableText filter_noFiltersQuestion() { return translatable("betterstats.api.client.gui.stats.panel.statfilterspanel.no_filters_question"); }
	//
	public static final MutableText filter_groupBy_default() { return translatable("betterstats.api.util.enumerations.filtergroupby.default"); }
	public static final MutableText filter_groupBy_mod()     { return translatable("betterstats.api.util.enumerations.filtergroupby.mod"); }
	// --------------------------------------------------
	public static final @Deprecated MutableText sWidget_mob_kills()  { return stp_mc_killed(); /*return translatable("betterstats.api.client.gui.stats.widget.mobstatwidget.kills");*/ }
	public static final @Deprecated MutableText sWidget_mob_deaths() { return stp_mc_killedBy(); /*return translatable("betterstats.api.client.gui.stats.widget.mobstatwidget.deaths");*/ }
	public static final MutableText sWidget_pbadge_obtained() { return translatable("betterstats.api.client.gui.stats.widget.playerbadgestatwidget.obtained"); }
	public static final MutableText sWidget_general_value()   { return translatable("betterstats.api.client.gui.stats.widget.generalstatwidget.value"); }
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
	public static final MutableText net_s3psTooltip()    { return translatable("betterstats.network.betterstatsnetworkhandler.s3ps_tooltip"); }
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