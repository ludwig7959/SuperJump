package com.tistory.hornslied.superjump;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.tistory.hornslied.superjump.ConfigManager.ConfigType;

public class SuperJumpManager implements Listener {
	
	private SuperJump plugin;
	
	private Jump defaultJump;
	private boolean removeFallDamage;
	
	private Map<Player, Integer> jumpedTimes;
	private Map<Integer, MultipleJump> multipleJumps;
	private Set<Player> offPlayers;

	public SuperJumpManager(SuperJump plugin) {
		this.plugin = plugin;
		
		jumpedTimes = new HashMap<>();
		multipleJumps = new HashMap<>();
		offPlayers = new HashSet<>();
		
		load();
		
		Bukkit.getPluginManager().registerEvents(this, plugin);
		
		new BukkitRunnable() {

			@Override
			public void run() {
				for(Player player : Bukkit.getOnlinePlayers()) {
					if(player.isOnGround()) {
						player.setAllowFlight(true);
						jumpedTimes.remove(player);
					}
				}
			}
			
		}.runTaskTimer(SuperJump.getInstance(), 1, 1);
	}
	
	private void load() {
		FileConfiguration config = plugin.getConfigManager().getConfig(ConfigType.DEFAULT);
		
		removeFallDamage = config.getBoolean("RemoveFallDamage");
		defaultJump = getJump(config.getConfigurationSection("Defaultjump"));
		
		if(config.getConfigurationSection("superjump_infinite") != null)
			multipleJumps.put(Integer.MAX_VALUE, getMultipleJump(config.getConfigurationSection("superjump_infinite"), Integer.MAX_VALUE));
		
		for(int i=1; i <= 10; i++) {
			if(config.getConfigurationSection("superjump_" + i) != null)
				multipleJumps.put(i, getMultipleJump(config.getConfigurationSection("superjump_" + i), i));
		}
	}
	
	private Jump getJump(ConfigurationSection section) {
		String[] soundInfo = section.getString("sound").split(":");
		
		return new Jump(Sound.valueOf(soundInfo[0]), Float.parseFloat(soundInfo[1]), Float.parseFloat(soundInfo[2]), (float) section.getDouble("angle"), section.getDouble("power"));
	}
	
	private MultipleJump getMultipleJump(ConfigurationSection section, int limit) {
		MultipleJump mj = new MultipleJump(limit);
		
		Set<String> keys = section.getKeys(false);
		
		if(keys != null) {
			for(String key : keys) {
				mj.addJump(Integer.parseInt(key), getJump(section.getConfigurationSection(key)));
			}
		}
		
		return mj;
	}
	
	public void reload() {
		HandlerList.unregisterAll(this);
		
		jumpedTimes.clear();
		multipleJumps.clear();
		offPlayers.clear();
		load();
		
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void detectSuperJump(PlayerToggleFlightEvent event) {
		Player player = event.getPlayer();
		
		if(player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)
			return;
		
		event.setCancelled(true);
		
		if(offPlayers.contains(player))
			return;
		
		Jump jump = null;
		
		int jumpedTime = 0;
		
		if(jumpedTimes.containsKey(player))
			jumpedTime = jumpedTimes.get(player);
		
		if(multipleJumps.containsKey(getJumpLimit(player))) {
			MultipleJump multipleJump = multipleJumps.get(getJumpLimit(player));
			
			if(multipleJump.hasJump(jumpedTime + 1))
				jump = multipleJump.getJump(jumpedTime + 1);
			else
				jump = defaultJump;
		} else {
			jump = defaultJump;
		}
		
		Location loc = player.getLocation();
		loc.setPitch(-jump.angle);
		player.setVelocity(loc.getDirection().multiply(jump.power));
		player.getWorld().playSound(loc, jump.sound, jump.volume, jump.pitch);
		jumpedTimes.put(player, jumpedTime + 1);
		
		if(jumpedTimes.get(player) >= getJumpLimit(player))
			player.setAllowFlight(false);
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		
		player.setAllowFlight(true);
		player.setFlying(false);
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		jumpedTimes.remove(event.getPlayer());
		offPlayers.remove(event.getPlayer());
	}
	
	@EventHandler
	public void onFall(EntityDamageEvent event) {
		if(!(event.getEntity() instanceof Player))
			return;
		
		if(!event.getCause().equals(DamageCause.FALL))
			return;
		
		Player player = (Player) event.getEntity();
		
		if(removeFallDamage && jumpedTimes.containsKey(player))
			event.setCancelled(true);
	}
	
	public int getJumpLimit(Player player) {
		if(player.hasPermission("superjump.infinite"))
			return Integer.MAX_VALUE;
		
		for(int i = 10 ; i > 0; i--) {
			if(player.hasPermission("superjump." + i))
				return i;
		}
		
		return 0;
	}
	
	public boolean isSuperJumpEnabled(Player player) {
		return !offPlayers.contains(player);
	}
	
	public void disableSuperJump(Player player) {
		offPlayers.add(player);
	}
	
	public void enableSuperJump(Player player) {
		offPlayers.remove(player);
	}
}
