package me.dretax.SaveIt;


public class SaveItAccessor {
	protected static int places = 0;
	protected static int breaks = 0;
	protected static Main plugin;

	public SaveItAccessor(Main instance)
	{
		plugin = instance;
	}

}