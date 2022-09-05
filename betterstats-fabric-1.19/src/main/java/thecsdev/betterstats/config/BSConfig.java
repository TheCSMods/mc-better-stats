package thecsdev.betterstats.config;

import static thecsdev.betterstats.BetterStats.LOGGER;
import static thecsdev.betterstats.BetterStats.ModID;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import thecsdev.betterstats.BetterStats;
import thecsdev.betterstats.config.BSProperty.BSPCategory;

public final class BSConfig
{
	// ==================================================
	//when turned off, the Options tab goes bye bye
	@BSProperty public static boolean BS_OPTIONS_GUI;
	
	//keeps track of whether or not the user has seen the new
	//statistics screen by pressing the 'Statistics' button
	@BSProperty public static boolean SEEN_BSS;
	
	//whether or not to draw an extra background image for the 'Statistics' button
	@BSProperty public static boolean BSS_BTN_IMG;
	
	//whether or not to allow the player to shift+click items and so on
	@BSProperty public static boolean ALLOW_CHEATS;
	
	//BS screen menu filters
	@BSProperty public static boolean FILTER_HIDE_EMPTY_STATS;
	@BSProperty public static boolean FILTER_SHOW_ITEM_NAMES;
	
	//BS screen GUI element colors
	@BSPropertyColorInt public static int COLOR_CONTENTPANE_BG;
	@BSPropertyColorInt public static int COLOR_STAT_GENERAL_TEXT;
	@BSPropertyColorInt public static int COLOR_STAT_BG;
	@BSPropertyColorInt public static int COLOR_STAT_OUTLINE;
	@BSPropertyColorInt public static int COLOR_CATEGORY_NAME_NORMAL;
	@BSPropertyColorInt public static int COLOR_CATEGORY_NAME_HIGHLIGHTED;
	// --------------------------------------------------
	@BSProperty(category = BSPCategory.Debug) public static boolean DEBUG_SHOW_EVERYTHING = false;
	@BSProperty(category = BSPCategory.Debug) public static boolean DEBUG_DINNERBONE_MODE = false;
	// ==================================================
	public static void saveProperties()
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
			prop.setProperty("SEEN_BSS", Boolean.toString(SEEN_BSS));
			prop.setProperty("BSS_BTN_IMG", Boolean.toString(BSS_BTN_IMG));
			prop.setProperty("ALLOW_CHEATS", Boolean.toString(ALLOW_CHEATS));
			prop.setProperty("BS_OPTIONS_GUI", Boolean.toString(BS_OPTIONS_GUI));
			
			prop.setProperty("FILTER_HIDE_EMPTY_STATS", Boolean.toString(FILTER_HIDE_EMPTY_STATS));
			prop.setProperty("FILTER_SHOW_ITEM_NAMES", Boolean.toString(FILTER_SHOW_ITEM_NAMES));
			
			prop.setProperty("COLOR_STAT_GENERAL_TEXT", Integer.toString(COLOR_STAT_GENERAL_TEXT));
			prop.setProperty("COLOR_STAT_BG", Integer.toString(COLOR_STAT_BG));
			prop.setProperty("COLOR_STAT_OUTLINE", Integer.toString(COLOR_STAT_OUTLINE));
			prop.setProperty("COLOR_CONTENTPANE_BG", Integer.toString(COLOR_CONTENTPANE_BG));
			prop.setProperty("COLOR_CATEGORY_NAME_NORMAL", Integer.toString(COLOR_CATEGORY_NAME_NORMAL));
			prop.setProperty("COLOR_CATEGORY_NAME_HIGHLIGHTED", Integer.toString(COLOR_CATEGORY_NAME_HIGHLIGHTED));
			
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
	
	public static void loadProperties()
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
			SEEN_BSS = smartBool(prop.getProperty("SEEN_BSS"), false);
			BSS_BTN_IMG = smartBool(prop.getProperty("BSS_BTN_IMG"), false);
			ALLOW_CHEATS = smartBool(prop.getProperty("ALLOW_CHEATS"), false);
			BS_OPTIONS_GUI = smartBool(prop.getProperty("BS_OPTIONS_GUI"), true);
			
			FILTER_HIDE_EMPTY_STATS = smartBool(prop.getProperty("FILTER_HIDE_EMPTY_STATS"), false);
			FILTER_SHOW_ITEM_NAMES = smartBool(prop.getProperty("FILTER_SHOW_ITEM_NAMES"), true);
			
			COLOR_STAT_GENERAL_TEXT = smartInt(prop.getProperty("COLOR_STAT_GENERAL_TEXT"), Color.white.getRGB());
			COLOR_STAT_BG = smartInt(prop.getProperty("COLOR_STAT_BG"), new Color(180,180,180, 35).getRGB());
			COLOR_STAT_OUTLINE = smartInt(prop.getProperty("COLOR_STAT_OUTLINE"), Color.lightGray.getRGB());
			COLOR_CONTENTPANE_BG = smartInt(prop.getProperty("COLOR_CONTENTPANE_BG"), new Color(0, 0, 0, 120).getRGB());
			COLOR_CATEGORY_NAME_NORMAL = smartInt(prop.getProperty("COLOR_CATEGORY_NAME_NORMAL"), new Color(255, 255, 0, 200).getRGB());
			COLOR_CATEGORY_NAME_HIGHLIGHTED = smartInt(prop.getProperty("COLOR_CATEGORY_NAME_HIGHLIGHTED"), Color.yellow.getRGB());
			
			//log
			LOGGER.info("Loaded '" + ModID + "' config.");
		}
		catch(IOException ioExc)
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
	// ==================================================
	private static boolean smartBool(String arg0, boolean def)
	{
		if(arg0 == null) return def;
		String a = arg0.split(" ")[0].toLowerCase();
		return (a.startsWith("true") || a.startsWith("ye") ||
				a.startsWith("ok") || a.startsWith("sur")) && a.length() <= 5;
	}
	
	private static int smartInt(String arg0, int def)
	{
		try { return Integer.parseInt(arg0); }
		catch(Exception e) { return def; }
	}
	
	/*private static int smartInt(String arg0, int min, int max, int def)
	{
		try { return MathHelper.clamp(Integer.parseInt(arg0), min, max); }
		catch(Exception e) { return MathHelper.clamp(def, min, max); }
	}*/
	// ==================================================
}