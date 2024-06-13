package helpers;

import java.util.Date;

import cores.Inventory;
import cores.PurchaseTransaction;
import cores.SaleTransaction;
import cores.Transaction;
import cores.UserObject;
import globals.ValueFormatter;

public class TransactionWrapper {
	private String id;
	private double amount;
	private double tax;
	private boolean isCredit;
	private UserObject userObject;
	private Inventory inventory;
	private Date date;
	private String remark;
	private String category;

	public TransactionWrapper(Transaction transaction) {
		super();
		this.id = ValueFormatter.formatBalanceId(transaction.getId(), Transaction.class);
		this.amount = transaction.getAmount();
		this.tax = transaction.getTax();
		this.isCredit = transaction.isCredit();
		this.userObject = transaction.getUserObject();
		this.inventory = transaction.getInventory();
		this.date = transaction.getDate();
		this.remark = transaction.getRemark();
		this.category = transaction.getCategory();
	}

	public TransactionWrapper(SaleTransaction transaction) {
		super();
		this.id = ValueFormatter.formatBalanceId(transaction.getId(), SaleTransaction.class);
		this.amount = transaction.getTotal();
		this.tax = transaction.getVat();
		this.isCredit = false;
		this.userObject = transaction.getCustomer();
		this.inventory = transaction.getStore().getInventory();
		this.date = transaction.getDate();
		this.remark = ValueFormatter.formatSaleRemark(transaction);
		this.category = "Sale";
	}
	
	public TransactionWrapper(PurchaseTransaction transaction) {
		super();
		this.id = ValueFormatter.formatBalanceId(transaction.getId(), PurchaseTransaction.class);
		this.amount = transaction.getTotal();
		this.tax = transaction.getVat();
		this.isCredit = true;
		this.userObject = transaction.getSupplier();
		this.inventory = transaction.getStore().getInventory();
		this.date = transaction.getDate();
		this.remark = ValueFormatter.formatPurchaseRemark(transaction);
		this.category = "Purchase";
	}

	public TransactionWrapper(String id, double amount, double tax, boolean isCredit, UserObject userObject,
			Inventory inventory, Date date, String remark, String category) {
		super();
		this.id = id;
		this.amount = amount;
		this.tax = tax;
		this.isCredit = isCredit;
		this.userObject = userObject;
		this.inventory = inventory;
		this.date = date;
		this.remark = remark;
		this.category = category;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public double getTax() {
		return tax;
	}

	public void setTax(double tax) {
		this.tax = tax;
	}

	public boolean isCredit() {
		return isCredit;
	}

	public void setCredit(boolean isCredit) {
		this.isCredit = isCredit;
	}

	public UserObject getUserObject() {
		return userObject;
	}

	public void setUserObject(UserObject userObject) {
		this.userObject = userObject;
	}

	public Inventory getInventory() {
		return inventory;
	}

	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

}
