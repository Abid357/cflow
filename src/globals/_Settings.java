package globals;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class _Settings {

	public static String directory;
	public static Color backgroundColor;
	public static Color textFieldColor;
	public static Color labelColor;
	public static Color backgroundColorT;
	public static Color textFieldColorT;
	public static Color labelColorT;
	public static String defaultInventory;
	public static String userName;
	public static File settingsDir = new File(System.getProperty("user.home") + "\\cFlow\\settings.txt");

	private static void readSettings() {
		try {
			Scanner reader = new Scanner(settingsDir);
			while (reader.hasNextLine()) {
				String line = reader.nextLine();
				String tokens[] = line.split("=");
				mapSettings(tokens[0], tokens[1]);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void writeSetting() {
		try {
			if (directory == null || directory.equals(""))
				directory = null;
			if (userName == null || userName.equals(""))
				userName = null;
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(settingsDir)));
			writer.println("directory=" + directory);
			writer.println("backgroundColor=" + backgroundColorT.getRed() + "," + backgroundColorT.getGreen() + ","
					+ backgroundColorT.getBlue());
			writer.println(
					"labelColor=" + labelColorT.getRed() + "," + labelColorT.getGreen() + "," + labelColorT.getBlue());
			writer.println("textFieldColor=" + textFieldColorT.getRed() + "," + textFieldColorT.getGreen() + ","
					+ textFieldColorT.getBlue());
			writer.println("defaultInventory=" + defaultInventory);
			writer.println("userName=" + userName);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void locate() {
		JOptionPane.showMessageDialog(null, "Select the folder you wish to make your workspace.", "Setup",
				JOptionPane.INFORMATION_MESSAGE);
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int choice = fileChooser.showOpenDialog(null);
		if (choice == JFileChooser.APPROVE_OPTION) {
			directory = fileChooser.getSelectedFile().getAbsolutePath();
		} else {
			JOptionPane.showMessageDialog(null, "You have cancelled the setup.", "Operation Failed",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	private static void mapSettings(String key, String value) {
		switch (key) {
		case "directory":
			if (value.equals("null"))
				locate();
			else
				directory = value;
			break;
		case "backgroundColor": {
			String colors[] = value.split(",");
			int R = Integer.parseInt(colors[0]);
			int G = Integer.parseInt(colors[1]);
			int B = Integer.parseInt(colors[2]);
			backgroundColor = new Color(R, G, B);
			backgroundColorT = backgroundColor;
			break;
		}
		case "labelColor": {
			String colors[] = value.split(",");
			int R = Integer.parseInt(colors[0]);
			int G = Integer.parseInt(colors[1]);
			int B = Integer.parseInt(colors[2]);
			labelColor = new Color(R, G, B);
			labelColorT = labelColor;
			break;
		}
		case "textFieldColor": {
			String colors[] = value.split(",");
			int R = Integer.parseInt(colors[0]);
			int G = Integer.parseInt(colors[1]);
			int B = Integer.parseInt(colors[2]);
			textFieldColor = new Color(R, G, B);
			textFieldColorT = textFieldColor;
			break;
		}
		case "defaultInventory":
			if (value.equals("null"))
				defaultInventory = null;
			else
				defaultInventory = value;
			break;
		case "userName":
			if (value.equals("null"))
				userName = null;
			else
				userName = value;
		}
	}

	public static void init() {
		if (!settingsDir.exists()) {
			JOptionPane.showMessageDialog(null,
					"cFlow settings are missing or have been corrupted. Please reinstall the software via the installer.",
					"Operation Failed", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		} else {
			readSettings();
			writeSetting();
		}
	}
}
