package databases;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cores.Production;
import cores.Stock;
import cores.Store;
import cores.Transaction;
import helpers.TransactionStatus;

public class Productions implements Database<Production> {
	private List<Production> list;
	private boolean isDirty;
	private handlers.Productions db;

	public Productions() {
		list = new ArrayList<Production>();
		db = new handlers.Productions();
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
		int id = -1;
		try {
			id = (int) objects[0];
		} catch (ClassCastException cce) {
			// TODO Auto-generated catch block
			String alternativeId = (String) objects[0];
			for (int i = 0; i < list.size(); i++)
				if (list.get(i).getAlternativeId().equals(alternativeId))
					return i;
			return -1;
		}
		for (int i = 0; i < list.size(); i++)
			if (list.get(i).getId() == id)
				return i;
		return -1;

	}

	@Override
	public boolean remove(Object... objects) {
		int id = (int) objects[0];
		for (Production transaction : list)
			if (transaction.getId() == id)
				return list.remove(transaction);
		return false;
	}

	@Override
	public boolean add(Object... objects) {
		isDirty = true;
		int id = (int) objects[0];
		String alternativeId = (String) objects[1];
		Date startDate = (Date) objects[2];
		Date endDate = (Date) objects[3];
		Stock product = (Stock) objects[4];
		Store productStore = (Store) objects[5];
		String productUnit = (String) objects[6];
		@SuppressWarnings("unchecked")
		List<Stock> materials = (List<Stock>) objects[7];
		@SuppressWarnings("unchecked")
		List<Store> materialStores = (List<Store>) objects[8];
		@SuppressWarnings("unchecked")
		List<Transaction> costs = (List<Transaction>) objects[9];
		TransactionStatus status = (TransactionStatus) objects[10];
		list.add(0, new Production(id, alternativeId, startDate, endDate, product, productStore, productUnit, materials,
				materialStores, costs, status));
		return true;
	}

	@Override
	public Production get(int index) {
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
			list = new ArrayList<Production>();
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
	public List<Production> getList() {
		return list;
	}
}
