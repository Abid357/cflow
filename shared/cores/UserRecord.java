package cores;

import globals.ValueFormatter;

public class UserRecord {
	private String id;
	private UserObject userObject;
	private double balance;

	public UserRecord(String id, UserObject userObject, double balance) {
		super();
		this.id = id;
		this.userObject = userObject;
		this.balance = balance;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public UserObject getUserObject() {
		return userObject;
	}

	public void setUserObject(UserObject userObject) {
		this.userObject = userObject;
	}

	@Override
	public String toString() {
		return id + "," + ValueFormatter.formatUserObject(userObject) + "," + ValueFormatter.formatMoneySafely(balance);
	}

	public static UserRecord parse(String[] record) {
		String id = record[0];
		UserObject userObject = ValueFormatter.parseUserObject(record[1]);
		double balance = Double.parseDouble(record[2]);
		return new UserRecord(id, userObject, balance);
	}
}
