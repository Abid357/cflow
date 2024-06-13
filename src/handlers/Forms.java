package handlers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import cores.Form;
import globals._Settings;

public class Forms {
	private final String[] headers = { "Directory", "Title", "List" };
	private final String filePath = _Settings.directory + "\\db\\" + "forms.csv";

	private String getHeaders() {
		String csvHeaders = "";
		for (int i = 0; i < headers.length; i++)
			csvHeaders += headers[i] + ",";
		return csvHeaders.substring(0, csvHeaders.length() - 1);
	}
	
	public boolean save(List<Form> list) {
		File file = new File(filePath);
		if (file.exists())
			file.delete();
		try {
			file.getParentFile().mkdirs();
			file.createNewFile();
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			writer.println(getHeaders());
			for (Form form : list)
				writer.println(form.toString());
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public List<Form> load() {
		File file = new File(filePath);
		List<Form> list = new ArrayList<Form>();
		if (!file.exists()) {
			save(list);
			return list;
		} else {
			try {
				Scanner reader = new Scanner(file);
				reader.nextLine(); // remove headers
				while (reader.hasNext())
					list.add(Form.parse(reader.nextLine().split(",")));
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			return list;
		}
	}
}
