package frames;

import java.awt.Dialog;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import databases.Inventories;
import globals.DatabaseFacade;
import globals.LOGGER;
import globals.ValueFormatter;
import globals._Settings;
import net.miginfocom.swing.MigLayout;

public class Inventory extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JButton btnSave;
	private JButton btnClose;
	private JToggleButton tglbtnBank;
	private JToggleButton tglbtnPersonal;
	private JLabel lblName;
	private boolean isNameEmpty = true, isAccountEmpty, isDuplicateKey, isAmountValid = true, isAccountValid = true;
	private JTextField txtName, txtStatus;
	private JTextField txtAccountNo;
	private JLabel lblInitial;
	private JLabel lblBalance;
	private JTextField txtBalance;
	private JLabel lblAccount;
	private JCheckBox chckbxNegBal;

	private void reset() {
		tglbtnPersonal.setSelected(true);
		tglbtnBank.setSelected(false);
		lblName.setText("Name");
		txtName.setText("");
		txtAccountNo.setText("");
		txtAccountNo.setEnabled(false);
		txtBalance.setText("0.00");
		isNameEmpty = isAccountEmpty = true;
		isDuplicateKey = isAmountValid = isAccountValid = false;
		txtStatus.setText("");
		btnSave.setEnabled(false);
	}

	private boolean isDuplicateKey() {
		int index = -1;
		Inventories db = (Inventories) DatabaseFacade.getDatabase("Inventories");
		if (tglbtnBank.isSelected())
			index = db.find(txtName.getText(), txtAccountNo.getText());
		else
			index = db.find(txtName.getText(), "");
		if (index == -1) {
			txtStatus.setText("");
			return false;
		} else {
			txtStatus.setText("Record already exists!");
			return true;
		}
	}

	private boolean isFieldEmpty(JTextField field) {
		if (field.getText().equals("")) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isAccountValid() {
		return Pattern.compile("[0-9]{4,}").matcher(txtAccountNo.getText()).matches();
	}
	
	private boolean isAmountValid() {
		return Pattern.compile("([0-9]{1,3}(\\,)?)*(\\.[0-9]*)?").matcher(txtBalance.getText()).matches();
	}

	private void validateButton() {
		if (isNameEmpty)
			txtStatus.setText("Name cannot be empty!");
		else if (isAccountEmpty && tglbtnBank.isSelected())
			txtStatus.setText("Account number cannot be empty!");
		else if (!isAccountValid && tglbtnBank.isSelected())
			txtStatus.setText("Enter at least a 4-digit account number!");
		else if (!isAmountValid)
			txtStatus.setText("Invalid amount value!");
		else
			txtStatus.setText("");
		boolean specialBoolean = true;
		if (tglbtnBank.isSelected())
			specialBoolean = !isAccountEmpty && isAccountValid;
		if ((tglbtnPersonal.isSelected() || specialBoolean) && !isNameEmpty && isAmountValid)
			isDuplicateKey = isDuplicateKey();
		if (!isNameEmpty && isAmountValid && !isDuplicateKey && specialBoolean)
			btnSave.setEnabled(true);
		else
			btnSave.setEnabled(false);
	}

	/**
	 * Create the frame.
	 */
	public Inventory(Window owner) {
		super(owner, "Inventory", Dialog.ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		setBounds(100, 100, 850, 700);
		contentPane = new JPanel();
		contentPane.setBackground(_Settings.backgroundColor);
		contentPane.setBorder(null);
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[10,grow][100][10][40,grow][70][20][60][10][150,grow][10]", "[60,grow][10][40][10][40][10][30][30][10][40][10][20][10][60,grow]"));

		tglbtnBank = new JToggleButton("BANK");
		tglbtnBank.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		tglbtnBank.setFont(new Font("Arial Black", Font.PLAIN, 19));
		tglbtnBank.setBackground(_Settings.backgroundColor);
		tglbtnBank.setForeground(_Settings.labelColor);
		tglbtnBank.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				lblName.setText("Bank Name");
				txtAccountNo.setEnabled(true);
				isAccountEmpty = isFieldEmpty(txtAccountNo);
				isAccountValid = isAccountValid();
				chckbxNegBal.setSelected(false);
				chckbxNegBal.setEnabled(false);
				validateButton();
			}
		});

		tglbtnPersonal = new JToggleButton("PERSONAL");
		tglbtnPersonal.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		tglbtnPersonal.setFont(new Font("Arial Black", Font.PLAIN, 19));
		tglbtnPersonal.setBackground(_Settings.backgroundColor);
		tglbtnPersonal.setForeground(_Settings.labelColor);
		tglbtnPersonal.setSelected(true);
		tglbtnPersonal.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				lblName.setText("Name");
				txtAccountNo.setEnabled(false);
				chckbxNegBal.setEnabled(true);
				validateButton();
			}
		});
		contentPane.add(tglbtnPersonal, "cell 1 0 4 1,grow");
		contentPane.add(tglbtnBank, "cell 6 0 3 1,grow");

		lblName = new JLabel("Name");
		lblName.setBorder(null);
		lblName.setHorizontalAlignment(SwingConstants.RIGHT);
		lblName.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblName.setForeground(_Settings.labelColor);
		lblName.setBackground(null);
		contentPane.add(lblName, "cell 0 2 2 1,grow");

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(tglbtnPersonal);
		buttonGroup.add(tglbtnBank);

		txtName = new JTextField();
		txtName.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtName.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtName.setBackground(null);
		txtName.setForeground(_Settings.textFieldColor);
		txtName.setColumns(10);
		txtName.setHorizontalAlignment(SwingConstants.CENTER);
		txtName.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				isNameEmpty = isFieldEmpty(txtName);
				validateButton();
			}

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub

			}
		});
		contentPane.add(txtName, "cell 3 2 6 1,grow");

		lblAccount = new JLabel("Account");
		lblAccount.setHorizontalAlignment(SwingConstants.RIGHT);
		lblAccount.setForeground(_Settings.labelColor);
		lblAccount.setBackground(null);
		lblAccount.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblAccount.setBorder(null);
		contentPane.add(lblAccount, "cell 1 4,grow");

		txtAccountNo = new JTextField();
		txtAccountNo.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtAccountNo.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtAccountNo.setBackground(null);
		txtAccountNo.setForeground(_Settings.textFieldColor);
		txtAccountNo.setColumns(10);
		txtAccountNo.setHorizontalAlignment(SwingConstants.CENTER);
		txtAccountNo.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {
				// TODO Auto-generated method stub
				isAccountEmpty = isFieldEmpty(txtAccountNo);
				if (!isAccountEmpty)
					isAccountValid = isAccountValid();
				validateButton();
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				// TODO Auto-generated method stub

			}
		});
		contentPane.add(txtAccountNo, "cell 3 4 6 1,grow");

		lblInitial = new JLabel("Initial");
		lblInitial.setVerticalAlignment(SwingConstants.BOTTOM);
		lblInitial.setHorizontalAlignment(SwingConstants.RIGHT);
		lblInitial.setForeground(_Settings.labelColor);
		lblInitial.setBackground(null);
		lblInitial.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblInitial.setBorder(null);
		contentPane.add(lblInitial, "cell 1 6,grow");

		txtBalance = new JTextField("0.00");
		txtBalance.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtBalance.setFont(new Font("Century Gothic", Font.BOLD, 24));
		txtBalance.setBackground(null);
		txtBalance.setForeground(_Settings.textFieldColor);
		txtBalance.setColumns(10);
		txtBalance.setHorizontalAlignment(SwingConstants.CENTER);
		txtBalance.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {
				// TODO Auto-generated method stub
				isAmountValid = isAmountValid();
				if (isAmountValid) {
					double amount = ValueFormatter.parseMoney(txtBalance.getText());
					txtBalance.setText(ValueFormatter.formatMoney(amount));
				}
				validateButton();
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				// TODO Auto-generated method stub
				txtBalance.selectAll();
			}
		});
		contentPane.add(txtBalance, "cell 3 6 6 2,grow");

		lblBalance = new JLabel("Balance");
		lblBalance.setVerticalAlignment(SwingConstants.TOP);
		lblBalance.setHorizontalAlignment(SwingConstants.RIGHT);
		lblBalance.setForeground(_Settings.labelColor);
		lblBalance.setBackground(null);
		lblBalance.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblBalance.setBorder(null);
		contentPane.add(lblBalance, "cell 1 7,grow");

		btnSave = new JButton("SAVE");
		btnSave.setEnabled(false);
		btnSave.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnSave.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnSave.setBackground(_Settings.backgroundColor);
		btnSave.setForeground(_Settings.labelColor);
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String name = txtName.getText().trim();
				String accountNo = txtAccountNo.getText().trim();
				double balance = ValueFormatter.parseMoney(txtBalance.getText());
				boolean isNegBal = chckbxNegBal.isSelected();
				if (tglbtnPersonal.isSelected())
					accountNo = "";
				Inventories inventories = (Inventories) DatabaseFacade.getDatabase("Inventories");
				if (inventories.add(name, accountNo, balance, isNegBal)) {
					reset();
					txtStatus.setText("Record saved!");
					LOGGER.Activity.log("Inventory", LOGGER.CREATE, ValueFormatter.formatInventory(inventories.get(inventories.getList().size() - 1)));
				} else
					txtStatus.setText("An error occurred!");
			}
		});

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
		
		chckbxNegBal = new JCheckBox("Negative Balance");
		chckbxNegBal.setIconTextGap(15);
		chckbxNegBal.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxNegBal.setFont(new Font("Arial Black", Font.PLAIN, 17));
		chckbxNegBal.setForeground(_Settings.labelColor);
		chckbxNegBal.setBackground(null);
		contentPane.add(chckbxNegBal, "cell 3 9 6 1,alignx left,growy");

		contentPane.add(btnClose, "cell 4 13 3 1,aligny center,grow");
		contentPane.add(btnSave, "cell 8 13,aligny center,grow");

		txtStatus = new JTextField();
		txtStatus.setEditable(false);
		txtStatus.setBackground(null);
		txtStatus.setForeground(_Settings.labelColor);
		txtStatus.setBorder(null);
		txtStatus.setHorizontalAlignment(SwingConstants.RIGHT);
		txtStatus.setFont(new Font("Arial Black", Font.PLAIN, 15));
		contentPane.add(txtStatus, "cell 4 11 5 1,growx");
		txtStatus.setColumns(10);
		
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
