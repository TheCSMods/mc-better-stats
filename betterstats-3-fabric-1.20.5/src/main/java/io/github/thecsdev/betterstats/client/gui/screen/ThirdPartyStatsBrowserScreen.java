package io.github.thecsdev.betterstats.client.gui.screen;

import static io.github.thecsdev.betterstats.api.client.gui.panel.BSComponentPanel.BS_WIDGETS_TEXTURE;
import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.awt.Rectangle;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.api.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.betterstats.api.client.gui.widget.ScrollBarWidget;
import io.github.thecsdev.betterstats.client.network.BetterStatsClientPlayNetworkHandler;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.client.gui.layout.UILayout;
import io.github.thecsdev.tcdcommons.api.client.gui.layout.UIListLayout;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TBlankElement;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TDemoBackgroundElement;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreenPlus;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.client.gui.util.UITexture;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TButtonWidget;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TTextFieldWidget;
import io.github.thecsdev.tcdcommons.api.client.util.interfaces.IParentScreenProvider;
import io.github.thecsdev.tcdcommons.api.util.enumerations.Axis2D;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;
import io.github.thecsdev.tcdcommons.api.util.enumerations.VerticalAlignment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;

/**
 * A {@link TScreenPlus} that allows the user to obtain statistics about
 * "third-party" sources, such as statistics about other players for example.
 */
public final class ThirdPartyStatsBrowserScreen extends TScreenPlus implements IParentScreenProvider
{
	// ==================================================
	private @Nullable Screen parent, bssParent;
	// --------------------------------------------------
	private @Nullable TPanelElement panel;
	// --------------------------------------------------
	private @Nullable String requestedSameServerPlayerName = null;
	// ==================================================
	public ThirdPartyStatsBrowserScreen(@Nullable Screen parent, @Nullable Screen bssParent)
	{
		super(BST.gui_tpsbs());
		this.parent = parent;
		this.bssParent = bssParent;
	}
	// --------------------------------------------------
	public final @Nullable @Override Screen getParentScreen() { return this.parent; }
	public final @Override boolean shouldRenderInGameHud() { return false; }
	public final @Override void close() { MC_CLIENT.setScreen(this.parent); }
	// --------------------------------------------------
	public final void refresh() { if(!isOpen()) return; clearChildren(); init(); }
	// ==================================================
	protected final @Override void init()
	{
		//layouts
		final var lay_centerHorizList = new UIListLayout(Axis2D.X, VerticalAlignment.CENTER, HorizontalAlignment.CENTER);
		
		//create background "root"
		final var root = new TDemoBackgroundElement(0, 0, getWidth() / 2, (int)(getHeight() * 0.9f));
		addChild(root, false);
		
		//create the main panel onto which all the elements will be placed
		this.panel = new TPanelElement(10, 10, root.getWidth() - 20, root.getHeight() - 20);
		this.panel.setScrollPadding(0);
		this.panel.setOutlineColor(0);
		this.panel.setBackgroundColor(0);
		root.addChild(this.panel, true);
		
		//create a scroll-bar for the main panel
		final var scroll_panel = new ScrollBarWidget(0, 0, 8, root.getHeight(), this.panel, true);
		scroll_panel.setAlpha(0.5f);
		addChild(scroll_panel, false);
		
		//apply the layout to this screen
		lay_centerHorizList.apply(this);
		
		//init components that go onto the panel
		init_sameServerPlayerSearch();
	}
	
	private final void init_sameServerPlayerSearch()
	{
		//prepare
		final var cpnh = Optional.ofNullable(BetterStatsClientPlayNetworkHandler.getInstance());
		final var tr = getTextRenderer();
		final int fh = getTextRenderer().fontHeight;
		
		//the title label
		final var lbl_title = new TLabelElement(0, 0, panel.getWidth(), fh + 6);
		lbl_title.setText(BST.gui_tpsbs_ssps());
		this.panel.addChild(lbl_title);
		
		//the description labels
		final var txt_descr = getTextRenderer().wrapLines(BST.gui_tpsbs_ssps_description(), this.panel.getWidth());
		for(final var line : txt_descr)
		{
			final var n1 = UILayout.nextChildVerticalRect(this.panel);
			final var lbl = new TBlankElement(n1.x, n1.y + 2, n1.width, fh)
			{
				public final @Override void render(TDrawContext pencil) {
					pencil.drawText(tr, line, getX(), getY(), 0xccffffff, true);
				}
			};
			this.panel.addChild(lbl, false);
		}
		
		//the input and submit widgets
		{
			boolean a = cpnh.isPresent() && cpnh.get().comms();
			boolean b = a && this.requestedSameServerPlayerName == null;
			final var n1 = UILayout.nextChildVerticalRect(this.panel);
			
			final var in_name = new TTextFieldWidget(n1.x, n1.y + 10, n1.width - 25, 20);
			in_name.setPlaceholderText(translatable("gui.abuseReport.type.name"));
			in_name.setTooltip(Tooltip.of(BST.gui_tpsbs_ssps_requirements()));
			if(this.requestedSameServerPlayerName != null)
				in_name.setInput(this.requestedSameServerPlayerName);
			in_name.setEnabled(b);
			this.panel.addChild(in_name, false);
			
			final var btn_submit = new TButtonWidget(in_name.getEndX() + 5, in_name.getY(), 20, 20);
			btn_submit.setIcon(new UITexture(BS_WIDGETS_TEXTURE, new Rectangle(20, 60, 20, 20)));
			btn_submit.setTooltip(Tooltip.of(BST.gui_tpsbs_ssps_requirements()));
			btn_submit.setEnabled(b);
			btn_submit.setOnClick(__ ->
			{
				//do nothing if already requested (fallback)
				if(this.requestedSameServerPlayerName != null) return;
				//also do nothing if the string is blank
				else if(StringUtils.isBlank(in_name.getInput())) return;
				
				//open better stats screen
				cpnh.ifPresent(net ->
				{
					//prepare
					final String input = in_name.getInput().trim();
					
					//handle requests for self, just in case the player for some reason does that
					if(Objects.equals(input, net.getPlayer().getName().getString()))
					{
						MC_CLIENT.setScreen(new BetterStatsScreen(this.bssParent).getAsScreen());
						return;
					}
					
					//handle requests for other players
					final var stats = net.getSessionPlayerStats(input);
					final var bss = new BetterStatsScreen(this.bssParent, stats);
					MC_CLIENT.setScreen(bss.getAsScreen());
				});
			});
			this.panel.addChild(btn_submit, false);
		}
	}
	// ==================================================
}