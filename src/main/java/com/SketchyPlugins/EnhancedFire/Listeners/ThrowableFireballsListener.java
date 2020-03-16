package com.SketchyPlugins.EnhancedFire.Listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ThrowableFireballsListener implements Listener {
	final JavaPlugin plugin;
	public ThrowableFireballsListener(JavaPlugin main) {
		main.getServer().getPluginManager().registerEvents(this, main);
		plugin = main;
	}
	@EventHandler(priority = EventPriority.HIGH)
	public void playerThrowMainhand(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_AIR)
			return;
		e.setCancelled(tryThrowFireball(e.getPlayer(), e.getPlayer().getInventory().getItemInMainHand())); //if it throws a fireball, cancel the event
	}
	@EventHandler(priority = EventPriority.HIGH)
	public void playerThrowOffhand(PlayerInteractEvent e) {
		if (e.getAction() != Action.LEFT_CLICK_BLOCK && e.getAction() != Action.LEFT_CLICK_AIR)
			return;
		e.setCancelled(tryThrowFireball(e.getPlayer(), e.getPlayer().getInventory().getItemInOffHand())); //if it throws a fireball, cancel the event
	}
	boolean tryThrowFireball(Player plr, ItemStack st) {
		if(st == null)
			return false;
		if(st.getType() != Material.FIRE_CHARGE)
			return false;
		
		if(plr.getGameMode() != GameMode.CREATIVE) {
			if(st.getAmount() > 1) st.setAmount(st.getAmount()-1);
			else plr.getInventory().remove(st);
		}
		
		Fireball fb = plr.launchProjectile(Fireball.class);
		fb.setIsIncendiary(true);
		
		return true;
	}
}
