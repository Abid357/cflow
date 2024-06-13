package cores;

import java.util.ArrayList;
import java.util.List;

import globals.ValueFormatter;

public class Stock {
	private Item item;
	private double quantity;
	private double price; // per unit
	private Double originalPrice;
	private String itemId;
	
	public Stock(Item item, double quantity, double price) {
		super();
		this.item = item;
		this.quantity = quantity;
		this.price = price;
	}

	public Stock(String itemId, double quantity, double price) {
		super();
		this.itemId = itemId;
		this.item = null;
		this.quantity = quantity;
		this.price = price;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public double getQuantity() {
		return quantity;
	}

	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public Double getOriginalPrice() {
		return originalPrice;
	}

	public void setOriginalPrice(double originalPrice) {
		this.originalPrice = new Double(originalPrice);
	}

	@Override
	public String toString() {
		String string = ValueFormatter.formatItem(item) + ";" + ValueFormatter.formatMoneySafely(quantity) + ";"
				+ ValueFormatter.formatMoneySafely(price);
		if (originalPrice != null)
			string += ";" + ValueFormatter.formatMoneySafely(originalPrice.doubleValue());
		return string;
	}

	public static List<Stock> parseItems(String itemString) {
		List<Stock> items = new ArrayList<Stock>();
		String decapsulatedString = itemString.substring(1, itemString.length() - 1);
		if (decapsulatedString.isEmpty())
			return items;
		String[] splitItems = decapsulatedString.split("/");
		for (String eachItem : splitItems)
			items.add(Stock.parse(eachItem.split(";")));
		return items;
	}

	public static Stock parse(String record[]) {
		Item item = ValueFormatter.parseItem(record[0]);
		double quantity = Double.parseDouble(record[1]);
		double price = Double.parseDouble(record[2]);
		Stock stock;
		if (item == null)
			stock =  new Stock(record[0], quantity, price);
		else
			stock =  new Stock(item, quantity, price);
		if (record.length==4)
			stock.setOriginalPrice(Double.parseDouble(record[3]));
		return stock;
	}
}
