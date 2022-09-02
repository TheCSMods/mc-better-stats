package thecsdev.betterstats.client.gui.screen;

import static thecsdev.betterstats.BetterStats.lt;
import static thecsdev.betterstats.BetterStats.tt;
import static thecsdev.betterstats.config.BSConfig.COLOR_CATEGORY_NAME_HIGHLIGHTED;
import static thecsdev.betterstats.config.BSConfig.COLOR_CATEGORY_NAME_NORMAL;
import static thecsdev.betterstats.config.BSConfig.COLOR_CONTENTPANE_BG;
import static thecsdev.betterstats.config.BSConfig.FILTER_HIDE_EMPTY_STATS;
import static thecsdev.betterstats.config.BSConfig.FILTER_SHOW_ITEM_NAMES;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Sets;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.StatsListener;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.Monster;
import net.minecraft.item.ItemGroup;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.stat.StatHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import thecsdev.betterstats.BetterStats;
import thecsdev.betterstats.client.gui.util.StatUtils;
import thecsdev.betterstats.client.gui.util.StatUtils.SUGeneralStat;
import thecsdev.betterstats.client.gui.util.StatUtils.SUItemStat;
import thecsdev.betterstats.client.gui.util.StatUtils.SUMobStat;
import thecsdev.betterstats.client.gui.widget.ActionCheckboxWidget;
import thecsdev.betterstats.client.gui.widget.CenteredTextWidget;
import thecsdev.betterstats.client.gui.widget.FillWidget;
import thecsdev.betterstats.client.gui.widget.FillWidget.FWBorderMode;
import thecsdev.betterstats.client.gui.widget.FillWidget.FWScrollOptions.FWScrollbarType;
import thecsdev.betterstats.client.gui.widget.StringWidget;
import thecsdev.betterstats.client.gui.widget.stats.BSGenetalStatWidget;
import thecsdev.betterstats.client.gui.widget.stats.BSItemStatWidget;
import thecsdev.betterstats.client.gui.widget.stats.BSMobStatWidget;

public class BetterStatsScreen extends ScreenWithScissors implements StatsListener
{
	// ==================================================
	private static final Text DOWNLOADING_STATS_TEXT = tt("multiplayer.downloadingStats");
	private static final Text NO_SEARCH_RESULTS_TEXT = tt("betterstats.gui.search.no_results");
	
	public enum CurrentTab
	{
		General(tt("stat.generalButton")),
		Items(tt("stat.itemsButton")),
		Entities(tt("stat.mobsButton")),
		
		FoodStuffs(tt("advancements.husbandry.balanced_diet.title")),
		MonstersHunted(tt("advancements.adventure.kill_all_mobs.title"));
		
		private final Text text;
		CurrentTab(Text text) { this.text = text; }
		public Text asText() { return text; }
	}
	// --------------------------------------------------
	//an attempt to preserve the selected tab when the Minecraft
	//window is being resized. for whatever reason, Minecraft
	//completely redraws a Screen when it's window is interacted with
	public static CurrentTab CACHE_TAB = CurrentTab.General;
	// --------------------------------------------------
	public final Screen parent;
	public final StatHandler statHandler;
	
	private boolean downloadingStats;
	// ==================================================
	public BetterStatsScreen(Screen parent, StatHandler statHandler)
	{
		super(tt("gui.stats"));
		this.parent = parent;
		this.statHandler = statHandler;
		downloadingStats = true; //must be true
	}
	
	@Override
	public boolean shouldPause() { return true; }
	
	@Override
	public void close()
	{
		super.close();
		BetterStatsScreen.CACHE_TAB = null;
	}

