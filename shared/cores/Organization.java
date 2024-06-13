package cores;

import java.util.List;

import globals.ValueFormatter;

public class Organization extends UserObject {
	private String name;
	private String contactName;

	public Organization(String name, String contactName, String location, String phone, String email, String address, double balance, List<String> categories) {
		super(location, phone, email, address, balance, categories);
		this.name = name;
		this.contactName = contactName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	@Override
	public String toString() {
		return name + "," + contactName + ",," + super.toString();
	}
	
	public static Organization parse(String[] record) {
		String name = record[0];
		String contactName = record[1];
		String location = record[3];
		String phone = record[4];
		String email = record[5];
		String address = record[6];
		double balance = ValueFormatter.parseMoney(record[7]);
		List<String> categories = UserObject.parseCategories(record[8]);
		return new Organization(name, contactName, location, phone, email, address, balance, categories);
	}
}