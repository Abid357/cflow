package databases;

import java.util.ArrayList;
import java.util.List;

import cores.Form;

public class Forms {

	private List<Form> list;
	private handlers.Forms db;

	public Forms() {
		list = new ArrayList<Form>();
		db = new handlers.Forms();
	}

	public int find(String title) {
		// TODO Auto-generated method stub
		for (int i = 0; i < list.size(); i++)
			if (list.get(i).getTitle().equals(title))
				return i;
		return -1;
	}

	public boolean add(Form form) {
		// TODO Auto-generated method stub
		return list.add(form);
	}

	public Form get(int index) {
		// TODO Auto-generated method stub
		return list.get(index);
	}

	public boolean remove(String title) {
		// TODO Auto-generated method stub
		for (Form form : list)
			if (form.getTitle().equals(title))
				return list.remove(form);
		return false;
	}

	public boolean loadList() {
		// TODO Auto-generated method stub
		list = db.load();
		if (list != null)
			return true;
		else {
			list = new ArrayList<Form>();
			return false;
		}
	}

	public boolean saveList() {
		// TODO Auto-generated method stub
		return db.save(list);
	}

	public List<Form> getList() {
		// TODO Auto-generated method stub
		return list;
	}

}
