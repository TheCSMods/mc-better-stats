package io.github.thecsdev.betterstats.client.gui.screen;

import static io.github.thecsdev.betterstats.BetterStats.LOGGER;
import static io.github.thecsdev.betterstats.client.network.BetterStatsClientNetworkHandler.enableBSSProtocol;
import static io.github.thecsdev.betterstats.client.network.BetterStatsClientNetworkHandler.serverHasBSS;
import static io.github.thecsdev.betterstats.network.BetterStatsNetworkHandler.C2S_REQ_STATS;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Objects;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.BiMap;
import com.mojang.authlib.GameProfile;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.api.client.features.player.badges.BssClientPlayerBadge_Badgeless;
import io.github.thecsdev.betterstats.api.client.features.player.badges.BssClientPlayerBadges;
import io.github.thecsdev.betterstats.client.gui.other.BSTooltipElement;
import io.github.thecsdev.betterstats.client.gui.panel.BSPanel_Downloading;
import io.github.thecsdev.betterstats.client.gui.panel.BSPanel_Statistics;
import io.github.thecsdev.betterstats.client.gui.panel.network.BSNetworkProfilePanel;
import io.github.thecsdev.betterstats.client.network.BStatsListener;
import io.github.thecsdev.betterstats.client.network.BetterStatsClientNetworkHandler;
import io.github.thecsdev.betterstats.network.BSNetworkProfile;
import io.github.thecsdev.betterstats.util.StatUtils;
import io.github.thecsdev.betterstats.util.StatUtils.StatUtilsStat;
import io.github.thecsdev.tcdcommons.TCDCommons;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TTooltipElement;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreenPlus;
import io.github.thecsdev.tcdcommons.api.client.network.PlayerBadgeNetworkListener;
import io.github.thecsdev.tcdcommons.api.features.player.badges.PlayerBadge;
import io.github.thecsdev.tcdcommons.api.util.GenericProperties;
import io.github.thecsdev.tcdcommons.api.util.SubjectToChange;
import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;

public final class BetterStatsScreen extends TScreenPlus implements BStatsListener, PlayerBadgeNetworkListener
{
	// ==================================================
	public static enum CurrentTab
	{
		General(translatable("stat.generalButton")),
		Items(translatable("stat.itemsButton")),
		Entities(translatable("stat.mobsButton")),
		FoodStuffs(translatable("advancements.husbandry.balanced_diet.title")),
		MonstersHunted(translatable("advancements.adventure.kill_all_mobs.title"))/*,
		Options(translatable("menu.options"))*/;
		
		private final MutableText text;
		CurrentTab(MutableText text) { this.text = text; }
		public MutableText asText() { return text; }
	}
	public static enum GroupStatsBy
	{
		Default(translatable("betterstats.gui.filters.group_by.default")),
		Mod(translatable("betterstats.gui.filters.group_by.mod")),
		None(translatable("betterstats.gui.filters.group_by.none"));

		private final MutableText text;
		GroupStatsBy(MutableText text) { this.text = text; }
		public MutableText asText() { return text; }
	}
	// --------------------------------------------------
	public static final Identifier BSS_WIDGETS_TEXTURE = new Identifier(BetterStats.getModID(), "textures/gui/widgets.png");
	// ==================================================
	protected boolean STATUS_RECIEVED;
	protected int STATUS_TIMEOUT;
	// --------------------------------------------------
	public final Screen parent;
	public final GameProfile targetProfile;
	protected @Nullable BSNetworkProfile bStatHandler; //holds current stats
	// --------------------------------------------------
	protected BSPanel_Downloading panel_download;
	protected BSPanel_Statistics panel_stats;
	// --------------------------------------------------
	public CurrentTab filter_currentTab;
	public String     filter_searchTerm;
	public double     filter_statsScroll;
	public static boolean filter_showEmpty = false;
	public GroupStatsBy filter_groupBy;
	// --------------------------------------------------
	public final GenericProperties cache = new GenericProperties();
	public final boolean PLAYER_BADGES_ENABLED;
	// ==================================================
	/**
	 * Creates a {@link BetterStatsScreen} instance.
	 * @param parent The screen that was open before this one.
	 */
	public BetterStatsScreen(Screen parent) { this(parent, null); }
	
