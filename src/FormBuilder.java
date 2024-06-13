import java.awt.EventQueue;
import java.io.File;

import javax.swing.JOptionPane;

import frames.Main;
import globals._Settings;

public class FormBuilder {
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		File file = new File(System.getProperty("user.home") + "\\cFlow");
		if (!file.exists()) {
			JOptionPane.showMessageDialog(null,
					"cFlow directory not found. Please reinstall the software via the installer.", "Operation Failed",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		} else {
			_Settings.init();

			databases.Forms formDB = new databases.Forms();
			formDB.loadList();

			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						new Main(formDB);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
}
