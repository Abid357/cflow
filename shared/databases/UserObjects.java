package databases;

import java.util.ArrayList;
import java.util.List;

import cores.Organization;
import cores.Person;
import cores.UserObject;
import globals.DatabaseFacade;

public class UserObjects implements Database<UserObject> {

	private List<UserObject> list;
	private boolean isDirty;
	private handlers.UserObjects db;

	public UserObjects() {
		list = new ArrayList<UserObject>();
		db = new handlers.UserObjects();
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
		String name1 = (String) objects[0];
		String name2 = (String) objects[1];
		for (int i = 0; i < list.size(); i++)
			if (list.get(i) instanceof Person) {
				Person person = (Person) list.get(i);
				if (person.getFirstName().equals(name1) && person.getLastName().equals(name2))
					return i;
			} else {
				Organization organization = (Organization) list.get(i);
				if (organization.getName().equals(name1) && organization.getContactName().equals(name2))
					return i;
			}
		return -1;
	}

	@Override
	public boolean remove(Object... objects) {
		String name1 = (String) objects[0];
		String name2 = (String) objects[1];
		UserObject removedUserObject = null;
		for (UserObject userObject : list)
			if (userObject instanceof Person) {
				Person person = (Person) userObject;
				if (person.getFirstName().equals(name1) && person.getLastName().equals(name2)) {
					isDirty = true;
					removedUserObject = userObject;
					break;
				}
			} else {
				Organization organization = (Organization) userObject;
				if (organization.getName().equals(name1) && organization.getContactName().equals(name2)) {
					isDirty = true;
					removedUserObject = userObject;
					break;
				}
			}
		if (removedUserObject != null) {
			list.remove(removedUserObject);
			UserCategories db = (UserCategories) DatabaseFacade.getDatabase("UserCategories");
			for (String category : removedUserObject.getCategories()) {
				db.unmap(removedUserObject, category);
			}
		}
		return removedUserObject != null;
	}

	@Override
	public boolean add(Object... objects) {
		isDirty = true;
		UserObject userObject = null;
		boolean isAdded = false;
		if (objects.length == 9) { // check if Person
			String firstName = (String) objects[0];
			String lastName = (String) objects[1];
			String nationality = (String) objects[2];
			String location = (String) objects[3];
			String phone = (String) objects[4];
			String email = (String) objects[5];
			String address = (String) objects[6];
			double balance = (double) objects[7];
			@SuppressWarnings("unchecked")
			List<String> categories = (List<String>) objects[8];
			userObject = new Person(firstName, lastName, nationality, location, phone, email, address, balance,
					categories);
			isAdded = list.add(userObject);
		} else {
			String name = (String) objects[0];
			String contactName = (String) objects[1];
			String location = (String) objects[2];
			String phone = (String) objects[3];
			String email = (String) objects[4];
			String address = (String) objects[5];
			double balance = (double) objects[6];
			@SuppressWarnings("unchecked")
			List<String> categories = (List<String>) objects[7];
			userObject = new Organization(name, contactName, location, phone, email, address, balance, categories);
			isAdded = list.add(userObject);
		}
		if (isAdded) {
			UserCategories db = (UserCategories) DatabaseFacade.getDatabase("UserCategories");
			for (String category : userObject.getCategories())
				db.map(userObject, category);
		}
		return isAdded;
	}

	@Override
	public UserObject get(int index) {
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
			list = new ArrayList<UserObject>();
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
	public List<UserObject> getList() {
		// TODO Auto-generated method stub
		return list;
	}

	public List<Person> getPersonList() {
		List<Person> personList = new ArrayList<Person>();
		for (UserObject userObject : list)
			if (userObject instanceof Person)
				personList.add((Person) userObject);
		return personList;
	}

	public List<Organization> getOrganizationList() {
		List<Organization> organizationList = new ArrayList<Organization>();
		for (UserObject userObject : list)
			if (userObject instanceof Organization)
				organizationList.add((Organization) userObject);
		return organizationList;
	}
}
