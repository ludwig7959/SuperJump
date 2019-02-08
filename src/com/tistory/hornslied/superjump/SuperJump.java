package com.tistory.hornslied.superjump;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.tistory.hornslied.superjump.ConfigManager.ConfigType;

public class SuperJump extends JavaPlugin {

	private static SuperJump instance;

	public static SuperJump getInstance() {
		return instance;
	}
	
	private ConfigManager configManager;
	private SuperJumpManager superJumpManager;
	
	public ConfigManager getConfigManager() {
		return configManager;
	}
	
	public SuperJumpManager getSuperJumpManager() {
		return superJumpManager;
	}
	
	private String enableMessage;
	private String disableMessage;
	
	@Override
	public void onEnable() {
		instance = this;
		
		configManager = new ConfigManager(this);
		superJumpManager = new SuperJumpManager(this);
		
		FileConfiguration config = configManager.getConfig(ConfigType.DEFAULT);

		enableMessage = ChatColor.translateAlternateColorCodes('&', config.getString("Messages.Enabled"));
		disableMessage = ChatColor.translateAlternateColorCodes('&', config.getString("Messages.Disabled"));
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length < 1) {
			sender.sendMessage(ChatColor.RED + "/sj [toggle/reload]");
			return false;
		}
		
		switch(args[0].toLowerCase()) {
		case "reload":
			if(!sender.hasPermission("superjump.reload")) {
				sender.sendMessage(ChatColor.RED + "You don't have permission to perform this command.");
				break;
			}
			
			sender.sendMessage(ChatColor.GREEN + "Reloading...");
			configManager.reload();
			sender.sendMessage(ChatColor.GREEN + "Reload Compeleted.");
			break;
		case "toggle":
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Only can players perform this command.");
				break;
			}
			
			Player player = (Player) sender;
			
			if(!sender.hasPermission("superjump.toggle")) {
				sender.sendMessage(ChatColor.RED + "You don't have permission to perform this command.");
				break;
			}
			
			if(superJumpManager.isSuperJumpEnabled(player)) {
				superJumpManager.disableSuperJump(player);
				player.sendMessage(disableMessage);
			} else {
				superJumpManager.enableSuperJump(player);
				player.sendMessage(enableMessage);
			}
			break;
		}
		return true;
	}
}
