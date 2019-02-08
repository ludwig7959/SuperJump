package com.tistory.hornslied.superjump;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {

	private SuperJump plugin;
	
	private HashMap<ConfigType, FileConfiguration> configs;
	
	public ConfigManager(SuperJump plugin) {
		this.plugin = plugin;
		
		configs = new HashMap<>();
		load();
	}
	
	private void load() {
		for(ConfigType type : ConfigType.values()) {
			if(!new File(plugin.getDataFolder(), type.getFileName()).exists())
				plugin.saveResource(type.getFileName(), false);
		
			configs.put(type, YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), type.getFileName())));
		}
	}
	
	public void reload() {
		load();
	}
	
	public FileConfiguration getConfig(ConfigType type) {
		return configs.get(type);
	}

	public void saveConfig(ConfigType type) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					configs.get(type).save(new File(plugin.getDataFolder(), type.getFileName()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public enum ConfigType {
		DEFAULT("config.yml");
		
		private String fileName;
		
		ConfigType(String fileName) {
			this.fileName = fileName;
		}
		
		public String getFileName() {
			return fileName;
		}
	}
}
