package databases;

import java.util.ArrayList;
import java.util.List;

import cores.Stock;
import cores.Store;
import cores.StoreInventory;
import cores.UserObject;

public class Stores implements Database<Store> {

	private List<Store> list;
	private boolean isDirty;
	private handlers.Stores db;

	public Stores() {
		list = new ArrayList<Store>();
		db = new handlers.Stores();
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
		String name = (String) objects[0];
		UserObject owner = (UserObject) objects[1];
		for (int i = 0; i < list.size(); i++)
			if (list.get(i).getName().equals(name) && list.get(i).getOwner().equals(owner))
				return i;
		return -1;
	}

	@Override
	public boolean remove(Object... objects) {
		String name = (String) objects[0];
		UserObject owner = (UserObject) objects[1];
		for (Store store : list)
			if (store.getName().equals(name) && store.getOwner().equals(owner))
				return list.remove(store);
		return false;
	}

	@Override
	public boolean add(Object... objects) {
		isDirty = true;
		String name = (String) objects[0];
		UserObject owner = (UserObject) objects[1];
		StoreInventory inventory = (StoreInventory) objects[2];
		@SuppressWarnings("unchecked")
		List<Stock> items = (List<Stock>) objects[3];
		return list.add(new Store(name, owner, inventory, items));
	}

	@Override
	public Store get(int index) {
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
			list = new ArrayList<Store>();
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
	public List<Store> getList() {
		// TODO Auto-generated method stub
		return list;
	}
}
