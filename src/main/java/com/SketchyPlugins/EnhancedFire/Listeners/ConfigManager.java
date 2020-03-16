package com.SketchyPlugins.EnhancedFire.Listeners;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {
	public static boolean throwFireballs = true;
	public static List<Material> hotBlocks;
	public static List<Material> cookables;
	public static List<Material> longBurn;
	public static List<Material> longerBurn;
	public static List<Material> ultraBurn; 
	//TODO: make a list and seperate config file to keep track of ash
	public static void init() {
		getConfig();
	}
	
	public static void saveAsh(List<Block> bl) {
		File locations = new File("plugins/EnhancedFire", "AshLocations.yml");
        if (!locations.exists()) {
        	try {
                locations.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(locations);
        config.options().header("Don't touch this please");
        List<String> serialized = new ArrayList<String>();
        for(Block b : bl) {
        	serialized.add(locationToString(b.getLocation()));
        }
        config.set("AshLocs", serialized);
        
        try {
			config.save(locations);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
        throwFireballs = config.getBoolean("CanThrowFireCharge",true);
        hotBlocks = toMaterialsList(config.getStringList("HeatableMaterials"));
        cookables = toMaterialsList(config.getStringList("CauldronCookables"));
        longBurn = toMaterialsList(config.getStringList("LongBurns"));
        longerBurn = toMaterialsList(config.getStringList("LongerBurns"));
        ultraBurn = toMaterialsList(config.getStringList("ExtremelyLongBurns"));
        
        //List<Block> bls = (List<Block>) config.getList("AshBlocks");
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
        if (!locations.exists()) {
            try {
                locations.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(locations);
        config.options().header("Feel free to modify this. Use spigot/bukkit material names (https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html). "
				+ "\nThe output of the Cauldron Cookables will be the result of the furnace recipe corresponding to said input");
        
        config.set("CanThrowFireCharge",true);
        
        List<String> hotBlocks = new ArrayList<String>();
        hotBlocks.add(Material.IRON_BLOCK.toString());
        hotBlocks.add(Material.GOLD_BLOCK.toString());
        hotBlocks.add(Material.SMOOTH_STONE.toString());
        hotBlocks.add(Material.ANDESITE.toString());
        hotBlocks.add(Material.DIORITE.toString());
        hotBlocks.add(Material.GRANITE.toString());
        hotBlocks.add(Material.CAULDRON.toString());
        hotBlocks.add(Material.HOPPER.toString());
        hotBlocks.add(Material.WATER.toString());
		config.set("HeatableMaterials", hotBlocks);
		
		
		List<String> cookables = new ArrayList<String>();
		cookables.add(Material.BAKED_POTATO.toString());
		cookables.add(Material.MUTTON.toString());
		cookables.add(Material.BEEF.toString());
		cookables.add(Material.PORKCHOP.toString());
		cookables.add(Material.CHICKEN.toString());
		cookables.add(Material.SALMON.toString());
		cookables.add(Material.COD.toString());
		cookables.add(Material.CHORUS_FRUIT.toString());
		cookables.add(Material.RABBIT.toString());
		config.set("CauldronCookables", cookables);
		
		List<String> longBurn = new ArrayList<String>();
		longBurn.add(Material.ACACIA_PLANKS.toString());
		longBurn.add(Material.BIRCH_PLANKS.toString());
		longBurn.add(Material.DARK_OAK_PLANKS.toString());
		longBurn.add(Material.JUNGLE_PLANKS.toString());
		longBurn.add(Material.OAK_PLANKS.toString());
		longBurn.add(Material.SPRUCE_PLANKS.toString());
		config.set("LongBurns", longBurn);
		
		List<String> longerBurn = new ArrayList<String>();
		longerBurn.add(Material.ACACIA_LOG.toString());
		longerBurn.add(Material.BIRCH_LOG.toString());
		longerBurn.add(Material.DARK_OAK_LOG.toString());
		longerBurn.add(Material.JUNGLE_LOG.toString());
		longerBurn.add(Material.OAK_LOG.toString());
		longerBurn.add(Material.SPRUCE_LOG.toString());
		longerBurn.add(Material.STRIPPED_ACACIA_LOG.toString());
		longerBurn.add(Material.STRIPPED_BIRCH_LOG.toString());
		longerBurn.add(Material.STRIPPED_DARK_OAK_LOG.toString());
		longerBurn.add(Material.STRIPPED_JUNGLE_LOG.toString());
		longerBurn.add(Material.STRIPPED_OAK_LOG.toString());
		longerBurn.add(Material.STRIPPED_SPRUCE_LOG.toString());
		config.set("LongerBurns", longBurn);
		
		List<String> ultraBurn = new ArrayList<String>();
		ultraBurn.add(Material.COAL_BLOCK.toString());
		config.set("ExtremelyLongBurns", ultraBurn);
        
        try {
        	config.save(locations);
        	getConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
