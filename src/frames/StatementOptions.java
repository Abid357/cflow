package frames;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableModel;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import cores.DeliveryTransaction;
import cores.IntraTransfer;
import cores.Inventory;
import cores.InventoryRecord;
import cores.PurchaseTransaction;
import cores.RecordAdjustment;
import cores.SaleTransaction;
import cores.Statement;
import cores.Stock;
import cores.Transaction;
import databases.DeliveryTransactions;
import databases.IntraTransfers;
import databases.Inventories;
import databases.InventoryRecords;
import databases.PurchaseTransactions;
import databases.RecordAdjustments;
import databases.SaleTransactions;
import databases.Transactions;
import globals.DatabaseFacade;
import globals.ValueFormatter;
import globals._Settings;
import helpers.SortedComboBoxModel;
import net.miginfocom.swing.MigLayout;

public class StatementOptions extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JComboBox<String> cbInventory;
	JComboBox<String> cbType;
	private JDateChooser dcFrom;
	private JDateChooser dcTo;
	private Date fromDate;
	private Date toDate;
	private DefaultTableModel dtm;
	private JTable tblStatement;
	private String[] columnHeaders = { "DATE", "DESCRIPTION", "DEBIT", "CREDIT", "BALANCE" };
	private boolean isConfirmed;

	public String getSelectedInventory() {
		return (String) cbInventory.getSelectedItem();
	}

	public Date getFromDate() {
		return fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public JTable getTable() {
		return tblStatement;
	}

	public boolean isConfirmed() {
		return isConfirmed;
	}

	public void setConfirmed(boolean isConfirmed) {
		this.isConfirmed = isConfirmed;
	}

	private double calculateTotal(double amount, double tax) {
		return tax + amount;
	}

	private double calculateDeliveryTotal(DeliveryTransaction delivery) {
		double total = 0;
		for (Stock stock : delivery.getItems())
			total += stock.getPrice() * stock.getQuantity();
		return total;
	}

	private double calculateSalesTotal(SaleTransaction sale) {
		double total = 0;
		for (Stock stock : sale.getItems())
			total += stock.getPrice() * stock.getQuantity();
		return total;
	}

	private double calculatePurchaseTotal(PurchaseTransaction purchase) {
		double total = 0;
		for (Stock stock : purchase.getItems())
			total += stock.getPrice() * stock.getQuantity();
		return total;
	}

	private Statement createStatement(InventoryRecord record) {
		String balanceId = record.getId();
		int idNo = Integer.parseInt(balanceId.substring(balanceId.indexOf("-") + 1));
		if (balanceId.startsWith("TR")) {
			Transactions db = (Transactions) DatabaseFacade.getDatabase("Transactions");
			Transaction transaction = db.get(db.find(idNo));
			return transaction != null
					? new Statement(transaction.getRemark(), transaction.getDate(), transaction.isCredit(),
							calculateTotal(transaction.getAmount(), transaction.getTax()), record.getBalance())
					: null;
		} else if (balanceId.startsWith("IT")) {
			IntraTransfers db = (IntraTransfers) DatabaseFacade.getDatabase("IntraTransfers");
			IntraTransfer intraTransfer = db.get(db.find(idNo));
			boolean isCredit = false;
			if (intraTransfer.getToInventory().equals(record.getInventory()))
				isCredit = true;
			return intraTransfer != null
					? new Statement(intraTransfer.getRemark(), intraTransfer.getDate(), isCredit,
							intraTransfer.getAmount(), record.getBalance())
					: null;
		} else if (balanceId.startsWith("ST")) {
			SaleTransactions db = (SaleTransactions) DatabaseFacade.getDatabase("SaleTransactions");
			SaleTransaction sale = db.get(db.find(idNo));
			boolean isCredit = false;
			return sale != null
					? new Statement(ValueFormatter.formatSaleRemark(sale), sale.getDate(), isCredit,
							calculateSalesTotal(sale), record.getBalance())
					: null;
		} else if (balanceId.startsWith("DT")) {
			DeliveryTransactions db = (DeliveryTransactions) DatabaseFacade.getDatabase("DeliveryTransactions");
			DeliveryTransaction delivery = db.get(db.find(idNo));
			boolean isCredit = false;
			if (delivery.getToStore().getInventory().equals(record.getInventory()))
				isCredit = true;
			return delivery != null
					? new Statement(ValueFormatter.formatDeliveryRemark(delivery), delivery.getDate(), isCredit,
							calculateDeliveryTotal(delivery), record.getBalance())
					: null;
		} else if (balanceId.startsWith("PT")) {
			PurchaseTransactions db = (PurchaseTransactions) DatabaseFacade.getDatabase("PurchaseTransactions");
			PurchaseTransaction purchase = db.get(db.find(idNo));
			boolean isCredit = false;
			return purchase != null
					? new Statement(ValueFormatter.formatPurchaseRemark(purchase), purchase.getDate(), isCredit,
							calculatePurchaseTotal(purchase), record.getBalance())
					: null;
		} else if (balanceId.startsWith("RA")) {
			RecordAdjustments db = (RecordAdjustments) DatabaseFacade.getDatabase("RecordAdjustments");
			RecordAdjustment adjustment = db.get(db.find(idNo));
			return adjustment != null
					? new Statement(adjustment.getRemark(), adjustment.getDate(), adjustment.isCredit(),
							adjustment.getAmount(), record.getBalance())
					: null;
		}
		return null;
	}

	private void updateTable() {
		dtm.setRowCount(0);
		String selectedInventory = (String) cbInventory.getSelectedItem();
		if (selectedInventory != null) {
			List<Statement> list = new ArrayList<Statement>();
			InventoryRecords dbInventoryRecord = (InventoryRecords) DatabaseFacade.getDatabase("InventoryRecords");
			String selectedType = (String) cbType.getSelectedItem();

			int startIndex = dbInventoryRecord.getInventoryStartIndex(selectedInventory);
			int endIndex = dbInventoryRecord.getInventoryEndIndex(selectedInventory);
			if (startIndex != -1)
				for (int i = startIndex; i <= endIndex; i++) {
					Statement statement = createStatement(dbInventoryRecord.get(i));
					String type = "Debit";
					if (statement.isCredit())
						type = "Credit";
					if (selectedType.equals("ALL") || selectedType.equals(type + " Only"))
						list.add(statement);
				}

			if (fromDate != null) {
				List<Statement> datedStatements = new ArrayList<Statement>();
				for (Statement statement : list)
					if (statement.getDate().after(fromDate) || statement.getDate().equals(fromDate))
						datedStatements.add(statement);
				list = datedStatements;
			}
			if (toDate != null) {
				List<Statement> datedStatements = new ArrayList<Statement>();
				for (Statement statement : list)
					if (statement.getDate().before(toDate) || statement.getDate().equals(toDate))
						datedStatements.add(statement);
				list = datedStatements;
			}

			Collections.reverse(list);
			for (Statement statement : list) {
				String description = statement.getDescription();
				if (description == null)
					description = "";
				String credit = "", debit = "";
				if (statement.isCredit())
					credit = "+" + ValueFormatter.formatMoney(statement.getAmount());
				else
					debit = ValueFormatter.formatMoney(statement.getAmount());
				dtm.addRow(new Object[] { ValueFormatter.formatDate(statement.getDate()), description, debit, credit,
						ValueFormatter.formatMoney(statement.getBalance()) });
			}
		}
	}

	private Date setTimeToZero(Date date) {
		if (date != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			return calendar.getTime();
		} else
			return null;
	}

	/**
	 * Create the frame.
	 */
	public StatementOptions(Window owner) {
		super(owner, "Statement Options", Dialog.ModalityType.DOCUMENT_MODAL);
		setBounds(100, 100, 337, 375);
		setAutoRequestFocus(true);
		contentPane = new JPanel();
		contentPane.setBackground(_Settings.backgroundColor);
		contentPane.setBorder(null);
		setContentPane(contentPane);
		contentPane.setLayout(
				new MigLayout("", "[150][10][150,grow][10][150][10][150][grow]", "[20][30][10][][grow][10][50]"));

		JLabel lblInventory = new JLabel("Inventory");
		lblInventory.setFont(new Font("Arial", Font.BOLD, 15));
		lblInventory.setHorizontalAlignment(SwingConstants.CENTER);
		lblInventory.setBorder(null);
		lblInventory.setBackground(_Settings.backgroundColor);
		lblInventory.setForeground(_Settings.labelColor);
		contentPane.add(lblInventory, "cell 0 0,grow");

		JLabel lblType = new JLabel("Type");
		lblType.setHorizontalAlignment(SwingConstants.CENTER);
		lblType.setForeground(_Settings.labelColor);
		lblType.setFont(new Font("Arial", Font.BOLD, 15));
		lblType.setBorder(null);
		lblType.setBackground(_Settings.backgroundColor);
		contentPane.add(lblType, "cell 2 0,grow");

		JLabel lblFrom = new JLabel("From");
		lblFrom.setHorizontalAlignment(SwingConstants.CENTER);
		lblFrom.setForeground(_Settings.labelColor);
		lblFrom.setFont(new Font("Arial", Font.BOLD, 15));
		lblFrom.setBorder(null);
		lblFrom.setBackground(_Settings.backgroundColor);
		contentPane.add(lblFrom, "cell 4 0,grow");

		JLabel lblTo = new JLabel("To");
		lblTo.setHorizontalAlignment(SwingConstants.CENTER);
		lblTo.setForeground(_Settings.labelColor);
		lblTo.setFont(new Font("Arial", Font.BOLD, 15));
		lblTo.setBorder(null);
		lblTo.setBackground(_Settings.backgroundColor);
		contentPane.add(lblTo, "cell 6 0,grow");

		Inventories inventories = (Inventories) DatabaseFacade.getDatabase("Inventories");
		SortedComboBoxModel<String> scbmInventory = new SortedComboBoxModel<String>();
		List<Inventory> inventoryList = inventories.getList();
		for (Inventory inventory : inventoryList)
			scbmInventory.addElement(ValueFormatter.formatInventory(inventory));
		cbInventory = new JComboBox<String>(scbmInventory);
		cbInventory.setMaximumSize(new Dimension(150, 32767));
		cbInventory.setFont(new Font("Century Gothic", Font.BOLD, 14));
		cbInventory.setBorder(null);
		cbInventory.setForeground(Color.DARK_GRAY);
		cbInventory.setSelectedIndex(-1);
		cbInventory.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if ((e.getStateChange() == ItemEvent.SELECTED)) {
					updateTable();
				}
			}
		});
		contentPane.add(cbInventory, "cell 0 1,grow");

		cbType = new JComboBox<String>();
		cbType.setSelectedIndex(-1);
		cbType.setForeground(Color.DARK_GRAY);
		cbType.setFont(new Font("Century Gothic", Font.BOLD, 14));
		cbType.setBorder(null);
		cbType.addItem("ALL");
		cbType.addItem("Credit Only");
		cbType.addItem("Debit Only");
		cbType.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if ((e.getStateChange() == ItemEvent.SELECTED)) {
					updateTable();
				}
			}
		});
		contentPane.add(cbType, "cell 2 1,grow");

		dcFrom = new JDateChooser();
		dcFrom.setFont(new Font("Century Gothic", Font.BOLD, 14));
		dcFrom.setBorder(null);
		dcFrom.setDateFormatString("dd-MMM-yyyy");
		JTextFieldDateEditor deFrom = (JTextFieldDateEditor) dcFrom.getComponent(1);
		deFrom.setHorizontalAlignment(JTextField.CENTER);
		deFrom.setEnabled(false);
		deFrom.setDisabledTextColor(Color.DARK_GRAY);
		dcFrom.addPropertyChangeListener("date", new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent e) {
				// TODO Auto-generated method stub
				fromDate = (Date) e.getNewValue();
				fromDate = setTimeToZero(fromDate);
				dcTo.setMinSelectableDate(fromDate);
				updateTable();
			}
		});
		contentPane.add(dcFrom, "cell 4 1,grow");

		dcTo = new JDateChooser();
		dcTo.setFont(new Font("Century Gothic", Font.BOLD, 14));
		dcTo.setBorder(null);
		dcTo.setDateFormatString("dd-MMM-yyyy");
		JTextFieldDateEditor deTo = (JTextFieldDateEditor) dcTo.getComponent(1);
		deTo.setHorizontalAlignment(JTextField.CENTER);
		deTo.setEnabled(false);
		deTo.setDisabledTextColor(Color.DARK_GRAY);
		dcTo.addPropertyChangeListener("date", new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent e) {
				// TODO Auto-generated method stub
				toDate = (Date) e.getNewValue();
				toDate = setTimeToZero(toDate);
				dcFrom.setMaxSelectableDate(toDate);
				updateTable();
			}
		});
		contentPane.add(dcTo, "cell 6 1,grow");

		JScrollPane spStatement = new JScrollPane();
		spStatement.setBackground(null);
		spStatement.setOpaque(false);
		spStatement.getViewport().setOpaque(false);
		contentPane.add(spStatement, "cell 0 3 8 2,grow");

		dtm = new DefaultTableModel() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}

		};
		dtm.setColumnIdentifiers(columnHeaders);
		tblStatement = new JTable(dtm);
		tblStatement.setGridColor(_Settings.labelColor);
		tblStatement.getTableHeader().setForeground(_Settings.labelColor);
		tblStatement.getTableHeader().setBackground(_Settings.backgroundColor);
		tblStatement.getTableHeader().setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		tblStatement.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
		tblStatement.setFont(new Font("Century Gothic", Font.BOLD, 16));
		tblStatement.setBackground(_Settings.backgroundColor);
		tblStatement.setForeground(_Settings.textFieldColor);
		tblStatement.setRowHeight(30);

		spStatement.setViewportView(tblStatement);

		JButton btnConfirm = new JButton("CONFIRM");
		btnConfirm.setPreferredSize(new Dimension(150, 50));
		btnConfirm.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnConfirm.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnConfirm.setBackground(_Settings.backgroundColor);
		btnConfirm.setForeground(_Settings.labelColor);
		btnConfirm.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (cbInventory.getSelectedIndex() == -1)
					new MyOptionPane("Select an inventory.", MyOptionPane.INFORMATION_MESSAGE);
				else {
					setConfirmed(true);
					dispose();
				}
			}
		});
		contentPane.add(btnConfirm, "cell 6 6,grow");

		JButton btnClose = new JButton("CLOSE");
		btnClose.setMaximumSize(new Dimension(2000, 2000));
		btnClose.setPreferredSize(new Dimension(150, 50));
		btnClose.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnClose.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnClose.setBackground(_Settings.backgroundColor);
		btnClose.setForeground(_Settings.labelColor);
		btnClose.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				setConfirmed(false);
				dispose();
			}
		});
		contentPane.add(btnClose, "cell 4 6,grow");

		updateTable();

		getRootPane().setDefaultButton(btnConfirm);
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
