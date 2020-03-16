package com.SketchyPlugins.EnhancedFire.Listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class RecipeListener  implements Listener {
	JavaPlugin plugin;
	List<NamespacedKey> recipes;
	public RecipeListener(JavaPlugin main) {
		plugin = main;
		main.getServer().getPluginManager().registerEvents(this, main);
		
		recipes = new ArrayList<NamespacedKey>();
	}
	
	//should work to get recipes in ur krafting buuk
	@EventHandler
	public void playerjoin(PlayerJoinEvent e) {
		e.getPlayer().discoverRecipes(recipes);
	}
}
