package databases;

import java.util.ArrayList;
import java.util.List;

import cores.Inventory;
import cores.StoreInventory;

public class Inventories implements Database<Inventory> {

	private List<Inventory> list;
	private boolean isDirty;
	private handlers.Inventories db;

	public Inventories() {
		list = new ArrayList<Inventory>();
		db = new handlers.Inventories();
	}

	@Override
	public int find(Object... objects) {
		// TODO Auto-generated method stub
		String name = (String) objects[0];
		String accountNo = "";
		if (objects.length == 2)
			accountNo = (String) objects[1];
		if (accountNo.isEmpty()) {
			for (int i = 0; i < list.size(); i++)
				if (list.get(i).getName().equals(name))
					return i;
		} else {
			for (int i = 0; i < list.size(); i++) {
				boolean negativeIndex = false;
				for (int j = 0; j < name.length(); j++)
					if (list.get(i).getName().indexOf(name.charAt(j)) == -1) {
						negativeIndex = true;
						break;
					}
				if (!negativeIndex && list.get(i).getAccountNo().contains(accountNo))
					return i;
			}
		}
		return -1;
	}

	@Override
	public boolean add(Object... objects) {
		isDirty = true;
		String name = (String) objects[0];
		if (objects.length == 2) {
			double balance = (double) objects[1];
			return list.add(new StoreInventory(name, balance));
		} else {
			String accountNo = (String) objects[1];
			double balance = (double) objects[2];
			boolean isNegBal = (boolean) objects[3];
			return list.add(new Inventory(name, accountNo, balance, isNegBal));
		}
	}

	@Override
	public Inventory get(int index) {
		if (index < 0 || index >= list.size())
			return null;
		else
			return list.get(index);
	}

	@Override
	public boolean remove(Object... objects) {
		String name = (String) objects[0];
		for (Inventory inventory : list)
			if (inventory.getName().equals(name))
				return list.remove(inventory);
		return false;
	}

	@Override
	public boolean loadList() {
		// TODO Auto-generated method stub
		isDirty = false;
		list = db.load();
		if (list != null)
			return true;
		else {
			list = new ArrayList<Inventory>();
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
	public List<Inventory> getList() {
		// TODO Auto-generated method stub
		return list;
	}

	public List<Inventory> getBankInventoryList() {
		List<Inventory> bankList = new ArrayList<Inventory>();
		for (Inventory inventory : list)
			if (inventory.getAccountNo() != null && !inventory.getAccountNo().isEmpty())
				bankList.add(inventory);
		return bankList;
	}

	public List<Inventory> getPersonalInventoryList() {
		List<Inventory> personalList = new ArrayList<Inventory>();
		for (Inventory inventory : list)
			if (inventory.getAccountNo() == null || inventory.getAccountNo().isEmpty())
				personalList.add(inventory);
		return personalList;
	}

	public List<StoreInventory> getStoreInventoryList() {
		List<StoreInventory> storeList = new ArrayList<StoreInventory>();
		for (Inventory inventory : list)
			if (inventory instanceof StoreInventory)
				storeList.add((StoreInventory) inventory);
		return storeList;
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
