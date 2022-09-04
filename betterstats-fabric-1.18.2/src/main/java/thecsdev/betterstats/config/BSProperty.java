package thecsdev.betterstats.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BSProperty 
{
	public enum BSPCategory { All, Config, Debug }
	
	String name() default "";
	BSPCategory category() default BSPCategory.Config;
}