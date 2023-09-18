package io.github.thecsdev.betterstats.api.client.gui.stats.widget;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.fLiteral;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.BetterStatsConfig;
import io.github.thecsdev.betterstats.api.util.stats.SUMobStat;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TEntityRendererElement;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;

public @Virtual class MobStatWidget extends AbstractStatWidget<SUMobStat>
{
	// ==================================================
	private static final BetterStatsConfig BSS_CONFIG = BetterStats.getInstance().getConfig(); //optimization
	// --------------------------------------------------
	public static final int SIZE = 55;
	//
	public static final Text TXT_STAT_KILLS  = translatable("betterstats.api.client.gui.stats.widget.mobstatwidget.kills");
	public static final Text TXT_STAT_DEATHS = translatable("betterstats.api.client.gui.stats.widget.mobstatwidget.deaths");
	// --------------------------------------------------
	protected final EntityType<?> entityType;
	protected final TEntityRendererElement entityRenderer;
	//
	protected final Tooltip defaultTooltip;
	// ==================================================
	public MobStatWidget(int x, int y, SUMobStat stat) throws NullPointerException { this(x, y, SIZE, stat); }
	public MobStatWidget(int x, int y, int size, SUMobStat stat) throws NullPointerException
	{
		super(x, y, size, size, stat);
		this.entityType = stat.getEntityType();
		
		final String entityNameStr = stat.getStatLabel().getString();
		final Text ttt = literal("") //MUST create new text instance
				.append(stat.getStatLabel())
				.append(fLiteral("\n§7" + stat.getStatID()))
				.append("\n\n§r")
				.append(stat.kills == 0 ?
						translatable("stat_type.minecraft.killed.none", entityNameStr) :
						translatable("stat_type.minecraft.killed", Integer.toString(stat.kills), entityNameStr))
				.append("\n")
				.append(stat.deaths == 0 ?
						translatable("stat_type.minecraft.killed_by.none", entityNameStr) :
						translatable("stat_type.minecraft.killed_by", entityNameStr, Integer.toString(stat.deaths)));
		setTooltip(this.defaultTooltip = Tooltip.of(ttt));
		
		this.entityRenderer = new TEntityRendererElement(x, y, size, size, this.entityType);
		this.entityRenderer.setFollowsCursor(BSS_CONFIG.guiMobsFollowCursor);
		addChild(this.entityRenderer, false);
	}
	// --------------------------------------------------
	public @Virtual @Override void setSize(int width, int height, int flags)
	{
		super.setSize(width, height, flags);
		this.entityRenderer.setSize(width, height, flags);
	}
	// ==================================================
}