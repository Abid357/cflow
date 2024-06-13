package cores;

import globals.ValueFormatter;

public class Inventory {
	private String name;
	private String accountNo;
	private double balance;
	private boolean isNegBal;

	public Inventory(String name, String accountNo, double balance, boolean isNegBal) {
		super();
		this.name = name;
		this.accountNo = accountNo;
		this.balance = balance;
		this.isNegBal = isNegBal;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public boolean isNegBal() {
		return isNegBal;
	}

	public void setNegBal(boolean isNegBal) {
		this.isNegBal = isNegBal;
	}

	@Override
	public String toString() {
			return name + "," + accountNo + "," + ValueFormatter.formatMoneySafely(balance) + "," + isNegBal;
	}

	public static Inventory parse(String[] record) {
		String name = record[0];
		String accountNo = record[1];
		double balance = ValueFormatter.parseMoney(record[2]);
		boolean isNegBal = Boolean.parseBoolean(record[3]);
		if (name.contains("/"))
			return new StoreInventory(name,balance);
		else
			return new Inventory(name, accountNo, balance, isNegBal);
	}
}
