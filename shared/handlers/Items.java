package handlers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import cores.Item;
import cores.Material;
import cores.Product;
import cores.Service;
import globals.LOGGER;
import globals._Settings;
import helpers.ItemType;

public class Items {

	private final String[] headers = { "ID", "Type", "Name", "Price", "Unit", "Conversion", "Details", "Barcode",
			"Components[]" };
	private static final String filePath = _Settings.directory + "\\db\\" + "items.csv";

	private String getHeaders() {
		String csvHeaders = "";
		for (int i = 0; i < headers.length; i++)
			csvHeaders += headers[i] + ",";
		return csvHeaders.substring(0, csvHeaders.length() - 1);
	}

	public boolean save(List<Item> list) {
		File file = new File(filePath);
		try {
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			writer.print("");
			writer.println(getHeaders());
			for (Item item : list) {
				if (item.getType().equals(ItemType.PRODUCT))
					writer.println((Product) item);
				else if (item.getType().equals(ItemType.MATERIAL))
					writer.println((Material) item);
				else if (item.getType().equals(ItemType.SERVICE))
					writer.println((Service) item);
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.Error.log(e);
			return false;
		}
		return true;
	}

	public List<Item> load() {
		File file = new File(filePath);
		List<Item> list = new ArrayList<Item>();
		if (!file.exists()) {
			save(list);
			return list;
		} else {
			try {
				Scanner reader = new Scanner(file);
				reader.nextLine(); // remove headers
				while (reader.hasNext()) {
					String[] records = reader.nextLine().split(",");
					if (records[1].equals(ItemType.PRODUCT.toString()))
						list.add(Product.parse(records));
					else if (records[1].equals(ItemType.MATERIAL.toString()))
						list.add(Material.parse(records));
					else if (records[1].equals(ItemType.SERVICE.toString()))
						list.add(Service.parse(records));
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
