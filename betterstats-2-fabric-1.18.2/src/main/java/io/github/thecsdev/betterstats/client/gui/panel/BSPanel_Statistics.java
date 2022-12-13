package io.github.thecsdev.betterstats.client.gui.panel;

import java.util.Objects;
import java.util.function.Consumer;

import io.github.thecsdev.betterstats.client.gui.panel.stats.BSStatPanel;
import io.github.thecsdev.betterstats.client.gui.panel.stats.BSStatPanel_BalancedDiet;
import io.github.thecsdev.betterstats.client.gui.panel.stats.BSStatPanel_General;
import io.github.thecsdev.betterstats.client.gui.panel.stats.BSStatPanel_Items;
import io.github.thecsdev.betterstats.client.gui.panel.stats.BSStatPanel_Mobs;
import io.github.thecsdev.betterstats.client.gui.panel.stats.BSStatPanel_MonsterHunter;
import io.github.thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.betterstats.client.gui.widget.BSScrollBarWidget;
import io.github.thecsdev.tcdcommons.api.client.gui.TParentElement;
import net.minecraft.client.util.math.MatrixStack;

public class BSPanel_Statistics extends BSPanel
{
	// ==================================================
	protected final BetterStatsScreen betterStats;
	// --------------------------------------------------
	protected BSPanel_StatisticsMenuBar panel_menuBar;
	protected BSPanel_StatisticsFilters panel_leftMenu;
	protected BSPanel                   panel_rightMenu;
	protected BSScrollBarWidget         scroll_left;
	// --------------------------------------------------
	//prevent the garbage collector from collecting these event handlers
	protected Consumer<Integer> __handler0;
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
		panel_leftMenu = new BSPanel_StatisticsFilters(mcpX, mcpY, mcpW, mcpH);
		panel_rightMenu = new BSPanel(scpX + 4, scpY, scpW - 4, scpH);
		
		//add panel elements
		addTChild(panel_menuBar);
		addTChild(panel_leftMenu);
		addTChild(panel_rightMenu);
		
		//scroll bar
		scroll_left = new BSScrollBarWidget(
				panel_leftMenu.getTpeEndX() - 1, panel_leftMenu.getTpeY(),
				8, panel_leftMenu.getTpeHeight(),
				panel_leftMenu);
		
		//add scroll bar elements
		addTChild(scroll_left);
		
		//and now for the menus and statistics
		init_menuBar();
		init_leftMenu();
		init_stats();
	}
	
	protected void init_menuBar()
	{
		//null check and clear the previous menu
		if(this.panel_menuBar == null) return;
		this.panel_menuBar.clearTChildren();
		//init the menu
		this.panel_menuBar.init(this.betterStats);
	}
	
	protected void init_leftMenu()
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
				sPanel.init(bssSh, bssSp);
				break;
			case Items:
				sPanel = new BSStatPanel_Items(panel_rightMenu);
				sPanel.init(bssSh, bssSp);
				break;
			case Entities:
				sPanel = new BSStatPanel_Mobs(panel_rightMenu);
				sPanel.init(bssSh, bssSp);
				break;
			case MonstersHunted:
				//use a different filter here
				sPanel = new BSStatPanel_MonsterHunter(panel_rightMenu);
				sPanel.init(bssSh, betterStats.getStatPredicate_searchFilter());
				break;
			case FoodStuffs:
				//use a different filter here
				sPanel = new BSStatPanel_BalancedDiet(panel_rightMenu);
				sPanel.init(bssSh, betterStats.getStatPredicate_searchFilter());
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
			__handler0 = sPanel.getEvents().SCROLL_V
					.addWeakEventHandler(dY -> betterStats.filter_statsScroll = scroll.getValue());
		}
	}
	// ==================================================
	@Override
	public void postRender(MatrixStack matrices, int mouseX, int mouseY, float deltaTime) { /*nope, no outline*/ }
	// ==================================================
}