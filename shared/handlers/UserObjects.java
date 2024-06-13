package handlers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import cores.Organization;
import cores.Person;
import cores.UserObject;
import globals.LOGGER;
import globals._Settings;

public class UserObjects {

	private final String[] headers = { "Name1", "Name2", "Nationality", "Location", "Phone", "Email", "Address",
			"Categories[]" };
	private static final String filePath = _Settings.directory + "\\db\\" + "userObjects.csv";

	private String getHeaders() {
		String csvHeaders = "";
		for (int i = 0; i < headers.length; i++)
			csvHeaders += headers[i] + ",";
		return csvHeaders.substring(0, csvHeaders.length() - 1);
	}

	public boolean save(List<UserObject> list) {
		File file = new File(filePath);
		try {
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			writer.print("");
			writer.println(getHeaders());
			for (UserObject userObject : list)
				if (userObject instanceof Person)
					writer.println((Person) userObject);
				else
					writer.println((Organization) userObject);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.Error.log(e);
			return false;
		}
		return true;
	}

	public List<UserObject> load() {
		File file = new File(filePath);
		List<UserObject> list = new ArrayList<UserObject>();
		if (!file.exists()) {
			save(list);
			return list;
		} else {
			try {
				Scanner reader = new Scanner(file);
				reader.nextLine(); // remove headers
				while (reader.hasNext()) {
					String[] records = reader.nextLine().split(",");
					if (records[2].equals("")) // check if nationality field is empty
						list.add(Organization.parse(records));
					else
						list.add(Person.parse(records));
				}
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.Error.log(e);
				return null;
			}
			return list;
		}
	}
}
