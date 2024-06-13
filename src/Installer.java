import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class Installer {
	public static void main(String[] args) {
		JOptionPane.showMessageDialog(null, "Select the installation zip file provided to you.", "Installation",
				JOptionPane.INFORMATION_MESSAGE);
		File destDir = new File(System.getProperty("user.home") + "\\cFlow");
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int choice = fileChooser.showOpenDialog(null);
		if (choice == JFileChooser.APPROVE_OPTION) {
			String zipDir = fileChooser.getSelectedFile().getAbsolutePath();
			if (!zipDir.contains("cFlow.zip")) {
				JOptionPane.showMessageDialog(null, "Please select the correct installation zip file and try again.",
						"Operation Failed", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			} else {
				try {
					destDir.mkdirs();
					FileInputStream fis;
					// buffer for read and write data to file
					byte[] buffer = new byte[1024];
					fis = new FileInputStream(zipDir);
					ZipInputStream zis = new ZipInputStream(fis);
					ZipEntry ze = zis.getNextEntry();
					while (ze != null) {
						String fileName = ze.getName();
						File newFile = new File(destDir.getAbsolutePath() + File.separator + fileName);
						// create directories for sub directories in zip
						new File(newFile.getParent()).mkdirs();
						FileOutputStream fos = new FileOutputStream(newFile);
						int len;
						while ((len = zis.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}
						fos.close();
						// close this ZipEntry
						zis.closeEntry();
						ze = zis.getNextEntry();
					}
					// close last ZipEntry
					zis.closeEntry();
					zis.close();
					fis.close();
					JOptionPane
							.showMessageDialog(null,
									"Thank you for installing! Please run cFlow.exe from the installation folder:\n"
											+ destDir.getAbsolutePath(),
									"Operation Success", JOptionPane.INFORMATION_MESSAGE);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			JOptionPane.showMessageDialog(null, "You have cancelled the installation.", "Operation Failed",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
}
