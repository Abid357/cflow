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

import cores.SaleTransaction;
import cores.Stock;
import cores.Store;
import cores.Transaction;
import cores.UserObject;
import databases.SaleTransactions;
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

public class Sale extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static String SORT_BY_DATE = "SORT BY: DATE";
	private static String SORT_BY_ID = "SORT BY: ID";

	private Main mainFrame;
	private JTable tblSale;
	private JDateChooser dcFrom, dcTo;
	private Date toDate, fromDate;
	private DefaultTableModel dtm;
	private String[] columnHeaders = { "ID", "USER", "STORE", "AMOUNT", "DATE", "ITEM STATUS", "PAYMENT STATUS" };
	private JComboBox<String> cbCustomer, cbPaymentStatus, cbItemStatus, cbStore;
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
		cbCustomer.removeAllItems();
		cbCustomer.addItem("ANY");
		List<String> ttUser = new ArrayList<String>();
		UserObjects userObjects = (UserObjects) DatabaseFacade.getDatabase("UserObjects");
		List<UserObject> userObjectList = userObjects.getList();
		for (UserObject userObject : userObjectList) {
			String userString = ValueFormatter.formatUserObject(userObject);
			cbCustomer.addItem(userString);
			ttUser.add(userString);
		}
		ttUser.sort(null);
		ttUser.add(0, "ANY");
		mcbrCustomer.setTooltips(ttUser);
		cbCustomer.setSelectedIndex(0);

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
		cbCustomer.setSelectedIndex(0);
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
		SaleTransactions db = (SaleTransactions) DatabaseFacade.getDatabase("SaleTransactions");
		List<SaleTransaction> list = db.getList();
		
		String selectedCustomer = (String) cbCustomer.getSelectedItem();
		if (!selectedCustomer.equals("ANY")) {
			UserObject userObject = ValueFormatter.parseUserObject(selectedCustomer);
			List<SaleTransaction> customerSales = new ArrayList<SaleTransaction>();
			for (SaleTransaction transaction : list)
				if (transaction.getCustomer().equals(userObject))
					customerSales.add(transaction);
			list = customerSales;
		}
		if (fromDate != null) {
			List<SaleTransaction> datedSales = new ArrayList<SaleTransaction>();
			for (SaleTransaction transaction : list)
				if (transaction.getDate().after(fromDate) || transaction.getDate().equals(fromDate))
					datedSales.add(transaction);
			list = datedSales;
		}
		if (toDate != null) {
			List<SaleTransaction> datedSales = new ArrayList<SaleTransaction>();
			for (SaleTransaction transaction : list)
				if (transaction.getDate().before(toDate) || transaction.getDate().equals(toDate))
					datedSales.add(transaction);
			list = datedSales;
		}
		String selectedStore = (String) cbStore.getSelectedItem();
		if (!selectedStore.equals("ANY")) {
			List<SaleTransaction> storeSales = new ArrayList<SaleTransaction>();
			Store store = ValueFormatter.parseStore(selectedStore);
			for (SaleTransaction transaction : list)
				if (transaction.getStore().equals(store))
					storeSales.add(transaction);
			list = storeSales;
		}
		String selectedItemStatus = (String) cbItemStatus.getSelectedItem();
		if (!selectedItemStatus.equals("ANY")) {
			List<SaleTransaction> statusSales = new ArrayList<SaleTransaction>();
			for (SaleTransaction transaction : list)
				if (transaction.getItemStatus().equals(TransactionStatus.valueOf(selectedItemStatus)))
					statusSales.add(transaction);
			list = statusSales;
		}
		String selectedPaymentStatus = (String) cbPaymentStatus.getSelectedItem();
		if (!selectedPaymentStatus.equals("ANY")) {
			List<SaleTransaction> statusSales = new ArrayList<SaleTransaction>();
			for (SaleTransaction transaction : list)
				if (transaction.getPaymentStatus().equals(TransactionStatus.valueOf(selectedPaymentStatus)))
					statusSales.add(transaction);
			list = statusSales;
		}
		
		if (btnSort.getText().equals(SORT_BY_DATE))
			Collections.sort(list);
		else
			Collections.sort(list, new Comparator<SaleTransaction>() {
				public int compare(SaleTransaction t1, SaleTransaction t2) {
					return t2.getDate().compareTo(t1.getDate());
				}
			});
		
		for (SaleTransaction transaction : list) {
			dtm.addRow(new Object[] { transaction.getId(), ValueFormatter.formatUserObject(transaction.getCustomer()),
					ValueFormatter.formatStore(transaction.getStore()),
					ValueFormatter.formatMoney(transaction.getTotal()),
					ValueFormatter.formatDate(transaction.getDate()), transaction.getItemStatus(),
					transaction.getPaymentStatus() });
		}
	}

	/**
	 * Create the panel.
	 */
	public Sale(Main frame) {
		this.mainFrame = frame;
		setBackground(_Settings.backgroundColor);
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setLayout(new MigLayout("", "[150][10][150][10][150][10][150][10][150][10][150][10][150][grow]", "[50][10][20][30][10][grow][10][50]"));

		JButton btnAddSale = new JButton("ADD SALE");
		btnAddSale.setBackground(_Settings.backgroundColor);
		btnAddSale.setForeground(_Settings.labelColor);
		btnAddSale.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnAddSale.setFont(new Font("Arial", Font.BOLD, 14));
		btnAddSale.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				new frames.Sale(mainFrame, null);
			}
		});
		add(btnAddSale, "cell 0 0,grow");

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
				updateTable();
			}
		});

		JButton btnGenerateReport = new JButton("PRINT STATEMENT");
		btnGenerateReport.setBackground(_Settings.backgroundColor);
		btnGenerateReport.setForeground(_Settings.labelColor);
		btnGenerateReport.setFont(new Font("Arial", Font.BOLD, 14));
		btnGenerateReport.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		add(btnGenerateReport, "cell 2 0,grow");
		add(btnSort, "cell 10 0,grow");

		JLabel lblCustomer = new JLabel("Customer");
		lblCustomer.setFont(new Font("Arial", Font.BOLD, 15));
		lblCustomer.setHorizontalAlignment(SwingConstants.CENTER);
		lblCustomer.setBorder(null);
		lblCustomer.setBackground(null);
		lblCustomer.setForeground(_Settings.labelColor);
		add(lblCustomer, "cell 0 2,grow");

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
		cbCustomer = new JComboBox<String>(new SortedComboBoxModel<String>());
		cbCustomer.setRenderer(mcbrCustomer);
		cbCustomer.setMaximumSize(new Dimension(150, 32767));
		cbCustomer.setFont(new Font("Century Gothic", Font.BOLD, 14));
		cbCustomer.setBorder(null);
		cbCustomer.setForeground(Color.DARK_GRAY);
		add(cbCustomer, "cell 0 3,grow");

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
		tblSale = new JTable(dtm);
		tblSale.setGridColor(_Settings.labelColor);
		tblSale.getTableHeader().setForeground(_Settings.labelColor);
		tblSale.getTableHeader().setBackground(_Settings.backgroundColor);
		tblSale.getTableHeader().setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		tblSale.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
		tblSale.setFont(new Font("Century Gothic", Font.BOLD, 16));
		tblSale.setBackground(_Settings.backgroundColor);
		tblSale.setForeground(_Settings.textFieldColor);
		tblSale.setRowHeight(30);
		tblSale.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		tblSale.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		tblSale.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
		tblSale.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
		tblSale.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				if (e.getClickCount() == 2) {
					int row = tblSale.getSelectedRow();
					if (row != -1) {
						int id = (int) tblSale.getValueAt(row, 0);
						SaleTransactions db = (SaleTransactions) DatabaseFacade.getDatabase("SaleTransactions");
						SaleTransaction transaction = db.get(db.find(id));
						new frames.Sale(mainFrame, transaction);
					}
				}
			}
		});
		spTransaction.setViewportView(tblSale);

		JButton btnDelete = new JButton("DELETE");
		btnDelete.setBackground(_Settings.backgroundColor);
		btnDelete.setForeground(_Settings.labelColor);
		btnDelete.setFont(new Font("Arial", Font.BOLD, 13));
		btnDelete.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnDelete.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				int index = tblSale.getSelectedRow();
				if (index != -1) {
					int id = (int) tblSale.getValueAt(index, 0);
					SaleTransactions dbSale = (SaleTransactions) DatabaseFacade.getDatabase("SaleTransactions");
					SaleTransaction sale = dbSale.get(dbSale.find(id));
					if (sale != null) {
						for (Transaction transaction : sale.getCosts())
							RecordFacade.removeTransaction(transaction.getId());
						for (Transaction transaction : sale.getPayments())
							RecordFacade.removeTransaction(transaction.getId());
						
						double total = 0.0;
						for (Stock saleStock : sale.getItems()) {
							ItemFacade.stock(sale.getStore(), saleStock.getItem(), saleStock.getQuantity(), saleStock.getOriginalPrice());
								total += (saleStock.getOriginalPrice() * saleStock.getQuantity());
						}

						String balanceId = ValueFormatter.formatBalanceId(sale.getId(), SaleTransaction.class);
						RecordFacade.record(sale.getStore().getInventory(), total, 0.0, false, null);
						RecordFacade.removeInventoryRecord(sale.getStore().getInventory(), total, 0.0, false, balanceId);
						RecordFacade.record(null, sale.getTotal(), 0.0, true, sale.getCustomer());
						RecordFacade.removeUserRecord(sale.getCustomer(), sale.getTotal(), 0.0, true, balanceId);
						
						dbSale.remove(id);
						dbSale.setDirty(true);
						updateTable();

						LOGGER.Activity.log("Sale", LOGGER.DELETE, balanceId);
					}
					else
						new MyOptionPane("This record cannot be deleted.",
								MyOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		add(btnDelete, "cell 10 7,grow");

		updateOptions();
		cbCustomer.addItemListener(new ItemListener() {

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
