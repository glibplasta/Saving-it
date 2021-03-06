package me.dretax.SaveIt.tasks;

import me.dretax.SaveIt.Main;

public class StartIntervalBackup implements Runnable {

	@Override
	public void run() {
		Main main = Main.getInstance();
		if (main.getSaveItConfig().PowerSave) {
			int players = main.getServer().getOnlinePlayers().size();
			if (players != 0) {
				main.getSaveItBackup().backupdir();
			}
		} else {
			main.getSaveItBackup().backupdir();
		}
	}
}
