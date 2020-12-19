package com.SketchyPlugins.EnhancedFire.Listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class HotBlocksListener implements Listener {
	final JavaPlugin plugin;
	public HotBlocksListener(JavaPlugin main) {
		plugin = main;
		main.getServer().getPluginManager().registerEvents(this, main);
		
		hotBlocks = new HashMap<Block,List<Block>>();
		
		possibleHotBlocks = ConfigManager.hotBlocks;
		
		//re-run loop
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, (Runnable)new Runnable() {
            public void run() {
                loop();
            }
        },30);
	}
	List<Material> possibleHotBlocks;
	protected HashMap<Block,List<Block>> hotBlocks;
	//returns true if block is hot
	boolean isHotBlock(Block bl) {
		if(!bl.getChunk().isLoaded()) //prevent chunk not loaded errors
			return false;
		if(!possibleHotBlocks.contains(bl.getType())) //if it can't be hot, return false
			return false;
		boolean foundFire = false;
		for(int y = -3; y <= -1; y++) {
			Block current =  bl.getLocation().add(0, y, 0).getBlock();
			//if it found a fire but was insulated by something
			if(foundFire && !possibleHotBlocks.contains(bl.getType())) 
				foundFire = false;
			//if found fire
			if(current.getType() == Material.FIRE || current.getType() == Material.LAVA)
				foundFire = true;
		}
		return foundFire;
	}
	boolean isHotWater(Block bl) {
		for(Block b : hotBlocks.keySet())
			if(b != null && hotBlocks.get(b) != null)
				for(Block water : hotBlocks.get(b))
					if(bl.equals(water))
						return true;
		return false;
	}
	
	/*//scan near all players
	@EventHandler
	public void scanForFire(PlayerMoveEvent e) {
		for(int x = -30; x < 30; x++)
			for(int z = -30; z < 30; z++)
				for(int y = -20; y < 5; y++) {
					Block b = e.getPlayer().getLocation().add(x, y, z).getBlock();
					addHotBlock(b);
				}
	}*/

	//does additional scans as well as the damage
	public void loop() {
		//scan near all players
		for(Player pl : Bukkit.getServer().getOnlinePlayers())
			for(int x = -30; x < 30; x++)
				for(int z = -30; z < 30; z++)
					for(int y = -20; y < 5; y++) {
						Block b = pl.getLocation().add(x, y, z).getBlock();
						addHotBlock(b);
					}
		//every so often, check all entities for hot blocks
		if(Math.random() < 0.3) {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, (Runnable)new Runnable() {
	            public void run() {
	                for(World w : Bukkit.getWorlds()) {
	                	if(w != null && w.getEntitiesByClass(Damageable.class) != null)
	                	for(Damageable e : w.getEntitiesByClass(Damageable.class)) {
	                		if(e.isOnGround()) {
	                			Block below = e.getLocation().add(0, -1, 0).getBlock();
	                			addHotBlock(below);
	                		}
	                	}
	                }
	            }
	        });
		}
		//tick all hot blocks, removing ones that aren't hot anymore
		List<Block> coldBlocks = new ArrayList<Block>();
		List<Block> hotWater = new ArrayList<Block>();
		for(final Block b : hotBlocks.keySet()) {
			if(!isHotBlock(b) || b.getType() == Material.AIR)
				coldBlocks.add(b);
			
			hotBlockTick(b);
			List<Block> coldWater = new ArrayList<Block>();
			for(final Block z : hotBlocks.get(b)) {
				if(z.getType() != Material.WATER)
					coldWater.add(z);
				if(!hotWater.contains(z)) {
					hotBlockTick(z);
					hotWater.add(z);
				}
			}
			hotBlocks.get(b).removeAll(coldWater);
		}
		for(Block b : coldBlocks) {
			hotBlocks.remove(b);
			
		}
		//re-run loop
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, (Runnable)new Runnable() {
            public void run() {
                loop();
            }
        },15);
	}
	void hotBlockTick(final Block b) {
		String permission = "EnhancedFire.immunity.";
		if(b.getType() == Material.WATER || b.getType() == Material.CAULDRON)
			permission += "water";
		else
			permission += "blocks";
		
		//do damage
		Location onTop = b.getLocation().add(0.5,1,0.5);
		for(Entity e : onTop.getWorld().getNearbyEntities(onTop, 0.24, 0.24, 0.24)) {
			if(e instanceof Damageable && !e.hasPermission("EnhancedFire.immunity") && !e.hasPermission(permission))
				((Damageable) e).damage(ConfigManager.hotdamage);
		}
		//if cauldron, do special stuff
		if(b.getType() == Material.CAULDRON) {
			//damage entities inside it as well
			for(Entity e : b.getWorld().getNearbyEntities(b.getLocation().add(0.5,0.25,0.5), 0.24, 0.24, 0.24)) {
				if(e instanceof Damageable && !e.hasPermission("EnhancedFire.immunity") && !e.hasPermission(permission))
					((Damageable) e).damage(ConfigManager.hotdamage);
			}
			//call cauldron tick
			if(b.getData() > 0)
				HotCauldronManager.handleBoilingCauldron(b, plugin);
		} 
		//else: spawn particles
		else if(b.getType() == Material.WATER) {
			for(int i = 0; i < 2; i++)
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, (Runnable)new Runnable() {
		            public void run() {
		            	b.getWorld().spawnParticle(Particle.WATER_SPLASH, b.getLocation().add(0.5, 1.1, 0.5), 3, 0.25, 0.0, 0.25);
		            }
		        },(long) (Math.random()*30));
		}
		else {
			Block above = b.getLocation().add(0,1,0).getBlock();
			//don't spawn particles if there's no reason to
			if(!above.isLiquid() && !above.getType().isSolid())
				for(int i = 0; i < 2; i++)
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, (Runnable)new Runnable() {
			            public void run() {
			            	b.getWorld().spawnParticle(Particle.SMOKE_LARGE, b.getLocation().add(0.5,1.1,0.5), 2, 0.25, 0, 0.25,0);
			            }
			        },(long) (Math.random()*30));
			//if block is liquid, add cool effects
			if(above.isLiquid())
				for(int i = 0; i < 2; i++)
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, (Runnable)new Runnable() {
			            public void run() {
			            	b.getWorld().spawnParticle(Particle.FLAME, b.getLocation().add(0.5,1.1,0.5), 2, 0.4, 0, 0.4,0);
			            }
			        },(long) (Math.random()*30));
		}
	}
	void addHotBlock(Block b) {
		if(b == null)
			return;
		if(!isHotBlock(b))
			return;
		if(!hotBlocks.containsKey(b)) {
			hotBlocks.put(b, new ArrayList<Block>());
		}
		//add nearby water if water gets hot
		if(b.getType() == Material.WATER) {
			for(int x = -4; x <= 4; x++)
				for(int z = -4; z <= 4; z++)
					for(int y = -1; y <= 1; y++) {
						Block test = b.getLocation().add(x,y,z).getBlock();
						if(test.getType() == Material.WATER)
							if(!hotBlocks.get(b).contains(test))
								hotBlocks.get(b).add(test);
				}
		}
	}

	//make sure that items dropped in cauldrons don't get picked up early
	@EventHandler
	public void catchCauldronHopperCollection(InventoryPickupItemEvent e) {
		if(e.getInventory().getLocation() != null)
			if(e.getInventory().getLocation().add(0,1,0).getBlock().getType() == Material.CAULDRON)
				e.setCancelled(true);
		
		//if(e.getItem().getLocation().getBlock().getType() == Material.CAULDRON)
		//	e.setCancelled(true);
	}
}
