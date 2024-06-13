package frames;

import java.awt.Dialog;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import databases.UserCategories;
import globals.DatabaseFacade;
import globals.LOGGER;
import globals._Settings;
import net.miginfocom.swing.MigLayout;

public class UserCategory extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	DefaultListModel<String> defaultListModel;
	private JButton btnSave;
	private JButton btnClose;
	private JLabel lblName;
	private JTextField txtName;
	private JTextField txtStatus;

	/**
	 * Create the frame.
	 */
	public UserCategory(Window owner, DefaultListModel<String> defaultListModel) {
		super(owner, "User Category", Dialog.ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		setBounds(100, 100, 850, 700);
		setAutoRequestFocus(true);
		contentPane = new JPanel();
		contentPane.setBackground(_Settings.backgroundColor);
		contentPane.setBorder(null);
		setContentPane(contentPane);
		contentPane
				.setLayout(new MigLayout("", "[10][120,grow][10][150][10][150,grow][10]", "[10][40][10][20][10][60]"));

		this.defaultListModel = defaultListModel;

		lblName = new JLabel("Category Name");
		lblName.setBorder(null);
		lblName.setHorizontalAlignment(SwingConstants.RIGHT);
		lblName.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblName.setForeground(_Settings.labelColor);
		contentPane.add(lblName, "cell 1 1,grow");

		btnSave = new JButton("SAVE");
		btnSave.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnSave.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnSave.setBackground(_Settings.backgroundColor);
		btnSave.setForeground(_Settings.labelColor);
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UserCategories db = (UserCategories) DatabaseFacade.getDatabase("UserCategories");
				String categoryName = txtName.getText().trim();
				if (txtName.getText().isEmpty())
					txtStatus.setText("Category name cannot be empty!");
				else if (db.find(categoryName) == -1) {
					db.add(categoryName);
					txtStatus.setText("New user category added!");
					LOGGER.Activity.log("User Category", LOGGER.CREATE, categoryName);
					txtName.setText("");
					if (defaultListModel != null)
						defaultListModel.addElement(categoryName);
				} else
					txtStatus.setText("This category already exists!");
			}
		});
		contentPane.add(btnSave, "cell 5 5,aligny center,grow");

		txtName = new JTextField();
		txtName.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtName.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtName.setBackground(null);
		txtName.setForeground(_Settings.textFieldColor);
		txtName.setColumns(10);
		contentPane.add(txtName, "cell 3 1 3 1,grow");

		txtStatus = new JTextField();
		txtStatus.setEditable(false);
		txtStatus.setBackground(null);
		txtStatus.setForeground(_Settings.labelColor);
		txtStatus.setBorder(null);
		txtStatus.setHorizontalAlignment(SwingConstants.RIGHT);
		txtStatus.setFont(new Font("Arial Black", Font.PLAIN, 15));
		txtStatus.setColumns(10);
		contentPane.add(txtStatus, "cell 3 3 3 1,growx");

		btnClose = new JButton("CLOSE");
		btnClose.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnClose.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnClose.setBackground(_Settings.backgroundColor);
		btnClose.setForeground(_Settings.labelColor);
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		contentPane.add(btnClose, "cell 3 5,aligny center,grow");
		
		getRootPane().setDefaultButton(btnSave);
		getRootPane().registerKeyboardAction(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				btnClose.doClick();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

		pack();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}

}
