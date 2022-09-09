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
		All("betterstats.config.category.all"),
		Config("betterstats.config.category.config"),
		Debug("betterstats.config.category.debug");
		
		public final Text text;
		public final String textKey;
		BSPCategory(String key) { this.textKey = key; this.text = tt(key); }
		public Text asText() { return text; }
	}
	BSPCategory category() default BSPCategory.Config;
}