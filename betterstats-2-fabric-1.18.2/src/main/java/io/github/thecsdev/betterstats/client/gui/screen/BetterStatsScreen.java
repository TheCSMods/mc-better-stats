package io.github.thecsdev.betterstats.client.gui.screen;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Objects;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.client.gui.other.BSTooltipElement;
import io.github.thecsdev.betterstats.client.gui.panel.BSPanel_Downloading;
import io.github.thecsdev.betterstats.client.gui.panel.BSPanel_Statistics;
import io.github.thecsdev.betterstats.network.BSNetworkProfile;
import io.github.thecsdev.betterstats.util.StatUtils.StatUtilsStat;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TTooltipElement;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreenPlus;
import io.github.thecsdev.tcdcommons.api.util.GenericProperties;
import io.github.thecsdev.tcdcommons.api.util.SubjectToChange;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.StatsListener;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket.Mode;
import net.minecraft.stat.StatHandler;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;

public class BetterStatsScreen extends TScreenPlus implements StatsListener
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
	public static final String FEEDBACK_URL = "https://github.com/TheCSDev/mc-better-stats";
	// ==================================================
	protected boolean STATUS_RECIEVED;
	// --------------------------------------------------
	public final Screen parent;
	public final BSNetworkProfile targetProfile;
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
	// ==================================================
	/**
	 * Creates a {@link BetterStatsScreen} instance.
	 * @param parent The screen that was open before this one.
	 */
	public BetterStatsScreen(Screen parent) { this(parent, BSNetworkProfile.ofLocalClient()); }
	
	/**
	 * Creates a {@link BetterStatsScreen} instance.
	 * @param parent The screen that was open before this one.
	 * @param targetProfile The statistics handler for the local player.
	 * @throws NullPointerException The {@link StatHandler} must not be null.
	 */
	public BetterStatsScreen(Screen parent, BSNetworkProfile targetProfile)
	{
		super(translatable("gui.stats"));
		this.STATUS_RECIEVED = false;
		this.client = MinecraftClient.getInstance(); //need this
		this.parent = parent;
		this.targetProfile = Objects.requireNonNull(targetProfile);
		
		this.filter_currentTab = CurrentTab.General;
		this.filter_searchTerm = "";
		this.filter_statsScroll = 0;
		this.filter_groupBy = GroupStatsBy.Default;
	}
	
	@SubjectToChange
	protected @Override TTooltipElement __createTooltip() { return new BSTooltipElement(getTpeWidth() / 2); }
	
	//prevent sending a status request every time this window
	//updates. instead, send it only once
	public @Override void onOpened() { if(!STATUS_RECIEVED) sendStatsRequest(); }
	// --------------------------------------------------
	public @Override boolean shouldRenderInGameHud() { return false; }
	// --------------------------------------------------
	/**
	 * Sends a {@link ClientStatusC2SPacket} to the server
	 * with the {@link ClientStatusC2SPacket.Mode.REQUEST_STATS} mode,
	 * aka a statistics request.
	 */
	public void sendStatsRequest()
	{
		panel_download.setVisible(true);
		sendStatsRequestPacket();
	}
	
	/**
	 * Sends a {@link ClientStatusC2SPacket} packet with the mode
	 * {@link Mode#REQUEST_STATS}. Aka it sends a statistics
	 * request to the server.
	 * @throws RuntimeException If something goes wrong while sending the packet.
	 */
	public static void sendStatsRequestPacket()
	{
		var client = MinecraftClient.getInstance();
		var packet = new ClientStatusC2SPacket(Mode.REQUEST_STATS);
		client.getNetworkHandler().sendPacket(packet);
	}
	// ==================================================
	/**
	 * Returns the {@link StatHandler} whose stats this
	 * {@link BetterStatsScreen} chooses to show on the screen.
	 */
	public StatHandler getStatHandler() { return this.targetProfile.stats; }
	
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
		if(STATUS_RECIEVED) onStatsReady();
	}
	// --------------------------------------------------
	public @Override void onStatsReady()
	{
		//update the status flag
		STATUS_RECIEVED = true;
		
		//hide the downloading panel
		//and show the statistics panel
		panel_download.setVisible(false);
		panel_stats.setVisible(true);
		
		//initialize the statistics panel
		panel_stats.clearTChildren();
		if(getStatHandler() != null) panel_stats.init();
	}
	// ==================================================
	public @Override void renderBackground(MatrixStack matrices) { /*no background*/ }
	// ==================================================
}