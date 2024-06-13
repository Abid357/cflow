package cores;

import java.util.List;

import globals.ValueFormatter;

public class Store {
	private String name;
	private UserObject owner;
	private List<Stock> items;
	private StoreInventory inventory;

	public Store(String name, UserObject owner, StoreInventory inventory, List<Stock> items) {
		super();
		this.name = name;
		this.owner = owner;
		this.inventory = inventory;
		this.items = items;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UserObject getOwner() {
		return owner;
	}

	public void setOwner(UserObject owner) {
		this.owner = owner;
	}

	public List<Stock> getItems() {
		return items;
	}

	public void setItems(List<Stock> items) {
		this.items = items;
	}

	public StoreInventory getInventory() {
		return inventory;
	}

	public void setInventory(StoreInventory inventory) {
		this.inventory = inventory;
	}

	@Override
	public String toString() {
		String itemString = "[";
		for (int i = 0; i < items.size(); i++)
			itemString += items.get(i) + "/";
		if (items.size() != 0)
			itemString = itemString.substring(0, itemString.length() - 1);
		itemString += "]";
		return name + "," + ValueFormatter.formatUserObject(owner) + "," + ValueFormatter.formatInventory(inventory)
				+ "," + itemString;
	}


	public static Store parse(String[] record) {
		String name = record[0];
		UserObject owner = ValueFormatter.parseUserObject(record[1]);
		StoreInventory inventory = (StoreInventory) ValueFormatter.parseInventory(record[2]);
		List<Stock> items = Stock.parseItems(record[3]);
		return new Store(name, owner, inventory, items);
	}

}
