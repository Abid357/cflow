package cores;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import databases.Transactions;
import globals.DatabaseFacade;
import globals.ValueFormatter;

public class Transaction implements Comparable<Transaction>, ITransaction {

	private int id;
	private double amount;
	private double tax;
	private boolean isCredit;
	private UserObject userObject;
	private Inventory inventory;
	private Date date;
	private String remark;
	private String category;

	public Transaction(int id, double amount, double tax, boolean isCredit, UserObject userObject, Inventory inventory,
			Date date, String remark, String category) {
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
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

    @Override
    public int compareTo(Transaction transaction) {
        int comparedId = transaction.getId();
        /* For Ascending order*/
        return this.id - comparedId;
    }
	
	@Override
	public String toString() {
		return id + "," + ValueFormatter.formatMoneySafely(amount) + "," + ValueFormatter.formatMoneySafely(tax) + "," + isCredit
				+ "," + ValueFormatter.formatUserObject(userObject) + "," + ValueFormatter.formatInventory(inventory)
				+ "," + ValueFormatter.formatDate(date) + "," + remark + "," + category;
	}
	
	public static List<Transaction> parseTransactions(String idString) {
		List<Transaction> transactions = new ArrayList<Transaction>();
		String decapsulatedString = idString.substring(1, idString.length() - 1);
		if (decapsulatedString.isEmpty())
			return transactions;
		Transactions db = (Transactions) DatabaseFacade.getDatabase("Transactions");
		String[] splitIds = decapsulatedString.split("/");
		for (String eachId : splitIds)
			transactions.add(db.get(db.find(Integer.parseInt(eachId))));
		return transactions;
	}

	public static Transaction parse(String[] record) {
		int id = Integer.parseInt(record[0]);
		double amount = ValueFormatter.parseMoney(record[1]);
		double tax = ValueFormatter.parseRate(record[2]);
		boolean isCredit = Boolean.parseBoolean(record[3]);
		UserObject userObject = ValueFormatter.parseUserObject(record[4]);
		Inventory inventory = ValueFormatter.parseInventory(record[5]);
		Date date = ValueFormatter.parseDate(record[6]);
		String remark = record[7];
		String category = record[8];
		return new Transaction(id, amount, tax, isCredit, userObject, inventory, date, remark, category);
	}
}
