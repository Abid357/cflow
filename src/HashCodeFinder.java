import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import globals.LOGGER;

public class HashCodeFinder {

	private static String USERNAME = "Abid357";
	private static String MAC = "30-9C-23-B5-0D-7E";
	private static boolean PRINT_TO_FILE = false;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Set<String> MACs = new HashSet<String>();
		Set<String> names = new HashSet<String>();
		if (MAC.equals("")) {
			try {
				Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
				while (networks.hasMoreElements()) {
					NetworkInterface network = networks.nextElement();
					byte[] mac = network.getHardwareAddress();
					if (mac != null) {
						StringBuilder sb = new StringBuilder();
						for (int i = 0; i < mac.length; i++) {
							sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
						}
						MACs.add(sb.toString());
						names.add(network.getDisplayName() + " (" + network.getName() + ")");
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOGGER.Error.log(e);
			}
		} else
			MACs.add(MAC);
		Iterator<String> iterator = MACs.iterator();
		while (iterator.hasNext()) {
			MAC = iterator.next();
			String authString = MAC + USERNAME;
			String string = "auth=" + authString.hashCode();
			System.out.println("MAC=" + MAC + " " + string);
		}
		
		if (PRINT_TO_FILE) {
			try {
			File dir = new File(System.getProperty("user.home") + "/Desktop", "info.txt");
			dir.getParentFile().mkdirs();
			dir.createNewFile();
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(dir, false)));
			iterator = MACs.iterator();
			Iterator<String> iterator2 = names.iterator();
			while (iterator.hasNext()) {
				MAC = iterator.next();
				writer.println(iterator2.next());
				writer.println(MAC);
			}
			writer.flush();
			writer.close();
			} catch (Exception e) {
				
			}
		}
	}

}
