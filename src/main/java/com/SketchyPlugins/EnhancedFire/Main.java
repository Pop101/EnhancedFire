package com.SketchyPlugins.EnhancedFire;

import org.bukkit.plugin.java.JavaPlugin;

import com.SketchyPlugins.EnhancedFire.Listeners.ConfigManager;

public final class Main extends JavaPlugin{
	public static void main (String[] args) {
		
	}
	ListenerParent listeners;
	@Override
	public void onEnable() {
		getLogger().info("[EnhancedFire] Enabling");
		ConfigManager.init();
		listeners = new ListenerParent(this);
	}
}