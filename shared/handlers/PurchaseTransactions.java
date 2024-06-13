package handlers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import cores.PurchaseTransaction;
import globals.LOGGER;
import globals._Settings;

public class PurchaseTransactions {
	private final String[] headers = { "ID", "Date", "User[Name1/Name2]", "Store", "Discount", "VAT", "Total",
			"PaymentIDs", "CostIDs", "ItemStatus", "PaymentStatus", "LPONo", "ProformaInvoiceNo", "InvoiceNo",
			"Items[]" };
	private final String filePath = _Settings.directory + "\\db\\" + "purchases.csv";

	private String getHeaders() {
		String csvHeaders = "";
		for (int i = 0; i < headers.length; i++)
			csvHeaders += headers[i] + ",";
		return csvHeaders.substring(0, csvHeaders.length() - 1);
	}

	public boolean save(List<PurchaseTransaction> list) {
		File file = new File(filePath);
		try {
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			writer.print("");
			writer.println(getHeaders());
			for (PurchaseTransaction transaction : list)
				writer.println(transaction);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.Error.log(e);
			return false;
		}
		return true;
	}

	public List<PurchaseTransaction> load() {
		File file = new File(filePath);
		List<PurchaseTransaction> list = new ArrayList<PurchaseTransaction>();
		if (!file.exists()) {
			save(list);
			return list;
		} else {
			try {
				Scanner reader = new Scanner(file);
				reader.nextLine(); // remove headers
				while (reader.hasNext()) {
					String[] record = reader.nextLine().split(",");
					list.add(PurchaseTransaction.parse(record));
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
