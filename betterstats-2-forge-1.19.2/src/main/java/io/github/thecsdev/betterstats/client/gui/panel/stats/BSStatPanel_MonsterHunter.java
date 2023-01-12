package io.github.thecsdev.betterstats.client.gui.panel.stats;

import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.thecsdev.betterstats.util.StatUtils.StatUtilsMobStat;
import io.github.thecsdev.betterstats.util.StatUtils.StatUtilsStat;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class BSStatPanel_MonsterHunter extends BSStatPanel_Mobs
{
	// ==================================================
	public BSStatPanel_MonsterHunter(int x, int y, int width, int height) { super(x, y, width, height); }
	public BSStatPanel_MonsterHunter(TPanelElement parentToFill) { super(parentToFill); }
	// ==================================================
	public @Override Predicate<StatUtilsStat> getStatPredicate()
	{
		return super.getStatPredicate().and(stat ->
		{
			//check the hunter stat for mob type,
			//and make sure it is a monster
			StatUtilsMobStat mobStat = (StatUtilsMobStat)stat;
			return mobStat.entityType.getCategory() == MobCategory.MONSTER &&
					mobStat.entityType != EntityType.GIANT &&
					mobStat.entityType != EntityType.ILLUSIONER;
		});
	}
	// ==================================================
	protected BSStatWidget_Mob createStatWidget(StatUtilsMobStat stat, int x, int y, int size)
	{
		return new BSStatWidget_MonsterHunter(stat, x, y, size);
	}
	// ==================================================
	protected class BSStatWidget_MonsterHunter extends BSStatWidget_Mob
	{
		public BSStatWidget_MonsterHunter(StatUtilsMobStat stat, int x, int y, int size) { super(stat, x, y, size); }
		public @Override void postRender(PoseStack matrices, int mouseX, int mouseY, float deltaTime)
		{
			if(stat.killed > 0) drawOutline(matrices, COLOR_GOLD_FOCUSED);
			else if(isFocused())
				drawOutline(matrices, COLOR_NORMAL_FOCUSED);
			else if(isHovered() || (!stat.isEmpty() && stat.killed < 1))
				drawOutline(matrices, COLOR_NORMAL_HOVERED);
		}
	}
	// ==================================================
}