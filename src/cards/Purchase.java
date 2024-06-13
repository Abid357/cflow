package cards;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import cores.PurchaseTransaction;
import cores.Stock;
import cores.Store;
import cores.Transaction;
import cores.UserObject;
import databases.PurchaseTransactions;
import databases.Stores;
import databases.UserObjects;
import frames.Main;
import frames.MyOptionPane;
import globals.DatabaseFacade;
import globals.ItemFacade;
import globals.LOGGER;
import globals.RecordFacade;
import globals.ValueFormatter;
import globals._Settings;
import helpers.MyComboBoxRenderer;
import helpers.SortedComboBoxModel;
import helpers.TransactionStatus;
import net.miginfocom.swing.MigLayout;

public class Purchase extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static String SORT_BY_DATE = "SORT BY: DATE";
	private static String SORT_BY_ID = "SORT BY: ID";

	private Main mainFrame;
	private JTable tblPurchase;
	private JDateChooser dcFrom, dcTo;
	private Date toDate, fromDate;
	private DefaultTableModel dtm;
	private String[] columnHeaders = { "ID", "USER", "STORE", "AMOUNT", "DATE", "ITEM STATUS", "PAYMENT STATUS" };
	private JComboBox<String> cbSupplier, cbPaymentStatus, cbItemStatus, cbStore;
	private MyComboBoxRenderer mcbrCustomer, mcbrStore;
	private JButton btnSort;

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

	public void updateOptions() {
		cbSupplier.removeAllItems();
		cbSupplier.addItem("ANY");
		List<String> ttUser = new ArrayList<String>();
		UserObjects userObjects = (UserObjects) DatabaseFacade.getDatabase("UserObjects");
		List<UserObject> userObjectList = userObjects.getList();
		for (UserObject userObject : userObjectList) {
			String userString = ValueFormatter.formatUserObject(userObject);
			cbSupplier.addItem(userString);
			ttUser.add(userString);
		}
		ttUser.sort(null);
		ttUser.add(0, "ANY");
		mcbrCustomer.setTooltips(ttUser);
		cbSupplier.setSelectedIndex(0);

		cbStore.removeAllItems();
		cbStore.addItem("ANY");
		List<String> ttStore = new ArrayList<String>();
		Stores stores = (Stores) DatabaseFacade.getDatabase("Stores");
		List<Store> storeList = stores.getList();
		for (Store store : storeList) {
			String storeString = ValueFormatter.formatStore(store);
			cbStore.addItem(storeString);
			ttStore.add(storeString);
		}
		ttStore.sort(null);
		ttStore.add(0, "ANY");
		mcbrStore.setTooltips(ttStore);
		cbStore.setSelectedIndex(0);
	}

	public void resetSelections() {
		cbSupplier.setSelectedIndex(0);
		cbItemStatus.setSelectedIndex(0);
		cbPaymentStatus.setSelectedIndex(0);
		fromDate = null;
		toDate = null;
		dcFrom.setDate(null);
		dcTo.setDate(null);
		updateTable();
	}

	public void updateTable() {
		dtm.setRowCount(0);
		PurchaseTransactions db = (PurchaseTransactions) DatabaseFacade.getDatabase("PurchaseTransactions");
		List<PurchaseTransaction> list = db.getList();

		String selectedCustomer = (String) cbSupplier.getSelectedItem();
		if (!selectedCustomer.equals("ANY")) {
			UserObject userObject = ValueFormatter.parseUserObject(selectedCustomer);
			List<PurchaseTransaction> customerPurchases = new ArrayList<PurchaseTransaction>();
			for (PurchaseTransaction transaction : list)
				if (transaction.getSupplier().equals(userObject))
					customerPurchases.add(transaction);
			list = customerPurchases;
		}
		if (fromDate != null) {
			List<PurchaseTransaction> datedPurchases = new ArrayList<PurchaseTransaction>();
			for (PurchaseTransaction transaction : list)
				if (transaction.getDate().after(fromDate) || transaction.getDate().equals(fromDate))
					datedPurchases.add(transaction);
			list = datedPurchases;
		}
		if (toDate != null) {
			List<PurchaseTransaction> datedPurchases = new ArrayList<PurchaseTransaction>();
			for (PurchaseTransaction transaction : list)
				if (transaction.getDate().before(toDate) || transaction.getDate().equals(toDate))
					datedPurchases.add(transaction);
			list = datedPurchases;
		}
		String selectedStore = (String) cbStore.getSelectedItem();
		if (!selectedStore.equals("ANY")) {
			List<PurchaseTransaction> storePurchases = new ArrayList<PurchaseTransaction>();
			Store store = ValueFormatter.parseStore(selectedStore);
			for (PurchaseTransaction transaction : list)
				if (transaction.getStore().equals(store))
					storePurchases.add(transaction);
			list = storePurchases;
		}
		String selectedItemStatus = (String) cbItemStatus.getSelectedItem();
		if (!selectedItemStatus.equals("ANY")) {
			List<PurchaseTransaction> statusPurchases = new ArrayList<PurchaseTransaction>();
			for (PurchaseTransaction transaction : list)
				if (transaction.getItemStatus().equals(TransactionStatus.valueOf(selectedItemStatus)))
					statusPurchases.add(transaction);
			list = statusPurchases;
		}
		String selectedPaymentStatus = (String) cbPaymentStatus.getSelectedItem();
		if (!selectedPaymentStatus.equals("ANY")) {
			List<PurchaseTransaction> statusPurchases = new ArrayList<PurchaseTransaction>();
			for (PurchaseTransaction transaction : list)
				if (transaction.getPaymentStatus().equals(TransactionStatus.valueOf(selectedPaymentStatus)))
					statusPurchases.add(transaction);
			list = statusPurchases;
		}

		if (btnSort.getText().equals(SORT_BY_DATE))
			Collections.sort(list);
		else
			Collections.sort(list, new Comparator<PurchaseTransaction>() {
				public int compare(PurchaseTransaction t1, PurchaseTransaction t2) {
					return t2.getDate().compareTo(t1.getDate());
				}
			});

		for (PurchaseTransaction transaction : list) {
			dtm.addRow(new Object[] { transaction.getId(), ValueFormatter.formatUserObject(transaction.getSupplier()),
					ValueFormatter.formatStore(transaction.getStore()),
					ValueFormatter.formatMoney(transaction.getTotal()),
					ValueFormatter.formatDate(transaction.getDate()), transaction.getItemStatus(),
					transaction.getPaymentStatus() });
		}
	}

	/**
	 * Create the panel.
	 */
	public Purchase(Main frame) {
		this.mainFrame = frame;
		setBackground(_Settings.backgroundColor);
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setLayout(new MigLayout("", "[150][10][150][10][150][10][150][10][150][10][150][10][150][grow]",
				"[50][10][20][30][10][grow][10][50]"));

		JButton btnAddPurchase = new JButton("ADD PURCHASE");
		btnAddPurchase.setBackground(_Settings.backgroundColor);
		btnAddPurchase.setForeground(_Settings.labelColor);
		btnAddPurchase.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnAddPurchase.setFont(new Font("Arial", Font.BOLD, 14));
		btnAddPurchase.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				new frames.Purchase(mainFrame, null);
			}
		});
		add(btnAddPurchase, "cell 0 0,grow");

		btnSort = new JButton("SORT BY: DATE");
		btnSort.setBackground(_Settings.backgroundColor);
		btnSort.setForeground(_Settings.labelColor);
		btnSort.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnSort.setFont(new Font("Arial", Font.BOLD, 14));
		btnSort.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (btnSort.getText().equals(SORT_BY_DATE))
					btnSort.setText(SORT_BY_ID);
				else
					btnSort.setText(SORT_BY_DATE);
			}
		});

		JButton btnGenerateReport = new JButton("PRINT STATEMENT");
		btnGenerateReport.setBackground(_Settings.backgroundColor);
		btnGenerateReport.setForeground(_Settings.labelColor);
		btnGenerateReport.setFont(new Font("Arial", Font.BOLD, 14));
		btnGenerateReport.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		add(btnGenerateReport, "cell 2 0,grow");
		add(btnSort, "cell 10 0,grow");

		JLabel lblSupplier = new JLabel("Supplier");
		lblSupplier.setFont(new Font("Arial", Font.BOLD, 15));
		lblSupplier.setHorizontalAlignment(SwingConstants.CENTER);
		lblSupplier.setBorder(null);
		lblSupplier.setBackground(null);
		lblSupplier.setForeground(_Settings.labelColor);
		add(lblSupplier, "cell 0 2,grow");

		JLabel lblStore = new JLabel("Store");
		lblStore.setHorizontalAlignment(SwingConstants.CENTER);
		lblStore.setForeground(_Settings.labelColor);
		lblStore.setFont(new Font("Arial", Font.BOLD, 15));
		lblStore.setBorder(null);
		lblStore.setBackground(_Settings.backgroundColor);
		add(lblStore, "cell 2 2,grow");

		JLabel lblFrom = new JLabel("From");
		lblFrom.setHorizontalAlignment(SwingConstants.CENTER);
		lblFrom.setFont(new Font("Arial", Font.BOLD, 15));
		lblFrom.setBorder(null);
		lblFrom.setBackground(null);
		lblFrom.setForeground(_Settings.labelColor);
		add(lblFrom, "cell 4 2,grow");

		JLabel lblTo = new JLabel("To");
		lblTo.setHorizontalAlignment(SwingConstants.CENTER);
		lblTo.setFont(new Font("Arial", Font.BOLD, 15));
		lblTo.setBorder(null);
		lblTo.setBackground(null);
		lblTo.setForeground(_Settings.labelColor);
		add(lblTo, "cell 6 2,grow");

		JLabel lblItemStatus = new JLabel("Item Status");
		lblItemStatus.setHorizontalAlignment(SwingConstants.CENTER);
		lblItemStatus.setFont(new Font("Arial", Font.BOLD, 15));
		lblItemStatus.setBorder(null);
		lblItemStatus.setBackground(null);
		lblItemStatus.setForeground(_Settings.labelColor);
		add(lblItemStatus, "cell 8 2,grow");

		JLabel lblPaymentStatus = new JLabel("Payment Status");
		lblPaymentStatus.setHorizontalAlignment(SwingConstants.CENTER);
		lblPaymentStatus.setFont(new Font("Arial", Font.BOLD, 15));
		lblPaymentStatus.setBorder(null);
		lblPaymentStatus.setBackground(null);
		lblPaymentStatus.setForeground(_Settings.labelColor);
		add(lblPaymentStatus, "cell 10 2,grow");

		JButton btnRefresh = new JButton("REFRESH");
		btnRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				resetSelections();
			}
		});
		btnRefresh.setBackground(_Settings.backgroundColor);
		btnRefresh.setForeground(_Settings.labelColor);
		btnRefresh.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnRefresh.setFont(new Font("Arial", Font.BOLD, 14));
		add(btnRefresh, "cell 12 2 1 2,grow");

		mcbrCustomer = new MyComboBoxRenderer();
		cbSupplier = new JComboBox<String>(new SortedComboBoxModel<String>());
		cbSupplier.setRenderer(mcbrCustomer);
		cbSupplier.setMaximumSize(new Dimension(150, 32767));
		cbSupplier.setFont(new Font("Century Gothic", Font.BOLD, 14));
		cbSupplier.setBorder(null);
		cbSupplier.setForeground(Color.DARK_GRAY);
		add(cbSupplier, "cell 0 3,grow");

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

		mcbrStore = new MyComboBoxRenderer();
		cbStore = new JComboBox<String>(new SortedComboBoxModel<String>());
		cbStore.setRenderer(mcbrStore);
		cbStore.setMaximumSize(new Dimension(150, 32767));
		cbStore.setForeground(Color.DARK_GRAY);
		cbStore.setFont(new Font("Century Gothic", Font.BOLD, 14));
		cbStore.setBorder(null);
		add(cbStore, "cell 2 3,grow");
		add(dcFrom, "cell 4 3,grow");
		add(dcTo, "cell 6 3,grow");

		cbItemStatus = new JComboBox<String>();
		cbItemStatus.setMaximumSize(new Dimension(150, 32767));
		cbItemStatus.setFont(new Font("Century Gothic", Font.BOLD, 14));
		cbItemStatus.setBorder(null);
		cbItemStatus.setForeground(Color.DARK_GRAY);
		cbItemStatus.addItem("ANY");
		cbItemStatus.addItem(TransactionStatus.DELIVERED.toString());
		cbItemStatus.addItem(TransactionStatus.UNDELIVERED.toString());
		cbItemStatus.setSelectedIndex(0);
		add(cbItemStatus, "cell 8 3,grow");

		cbPaymentStatus = new JComboBox<String>();
		cbPaymentStatus.setMaximumSize(new Dimension(150, 32767));
		cbPaymentStatus.setFont(new Font("Century Gothic", Font.BOLD, 14));
		cbPaymentStatus.setBorder(null);
		cbPaymentStatus.setForeground(Color.DARK_GRAY);
		cbPaymentStatus.addItem("ANY");
		cbPaymentStatus.addItem(TransactionStatus.PENDING.toString());
		cbPaymentStatus.addItem(TransactionStatus.PAID.toString());
		cbPaymentStatus.addItem(TransactionStatus.UNPAID.toString());
		cbPaymentStatus.setSelectedIndex(0);
		add(cbPaymentStatus, "cell 10 3,grow");

		JScrollPane spTransaction = new JScrollPane();
		spTransaction.setBackground(null);
		spTransaction.setOpaque(false);
		spTransaction.getViewport().setOpaque(false);
		add(spTransaction, "cell 0 5 14 1,grow");

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
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
		tblPurchase = new JTable(dtm);
		tblPurchase.setGridColor(_Settings.labelColor);
		tblPurchase.getTableHeader().setForeground(_Settings.labelColor);
		tblPurchase.getTableHeader().setBackground(_Settings.backgroundColor);
		tblPurchase.getTableHeader().setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		tblPurchase.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
		tblPurchase.setFont(new Font("Century Gothic", Font.BOLD, 16));
		tblPurchase.setBackground(_Settings.backgroundColor);
		tblPurchase.setForeground(_Settings.textFieldColor);
		tblPurchase.setRowHeight(30);
		tblPurchase.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		tblPurchase.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		tblPurchase.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
		tblPurchase.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
		tblPurchase.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				if (e.getClickCount() == 2) {
					int row = tblPurchase.getSelectedRow();
					if (row != -1) {
						int id = (int) tblPurchase.getValueAt(row, 0);
						PurchaseTransactions db = (PurchaseTransactions) DatabaseFacade
								.getDatabase("PurchaseTransactions");
						PurchaseTransaction transaction = db.get(db.find(id));
						new frames.Purchase(mainFrame, transaction);
					}
				}
			}
		});
		spTransaction.setViewportView(tblPurchase);

		JButton btnDelete = new JButton("DELETE");
		btnDelete.setBackground(_Settings.backgroundColor);
		btnDelete.setForeground(_Settings.labelColor);
		btnDelete.setFont(new Font("Arial", Font.BOLD, 13));
		btnDelete.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnDelete.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				int index = tblPurchase.getSelectedRow();
				if (index != -1) {
					int id = (int) tblPurchase.getValueAt(index, 0);
					PurchaseTransactions dbPurchase = (PurchaseTransactions) DatabaseFacade
							.getDatabase("PurchaseTransactions");
					PurchaseTransaction purchase = dbPurchase.get(dbPurchase.find(id));
					if (purchase != null) {
						for (Transaction transaction : purchase.getCosts())
							RecordFacade.removeTransaction(transaction.getId());
						for (Transaction transaction : purchase.getPayments())
							RecordFacade.removeTransaction(transaction.getId());

						double total = 0.0;
						for (Stock purchaseStock : purchase.getItems()) {
							Stock storeStock = ItemFacade.findStockById(purchase.getItems(),
									purchaseStock.getItem().getId());
							if (storeStock == null || storeStock.getQuantity() < purchaseStock.getQuantity()
									|| storeStock.getPrice() != purchaseStock.getPrice()) {
								new MyOptionPane(
										"This record cannot be deleted because purchased stock(s) in the store have been modified.",
										MyOptionPane.INFORMATION_MESSAGE);
								return;
							}
						}

						for (Stock purchaseStock : purchase.getItems()) {
							ItemFacade.unstock(purchase.getStore(), purchaseStock.getItem(),
									purchaseStock.getQuantity());
							total += (purchaseStock.getPrice() * purchaseStock.getQuantity());
						}

						String balanceId = ValueFormatter.formatBalanceId(purchase.getId(), PurchaseTransaction.class);
						RecordFacade.record(purchase.getStore().getInventory(), total, 0.0, true, null);
						RecordFacade.removeInventoryRecord(purchase.getStore().getInventory(), total, 0.0, true,
								balanceId);
						RecordFacade.record(null, purchase.getTotal(), 0.0, false, purchase.getSupplier());
						RecordFacade.removeUserRecord(purchase.getSupplier(), purchase.getTotal(), 0.0, false,
								balanceId);

						dbPurchase.remove(id);
						dbPurchase.setDirty(true);
						updateTable();
						LOGGER.Activity.log("Purchase", LOGGER.DELETE, balanceId);
					} else
						new MyOptionPane("This record cannot be deleted.", MyOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		add(btnDelete, "cell 10 7,grow");

		updateOptions();
		cbSupplier.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if ((e.getStateChange() == ItemEvent.SELECTED)) {
					updateTable();
				}
			}
		});
		cbStore.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if ((e.getStateChange() == ItemEvent.SELECTED)) {
					updateTable();
				}
			}
		});
		cbItemStatus.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if ((e.getStateChange() == ItemEvent.SELECTED)) {
					updateTable();
				}
			}
		});
		cbPaymentStatus.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if ((e.getStateChange() == ItemEvent.SELECTED)) {
					updateTable();
				}
			}
		});
		updateTable();
	}
}
