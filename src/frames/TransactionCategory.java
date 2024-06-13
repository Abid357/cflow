package frames;

import java.awt.Dialog;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import databases.TransactionCategories;
import globals.DatabaseFacade;
import globals.LOGGER;
import globals._Settings;
import net.miginfocom.swing.MigLayout;

public class TransactionCategory extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JButton btnSave;
	private JButton btnClose;
	private JLabel lblName;
	private JTextField txtName;
	private JTextField txtStatus;
	private JRadioButton rdbtnCreditable;
	private JRadioButton rdbtnDebitable;
	private JCheckBox chckbxBoth;

	/**
	 * Create the frame.
	 */
	public TransactionCategory(Window owner) {
		super(owner, "Transaction Category", Dialog.ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		setBounds(100, 100, 850, 700);
		setAutoRequestFocus(true);
		contentPane = new JPanel();
		contentPane.setBackground(_Settings.backgroundColor);
		contentPane.setBorder(null);
		setContentPane(contentPane);
		contentPane
				.setLayout(new MigLayout("", "[10][120,grow][10][150][10][150,grow][10]", "[10][40][10][][10][20][10][60]"));

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
				TransactionCategories db = (TransactionCategories) DatabaseFacade.getDatabase("TransactionCategories");
				String name = txtName.getText().trim();
				boolean isCreditable = rdbtnCreditable.isSelected();
				boolean isDebitable = rdbtnDebitable.isSelected();
				if (chckbxBoth.isSelected()) {
					isCreditable = isDebitable = true;
				}
				if (txtName.getText().isEmpty())
					txtStatus.setText("Category name cannot be empty!");
				else if (db.find(name) == -1) {
					db.add(name, isCreditable, isDebitable);
					txtStatus.setText("New transaction category added!");
					LOGGER.Activity.log("Transaction Category", LOGGER.CREATE, name);
					txtName.setText("");
				} else
					txtStatus.setText("This category already exists!");
			}
		});
		
		rdbtnCreditable = new JRadioButton("Creditable");
		rdbtnCreditable.setBackground(null);
		rdbtnCreditable.setForeground(_Settings.labelColor);
		rdbtnCreditable.setFont(new Font("Arial Black", Font.PLAIN, 17));
		rdbtnCreditable.setHorizontalTextPosition(SwingConstants.LEADING);
		rdbtnCreditable.setHorizontalAlignment(SwingConstants.LEFT);
		rdbtnCreditable.setSelected(true);
		contentPane.add(rdbtnCreditable, "cell 1 3,grow");
		
		rdbtnDebitable = new JRadioButton("Debitable");
		rdbtnDebitable.setBackground(null);
		rdbtnDebitable.setForeground(_Settings.labelColor);
		rdbtnDebitable.setHorizontalAlignment(SwingConstants.LEFT);
		rdbtnDebitable.setHorizontalTextPosition(SwingConstants.LEADING);
		rdbtnDebitable.setFont(new Font("Arial Black", Font.PLAIN, 17));
		contentPane.add(rdbtnDebitable, "cell 3 3,grow");
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(rdbtnCreditable);
		buttonGroup.add(rdbtnDebitable);
		
		chckbxBoth = new JCheckBox("Both");
		chckbxBoth.setBackground(null);
		chckbxBoth.setForeground(_Settings.labelColor);
		chckbxBoth.setFont(new Font("Arial Black", Font.PLAIN, 17));
		chckbxBoth.setHorizontalAlignment(SwingConstants.LEFT);
		chckbxBoth.setHorizontalTextPosition(SwingConstants.LEADING);
		chckbxBoth.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
		        boolean isChecked = ((AbstractButton) e.getSource()).getModel().isSelected();
		        if (isChecked) {
		        	rdbtnCreditable.setEnabled(false);
		        	rdbtnDebitable.setEnabled(false);
		        }
		        else {
		        	rdbtnCreditable.setEnabled(true);
		        	rdbtnDebitable.setEnabled(true);
		        }
			}
		});
		contentPane.add(chckbxBoth, "cell 5 3,grow");
		contentPane.add(btnSave, "cell 5 7,aligny center,grow");

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
		contentPane.add(txtStatus, "cell 3 5 3 1,growx");

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
		contentPane.add(btnClose, "cell 3 7,aligny center,grow");
		
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
