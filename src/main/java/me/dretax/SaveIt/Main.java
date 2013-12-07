package me.dretax.SaveIt;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import me.dretax.SaveIt.metrics.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	/*
	 *
	 * @Author: DreTaX
	 *
	 */
	private int Delay, Delay2;
	protected PluginManager _pm;
	protected ConsoleCommandSender _cs;
	protected String _prefix = ChatColor.AQUA + "[SaveIt] ";
	private SaveItConfig SaveItConfig = new SaveItConfig(this);
	private SaveItExpansions expansions = new SaveItExpansions(this, SaveItConfig);
	private BackUp backup = new BackUp(this, SaveItConfig);
	protected FileConfiguration config;
	private boolean update = false;

	public void onDisable() {
		super.onDisable();
	}

	public void onEnable() {
		config = getConfig();
		SaveItConfig.create();
		backup.check();
		backup.kcheck();
		backup.delZip();
		this._pm = getServer().getPluginManager();
		_cs = getServer().getConsoleSender();
		if (SaveItConfig.EnableBackup) {
			if (SaveItConfig.AutoBackup) {
				if ((SaveItConfig.Decide).equalsIgnoreCase("INTERVAL")) {
					long t = (long) (72000 * SaveItConfig.intv);
					if (t > 0) {
						long delay = SaveItConfig.StartOnAGivenHour != null ? s(SaveItConfig.StartOnAGivenHour) : t;
						getServer().getScheduler().runTaskTimer(this, new Runnable() {
							@Override
							public void run() {
								if (SaveItConfig.PowerSave) {
									int players = getServer().getOnlinePlayers().length;
									if (players != 0) {
										backup.backupdir();
									}
								} else {
									backup.backupdir();
								}
							}
						}
						, delay, t);
					}
				}
				else if ((SaveItConfig.Decide).equalsIgnoreCase("DAY")) {
					getServer().getScheduler().runTaskTimer(this, new Runnable() {
						@Override
						public void run() {
							backup.kcheck();
						}
					}
					, 1200L * 60, 1200L * 60);
				}
			}
			if (SaveItConfig.PurgeBackups) {
				getServer().getScheduler().runTaskTimer(this, new Runnable() {
					@Override
					public void run() {
						backup.delZip();
					}
				}
				, 1200L * 60, 1200L * 60);
			}
		}
		/*
		 * Metrics
		 */
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
			if (SaveItConfig.Debug) sendConsoleMessage(ChatColor.GREEN + "SaveIt Metrics Successfully Enabled!");
		}
		// Couldn't Connect.
		catch (IOException localIOException) {
			if (SaveItConfig.Debug) sendConsoleMessage(ChatColor.RED + "SaveIt Metrics Failed to boot! Notify DreTaX!");
		}
		getCommand("saveit").setExecutor(this);

		/*
		 * Delay
		 */

		Delay = config.getInt("DelayInMinutes");

		getServer().getScheduler().runTaskTimer(this, new Runnable() {
			public void run() {
				WorldSaveDelayed();
			}
		}
		, 1200L * Delay, 1200L * Delay);
		/*
		 * Others
		 */

		if (SaveItConfig.DisableDefaultWorldSave) {
			FileConfiguration bukkit = YamlConfiguration.loadConfiguration(new File(getServer().getWorldContainer(), "bukkit.yml"));
			File fb = new File("bukkit.yml");
			bukkit.set("ticks-per.autosave", 0);
			try {
				bukkit.save(fb);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (SaveItConfig.CheckForUpdates) {
			sendConsoleMessage(ChatColor.GREEN + "Checking for updates.....");
			SaveItUpdate saveItUpdate = new SaveItUpdate(this, 33841, this.getFile(), SaveItUpdate.UpdateType.NO_DOWNLOAD, false);
			update = saveItUpdate.getResult() == SaveItUpdate.UpdateResult.UPDATE_AVAILABLE;
			if (update) {
				sendConsoleMessage(ChatColor.GREEN + "New Update Available! Version: " + ChatColor.RED + saveItUpdate.getLatestName());
				sendConsoleMessage(ChatColor.GREEN + "Your Version: " + _pm.getPlugin("SaveIt").getDescription().getVersion());
				sendConsoleMessage(ChatColor.GREEN + "Download at: http://dev.bukkit.org/bukkit-plugins/automatically-world-saving/");
				sendConsoleMessage(ChatColor.GREEN + "Or simply type /saveit update to update it automatically");
			} else {
				sendConsoleMessage(ChatColor.GREEN + "No updates available! You are cool :D!");
			}
		}

		if (SaveItConfig.SaveOnLogin || SaveItConfig.SaveOnQuit || SaveItConfig.SaveOnBlockBreak || SaveItConfig.SaveOnBlockPlace) {
			_pm.registerEvents(this.expansions, this);
		}
		sendConsoleMessage(ChatColor.GREEN + "Successfully Enabled!");
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("save")) {
				if (sender.hasPermission("saveit.save")) {
					WorldSaveDelayed();
				} else sender.sendMessage(_prefix + ChatColor.RED + "You Don't Have Permission to do this!");
			}
			else if (args[0].equalsIgnoreCase("reload")) {
				if (sender.hasPermission("saveit.reload")) {
					ConfigReload();
					sender.sendMessage(_prefix + ChatColor.GREEN + "Config Reloaded! Check Console for Errors, If Config doesn't Work");
				} else sender.sendMessage(_prefix + ChatColor.RED + "You Don't Have Permission to do this!");
			}
			else if (args[0].equalsIgnoreCase("selfsave")) {
				if (SaveItConfig.SelfInventorySave) {
					if (sender.hasPermission("saveit.selfsave")) {
						if (!(sender instanceof Player)) {
							sender.sendMessage(_prefix + ChatColor.GREEN + "This command can only be run by a player.");
						} else {
							((Player) sender).saveData();
							sender.sendMessage(_prefix + ChatColor.GREEN + "Your Inventory has been Saved!");
						}
					} else sender.sendMessage(_prefix + ChatColor.RED + "You Don't Have Permission to do this!");
				} else sender.sendMessage(_prefix + ChatColor.RED + "This Option isn't Enabled!");
			}
			else if (args[0].equalsIgnoreCase("add")) {
				if (sender.hasPermission("saveit.manage")) {
					if (args.length == 2) {
						if (SaveItConfig.SaveAllWorlds) {
							sender.sendMessage(_prefix + ChatColor.GREEN + "You are Saving all Existing Worlds.");
							sender.sendMessage(_prefix + ChatColor.GREEN + "You don't need this.");
							return false;
						}
						config = getConfig();
						SaveItConfig.load();
						if (!SaveItConfig.ExWorlds.contains(args[1])) {
							SaveItConfig.ExWorlds.add(args[1]);
							config.set("Worlds", SaveItConfig.ExWorlds);
							try {
								config.save(SaveItConfig.configFile);
							} catch (Exception e) {
								e.printStackTrace();
							}
							ConfigReload();
							sender.sendMessage(_prefix + ChatColor.GREEN + "Added World: " + args[1]);
						} else {
							sender.sendMessage(_prefix + ChatColor.RED + "World Already Exists in config: " + args[1]);
						}
					} else sender.sendMessage(_prefix + ChatColor.RED + "Specify a World Name!");
				} else sender.sendMessage(_prefix + ChatColor.RED + "You Don't Have Permission to do this!");
			}
			else if (args[0].equalsIgnoreCase("remove")) {
				if (sender.hasPermission("saveit.manage")) {
					if (args.length == 2) {
						if (SaveItConfig.SaveAllWorlds) {
							sender.sendMessage(_prefix + ChatColor.GREEN + "You are Saving all Existing Worlds.");
							sender.sendMessage(_prefix + ChatColor.GREEN + "You don't need this.");
							return false;
						}
						config = getConfig();
						SaveItConfig.load();
						if (SaveItConfig.ExWorlds.contains(args[1])) {
							SaveItConfig.ExWorlds.remove(args[1]);
							config.set("Worlds", SaveItConfig.ExWorlds);
							try {
								config.save(SaveItConfig.configFile);
							} catch (Exception e) {
								e.printStackTrace();
							}
							ConfigReload();
							sender.sendMessage(_prefix + ChatColor.GREEN + "Removed World: " + args[1]);
						} else {
							sender.sendMessage(_prefix + ChatColor.RED + "World Doesn't Exist in config: " + args[1]);
						}
					} else sender.sendMessage(_prefix + ChatColor.RED + "Specify a World Name!");
				} else sender.sendMessage(_prefix + ChatColor.RED + "You Don't Have Permission to do this!");
			}
			else if (args[0].equalsIgnoreCase("list")) {
				if (sender.hasPermission("saveit.manage")) {
					if (!SaveItConfig.SaveAllWorlds) {
						config = getConfig();
						SaveItConfig.load();
						sender.sendMessage(_prefix + ChatColor.GREEN + SaveItConfig.ExWorlds);
					} else {
						sender.sendMessage(_prefix + ChatColor.GREEN + "You are Saving all Existing Worlds.");
						sender.sendMessage(_prefix + ChatColor.GREEN + "You don't need the list.");
					}
				} else sender.sendMessage(_prefix + ChatColor.RED + "You Don't Have Permission to do this!");
			}
			else if (args[0].equalsIgnoreCase("backup")) {
				if (sender.hasPermission("saveit.backup")) {
					if (SaveItConfig.EnableBackup) {
						backup.delZip();
						sender.sendMessage(_prefix + ChatColor.GREEN + "StandBy, backup starts in 5 seconds...");
						getServer().getScheduler().runTaskLater(this, new Runnable() {
							public void run() {
								backup.backupdir();
							}
						}
						, 20L * 5);
					} else sender.sendMessage(_prefix + ChatColor.RED + "Backup Mode isn't Enabled!");

				} else sender.sendMessage(_prefix + ChatColor.RED + "You Don't Have Permission to do this!");
			}
			else if (args[0].equalsIgnoreCase("update")) {
				if (sender.hasPermission("saveit.manage")) {
					sender.sendMessage(_prefix + ChatColor.GREEN + "Updating...");
					SaveItUpdate saveItUpdate = new SaveItUpdate(this, 33841, this.getFile(), SaveItUpdate.UpdateType.NO_DOWNLOAD, false);
					update = saveItUpdate.getResult() == SaveItUpdate.UpdateResult.UPDATE_AVAILABLE;
					if (update) {
						SaveItUpdate saveItUpdate2 = new SaveItUpdate(this, 33841, this.getFile(), SaveItUpdate.UpdateType.NO_VERSION_CHECK, true);
						update = saveItUpdate2.getResult() == SaveItUpdate.UpdateResult.SUCCESS;
						if (update) {
							sender.sendMessage(_prefix + ChatColor.GREEN + "Success! Restart or Reload to make changes!");
						} else {
							sender.sendMessage(_prefix + ChatColor.RED + "Update failed, check console!");
						}
					}
					else {
						sender.sendMessage(_prefix + ChatColor.RED + "You already have the latest version!");
					}
				}
			}
		} else {
			sender.sendMessage(_prefix + ChatColor.GREEN + "1.1.5 " + ChatColor.AQUA + "===Commands:===");
			sender.sendMessage(ChatColor.BLUE + "/saveit save" + ChatColor.GREEN + " - Saves All the Configured Worlds, and Inventories" + ChatColor.YELLOW + "(FULLSAVE)");
			sender.sendMessage(ChatColor.BLUE + "/saveit reload" + ChatColor.GREEN + " - Reloads Config");
			sender.sendMessage(ChatColor.BLUE + "/saveit selfsave" + ChatColor.GREEN + " - Saves Your Data Only");
			sender.sendMessage(ChatColor.BLUE + "/saveit add " + ChatColor.YELLOW + "WORLDNAME (Case Sensitive)" + ChatColor.GREEN + " - Adds a Given World to Config");
			sender.sendMessage(ChatColor.BLUE + "/saveit remove " + ChatColor.YELLOW + "WORLDNAME (Case Sensitive)" + ChatColor.GREEN + " - Removes a Given World from Config");
			sender.sendMessage(ChatColor.BLUE + "/saveit list" + ChatColor.GREEN + " - Lists Current Worlds in Config");
			sender.sendMessage(ChatColor.BLUE + "/saveit backup" + ChatColor.GREEN + " - Creates a Zip of all your Server Folders");
			sender.sendMessage(ChatColor.BLUE + "/saveit update" + ChatColor.GREEN + " - Download the Latest Update");
		}
		return false;

	}

	protected void WorldSaveDelayed() {
		if (SaveItConfig.PowerSave) {
			int players = getServer().getOnlinePlayers().length;
			if (players == 0) {
				return;
			}
		}
		Delay2 = 1;
		// Checking on "EnableSaveMSG".
		if (SaveItConfig.EnableMsg) getServer().broadcastMessage(colorize(config.getString("SaveMSG")));

		/** Full Save On Players, if Enabled
		 * If not, It will only Save Players in
		 * The Configured Worlds
		 */
		if (SaveItConfig.SavePlayersFully) {
			getServer().getScheduler().runTaskLater(this, new Runnable() {
				@Override
				public void run() {
					Delay2 += 1;
					getServer().savePlayers();
				}
			}
			, 20L * Delay2);
		}
		if (!SaveItConfig.SaveAllWorlds) {
			// Checking if an Existing World is written in the Config
			for (final String worldname : SaveItConfig.ExWorlds) {
				if (getServer().getWorld(worldname) == null) {
					sendConsoleMessage(ChatColor.RED + "[ERROR] Not Existing World in Config!");
					sendConsoleMessage(ChatColor.RED + "[ERROR] " + ChatColor.BLUE + worldname + ChatColor.RED + " does not exist! Remove it from the config!");
					if (SaveItConfig.BroadCastErrorIg) {
						getServer().broadcastMessage(_prefix + ChatColor.RED + "[ERROR] Not Existing World In Config!");
						getServer().broadcastMessage(_prefix + ChatColor.RED + "[ERROR] " + ChatColor.BLUE + worldname + ChatColor.RED + " does not exist! Remove it from the config!");
					}
					return;
				}
				getServer().getScheduler().runTaskLater(this, new Runnable() {
					@Override
					public void run() {
						Delay2 += 1;
						getServer().getWorld(worldname).save();
						// Getting All The Players, and Saving Them, only in the Configured Worlds.
						if (!SaveItConfig.SavePlayersFully) {
							for (Player player : getServer().getWorld(worldname).getPlayers()) {
								player.saveData();
							}
						}
					}
				}
				, 20L * Delay2);
			}
		}
	   /** If SaveAllWorlds is true
		* We will Save all the worlds instead of the configured one
		* Also Calling Performance Method here
		*/
		else {
			// Getting Worlds, and Saving Them.
			for (final World world : getServer().getWorlds()) {
				getServer().getScheduler().runTaskLater(this, new Runnable() {
					@Override
					public void run() {
						Delay2 += 1;
						world.save();
						// Getting All The Players, and Saving Them, only in the Configured Worlds.
						if (!SaveItConfig.SavePlayersFully) {
							for (Player player : world.getPlayers()) {
								player.saveData();
							}
						}
					}
				}
				, 20L * Delay2);
			}
		}

		if (SaveItConfig.EnableMsg) getServer().broadcastMessage(colorize(config.getString("SaveMSG2")));
	}

	private void sendConsoleMessage(String msg) {
		// My Nice Colored Console Message Prefix.
		_cs.sendMessage(_prefix + ChatColor.GREEN + msg);
	}

	private String colorize(String s) {
		// This little code supports coloring.
		// If String is null it will return null
		if (s == null) return null;
		return ChatColor.translateAlternateColorCodes('&', s);
	}

	private void ConfigReload() {
		config = getConfig();
		SaveItConfig.load();
		Delay = config.getInt("DelayInMinutes");
		if (SaveItConfig.Debug) sendConsoleMessage(ChatColor.GREEN + "Config Reloaded!");
	}

	@SuppressWarnings("unused")
	private long s(double s) {
		double n = h(new Date());
		double d = n - SaveItConfig.StartOnAGivenHour;
		if (d < 0) {
			d += 24;
		}
		double ip = d - Math.floor(d / SaveItConfig.intv) * SaveItConfig.intv;
		double r = SaveItConfig.intv - ip;
		return (long) (r * 72000);
	}

	@SuppressWarnings("deprecation")
	protected double h(Date t) {
		return t.getHours() + t.getMinutes() / 60. + t.getSeconds() / 3600.;
	}
}
