package io.github.thecsdev.betterstats.client.gui_hud.screen;

import static io.github.thecsdev.betterstats.BetterStats.getModID;
import static io.github.thecsdev.betterstats.client.gui_hud.widget.BSHudStatWidget.SIZE;
import static io.github.thecsdev.betterstats.client.network.BetterStatsClientNetworkHandler.enableBSSProtocol;
import static io.github.thecsdev.betterstats.client.network.BetterStatsClientNetworkHandler.serverHasBSS;
import static io.github.thecsdev.tcdcommons.api.client.registry.TCDCommonsClientRegistry.InGameHud_Screens;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.HashSet;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Sets;

import io.github.thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.betterstats.client.gui_hud.widget.BSHudStatWidget;
import io.github.thecsdev.betterstats.client.network.BetterStatsClientNetworkHandler;
import io.github.thecsdev.tcdcommons.api.client.gui.TElement;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TTextureElement;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreen;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TButtonWidget;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class BetterStatsHudScreen extends TScreen
{
	// ==================================================
	public static final Identifier HUD_ID = new Identifier(getModID(), "hud");
	// --------------------------------------------------
	protected Screen parent;
	protected TButtonWidget btn_done, btn_accurate;
	protected TTextureElement img_accurate;
	protected final BooleanConsumer confsc_callback = (accepted ->
	{
		enableBSSProtocol = accepted;
		getClient().setScreen(this);
		updateImgAccurate();
	});
	protected final Text confsc_title = translatable("betterstats.hud.accuracy_mode_warning.title", getModID());
	protected final Text confsc_msg = translatable("betterstats.hud.accuracy_mode_warning.message", getModID());
	// --------------------------------------------------
	/**
	 * Set this flag to a value greater than 0 to schedule
	 * a {@link #tickChildren()} call in X amount of ticks.
	 */
	public int flag_tickChildren = -1;
	public final HashSet<BSHudStatWidget> stat_widgets = Sets.newHashSet();
	// ==================================================
	/**
	 * Returns the currently existing instance of {@link BetterStatsHudScreen},
	 * or null if one does not exist yet.
	 * @see {@link #getOrCreateInstance(Screen)}
	 */
	public static @Nullable BetterStatsHudScreen getInstance()
	{
		//get
		var bshs = InGameHud_Screens.get(HUD_ID);
		//return
		if(bshs instanceof BetterStatsHudScreen)
			return (BetterStatsHudScreen)bshs;
		else return null;
	}
	
	/**
	 * Gets the main instance of or creates a new instance
	 * of {@link BetterStatsHudScreen}, and then returns it.
	 * @param parent The parent {@link Screen} to assign.
	 * @throws RuntimeException When the {@link MinecraftClient} is not initialized yet.
	 */
	public static BetterStatsHudScreen getOrCreateInstance(Screen parent) throws RuntimeException
	{
		//get
		var bshs = getInstance();
		//create if null
		if(bshs == null)
		{
			bshs = new BetterStatsHudScreen(null);
			InGameHud_Screens.put(HUD_ID, bshs);
		}
		//assign parent
		bshs.parent = parent;
		//return
		return bshs;
	}
	// ==================================================
	public BetterStatsHudScreen(Screen parent)
	{
		//initialize
		super(translatable("betterstats.hud"));
		this.parent = parent;
	}
	// --------------------------------------------------
	protected @Override void onClosed()
	{
		//set screen to parent
		getClient().setScreen(this.parent);
		//tick this screen and its elements
		this.flag_tickChildren = 0;
		this.tick();
		//dispose of the parent, as it is no longer needed
		this.parent = null;
		//dispose of this screen if there are no widgets on it
		if(findTChildOfType(BSHudStatWidget.class, false) == null)
			InGameHud_Screens.remove(HUD_ID, this);
		//update the server on the prefs
		BetterStatsClientNetworkHandler.c2s_sendPrefs();
	}
	// ==================================================
	public <T extends BSHudStatWidget> T addHudStatWidget(T widget)
	{
		//null and already exists check
		if(widget == null || this.stat_widgets.contains(widget))
			return widget;
		//add the widget
		widget.setPosition((getTpeWidth() / 2) - (widget.getTpeWidth() / 2), (getTpeHeight() / 2) - (SIZE * 2), false);
		addTChild(widget, false);
		//return the widget
		return widget;
	}
	// --------------------------------------------------
	@Override
	public <T extends TElement> boolean addTChild(T child, boolean reposition)
	{
		if(super.addTChild(child, reposition))
		{
			if((child instanceof BSHudStatWidget) && !stat_widgets.contains(child))
			{
				var bshsw = (BSHudStatWidget) child;
				//keep track of the added hud stat widget
				stat_widgets.add(bshsw);
				//after adding for the 1st time, must be re-calculated
				bshsw.reCalculateAnchor();
			}
			return true;
		}
		return false;
	}
	// --------------------------------------------------
	@Override
	public <T extends TElement> boolean removeTChild(T child, boolean reposition)
	{
		if(super.removeTChild(child, reposition))
		{
			if((child instanceof BSHudStatWidget) /*&& stat_widgets.contains(child)*/)
				stat_widgets.remove((BSHudStatWidget) child);
			return true;
		}
		return false;
	}
	// ==================================================
	protected void updateImgAccurate()
	{
		if(img_accurate != null)
			if(enableBSSProtocol) img_accurate.setTextureUVs(20, 20, 20, 20);
			else img_accurate.setTextureUVs(20, 0, 20, 20);
	}
	protected @Override void init()
	{
		//create the done button
		btn_done = new TButtonWidget(
				getTpeWidth() / 2 - 50, getTpeHeight() / 2 - 10,
				100, 20,
				translatable("gui.done"), btn -> close());
		btn_done.setDrawsVanillaButton(true);
		addTChild(btn_done);
		
		//initialize done button tooltip
		var txt_hint_a = translatable("betterstats.hud.hint.add_widget");
		var txt_hint_b = translatable("betterstats.hud.hint.del_widget");
		var txt_hint_c = translatable("betterstats.hud.hint.esc_close");
		var txt_hint = literal(
				"1. " + txt_hint_a.getString() + "\n" +
				"2. " + txt_hint_b.getString() + "\n" +
				"3. " + txt_hint_c.getString());
		btn_done.setTooltip(txt_hint);
		
		//create the accuracy button
		btn_accurate = new TButtonWidget(btn_done.getTpeEndX() + 5, btn_done.getTpeY(), 20, 20, null, btn ->
		{
			//disabling
			if(enableBSSProtocol) enableBSSProtocol = false;
			//enabling ('else if' is important)
			else if(getClient().isInSingleplayer()) enableBSSProtocol = true;
			else
			{
				var confsc = new ConfirmScreen(confsc_callback, confsc_title, confsc_msg);
				getClient().setScreen(confsc);
				return;
			}
			//indicator
			updateImgAccurate();
		});
		btn_accurate.setDrawsVanillaButton(true);
		btn_accurate.setTooltip(translatable("betterstats.hud.accuracy_mode_warning.tooltip"));
		addTChild(btn_accurate);

		//accuracy button sprite
		img_accurate = new TTextureElement(2, 2, 16, 16);
		img_accurate.setZOffset(btn_accurate.getZOffset() + 1);
		img_accurate.setTexture(BetterStatsScreen.BSS_WIDGETS_TEXTURE, 256, 256);
		img_accurate.setTextureUVs(20, 0, 20, 20);
		btn_accurate.addTChild(img_accurate, true);
		updateImgAccurate();
		
		//re-add stat widget entries
		this.stat_widgets.remove(null);
		for(var entry : this.stat_widgets)
		{
			//re-add the widget
			addTChild(entry, false);
			//and then re-position it
			entry.rePositionToAnchor();
		}
		
		//tick this screen and its elements
		this.flag_tickChildren = 0;
		this.tick();
	}
	// --------------------------------------------------
	public @Override void tick()
	{
		//tick super
		super.tick();
		
		//tick all elements when prompted too
		if(flag_tickChildren > 0) flag_tickChildren--;
		else if(flag_tickChildren == 0)
		{
			tickChildren();
			flag_tickChildren = -1;
		}
		
		//----- handle other stuff
		//tick the BetterStatsHudScreen auto requester
		BshsAutoRequest.tick();
	}
	
	@SuppressWarnings({ "deprecation", "resource" })
	public final void tickChildren()
	{
		//done button visibility
		btn_done.setVisible(getClient().currentScreen == this);
		btn_accurate.setVisible(btn_done.getVisible() && (!getClient().isInSingleplayer() && serverHasBSS));
		//children tick
		forEachChild(child -> { child.tick(); return false; }, true);
	}
	// ==================================================
	@SuppressWarnings("resource")
	public @Override void renderBackground(DrawContext pencil)
	{
		//fill if current screen is this screen
		if(getClient().currentScreen == this)
			pencil.fill(0, 0, this.width, this.height, 1342177280);
	}
	
	@SuppressWarnings("resource")
	public @Override void render(DrawContext pencil, int mouseX, int mouseY, float deltaTime)
	{
		//(do not render when another window is open)
		//only render when there's no screen set, or if the set screen is the current screen
		if(getClient().currentScreen != null && getClient().currentScreen != this)
			return;
		
		//render otherwise
		super.render(pencil, mouseX, mouseY, deltaTime);
	}
	// ==================================================
	@SuppressWarnings("resource")
	public @Override boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		if(getClient().currentScreen != this) return false;
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if(super.keyPressed(keyCode, scanCode, modifiers))
			return true;
		else if(keyCode == 259 || keyCode == 261)
		{
			var hovered = getHoveredTChild();
			if(hovered instanceof BSHudStatWidget)
			{
				removeTChild(hovered);
				return true;
			}
		}
		return false;
	}
	// ==================================================
}