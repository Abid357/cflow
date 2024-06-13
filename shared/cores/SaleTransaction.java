package cores;

import java.util.Date;
import java.util.List;

import globals.ValueFormatter;
import helpers.TransactionStatus;

public class SaleTransaction implements Comparable<SaleTransaction>, ITransaction {
	private int id;
	private Date date;
	private UserObject customer;
	private Store store;
	private List<Stock> items;
	private double discount;
	private double vat;
	private double total;
	private List<Transaction> payments;
	private List<Transaction> costs;
	private TransactionStatus itemStatus;
	private TransactionStatus paymentStatus;
	private String invoiceNo;

	public SaleTransaction(int id, Date date, UserObject customer, Store store, List<Stock> items, double discount, double vat,
			double total, List<Transaction> payments, List<Transaction> costs, TransactionStatus itemStatus,
			TransactionStatus paymentStatus, String invoiceNo) {
		super();
		this.id = id;
		this.date = date;
		this.customer = customer;
		this.store = store;
		this.items = items;
		this.discount = discount;
		this.vat = vat;
		this.total = total;
		this.payments = payments;
		this.costs = costs;
		this.itemStatus = itemStatus;
		this.paymentStatus = paymentStatus;
		this.invoiceNo = invoiceNo;
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

	public UserObject getCustomer() {
		return customer;
	}

	public void setCustomer(UserObject customer) {
		this.customer = customer;
	}

	public Store getStore() {
		return store;
	}

	public List<Transaction> getCosts() {
		return costs;
	}

	public void setCosts(List<Transaction> costs) {
		this.costs = costs;
	}

	public void setStore(Store store) {
		this.store = store;
	}

	public List<Stock> getItems() {
		return items;
	}

	public void setItems(List<Stock> items) {
		this.items = items;
	}

	public double getVat() {
		return vat;
	}

	public double getDiscount() {
		return discount;
	}

	public void setDiscount(double discount) {
		this.discount = discount;
	}

	public void setVat(double vat) {
		this.vat = vat;
	}

	public double getTotal() {
		return total;
	}

	public void setTotal(double total) {
		this.total = total;
	}

	public List<Transaction> getPayments() {
		return payments;
	}

	public void setPayments(List<Transaction> payments) {
		this.payments = payments;
	}

	public TransactionStatus getItemStatus() {
		return itemStatus;
	}

	public void setItemStatus(TransactionStatus itemStatus) {
		this.itemStatus = itemStatus;
	}

	public TransactionStatus getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(TransactionStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public String getInvoiceNo() {
		return invoiceNo;
	}

	public void setInvoiceNo(String invoiceNo) {
		this.invoiceNo = invoiceNo;
	}

	@Override
	public String toString() {
		String itemString = "[";
		for (int i = 0; i < items.size(); i++)
			itemString += items.get(i) + "/";
		if (items.size() != 0)
			itemString = itemString.substring(0, itemString.length() - 1);
		itemString += "]";
		String paymentIds = "[";
		for (int i = 0; i < payments.size(); i++)
			paymentIds += payments.get(i).getId() + "/";
		if (payments.size() != 0)
			paymentIds = paymentIds.substring(0, paymentIds.length() - 1);
		paymentIds += "]";
		String costIds = "[";
		for (int i = 0; i < costs.size(); i++)
			costIds += costs.get(i).getId() + "/";
		if (costs.size() != 0)
			costIds = costIds.substring(0, costIds.length() - 1);
		costIds += "]";
		return id + "," + ValueFormatter.formatDate(date) + "," + ValueFormatter.formatUserObject(customer) + ","
				+ ValueFormatter.formatStore(store) + "," + ValueFormatter.formatMoneySafely(discount) + "," +ValueFormatter.formatMoneySafely(vat) + ","
				+ ValueFormatter.formatMoneySafely(total) + "," + paymentIds + "," + costIds + "," + itemStatus + ","
				+ paymentStatus + "," + invoiceNo + "," + itemString;
	}

	@Override
	public int compareTo(SaleTransaction transaction) {
		int comparedId = transaction.getId();
		return comparedId - this.id;
	}

	public static SaleTransaction parse(String record[]) {
		int id = Integer.parseInt(record[0]);
		Date date = ValueFormatter.parseDate(record[1]);
		UserObject customer = ValueFormatter.parseUserObject(record[2]);
		Store store = ValueFormatter.parseStore(record[3]);
		double discount = Double.parseDouble(record[4]);
		double vat = Double.parseDouble(record[5]);
		double total = Double.parseDouble(record[6]);
		List<Transaction> payments = Transaction.parseTransactions(record[7]);
		List<Transaction> costs = Transaction.parseTransactions(record[8]);
		TransactionStatus itemStatus = TransactionStatus.valueOf(record[9]);
		TransactionStatus paymentStatus = TransactionStatus.valueOf(record[10]);
		String invoiceNo = record[11];
		List<Stock> items = Stock.parseItems(record[12]);
		return new SaleTransaction(id, date, customer, store, items, discount, vat, total, payments, costs, itemStatus,
				paymentStatus, invoiceNo);
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
