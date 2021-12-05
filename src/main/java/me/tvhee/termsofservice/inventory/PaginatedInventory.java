package me.tvhee.termsofservice.inventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.tvhee.termsofservice.TermsOfServicePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PaginatedInventory extends Inventory
{
	private final Map<Integer, ItemStack> permanentSlotIcons = new HashMap<>();
	private int nextClickSlot = -1;
	private int previousClickSlot = -1;
	private int page = 1;

	PaginatedInventory(TermsOfServicePlugin plugin, String name, InventoryType inventoryType, Player player)
	{
		super(plugin, name, inventoryType, player);
	}

	PaginatedInventory(TermsOfServicePlugin plugin, String name, int size, Player player)
	{
		super(plugin, name, size, player);
	}

	public int getPage()
	{
		return page;
	}

	public void setPermanentItem(int slot, String name, Material material, List<String> lore, PermanentItemType itemType)
	{
		setPermanentItem(slot, buildStack(name, new ItemStack(material), lore), itemType);
	}

	public void setPermanentItem(int slot, String name, ItemStack item, List<String> lore, PermanentItemType itemType)
	{
		setPermanentItem(slot, buildStack(name, item, lore), itemType);
	}

	public void setPermanentItem(int slot, String name, ItemStack item, PermanentItemType itemType)
	{
		setPermanentItem(slot, buildStack(name, item), itemType);
	}

	public void setPermanentItem(int slot, String name, Material material, PermanentItemType itemType)
	{
		setPermanentItem(slot, buildStack(name, new ItemStack(material)), itemType);
	}

	public void setPermanentItem(int slot, ItemStack item, PermanentItemType itemType)
	{
		if(this.permanentSlotIcons.containsKey(slot))
			throw new IllegalArgumentException("Slot is already set!");

		if(itemType == PermanentItemType.NEXT_BUTTON)
		{
			if(this.nextClickSlot != -1)
				this.permanentSlotIcons.remove(nextClickSlot);

			this.nextClickSlot = slot;
		}
		else if(itemType == PermanentItemType.PREVIOUS_BUTTON)
		{
			if(this.previousClickSlot != -1)
				this.permanentSlotIcons.remove(previousClickSlot);

			this.previousClickSlot = slot;
		}

		this.permanentSlotIcons.put(slot, item);
	}
	
	public ItemStack getPermanentItem(int slot)
	{
		return this.permanentSlotIcons.get(slot);
	}

	public boolean isPermanentItem(int slot)
	{
		return this.permanentSlotIcons.containsKey(slot);
	}

	@Override
	public boolean isSlotFiller(int slot)
	{
		return !this.permanentSlotIcons.containsKey(slot) && !getContents().containsKey(slot);
	}

	public void open(int page)
	{
		if(page > getMaxPages() || page < 1)
			throw new IllegalArgumentException("The page is not an available page!");

		this.page = page;
		super.open();
	}

	@Override
	public void open()
	{
		this.page = 1;
		super.open();
	}

	public int getMaxPages()
	{
		int pages = 0;
		int maxContents = 0;

		for(Integer key : getUsedSlots())
		{
			if(key + 1 > maxContents)
				maxContents = key + 1;
		}

		int contentsPerPage = getSize() - this.permanentSlotIcons.size();

		for(int i = maxContents; i > 0; i = i - contentsPerPage)
			pages++;

		return pages;
	}

	@Override
	public org.bukkit.inventory.Inventory getInventory()
	{
		org.bukkit.inventory.Inventory inventory = getInventoryType() == null ? Bukkit.createInventory(this, getSize(), getName()) : Bukkit.createInventory(this, getInventoryType(), getName());
		int currentSlot = (page - 1) * (getSize() - permanentSlotIcons.size());

		if(currentSlot > 0)
			currentSlot--;

		for(int i = 0; i < getSize(); i++)
		{
			if(this.permanentSlotIcons.containsKey(i))
			{
				inventory.setItem(i, replacePageModifier(permanentSlotIcons.get(i)));
			}
			else
			{
				if(getItem(currentSlot) != null)
					inventory.setItem(i, replacePageModifier(getItem(currentSlot)));

				currentSlot++;
			}
		}

		return inventory;
	}

	@Override
	public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent e)
	{
		if(!isAllowedToReplace())
			e.setCancelled(true);

		int slot = e.getRawSlot();

		if(e.getCurrentItem() == null || (e.getCurrentItem().getType() != Material.AIR && !e.getCurrentItem().getType().isItem()))
			return;

		if(slot == nextClickSlot && e.getCurrentItem().getType() == getPermanentItem(nextClickSlot).getType() && page + 1 < getMaxPages())
		{
			page++;
			rearrangeItems(e.getInventory());
			return;
		}
		else if(slot == previousClickSlot && e.getCurrentItem().getType() == getPermanentItem(previousClickSlot).getType() && page - 1 > 1)
		{
			page--;
			rearrangeItems(e.getInventory());
			return;
		}

		int iconSlot = slot;

		if(!permanentSlotIcons.containsKey(slot))
			iconSlot = slot + ((getSize() - permanentSlotIcons.size()) * (page - 1));

		if(slot < 0 || slot > getSize())
			return;

		SlotType slotType = SlotType.NORMAL_SLOT;

		if(isSlotFiller(slot))
			slotType = SlotType.FILLED_SLOT;
		else if(isPermanentItem(slot))
			slotType = SlotType.PERMANENT_SLOT;

		InventoryClickEvent optionClick = new InventoryClickEvent(this, page, (Player) e.getWhoClicked(), e.getCurrentItem(), iconSlot, e.getAction(), e.getClick(), slotType);
		callClickEvent(optionClick);
	}

	private ItemStack replacePageModifier(ItemStack item)
	{
		ItemStack newItem = item.clone();
		ItemMeta itemMeta = newItem.getItemMeta();

		if(itemMeta != null)
		{
			String name = itemMeta.getDisplayName().replaceAll("%page%", String.valueOf(page));
			itemMeta.setDisplayName(name);
		}

		newItem.setItemMeta(itemMeta);
		return newItem;
	}

	public enum PermanentItemType
	{
		NEXT_BUTTON,
		PREVIOUS_BUTTON,
		NORMAL;
	}
}
