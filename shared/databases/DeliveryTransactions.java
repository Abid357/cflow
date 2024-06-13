package databases;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cores.DeliveryTransaction;
import cores.Stock;
import cores.Store;
import cores.Transaction;

public class DeliveryTransactions implements Database<DeliveryTransaction> {
	private List<DeliveryTransaction> list;
	private boolean isDirty;
	private handlers.DeliveryTransactions db;

	public DeliveryTransactions() {
		list = new ArrayList<DeliveryTransaction>();
		db = new handlers.DeliveryTransactions();
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
		int id = (int) objects[0];
		for (DeliveryTransaction transaction : list)
			if (transaction.getId() == id)
				return list.remove(transaction);
		return false;
	}

	@Override
	public boolean add(Object... objects) {
		isDirty = true;
		int id = (int) objects[0];
		Date date = (Date) objects[1];
		Store fromStore = (Store) objects[2];
		Store toStore = (Store) objects[3];
		double total = (double) objects[4];
		@SuppressWarnings("unchecked")
		List<Transaction> costs = (List<Transaction>) objects[5];
		@SuppressWarnings("unchecked")
		List<Stock> items = (List<Stock>) objects[6];
		list.add(0,new DeliveryTransaction(id, date, fromStore, toStore, total, costs, items));
		return true;
	}

	@Override
	public DeliveryTransaction get(int index) {
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
			list = new ArrayList<DeliveryTransaction>();
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
	public List<DeliveryTransaction> getList() {
		return list;
	}
}
