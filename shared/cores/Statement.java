package cores;

import java.util.Date;

public class Statement {
	private String description;
	private Date date;
	private boolean isCredit;
	private double amount;
	private double balance;

	public Statement(String description, Date date, boolean isCredit, double amount, double balance) {
		super();
		this.description = description;
		this.date = date;
		this.isCredit = isCredit;
		this.amount = amount;
		this.balance = balance;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public boolean isCredit() {
		return isCredit;
	}

	public void setCredit(boolean isCredit) {
		this.isCredit = isCredit;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	@Override
	public String toString() {
		return "Statement [description=" + description + ", date=" + date + ", isCredit=" + isCredit + ", amount="
				+ amount + ", balance=" + balance + "]";
	}

}
