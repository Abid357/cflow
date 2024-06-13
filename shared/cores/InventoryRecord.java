package cores;

import globals.ValueFormatter;

public class InventoryRecord {
	private String id;
	private Inventory inventory;
	private double balance;

	public InventoryRecord(String id, Inventory inventory, double balance) {
		super();
		this.id = id;
		this.inventory = inventory;
		this.balance = balance;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public Inventory getInventory() {
		return inventory;
	}

	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}

	@Override
	public String toString() {
		return id + "," + ValueFormatter.formatInventory(inventory) + "," + ValueFormatter.formatMoneySafely(balance);
	}

	public static InventoryRecord parse(String[] record) {
		String id = record[0];
		Inventory inventory = ValueFormatter.parseInventory(record[1]);
		double balance = Double.parseDouble(record[2]);
		return new InventoryRecord(id, inventory, balance);
	}
}
