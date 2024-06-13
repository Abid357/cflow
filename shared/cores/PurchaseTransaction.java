package cores;

import java.util.Date;
import java.util.List;

import globals.ValueFormatter;
import helpers.TransactionStatus;

public class PurchaseTransaction implements Comparable<PurchaseTransaction>, ITransaction {
	private int id;
	private Date date;
	private UserObject supplier;
	private Store store;
	private List<Stock> items;
	private double discount;
	private double vat;
	private double total;
	private List<Transaction> payments;
	private List<Transaction> costs;
	private TransactionStatus itemStatus;
	private TransactionStatus paymentStatus;
	private String lpoNo;
	private String piNo;
	private String invoiceNo;

	public PurchaseTransaction(int id, Date date, UserObject supplier, Store store, List<Stock> items, double discount, double vat,
			double total, List<Transaction> payments, List<Transaction> costs, TransactionStatus itemStatus,
			TransactionStatus paymentStatus, String lpoNo, String piNo, String invoiceNo) {
		super();
		this.id = id;
		this.date = date;
		this.supplier = supplier;
		this.store = store;
		this.items = items;
		this.discount = discount;
		this.vat = vat;
		this.total = total;
		this.payments = payments;
		this.costs = costs;
		this.itemStatus = itemStatus;
		this.paymentStatus = paymentStatus;
		this.lpoNo = lpoNo;
		this.piNo = piNo;
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

	public UserObject getSupplier() {
		return supplier;
	}

	public void setSupplier(UserObject supplier) {
		this.supplier = supplier;
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

	public String getLpoNo() {
		return lpoNo;
	}

	public void setLpoNo(String lpoNo) {
		this.lpoNo = lpoNo;
	}

	public String getPiNo() {
		return piNo;
	}

	public void setPiNo(String piNo) {
		this.piNo = piNo;
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
		return id + "," + ValueFormatter.formatDate(date) + "," + ValueFormatter.formatUserObject(supplier) + ","
				+ ValueFormatter.formatStore(store) + "," + ValueFormatter.formatMoneySafely(discount) + "," +ValueFormatter.formatMoneySafely(vat) + ","
				+ ValueFormatter.formatMoneySafely(total) + "," + paymentIds + "," + costIds + "," + itemStatus + ","
				+ paymentStatus + "," + lpoNo + "," + piNo + "," + invoiceNo + "," + itemString;
	}

	@Override
	public int compareTo(PurchaseTransaction transaction) {
		int comparedId = transaction.getId();
		/* For Ascending order */
		return comparedId - this.id;
	}

	public static PurchaseTransaction parse(String record[]) {
		int id = Integer.parseInt(record[0]);
		Date date = ValueFormatter.parseDate(record[1]);
		UserObject supplier = ValueFormatter.parseUserObject(record[2]);
		Store store = ValueFormatter.parseStore(record[3]);
		double discount = Double.parseDouble(record[4]);
		double vat = Double.parseDouble(record[5]);
		double total = Double.parseDouble(record[6]);
		List<Transaction> payments = Transaction.parseTransactions(record[7]);
		List<Transaction> costs = Transaction.parseTransactions(record[8]);
		TransactionStatus itemStatus = TransactionStatus.valueOf(record[9]);
		TransactionStatus paymentStatus = TransactionStatus.valueOf(record[10]);
		String lpoNo = record[11];
		String piNo = record[12];
		String invoiceNo = record[13];
		List<Stock> items = Stock.parseItems(record[14]);
		return new PurchaseTransaction(id, date, supplier, store, items, discount, vat, total, payments, costs, itemStatus,
				paymentStatus, lpoNo, piNo, invoiceNo);
	}

	@Override
	public double getAmount() {
		// TODO Auto-generated method stub
		return total;
	}

	@Override
	public boolean isCredit() {
		// TODO Auto-generated method stub
		return true;
	}
}
