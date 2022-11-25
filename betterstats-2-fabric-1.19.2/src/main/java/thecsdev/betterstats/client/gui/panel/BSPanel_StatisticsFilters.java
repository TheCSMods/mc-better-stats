package thecsdev.betterstats.client.gui.panel;

import static thecsdev.betterstats.client.gui.screen.BetterStatsScreen.filter_showEmpty;
import static thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import thecsdev.betterstats.client.gui.screen.BetterStatsScreen.CurrentTab;
import thecsdev.tcdcommons.api.client.gui.other.TBlankElement;
import thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import thecsdev.tcdcommons.api.client.gui.util.HorizontalAlignment;
import thecsdev.tcdcommons.api.client.gui.widget.TButtonWidget;
import thecsdev.tcdcommons.api.client.gui.widget.TCheckboxWidget;
import thecsdev.tcdcommons.api.client.gui.widget.TSelectEnumWidget;
import thecsdev.tcdcommons.api.client.gui.widget.TTextFieldWidget;

public class BSPanel_StatisticsFilters extends BSPanel
{
	// ==================================================
	//prevent the garbage collector from collecting these event handlers
	protected Runnable         __handler0;
	protected Consumer<String> __handler1;
	// --------------------------------------------------
	public @Nullable TSelectEnumWidget<CurrentTab> btn_tab;
	// ==================================================
	public BSPanel_StatisticsFilters(int x, int y, int width, int height)
	{
		super(x, y, width, height);
		setSmoothScroll(true);
	}
	// ==================================================
	//@SuppressWarnings("resource") //getClient() is safe to call
	public void init(BetterStatsScreen bss)
	{
		//blank placeholder element (used for getLastTChild())
		var blank = new TBlankElement(getTpeX(), getTpeY() + 5, 0, 0);
		addTChild(blank, false);
		
		//nametag label
		/*{
			BSPanel p = new BSPanel(10, 10, getTpeWidth() - 20, 20);
			var lbl = new TLabelElement(0, 0, getTpeWidth() - 20, 20);
			lbl.setHorizontalAlignment(HorizontalAlignment.CENTER);
			lbl.setText(getClient().player.getDisplayName());
			p.addTChild(lbl);
			addTChild(p);
		}
		//avatar showcase
		{
			int w = getTpeWidth() - 20;
			BSPanel p = new BSPanel(getTpeX() + 10, getLastTChild(false).getTpeEndY() + 3, w, w);
			var er = new TEntityRendererElement(10, 10, w - 20, w - 20, EntityType.PLAYER);
			p.addTChild(er);
			addTChild(p, false);
		}*/
		
		//filters label
		var lbl_filters = new TLabelElement(nextX(), nextY(), nextW(), 20);
		lbl_filters.setText(translatable("betterstats.gui.filters"));
		lbl_filters.setHorizontalAlignment(HorizontalAlignment.CENTER);
		addTChild(lbl_filters, false);
		
		//tab selector
		btn_tab = new TSelectEnumWidget<>(nextX(), nextY(), nextW(), 20, CurrentTab.class);
		btn_tab.setSelected(bss.filter_currentTab, false);
		btn_tab.setOnSelectionChange(tab ->
		{
			bss.filter_currentTab = (CurrentTab) tab;
			bss.filter_statsScroll = 0;
			bss.getStatPanel().init_stats();
		});
		btn_tab.setEnumValueToLabel(val -> ((CurrentTab)val).asText());
		addTChild(btn_tab, false);
		
		//search bar
		var txt_search = new TTextFieldWidget(nextX(), nextY(), nextW(), 20);
		txt_search.setText(bss.filter_searchTerm, false);
		__handler1 = txt_search.getEvents().TEXT_CHANGED.addWeakEventHandler(txt ->
		{
			bss.filter_searchTerm = txt;
			bss.getStatPanel().init_stats();
		});
		addTChild(txt_search, false);
		
		//show empty stats
		var check_emptyStats = new TCheckboxWidget(nextX(), nextY(), nextW(), 20,
				translatable("betterstats.gui.show_empty_stats"), filter_showEmpty);
		__handler0 = check_emptyStats.getEvents().CLICKED.addWeakEventHandler(() ->
		{
			filter_showEmpty = check_emptyStats.getChecked();
			bss.getStatPanel().init_stats();
		});
		addTChild(check_emptyStats, false);
		
		//bottom
		{
			int bY = getTpeEndY() - getScrollPadding() - 20;
			//close
			var btn_close = new TButtonWidget(nextX(), bY, nextW(), 20,
					translatable("gui.done"),
					btn -> getClient().setScreen(bss.parent));
			addTChild(btn_close, false);
		}
		
		//remove the blank element as it is no longer needed
		removeTChild(blank);
	}
	// ==================================================
	private int nextX() { return getTpeX() + getScrollPadding(); }
	private int nextY() { return getLastTChild(false).getTpeEndY() + 5; }
	private int nextW() { return getTpeWidth() - (getScrollPadding() * 2); }
	// ==================================================
}