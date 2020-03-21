package com.SketchyPlugins.EnhancedFire.Listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;

//subclass of HotBlocksListener, doesn't do anythign on it's own
public class HotCauldronManager {
	//what should happen if a cauldron boils. Only called on non-empty cauldrons
	static final List<Block> cookingCauldrons = new ArrayList<Block>();
	public static void handleBoilingCauldron(final Block c, final JavaPlugin plugin) {
		//spawn particles at different heights
		if (c.getData() == 1)
            c.getWorld().spawnParticle(Particle.WATER_SPLASH, c.getLocation().add(0.5, 0.7, 0.5), 10, 0.25, 0.0, 0.25);
        else if (c.getData() == 2)
        	c.getWorld().spawnParticle(Particle.WATER_SPLASH, c.getLocation().add(0.5, 0.9, 0.5), 10, 0.25, 0.0, 0.25);
        else
            c.getWorld().spawnParticle(Particle.WATER_SPLASH, c.getLocation().add(0.5, 1.1, 0.5), 10, 0.25, 0.0, 0.25);
		
		//if it is cooking, return
		if(cookingCauldrons.contains(c))
			return;
		
		//check for ingredients thrown in the cauldron
		for(Entity e : c.getWorld().getNearbyEntities(c.getLocation().add(0.5, 0.5, 0.5), 0.25, 0.25, 0.25)) {
			if(e instanceof Item) {
				final ItemStack stack = ((Item) e).getItemStack();
				if(isCookable(stack.getType())) {
					if(stack.getAmount() > 1) {
						stack.setAmount(stack.getAmount() - 1);
						((Item) e).setItemStack(stack);
					}
					else {
						e.remove();
					}
					cookingCauldrons.add(c);
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, (Runnable)new Runnable() {
						public void run() {
		                	ItemStack is = FireListener.getFurnaceRecipeResult(stack); //don't check for null because isCookable does that
		                	ejectItem(c,is);
		                	//change water 40% of the time
		                	if(Math.random() < 0.4) {
			                	changeCauldron(c);
		                	}
		                	cookingCauldrons.remove(c);
						}
					},(long) (20*2.5));
				}
			}
		}
	}
	public static void ejectItem(Block source, ItemStack item) {
		Block under = source.getLocation().add(0,-1,0).getBlock();
		if(source.getState() instanceof Hopper) {
			Hopper u = (Hopper) source.getState();
			u.getInventory().addItem(item);	
		}
		else if(under.getState() instanceof Hopper) {
			Hopper u = (Hopper) under.getState();
			u.getInventory().addItem(item);
		}
		else {
			source.getWorld().dropItemNaturally(source.getLocation().add(0.5,0.5,0.5), item);
		}
	}
	
	//changes a cauldron's water level based on config options
	private static void changeCauldron(Block c) {
		BlockState state = c.getState();
		if(ConfigManager.infiniteCauldrons)
			adjustCauldronInfinite(state);
		else
			adjustCauldron(state);
    	state.update();
	}
	
	//returns if a material is cookable by cauldrons
	private static boolean isCookable(Material m) {
		if(FireListener.getFurnaceRecipeResult(new ItemStack(m)) == null) //if it doesn't have a furnace recipe
			return false;
		if(ConfigManager.cookables != null) //if its in the config, return true
			if(ConfigManager.cookables.contains(m))
				return true;
		return false; //else return false
	}
	
	//adjusts the cauldron's water level in such a way that it never runs out
	@SuppressWarnings("deprecation")
	private static void adjustCauldronInfinite(BlockState state) {
		switch(state.getData().getData()) {
		case 1: {
			state.setData(new MaterialData(Material.CAULDRON,(byte) (state.getData().getData()+1)));
			break;
		}
		case 2: {
			if(Math.random() < 0.5)
				state.setData(new MaterialData(Material.CAULDRON,(byte) (state.getData().getData()-1)));
			else
				state.setData(new MaterialData(Material.CAULDRON,(byte) (state.getData().getData()+1)));
			break;
		}
		case 3: state.setData(new MaterialData(Material.CAULDRON,(byte) (state.getData().getData()-1))); break;
		}
	}
	//adjusts the cauldron's water level in such a way that it never runs out
	private static void adjustCauldron(BlockState state) {
		byte data = state.getData().getData();
		if(data > 0 && Math.random() < 0.4) //remove water (if there's water in it) with a 40% chance
			state.setData(new MaterialData(Material.CAULDRON, data--));
	}
}
