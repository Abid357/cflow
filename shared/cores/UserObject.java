package cores;

import java.util.Arrays;
import java.util.List;

import globals.ValueFormatter;

public abstract class UserObject {

	private String location;
	private String phone;
	private String email;
	private String address;
	private double balance;
	private List<String> categories;

	public UserObject(String location, String phone, String email, String address, double balance,
			List<String> categories) {
		super();
		this.location = location;
		this.phone = phone;
		this.email = email;
		this.address = address;
		this.balance = balance;
		this.categories = categories;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	@Override
	public String toString() {
		String categoryString = "["; // encapsulate by adding square brackets
		for (int i = 0; i < categories.size(); i++)
			// forward slash is the array element delimiter since , is used in CSV files
			categoryString += categories.get(i) + "/";
		if (categories.size() != 0)
			categoryString = categoryString.substring(0, categoryString.length() - 1);
		categoryString += "]";
		return location + "," + phone + "," + email + "," + address + "," + ValueFormatter.formatMoneySafely(balance) + "," + categoryString;
	}

	public static List<String> parseCategories(String categoryString) {
		// decapsulate by removing square brackets
		String decapsulatedString = categoryString.substring(1, categoryString.length() - 1);
		String[] categories = decapsulatedString.split("/");
		return Arrays.asList(categories);
	}
}