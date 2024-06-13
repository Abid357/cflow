package databases;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cores.Inventory;
import cores.Transaction;
import cores.UserObject;

public class Transactions implements Database<Transaction> {

	private List<Transaction> list;
	private boolean isDirty;
	private handlers.Transactions db;

	public Transactions() {
		list = new ArrayList<Transaction>();
		db = new handlers.Transactions();
	}

	public int maxID() {
		int max = 0;
		if (!list.isEmpty()) {
			max = list.get(0).getId();
			for (int i = 1; i < list.size(); i++)
				if (list.get(i).getId() > max)
					max = list.get(i).getId();
		}
		return max;
	}

	@Override
	public boolean isDirty() {
		return isDirty;
	}

	@Override
	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}

	@Override
	public int find(Object... objects) {
		int id = (int) objects[0];
		for (int i = 0; i < list.size(); i++)
			if (list.get(i).getId() == id)
				return i;
		return -1;
	}

	@Override
	public boolean remove(Object... objects) {
		isDirty = true;
		int id = (int) objects[0];
		for (Transaction transaction : list)
			if (transaction.getId() == id)
				return list.remove(transaction);
		return false;
	}

	@Override
	public boolean add(Object... objects) {
		isDirty = true;
		int id = (int) objects[0];
		double amount = (double) objects[1];
		double tax = (double) objects[2];
		boolean isCredit = (boolean) objects[3];
		UserObject userObject = (UserObject) objects[4];
		Inventory inventory = (Inventory) objects[5];
		Date date = (Date) objects[6];
		String remark = (String) objects[7];
		String category = (String) objects[8];
		list.add(0, new Transaction(id, amount, tax, isCredit, userObject, inventory, date, remark, category));
		return true;
	}

	@Override
	public Transaction get(int index) {
		if (index < 0 || index >= list.size())
			return null;
		else
			return list.get(index);
	}

	@Override
	public boolean loadList() {
		// TODO Auto-generated method stub
		isDirty = false;
		list = db.load();
		if (list != null)
			return true;
		else {
			list = new ArrayList<Transaction>();
			return false;
		}
	}

	@Override
	public boolean saveList() {
		// TODO Auto-generated method stub
		if (isDirty) {
			isDirty = false;
			return db.save(list);
		} else
			return false;
	}

	@Override
	public List<Transaction> getList() {
		return list;
	}
}