	public TextRenderer getTextRenderer() { return this.textRenderer; }
	public ItemRenderer getItemRenderer() { return this.itemRenderer; }
	// --------------------------------------------------
	@Override
	protected void init()
	{
		super.init();
		downloadingStats = true;
		ClientStatusC2SPacket statsRequest = new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.REQUEST_STATS);
		client.getNetworkHandler().sendPacket(statsRequest);
	}
	
	@Override
	public void onStatsReady()
	{
		if(!downloadingStats) return;
		downloadingStats = false;
		initElements();
	}
	// --------------------------------------------------
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		//render background
		renderBackground(matrices);
		
		//if still downloading, render the downloading screen
		if(downloadingStats || menuContentPane == null)
		{
			drawCenteredText(matrices, this.textRenderer, DOWNLOADING_STATS_TEXT, this.width / 2, this.height / 2, 16777215);
			drawCenteredText(matrices, this.textRenderer,
					PROGRESS_BAR_STAGES[(int)(Util.getMeasuringTimeMs() / 150L % PROGRESS_BAR_STAGES.length)],
					this.width / 2, this.height / 2 + 9 * 2, 16777215);
			return;
		}
		
		//when downloading is done, render the other elements
		super.render(matrices, mouseX, mouseY, delta);
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(mcp_txt_search != null) mcp_txt_search.tick();
	}
	// ==================================================
	public FillWidget menuContentPane;
	public FillWidget statContentPane;
	
	public CenteredTextWidget mcp_lbl_filters;
	public CyclingButtonWidget<CurrentTab> mcp_btn_tab;
	public TextFieldWidget mcp_txt_search;
	public ActionCheckboxWidget mcp_bool_hideEmpty;
	public ActionCheckboxWidget mcp_bool_showItemNames;
	
	public ButtonWidget mcp_btn_feedback;
	public ButtonWidget mcp_btn_vanilla;
	public ButtonWidget mcp_btn_close;
	// --------------------------------------------------
	public void initElements()
	{
		//checks and clear previous children
		if(downloadingStats) return;
		clearChildren();
		
		//content pane backgrounds
		int mcpX = 10, mcpY = 10, mcpW = (int)(width / 3.5f), mcpH = height - 20;
		menuContentPane = new FillWidget(mcpX, mcpY, mcpW, mcpH, COLOR_CONTENTPANE_BG)
				.withoutFocus();
		
		int scpX = mcpX + mcpW + 10;
		int scpY = mcpY;
		int scpW = width - scpX - 10;
		int scpH = mcpH;
		statContentPane = new FillWidget(scpX, scpY, scpW, scpH, COLOR_CONTENTPANE_BG)
				.withoutFocus()
				.withScroll(FWScrollbarType.AlwaysVertical, 0, 0);
		
		//menu buttons
		mcp_lbl_filters = new CenteredTextWidget(textRenderer, tt("betterstats.gui.filters"),
				mcpX + (mcpW / 2),
				mcpY + 5 + 7,
				Color.white.getRGB());
		
		mcp_btn_tab = CyclingButtonWidget.builder(CurrentTab::asText)
				.values(CurrentTab.values())
				.initially(CACHE_TAB == null ? CurrentTab.General : CACHE_TAB)
				.narration(btn -> ClickableWidget.getNarrationMessage(btn.getMessage()))
				.build(mcpX + 5, mcpY + 30, mcpW - 10, 20, tt("betterstats.gui.current_tab"), (arg0, arg1) ->
				{
					CACHE_TAB = arg1;
					this.update();
				});
		
		mcp_txt_search = new TextFieldWidget(textRenderer,
				mcpX + 5 + 1, mcpY + 55,
				mcpW - 10 - 2, 20,
				tt("betterstats.gui.search"));
		mcp_txt_search.setChangedListener(arg0 -> this.update());
		
		mcp_bool_hideEmpty = new ActionCheckboxWidget(
				mcpX + 5, mcpY + 80,
				mcpW - 10, 20,
				tt("betterstats.gui.hide_empty"), FILTER_HIDE_EMPTY_STATS)
				.withTooltip(tt("betterstats.gui.hide_empty.tooltip"));
		mcp_bool_hideEmpty.setChangedListener(arg0 ->
		{
			FILTER_HIDE_EMPTY_STATS = arg0.isChecked();
			this.update();
		});
		
		mcp_bool_showItemNames = new ActionCheckboxWidget(
				mcpX + 5, mcpY + 105,
				mcpW - 10, 20,
				tt("betterstats.gui.show_item_names"), FILTER_SHOW_ITEM_NAMES)
				.withTooltip(tt("betterstats.gui.show_item_names.tooltip"));
		mcp_bool_showItemNames.setChangedListener(arg0 ->
		{
			FILTER_SHOW_ITEM_NAMES = arg0.isChecked();
			for(Map.Entry<Drawable, FillWidget> dfc : drawablesForCutting.entrySet())
			{
				if(dfc.getKey() instanceof BSItemStatWidget)
					((BSItemStatWidget)dfc.getKey()).updateTooltip();
			}
		});
		//---------------
		mcp_btn_feedback = new ButtonWidget(
				mcpX + 5, mcpY + mcpH - 75,
				mcpW - 10, 20,
				tt("menu.sendFeedback"),
				btn -> this.client.setScreen(new ConfirmChatLinkScreen(arg0 ->
				{
					if(arg0) Util.getOperatingSystem().open(BetterStats.FeedbackSite);
					this.client.setScreen(this);
				}, BetterStats.FeedbackSite, true)));
		
		mcp_btn_vanilla = new ButtonWidget(
				mcpX + 5, mcpY + mcpH - 50,
				mcpW - 10, 20,
				tt("betterstats.gui.vanilla_stats_screen"),
				btn -> client.setScreen(new StatsScreen(parent, statHandler)));
		
		mcp_btn_close = new ButtonWidget(
				mcpX + 5, mcpY + mcpH - 25,
				mcpW - 10, 20,
				tt("betterstats.gui.close"),
				btn -> { client.setScreen(parent); CACHE_TAB = null; });
		
		//add items - the order is important
		addDrawableChild(menuContentPane);
		addDrawableChild(statContentPane);
		
		addCutDrawable(mcp_lbl_filters, menuContentPane);
		addCutDrawableChild(mcp_btn_tab, menuContentPane);
		addCutDrawableChild(mcp_txt_search, menuContentPane);
		addCutDrawableChild(mcp_bool_hideEmpty, menuContentPane);
		addCutDrawableChild(mcp_bool_showItemNames, menuContentPane);
		
		addCutDrawableChild(mcp_btn_feedback, menuContentPane);
		addCutDrawableChild(mcp_btn_vanilla, menuContentPane);
		addCutDrawableChild(mcp_btn_close, menuContentPane);
				
		//call update
		update();
	}
	
	public void update()
	{
		//clear all previous stat items
		Iterator<Map.Entry<Drawable, FillWidget>> dfcIter = drawablesForCutting.entrySet().iterator();
		HashSet<Drawable> dfcsToRemove = Sets.newHashSet();
		while(dfcIter.hasNext())
		{
			Map.Entry<Drawable, FillWidget> entry = dfcIter.next();
			if(entry.getValue() == statContentPane)
				dfcsToRemove.add(entry.getKey());
		}
		for (Drawable dfcToRemove : dfcsToRemove)
			removeDrawable(dfcToRemove);
		
		//draw based on selected tab
		Dimension lY_dE = null; //holds lastY and drawnEntries
		
		statContentPane.scroll.clearScrollData();
		switch(mcp_btn_tab.getValue())
		{
			case General: lY_dE = update_drawGeneralStats(); break;
			case Items: lY_dE = update_drawItemStats(); break;
			case Entities: lY_dE = update_drawEntityStats(); break;
			case FoodStuffs: lY_dE = update_drawFoodStats(); break;
			case MonstersHunted: lY_dE = update_drawHuntingStats(); break;
			default: break;
		}
		
		//update scroll and stuff based on lastY and drawnEntries
		int lastY = lY_dE.width, drawnEntries = lY_dE.height;
		
		if(drawnEntries > 0)
		{
			//set scroll
			if(lastY > statContentPane.y + statContentPane.getHeight())
				statContentPane.scroll.min = -Math.abs(lastY - statContentPane.getHeight() - 5);
		}
		else
		{
			//draw the 'no entires found' text
			addCutDrawable(new CenteredTextWidget(
					textRenderer,
					NO_SEARCH_RESULTS_TEXT,
					statContentPane.x + (statContentPane.getWidth() / 2),
					statContentPane.y + (statContentPane.getHeight() / 2),
					Color.white.getRGB()), statContentPane);
		}
	}
	
	private Dimension update_drawGeneralStats()
	{
		//data
		String search = mcp_txt_search.getText();
		int drawnEntries = 0;
		int nextX = statContentPane.x + 5, nextY = statContentPane.y + 5;
		int nextW = statContentPane.getWidth() - 10 - (8 /*scrollbar*/);
		int lastY = 0;
		
		//iterate all stats
		for (SUGeneralStat statEntry : StatUtils.getGeneralStats(statHandler))
		{
			//filter empty
			if(FILTER_HIDE_EMPTY_STATS && statEntry.intValue == 0)
				continue;
			
			//filter search
			if(search.length() > 0 && !StringUtils.containsIgnoreCase(statEntry.title.getString(), search))
				continue;
			drawnEntries++;
			
			//create and add a FillWidget for the entry background
			BSGenetalStatWidget bg = (BSGenetalStatWidget) new BSGenetalStatWidget(this, nextX, nextY, nextW, statEntry)
					.setTooltipZOffset(drawnEntries);
			statContentPane.scroll.makeScrollable(statContentPane, bg);
			addCutDrawableChild(bg, statContentPane);
			
			//increase next Y
			nextY += bg.getHeight() + 2;
			lastY = bg.y + bg.getHeight();
		}
		
		//return
		return new Dimension(lastY, drawnEntries);
	}
	
	private Dimension update_drawItemStats()
	{
		//data
		String search = mcp_txt_search.getText();
		int drawnEntries = 0;
		int nextX = statContentPane.x + 10;
		int nextY = statContentPane.y + 10;
		int lastY = nextY;
		
		//draw entries by groups
		for (Map.Entry<ItemGroup, ArrayList<SUItemStat>> groupEntry : StatUtils.getItemStats(statHandler, search).entrySet())
		{
			//reset nextX
			nextX = statContentPane.x + 10;
			
			//create label
			Text groupName = groupEntry.getKey() != null ? groupEntry.getKey().getDisplayName() : lt("*");
			StringWidget lbl_groupName = new StringWidget(textRenderer, groupName, nextX, nextY, COLOR_CATEGORY_NAME_HIGHLIGHTED);
			statContentPane.scroll.makeScrollable(statContentPane, lbl_groupName, nextX, nextY, arg0 -> { lbl_groupName.y = (int) (arg0.y + arg0.scroll); });
			addCutDrawable(lbl_groupName, statContentPane);
			
			nextY += lbl_groupName.getHeight() + 5;
			lastY = nextY;
			
			//iterate and add all items
			for(SUItemStat itemStat : groupEntry.getValue())
			{
				drawnEntries++;
				
				//create
				BSItemStatWidget bg = (BSItemStatWidget) new BSItemStatWidget(this, itemStat, 0, 0)
						.setTooltipZOffset(drawnEntries);
				
				//position
				if(nextX + bg.getWidth() > statContentPane.x + statContentPane.getWidth() - statContentPane.scroll.barTransform.width - 10)
				{
					nextX = statContentPane.x + 10;
					nextY += bg.getHeight() + 1;
				}
				bg.x = nextX;
				bg.y = nextY;
				nextX += bg.getWidth() + 1;
				lastY = nextY + bg.getHeight();
				
				//apply scroll and add
				statContentPane.scroll.makeScrollable(statContentPane, bg);
				addCutDrawableChild(bg, statContentPane);
			}
			
			//increase nextY
			nextY = lastY + 15;
		}
		
		//return
		return new Dimension(lastY, drawnEntries);
	}
	
	private Dimension update_drawEntityStats()
	{
		//data
		String search = mcp_txt_search.getText();
		int drawnEntries = 0;
		int nextX = statContentPane.x + 10;
		int nextY = statContentPane.y + 10;
		int lastY = nextY;
		
		//draw entries by groups
		LinkedHashMap<String, ArrayList<SUMobStat>> groupEntries = StatUtils.getMobStats(statHandler, search);
		for (Map.Entry<String, ArrayList<SUMobStat>> groupEntry : groupEntries.entrySet())
		{
			//reset nextX
			nextX = statContentPane.x + 10;
			
			//create label
			if(groupEntries.size() > 1)
			{
				Text groupName = lt(StatUtils.getModNameFromID(groupEntry.getKey()));
				StringWidget lbl_groupName = new StringWidget(textRenderer, groupName, nextX, nextY, COLOR_CATEGORY_NAME_HIGHLIGHTED);
				statContentPane.scroll.makeScrollable(statContentPane, lbl_groupName, nextX, nextY, arg0 -> { lbl_groupName.y = (int) (arg0.y + arg0.scroll); });
				addCutDrawable(lbl_groupName, statContentPane);
				
				nextY += lbl_groupName.getHeight() + 5;
				lastY = nextY;
			}
			
			//iterate all mobs
			for(SUMobStat mobStat : groupEntry.getValue())
			{
				drawnEntries++;
				
				//create
				BSMobStatWidget bg = (BSMobStatWidget) new BSMobStatWidget(this, mobStat, 0, 0)
						.setTooltipZOffset(drawnEntries);
				
				//position
				if(nextX + bg.getWidth() > statContentPane.x + statContentPane.getWidth() - statContentPane.scroll.barTransform.width - 10)
				{
					nextX = statContentPane.x + 10;
					nextY += bg.getHeight() + 2;
				}
				bg.x = nextX;
				bg.y = nextY;
				nextX += bg.getWidth() + 2;
				lastY = nextY + bg.getHeight();
				
				//apply scroll and add
				statContentPane.scroll.makeScrollable(statContentPane, bg);
				addCutDrawableChild(bg, statContentPane);
			}
			
			//increase nextY
			nextY = lastY + 15;
		}
		
		//return
		return new Dimension(lastY, drawnEntries);
	}
	
	private Dimension update_drawFoodStats()
	{
		//data
		String search = mcp_txt_search.getText();
		int drawnEntries = 0;
		int nextX = statContentPane.x + 10;
		int nextY = statContentPane.y + 10;
		int lastY = nextY;
		
		//draw entries by groups
		LinkedHashMap<String, ArrayList<SUItemStat>> groupEntries = StatUtils.getFoodStats(statHandler, itemStat ->
		{
			//filter items
			String itemName = tt(itemStat.item.getTranslationKey()).getString();
			boolean b0 = StringUtils.isAllBlank(search) || StringUtils.containsIgnoreCase(itemName, search);
			boolean b1 = !FILTER_HIDE_EMPTY_STATS || StatUtils.ITEM_STAT_EMPTY_FILTER.apply(itemStat);
			return b0 && b1;
		});
		
		for (Map.Entry<String, ArrayList<SUItemStat>> groupEntry : groupEntries.entrySet())
		{
			//reset nextX
			nextX = statContentPane.x + 10;
			
			//create label
			StringWidget lbl_groupName = null;
			if(groupEntries.size() > 1)
			{
				Text groupName = lt(StatUtils.getModNameFromID(groupEntry.getKey()));
				lbl_groupName = new StringWidget(textRenderer, groupName, nextX, nextY, COLOR_CATEGORY_NAME_NORMAL);
				
				final StringWidget lbl = lbl_groupName;
				statContentPane.scroll.makeScrollable(statContentPane, lbl_groupName, nextX, nextY, arg0 -> { lbl.y = (int) (arg0.y + arg0.scroll); });
				addCutDrawable(lbl_groupName, statContentPane);
				
				nextY += lbl_groupName.getHeight() + 5;
				lastY = nextY;
			}
			
			//iterate all items
			boolean ateAllItems = true;
			for(SUItemStat itemStat : groupEntry.getValue())
			{
				drawnEntries++;
				
				//create
				BSItemStatWidget bg = (BSItemStatWidget) new BSItemStatWidget(this, itemStat, 0, 0)
						.setTooltipZOffset(drawnEntries);
				
				//outline eaten items with a special color
				if(itemStat.used > 0)
					bg.withBorder(FWBorderMode.Always, COLOR_CATEGORY_NAME_HIGHLIGHTED);
				else ateAllItems = false;
				
				//position
				if(nextX + bg.getWidth() > statContentPane.x + statContentPane.getWidth() - statContentPane.scroll.barTransform.width - 10)
				{
					nextX = statContentPane.x + 10;
					nextY += bg.getHeight() + 3;
				}
				bg.x = nextX;
				bg.y = nextY;
				nextX += bg.getWidth() + 3;
				lastY = nextY + bg.getHeight();
				
				//apply scroll and add
				statContentPane.scroll.makeScrollable(statContentPane, bg);
				addCutDrawableChild(bg, statContentPane);
			}
			
			//special color for eating all items
			if(lbl_groupName != null && ateAllItems)
				lbl_groupName.color = COLOR_CATEGORY_NAME_HIGHLIGHTED;
			
			//increase nextY
			nextY = lastY + 15;
		}
		
		//return
		return new Dimension(lastY, drawnEntries);
	}
	
	private Dimension update_drawHuntingStats()
	{
		//data
		String search = mcp_txt_search.getText();
		int drawnEntries = 0;
		int nextX = statContentPane.x + 10;
		int nextY = statContentPane.y + 10;
		int lastY = nextY;
		
		//draw entries by groups
		LinkedHashMap<String, ArrayList<SUMobStat>> groupEntries = StatUtils.getMobStats(statHandler, mobStat ->
		{
			//filter
			String entityName = tt(mobStat.entityType.getTranslationKey()).getString();
			boolean b0 = StringUtils.isAllBlank(search) ||
					StringUtils.containsIgnoreCase(entityName, search);
			boolean b1 = !FILTER_HIDE_EMPTY_STATS || StatUtils.MOB_STAT_EMPTY_FILTER.apply(mobStat);
			boolean b2 = mobStat.entity instanceof Monster;
			return b0 && b1 && b2;
		});
		
		for (Map.Entry<String, ArrayList<SUMobStat>> groupEntry : groupEntries.entrySet())
		{
			//reset nextX
			nextX = statContentPane.x + 10;
			
			//create label
			StringWidget lbl_groupName = null;
			if(groupEntries.size() > 1)
			{
				Text groupName = lt(StatUtils.getModNameFromID(groupEntry.getKey()));
				lbl_groupName = new StringWidget(textRenderer, groupName, nextX, nextY, COLOR_CATEGORY_NAME_NORMAL);
				
				StringWidget lbl = lbl_groupName;
				statContentPane.scroll.makeScrollable(statContentPane, lbl_groupName, nextX, nextY, arg0 -> { lbl.y = (int) (arg0.y + arg0.scroll); });
				addCutDrawable(lbl_groupName, statContentPane);
				
				nextY += lbl_groupName.getHeight() + 5;
				lastY = nextY;
			}
			
			//iterate all mobs
			boolean allMonstersHunted = true;
			for(SUMobStat mobStat : groupEntry.getValue())
			{
				drawnEntries++;
				
				//create
				BSMobStatWidget bg = (BSMobStatWidget) new BSMobStatWidget(this, mobStat, 0, 0)
						.setTooltipZOffset(drawnEntries);
				
				//outline killed monsters with a special color
				if(mobStat.killed > 0)
					bg.withBorder(FWBorderMode.Always, COLOR_CATEGORY_NAME_HIGHLIGHTED);
				else allMonstersHunted = false;
				
				//position
				if(nextX + bg.getWidth() > statContentPane.x + statContentPane.getWidth() - statContentPane.scroll.barTransform.width - 10)
				{
					nextX = statContentPane.x + 10;
					nextY += bg.getHeight() + 3;
				}
				bg.x = nextX;
				bg.y = nextY;
				nextX += bg.getWidth() + 3;
				lastY = nextY + bg.getHeight();
				
				//apply scroll and add
				statContentPane.scroll.makeScrollable(statContentPane, bg);
				addCutDrawableChild(bg, statContentPane);
			}
			
			//special color for hunting all monsters
			if(lbl_groupName != null && allMonstersHunted)
				lbl_groupName.color = COLOR_CATEGORY_NAME_HIGHLIGHTED;
			
			//increase nextY
			nextY = lastY + 15;
		}
		
		//return
		return new Dimension(lastY, drawnEntries);
	}
	// ==================================================
}