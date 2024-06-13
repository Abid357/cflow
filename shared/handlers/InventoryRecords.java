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

import cores.InventoryRecord;
import globals.LOGGER;
import globals._Settings;

public class InventoryRecords {

	private final String[] headers = { "ID", "Inventory", "Balance" };
	private final String filePath = _Settings.directory + "\\db\\" + "inventoryRecords.csv";

	private String getHeaders() {
		String csvHeaders = "";
		for (int i = 0; i < headers.length; i++)
			csvHeaders += headers[i] + ",";
		return csvHeaders.substring(0, csvHeaders.length() - 1);
	}

	public boolean save(List<InventoryRecord> list) {
		File file = new File(filePath);
		try {
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			writer.print("");
			writer.println(getHeaders());
			for (InventoryRecord inventoryRecord : list)
				writer.println(inventoryRecord);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.Error.log(e);
			return false;
		}
		return true;
	}

	public List<InventoryRecord> load(Map<String, Integer> map) {
		File file = new File(filePath);
		List<InventoryRecord> list = new ArrayList<InventoryRecord>();
		if (!file.exists()) {
			save(list);
			return list;
		} else {
			try {
				int index = 0;
				String inventoryName = "";
				Scanner reader = new Scanner(file);
				reader.nextLine(); // remove headers
				while (reader.hasNext()) {
					String[] record = reader.nextLine().split(",");
					if (!inventoryName.equals(record[1])) {
						inventoryName = record[1];
						map.put(inventoryName, index);
					}
					list.add(InventoryRecord.parse(record));
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
