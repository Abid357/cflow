package globals;

import java.util.List;

import cores.Item;
import cores.Stock;
import cores.Store;
import databases.Stores;
import helpers.ItemType;

public class ItemFacade {

	public static Stock findStockById(List<Stock> items, int id) {
		for (Stock item : items)
			if (item.getItem().getId() == id)
				return item;
		return null;
	}

	public static boolean removeStockById(List<Stock> items, int id) {
		for (Stock item : items)
			if (item.getItem().getId() == id) {
				items.remove(item);
				break;
			}
		Stores db = (Stores) DatabaseFacade.getDatabase("Stores");
		db.setDirty(true);
		return true;
	}

	private static double calculateAveragePrice(double quantity1, double price1, double quantity2, double price2) {
		return ((quantity1 * price1) + (quantity2 * price2)) / (quantity1 + quantity2);
	}

	public static boolean stock(Store store, Item item, double quantity, double price) {
		Stock stock = findStockById(store.getItems(), item.getId());
		if (item.getType().equals(ItemType.SERVICE)) {
			if (stock != null)
				stock.setPrice(price);
			else
				store.getItems().add(new Stock(item, -1, price));
		} else {
			if (stock != null) {
				if (stock.getPrice() != price)
					stock.setPrice(calculateAveragePrice(stock.getQuantity(), stock.getPrice(), quantity, price));
				stock.setQuantity(stock.getQuantity() + quantity);
			} else
				store.getItems().add(new Stock(item, quantity, price));
		}
		Stores db = (Stores) DatabaseFacade.getDatabase("Stores");
		db.setDirty(true);
		return true;
	}

	public static boolean unstock(Store store, Item item, double quantity) {
		Stock stock = findStockById(store.getItems(), item.getId());
		boolean isDirty = false;
		if (stock != null)
			if (item.getType().equals(ItemType.SERVICE))
				return true;
			else if (quantity == stock.getQuantity())
				isDirty = store.getItems().remove(stock);
			else if (quantity < stock.getQuantity()) {
				stock.setQuantity(stock.getQuantity() - quantity);
				isDirty = true;
			} else
				return false;
		else
			return false;
		Stores db = (Stores) DatabaseFacade.getDatabase("Stores");
		db.setDirty(isDirty);
		return true;
	}
}
