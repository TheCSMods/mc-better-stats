package io.github.thecsdev.betterstats.client.gui.screen;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.thecsdev.betterstats.client.gui.other.BSTooltipElement;
import io.github.thecsdev.betterstats.client.gui.panel.BSPanel_Downloading;
import io.github.thecsdev.betterstats.client.gui.panel.BSPanel_Statistics;
import io.github.thecsdev.betterstats.util.StatUtils.StatUtilsStat;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TTooltipElement;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreenPlus;
import io.github.thecsdev.tcdcommons.api.util.SubjectToChange;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.achievement.StatsUpdateListener;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.stats.StatsCounter;

public class BetterStatsScreen extends TScreenPlus implements StatsUpdateListener
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
		
		private final MutableComponent text;
		CurrentTab(MutableComponent text) { this.text = text; }
		public MutableComponent asText() { return text; }
	}
	// --------------------------------------------------
	public static final String FEEDBACK_URL = "https://github.com/TheCSDev/mc-better-stats";
	// ==================================================
	protected boolean STATUS_RECIEVED;
	// --------------------------------------------------
	public final Screen parent;
	protected final StatsCounter localStatHandler;
	// --------------------------------------------------
	protected BSPanel_Downloading panel_download;
	protected BSPanel_Statistics panel_stats;
	// --------------------------------------------------
	public CurrentTab filter_currentTab;
	public String     filter_searchTerm;
	public double     filter_statsScroll;
	public static boolean filter_showEmpty = false;
	// ==================================================
	/**
	 * Creates a {@link BetterStatsScreen} instance.
	 * @param parent The screen that was open before this one.
	 * @param localStatHandler The statistics handler for the local player.
	 * @throws NullPointerException The {@link StatHandler} must not be null.
	 */
	public BetterStatsScreen(Screen parent)
	{
		super(translatable("gui.stats"));
		this.STATUS_RECIEVED = false;
		this.minecraft = Minecraft.getInstance(); //need this
		this.parent = parent;
		this.localStatHandler = this.minecraft.player.getStats();
		
		this.filter_currentTab = CurrentTab.General;
		this.filter_searchTerm = "";
		this.filter_statsScroll = 0;
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
		var client = Minecraft.getInstance();
		var packet = new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS);
		client.getConnection().send(packet);
		//client.getNetworkHandler().sendPacket(packet);
	}
	// ==================================================
	/**
	 * Returns the {@link StatHandler} whose stats this
	 * {@link BetterStatsScreen} chooses to show on the screen.
	 */
	public StatsCounter getStatHandler() { return this.localStatHandler; } 
	
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
		if(STATUS_RECIEVED) onStatsUpdated();
	}
	// --------------------------------------------------
	public @Override void onStatsUpdated()
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
	public @Override void renderBackground(PoseStack matrices) { /*no background*/ }
	// ==================================================
}