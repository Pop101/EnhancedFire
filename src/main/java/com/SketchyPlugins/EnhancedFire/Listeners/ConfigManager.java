package com.SketchyPlugins.EnhancedFire.Listeners;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
	public static JavaPlugin plugin;
	
	public static boolean infiniteCauldrons = false;
	public static boolean throwFireballs = true;
	public static boolean enableAsh = true;
	public static double ashFlintchance = 0.1;
	public static List<Material> hotBlocks;
	public static List<Material> cookables;
	public static List<Material> longBurn;
	public static List<Material> longerBurn;
	public static List<Material> ultraBurn; 
	//TODO: make a list and seperate config file to keep track of ash
	public static void init(JavaPlugin plugin_) {
		plugin = plugin_;
		getConfig();
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
        	if(b.getType() == Material.LIGHT_GRAY_CONCRETE_POWDER) //double check here (hard coding bad though)
        		serialized.add(locationToString(b.getLocation()));
        }
        
        config.set("AshLocs", serialized);
        
        try {
			config.save(locations);
		} catch (IOException e) {e.printStackTrace();}
	}
	public static List<Block> getAsh(){
		File locations = new File("plugins/EnhancedFire", "AshLocations.yml");
        if (!locations.exists()) {
        	saveAsh(new ArrayList<Block>());
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(locations);
        List<Block> unserialized = new ArrayList<Block>();
        for(String s : config.getStringList("AshLocs")) {
        	if(s != null) {
	        	Location loc = locationFromString(s);
	        	if(loc != null && !loc.equals(null)) {
	        		Block bl = loc.getBlock();
	        		if(bl != null)
	        			unserialized.add(bl);
	        	}
        	}
        }
        return unserialized;
	}
	static void getConfig() {
		File locations = new File("plugins/EnhancedFire", "Config.yml");
        if (!locations.exists()) {
        	createHotBlocks();
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(locations);
        infiniteCauldrons = config.getBoolean("Cauldron Cooking uses Water",true);
        throwFireballs = config.getBoolean("Throwable Fire Charges",true);
        enableAsh = config.getBoolean("Burning Blocks make Ash", true);
        ashFlintchance = config.getDouble("Ash Flint Chance", ashFlintchance);
        
        hotBlocks = toMaterialsList(config.getStringList("HeatableMaterials"));
        cookables = toMaterialsList(config.getStringList("CauldronCookables"));
        longBurn = toMaterialsList(config.getStringList("LongBurns"));
        longerBurn = toMaterialsList(config.getStringList("LongerBurns"));
        ultraBurn = toMaterialsList(config.getStringList("ExtremelyLongBurns"));
	}
	static List<Material> toMaterialsList(List<String> strs) {
		List<Material> out = new ArrayList<Material>();
		for(String str : strs) {
			out.add(Material.valueOf(str));
		}
		return out;
	}
	static final String regex = "~";
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
    public static void createHotBlocks() {
    	File locations = new File("plugins/EnhancedFire", "Config.yml");
    	locations.mkdirs();
        
    	copyResource("config.yml", plugin.getDataFolder().getAbsolutePath()+File.separator+"Config.yml");
    }
	public static void copyResource(String res, String dest) {
	    try {
	    	InputStream src = plugin.getResource(res);
			Files.copy(src, Paths.get(dest), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.WARNING, "Internal JAR file missing");
		}
	}
    
}
