package com.SketchyPlugins.EnhancedFire.Listeners;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
	public static JavaPlugin plugin;
	
	public static Material ashMat = Material.LIGHT_GRAY_CONCRETE_POWDER;
	public static double hotdamage = 2.0;
	public static boolean burncook = true;
	public static boolean infiniteCauldrons = false;
	public static boolean throwFireballs = true;
	public static boolean enableAsh = true;
	public static double ashFlintchance = 0.1;
	public static double ashCreationChance = 0.6;
	public static double ashPassableCreationChance = 0.3;
	public static double ashExpChance = 0.4;
	public static List<Material> hotBlocks;
	public static List<Material> cookables;
	public static HashMap<Material, Integer> burnLength;
	public static HashMap<Material, AshData> ashType; 
	public static HashMap<Material, Material> scorchRecipes;
	
	
	public static void init(JavaPlugin plugin_) {
		plugin = plugin_;
		
		File location = new File("plugins/EnhancedFire", "Config.yml");
        if (!location.exists()) {
        	location.mkdirs();
        	copyResource("config.yml", plugin.getDataFolder().getAbsolutePath()+File.separator+"Config.yml");
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(location);
        if(config.getConfigurationSection("Burn Times") == null) { // some new arbitrary check
        	copyResource("config.yml", plugin.getDataFolder().getAbsolutePath()+File.separator+"Config.yml");
        	config = YamlConfiguration.loadConfiguration(location);
        }
        
        ashMat = safeMatParse(config.getString("Ash Material", "LIGHT_GRAY_CONCRETE_POWDER"));
        if(ashMat == null) ashMat = Material.LIGHT_GRAY_CONCRETE_POWDER;
        
        burncook = config.getBoolean("Burning down Blocks Cooks them", true);
        hotdamage = config.getDouble("Hot Block Damage", 2.0);
        infiniteCauldrons = config.getBoolean("Cauldron Cooking uses Water",true);
        throwFireballs = config.getBoolean("Throwable Fire Charges",true);
        enableAsh = config.getBoolean("Burning Blocks make Ash", true);
        ashFlintchance = config.getDouble("Ash Flint Chance", ashFlintchance);
        ashCreationChance = config.getDouble("Ash Creation Chance", ashCreationChance);
        ashPassableCreationChance = config.getDouble("Passable Ash Creation Chance", ashPassableCreationChance);
        
        ashExpChance = config.getDouble("Cumulative Ash Experience Chance", ashExpChance);
        if(ashExpChance >= 0.98) ashExpChance = 0.98; //still stupidly high
        
        hotBlocks = toMaterialsList(config.getStringList("Heatable Materials"));
        cookables = toMaterialsList(config.getStringList("Cauldron Cookables"));
        
        burnLength = new HashMap<Material, Integer>();
        ashType = new HashMap<Material, AshData>();
        scorchRecipes = new HashMap<Material, Material>();
        
        //load burn times
        Map<String, Object> bs = plugin.getConfig().getConfigurationSection("Burn Times").getValues(false);
		for(String key : bs.keySet()) {
			Material m = safeMatParse(key);
			int time = safeParse(bs.toString(), 1);
			if(m != null) burnLength.put(m, time);
		}
		Bukkit.getLogger().info("Loaded "+burnLength.size()+" burn times");
		
		//load ash info
		Map<String, Object> ads = plugin.getConfig().getConfigurationSection("Ash Details").getValues(false);
		AshData def = null;
		AshData pass = null;
		for(String key : ads.keySet()) {
			AshData ad = null;
			Object o = ads.get(key);
			
			//parse to AshData
			if(o instanceof ConfigurationSection) {
				ConfigurationSection cs = (ConfigurationSection) o;
				String type = cs.getString("Type","block").strip().toLowerCase();
				int type_int = 0;
				if(type.startsWith("block")) type_int = 1;
				if(type.startsWith("particle")) type_int = 2;
				if(type.startsWith("scorch")) type_int = 3;
				ad = new AshData(type_int, (float) cs.getDouble("Chance", 0.0));
			}
			if(ad == null) continue;
			
			//parse and apply key
			if(key.strip().toLowerCase().startsWith("default")) def = ad;
			else if(key.strip().toLowerCase().startsWith("passable")) pass = ad;
			else {
				Material m = safeMatParse(key);
				if(m != null) ashType.put(m, ad);
			}
		}
		Bukkit.getLogger().info("Loaded "+ashType.size()+" explicit ash recipes");
		
		//apply ash defaults
		for(Material m : Material.values()) {
			if(ashType.containsKey(m)) continue;
			
			if(pass != null && m.isTransparent()) ashType.put(m, pass);
			else if(def != null) ashType.put(m, def);
		}
		Bukkit.getLogger().info("Loaded "+ashType.size()+" explicit+implicit ash recipes");
		
		//parse scorch recipes
		Map<String, Object> sr = plugin.getConfig().getConfigurationSection("Scorch Recipes").getValues(false);
		for(String key : sr.keySet()) {
			Material i = safeMatParse(key);
			Material o = safeMatParse(sr.get(key).toString());
			if(i != null && o != null) scorchRecipes.put(i, o);
		}
		Bukkit.getLogger().info("Loaded "+scorchRecipes.size()+" scorches");
	}
	
	public static void saveAsh(List<Block> bl) {
		File locations = new File("plugins/EnhancedFire", "AshLocations.yml");
		locations.delete();
    	try {
            locations.createNewFile();
        } catch (IOException e) {e.printStackTrace();}
    	
        FileConfiguration config = YamlConfiguration.loadConfiguration(locations);
        config.options().header("Don't touch this please");
        List<String> serialized = new ArrayList<String>();
        for(Block b : bl) {
        	if(b.getType() == Material.LIGHT_GRAY_CONCRETE_POWDER && b.getLocation() != null) //double check here (hard coding bad though)
        		serialized.add(locationToString(b.getLocation()));
        }
        
        config.set("AshLocs", serialized);
        
        try {
			config.save(locations);
		} catch (IOException e) {e.printStackTrace();}
	}
	
	public static List<Block> loadAsh(){
		File locations = new File("plugins/EnhancedFire", "AshLocations.yml");
        if (!locations.exists()) {
        	saveAsh(new ArrayList<Block>());
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(locations);
        List<Block> unserialized = new ArrayList<Block>();
        for(String s : config.getStringList("AshLocs")) {
        	if(s != null) {
	        	Location loc = locationFromString(s);
	        	if(loc != null) {
	        		Block bl = loc.getBlock();
	        		if(bl != null) unserialized.add(bl);
	        	}
        	}
        }
        return unserialized;
	}
	
	private static List<Material> toMaterialsList(List<String> strs) {
		List<Material> out = new ArrayList<Material>();
		for(String str : strs) {
			out.add(safeMatParse(str));
		}
		return out;
	}
	private static int safeParse(String s, int def) {
		try {
			return Integer.parseInt(s);
		}
		catch(Exception e) {
			return def;
		}
	}
	
	private static Material safeMatParse(String s) {
		try {
			return Material.valueOf(s.strip().toUpperCase());
		}
		catch(Exception e) {
			return null;
		}
	}
	
	private static final String regex = "~";
	public static String locationToString(Location loc) {
		return loc.getWorld().getUID()+regex+loc.getX()+regex+loc.getY()+regex+loc.getZ();
	}
	public static Location locationFromString(String str) {
		String[] strs = str.split(regex);
		try {
			return new Location(Bukkit.getWorld(UUID.fromString(strs[0])),Double.parseDouble(strs[1]),Double.parseDouble(strs[2]),Double.parseDouble(strs[3]));
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void copyResource(String res, String dest) {
	    try {
	    	InputStream src = plugin.getResource(res);
			Files.copy(src, Paths.get(dest), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.WARNING, "Internal JAR file missing");
		}
	}
    
	static class AshData {
		public int type; //0: none. 1: default ash. 2: particle. 3: scorch earth
		public float chance;
		public AshData(int t, float c) {type=t; chance=c;}
	}
}
