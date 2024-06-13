package cores;

import java.util.List;

import helpers.ItemType;

public class Material extends Item {
	List<Stock> components;

	public Material(int id, String name, double price, String unit, double conversion, String details,
			List<Stock> components) {
		super(id, ItemType.MATERIAL, name, price, unit, conversion, details);
		this.components = components;
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
		return super.toString() + ",," + componentString;
	}

	public static Material parse(String[] record) {
		int id = Integer.parseInt(record[0]);
		String name = record[2];
		double price = Double.parseDouble(record[3]);
		String unit = record[4];
		double conversion = Double.parseDouble(record[5]);
		String details = record[6];
		List<Stock> components = Stock.parseItems(record[8]);
		return new Material(id, name, price, unit, conversion, details, components);
	}
}
