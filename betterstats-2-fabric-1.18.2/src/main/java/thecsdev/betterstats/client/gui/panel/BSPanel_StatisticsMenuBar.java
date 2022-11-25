package thecsdev.betterstats.client.gui.panel;

import static thecsdev.betterstats.api.client.registry.BetterStatsClientRegistry.MOD_MENU_ITEMS;
import static thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.awt.Color;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.util.math.MatrixStack;
import thecsdev.betterstats.BetterStats;
import thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import thecsdev.betterstats.client.gui.screen.BetterStatsScreen.CurrentTab;
import thecsdev.tcdcommons.api.client.gui.panel.TMenuBarPanel;
import thecsdev.tcdcommons.api.client.gui.util.GuiUtils;

public class BSPanel_StatisticsMenuBar extends TMenuBarPanel
{
	// ==================================================
	public static final int COLOR_OUTLINE = Color.black.getRGB();
	// ==================================================
	public BSPanel_StatisticsMenuBar(int x, int y, int width, int height) { super(x, y, width, height); }
	// --------------------------------------------------
	public void init(BetterStatsScreen bss)
	{
		var panel_leftMenu = bss.getStatPanel().panel_leftMenu;
		
		//view
		{
			var menu_view = addItem(translatable("betterstats.gui.menu_bar.view"));
			menu_view.addDropdownOption(translatable("betterstats.gui.menu_bar.view.vanilla_stats"), () ->
			{
				getClient().setScreen(new StatsScreen(bss.parent, bss.getStatHandler()));
			});
			menu_view.addDropdownSeparator();
			for(final CurrentTab crrTab : CurrentTab.values())
			{
				menu_view.addDropdownOption(crrTab.asText(), () ->
				{
					if(panel_leftMenu != null && panel_leftMenu.btn_tab != null)
						panel_leftMenu.btn_tab.setSelected(crrTab, true);
				});
			}
		}
		
		//mods
		if(MOD_MENU_ITEMS.size() > 0)
		{
			final var menu_mods = addItem(translatable("mods"));
			MOD_MENU_ITEMS.asMap().values().forEach((modEntry) ->
			{
				modEntry.forEach((modEntryValue) ->
				{
					menu_mods.addDropdownOption(modEntryValue.getKey(), modEntryValue.getValue());
				});
			});
		}
		
		//about
		final var mod_info = BetterStats.getInstance().getModInfo().getMetadata();
		final var mod_links = mod_info.getCustomValue("modmenu").getAsObject().get("links").getAsObject();
		{
			//define about menu item
			var menu_about = addItem(translatable("betterstats.gui.menu_bar.about"));
			
			//source code and bug reports
			menu_about.addDropdownOption(translatable("betterstats.gui.menu_bar.about.source"), () ->
			{
				String url = mod_info.getContact().get("sources").orElse(null);
				if(url != null) GuiUtils.showUrlPrompt(url, true);
			});
			menu_about.addDropdownOption(translatable("menu.reportBugs"), () ->
			{
				String url = mod_info.getContact().get("issues").orElse(null);
				if(url != null) GuiUtils.showUrlPrompt(url, true);
			});
			
			//links for modmenu
			if(FabricLoader.getInstance().isModLoaded("modmenu"))
			{
				//add a separator
				menu_about.addDropdownSeparator();
				//iterate all links for modmenu
				for(var mod_link : mod_links)
				{
					//obtain key/value pairs
					String key = mod_link.getKey();
					String value = mod_link.getValue().getAsString();
					//add the menu items
					menu_about.addDropdownOption(translatable(key), () -> GuiUtils.showUrlPrompt(value, true));
				}
			}
		}
	}
	// ==================================================
	public @Override void postRender(MatrixStack matrices, int mouseX, int mouseY, float deltaTime)
	{
		drawOutline(matrices, GuiUtils.applyAlpha(isFocused() ? COLOR_OUTLINE_FOCUSED : BSPanel.COLOR_OUTLINE, getAlpha()));
	}
	// ==================================================
}