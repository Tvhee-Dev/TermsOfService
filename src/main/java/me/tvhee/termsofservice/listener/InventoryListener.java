package me.tvhee.termsofservice.listener;

import me.tvhee.termsofservice.inventory.Inventory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

public class InventoryListener implements Listener
{
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBukkitInventoryClick(InventoryClickEvent e)
	{
		InventoryHolder inventoryHolder = e.getInventory().getHolder();

		if(inventoryHolder instanceof Inventory)
		{
			e.setCancelled(true);
			((Inventory) inventoryHolder).onInventoryClick(e);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBukkitInventoryClose(InventoryCloseEvent e)
	{
		InventoryHolder inventoryHolder = e.getInventory().getHolder();

		if(inventoryHolder instanceof Inventory inventory)
		{
			if(!inventory.isAllowedToClose())
				inventory.open();
		}
	}
}
