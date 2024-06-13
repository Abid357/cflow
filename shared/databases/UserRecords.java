package databases;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cores.UserObject;
import cores.UserRecord;
import globals.ValueFormatter;

public class UserRecords implements Database<UserRecord> {

	private Map<String, Integer> map;
	private List<UserRecord> list;
	private boolean isDirty;
	private handlers.UserRecords db;

	public UserRecords() {
		map = new LinkedHashMap<String, Integer>();
		list = new ArrayList<UserRecord>();
		db = new handlers.UserRecords();
	}

	public int getUserStartIndex(String userName) {
		if (map.containsKey(userName))
			return map.get(userName);
		else
			return -1;
	}

	public int getUserEndIndex(String userName) {
		if (map.containsKey(userName)) {
			String[] users = map.keySet().toArray(new String[0]);
			int index = 0;
			for (; index < users.length; index++)
				if (users[index].equals(userName))
					break;
			if (index == users.length - 1) // last element or only element in the array
				return list.size() - 1;
			else
				return map.get(users[++index]) - 1;
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
		UserObject userObject = (UserObject) objects[1];
		for (int i = 0; i < list.size(); i++)
			if (list.get(i).getId().equals(id) && list.get(i).getUserObject().equals(userObject))
				return i;
		return -1;
	}

	@Override
	public boolean remove(Object... objects) {
		String id = (String) objects[0];
		UserObject userObject = (UserObject) objects[1];
		int index = find(id, userObject);
		if (index != -1) {
			String userName = ValueFormatter.formatUserObject(userObject);
			if (getUserStartIndex(userName) == getUserEndIndex(userName))
				map.remove(userName);
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
		UserObject userObject = (UserObject) objects[1];
		double balance = (double) objects[2];

		boolean userFound = false;
		String userName = ValueFormatter.formatUserObject(userObject);
		for (Map.Entry<String, Integer> entry : map.entrySet())
			if (entry.getKey().equals(userName)) {
				userFound = true;
				break;
			}
		if (!userFound) {
			map.put(userName, list.size());
			return list.add(new UserRecord(id, userObject, balance));
		}

		if (objects.length == 4) {// if index specified, consider insertion, else sequential update only
			int index = (int) objects[3];
			for (Map.Entry<String, Integer> entry : map.entrySet())
				if (index <= entry.getValue().intValue() && !entry.getKey().equals(userName))
					entry.setValue(entry.getValue().intValue() + 1);
			list.add(index, new UserRecord(id, userObject, balance));
			return true;
		}

		int index = find(id, userObject);
		if (index != -1) {
			list.set(index, new UserRecord(id, userObject, balance));
			return true;
		}
		return false;
	}

	public Map<String, Integer> getMap() {
		return map;
	}

	@Override
	public UserRecord get(int index) {
		if (index < 0 || index >= list.size())
			return null;
		else
			return list.get(index);
	}

	@Override
	public boolean loadList() {
		// TODO Auto-generated method stub
		isDirty = false;
		list = db.load(map);
		if (list != null)
			return true;
		else {
			list = new ArrayList<UserRecord>();
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
	public List<UserRecord> getList() {
		return list;
	}
}
