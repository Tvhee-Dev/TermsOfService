package me.tvhee.termsofservice.inventory;

import me.tvhee.termsofservice.TermsOfServicePlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryBuilder
{
	private final TermsOfServicePlugin plugin;
	private final String name;
	private final Player player;
	private int size = -1;
	private InventoryType inventoryType;

	public InventoryBuilder(TermsOfServicePlugin plugin, String name, InventoryType inventoryType, Player player)
	{
		this.plugin = plugin;
		this.name = name;
		this.inventoryType = inventoryType;
		this.player = player;
	}

	public InventoryBuilder(TermsOfServicePlugin plugin, String name, int size, Player player)
	{
		this.plugin = plugin;
		this.name = name;
		this.size = size;
		this.player = player;
	}

	public Inventory buildInventory()
	{
		if(inventoryType != null && size == -1)
			return new InventoryMenu(plugin, name, inventoryType, player);
		else if(inventoryType == null && size != -1)
			return new InventoryMenu(plugin, name, size, player);
		else
			throw new IllegalArgumentException("Illegal inventory size!");
	}

	public PaginatedInventory buildPaginatedInventory(int nextButtonSlot, ItemStack nextButton, int previousButtonSlot, ItemStack previousButton)
	{
		PaginatedInventory inventory;

		if(inventoryType != null && size == -1)
			inventory = new PaginatedInventory(plugin, name, inventoryType, player);
		else if(inventoryType == null && size != -1)
			inventory =  new PaginatedInventory(plugin, name, size, player);
		else
			throw new IllegalArgumentException("Illegal inventory size!");

		inventory.setPermanentItem(nextButtonSlot, nextButton, PaginatedInventory.PermanentItemType.NEXT_BUTTON);
		inventory.setPermanentItem(previousButtonSlot, previousButton, PaginatedInventory.PermanentItemType.PREVIOUS_BUTTON);
		return inventory;
	}

	public PaginatedInventory buildPaginatedInventory(int nextButtonSlot, String nextButtonName, Material nextButton, int previousButtonSlot, String previousButtonName, Material previousButton)
	{
		ItemStack nextButtonItem = new ItemStack(nextButton);
		ItemMeta nextButtonMeta = nextButtonItem.getItemMeta();
		nextButtonMeta.setDisplayName(nextButtonName);
		nextButtonItem.setItemMeta(nextButtonMeta);

		ItemStack previousButtonItem = new ItemStack(previousButton);
		ItemMeta previousButtonMeta = previousButtonItem.getItemMeta();
		previousButtonMeta.setDisplayName(previousButtonName);
		previousButtonItem.setItemMeta(previousButtonMeta);

		return buildPaginatedInventory(nextButtonSlot, nextButtonItem, previousButtonSlot, previousButtonItem);
	}
}
