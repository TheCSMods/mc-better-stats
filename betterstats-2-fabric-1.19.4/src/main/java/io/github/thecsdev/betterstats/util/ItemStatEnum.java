package io.github.thecsdev.betterstats.util;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import io.github.thecsdev.tcdcommons.api.util.ITextProvider;
import net.minecraft.text.Text;

public enum ItemStatEnum implements ITextProvider
{
	MINED("stat_type.minecraft.mined"),
	CRAFTED("stat_type.minecraft.crafted"),
	PICKED_UP("stat_type.minecraft.picked_up"),
	DROPPED("stat_type.minecraft.dropped"),
	USED("stat_type.minecraft.used"),
	BROKEN("stat_type.minecraft.broken");
	
	private final Text text;
	ItemStatEnum(String translationKey) { this.text = translatable(translationKey); }
	public @Override Text getIText() { return this.text; }
}