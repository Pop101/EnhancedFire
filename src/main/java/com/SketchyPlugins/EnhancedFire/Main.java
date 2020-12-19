package com.SketchyPlugins.EnhancedFire;

import org.bukkit.plugin.java.JavaPlugin;

import com.SketchyPlugins.EnhancedFire.Listeners.ConfigManager;

public final class Main extends JavaPlugin{
	ListenerParent listeners;
	@Override
	public void onEnable() {
		ConfigManager.init(this);
		listeners = new ListenerParent(this);
	}
}
