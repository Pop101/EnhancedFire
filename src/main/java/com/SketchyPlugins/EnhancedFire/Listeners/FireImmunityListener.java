package com.SketchyPlugins.EnhancedFire.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.java.JavaPlugin;

public class FireImmunityListener implements Listener {
	final JavaPlugin plugin;
	public FireImmunityListener(JavaPlugin main) {
		plugin = main;
		main.getServer().getPluginManager().registerEvents(this, main);
	}
	
	@EventHandler
	public void damageListener(EntityDamageEvent e) {
		if(e.getEntity().hasPermission("EnhancedFire.immunity.fire") || e.getEntity().hasPermission("EnhancedFire.immunity"))
			if(e.getCause() == DamageCause.FIRE || e.getCause() == DamageCause.FIRE_TICK)
				e.setCancelled(true);
		
		if(e.getEntity().hasPermission("EnhancedFire.immunity.lava") || e.getEntity().hasPermission("EnhancedFire.immunity"))
			if(e.getCause() == DamageCause.LAVA || e.getCause() == DamageCause.HOT_FLOOR)
				e.setCancelled(true);
	}
}
