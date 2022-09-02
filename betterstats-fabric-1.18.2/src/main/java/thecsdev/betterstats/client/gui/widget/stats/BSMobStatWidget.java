package thecsdev.betterstats.client.gui.widget.stats;

import static thecsdev.betterstats.BetterStats.lt;
import static thecsdev.betterstats.BetterStats.tt;

import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.text.Text;
import thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import thecsdev.betterstats.client.gui.util.GuiUtils;
import thecsdev.betterstats.client.gui.util.StatUtils.SUMobStat;
import thecsdev.betterstats.config.BSConfig;

public class BSMobStatWidget extends BSStatWidget
{
	// ==================================================
	public static final int defaultMobGuiSize = 38; //the value is 50 for 80x80px
	// --------------------------------------------------
	public final SUMobStat mobStat;
	public final String[] mobNameSplit;
	
	public final LivingEntity livingEntity;
	public final int mobGuiSize;
	// ==================================================
	public BSMobStatWidget(BetterStatsScreen parent, SUMobStat mobStat, int x, int y)
	{
		super(parent, x, y, 60, 60, BSConfig.COLOR_STAT_BG);
		this.mobStat = mobStat;
		this.mobNameSplit = mobStat.entityName.split("([\\r?\\n])|([ ]{1,})");
		
		livingEntity = (mobStat.entity instanceof LivingEntity) ? (LivingEntity) mobStat.entity : null;
		mobGuiSize = getLivingEntityGUISize(livingEntity);
		updateTooltip();
	}
	// --------------------------------------------------
	@Override
	protected void onUpdateTooltip() { tooltip = onUpdateTooltip(mobStat); }
	
	public static Text onUpdateTooltip(SUMobStat mobStat)
	{
		String s0 = tt("stat_type.minecraft.killed.none", mobStat.entityName).getString();
		String s1 = tt("stat_type.minecraft.killed_by.none", mobStat.entityName).getString();
		
		if(mobStat.killed != 0)
			s0 = tt("stat_type.minecraft.killed", Integer.toString(mobStat.killed), mobStat.entityName).getString();
		if(mobStat.killedBy != 0)
			s1 = tt("stat_type.minecraft.killed_by", mobStat.entityName, Integer.toString(mobStat.killedBy)).getString();
		
		return lt(s0 + "\n" + s1);
	}
	// ==================================================
	@Override
	public void onRenderStat(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		//scissor y and height
		int bottom = y + height;
		int scBottom = parent.statContentPane.y + parent.statContentPane.getHeight();
		
		int scissorY = this.y;
		int scissorHeight = this.height;
		if(this.y < parent.statContentPane.y)
		{
			int i0 = Math.abs(this.y - parent.statContentPane.y);
			scissorY += i0;
			scissorHeight -= i0;
		}
		if(bottom > scBottom)
			scissorHeight -= Math.abs(bottom - scBottom);
		
		//render living_entity/entity_name
		if(livingEntity != null)
			GuiUtils.applyScissor(x, scissorY, width, scissorHeight, () ->
			{
				int centerX = this.x + (this.width / 2);
				int centerY = this.y + (this.height / 2);
				
				InventoryScreen.drawEntity(
						centerX,
						this.y + this.height - 7,
						mobGuiSize,
						-rInt(mouseX, centerX), -rInt(mouseY, centerY),
						livingEntity);
			});
		else
			GuiUtils.applyScissor(x, scissorY, width, scissorHeight, () ->
			GuiUtils.drawCenteredTextLines(
					matrices,
					parent.getTextRenderer(),
					this.x + (this.width / 2),
					this.y + (this.height / 2),
					mobNameSplit,
					BSConfig.COLOR_STAT_GENERAL_TEXT));
	}
	
	private static int rInt(int input, int relativeTo) { return input - relativeTo; }
	
	private static int getLivingEntityGUISize(LivingEntity e)
	{
		//if null, return default
		if(e == null) return defaultMobGuiSize;
		
		//return size based on entity model size
		float f1 = e.getType().getDimensions().width, f2 = e.getType().getDimensions().height;
		double d0 = Math.sqrt((f1 * f1) + (f2 * f2));
		if(d0 == 0) d0 = 0.1;
		
		//some mobs are too wide that even the
		//formula above doesn't work properly on them...
		//*looks at ender dragon*
		if(e instanceof EnderDragonEntity)
			d0 /= 2;
		
		//calculate and return
		return (int) (defaultMobGuiSize / d0);
	}
	// ==================================================
}