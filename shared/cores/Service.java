package cores;

import helpers.ItemType;

public class Service extends Item{

	public Service(int id, String name, double price, String details) {
		super(id, ItemType.SERVICE, name, price, "pc", 1.00, details);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return super.toString() + ",,[]";
	}

	public static Service parse(String[] record) {
		int id = Integer.parseInt(record[0]);
		String name = record[2];
		double price = Double.parseDouble(record[3]);
		String details = record[6];
		return new Service(id, name, price, details);
	}
}
