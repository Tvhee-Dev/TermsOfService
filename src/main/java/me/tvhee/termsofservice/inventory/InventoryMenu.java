package me.tvhee.termsofservice.inventory;

import me.tvhee.termsofservice.TermsOfServicePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class InventoryMenu extends Inventory implements Listener
{
	InventoryMenu(TermsOfServicePlugin plugin, String name, InventoryType inventoryType, Player player)
	{
		super(plugin, name, inventoryType, player);
	}

	InventoryMenu(TermsOfServicePlugin plugin, String name, int size, Player player)
	{
		super(plugin, name, size, player);
	}

	@Override
	public org.bukkit.inventory.Inventory getInventory()
	{
		org.bukkit.inventory.Inventory inventory = getInventoryType() == null ? Bukkit.createInventory(this, getSize(), getName()) : Bukkit.createInventory(this, getInventoryType(), getName());

		int maxContents = 0;

		for(Integer key : getUsedSlots())
		{
			if(key + 1 > maxContents)
				maxContents = key + 1;
		}

		for(int i = 0; i < maxContents; i++)
		{
			if(getItem(i) != null)
				inventory.setItem(i, getItem(i));
		}

		return inventory;
	}

	@Override
	public void onInventoryClick(InventoryClickEvent e)
	{
		if(!isAllowedToReplace())
			e.setCancelled(true);

		int slot = e.getRawSlot();

		if(e.getCurrentItem() == null || (e.getCurrentItem().getType() != Material.AIR && !e.getCurrentItem().getType().isItem()))
			return;

		if(slot < 0 || slot > getSize())
			return;

		SlotType slotType = SlotType.NORMAL_SLOT;

		if(isSlotFiller(slot))
			slotType = SlotType.FILLED_SLOT;

		me.tvhee.termsofservice.inventory.InventoryClickEvent optionClick = new me.tvhee.termsofservice.inventory.InventoryClickEvent(this, 0, (Player) e.getWhoClicked(), e.getCurrentItem(), slot, e.getAction(), e.getClick(), slotType);
		callClickEvent(optionClick);
	}
}
