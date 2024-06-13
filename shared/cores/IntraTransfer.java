package cores;

import java.util.Date;

import globals.ValueFormatter;

public class IntraTransfer implements Comparable<IntraTransfer>, ITransaction {

	private int id;
	private double amount;
	private Date date;
	private Inventory fromInventory;
	private Inventory toInventory;
	private String remark;

	public IntraTransfer(int id, double amount, Date date, Inventory fromInventory, Inventory toInventory,
			String remark) {
		super();
		this.id = id;
		this.amount = amount;
		this.date = date;
		this.fromInventory = fromInventory;
		this.toInventory = toInventory;
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

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Inventory getFromInventory() {
		return fromInventory;
	}

	public void setFromInventory(Inventory fromInventory) {
		this.fromInventory = fromInventory;
	}

	public Inventory getToInventory() {
		return toInventory;
	}

	public void setToInventory(Inventory toInventory) {
		this.toInventory = toInventory;
	}

	@Override
	public int compareTo(IntraTransfer intraTransfer) {
		int comparedId = intraTransfer.getId();
		/* For Ascending order */
		return this.id - comparedId;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Override
	public String toString() {
		return id + "," + ValueFormatter.formatMoneySafely(amount) + "," + ValueFormatter.formatDate(date) + ","
				+ ValueFormatter.formatInventory(fromInventory) + "," + ValueFormatter.formatInventory(toInventory) + "," + remark;
	}

	public static IntraTransfer parse(String[] record) {
		int id = Integer.parseInt(record[0]);
		double amount = ValueFormatter.parseMoney(record[1]);
		Date date = ValueFormatter.parseDate(record[2]);
		Inventory fromInventory = ValueFormatter.parseInventory(record[3]);
		Inventory toInventory = ValueFormatter.parseInventory(record[4]);
		String remark = null;
		if (record.length == 6)
			remark = record[5];
		return new IntraTransfer(id, amount, date, fromInventory, toInventory, remark);
	}

	@Override
	public boolean isCredit() {
		// TODO Auto-generated method stub
		return false;
	}
}