	/**
	 * Creates a {@link BetterStatsScreen} instance.
	 * @param parent The screen that was open before this one.
	 * @param gameProfile The requested player whose stats will be shown.
	 * @throws NullPointerException The {@link StatHandler} must not be null.
	 */
	public BetterStatsScreen(Screen parent, GameProfile gameProfile)
	{
		//initialize important stuff
		super(translatable("gui.stats"));
		this.STATUS_RECIEVED = false;
		this.STATUS_TIMEOUT = 0;
		this.client = MinecraftClient.getInstance(); //need this
		this.parent = parent;
		this.PLAYER_BADGES_ENABLED = TCDCommons.getInstance().getConfig().enablePlayerBadges;
		
		//always try to make sure the stat handler isn't null
		//by initializing it with a blank/empty profile
		if(gameProfile == null) gameProfile = this.client.player.getGameProfile();
		var cachedStatHandler = BetterStatsClientNetworkHandler.getCachedProfile(gameProfile);
		if(cachedStatHandler == null)
			cachedStatHandler = BSNetworkProfile.compareGameProfiles(gameProfile, this.client.player.getGameProfile()) ?
					BSNetworkProfile.ofLocalClient() : BSNetworkProfile.ofGameProfile(gameProfile);
		this.bStatHandler = cachedStatHandler;
		
		//define the rest
		this.targetProfile = gameProfile;
		this.filter_currentTab = CurrentTab.General;
		this.filter_searchTerm = "";
		this.filter_statsScroll = 0;
		this.filter_groupBy = GroupStatsBy.Default;
	}
	// ==================================================
	@SubjectToChange
	protected @Override TTooltipElement __createTooltip() { return new BSTooltipElement(getTpeWidth() / 2); }
	
	//prevent sending a status request every time this window
	//updates. instead, send it only once
	public @Override void onOpened()
	{
		if(!STATUS_RECIEVED) sendStatsRequest();
		//as of 1.19.3, item groups need a manual update
		@SuppressWarnings("resource")
		var wl = getClient().player.getWorld().getRegistryManager();
		ItemGroups.updateDisplayContext(FeatureSet.of(FeatureFlags.VANILLA), true, wl);
	}
	// --------------------------------------------------
	public @Override boolean shouldRenderInGameHud() { return false; }
	// --------------------------------------------------
	public @Override void tick()
	{
		if(this.STATUS_RECIEVED || this.STATUS_TIMEOUT > 60)
			return;
		else if(this.STATUS_TIMEOUT < 60)
			this.STATUS_TIMEOUT++;
		else if(this.STATUS_TIMEOUT == 60)
		{
			this.panel_download.onTimedOut();
			this.STATUS_TIMEOUT++;
		}
	}
	
	/**
	 * Sends a {@link ClientStatusC2SPacket} to the server
	 * with the {@link ClientStatusC2SPacket.Mode.REQUEST_STATS} mode,
	 * aka a statistics request.
	 */
	public void sendStatsRequest()
	{
		this.STATUS_RECIEVED = false;
		this.STATUS_TIMEOUT = 0;
		this.panel_download.onSendRequest();
		
		panel_download.setVisible(true);
		if(!sendStatsRequestPacket())
			this.panel_download.onTimedOut();
	}
	
	/**
	 * Sends a {@link ClientStatusC2SPacket} packet with the mode
	 * {@link Mode#REQUEST_STATS}. Aka it sends a statistics
	 * request to the server.
	 * @throws RuntimeException If something goes wrong while sending the packet.
	 * @return True if the request was sent successfully.
	 */
	public boolean sendStatsRequestPacket()
	{
		//prepare
		var client = getClient();
		var localPlayer = client.player;
		if(localPlayer == null) return false;
		
		//bss network protocol method
		var targetGameProfile = this.getListenerTargetGameProfile();
		if(!BSNetworkProfile.compareGameProfiles(targetGameProfile, localPlayer.getGameProfile()))
		{
			// ---------- if not requesting localPlayer stats
			//make sure bss is installed on the server, and
			//that the client is okay with sending bss requests to it
			if(!serverHasBSS || !enableBSSProtocol) return false;
			//create packet
			var data = new PacketByteBuf(Unpooled.buffer());
			BSNetworkProfile.writeGameProfile(data, targetGameProfile);
			var packet = new CustomPayloadC2SPacket(C2S_REQ_STATS, data);
			//send packet and return
			client.getNetworkHandler().sendPacket(packet);
			LOGGER.debug("Client requesting stats from server using CustomPayloadC2SPacket.");
			return true;
		}
		
		//vanilla network protocol method //if requesting localPlayer stats
		var packet = new ClientStatusC2SPacket(Mode.REQUEST_STATS);
		client.getNetworkHandler().sendPacket(packet);
		LOGGER.debug("Client requesting stats from server using ClientStatusC2SPacket.");
		return true;
	}
	// ==================================================
	/**
	 * Returns the {@link StatHandler} whose stats this
	 * {@link BetterStatsScreen} chooses to show on the screen.
	 */
	public StatHandler getStatHandler() { return this.bStatHandler.stats; }
	public @Nullable BSNetworkProfile getBSStatHandler() { return this.bStatHandler; }
	
	/**
	 * Returns the currently used stat panel.
	 */
	public @Nullable BSPanel_Statistics getStatPanel() { return this.panel_stats; }
	
