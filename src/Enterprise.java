import java.awt.EventQueue;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import javax.swing.JOptionPane;

import frames.Auth;
import frames.Main;
import frames.MyOptionPane;
import globals.DatabaseFacade;
import globals.LOGGER;
import globals._Settings;

/**
 *
 * @author Abid-Temp
 */
public class Enterprise {

	private static FileChannel channel;
	private static FileLock lock;

	@SuppressWarnings("resource")
	private static void lockLocally() {
		File file = new File(System.getProperty("user.home"), "cFlow.tmp");
		if (file.exists()) // if exist try to delete it
			file.delete();

		try {
			channel = new RandomAccessFile(file, "rw").getChannel();
			lock = channel.tryLock();
			if (lock == null) {
				channel.close();
				JOptionPane.showMessageDialog(null,
						"Another instance of cFlow is already running. To close the previous instance, go to Task Manager (Ctrl + Shift + Esc on Windows),"
								+ "\nend all processes with the name \"javaw.exe\" and then restart cFlow.",
						"Operation Failed", MyOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		} catch (Exception e) {
			LOGGER.Error.log(e);
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					if (lock != null) {
						lock.release();
						channel.close();
						file.delete();
					}
				} catch (Exception e) {
					LOGGER.Error.log(e);
				}
			}
		});
	}
	
	private static void lockGlobally() {
		File file = new File(_Settings.directory, "lock.tmp");
		try {
			if (!file.createNewFile()) {
				JOptionPane.showMessageDialog(null,
						"Another user is already logged into cFlow. Please log out of other session first.",
						"Operation Failed", MyOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		} catch (Exception e) {
			LOGGER.Error.log(e);
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
						file.delete();
				} catch (Exception e) {
					LOGGER.Error.log(e);
				}
			}
		});
	}


	/**
	 *
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		// TODO code application logic here
		lockLocally(); // avoids multiple instances of the app
		File file = new File(System.getProperty("user.home") + "\\cFlow");
		if (!file.exists()) {
			JOptionPane.showMessageDialog(null,
					"cFlow directory not found. Please reinstall the software via the installer.", "Operation Failed",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		} else {
			_Settings.init();
//				new Auth();
			 Auth.GRANT_ACCESS = true;

			if (Auth.GRANT_ACCESS) {
				new LOGGER();
				LOGGER.LOG_TO_FILE = false;

				lockGlobally();
				
				_Settings.userSignIn("cFlow Enterprise");

				// initialize all databases
				DatabaseFacade.init();
				DatabaseFacade.loadDatabases();

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							new Main();
						} catch (Exception e) {
							LOGGER.Error.log(e);
						}
					}
				});
			}
		}
	}
}
