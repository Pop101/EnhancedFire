package com.SketchyPlugins.EnhancedFire;

import com.SketchyPlugins.EnhancedFire.Listeners.AshListener;
import com.SketchyPlugins.EnhancedFire.Listeners.ConfigManager;
import com.SketchyPlugins.EnhancedFire.Listeners.FireImmunityListener;
import com.SketchyPlugins.EnhancedFire.Listeners.FireListener;
import com.SketchyPlugins.EnhancedFire.Listeners.HotBlocksListener;
import com.SketchyPlugins.EnhancedFire.Listeners.ThrowableFireballsListener;

public class ListenerParent{
	Main plugin;
	
	AshListener ash;
	FireListener fire;
	HotBlocksListener hot;
	ThrowableFireballsListener balls;
	FireImmunityListener fil;
	public ListenerParent(Main main) {
		plugin = main;
		
		ash = new AshListener(plugin);
		fire = new FireListener(plugin);
		hot = new HotBlocksListener(plugin);
		if(ConfigManager.throwFireballs)
			balls = new ThrowableFireballsListener(plugin);
		fil = new FireImmunityListener(plugin);
	}
}
