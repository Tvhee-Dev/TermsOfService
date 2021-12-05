package me.tvhee.termsofservice;

import java.util.List;
import me.tvhee.termsofservice.inventory.Inventory;
import me.tvhee.termsofservice.inventory.InventoryBuilder;
import me.tvhee.termsofservice.inventory.InventoryClickEvent;
import me.tvhee.termsofservice.inventory.InventoryClickHandler;
import me.tvhee.termsofservice.inventory.PaginatedInventory;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import java.util.ArrayList;

public class TermsOfServiceInventory implements InventoryClickHandler
{
	private final TermsOfServicePlugin plugin;
	private final Player player;

	public TermsOfServiceInventory(TermsOfServicePlugin plugin, Player player)
	{
		this.plugin = plugin;
		this.player = player;
	}

	public Inventory getMainMenu()
	{
		FileConfiguration config = plugin.getConfig();
		Inventory inventory = new InventoryBuilder(plugin, ChatColor.translateAlternateColorCodes('&', config.getString("plugin.tos-menu.name")), getEnumValue(config.getString("plugin.tos-menu.inventory"), InventoryType.class), player).buildInventory();

		inventory.setAllowedToClose(false);
		inventory.addClickHandler(this);
		inventory.setItem(0, ChatColor.translateAlternateColorCodes('&', config.getString("plugin.tos-menu.accept-item.name")), getEnumValue(config.getString("plugin.tos-menu.accept-item.item"), Material.class));
		inventory.setItem(1, ChatColor.translateAlternateColorCodes('&', config.getString("plugin.tos-menu.discord-item.name")), getEnumValue(config.getString("plugin.tos-menu.discord-item.item"), Material.class));
		inventory.setItem(2, ChatColor.translateAlternateColorCodes('&', config.getString("plugin.tos-menu.deny-item.name")), getEnumValue(config.getString("plugin.tos-menu.deny-item.item"), Material.class));
		inventory.setItem(3, ChatColor.translateAlternateColorCodes('&', config.getString("plugin.tos-menu.tos-item.name")), getEnumValue(config.getString("plugin.tos-menu.tos-item.item"), Material.class));

		return inventory;
	}

	public Inventory getTosMenu()
	{
		PaginatedInventory tosMenu = new InventoryBuilder(plugin, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("plugin.tos-menu.tos-item.name")), getEnumValue(plugin.getConfig().getString("plugin.tos-menu.inventory"), InventoryType.class), player).buildPaginatedInventory(2, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("plugin.tos-menu.tos-item.buttons.next.name")), Material.valueOf(plugin.getConfig().getString("plugin.tos-menu.tos-item.buttons.next.item")), 0, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("plugin.tos-menu.tos-item.buttons.previous.name")), Material.valueOf(plugin.getConfig().getString("plugin.tos-menu.tos-item.buttons.previous.item")));
		tosMenu.addClickHandler(this);

		List<List<String>> pages = new ArrayList<>();

		for(String page : plugin.getConfig().getStringList("plugin.tos-menu.tos-item.tos"))
		{
			String[] lines = page.split("%n");
			List<String> linesList = new ArrayList<>();

			for(String line : lines)
				linesList.add(ChatColor.translateAlternateColorCodes('&', line));

			pages.add(linesList);
		}

		for(int i = 0; i < pages.size(); i++)
			tosMenu.setItem(i, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("plugin.tos-menu.tos-item.buttons.page.name")), getEnumValue(plugin.getConfig().getString("plugin.tos-menu.tos-item.buttons.page.item"), Material.class), pages.get(i));

		tosMenu.setPermanentItem(3, "", Material.AIR, PaginatedInventory.PermanentItemType.NORMAL);
		tosMenu.setPermanentItem(4, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("plugin.tos-menu.tos-item.buttons.back.name")), getEnumValue(plugin.getConfig().getString("plugin.tos-menu.tos-item.buttons.back.item"), Material.class), PaginatedInventory.PermanentItemType.NORMAL);
		return tosMenu;
	}

	private <T extends Enum> T getEnumValue(String value, Class<? extends T> enumClass)
	{
		try
		{
			return (T) Enum.valueOf(enumClass, value.toUpperCase());
		}
		catch(IllegalArgumentException e)
		{
			plugin.getLogger().warning(value.toUpperCase() + " is not a valid enum value!");
			return null;
		}
	}

	@Override
	public void onInventoryClick(InventoryClickEvent e)
	{
		Player player = e.getPlayer();
		int slot = e.getSlot();

		if(e.getInventory() instanceof PaginatedInventory)
		{
			if(slot == 4 && e.getSlotType() == Inventory.SlotType.PERMANENT_SLOT)
				e.setRedirect(getMainMenu());
		}
		else
		{
			if(slot == 0)
			{
				plugin.acceptedTOS.add(e.getPlayer().getUniqueId());
				e.setClosed(true);
			}
			else if(slot == 2)
				player.kickPlayer(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("plugin.tos-menu.deny-item.message")));
			else if(slot == 3)
				e.setRedirect(getTosMenu());
		}
	}
}
