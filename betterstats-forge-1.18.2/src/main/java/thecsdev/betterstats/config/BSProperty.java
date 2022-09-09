package thecsdev.betterstats.config;

import static thecsdev.betterstats.BetterStats.tt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.minecraft.network.chat.Component;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BSProperty 
{
	public enum BSPCategory
	{
		All(tt("betterstats.config.category.all")),
		Config(tt("betterstats.config.category.config")),
		Debug(tt("betterstats.config.category.debug"));
		
		private final Component text;
		BSPCategory(Component text) { this.text = text; }
		public Component asText() { return text; }
	}
	BSPCategory category() default BSPCategory.Config;
}