package frames;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import cores.Inventory;
import databases.IntraTransfers;
import databases.Inventories;
import globals.DatabaseFacade;
import globals.LOGGER;
import globals.RecordFacade;
import globals.ValueFormatter;
import globals._Settings;
import helpers.MyComboBoxRenderer;
import helpers.SortedComboBoxModel;
import net.miginfocom.swing.MigLayout;

public class IntraTransfer extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JButton btnTransfer;
	private JButton btnClose;
	private JLabel lblFrom, lblTo, lblAvailable, lblAmount;
	private JComboBox<String> cbFromInventory, cbToInventory;
	private JTextField txtStatus, txtAvailable, txtAmount;
	private boolean isFromInventorySelected, isToInventorySelected, isDateEmpty = true, isAmountValid = true,
			isAmountAvailable = true, isNegBal;
	private JLabel lblDate;
	private JDateChooser dateChooser;
	private Date date;
	private JLabel lblDescription;
	private JScrollPane spRemark;
	private JTextArea txtDescription;

	private void setAvailableAmount() {
		cores.Inventory inventory = ValueFormatter.parseInventory((String) cbFromInventory.getSelectedItem());
		txtAvailable.setText(ValueFormatter.formatMoney(inventory.getBalance()));
	}

	private void reset() {
		txtAmount.setText("0.00");
		isAmountValid = true;
		setAvailableAmount();
		isAmountAvailable = isAmountAvailable();
		txtStatus.setText("");
		btnTransfer.setEnabled(false);
	}

	private void validateButton() {
		if (!isFromInventorySelected)
			txtStatus.setText("Select a source inventory!");
		else if (!isAmountValid)
			txtStatus.setText("Invalid amount value!");
		else if (!isAmountAvailable && !isNegBal)
			txtStatus.setText("Amount specified is not available!");
		else if (!isToInventorySelected)
			txtStatus.setText("Select a destination inventory!");
		else if (isDateEmpty)
			txtStatus.setText("Select a date!");
		else
			txtStatus.setText("");
		if (isAmountValid && isToInventorySelected && isFromInventorySelected && (isAmountAvailable || isNegBal))
			btnTransfer.setEnabled(true);
		else
			btnTransfer.setEnabled(false);
	}

	private boolean isAmountValid() {
		return Pattern.compile("([0-9]{1,3}(\\,)?)*(\\.[0-9]*)?").matcher(txtAmount.getText()).matches();
	}

	private boolean isAmountAvailable() {
		if (txtAvailable.getText().isEmpty())
			return false;
		double available = ValueFormatter.parseMoney(txtAvailable.getText());
		double amount = ValueFormatter.parseMoney(txtAmount.getText());
		return amount <= available;
	}

	/**
	 * Create the frame.
	 */
	public IntraTransfer(Window owner) {
		super(owner, "Transfer between Inventories", Dialog.ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		setBounds(100, 100, 850, 700);
		setAutoRequestFocus(true);
		contentPane = new JPanel();
		contentPane.setBackground(_Settings.backgroundColor);
		contentPane.setBorder(null);
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[10][120,grow][10][150][10][150][10]",
				"[10][40][10][40][10][40][10][40][10][40][10][80,grow][10][20][10][60]"));

		lblDate = new JLabel("Date");
		lblDate.setBorder(null);
		lblDate.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDate.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblDate.setForeground(_Settings.labelColor);
		contentPane.add(lblDate, "cell 1 9,grow");

		dateChooser = new JDateChooser();
		dateChooser.setFont(new Font("Century Gothic", Font.BOLD, 17));
		dateChooser.setBorder(null);
		dateChooser.setDateFormatString("dd-MMM-yyyy");
		JTextFieldDateEditor dateEditor = (JTextFieldDateEditor) dateChooser.getComponent(1);
		dateEditor.setHorizontalAlignment(JTextField.CENTER);
		dateEditor.setEnabled(false);
		dateEditor.setDisabledTextColor(Color.DARK_GRAY);
		dateChooser.addPropertyChangeListener("date", new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent e) {
				// TODO Auto-generated method stub
				date = (Date) e.getNewValue();
				isDateEmpty = false;
				validateButton();
			}
		});
		contentPane.add(dateChooser, "cell 3 9 3 1,grow");

		lblDescription = new JLabel("Description");
		lblDescription.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDescription.setForeground(_Settings.labelColor);
		lblDescription.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblDescription.setBorder(null);
		contentPane.add(lblDescription, "cell 1 11,grow");

		txtDescription = new JTextArea();
		txtDescription.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtDescription.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtDescription.setBackground(_Settings.backgroundColor);
		txtDescription.setForeground(_Settings.textFieldColor);
		txtDescription.setLineWrap(true);

		spRemark = new JScrollPane(txtDescription);
		spRemark.setBorder(null);
		contentPane.add(spRemark, "cell 3 11 3 1,grow");

		txtStatus = new JTextField();
		txtStatus.setEditable(false);
		txtStatus.setBackground(null);
		txtStatus.setForeground(_Settings.labelColor);
		txtStatus.setBorder(null);
		txtStatus.setHorizontalAlignment(SwingConstants.RIGHT);
		txtStatus.setFont(new Font("Arial Black", Font.PLAIN, 15));
		txtStatus.setColumns(10);
		contentPane.add(txtStatus, "cell 3 13 3 1,growx");

		lblFrom = new JLabel("From");
		lblFrom.setBorder(null);
		lblFrom.setHorizontalAlignment(SwingConstants.RIGHT);
		lblFrom.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblFrom.setForeground(_Settings.labelColor);
		contentPane.add(lblFrom, "cell 1 1,grow");

		btnTransfer = new JButton("TRANSFER");
		btnTransfer.setEnabled(false);
		btnTransfer.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnTransfer.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnTransfer.setBackground(_Settings.backgroundColor);
		btnTransfer.setForeground(_Settings.labelColor);
		btnTransfer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				IntraTransfers db = (IntraTransfers) DatabaseFacade.getDatabase("IntraTransfers");
				int id = db.maxID() + 1;
				double amount = ValueFormatter.parseMoney(txtAmount.getText());
				cores.Inventory srcInventory = ValueFormatter
						.parseInventory((String) cbFromInventory.getSelectedItem());
				cores.Inventory destInventory = ValueFormatter.parseInventory((String) cbToInventory.getSelectedItem());
				String remark = txtDescription.getText().replaceAll("\n", " ");
				remark = remark.replaceAll(",", ";");
				db.add(id, amount, date, srcInventory, destInventory, remark);
				String balanceId = ValueFormatter.formatBalanceId(id, cores.IntraTransfer.class);
				RecordFacade.record(srcInventory, amount, 0f, false, null);
				RecordFacade.record(destInventory, amount, 0f, true, null);
				RecordFacade.addInventoryRecord(srcInventory, amount, 0f, false, balanceId, date);
				RecordFacade.addInventoryRecord(destInventory, amount, 0f, true, balanceId, date);
				reset();
				txtStatus.setText("Amount transferred!");
				LOGGER.Activity.log("Transfer", LOGGER.ADD, ValueFormatter.formatBalanceId(id, cores.IntraTransfer.class));
			}
		});
		contentPane.add(btnTransfer, "cell 5 15,aligny center,grow");

		lblAvailable = new JLabel("Available");
		lblAvailable.setBorder(null);
		lblAvailable.setHorizontalAlignment(SwingConstants.RIGHT);
		lblAvailable.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblAvailable.setForeground(_Settings.labelColor);
		contentPane.add(lblAvailable, "cell 1 3,grow");

		txtAvailable = new JTextField();
		txtAvailable.setBackground(_Settings.backgroundColor);
		txtAvailable.setForeground(_Settings.textFieldColor);
		txtAvailable.setHorizontalAlignment(SwingConstants.CENTER);
		txtAvailable.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtAvailable.setEnabled(false);
		txtAvailable.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtAvailable.setEditable(false);
		contentPane.add(txtAvailable, "cell 3 3 3 1,grow");
		txtAvailable.setColumns(10);

		lblTo = new JLabel("To");
		lblTo.setBorder(null);
		lblTo.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTo.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblTo.setForeground(_Settings.labelColor);
		contentPane.add(lblTo, "cell 1 7,grow");

		Inventories db = (Inventories) DatabaseFacade.getDatabase("Inventories");
		List<Inventory> list = db.getList();

		SortedComboBoxModel<String> scbmToInventory = new SortedComboBoxModel<String>();
		MyComboBoxRenderer mcbrToInventory = new MyComboBoxRenderer();
		cbToInventory = new JComboBox<String>(scbmToInventory);
		cbToInventory.setMaximumSize(new Dimension(320, 32767));
		cbToInventory.setRenderer(mcbrToInventory);
		cbToInventory.setForeground(Color.DARK_GRAY);
		cbToInventory.setFont(new Font("Century Gothic", Font.BOLD, 21));
		cbToInventory.setBorder(null);
		cbToInventory.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if (e.getStateChange() == ItemEvent.SELECTED) {
					isToInventorySelected = true;
					validateButton();
				}
			}
		});
		contentPane.add(cbToInventory, "cell 3 7 3 1,grow");

		lblAmount = new JLabel("Amount");
		lblAmount.setBorder(null);
		lblAmount.setHorizontalAlignment(SwingConstants.RIGHT);
		lblAmount.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblAmount.setForeground(_Settings.labelColor);
		contentPane.add(lblAmount, "cell 1 5,grow");

		txtAmount = new JTextField();
		txtAmount.setText("0.00");
		txtAmount.setEnabled(false);
		txtAmount.setHorizontalAlignment(SwingConstants.CENTER);
		txtAmount.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtAmount.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtAmount.setBackground(null);
		txtAmount.setForeground(_Settings.textFieldColor);
		txtAmount.setColumns(10);
		txtAmount.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				isAmountValid = isAmountValid();
				if (isAmountValid) {
					double amount = ValueFormatter.parseMoney(txtAmount.getText());
					txtAmount.setText(ValueFormatter.formatMoney(amount));
					isAmountAvailable = isAmountAvailable();
				}
				validateButton();
			}

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				txtAmount.selectAll();
			}
		});
		contentPane.add(txtAmount, "cell 3 5 3 0,grow");

		MyComboBoxRenderer mcbrFromInventory = new MyComboBoxRenderer();
		List<String> ttInventory = new ArrayList<String>();
		SortedComboBoxModel<String> scbmFromInventory = new SortedComboBoxModel<String>();
		cbFromInventory = new JComboBox<String>(scbmFromInventory);
		cbFromInventory.setMaximumSize(new Dimension(320, 32767));
		cbFromInventory.setRenderer(mcbrFromInventory);
		cbFromInventory.setForeground(Color.DARK_GRAY);
		cbFromInventory.setFont(new Font("Century Gothic", Font.BOLD, 21));
		cbFromInventory.setBorder(null);
		for (cores.Inventory inventory : list) {
			scbmFromInventory.addElement(ValueFormatter.formatInventory(inventory));
			scbmToInventory.addElement(ValueFormatter.formatInventory(inventory));
		}
		for (int i = 0; i < cbFromInventory.getItemCount(); i++) {
			Inventory inventory = ValueFormatter.parseInventory(cbFromInventory.getItemAt(i));
			String inventoryString = inventory.getName();
			if (!inventory.getAccountNo().isEmpty())
				inventoryString += " [" + inventory.getAccountNo() + "]";
			ttInventory.add(inventoryString);
		}
		cbFromInventory.setSelectedIndex(-1);
		cbToInventory.setSelectedIndex(-1);
		mcbrFromInventory.setTooltips(ttInventory);
		mcbrToInventory.setTooltips(ttInventory);
		cbFromInventory.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if ((e.getStateChange() == ItemEvent.SELECTED)) {
					isFromInventorySelected = true;
					setAvailableAmount();
					txtAmount.setEnabled(true);
					txtAmount.setText("0.00");
					cores.Inventory srcInventory = ValueFormatter
							.parseInventory((String) cbFromInventory.getSelectedItem());
					isNegBal = srcInventory.isNegBal();
					validateButton();
				}
			}
		});
		contentPane.add(cbFromInventory, "cell 3 1 3 1,grow");

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
		contentPane.add(btnClose, "cell 3 15,aligny center,grow");

		getRootPane().setDefaultButton(btnTransfer);
		getRootPane().registerKeyboardAction(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				dispose();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

		pack();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}

}
