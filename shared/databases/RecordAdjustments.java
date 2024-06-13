package databases;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cores.Inventory;
import cores.RecordAdjustment;
import cores.UserObject;

public class RecordAdjustments implements Database<RecordAdjustment> {

	private List<RecordAdjustment> list;
	private boolean isDirty;
	private handlers.RecordAdjustments db;

	public RecordAdjustments() {
		list = new ArrayList<RecordAdjustment>();
		db = new handlers.RecordAdjustments();
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
		for (RecordAdjustment transaction : list)
			if (transaction.getId() == id)
				return list.remove(transaction);
		return false;
	}

	@Override
	public boolean add(Object... objects) {
		isDirty = true;
		int id = (int) objects[0];
		double amount = (double) objects[1];
		boolean isCredit = (boolean) objects[2];
		UserObject userObject = (UserObject) objects[3];
		Inventory inventory = (Inventory) objects[4];
		String remark = (String) objects[5];
		Date date = (Date) objects[6];
		list.add(0, new RecordAdjustment(id, amount, isCredit, userObject, inventory, remark, date));
		return true;
	}

	@Override
	public RecordAdjustment get(int index) {
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
			list = new ArrayList<RecordAdjustment>();
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
	public List<RecordAdjustment> getList() {
		return list;
	}
}
