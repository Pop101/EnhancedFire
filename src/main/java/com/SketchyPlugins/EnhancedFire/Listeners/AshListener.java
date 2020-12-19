package com.SketchyPlugins.EnhancedFire.Listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class AshListener  implements Listener {
	JavaPlugin plugin;
	static List<Block> ashBlocks;
	static List<FallingBlock> ashEnts;
	public AshListener(JavaPlugin main) {
		plugin = main;
		main.getServer().getPluginManager().registerEvents(this, main);
		
		ashBlocks = ConfigManager.loadAsh();
		if(ashBlocks == null)
			ashBlocks = new ArrayList<Block>();
		
		//remove all ash blocks that aren't concrete
		for(int i = ashBlocks.size()-1; i >= 0; i--) 
			if(ashBlocks.get(i).getType() != ConfigManager.ashMat)
				ashBlocks.remove(i);
		
		//save the blocks
		ConfigManager.saveAsh(ashBlocks);
		
		ashEnts = new ArrayList<FallingBlock>();
		
		saveLoop();
	}
	//called by other classes to create ash
	public static void createAsh(Block bl) {
		if(!ConfigManager.enableAsh) //if ash is not enabled, don't make it
			return;
		if(ConfigManager.ashType.getOrDefault(bl.getType(), null) == null) return;
		
		//Only create if chance matches
		if(Math.random() > ConfigManager.ashType.get(bl.getType()).chance) 
			return;
		
		int type = ConfigManager.ashType.get(bl.getType()).type;
		if(type == 1) { //block
			//only create ash if the class was properly initialized
			if(ashBlocks != null) {
				bl.setType(ConfigManager.ashMat, true);
				ashBlocks.add(bl);
			}
		}
		else if (type == 2) { //particles
			bl.getWorld().spawnParticle(Particle.SUSPENDED_DEPTH, bl.getLocation().add(0.5, 0.4, 0.5), 4, 0.4, 0.4, 0.4, 0.05);
			//drop flint with 10% chance
			if(Math.random() < ConfigManager.ashFlintchance)
				bl.getWorld().dropItemNaturally(bl.getLocation().add(0.5,0.5,0.5), new ItemStack(Material.FLINT));
			
			//drop exp
			while(Math.random() > ConfigManager.ashExpChance) {
				ExperienceOrb o = (ExperienceOrb) bl.getWorld().spawnEntity(bl.getLocation().add(Math.random(),1.1,Math.random()), EntityType.EXPERIENCE_ORB);
				o.setExperience((int)(Math.random()*4)+1);
			}
		}
		else if (type == 3) { //scorch
			Block below = bl.getLocation().add(0,-1,0).getBlock();
			Material toReplace = ConfigManager.scorchRecipes.getOrDefault(below.getType(), null);
			if(toReplace != null) below.setType(toReplace);
		}
	}
	
	//save all ash on server restart
	@EventHandler
	public void onDisable(PluginDisableEvent e) {
		//TODO: save this in a yaml file instead of deleting it all
		ConfigManager.saveAsh(ashBlocks);
		//for(Block bl : ashBlocks) 
		//	bl.setType(Material.AIR);
		for(FallingBlock ash : ashEnts)
			ash.remove();
	}
	void saveLoop() {
		ConfigManager.saveAsh(ashBlocks);
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, (Runnable)new Runnable() {
            public void run() {
            	saveLoop();
            }
        },20*30);
	}
	
	//catches falling sand spawning where a block would be
	@EventHandler
	public void ashFalling(EntitySpawnEvent e) {
		if(!(e.getEntity() instanceof FallingBlock))
			return;
		if(ashBlocks.contains(e.getLocation().getBlock())){
			ashEnts.add((FallingBlock) e.getEntity());
		}
	}

	//catches any ash entities landing
	@EventHandler
	public void ashLand(EntityChangeBlockEvent e) {
		if(!ashEnts.contains(e.getEntity()))
			return;
		//now we know it's falling sand, so we can remove it and keep the block
		ashBlocks.add(e.getBlock());
	}
	//when ash/concrete powder hardens, prevent it
	@EventHandler
	public void ashHarden(BlockFormEvent e) {
		if(ashBlocks.contains(e.getBlock()))
			e.setCancelled(true);
	}
	
	//when ash is destroyed or exploded, make sure it breaks it
	@EventHandler
	public void ashDestroy(BlockBreakEvent e) {
		//attempt ash break. if it was an ash block, manually damage the player's held item
		if(tryAshBreak(e.getBlock())) {
			ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();
			
			//respect unbreaking
			if(Math.random() <= 1.0/hand.getEnchantmentLevel(Enchantment.DURABILITY))
				damageItem(hand, 1);
		}
	}
	
	@EventHandler
	public void ashExplode(BlockExplodeEvent e) {
		tryAshBreak(e.getBlock());
	}
	
	//ash can't be pushed
	@EventHandler
	public void ashPush(BlockPistonExtendEvent e) {
		if(e.getBlocks() == null)
			return;
		for(Block b : ashBlocks) 
			if(e.getBlocks().contains(b)) {
				e.setCancelled(true);
				return;
			}
	}
	
	//ash can't be pulled
	@EventHandler
	public void ashPull(BlockPistonRetractEvent e) {
		if(e.getBlocks() == null)
			return;
		for(Block b : ashBlocks) 
			if(e.getBlocks().contains(b)) {
				e.setCancelled(true);
				return;
			}
	}
	
	//when ash is mined, give nothing, except for a small chance at flint
	private boolean tryAshBreak(Block b) {
		if(b == null)
			return false;
		if(!ashBlocks.contains(b))
			return false;
		
		b.setType(Material.AIR);		
		
		//drop flint with 10% chance
		if(Math.random() < ConfigManager.ashFlintchance)
			b.getWorld().dropItemNaturally(b.getLocation().add(0.5, 0.5, 0.5), new ItemStack(Material.FLINT));
		
		//drop exp
		while(Math.random() > ConfigManager.ashExpChance) {
			ExperienceOrb o = (ExperienceOrb) b.getWorld().spawnEntity(b.getLocation().add(Math.random(),1.1,Math.random()), EntityType.EXPERIENCE_ORB);
			o.setExperience((int)(Math.random()*4)+1);
		}
		ashBlocks.remove(b);
		
		return true;
	}
	
	//damages the item stack
	public void damageItem(ItemStack tool, int toDamage) {
		if(!(tool.getItemMeta() instanceof Damageable)) return;
		
        Damageable itemdmg = (Damageable) tool.getItemMeta();
        int damage = itemdmg.getDamage() + toDamage;
        itemdmg.setDamage((short) damage);
        tool.setItemMeta((ItemMeta) itemdmg);
     
        if(itemdmg.getDamage() <= 0) {
        	tool.setType(Material.AIR);
        }
    }
}
