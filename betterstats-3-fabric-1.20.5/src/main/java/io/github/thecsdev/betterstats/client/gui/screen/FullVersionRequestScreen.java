package io.github.thecsdev.betterstats.client.gui.screen;

import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.BetterStatsConfig;
import io.github.thecsdev.betterstats.BetterStatsProperties;
import io.github.thecsdev.betterstats.api.client.gui.widget.ScrollBarWidget;
import io.github.thecsdev.betterstats.client.gui.widget.CreditsTabPersonWidget;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.api.client.gui.layout.UILayout;
import io.github.thecsdev.tcdcommons.api.client.gui.layout.UIListLayout;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TBlankElement;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreenPlus;
import io.github.thecsdev.tcdcommons.api.client.gui.util.GuiUtils;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TDrawContext;
import io.github.thecsdev.tcdcommons.api.client.gui.util.UITexture;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TButtonWidget;
import io.github.thecsdev.tcdcommons.api.client.util.interfaces.IParentScreenProvider;
import io.github.thecsdev.tcdcommons.api.util.enumerations.Axis2D;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;
import io.github.thecsdev.tcdcommons.api.util.enumerations.VerticalAlignment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/**
 * A {@link TScreenPlus} whose purpose is to ask the user to download and
 * install the "full version" of this mod.
 * @apiNote Full version can be "force-enabled" without any additional downloads
 * using the {@link BetterStatsConfig#forceFullVersion} property.
 */
public final class FullVersionRequestScreen extends TScreenPlus implements IParentScreenProvider
{
	// ==================================================
	private static final UITexture TEX_PANORAMA = new UITexture(
			new Identifier("textures/gui/title/background/panorama_0.png"));
	// ==================================================
	private @Nullable Screen parent;
	private @Nullable TPanelElement panel;
	// ==================================================
	public FullVersionRequestScreen(@Nullable Screen parent)
	{
		super(BST.gui_fvrs());
		this.parent = parent;
	}
	// ---------------------------------------------------
	public final @Override Screen getParentScreen() { return this.parent; }
	public final @Override boolean shouldRenderInGameHud() { return false; }
	public final @Override void close() { MC_CLIENT.setScreen(this.parent); }
	// ==================================================
	protected final @Override void init() throws IllegalStateException
	{
		//check the state
		if(BetterStats.getInstance().getConfig().isFullVersion())
			throw new IllegalStateException("Something has attempted to open the '" +
					FullVersionRequestScreen.class.getSimpleName() + "' even though " +
					"the full version of this mod is already installed.");
		
		//the "root" element
		final var root = new TBlankElement(0, 0, (int)(getWidth() * 0.7f), (int)(getHeight() * 0.9f));
		addChild(root, false);
		
		//title-bar panel
		final var panel_title = new TPanelElement(0, 0, root.getWidth(), 30);
		panel_title.setScrollPadding(0);
		panel_title.setBackgroundColor(0x99000000);
		panel_title.setOutlineColor(0xff000000);
		root.addChild(panel_title);
		
		final var lbl_title = new TLabelElement(0, 0, panel_title.getWidth(), panel_title.getHeight());
		lbl_title.setTextHorizontalAlignment(HorizontalAlignment.CENTER);
		lbl_title.setText(BST.bss());
		lbl_title.setTextScale(1.2f);
		panel_title.addChild(lbl_title);
		
		//the contents panel
		final var panel_content = new TBlankElement(
				0, panel_title.getHeight() + 5,
				root.getWidth(), root.getHeight() - (panel_title.getHeight() + 5))
		{
			public @Override void render(TDrawContext pencil)
			{
				final int max = Math.max(getWidth(), getHeight());
				pencil.enableScissor(getX(), getY(), getEndX(), getEndY());
				pencil.pushTShaderColor(0.5f, 0.5f, 0.5f, 1);
				TEX_PANORAMA.drawTexture(pencil, getX(), getY(), max, max);
				pencil.popTShaderColor();
				pencil.disableScissor();
				pencil.drawTBorder(0xff000000);
			}
		};
		root.addChild(panel_content);
		
		this.panel = new TPanelElement(10, 10, panel_content.getWidth() - (20 + 8), panel_content.getHeight() - 20);
		this.panel.setScrollFlags(TPanelElement.SCROLL_VERTICAL);
		this.panel.setScrollPadding(10);
		this.panel.setBackgroundColor(0xbb353535);
		this.panel.setOutlineColor(0xff000000);
		panel_content.addChild(this.panel);
		
		final var scroll_panel = new ScrollBarWidget(
				this.panel.getEndX(), this.panel.getY(),
				8, this.panel.getHeight(),
				this.panel);
		panel_content.addChild(scroll_panel, false);
		
		//apply a layout
		new UIListLayout(Axis2D.Y, VerticalAlignment.CENTER, HorizontalAlignment.CENTER).apply(this);
		
		//and finally, initialize content
		init_content();
	}
	
	private final void init_content()
	{
		//prepare
		final var tr = getTextRenderer();
		final var fh = tr.fontHeight;
		
		//define a consumer function that will take text, and add
		//it to the panel, in a multiline way
		final Consumer<Text> textAppender = txt ->
		{
			for(final var line : tr.wrapLines(txt, this.panel.getWidth() - (this.panel.getScrollPadding() * 2)))
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
		};
		final Runnable separatorAppender = () ->
		{
			final var n2 = UILayout.nextChildVerticalRect(this.panel);
			final var separator = new TBlankElement(n2.x, n2.y, n2.width, 15);
			this.panel.addChild(separator, false);
		};
		
		//the text
		textAppender.accept(BST.gui_fvrs_whatsThis().formatted(Formatting.YELLOW));
		textAppender.accept(BST.gui_fvrs_whatsThis_a().formatted(Formatting.GRAY));
		separatorAppender.run();
		textAppender.accept(BST.gui_fvrs_whyFullVersion().formatted(Formatting.YELLOW));
		textAppender.accept(BST.gui_fvrs_whyFullVersion_a().formatted(Formatting.GRAY));
		separatorAppender.run();
		textAppender.accept(BST.gui_fvrs_isItFree().formatted(Formatting.YELLOW));
		textAppender.accept(BST.gui_fvrs_isItFree_a().formatted(Formatting.GRAY));
		separatorAppender.run();
		textAppender.accept(BST.gui_fvrs_whereToGet().formatted(Formatting.YELLOW));
		textAppender.accept(BST.gui_fvrs_whereToGet_a().formatted(Formatting.GRAY));
		separatorAppender.run();
		{
			final var n3 = UILayout.nextChildVerticalRect(this.panel);
			final var btn = new TButtonWidget(n3.x + (n3.width / 4), n3.y, n3.width / 2, 20);
			btn.setText(CreditsTabPersonWidget.TEXT_OPEN_LINK);
			btn.setOnClick(__ -> GuiUtils.showUrlPrompt(BetterStatsProperties.URL_WEBSITE, true));
			this.panel.addChild(btn, false);
		}
		separatorAppender.run();
		textAppender.accept(BST.gui_fvrs_notes().formatted(Formatting.YELLOW));
		textAppender.accept(BST.gui_fvrs_notes_a().formatted(Formatting.GRAY));
	}
	// ==================================================
}