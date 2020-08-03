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
		
		burnDictionary = new HashMap<Material, Integer>();
		burningBlocks = new HashMap<Block, Integer>();	
		
		//populate ideal burn times
		for(Material mat : ConfigManager.ultraBurn)
			burnDictionary.put(mat, 5);
		
		for(Material mat : ConfigManager.longerBurn)
			burnDictionary.put(mat, 2);
		
		for(Material mat : ConfigManager.longBurn)
			burnDictionary.put(mat, 1);
	}
	protected Map<Material, Integer> burnDictionary; //tracks ideal burn times
	protected Map<Block, Integer> burningBlocks; //tracks
	
	@EventHandler
	public void blockLightedHandler(BlockIgniteEvent e) {
		burningBlocks.put(e.getBlock(), burnDictionary.getOrDefault(e.getBlock().getType(),0));
	}
	@EventHandler
	public void blockPlaceEvent(BlockPlaceEvent e) { //special-case for when a player places fire
		if(e.getBlock().getType() == Material.FIRE)
			burningBlocks.put(e.getBlockAgainst(), burnDictionary.getOrDefault(e.getBlockAgainst().getType(),0));
	}
	//if block is broken, remove it
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
		ItemStack toDrop = getFurnaceRecipeResult(new ItemStack(e.getBlock().getType()));
		if(toDrop != null)
			e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), toDrop);
		
		//set block to ash, to be tracked by the AshListener
		AshListener.addAshBlock(e.getBlock());
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
class BlockBurnInfo{
	protected Material mat;
	protected int burnCatches;
	
	public BlockBurnInfo(Material _mat, int lifetime) {
		mat = _mat;
		burnCatches = lifetime;
	}
}
