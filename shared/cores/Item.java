package cores;

import helpers.ItemType;

public abstract class Item {
	private int id;
	private ItemType type;
	private String name;
	private double price; // per unit
	private String unit; // default unit = pc (piece)
	private double conversion; // 1 unit = ? pc
	private String details;

	public Item(int id, ItemType type, String name, double price, String unit, double conversion, String details) {
		super();
		this.id = id;
		this.type = type;
		this.name = name;
		this.price = price;
		this.unit = unit;
		this.conversion = conversion;
		this.details = details;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ItemType getType() {
		return type;
	}

	public void setType(ItemType type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public double getConversion() {
		return conversion;
	}

	public void setConversion(double conversion) {
		this.conversion = conversion;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	@Override
	public String toString() {
		return id + "," + type + "," + name + "," + price + "," + unit + "," + conversion + "," + details;
	}
}
