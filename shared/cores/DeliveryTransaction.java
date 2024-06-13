package cores;

import java.util.Date;
import java.util.List;

import globals.ValueFormatter;

public class DeliveryTransaction implements Comparable<DeliveryTransaction>, ITransaction {

	private int id;
	private Date date;
	private Store fromStore;
	private Store toStore;
	private List<Stock> items;
	private double total;
	private List<Transaction> costs;

	public DeliveryTransaction(int id, Date date, Store fromStore, Store toStore, double total, List<Transaction> costs,
			List<Stock> items) {
		super();
		this.id = id;
		this.date = date;
		this.fromStore = fromStore;
		this.toStore = toStore;
		this.items = items;
		this.total = total;
		this.costs = costs;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Store getFromStore() {
		return fromStore;
	}

	public void setFromStore(Store fromStore) {
		this.fromStore = fromStore;
	}

	public Store getToStore() {
		return toStore;
	}

	public void setToStore(Store toStore) {
		this.toStore = toStore;
	}

	public List<Stock> getItems() {
		return items;
	}

	public void setItems(List<Stock> items) {
		this.items = items;
	}

	public double getTotal() {
		return total;
	}

	public void setTotal(double total) {
		this.total = total;
	}

	public List<Transaction> getCosts() {
		return costs;
	}

	public void setCosts(List<Transaction> costs) {
		this.costs = costs;
	}

	@Override
	public int compareTo(DeliveryTransaction transaction) {
		int comparedId = transaction.getId();
		/* For Ascending order */
		return this.id - comparedId;
	}

	@Override
	public String toString() {
		String itemString = "[";
		for (int i = 0; i < items.size(); i++)
			itemString += items.get(i) + "/";
		if (items.size() != 0)
			itemString = itemString.substring(0, itemString.length() - 1);
		itemString += "]";
		String costIds = "[";
		for (int i = 0; i < costs.size(); i++)
			costIds += costs.get(i).getId() + "/";
		if (costs.size() != 0)
			costIds = costIds.substring(0, costIds.length() - 1);
		costIds += "]";
		return id + "," + ValueFormatter.formatDate(date) + "," + ValueFormatter.formatStore(fromStore) + ","
				+ ValueFormatter.formatStore(toStore) + "," + ValueFormatter.formatMoneySafely(total) + "," + costIds
				+ "," + itemString;
	}

	public static DeliveryTransaction parse(String record[]) {
		int id = Integer.parseInt(record[0]);
		Date date = ValueFormatter.parseDate(record[1]);
		Store fromStore = ValueFormatter.parseStore(record[2]);
		Store toStore = ValueFormatter.parseStore(record[3]);
		double total = Double.parseDouble(record[4]);
		List<Transaction> costs = Transaction.parseTransactions(record[5]);
		List<Stock> items = Stock.parseItems(record[6]);
		return new DeliveryTransaction(id, date, fromStore, toStore, total, costs, items);
	}

	@Override
	public double getAmount() {
		// TODO Auto-generated method stub
		return total;
	}

	@Override
	public boolean isCredit() {
		// TODO Auto-generated method stub
		return false;
	}
}
