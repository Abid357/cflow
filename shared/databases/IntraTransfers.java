package databases;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cores.IntraTransfer;
import cores.Inventory;
import globals.ValueFormatter;

public class IntraTransfers implements Database<IntraTransfer> {

	private List<IntraTransfer> list;
	private boolean isDirty;
	private handlers.IntraTransfers db;

	public IntraTransfers() {
		list = new ArrayList<IntraTransfer>();
		db = new handlers.IntraTransfers();
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
		for (IntraTransfer intraTransfer : list)
			if (intraTransfer.getId() == id)
				return list.remove(intraTransfer);
		return false;
	}

	@Override
	public boolean add(Object... objects) {
		isDirty = true;
		int id = (int) objects[0];
		double amount = (double) objects[1];
		Date date = (Date) objects[2];
		Inventory fromInventory = (Inventory) objects[3];
		Inventory toInventory = (Inventory) objects[4];
		String remark = (String) objects[5];
		if (remark == null || remark.isEmpty())
			remark = "Withdraw from " + ValueFormatter.formatInventory(fromInventory) + " & Deposit into "
					+ ValueFormatter.formatInventory(toInventory);
		return list.add(new IntraTransfer(id, amount, date, fromInventory, toInventory, remark));
	}

	@Override
	public IntraTransfer get(int index) {
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
			list = new ArrayList<IntraTransfer>();
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
	public List<IntraTransfer> getList() {
		return list;
	}
}
