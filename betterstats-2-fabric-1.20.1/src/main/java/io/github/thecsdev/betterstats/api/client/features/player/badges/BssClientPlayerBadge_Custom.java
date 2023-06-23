package io.github.thecsdev.betterstats.api.client.features.player.badges;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import net.minecraft.text.Text;

public final class BssClientPlayerBadge_Custom extends BssClientPlayerBadge
{
	// ==================================================
	public static final Text TXT_DASH = literal("-");
	// --------------------------------------------------
	protected Text txtName = TXT_DASH;
	protected Text txtDescription = TXT_DASH;
	// ==================================================
	public BssClientPlayerBadge_Custom() { super(); }
	public BssClientPlayerBadge_Custom(String translationId)
	{
		super();
		setName(translatable("betterstats.gui.network.badges." + translationId + ".name"));
		setDescription(translatable("betterstats.gui.network.badges." + translationId + ".description"));
	}
	// ==================================================
	public @Override BssClientPlayerBadge_Custom setUVCoords(int x, int y, int w, int h)
	{
		super.setUVCoords(x, y, w, h);
		return this;
	}
	// ==================================================
	public BssClientPlayerBadge_Custom setName(Text newName)
	{
		if(newName == null) this.txtName = TXT_DASH;
		else this.txtName = newName;
		return this;
	}
	public BssClientPlayerBadge_Custom setDescription(Text newDescription)
	{
		if(newDescription == null) this.txtDescription = TXT_DASH;
		else this.txtDescription = newDescription;
		return this;
	}
	// --------------------------------------------------
	public @Override Text getName() { return this.txtName; }
	public @Override Text getDescription() { return this.txtDescription; }
	// ==================================================
}