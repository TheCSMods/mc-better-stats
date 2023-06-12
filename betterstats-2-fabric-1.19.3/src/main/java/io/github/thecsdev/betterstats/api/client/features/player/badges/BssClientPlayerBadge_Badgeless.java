package io.github.thecsdev.betterstats.api.client.features.player.badges;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

import io.github.thecsdev.betterstats.BetterStats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * A {@link BssClientPlayerBadge} for players that don't have a badge.
 */
public final class BssClientPlayerBadge_Badgeless extends BssClientPlayerBadge
{
	// ==================================================
	public static final Identifier BADGE_ID = new Identifier(BetterStats.getModID(), "badgeless");
	public static final BssClientPlayerBadge_Badgeless instance = new BssClientPlayerBadge_Badgeless();
	// --------------------------------------------------
	protected final Text txtName;
	protected Text txtDescription;
	// ==================================================
	protected BssClientPlayerBadge_Badgeless()
	{
		super();
		this.txtName = translatable("betterstats.gui.network.badges.badgeless.name");
		tick();
	}
	// ==================================================
	public @Override Text getName() { return this.txtName; }
	public @Override Text getDescription() { return this.txtDescription; }
	// --------------------------------------------------
	public void tick()
	{
		//handle easter eggs
		boolean easterEggMode = false;
		
		//obtain date info
		final var localDate = LocalDate.now();
		final int month = localDate.getMonthValue();
		final int mDay = localDate.getDayOfMonth();
		
		if(month == 4 && mDay == 1)
		{
			easterEggMode = true;
			this.uv_coords.x = 180;
		}
		else if(month == 6 && ThreadLocalRandom.current().nextBoolean()) //50% chance in June
		{
			easterEggMode = true;
			this.uv_coords.x = 200;
		}
		else this.uv_coords.x = 220;
		
		//handle description
		if(easterEggMode)
			this.txtDescription = translatable("betterstats.gui.network.badges.badgeless.description")
				.append("\n\n").append(translatable("betterstats.gui.network.badges.ee_icon_note"));
		else this.txtDescription = translatable("betterstats.gui.network.badges.badgeless.description");
	}
	// ==================================================
}