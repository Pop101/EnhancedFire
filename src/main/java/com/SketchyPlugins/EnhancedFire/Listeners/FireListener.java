package com.SketchyPlugins.EnhancedFire.Listeners;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;

public class FireListener  implements Listener {
	final JavaPlugin plugin;
	public FireListener(JavaPlugin main) {
		plugin = main;
		main.getServer().getPluginManager().registerEvents(this, main);
		
		burningBlocks = new HashMap<Block, Integer>();	
	}
	
	protected Map<Block, Integer> burningBlocks; //tracks
	
	@EventHandler
	public void blockLightedHandler(BlockIgniteEvent e) {
		if(ConfigManager.burnLength.getOrDefault(e.getBlock().getType(),1) > 1)
			burningBlocks.put(e.getBlock(), ConfigManager.burnLength.getOrDefault(e.getBlock().getType(),1)-1);
	}
	
	@EventHandler
	public void blockPlaceEvent(BlockPlaceEvent e) { //special-case for when a player places fire
		if(e.getBlock().getType() == Material.FIRE)
			if(ConfigManager.burnLength.getOrDefault(e.getBlockAgainst().getType(),1) > 1)
				burningBlocks.put(e.getBlockAgainst(), ConfigManager.burnLength.getOrDefault(e.getBlockAgainst().getType(),1)-1);
	}
	
	//if block is broken, remove it
	@EventHandler
	public void blockBreakRemover(BlockBreakEvent e) {
		if(burningBlocks.containsKey(e.getBlock()))
			burningBlocks.remove(e.getBlock());
	}
	
	//when a block burns down, catch it if it's supposed to be caught
	@EventHandler
	public void blockBurnHandler(BlockBurnEvent e) {
		if(burningBlocks.containsKey(e.getBlock())) {
			if(burningBlocks.get(e.getBlock()) > 0) { //if it still has lifespan left
				burningBlocks.put(e.getBlock(), burningBlocks.get(e.getBlock())-1); //remove 1 lifespan
				e.getBlock().getWorld().spawnParticle(Particle.FLAME, e.getBlock().getLocation().add(0.5,1.1,0.5), 8, 0.25, 0.1, 0.25, 0.1);
				e.setCancelled(true); //cancel and return
				return;
			}
			else { //otherwise kill the block
				burningBlocks.remove(e.getBlock());
			}
		}
		
		//drop furnace results
		if(ConfigManager.burncook) {
			ItemStack toDrop = getFurnaceRecipeResult(new ItemStack(e.getBlock().getType()));
			if(toDrop != null)
				e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), toDrop);
		}
		
		//set block to ash, to be tracked by the AshListener
		Material org = e.getBlock().getType();
		AshListener.createAsh(e.getBlock());
		if(org != e.getBlock().getType()) {
			e.setCancelled(true);
			return;
		}
	}
	
	public static ItemStack getFurnaceRecipeResult(ItemStack in) {
		//loop through all recipes
		Iterator<Recipe> recIter = Bukkit.recipeIterator();
		while(recIter.hasNext()) {
			Recipe rec = recIter.next();
			//check if it's a furnace recipe
			if(rec instanceof FurnaceRecipe) {
				FurnaceRecipe frec = (FurnaceRecipe) rec;
				//if it's input is similar to the given item stack
				if(frec.getInput().isSimilar(in))
					return frec.getResult(); //return result
			}
		}
		//otherwise return a stack of air (null basically)
		return null;
	}
}
