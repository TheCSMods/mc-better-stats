package thecsdev.betterstats.config;

import static thecsdev.betterstats.BetterStats.tt;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.minecraft.text.Text;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BSProperty 
{
	public enum BSPCategory
	{
		All(tt("betterstats.config.category.all")),
		Config(tt("betterstats.config.category.config")),
		Debug(tt("betterstats.config.category.debug"));
		
		private final Text text;
		BSPCategory(Text text) { this.text = text; }
		public Text asText() { return text; }
	}
	BSPCategory category() default BSPCategory.Config;
}