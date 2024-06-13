package cards;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import frames.Main;
import frames.MyOptionPane;
import globals._Settings;
import net.miginfocom.swing.MigLayout;

public class Functions extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create the panel.
	 */
	public Functions(Main frame) {
		setBackground(_Settings.backgroundColor);
		setForeground(_Settings.labelColor);
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setLayout(new MigLayout("", "[10][200][grow][200][grow][200][grow][200][10]", "[10][200][10][grow]"));

		JButton btnReports = new JButton("Reports");
		btnReports.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnReports.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnReports.setBackground(_Settings.backgroundColor);
		btnReports.setForeground(_Settings.labelColor);
		add(btnReports, "cell 1 1,grow");

		JButton btnFormBuilder = new JButton("Form Builder");
		btnFormBuilder.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnFormBuilder.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnFormBuilder.setBackground(_Settings.backgroundColor);
		btnFormBuilder.setForeground(_Settings.labelColor);
		btnFormBuilder.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				File file = new File(System.getProperty("user.home") + "\\cFlow\\Form Builder.jar");
				if (file.exists()) {
					try {
						Desktop.getDesktop().open(file);
						System.exit(0);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						new MyOptionPane(
								"The function failed to start. Please try reinstalling the software via the installer.",
								MyOptionPane.ERROR_DIALOG_BOX);
					}
				}
				else
					new MyOptionPane(
							"The function was not found. Please try reinstalling the software via the installer.",
							MyOptionPane.INFORMATION_MESSAGE);
			}
		});
		add(btnFormBuilder, "cell 3 1,grow");

		JButton button_2 = new JButton("More Function");
		button_2.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		button_2.setFont(new Font("Arial Black", Font.PLAIN, 19));
		button_2.setBackground(_Settings.backgroundColor);
		button_2.setForeground(_Settings.labelColor);
		add(button_2, "cell 5 1,grow");

		JButton button_3 = new JButton("More Function");
		button_3.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		button_3.setFont(new Font("Arial Black", Font.PLAIN, 19));
		button_3.setBackground(_Settings.backgroundColor);
		button_3.setForeground(_Settings.labelColor);
		add(button_3, "cell 7 1,grow");

		JLabel lblWorkInProgress = new JLabel("Work in Progress...");
		lblWorkInProgress.setBorder(null);
		lblWorkInProgress.setHorizontalAlignment(SwingConstants.CENTER);
		lblWorkInProgress.setFont(new Font("Arial", Font.PLAIN, 35));
		lblWorkInProgress.setForeground(_Settings.labelColor);
		add(lblWorkInProgress, "cell 1 3 7 1,grow");
	}

}
