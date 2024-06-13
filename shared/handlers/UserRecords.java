package handlers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import cores.UserRecord;
import globals.LOGGER;
import globals._Settings;

public class UserRecords {

	private final String[] headers = { "ID", "User[Name1/Name2]", "Balance" };
	private final String filePath = _Settings.directory + "\\db\\" + "userRecords.csv";

	private String getHeaders() {
		String csvHeaders = "";
		for (int i = 0; i < headers.length; i++)
			csvHeaders += headers[i] + ",";
		return csvHeaders.substring(0, csvHeaders.length() - 1);
	}

	public boolean save(List<UserRecord> list) {
		File file = new File(filePath);
		try {
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			writer.print("");
			writer.println(getHeaders());
			for (UserRecord UserRecord : list)
				writer.println(UserRecord);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.Error.log(e);
			return false;
		}
		return true;
	}

	public List<UserRecord> load(Map<String, Integer> map) {
		File file = new File(filePath);
		List<UserRecord> list = new ArrayList<UserRecord>();
		if (!file.exists()) {
			save(list);
			return list;
		} else {
			try {
				int index = 0;
				String userName = "";
				Scanner reader = new Scanner(file);
				reader.nextLine(); // remove headers
				while (reader.hasNext()) {
					String[] record = reader.nextLine().split(",");
					if (!userName.equals(record[1])) {
						userName = record[1];
						map.put(userName, index);
					}
					list.add(UserRecord.parse(record));
					index++;
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
