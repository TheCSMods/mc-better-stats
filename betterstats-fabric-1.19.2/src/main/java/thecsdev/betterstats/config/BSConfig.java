package thecsdev.betterstats.config;

import static thecsdev.betterstats.BetterStats.LOGGER;
import static thecsdev.betterstats.BetterStats.ModID;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Properties;

import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import thecsdev.betterstats.BetterStats;
import thecsdev.betterstats.config.BSProperty.BSPCategory;

public final class BSConfig
{
	// ==================================================
	//when turned off, the Options tab goes bye bye
	@BSConfigHeader("general")
	@BSProperty public static boolean BS_OPTIONS_GUI = true;
	
	//keeps track of whether or not the user has seen the new
	//statistics screen by pressing the 'Statistics' button
	@BSProperty public static boolean SEEN_BSS = false;
	
	//whether or not to draw an extra background image for the 'Statistics' button
	@BSProperty public static boolean BSS_BTN_IMG = false;
	
	//whether or not to allow the player to shift+click items and so on
	@BSProperty public static boolean ALLOW_CHEATS = false;
	
	//recommended not to do this. ignores any errors
	//modded entities may throw while they are being rendered
	@BSProperty public static boolean IGNORE_ENTITY_RENDER_ERRORS = true;
	
	//when enabled, the mod will be able to open wiki article
	//links for given statistic entries
	@BSConfigHeader("help")
	@BSProperty public static boolean ENABLE_WIKI_LINKS = true;
	@BSProperty public static boolean ENABLE_REI_LINKS = true;
	
	//BS screen menu filters
	@BSConfigHeader("filters")
	@BSProperty public static boolean FILTER_HIDE_EMPTY_STATS = false;
	@BSProperty public static boolean FILTER_SHOW_ITEM_NAMES = true;
	
	//tooltip colors
	@BSConfigHeader("colors")
	@BSPropertyColorInt public static int COLOR_TOOLTIP_TEXT = Color.white.getRGB();
	@BSPropertyColorInt public static int COLOR_TOOLTIP_BG = new Color(15,0,15).getRGB();
	@BSPropertyColorInt public static int COLOR_TOOLTIP_OUTLINE = new Color(27,0,62).getRGB();
	
	//BS screen GUI element colors
	@BSPropertyColorInt public static int COLOR_CONTENTPANE_BG = new Color(0, 0, 0, 120).getRGB();
	@BSPropertyColorInt public static int COLOR_STAT_GENERAL_TEXT = Color.white.getRGB();
	@BSPropertyColorInt public static int COLOR_STAT_BG = new Color(180,180,180, 35).getRGB();
	@BSPropertyColorInt public static int COLOR_STAT_BG_ERRORED = new Color(255,150,150, 45).getRGB();
	@BSPropertyColorInt public static int COLOR_STAT_OUTLINE = Color.lightGray.getRGB();
	@BSPropertyColorInt public static int COLOR_CATEGORY_NAME_NORMAL = new Color(255, 255, 0, 200).getRGB();
	@BSPropertyColorInt public static int COLOR_CATEGORY_NAME_HIGHLIGHTED = Color.yellow.getRGB();
	// --------------------------------------------------
	@BSProperty(category = BSPCategory.Debug) public static boolean DEBUG_SHOW_EVERYTHING = false;
	@BSProperty(category = BSPCategory.Debug) public static boolean DEBUG_DINNERBONE_MODE = false;
	@BSProperty(category = BSPCategory.Debug) public static boolean DEBUG_LOG_SCREEN_CHANGES = false;
	// ==================================================
	public static void saveProperties() { saveProperties(BSConfig.class); }
	public static void saveProperties(Class<?> configClazz)
	{
		try
		{
			//get and make sure the properties file exists
			File fProp = getPropertiesFile();
			if(!fProp.exists())
			{
				fProp.getParentFile().mkdirs();
				fProp.createNewFile();
			}
			
			//create a Properties instance and store the properties
			Properties prop = new Properties();
			for(Field configField : configClazz.getDeclaredFields())
			{
				BSPCategory bspc = getPropertyCategory(configField);
				if(bspc == null || bspc == BSPCategory.Debug) continue;
				
				try { prop.setProperty(configField.getName(), Objects.toString(configField.get(null))); }
				catch(Exception e) { throw new IOException("Unable to read config values."); }
			}
			
			//save the properties
			FileOutputStream fos = new FileOutputStream(fProp);
			prop.store(fos, ModID + " properties");
			fos.close();

			//log
			LOGGER.info("Saved '" + ModID + "' config.");
		}
		catch(IOException ioExc)
		{
			throw new CrashException(new CrashReport("Unable to save the '" + ModID + "' mod config.", ioExc));
		}
	}
	
	public static void loadProperties() { loadProperties(BSConfig.class); }
	public static void loadProperties(Class<?> configClazz)
	{
		try
		{
			//create a Properties instance and load the properties
			Properties prop = new Properties();
			
			//get and make sure the properties file exists
			File fProp = getPropertiesFile();
			if(!fProp.exists())
			{
				LOGGER.info("Could not load '" + ModID + "' config. File not found.");
				//default properties will be loaded from this point onward...
			}
			else
			{
				FileInputStream fis = new FileInputStream(fProp);
				prop.load(fis);
				fis.close();
			}
			
			//read the properties
			for(Field configField : configClazz.getDeclaredFields())
			{
				BSPCategory bspc = getPropertyCategory(configField);
				if(bspc == null || bspc == BSPCategory.Debug) continue;
				
				String value = prop.getProperty(configField.getName(), null);
				if(value == null) continue;
				
				if(configField.getType().equals(Boolean.TYPE))
					configField.set(null, smartBool(value));
				else if(configField.getType().equals(Integer.TYPE))
					configField.set(null, Integer.parseInt(value));
				else if(configField.getType().equals(String.class))
					configField.set(null, value);
			}
			
			//log
			LOGGER.info("Loaded '" + ModID + "' config.");
		}
		catch(IOException | IllegalAccessException ioExc)
		{
			throw new CrashException(new CrashReport("Unable to load the '" + ModID + "' mod config.", ioExc));
		}
	}
	// ==================================================
	public static File getPropertiesFile()
	{
		return new File(System.getProperty("user.dir") +
				"/config/" + BetterStats.ModID + ".properties");
	}
	// --------------------------------------------------
	public static BSPCategory getPropertyCategory(Field propertyField)
	{
		BSProperty bsp = propertyField.getAnnotation(BSProperty.class);
		BSPropertyColorInt bspci = propertyField.getAnnotation(BSPropertyColorInt.class);
		if(bsp != null) return bsp.category();
		else if(bspci != null) return bspci.property().category();
		else return null;
	}
	// ==================================================
	private static boolean smartBool(String arg0)
	{
		if(arg0 == null) throw new IllegalArgumentException(arg0);
		String a = arg0.split(" ")[0].toLowerCase();
		return (a.startsWith("true") || a.startsWith("ye") ||
				a.startsWith("ok") || a.startsWith("sur")) && a.length() <= 5;
	}
	// ==================================================
}