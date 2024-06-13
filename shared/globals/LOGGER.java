package globals;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import frames.MyOptionPane;
import helpers.AdvancedEncryptionStandard;

public class LOGGER {

	public static boolean LOG_TO_FILE = true;
	public static final File errorFile = new File(_Settings.directory + "\\logs_error.txt");
	public static final File activityFile = new File(_Settings.directory + "\\logs_activity.txt");
	public static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");

	public static final String CREATE = "created";
	public static final String ADD = "added";
	public static final String DELETE = "deleted";
	public static final String UPDATE = "updated";
	public static final String DELIVER = "delivered";
	public static final String COMPLETE = "completed";
	public static final int IN = 0;
	public static final int OUT = 1;

	public static AdvancedEncryptionStandard AES = new AdvancedEncryptionStandard(
			AdvancedEncryptionStandard.convert("MZygpewJsCpRrfOr"));

	public static class Error {

		public static void log(Exception e) {
			if (LOG_TO_FILE) {
				try {
					if (!errorFile.exists()) {
						errorFile.getParentFile().mkdirs();
						errorFile.createNewFile();
						Files.setAttribute(errorFile.toPath(), "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
					}
					PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(errorFile, true)));
					writer.println("=========================");
					writer.println("[" + sdf.format(new Date()) + "]");
					e.printStackTrace(writer);
					writer.flush();
					writer.close();
					new MyOptionPane("An error has occurred. Please check the error logs.", MyOptionPane.ERROR_DIALOG_BOX);
				} catch (IOException ioe) {
					// TODO Auto-generated catch block
					ioe.printStackTrace();
				}
			} else
				e.printStackTrace();
		}

		public static void log(String string) {
			try {
				if (!errorFile.exists()) {
					errorFile.getParentFile().mkdirs();
					errorFile.createNewFile();
					Files.setAttribute(errorFile.toPath(), "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
				}
				PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(errorFile, true)));
				writer.println("=========================");
				writer.println("[" + sdf.format(new Date()) + "]");
				writer.println(string);
				writer.flush();
				writer.close();
				new MyOptionPane("An error has occurred. Please check the error logs.", MyOptionPane.ERROR_DIALOG_BOX);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log(e);
			}
		}

		public static String getLogByDate(Date date) {
			StringBuffer buffer = new StringBuffer();
			boolean doBuffer = false;
			try {
				Scanner reader = new Scanner(LOGGER.errorFile);
				while (reader.hasNext()) {
					String line = reader.nextLine();
					if (line.charAt(0) == '=' && line.length() == 25) {
						if (doBuffer) {
							doBuffer = false;
						}
					} else {
						if (doBuffer) {
							buffer.append(line);
							buffer.append('\n');
						} else if (line.charAt(0) == '[') {
							String dateString = line.substring(1, 24); // indexOf(']')
							Date entryDate = LOGGER.sdf.parse(dateString);
							entryDate = ValueFormatter.setTimeToZero(entryDate);
							if (entryDate.equals(date)) {
								doBuffer = true;
								buffer.append("=========================");
								buffer.append('\n');
								buffer.append(line);
								buffer.append('\n');
							}
						}
					}
				}
				reader.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log("Error log file has been either moved or deleted.");
			}
			return buffer.toString();
		}
	}

	public static class Activity {

		public static void log(String prefix, String action) {
			log(prefix, action, null, null);
		}

		public static void log(String prefix, String action, String detail) {
			log(prefix, action, detail, null);
		}

		private static void checkFile() {
			try {
				if (!activityFile.exists()) {
					activityFile.getParentFile().mkdirs();
					activityFile.createNewFile();
					Files.setAttribute(activityFile.toPath(), "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOGGER.Error.log(e);
			}
		}

		public static void log(String prefix, String action, String detail, String suffix) {
			try {
				checkFile();
				
				PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(activityFile, true)));
				String plainText = "";
				plainText += ("[" + sdf.format(new Date()) + "] ");
				plainText += (prefix + " has been " + action);
				if (suffix != null)
					plainText += (", associated with " + suffix);
				if (detail != null)
					plainText += (": " + detail);

				String cipherText = AES.encrypt(plainText);
				writer.println(cipherText);
				writer.flush();
				writer.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOGGER.Error.log("Activity log file has been either moved or deleted.");
				checkFile();
			}
		}

		public static void userSign(int action, String username, String app, String hostname, String ip) {
			try {
				checkFile();

				PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(activityFile, true)));
				String plainText = "";
				if (action == IN) {
					writer.println("=========================");
					plainText += ("[" + sdf.format(new Date()) + "] ");
					plainText += (username + " has logged in to " + app);
					plainText += (" [hostname: " + hostname + ", hostIP: " + ip + "]");
					String cipherText = AES.encrypt(plainText);
					writer.println(cipherText);
				} else {
					plainText += ("[" + sdf.format(new Date()) + "] ");
					plainText += (username + " has logged out of " + app);
					String cipherText = AES.encrypt(plainText);
					writer.println(cipherText);
					writer.println("=========================");
				}

				writer.flush();
				writer.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOGGER.Error.log("Activity log file has been either moved or deleted.");
				checkFile();
			}
		}

		public static String getLogByDate(Date date) {
			StringBuffer buffer = new StringBuffer();
			boolean doBuffer = false;
			try {
				Scanner reader = new Scanner(LOGGER.activityFile);
				while (reader.hasNext()) {
					String line = reader.nextLine();
					if (line.charAt(0) == '=' && line.length() == 25) {
						if (doBuffer) {
							buffer.append(line);
							buffer.append('\n');
							doBuffer = false;
						}
					} else {
						if (doBuffer) {
							line = LOGGER.AES.decrypt(line);
							buffer.append(line);
							buffer.append('\n');
						} else {
							line = LOGGER.AES.decrypt(line);
							String dateString = line.substring(1, 24); // indexOf(']')
							Date entryDate = LOGGER.sdf.parse(dateString);
							entryDate = ValueFormatter.setTimeToZero(entryDate);
							if (entryDate.equals(date)) {
								doBuffer = true;
								buffer.append("=========================");
								buffer.append('\n');
								buffer.append(line);
								buffer.append('\n');
							}
						}
					}
				}
				reader.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOGGER.Error.log("Activity log file has been either moved or deleted.");
				checkFile();
			}
			return buffer.toString();
		}
	}
}