	/**
	 * Returns the {@link StatUtilsStat} {@link Predicate} for this
	 * {@link BetterStatsScreen} that will filter out stats based
	 * on the search query and other filters.
	 */
	public Predicate<StatUtilsStat> getStatPredicate()
	{
		return getStatPredicate_emptyFilter().and(getStatPredicate_searchFilter());
	}
	
	public Predicate<StatUtilsStat> getStatPredicate_emptyFilter() { return stat -> filter_showEmpty || !stat.isEmpty(); }
	public Predicate<StatUtilsStat> getStatPredicate_searchFilter()
	{
		return stat ->
		{
			//approve if there is no search term
			if(StringUtils.isAllBlank(this.filter_searchTerm))
				return true;
			
			//obtain filter strings
			String lbl = stat.label.getString().toLowerCase().replaceAll("\\s+","");
			String st = this.filter_searchTerm.toLowerCase().replaceAll("\\s+","");
			
			//test
			return lbl.contains(st);
		};
	}
	// ==================================================
	protected @Override void init()
	{
		//initialize all elements
		panel_download = new BSPanel_Downloading(this);
		panel_stats = new BSPanel_Statistics(this);
		addTChild(panel_download);
		addTChild(panel_stats);
		
		//after initialization of all necessary elements is
		//done, start off by sending a request
		if(STATUS_RECIEVED) onBetterStatsReady(this.bStatHandler);
	}
	// --------------------------------------------------
	public @Override GameProfile getListenerTargetGameProfile() { return this.bStatHandler.gameProfile; }
	public @Override void onBetterStatsReady(BSNetworkProfile recievedProfile)
	{
		//tick client-side badge(s)
		BssClientPlayerBadge_Badgeless.instance.tick();
		
		//update the status flag
		STATUS_RECIEVED = true;
		this.bStatHandler = Objects.requireNonNull(recievedProfile);
		LOGGER.debug("Client received stats from server for: " + recievedProfile);
		
		//put some extra stat-based badges
		if(PLAYER_BADGES_ENABLED)
		{
			//play-time based badges
			final var playTime = this.bStatHandler.stats.getStat(Stats.CUSTOM, Stats.PLAY_TIME);
			if(playTime > 20736000)
				this.bStatHandler.playerBadgeIds.add(BssClientPlayerBadges.DEDICATION.getBadgeId());
			if(playTime > 20736000 * 2)
				this.bStatHandler.playerBadgeIds.add(BssClientPlayerBadges.LOYALTY.getBadgeId());
			
			//ender-dragon-egg based badge
			if(!new StatUtils.StatUtilsItemStat(getStatHandler(), Items.DRAGON_EGG).isEmpty())
				this.bStatHandler.playerBadgeIds.add(BssClientPlayerBadges.THE_NEXT_GEN.getBadgeId());
			
			//travel-based badge
			if(this.bStatHandler.stats.getStat(Stats.CUSTOM, Stats.WALK_ONE_CM) +
					this.bStatHandler.stats.getStat(Stats.CUSTOM, Stats.SPRINT_ONE_CM) +
					this.bStatHandler.stats.getStat(Stats.CUSTOM, Stats.AVIATE_ONE_CM) > 72700000)
				this.bStatHandler.playerBadgeIds.add(BssClientPlayerBadges.ADVENTUROUS_TRAVELER.getBadgeId());
			
			//survival-time based badge
			if(this.bStatHandler.stats.getStat(Stats.CUSTOM, Stats.TIME_SINCE_DEATH) > 864000)
				this.bStatHandler.playerBadgeIds.add(BssClientPlayerBadges.PERENNIAL_SURVIVOR.getBadgeId());
		}
		
		//hide the downloading panel
		//and show the statistics panel
		panel_download.setVisible(false);
		panel_stats.setVisible(true);
		
		//initialize the statistics panel
		panel_stats.clearTChildren();
		if(getStatHandler() != null) panel_stats.init();
	}
	public @Override void onStatsPlayerNotFound()
	{
		if(this.panel_download != null)
			this.panel_download.onPlayer404();
		this.STATUS_TIMEOUT = 256;
	}
	
	//TCDCommons network protocol handling - Important for fetching local/client-player's badges
	public @Override void onPlayerBadgesReady(BiMap<Identifier, PlayerBadge> badges)
	{
		//add all the badgies to the target profile,
		//and then re-init any network profile panels
		this.bStatHandler.playerBadgeIds.addAll(badges.keySet());
		reInit_BSNetworkProfilePanel();
	}
	public boolean reInit_BSNetworkProfilePanel()
	{
		if(!STATUS_RECIEVED) return false;
		final var npp = findTChildOfType(BSNetworkProfilePanel.class, true);
		if(npp != null) npp.init(this);
		else return false;
		return true;
	}
	// ==================================================
	public @Override void renderBackground(DrawContext pencil) { /*no background*/ }
	// ==================================================
}