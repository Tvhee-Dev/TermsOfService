package me.tvhee.termsofservice.listener;

import me.tvhee.termsofservice.TermsOfServiceInventory;
import me.tvhee.termsofservice.TermsOfServicePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerListener implements Listener
{
	private final TermsOfServicePlugin plugin;

	public PlayerListener(TermsOfServicePlugin plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		Player player = e.getPlayer();

		if(!plugin.acceptedTOS.contains(e.getPlayer().getUniqueId()))
		{
			new TermsOfServiceInventory(plugin, player).getMainMenu().open();

			Bukkit.getScheduler().runTaskLater(plugin, () ->
			{
				if(!plugin.acceptedTOS.contains(player.getUniqueId()) && player.isOnline())
					player.kickPlayer(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("plugin.tos-menu.deny-item.message")));
			}, 1200L * plugin.getConfig().getInt("plugin.minutes-to-accept"));
		}
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e)
	{
		if(!plugin.acceptedTOS.contains(e.getPlayer().getUniqueId()))
			e.getRecipients().clear();

		e.getRecipients().removeIf(recipient -> !plugin.acceptedTOS.contains(recipient.getUniqueId()));
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e)
	{
		if(!plugin.acceptedTOS.contains(e.getPlayer().getUniqueId()))
			e.setCancelled(true);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e)
	{
		if(!plugin.acceptedTOS.contains(e.getPlayer().getUniqueId()))
			e.setCancelled(true);
	}
}
