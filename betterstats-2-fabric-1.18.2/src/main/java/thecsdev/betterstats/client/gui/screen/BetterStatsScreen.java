package thecsdev.betterstats.client.gui.screen;

import static thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.StatsListener;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.stat.StatHandler;
import net.minecraft.text.MutableText;
import thecsdev.betterstats.client.gui.other.BSTooltipElement;
import thecsdev.betterstats.client.gui.panel.BSPanel_Downloading;
import thecsdev.betterstats.client.gui.panel.BSPanel_Statistics;
import thecsdev.betterstats.util.StatUtils.StatUtilsStat;
import thecsdev.tcdcommons.api.client.gui.other.TTooltipElement;
import thecsdev.tcdcommons.api.client.gui.screen.TScreenPlus;
import thecsdev.tcdcommons.api.util.SubjectToChange;
import thecsdev.tcdcommons.api.util.TextUtils;

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
	// --------------------------------------------------
	public static final String FEEDBACK_URL = "https://github.com/TheCSDev/mc-better-stats";
	// ==================================================
	protected boolean STATUS_RECIEVED;
	// --------------------------------------------------
	public final Screen parent;
	protected final StatHandler localStatHandler;
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
		super(TextUtils.translatable("gui.stats"));
		this.STATUS_RECIEVED = false;
		this.client = MinecraftClient.getInstance(); //need this
		this.parent = parent;
		this.localStatHandler = this.client.player.getStatHandler();
		
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
		ClientStatusC2SPacket statsRequest = new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.REQUEST_STATS);
		client.getNetworkHandler().sendPacket(statsRequest);
	}
	// ==================================================
	/**
	 * Returns the {@link StatHandler} whose stats this
	 * {@link BetterStatsScreen} chooses to show on the screen.
	 */
	public StatHandler getStatHandler() { return this.localStatHandler; } 
	
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