package io.github.thecsdev.betterstats.client.gui.panel;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.fTranslatable;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.awt.Color;
import java.util.Objects;

import io.github.thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.util.HorizontalAlignment;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TButtonWidget;

public class BSPanel_Downloading extends BSPanel
{
	// ==================================================
	protected final BetterStatsScreen betterStats;
	public final TLabelElement txt_downloading;
	// ==================================================
	public BSPanel_Downloading(final BetterStatsScreen bss)
	{
		//init class
		super(bss.getTpeX(), bss.getTpeY(), bss.getTpeWidth(), bss.getTpeHeight());
		this.betterStats = Objects.requireNonNull(bss, "bss must not be null.");
		setZOffset(70);
		setVisible(false);
		
		//init elements
		txt_downloading = new TLabelElement(0, (getTpeHeight() / 2) - 10, getTpeWidth(), 20);
		txt_downloading.setHorizontalAlignment(HorizontalAlignment.CENTER);
		onSendRequest();
		addTChild(txt_downloading, false);
		
		var txt_targetPlayer = new TLabelElement(0, txt_downloading.getTpeEndY(), getTpeWidth(), 20);
		txt_targetPlayer.setColor(Color.LIGHT_GRAY.getRGB(), Color.LIGHT_GRAY.getRGB());
		txt_targetPlayer.setHorizontalAlignment(HorizontalAlignment.CENTER);
		var tpn = Objects.toString(bss.targetProfile.gameProfile.getId()) + " - "
				+ Objects.toString(bss.targetProfile.gameProfile.getName());
		txt_targetPlayer.setText(literal(tpn));
		addTChild(txt_targetPlayer, false);
		
		var btn_cancel = new TButtonWidget((getTpeWidth() / 2) - 50, getTpeHeight() - 30, 100, 20, null, null);
		btn_cancel.setDrawsVanillaButton(true);
		btn_cancel.setMessage(translatable("gui.cancel"));
		btn_cancel.setOnClick(btn -> getClient().setScreen(bss.parent));
		addTChild(btn_cancel, false);
	}
	// ==================================================
	public void onSendRequest()
	{
		txt_downloading.setColor(Color.YELLOW.getRGB(), Color.YELLOW.getRGB());
		txt_downloading.setText(fTranslatable("multiplayer.downloadingStats"));
	}
	public void onTimedOut()
	{
		txt_downloading.setColor(Color.RED.getRGB(), Color.RED.getRGB());
		txt_downloading.setText(fTranslatable("betterstats.gui.network.stats_request.timed_out"));
	}
	public void onPlayer404()
	{
		txt_downloading.setColor(Color.RED.getRGB(), Color.RED.getRGB());
		txt_downloading.setText(fTranslatable("betterstats.gui.network.stats_request.player_404"));
	}
	// ==================================================
}