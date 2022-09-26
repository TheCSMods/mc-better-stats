package thecsdev.betterstats.client.gui.widget.stats;

import static thecsdev.betterstats.BetterStats.lt;
import static thecsdev.betterstats.BetterStats.tt;
import static thecsdev.betterstats.config.BSConfig.IGNORE_ENTITY_RENDER_ERRORS;

import java.awt.Point;

import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import thecsdev.betterstats.client.BetterStatsClient;
import thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import thecsdev.betterstats.client.gui.util.GuiUtils;
import thecsdev.betterstats.client.gui.util.StatUtils.SUMobStat;
import thecsdev.betterstats.config.BSConfig;
import thecsdev.betterstats.config.BSMobStatRenderConfig;
import thecsdev.betterstats.config.BSWikiLinkConfig;
import thecsdev.betterstats.config.BSWikiLinkConfig.WikiType;

public class BSMobStatWidget extends BSStatWidget
{
	// ==================================================
	//public static final int defaultMobGuiSize = 38; //the value is 50 for 80x80px - UNUSED
	// --------------------------------------------------
	public final SUMobStat mobStat;
	public final String[] mobNameSplit;
	
	public final LivingEntity livingEntity;
	// --------------------------------------------------
	public final int cache_mobSize;
	public final Point cache_mobOffset;
	// ==================================================
	public BSMobStatWidget(BetterStatsScreen parent, SUMobStat mobStat, int x, int y) { this(parent, mobStat, x, y, 50); }
	public BSMobStatWidget(BetterStatsScreen parent, SUMobStat mobStat, int x, int y, int width)
	{
		//define mob info and stuff
		super(parent, x, y, width, width, BSConfig.COLOR_STAT_BG);
		this.mobStat = mobStat;
		this.mobNameSplit = mobStat.entityName.split("([\\r?\\n])|([ ]{1,})");
		livingEntity = (mobStat.entity instanceof LivingEntity) ? (LivingEntity) mobStat.entity : null;
		updateTooltip();
		
		if(!(livingEntity instanceof PlayerEntity))
			livingEntity.baseTick();
		if(mobStat.errored)
			setErrored(1);
		
		//calculate the mob's gui size and offset in pixels, and then cache it
		cache_mobSize = BSMobStatRenderConfig.getLivingEntityGUISize(livingEntity, width);
		cache_mobOffset = BSMobStatRenderConfig.getLivingEntityGUIPos(livingEntity, width);
	}
	// --------------------------------------------------
	@Override
	protected void onUpdateTooltip() { tooltip = onUpdateTooltip(this, mobStat); }
	
	public static Text onUpdateTooltip(BSMobStatWidget widget, SUMobStat mobStat)
	{
		String s0 = tt("stat_type.minecraft.killed.none", mobStat.entityName).getString();
		String s1 = tt("stat_type.minecraft.killed_by.none", mobStat.entityName).getString();
		String s2 = "";
		
		if(mobStat.killed != 0)
			s0 = tt("stat_type.minecraft.killed", Integer.toString(mobStat.killed), mobStat.entityName).getString();
		if(mobStat.killedBy != 0)
			s1 = tt("stat_type.minecraft.killed_by", mobStat.entityName, Integer.toString(mobStat.killedBy)).getString();
		if(widget.errored != 0 && !widget.ignoreErrorMessages())
			s2 = "\n" + tt("betterstats.gui.stat.mob.error").getString();
		
		return lt(s0 + "\n" + s1 + "\n" + s2);
	}
	// --------------------------------------------------
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		boolean b0 = super.mouseClicked(mouseX, mouseY, button);
		boolean b1 = hovered && button == 2 && openWikiArticle();
		return b0 || b1;
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
		if(livingEntity != null && !livingEntity.isDead() && errored < 2)
			GuiUtils.applyScissor(x + 1, scissorY + 1, width - 2, scissorHeight - 2, () ->
			{
				int centerX = this.x + (this.width / 2);
				int centerY = this.y + (this.height / 2);
				
				try
				{
					InventoryScreen.drawEntity(
							this.x + cache_mobOffset.x,
							this.y + cache_mobOffset.y,
							cache_mobSize,
							-rInt(mouseX, centerX), -rInt(mouseY, centerY),
							livingEntity);
				}
				catch(Exception exc)
				{
					if(!IGNORE_ENTITY_RENDER_ERRORS) throw exc;
					setErrored(2);
				}
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
	// ==================================================
	/** will show a confirmation screen */
	public boolean openWikiArticle()
	{
		//requires b3 check to pass, aka requires Client environment
		Identifier entityId = EntityType.getId(mobStat.entityType);
		if(!BSWikiLinkConfig.canOpenUrl(entityId, WikiType.MobWiki))
			return false;
		
		//show confirmation screen
		try
		{
			//get current screen
			final Screen s0 = BetterStatsClient.MCClient.currentScreen;
			
			//create confirm screen
			Screen s1 = new ConfirmLinkScreen(
					pass ->
					{
						if(pass)
							BSWikiLinkConfig.openUrl(entityId, WikiType.MobWiki);
						BetterStatsClient.MCClient.setScreen(s0);
					},
					BSWikiLinkConfig.getUrl(entityId, WikiType.MobWiki, "N/A"),
					false);
			
			//set confirm screen
			BetterStatsClient.MCClient.setScreen(s1);
			
			//return
			return true;
		}
		catch(NoClassDefFoundError | NullPointerException err) { return false; }
	}
	// ==================================================
}