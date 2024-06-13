package databases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cores.Transaction;
import cores.TransactionCategory;
import globals.DatabaseFacade;

public class TransactionCategories implements Database<TransactionCategory> {

	private Map<String, List<Integer>> map;
	private List<TransactionCategory> list;
	private List<Transaction> transactions;
	private boolean isDirty;
	private handlers.TransactionCategories db;

	public TransactionCategories() {
		map = new HashMap<String, List<Integer>>();
		db = new handlers.TransactionCategories();
	}

	@Override
	public int find(Object... objects) {
		// TODO Auto-generated method stub
		String category = (String) objects[0];
		Iterator<String> iterator = map.keySet().iterator();
		int index = 0;
		while (iterator.hasNext()) {
			if (iterator.next().equals(category))
				return index;
			index++;
		}
		return -1;
	}

	@Override
	public boolean add(Object... objects) {
		// TODO Auto-generated method stub
		isDirty = true;
		String name = (String) objects[0];
		boolean isCreditable = (boolean) objects[1];
		boolean isDebitable = (boolean) objects[2];
		TransactionCategory key = new TransactionCategory(name, isCreditable, isDebitable);
		list.add(key);
		List<Integer> ids = new ArrayList<Integer>();
		return map.put(key.getName(), ids) != null;
	}

	@Override
	public Object get(int index) {
		// TODO Auto-generated method stub
		Iterator<String> iterator = map.keySet().iterator();
		int counter = 0;
		String category = null;
		while (iterator.hasNext()) {
			if (counter == index) {
				category = iterator.next();
				break;
			}
			category = iterator.next();
			counter++;
		}
		return map.get(category);
	}

	@Override
	public boolean remove(Object... objects) {
		// TODO Auto-generated method stub
		String key = (String) objects[0];
		return map.remove(key) != null;
	}

	private void categorize() {
		this.transactions = (List<Transaction>) ((Transactions) DatabaseFacade.getDatabase("Transactions")).getList();
		for (Transaction transaction : transactions)
			map.get(transaction.getCategory()).add(transaction.getId());
	}

	@Override
	public boolean loadList() {
		// TODO Auto-generated method stub
		isDirty = false;
		list = db.load();
		map = new HashMap<String, List<Integer>>();
		if (list != null) {
			for (TransactionCategory category : list)
				map.put(category.getName(), new ArrayList<Integer>());
			categorize();
			return true;
		} else {
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
	public List<TransactionCategory> getList() {
		// TODO Auto-generated method stub
		return list;
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return isDirty;
	}

	@Override
	public void setDirty(boolean isDirty) {
		// TODO Auto-generated method stub
		this.isDirty = isDirty;
	}

}
