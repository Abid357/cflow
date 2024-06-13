package databases;

import java.util.ArrayList;
import java.util.List;

import cores.Item;
import cores.Material;
import cores.Product;
import cores.Service;
import cores.Stock;
import globals.ValueFormatter;
import helpers.ItemType;

public class Items implements Database<Item> {

	private List<Item> list;
	private boolean isDirty;
	private handlers.Items db;

	public Items() {
		list = new ArrayList<Item>();
		db = new handlers.Items();
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
		Item removedItem = null;
		for (Item item : list)
			if (item.getId() == id) {
				isDirty = true;
				removedItem = item;
				break;
			}
		if (removedItem != null) {
			list.remove(removedItem);
			// remove from all stores if necessary
		}
		return removedItem != null;
	}

	@Override
	public boolean add(Object... objects) {
		isDirty = true;
		int id = (int) objects[0];
		ItemType type = (ItemType) objects[1];
		String name = (String) objects[2];
		double price = (double) objects[3];

		if (ItemType.PRODUCT.compareTo(type) == 0) {
			String unit = (String) objects[4];
			double conversion = (double) objects[5];
			String details = (String) objects[6];
			String barcode = (String) objects[7];
			@SuppressWarnings("unchecked")
			List<Stock> components = (List<Stock>) objects[8];
			return list.add(new Product(id, name, price, unit, conversion, details, barcode, components));
		} else if (ItemType.MATERIAL.compareTo(type) == 0) {
			String unit = (String) objects[4];
			double conversion = (double) objects[5];
			String details = (String) objects[6];
			@SuppressWarnings("unchecked")
			List<Stock> components = (List<Stock>) objects[7];
			return list.add(new Material(id, name, price, unit, conversion, details, components));
		} else if (ItemType.SERVICE.compareTo(type) == 0) {
			String details = (String) objects[4];
			return list.add(new Service(id, name, price, details));
		}
		return false;
	}

	@Override
	public Item get(int index) {
		if (index < 0 || index >= list.size())
			return null;
		else
			return list.get(index);
	}

	public boolean findComponents() {
		for (Item item : list)
			if (item.getType().equals(ItemType.PRODUCT))
				for (Stock stock : ((Product) item).getComponents())
					stock.setItem(ValueFormatter.parseItem(stock.getItemId()));
			else if (item.getType().equals(ItemType.MATERIAL))
				for (Stock stock : ((Material) item).getComponents())
					stock.setItem(ValueFormatter.parseItem(stock.getItemId()));
		return true;
	}
	
	@Override
	public boolean loadList() {
		// TODO Auto-generated method stub
		isDirty = false;
		list = db.load();
		if (list != null)
			return findComponents();
		else {
			list = new ArrayList<Item>();
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
	public List<Item> getList() {
		// TODO Auto-generated method stub
		return list;
	}

	public List<Product> getProductList() {
		List<Product> productList = new ArrayList<Product>();
		for (Item item : list)
			if (item.getType().equals(ItemType.PRODUCT))
				productList.add((Product) item);
		return productList;
	}

	public List<Material> getMaterialList() {
		List<Material> materialList = new ArrayList<Material>();
		for (Item item : list)
			if (item.getType().equals(ItemType.MATERIAL))
				materialList.add((Material) item);
		return materialList;
	}
	
	public List<Service> getServiceList() {
		List<Service> serviceList = new ArrayList<Service>();
		for (Item item : list)
			if (item.getType().equals(ItemType.SERVICE))
				serviceList.add((Service) item);
		return serviceList;
	}
}
