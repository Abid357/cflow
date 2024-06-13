package cores;

import java.util.List;

import helpers.ItemType;

public class Product extends Item {
	private String barcode;
	List<Stock> components;

	public Product(int id, String name, double price, String unit, double conversion, String details, String barcode,
			List<Stock> components) {
		super(id, ItemType.PRODUCT, name, price, unit, conversion, details);
		this.barcode = barcode;
		this.components = components;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public List<Stock> getComponents() {
		return components;
	}

	public void setComponents(List<Stock> components) {
		this.components = components;
	}

	@Override
	public String toString() {
		String componentString = "["; // encapsulate by adding square brackets
		for (int i = 0; i < components.size(); i++)
			componentString += components.get(i) + "/";
		if (components.size() != 0)
			componentString = componentString.substring(0, componentString.length() - 1);
		componentString += "]";
		return super.toString() + "," + barcode + "," + componentString;
	}

	public static Product parse(String[] record) {
		int id = Integer.parseInt(record[0]);
		String name = record[2];
		double price = Double.parseDouble(record[3]);
		String unit = record[4];
		double conversion = Double.parseDouble(record[5]);
		String details = record[6];
		String barcode = record[7];
		List<Stock> components = Stock.parseItems(record[8]);
		return new Product(id, name, price, unit, conversion, details, barcode, components);
	}
}
