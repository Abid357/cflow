package handlers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import cores.TransactionCategory;
import globals.LOGGER;
import globals._Settings;

public class TransactionCategories {

	private final String[] headers = { "Transaction Category", "isCreditable", "isDebitable" };
	private final String filePath = _Settings.directory + "\\db\\" + "transactionCategories.csv";

	private String getHeaders() {
		String csvHeaders = "";
		for (int i = 0; i < headers.length; i++)
			csvHeaders += headers[i] + ",";
		return csvHeaders.substring(0, csvHeaders.length() - 1);
	}

	public List<TransactionCategory> load() {
		File file = new File(filePath);
		List<TransactionCategory> list = new ArrayList<TransactionCategory>();
		if (!file.exists()) {
			save(list);
			return list;
		} else {
			try {
				Scanner reader = new Scanner(file);
				reader.nextLine(); // remove headers
				while (reader.hasNext()) {
					String record[] = reader.nextLine().split(",");
					String name = record[0];
					boolean isCreditable = Boolean.parseBoolean(record[1]);
					boolean isDebitable = Boolean.parseBoolean(record[2]);
					list.add(new TransactionCategory(name, isCreditable, isDebitable));
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

	public boolean save(List<TransactionCategory> categories) {
		File file = new File(filePath);
		try {
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			writer.print("");
			writer.println(getHeaders());
			for (TransactionCategory category : categories)
				writer.println(category);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.Error.log(e);
			return false;
		}
		return true;
	}
}
