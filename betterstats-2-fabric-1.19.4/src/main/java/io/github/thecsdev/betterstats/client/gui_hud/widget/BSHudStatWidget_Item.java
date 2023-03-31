package io.github.thecsdev.betterstats.client.gui_hud.widget;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Objects;

import io.github.thecsdev.betterstats.util.ItemStatEnum;
import io.github.thecsdev.betterstats.util.StatUtils;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TContextMenuPanel;
import net.minecraft.item.Item;
import net.minecraft.stat.StatHandler;
import net.minecraft.text.Text;

public class BSHudStatWidget_Item extends BSHudStatWidget
{
	// ==================================================
	public final Item item;
	// --------------------------------------------------
	protected LabelEntry lblStatEntry;
	protected ItemStatEnum shownStat;
	// ==================================================
	public BSHudStatWidget_Item(int x, int y, StatHandler statHandler, Item item)
	{
		super(x, y, statHandler);
		this.item = Objects.requireNonNull(item, "item must not be null.");
		this.shownStat = ItemStatEnum.MINED;
	}
	// ==================================================
	public @Override void tick() { this.lblStatEntry.setText(createText()); }
	public @Override void onInit()
	{
		addItemEntry(this.item);
		this.lblStatEntry = new LabelEntry(null);
	}
	// ==================================================
	public Text createText()
	{
		//construct the string text
		var stat = new StatUtils.StatUtilsItemStat(statHandler, item);
		String txt = "";
		switch(this.shownStat)
		{
			case MINED: txt = stat.sMined + " " + translatable("stat_type.minecraft.mined").getString(); break;
			case CRAFTED: txt = stat.sCrafted + " " + translatable("stat_type.minecraft.crafted").getString(); break;
			case USED: txt = stat.sUsed + " " + translatable("stat_type.minecraft.used").getString(); break;
			case BROKEN: txt = stat.sBroken + " " + translatable("stat_type.minecraft.broken").getString(); break;
			case PICKED_UP: txt = stat.sPickedUp + " " + translatable("stat_type.minecraft.picked_up").getString(); break;
			case DROPPED: txt = stat.sDropped + " " + translatable("stat_type.minecraft.dropped").getString(); break;
			default: txt = "null";
		}
		
		//return the text
		return literal(txt);
	}
	// --------------------------------------------------
	@Override
	protected void onContextMenu(TContextMenuPanel contextMenu)
	{
		//iterate all item stats
		for(ItemStatEnum itemStat : ItemStatEnum.values())
			//add each item stat to the list
			contextMenu.addButton(itemStat.getIText(), btn ->
			{
				this.shownStat = itemStat;
				tick();
			});
		//add super
		super.onContextMenu(contextMenu);
	}
	// ==================================================
}
