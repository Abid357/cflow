package cores;

import java.util.List;

import globals.ValueFormatter;

public class Person extends UserObject {
	private String firstName;
	private String lastName;
	private String nationality;

	public Person(String firstName, String lastName, String nationality, String location, String phone, String email,
			String address, double balance, List<String> categories) {
		super(location, phone, email, address, balance, categories);
		this.firstName = firstName;
		this.lastName = lastName;
		this.nationality = nationality;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getNationality() {
		return nationality;
	}

	public void setNationality(String nationality) {
		this.nationality = nationality;
	}

	@Override
	public String toString() {
		return firstName + "," + lastName + "," + nationality + "," + super.toString();
	}

	public static Person parse(String[] record) {
		String firstName = record[0];
		String lastName = record[1];
		String nationality = record[2];
		String location = record[3];
		String phone = record[4];
		String email = record[5];
		String address = record[6];
		double balance = ValueFormatter.parseMoney(record[7]);
		List<String> categories = UserObject.parseCategories(record[8]);
		return new Person(firstName, lastName, nationality, location, phone, email, address, balance, categories);
	}
}