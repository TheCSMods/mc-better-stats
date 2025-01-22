package io.github.thecsdev.betterstats.mixin.hooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.stat.Stat;
import net.minecraft.stat.StatFormatter;

@Mixin(value = Stat.class)
public interface AccessorStat
{
	@Accessor("formatter") StatFormatter getFormatter();
}