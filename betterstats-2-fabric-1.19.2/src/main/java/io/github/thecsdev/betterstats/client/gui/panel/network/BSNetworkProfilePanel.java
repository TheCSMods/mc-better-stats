package io.github.thecsdev.betterstats.client.gui.panel.network;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.awt.Color;
import java.util.Objects;

import io.github.thecsdev.betterstats.client.gui.panel.BSPanel;
import io.github.thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TOcpeRendererElement;

public class BSNetworkProfilePanel extends BSPanel
{
	// ==================================================
	protected TOcpeRendererElement playerRenderer;
	// ==================================================
	public BSNetworkProfilePanel(int x, int y, int width)
	{
		super(x, y, width, 64);
		setScrollPadding(7);
		setOutlineColor(0);
	}
	// ==================================================
	public final void init(final BetterStatsScreen bss) { clearTChildren(); onInit(bss); }
	protected void onInit(final BetterStatsScreen bss)
	{
		// -- vertical alignment
		//scroll padding - 7px
		//name           - 20px
		//gap            - 10px
		//uuid           - 20px
		//badge_list     - 40px
		//scroll padding - 7px
		
		//calculations
		final int pad = getScrollPadding(); //scroll padding
		final int _13 = (int)(getTpeWidth() * 0.2); //one third (1/3)
		final int wm13 = getTpeWidth() - _13; //width - one third
		final int ylw = Color.YELLOW.getRGB(); //yellow color
		final String name = Objects.toString(bss.targetProfile.gameProfile.getName(), "-");
		final String uuid = Objects.toString(bss.targetProfile.gameProfile.getId(), "-");
		
		//player element
		this.playerRenderer = new TOcpeRendererElement(pad, pad, (int)(_13 * 0.8), getTpeHeight());
		this.playerRenderer.setProfileGP(bss.targetProfile.gameProfile);
		this.playerRenderer.setScale(1.1);
		addTChild(this.playerRenderer, true);
		
		//player info labels
		final var lbl_nameKey = new TLabelElement(_13, pad, wm13, 10, translatable("entity.minecraft.player"));
		lbl_nameKey.setColor(ylw, ylw);
		addTChild(lbl_nameKey, true);
		final var lbl_nameVal = new TLabelElement(_13, pad + 10, wm13, 10, literal(name));
		addTChild(lbl_nameVal, true);
		
		final var lbl_uuidKey = new TLabelElement(_13, pad + 30, wm13, 10, literal("UUID"));
		lbl_uuidKey.setColor(ylw, ylw);
		addTChild(lbl_uuidKey, true);
		final var lbl_uuidVal = new TLabelElement(_13, pad + 40, wm13, 10, literal(uuid));
		addTChild(lbl_uuidVal, true);
	}
	// ==================================================
}