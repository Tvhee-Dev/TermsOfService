package me.tvhee.termsofservice;

import me.tvhee.termsofservice.listener.InventoryListener;
import me.tvhee.termsofservice.listener.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TermsOfServicePlugin extends JavaPlugin
{
	public final List<UUID> acceptedTOS = new ArrayList<>();

	@Override
	public void onEnable()
	{
		saveDefaultConfig();

		if(getConfig().contains("storage.accepted-tos"))
		{
			for(String uuid : getConfig().getStringList("storage.accepted-tos"))
				acceptedTOS.add(UUID.fromString(uuid));

			getConfig().set("storage.accepted-tos", null);
			saveConfig();
		}

		getServer().getPluginManager().registerEvents(new InventoryListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

		getLogger().info("has been enabled!");
		getLogger().info("made by tvhee");
	}

	@Override
	public void onDisable()
	{
		List<String> uuids = new ArrayList<>();

		for(UUID uuid : acceptedTOS)
			uuids.add(uuid.toString());

		getConfig().set("storage.accepted-tos", uuids);
		saveConfig();

		getLogger().info("made by tvhee");
		getLogger().info("has been disabled!");
	}
}
