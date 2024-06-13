package databases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cores.UserObject;
import globals.DatabaseFacade;

public class UserCategories implements Database<String> {

	private Map<String, List<UserObject>> map;
	private List<UserObject> userObjects;
	private boolean isDirty;
	private handlers.UserCategories db;

	public UserCategories() {
		map = new HashMap<String, List<UserObject>>();
		db = new handlers.UserCategories();
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
		String key = (String) objects[0];
		List<UserObject> categories = new ArrayList<UserObject>();
		if (map.put(key, categories) == null)
			return true; // entry newly added
		else
			return false; // entry replaced existing one
	}
	
	public void unmap(UserObject userObject, String category) {
		List<UserObject> userObjects = map.get(category);
		if (userObjects == null)
			return;
		userObjects.remove(userObject);
	}
	
	public void map(UserObject userObject, String category) {
		List<UserObject> userObjects = map.get(category);
		if (userObjects == null)
			userObjects = new ArrayList<UserObject>();
		userObjects.add(userObject);
		//map.put(category, userObjects);
	}

	@Override
	public Object get(int index) {
		// TODO Auto-generated method stub
		Iterator<String> iterator = map.keySet().iterator();
		String category = null;
		int counter = 0;
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
	
	public List<UserObject> get(String category){
		return map.get(category);
	}

	@Override
	public boolean remove(Object... objects) {
		// TODO Auto-generated method stub
		String key = (String) objects[0];
		return map.remove(key) != null;
	}

	public void categorize(List<String> categories) {
		map = new HashMap<String, List<UserObject>>();
		for (String category : categories)
			map.put(category, new ArrayList<UserObject>());
		this.userObjects = (List<UserObject>) ((UserObjects) DatabaseFacade.getDatabase("UserObjects")).getList();
		for (UserObject userObject : userObjects) {
			List<String> userCategories = userObject.getCategories();
			for (String userCategory : userCategories)
				map.get(userCategory).add(userObject);
		}
	}

	@Override
	public boolean loadList() {
		// TODO Auto-generated method stub
		isDirty = false;
		List<String> categories = db.load();
		if (categories != null) {
			categorize(categories);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean saveList() {
		// TODO Auto-generated method stub
		if (isDirty) {
			List<String> list = new ArrayList<String>(map.keySet());
			isDirty = false;
			return db.save(list);
		} else
			return false;
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

	@Override
	public List<String> getList() {
		// TODO Auto-generated method stub
		return new ArrayList<String>(map.keySet());
	}

}
