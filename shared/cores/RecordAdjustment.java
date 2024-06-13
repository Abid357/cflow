package cores;

import java.util.Date;

import globals.ValueFormatter;

public class RecordAdjustment implements ITransaction{
	private int id;
	private double amount;
	private boolean isCredit;
	private UserObject user;
	private Inventory inventory;
	private String remark;
	private Date date;

	public RecordAdjustment(int id, double amount, boolean isCredit, UserObject user, Inventory inventory,
			String remark, Date date) {
		super();
		this.id = id;
		this.amount = amount;
		this.isCredit = isCredit;
		this.date = date;
		this.user = user;
		this.inventory = inventory;
		this.remark = remark;
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

	public boolean isCredit() {
		return isCredit;
	}

	public void setCredit(boolean isCredit) {
		this.isCredit = isCredit;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public UserObject getUser() {
		return user;
	}

	public void setUser(UserObject user) {
		this.user = user;
	}

	public Inventory getInventory() {
		return inventory;
	}

	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Override
	public String toString() {
		String userString = "";
		if (user != null)
			userString = ValueFormatter.formatUserObject(user);
		String inventoryString = "";
		if (inventory != null)
			inventoryString = ValueFormatter.formatInventory(inventory);
		return id + "," + ValueFormatter.formatMoneySafely(amount) + "," + isCredit + "," + userString + ","
				+ inventoryString + "," + remark + "," + ValueFormatter.formatDate(date);
	}

	public static RecordAdjustment parse(String[] record) {
		int id = Integer.parseInt(record[0]);
		double amount = ValueFormatter.parseMoney(record[1]);
		boolean isCredit = Boolean.parseBoolean(record[2]);
		UserObject user = null;
		if (!record[3].isEmpty())
			user = ValueFormatter.parseUserObject(record[3]);
		Inventory inventory = null;
		if (!record[4].isEmpty())
			inventory = ValueFormatter.parseInventory(record[4]);
		String remark = record[5];
		Date date = ValueFormatter.parseDate(record[6]);
		return new RecordAdjustment(id, amount, isCredit, user, inventory, remark, date);
	}

}
