package me.dretax.SaveIt;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SaveItExpansions implements Listener {
	
	/*
	 * @Author: DreTaX
	 */
	protected int places = 0;
	protected int breaks = 0;
	protected int logins = 0;
	protected int quits = 0;
	protected Main plugin;

	
	public SaveItExpansions(Main instance)
	{
		this.plugin = instance;
	}

    @EventHandler(priority = EventPriority.LOW)
	public void onPlayerLoginEvent(PlayerLoginEvent e) {
		if (SaveItConfig.SaveOnLogin) {
			this.logins += 1;
			if (this.logins == (SaveItConfig.SaveOnLoginCount)) {
				plugin.WorldSaveDelayed();
				this.logins -= (SaveItConfig.SaveOnLoginCount);
				if (SaveItConfig.Debug) {
					sendConsoleMessage(ChatColor.GREEN + "Login limit reached, reseted!");
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerQuitEvent(PlayerQuitEvent e) {
		if (SaveItConfig.SaveOnQuit) {
			this.quits += 1;
			if (this.quits == (SaveItConfig.SaveOnQuitCount)) {
				plugin.WorldSaveDelayed();
				this.quits -= (SaveItConfig.SaveOnQuitCount);
				if (SaveItConfig.Debug) {
					sendConsoleMessage(ChatColor.GREEN + "Quit limit reached, reseted!");
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (SaveItConfig.SaveOnBlockPlace) {
			this.places += 1;
			if (this.places == (SaveItConfig.SaveOnBlockPlacecount)) {
				plugin.WorldSaveDelayed();
				this.places -= (SaveItConfig.SaveOnBlockPlacecount);
				if (SaveItConfig.Debug) {
					sendConsoleMessage(ChatColor.GREEN + "Place limit reached, reseted!");
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBlockBreak(BlockBreakEvent event) {
		if(SaveItConfig.SaveOnBlockBreak) {
			this.breaks += 1;
			if (this.breaks == (SaveItConfig.SaveOnBlockBreakcount)) {
				plugin.WorldSaveDelayed();
				this.breaks -= (SaveItConfig.SaveOnBlockBreakcount);
				if (SaveItConfig.Debug) {
					sendConsoleMessage(ChatColor.GREEN + "Break limit reached, reseted!");
				}
			}
		}
	}
	
	public void sendConsoleMessage(String msg) {
		// My Nice Colored Console Message Prefix.
		plugin._cs.sendMessage(plugin._prefix + ChatColor.AQUA + msg);
	}
}