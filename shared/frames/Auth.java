package frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import globals.LOGGER;
import globals._Settings;
import net.miginfocom.swing.MigLayout;

public class Auth extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtUsername;
	public static boolean GRANT_ACCESS;

	/**
	 * Create the frame.
	 */
	public Auth() {
		super();
		setModal(true);
		setResizable(false);
		setTitle("cFlow Login");

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(100, 100, (int) (screenSize.width * 0.3), (int) (screenSize.height * 0.6));
		contentPane = new JPanel();
		contentPane.setBorder(null);
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(0, 2, 0, 0));

		JPanel panelImage = new JPanel();
		panelImage.setPreferredSize(new Dimension(getWidth(), getHeight()));
		panelImage.setLayout(new BorderLayout(0, 0));
		contentPane.add(panelImage);

		JPanel panelLogin = new JPanel();
		panelLogin.setPreferredSize(new Dimension(getWidth(), getHeight()));
		panelLogin.setBackground(new Color(30, 30, 30));
		contentPane.add(panelLogin);
		panelLogin.setLayout(new MigLayout("", "[100][380,grow][100]", "[130.00][20][40][5][grow][40.00][60.00][40]"));

		JLabel lblUsername = new JLabel("Username");
		lblUsername.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblUsername.setForeground(new Color(225, 165, 30));
		panelLogin.add(lblUsername, "cell 1 1,growx");

		txtUsername = new JTextField();
		txtUsername.setForeground(Color.WHITE);
		txtUsername.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtUsername.setBackground(null);
		txtUsername.setBorder(null);
		panelLogin.add(txtUsername, "cell 1 2,grow");
		txtUsername.setColumns(10);

		JSeparator sepUsername = new JSeparator();
		sepUsername.setForeground(new Color(160, 160, 160));
		panelLogin.add(sepUsername, "cell 1 3,growx");

		JLabel lblStatus = new JLabel("");
		lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
		lblStatus.setFont(new Font("Century Gothic", Font.PLAIN, 14));
		lblStatus.setBackground(null);
		lblStatus.setForeground(new Color(225, 165, 30));
		panelLogin.add(lblStatus, "cell 1 5,growx,aligny top");

		Set<String> MACs = new HashSet<String>();
		JButton btnLogin = new JButton("Login");
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Iterator<String> iterator = MACs.iterator();
				while (iterator.hasNext()) {
					String username = txtUsername.getText().trim();
					String authString = iterator.next() + username;
					for (int i = 0; i < _Settings.auth.length; i++)
						if (authString.hashCode() == _Settings.auth[i]) {
							GRANT_ACCESS = true;
							_Settings.userName = username;
							dispose();
						}
					lblStatus.setText("Incorrect username or unauthorized computer");
				}
			}
		});
		btnLogin.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnLogin.setBackground(new Color(225, 165, 30));
		panelLogin.add(btnLogin, "cell 1 6,grow");

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
				}
			}
		} catch (Exception e) {
			LOGGER.Error.log(e);
			lblStatus.setText("MAC address related exception occurred");
			btnLogin.setEnabled(false);
		}

		pack();

		JLabel lblImage = new JLabel();
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(System.getProperty("user.home") + "\\cFlow\\login.jpg"));
		} catch (IOException e) {
			LOGGER.Error.log(e);
			lblImage.setText("Logo could not be loaded");
		}
		Image dimg = img.getScaledInstance(panelImage.getWidth(), panelImage.getHeight(), Image.SCALE_SMOOTH);
		ImageIcon imageIcon = new ImageIcon(dimg);
		lblImage.setIcon(imageIcon);
		panelImage.add(lblImage, BorderLayout.CENTER);

		getRootPane().setDefaultButton(btnLogin);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}

}
