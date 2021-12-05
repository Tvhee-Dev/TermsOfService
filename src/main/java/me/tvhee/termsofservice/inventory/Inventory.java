package me.tvhee.termsofservice.inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import me.tvhee.termsofservice.TermsOfServicePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class Inventory implements InventoryHolder
{
	private final TermsOfServicePlugin plugin;
	private final List<InventoryClickHandler> handlers = new ArrayList<>();
	private final String name;
	private final int size;
	private final Player player;
	private final Map<Integer, ItemStack> contents = new HashMap<>();
	private int maxStackSize = 64;
	private ItemStack slotFiller = new ItemStack(Material.AIR);
	private InventoryType inventoryType;
	private boolean allowedToClose = true;
	private boolean allowedToReplace = false;

	Inventory(TermsOfServicePlugin plugin, String name, InventoryType inventoryType, Player player)
	{
		this.plugin = plugin;
		this.name = name;
		this.inventoryType = inventoryType;
		this.player = player;
		this.size = inventoryType.getDefaultSize();
	}

	Inventory(TermsOfServicePlugin plugin, String name, int size, Player player)
	{
		this.plugin = plugin;
		this.name = name;
		this.size = size;
		this.player = player;
	}

	public void open()
	{
		org.bukkit.inventory.Inventory filled = fillInventory(getInventory());
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> player.openInventory(filled), 1);
	}

	public void rearrangeItems(org.bukkit.inventory.Inventory inventory)
	{
		org.bukkit.inventory.Inventory filled = fillInventory(getInventory());

		for(int i = 0; i < inventory.getSize(); i++)
			inventory.setItem(i, filled.getItem(i));
	}

	public void addClickHandler(InventoryClickHandler handler)
	{
		this.handlers.add(handler);
	}

	public void removeClickHandler(InventoryClickHandler handler)
	{
		this.handlers.remove(handler);
	}

	public String getName()
	{
		return name;
	}

	public Player getPlayer()
	{
		return player;
	}

	public InventoryType getInventoryType()
	{
		return inventoryType;
	}

	public void setAllowedToClose(boolean allowedToClose)
	{
		this.allowedToClose = allowedToClose;
	}

	public void setAllowedToReplace(boolean allowedToReplace)
	{
		this.allowedToReplace = allowedToReplace;
	}

	public boolean isAllowedToClose()
	{
		return allowedToClose;
	}

	public boolean isAllowedToReplace()
	{
		return allowedToReplace;
	}

	public int getSize()
	{
		return size;
	}

	public int getContentSize()
	{
		return this.contents.size();
	}

	public int getMaxStackSize()
	{
		return maxStackSize;
	}

	public void setMaxStackSize(int maxStackSize)
	{
		this.maxStackSize = maxStackSize;
	}

	public ItemStack getItem(int slot)
	{
		return this.contents.get(slot);
	}

	public ItemStack getSlotFiller()
	{
		return slotFiller;
	}

	public boolean isSlotFiller(int slot)
	{
		return !this.contents.containsKey(slot);
	}

	public void setItem(int slot, String name, Material material, List<String> lore)
	{
		setItem(slot, buildStack(name, new ItemStack(material), lore));
	}

	public void setItem(int slot, String name, ItemStack item, List<String> lore)
	{
		setItem(slot, buildStack(name, item, lore));
	}

	public void setItem(int slot, String name, ItemStack item)
	{
		setItem(slot, buildStack(name, item));
	}

	public void setItem(int slot, String name, Material material)
	{
		setItem(slot, buildStack(name, new ItemStack(material)));
	}

	public void setItem(int slot, ItemStack item)
	{
		if(item.getAmount() > maxStackSize)
			throw new IllegalArgumentException("The item's amount is higher then the maxStackSize!");

		if(slot >= this.size && !(this instanceof PaginatedInventory))
			throw new IllegalArgumentException("The slots must be less then the size!");

		if(this.slotFiller.isSimilar(item))
			throw new IllegalArgumentException(item + " is the same as the slot filler!");

		if(this.contents.size() + 1 > size && !(this instanceof PaginatedInventory))
			throw new IllegalArgumentException("There are too many items!");

		this.contents.put(slot, item);
	}

	public void setSlotFiller(String name, Material material, List<String> lore)
	{
		setSlotFiller(buildStack(name, new ItemStack(material), lore));
	}

	public void setSlotFiller(String name, ItemStack item, List<String> lore)
	{
		setSlotFiller(buildStack(name, item, lore));
	}

	public void setSlotFiller(String name, ItemStack item)
	{
		setSlotFiller(buildStack(name, item));
	}

	public void setSlotFiller(String name, Material material)
	{
		setSlotFiller(buildStack(name, new ItemStack(material)));
	}

	public void setSlotFiller(ItemStack slotFiller)
	{
		this.slotFiller = slotFiller;
	}

	public Set<Integer> getUsedSlots()
	{
		return this.contents.keySet();
	}

	public void addItems(ItemStack... items)
	{
		int max = this instanceof PaginatedInventory ? Integer.MAX_VALUE : size;
		int itemCount = 0;

		for(int i = 0; i < max; i++)
		{
			if(itemCount == items.length)
				break;

			if(getItem(i) == null)
			{
				setItem(i, items[itemCount]);
				itemCount++;
			}
		}
	}

	public void removeItems(ItemStack... items)
	{
		for(ItemStack item : items)
		{
			for(int slot : this.contents.keySet())
			{
				if(getItem(slot).equals(item))
				{
					this.contents.remove(slot);
					break;
				}
			}
		}
	}

	public Map<Integer, ItemStack> getContents()
	{
		return this.contents;
	}

	public void setContents(Map<Integer, ItemStack> contents)
	{
		for(Entry<Integer, ItemStack> entry : contents.entrySet())
			setItem(entry.getKey(), entry.getValue());
	}

	public boolean contains(Material material)
	{
		for(ItemStack item : this.contents.values())
		{
			if(item.getType() == material)
				return true;
		}

		return false;
	}

	public boolean containsExact(Material material, int amount)
	{
		if(amount < 1)
			return true;

		int found = 0;

		for(ItemStack item : this.contents.values())
		{
			if(item.getType() == material)
				found++;
		}

		return found == amount;
	}

	public boolean containsAtLeast(Material material, int amount)
	{
		if(amount < 1)
			return true;

		int found = 0;

		for(ItemStack item : this.contents.values())
		{
			if(item.getType() == material)
				found++;
		}

		return found >= amount;
	}

	public boolean contains(ItemStack itemStack)
	{
		for(ItemStack item : this.contents.values())
		{
			if(item.isSimilar(itemStack))
				return true;
		}

		return false;
	}

	public boolean containsExact(ItemStack itemStack, int amount)
	{
		if(amount < 1)
			return true;

		int found = 0;

		for(ItemStack item : this.contents.values())
		{
			if(item.equals(itemStack))
				found++;
		}

		return found == amount;
	}

	public boolean containsAtLeast(ItemStack itemStack, int amount)
	{
		if(amount < 1)
			return true;

		int found = 0;

		for(ItemStack item : this.contents.values())
		{
			if(item.equals(itemStack))
				found++;
		}

		return found >= amount;
	}

	public Map<Integer, ItemStack> all(ItemStack item)
	{
		Map<Integer, ItemStack> foundItems = new HashMap<>();

		for(Entry<Integer, ItemStack> entry : this.contents.entrySet())
		{
			if(entry.getValue().equals(item))
				foundItems.put(entry.getKey(), entry.getValue());
		}

		return foundItems;
	}

	public Map<Integer, ItemStack> all(Material material)
	{
		Map<Integer, ItemStack> foundItems = new HashMap<>();

		for(Entry<Integer, ItemStack> entry : this.contents.entrySet())
		{
			if(entry.getValue().getType() == material)
				foundItems.put(entry.getKey(), entry.getValue());
		}

		return foundItems;
	}

	public int first(ItemStack item)
	{
		for(Entry<Integer, ItemStack> entry : this.contents.entrySet())
		{
			if(entry.getValue().equals(item))
				return entry.getKey();
		}

		return -1;
	}

	public int first(Material material)
	{
		for(Entry<Integer, ItemStack> entry : this.contents.entrySet())
		{
			if(entry.getValue().getType() == material)
				return entry.getKey();
		}

		return -1;
	}

	public int firstEmpty()
	{
		for(Integer slot : this.contents.keySet())
		{
			if(!this.contents.containsKey(slot + 1))
				return slot + 1;
		}

		return -1;
	}

	public int lastFilled()
	{
		List<Integer> keys = new ArrayList<>(this.contents.keySet());
		Collections.reverse(keys);

		for(Integer slot : keys)
		{
			if(!this.contents.containsKey(slot - 1))
				return slot - 1;
		}

		return -1;
	}

	public boolean isEmpty()
	{
		return this.contents.isEmpty();
	}

	public void remove(Material material)
	{
		List<Integer> toRemoveKeys = new ArrayList<>();

		for(Entry<Integer, ItemStack> entry : this.contents.entrySet())
		{
			if(entry.getValue().getType() == material)
				toRemoveKeys.add(entry.getKey());
		}

		for(Integer key : toRemoveKeys)
			this.contents.remove(key);
	}

	public void remove(ItemStack item)
	{
		List<Integer> toRemoveKeys = new ArrayList<>();

		for(Entry<Integer, ItemStack> entry : this.contents.entrySet())
		{
			if(entry.getValue().equals(item))
				toRemoveKeys.add(entry.getKey());
		}

		for(Integer key : toRemoveKeys)
			this.contents.remove(key);
	}

	public void clear()
	{
		this.contents.clear();
	}

	protected void callClickEvent(me.tvhee.termsofservice.inventory.InventoryClickEvent event)
	{
		for(InventoryClickHandler handler : this.handlers)
			handler.onInventoryClick(event);

		if(event.willClose() || event.getRedirect() != null)
		{
			setAllowedToClose(true);
			Player player = event.getPlayer();
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, player::closeInventory, 1);

			if(event.getRedirect() != null)
				event.getRedirect().open();
		}
	}

	public abstract void onInventoryClick(InventoryClickEvent e);

	protected org.bukkit.inventory.Inventory fillInventory(org.bukkit.inventory.Inventory inventory)
	{
		if(getSlotFiller() != null)
		{
			for(int i = 0; i < inventory.getSize(); i++)
			{
				if(inventory.getContents()[i] == null)
					inventory.setItem(i, getSlotFiller());
			}
		}

		return inventory;
	}

	protected ItemStack buildStack(String name, ItemStack material, List<String> lore)
	{
		ItemStack itemStack = material.clone();
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(name);

		List<String> formattedLore = new ArrayList<>();

		for(String line : lore)
			formattedLore.add(line);

		meta.setLore(formattedLore);
		itemStack.setItemMeta(meta);
		return itemStack;
	}

	protected ItemStack buildStack(String name, ItemStack material)
	{
		ItemStack itemStack = material.clone();
		ItemMeta meta = itemStack.getItemMeta();

		if(meta != null)
		{
			meta.setDisplayName(name);
			itemStack.setItemMeta(meta);
		}

		return itemStack;
	}

	public enum SlotType
	{
		PERMANENT_SLOT, FILLED_SLOT, NORMAL_SLOT
	}
}
