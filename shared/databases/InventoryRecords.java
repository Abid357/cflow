package databases;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cores.Inventory;
import cores.InventoryRecord;
import globals.ValueFormatter;

public class InventoryRecords implements Database<InventoryRecord> {

	private Map<String, Integer> map;
	private List<InventoryRecord> list;
	private boolean isDirty;
	private handlers.InventoryRecords db;

	public InventoryRecords() {
		map = new LinkedHashMap<String, Integer>();
		list = new ArrayList<InventoryRecord>();
		db = new handlers.InventoryRecords();
	}

	public int getInventoryStartIndex(String inventoryName) {
		if (map.containsKey(inventoryName))
			return map.get(inventoryName);
		else
			return -1;
	}

	public int getInventoryEndIndex(String inventoryName) {
		if (map.containsKey(inventoryName)) {
			String[] inventories = map.keySet().toArray(new String[0]);
			int index = 0;
			for (; index < inventories.length; index++)
				if (inventories[index].equals(inventoryName))
					break;
			if (index == inventories.length - 1) // last element or only element in the array
				return list.size() - 1;
			else
				return map.get(inventories[++index]) - 1;
		} else
			return -1;
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
		String id = (String) objects[0];
		Inventory inventory = (Inventory) objects[1];
		for (int i = 0; i < list.size(); i++)
			if (list.get(i).getId().equals(id) && list.get(i).getInventory().equals(inventory))
				return i;
		return -1;
	}

	@Override
	public boolean remove(Object... objects) {
		String id = (String) objects[0];
		Inventory inventory = (Inventory) objects[1];
		int index = find(id, inventory);
		if (index != -1) {
			String inventoryName = ValueFormatter.formatInventory(inventory);
			if (getInventoryStartIndex(inventoryName) == getInventoryEndIndex(inventoryName))
				map.remove(inventoryName);
			list.remove(index);
			for (Map.Entry<String, Integer> entry : map.entrySet())
				if (index < entry.getValue().intValue())
					entry.setValue(entry.getValue().intValue() - 1);
			return true;
		}
		return false;
	}

	@Override
	public boolean add(Object... objects) {
		isDirty = true;
		String id = (String) objects[0];
		Inventory inventory = (Inventory) objects[1];
		double balance = (double) objects[2];

		boolean inventoryFound = false;
		String inventoryName = ValueFormatter.formatInventory(inventory);
		for (Map.Entry<String, Integer> entry : map.entrySet())
			if (entry.getKey().equals(inventoryName)) {
				inventoryFound = true;
				break;
			}
		if (!inventoryFound) {
			map.put(inventoryName, list.size());
			return list.add(new InventoryRecord(id, inventory, balance));
		}

		if (objects.length == 4) {// if index specified, consider insertion, else sequential update only
			int index = (int) objects[3];
			for (Map.Entry<String, Integer> entry : map.entrySet())
				if (index <= entry.getValue().intValue() && !entry.getKey().equals(inventoryName))
					entry.setValue(entry.getValue().intValue() + 1);
			list.add(index, new InventoryRecord(id, inventory, balance));
			return true;
		}

		int index = find(id, inventory);
		if (index != -1) {
			list.set(index, new InventoryRecord(id, inventory, balance));
			return true;
		}
		return false;
	}

	@Override
	public InventoryRecord get(int index) {
		if (index < 0 || index >= list.size())
			return null;
		else
			return list.get(index);
	}

	public Map<String, Integer> getMap() {
		return map;
	}

	@Override
	public boolean loadList() {
		// TODO Auto-generated method stub
		isDirty = false;
		list = db.load(map);
		if (list != null)
			return true;
		else {
			list = new ArrayList<InventoryRecord>();
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
	public List<InventoryRecord> getList() {
		return list;
	}
}
