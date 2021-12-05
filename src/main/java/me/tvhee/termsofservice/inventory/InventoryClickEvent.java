package me.tvhee.termsofservice.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

public class InventoryClickEvent
{
	private final Inventory inventory;
	private final int page;
	private final Player player;
	private final ItemStack icon;
	private final int slot;
	private final InventoryAction action;
	private final ClickType click;
	private final Inventory.SlotType slotType;
	private Inventory redirect;
	private boolean closed;

	public InventoryClickEvent(Inventory inventory, int page, Player player, ItemStack icon, int slot, InventoryAction action, ClickType click, Inventory.SlotType slotType)
	{
		this.inventory = inventory;
		this.page = page;
		this.player = player;
		this.icon = icon;
		this.slot = slot;
		this.action = action;
		this.click = click;
		this.slotType = slotType;
		this.closed = false;
	}

	public Inventory getInventory()
	{
		return inventory;
	}

	/**
	 * If not a paginated menu always 0
	 * @see PaginatedInventory
	 * @return
	 */

	public int getPage()
	{
		return page;
	}

	public Player getPlayer()
	{
		return player;
	}

	public ItemStack getItem()
	{
		return icon;
	}

	public int getSlot()
	{
		return slot;
	}

	public InventoryAction getAction()
	{
		return action;
	}

	public ClickType getClick()
	{
		return click;
	}

	public boolean willClose()
	{
		return closed;
	}

	public Inventory.SlotType getSlotType()
	{
		return slotType;
	}

	public void setClosed(boolean close)
	{
		if(close && redirect != null)
			throw new IllegalArgumentException("You can't close the inventory because you are redirecting");

		this.closed = close;
	}

	public void setRedirect(Inventory redirect)
	{
		this.redirect = redirect;
		setClosed(false);
	}

	public Inventory getRedirect()
	{
		return redirect;
	}
}
