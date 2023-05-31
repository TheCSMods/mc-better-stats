package io.github.thecsdev.betterstats.client.gui.panel;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;

import java.awt.Color;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.client.gui.panel.network.BSNetworkSearchPanel;
import io.github.thecsdev.betterstats.client.gui.panel.stats.BSStatPanel;
import io.github.thecsdev.betterstats.client.gui.panel.stats.BSStatPanel_BalancedDiet;
import io.github.thecsdev.betterstats.client.gui.panel.stats.BSStatPanel_General;
import io.github.thecsdev.betterstats.client.gui.panel.stats.BSStatPanel_Items;
import io.github.thecsdev.betterstats.client.gui.panel.stats.BSStatPanel_Mobs;
import io.github.thecsdev.betterstats.client.gui.panel.stats.BSStatPanel_MonsterHunter;
import io.github.thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.betterstats.client.gui.widget.BSScrollBarWidget;
import io.github.thecsdev.tcdcommons.api.client.gui.TParentElement;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.util.HorizontalAlignment;
import net.minecraft.client.util.math.MatrixStack;

public class BSPanel_Statistics extends BSPanel
{
	// ==================================================
	protected final BetterStatsScreen betterStats;
	// --------------------------------------------------
	protected BSPanel_StatisticsMenuBar panel_menuBar;
	protected BSPanel_StatisticsFilters panel_leftMenu;
	protected BSPanel                   panel_rightMenu;
	protected BSStatPanel               panel_stats;
	protected BSScrollBarWidget         scroll_left;
	protected BSNetworkSearchPanel      panel_networkSearch;
	// ==================================================
	public BSPanel_Statistics(BetterStatsScreen bss)
	{
		super(bss.getTpeX(), bss.getTpeY(), bss.getTpeWidth(), bss.getTpeHeight());
		this.betterStats = Objects.requireNonNull(bss, "bss must not be null.");
		setVisible(false);
	}
	// --------------------------------------------------
	@Override
	public boolean canBeAddedTo(TParentElement parent) { return parent == this.betterStats; }
	// --------------------------------------------------
	public @Nullable BSStatPanel getCurrentStatPanel() { return this.panel_stats; }
	// ==================================================
	public void init()
	{
		//dimensions
		int mcpX = 10, mcpY = 10, mcpW = (int)(getTpeWidth() / 3.5f), mcpH = getTpeHeight() - 20;
		int scpX = mcpX + mcpW + 10, scpY = mcpY, scpW = getTpeWidth() - scpX - 10, scpH = mcpH;
		mcpY += 10; mcpH -= 10;
		scpY += 10; scpH -= 10;
		
		//menu bar panel,
		//left and right menu panels
		panel_menuBar = new BSPanel_StatisticsMenuBar(mcpX, 0, Math.abs(mcpX - (scpX + scpW)), 15);
		panel_leftMenu = new BSPanel_StatisticsFilters(mcpX, mcpY, mcpW, mcpH - 25);
		panel_rightMenu = new BSPanel(scpX + 4, scpY, scpW - 4, scpH);
		panel_stats = null;
		panel_networkSearch = new BSNetworkSearchPanel(mcpX, mcpY + mcpH - 20, mcpW + 8, 20);
		
		//add panel elements
		addTChild(panel_menuBar);
		addTChild(panel_leftMenu);
		addTChild(panel_rightMenu);
		addTChild(panel_networkSearch);
		
		//scroll bar
		scroll_left = new BSScrollBarWidget(
				panel_leftMenu.getTpeEndX() - 1, panel_leftMenu.getTpeY(),
				8, panel_leftMenu.getTpeHeight(),
				panel_leftMenu);
		
		//add scroll bar elements
		addTChild(scroll_left);
		
		//and now for the menus and statistics
		init_menuBar(); //1
		init_stats(); //2
		init_leftMenu(); //3
		panel_networkSearch.init(this.betterStats);
		
		//create label for target player name
		var str_playerName = literal(this.betterStats.targetProfile.getProfileDisplayName());
		var str_playerName_w = getTextRenderer().getWidth(str_playerName) + 5;
		var lbl_playerName = new TLabelElement(
				panel_menuBar.getTpeEndX() - (str_playerName_w + 5), panel_menuBar.getTpeY() + 1,
				str_playerName_w, panel_menuBar.getTpeHeight());
		lbl_playerName.setZOffset(panel_menuBar.getZOffset() + 5);
		lbl_playerName.setHorizontalAlignment(HorizontalAlignment.RIGHT);
		lbl_playerName.setColor(Color.YELLOW.getRGB(), Color.YELLOW.getRGB());
		lbl_playerName.setText(str_playerName);
		addTChild(lbl_playerName, false);
	}
	
	protected void init_menuBar()
	{
		//null check and clear the previous menu
		if(this.panel_menuBar == null) return;
		this.panel_menuBar.clearTChildren();
		//init the menu
		this.panel_menuBar.init(this.betterStats);
	}
	
	public void init_leftMenu()
	{
		//null check and clear the previous menu
		if(this.panel_leftMenu == null) return;
		this.panel_leftMenu.clearTChildren();
		//init the panel
		this.panel_leftMenu.init(this.betterStats);
	}
	
	public void init_stats()
	{
		//null check and clear the previous stats
		if(this.panel_rightMenu == null) return;
		this.panel_rightMenu.clearTChildren();
		//obtain predicate
		var bssSh = betterStats.getStatHandler();
		var bssSp = betterStats.getStatPredicate();
		//init the stats panel
		BSStatPanel sPanel = null;
		switch(betterStats.filter_currentTab)
		{
			case General:
				sPanel = new BSStatPanel_General(panel_rightMenu);
				sPanel.init(betterStats, bssSh, bssSp);
				break;
			case Items:
				sPanel = new BSStatPanel_Items(panel_rightMenu);
				sPanel.init(betterStats, bssSh, bssSp);
				break;
			case Entities:
				sPanel = new BSStatPanel_Mobs(panel_rightMenu);
				sPanel.init(betterStats, bssSh, bssSp);
				break;
			case MonstersHunted:
				//use a different filter here
				sPanel = new BSStatPanel_MonsterHunter(panel_rightMenu);
				sPanel.init(betterStats, bssSh, betterStats.getStatPredicate_searchFilter());
				break;
			case FoodStuffs:
				//use a different filter here
				sPanel = new BSStatPanel_BalancedDiet(panel_rightMenu);
				sPanel.init(betterStats, bssSh, betterStats.getStatPredicate_searchFilter());
				break;
			default:
				panel_rightMenu.clearTChildren();
				break;
		}
		//set scroll from cache
		if(sPanel != null)
		{
			var scroll = sPanel.getVerticalScrollBar();
			scroll.setValue(betterStats.filter_statsScroll);
			sPanel.eScrollVertically.register((element, dY) -> betterStats.filter_statsScroll = scroll.getValue());
		}
		//assign stat current panel
		this.panel_stats = sPanel;
	}
	// ==================================================
	@Override
	public void postRender(MatrixStack matrices, int mouseX, int mouseY, float deltaTime) { /*nope, no outline*/ }
	// ==================================================
}