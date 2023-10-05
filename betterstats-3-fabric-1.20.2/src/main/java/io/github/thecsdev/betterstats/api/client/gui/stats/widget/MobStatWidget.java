package io.github.thecsdev.betterstats.api.client.gui.stats.widget;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.fLiteral;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.BetterStatsConfig;
import io.github.thecsdev.betterstats.api.registry.BSRegistries;
import io.github.thecsdev.betterstats.api.util.enumerations.MobStatType;
import io.github.thecsdev.betterstats.api.util.stats.SUMobStat;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TEntityRendererElement;
import io.github.thecsdev.tcdcommons.api.client.gui.util.GuiUtils;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TInputContext;
import io.github.thecsdev.tcdcommons.api.client.gui.util.TInputContext.InputType;
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
	public static final Text TEXT_STAT_KILLS  = MobStatType.KILLED.getText();
	public static final Text TEXT_STAT_DEATHS = MobStatType.KILLED_BY.getText();
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
	public @Virtual @Override boolean input(TInputContext inputContext)
	{
		//only handle mouse presses
		if(inputContext.getInputType() != InputType.MOUSE_PRESS)
			return false;
		
		//handle the mouse press
		final int btn = inputContext.getMouseButton();
		
		//handle input
		if(btn == 2)
		{
			final @Nullable var url = BSRegistries.getMobWikiURL(this.stat.getStatID());
			if(url != null)
			{
				GuiUtils.showUrlPrompt(url, false);
				return false; //if successful, block the focus by returning false
			}
		}
		
		//return super
		return super.input(inputContext);
	}
	// ==================================================
}