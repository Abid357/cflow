package frames;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import cores.Item;
import cores.PurchaseTransaction;
import cores.Stock;
import cores.Store;
import cores.Transaction;
import cores.TransactionCategory;
import cores.UserObject;
import databases.Items;
import databases.PurchaseTransactions;
import databases.UserCategories;
import globals.DatabaseFacade;
import globals.ItemFacade;
import globals.LOGGER;
import globals.RecordFacade;
import globals.ValueFormatter;
import globals._Settings;
import helpers.ItemType;
import helpers.MyComboBoxRenderer;
import helpers.SortedComboBoxModel;
import helpers.TransactionStatus;
import helpers.TransactionType;
import net.miginfocom.swing.MigLayout;

public class Purchase extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtId, txtVat, txtStatus, txtSubtotal, txtDiscount, txtTotal, txtInvoice;
	private JLabel lblId, lblVat, lblCategory, lblSupplier, lblStore, lblDate, lblTotal, lblNo, lblItem, lblQuantity,
			lblPrice, lblUnit, lblAmount, lblItems, lblItemStatus, lblPayments, lblPaymentStatus, lblDiscount, lblPaid,
			lblPaidAmount, lblPending, lblPendingAmount, lblInvoice, lblSubtotal;
	private boolean isDateEmpty = true, isSupplierSelected, isStoreSelected, oneItemSelected, containsZero = true;
	private cores.PurchaseTransaction transaction;
	private JComboBox<String> cbCategory, cbSupplier, cbStore;
	private JDateChooser dateChooser;
	private Date date;
	private JButton btnNew, btnSave, btnClose, btnDeliver, btnPay;
	private List<JLabel> lblUnits, lblNos;
	private List<JTextField> txtQuantitys, txtPrices, txtAmounts, txtNews;
	private List<JComboBox<String>> cbItems;
	private JLabel lblOther;
	private JTextField txtOther;
	private JButton btnOther;
	private List<Transaction> otherTransactions;
	private JLabel lblLpo;
	private JLabel lblPi;
	private JTextField txtLpo;
	private JTextField txtPi;
	private JLabel lblNewPrice;
	private JLabel lblNew;
	private JLabel lblOriginal;

	private void calculateNewPrice() {
		double cost = ValueFormatter.parseMoney(txtOther.getText());
		cost += ValueFormatter.parseMoney(txtVat.getText());
		List<Double> quantities = new ArrayList<Double>();
		List<Double> oldPrices = new ArrayList<Double>();
		List<Double> conversions = new ArrayList<Double>();
		for (int i = 0; i < cbItems.size(); i++)
			if (cbItems.get(i).getSelectedIndex() != -1) {
				Item item = ValueFormatter.parseItemFromComboBox((String) cbItems.get(i).getSelectedItem());
				quantities.add(ValueFormatter.parseQuantity(txtQuantitys.get(i).getText()));
				conversions.add(item.getConversion());
				oldPrices.add(ValueFormatter.parseMoney(txtPrices.get(i).getText()));
			}
		double deno = 0;
		for (int i = 0; i < quantities.size(); i++)
			deno += (quantities.get(i) * conversions.get(i));
		double fraction = 0;
		if (deno != 0)
			fraction = cost / deno;
		for (int i = 0; i < quantities.size(); i++)
			txtNews.get(i).setText(ValueFormatter.formatMoney(oldPrices.get(i) + (fraction * conversions.get(i))));
	}

	public void setTransaction(PurchaseTransaction transaction) {
		this.transaction = transaction;
	}

	public PurchaseTransaction getTransaction() {
		return transaction;
	}

	public List<Transaction> getOtherTransactions() {
		return otherTransactions;
	}

	public JDialog getThis() {
		return this;
	}

	private double calculatePending() {
		return transaction.getTotal() - calculatePaid();
	}

	private double calculatePaid() {
		double paid = 0;
		for (Transaction t : transaction.getPayments())
			paid += calculateTotalForTransaction(t.getAmount(), t.getTax());
		return paid;
	}

	private boolean deliverItems() {
		Store store = transaction.getStore();
		double total = 0.0;
		for (Stock purchaseStock : transaction.getItems()) {
			if (ItemFacade.stock(store, purchaseStock.getItem(), purchaseStock.getQuantity(), purchaseStock.getPrice())
					&& !purchaseStock.getItem().getType().equals(ItemType.SERVICE))
				total += (purchaseStock.getPrice() * purchaseStock.getQuantity());
		}

		String balanceId = ValueFormatter.formatBalanceId(transaction.getId(), PurchaseTransaction.class);
		if (total != 0.0) {
			RecordFacade.record(store.getInventory(), total, 0.0, true, null);
			RecordFacade.addInventoryRecord(store.getInventory(), total, 0.0, true, balanceId, date);
		}
		RecordFacade.record(null, transaction.getTotal(), 0.0, false, transaction.getSupplier());
		RecordFacade.addUserRecord(transaction.getSupplier(), transaction.getTotal(), 0.0, false, balanceId, date);

		transaction.setLpoNo(ValueFormatter.formatTextSafely(txtLpo.getText()));
		transaction.setPiNo(ValueFormatter.formatTextSafely(txtPi.getText()));
		transaction.setInvoiceNo(ValueFormatter.formatTextSafely(txtInvoice.getText()));
		PurchaseTransactions db = (PurchaseTransactions) DatabaseFacade.getDatabase("PurchaseTransactions");
		db.setDirty(true);
		transaction.setItemStatus(TransactionStatus.DELIVERED);
		txtLpo.setEnabled(false);
		txtPi.setEnabled(false);
		txtInvoice.setEnabled(false);
		return true;
	}

	private boolean addTransaction(int id, UserObject supplier, Store store, List<Stock> items, double discount,
			double vat, double total, List<Transaction> payments, TransactionStatus itemStatus,
			TransactionStatus paymentStatus, String lpoNo, String piNo, String invoiceNo) {
		PurchaseTransactions dbPurchases = (PurchaseTransactions) DatabaseFacade.getDatabase("PurchaseTransactions");
		return dbPurchases.add(id, date, supplier, store, items, discount, vat, total, payments, otherTransactions,
				itemStatus, paymentStatus, lpoNo, piNo, invoiceNo);
	}

	private double calculateAmount(double quantity, double price) {
		return quantity * price;
	}

	private void updatePaymentStatus() {
		double pending = calculatePending();
		double paid = calculatePaid();
		if (!lblPaidAmount.getText().isEmpty()) {
			double oldPaid = ValueFormatter.parseMoney(lblPaidAmount.getText());
			if (paid != oldPaid)
				txtStatus.setText("Payment added!");
		}
		lblPendingAmount.setText(ValueFormatter.formatMoney(pending));
		lblPaidAmount.setText(ValueFormatter.formatMoney(paid));
		transaction.setPaymentStatus(paid == 0 ? TransactionStatus.UNPAID
				: pending > 0 ? TransactionStatus.PENDING : TransactionStatus.PAID);
		lblPaymentStatus.setText(transaction.getPaymentStatus().toString());
		btnPay.setEnabled(!transaction.getPaymentStatus().equals(TransactionStatus.PAID));
	}

	private double calculateSubtotal() {
		double sum = 0;
		for (JTextField txtAmount : txtAmounts) {
			String string = txtAmount.getText();
			if (!string.isEmpty())
				sum += ValueFormatter.parseMoney(txtAmount.getText());
		}
		return sum;
	}

	private double calculateVat() {
		String string = txtSubtotal.getText();
		double subtotal = 0;
		if (!string.isEmpty())
			subtotal = ValueFormatter.parseMoney(string);
		return subtotal * 0.05;
	}

	private double calculateTotalForTransaction(double amount, double tax) {
		return tax + amount;
	}

	private double calculateTotal() {
		double subtotal = 0;
		String string = txtSubtotal.getText();
		if (!string.isEmpty())
			subtotal = ValueFormatter.parseMoney(string);
		double vat = 0;
		string = txtVat.getText();
		if (!string.isEmpty())
			vat = ValueFormatter.parseMoney(string);
		double discount = 0;
		string = txtDiscount.getText();
		if (!string.isEmpty())
			discount = ValueFormatter.parseMoney(string);
		double other = 0;
		string = txtOther.getText();
		if (!string.isEmpty())
			other = ValueFormatter.parseMoney(string);
		return subtotal + vat - discount + other;
	}

	private double calculateCosts() {
		double cost = 0.0;
		List<Transaction> costs = transaction.getCosts();
		if (costs != null)
			for (Transaction t : costs)
				cost += t.getAmount();
		return cost;
	}

	private void validateButton() {
		containsZero = checkForZeroAmounts();
		oneItemSelected = !txtAmounts.get(0).getText().isEmpty();
		if (!isSupplierSelected)
			txtStatus.setText("Select a supplier!");
		else if (!isStoreSelected)
			txtStatus.setText("Select a store!");
		else if (isDateEmpty)
			txtStatus.setText("Select a date!");
		else if (!oneItemSelected)
			txtStatus.setText("Select at least one item!");
		else if (containsZero)
			txtStatus.setText("Amount value for an item cannot be zero!");
		else
			txtStatus.setText("");
		btnSave.setEnabled(oneItemSelected && isSupplierSelected && isStoreSelected && !isDateEmpty && !containsZero);
	}

	private void populateFrame() {
		setFormEnabled(false);
		txtId.setText(Integer.toString(transaction.getId()));
		cbSupplier.addItem(ValueFormatter.formatUserObject(transaction.getSupplier()));
		cbSupplier.setSelectedIndex(0);
		cbStore.setSelectedItem(ValueFormatter.formatStore(transaction.getStore()));
		dateChooser.setDate(transaction.getDate());
		date = transaction.getDate();
		List<Stock> items = transaction.getItems();
		for (int i = 0; i < items.size(); i++) {
			ItemListener listener = cbItems.get(i).getItemListeners()[0];
			cbItems.get(i).removeItemListener(listener);
			cbItems.get(i).setSelectedItem(ValueFormatter.formatItemForComboBox(items.get(i).getItem()));
			cbItems.get(i).addItemListener(listener);
			cbItems.get(i).setEnabled(false);
			txtQuantitys.get(i).setText(ValueFormatter.formatQuantity(items.get(i).getQuantity()));
			txtNews.get(i).setText(ValueFormatter.formatMoney(items.get(i).getPrice()));
		}
		txtVat.setText(ValueFormatter.formatMoney(transaction.getVat()));
		txtTotal.setText(ValueFormatter.formatMoney(transaction.getTotal()));
		txtSubtotal.setText(ValueFormatter.formatMoney(
				transaction.getTotal() - transaction.getVat() - transaction.getDiscount() - calculateCosts()));
		lblItemStatus.setText(transaction.getItemStatus().toString());
		if (transaction.getItemStatus().equals(TransactionStatus.UNDELIVERED))
			btnDeliver.setEnabled(true);
		updatePaymentStatus();
		txtStatus.setText("");
		btnSave.setEnabled(false);
		btnOther.setEnabled(false);
	}

	private void setFormEnabled(boolean isEnabled) {
		for (JTextField txt : txtAmounts)
			txt.setEnabled(isEnabled);
		for (JTextField txt : txtQuantitys)
			txt.setEnabled(isEnabled);
		for (JTextField txt : txtPrices)
			txt.setEnabled(isEnabled);
		for (JTextField txt : txtNews)
			txt.setEnabled(isEnabled);
		for (JComboBox<String> cb : cbItems)
			cb.setEnabled(isEnabled);
		cbCategory.setEnabled(isEnabled);
		cbSupplier.setEnabled(isEnabled);
		cbStore.setEnabled(isEnabled);
		dateChooser.setEnabled(isEnabled);
		txtSubtotal.setEnabled(isEnabled);
		txtVat.setEnabled(isEnabled);
		txtOther.setEnabled(isEnabled);
		txtTotal.setEnabled(isEnabled);
		txtDiscount.setEnabled(isEnabled);
		txtLpo.setEnabled(isEnabled);
		txtPi.setEnabled(isEnabled);
		txtInvoice.setEnabled(isEnabled);
		btnSave.setEnabled(isEnabled);
	}

	private boolean checkForZeroAmounts() {
		for (JTextField txtAmount : txtAmounts)
			if (!txtAmount.getText().isEmpty())
				if (ValueFormatter.parseMoney(txtAmount.getText()) == 0)
					return true;
		return false;
	}

	private boolean isValidNumeric(JTextField txt) {
		return Pattern.compile("([0-9]{1,3}(\\,)?)*(\\.[0-9]*)?").matcher(txt.getText()).matches();
	}

	private void resetUnits() {
		for (JLabel lbl : lblUnits)
			lbl.setText("");
	}

	private void resetAmounts() {
		for (JTextField txt : txtAmounts)
			txt.setText("");
	}

	private void resetPrices() {
		for (JTextField txt : txtPrices) {
			txt.setText("");
			txt.setEnabled(false);
		}
	}

	private void resetNews() {
		for (JTextField txt : txtNews)
			txt.setText("");
	}

	private void resetQuantitys() {
		for (JTextField txt : txtQuantitys) {
			txt.setText("");
			txt.setEnabled(false);
		}
	}

	private void resetItems() {
		for (JComboBox<String> cb : cbItems) {
			ItemListener listener = cb.getItemListeners()[0];
			cb.removeItemListener(listener);
			cb.removeAllItems();
			cb.setEnabled(false);
			cb.addItemListener(listener);
		}
	}

	private void reset() {
		resetItems();
		resetAmounts();
		resetQuantitys();
		resetPrices();
		resetNews();
		resetUnits();
		cbStore.setSelectedIndex(-1);
		cbSupplier.setSelectedIndex(-1);
		cbCategory.setSelectedIndex(-1);
		lblPaidAmount.setText("0.00");
		lblPendingAmount.setText("0.00");
		lblItemStatus.setText("");
		lblPaymentStatus.setText("");
		txtInvoice.setText("");
		txtStatus.setText("");
		containsZero = oneItemSelected = isStoreSelected = isSupplierSelected = false;
		btnSave.setEnabled(false);
		btnOther.setEnabled(true);
		txtId.setText(Integer
				.toString(((PurchaseTransactions) DatabaseFacade.getDatabase("PurchaseTransactions")).maxID() + 1));
	}

	/**
	 * Create the frame.
	 */
	@SuppressWarnings("unchecked")
	public Purchase(Window owner, cores.PurchaseTransaction transaction) {
		super(owner, "Purchase Transaction", Dialog.ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		setBounds(100, 100, 850, 700);
		contentPane = new JPanel();
		contentPane.setBackground(_Settings.backgroundColor);
		contentPane.setBorder(null);
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("",
				"[10][60][10][30][20][30][10][60][20][10][40][10][55][10][85][10][10][80][10][10][10][20,grow][10][20,grow][30,grow][10][10][90][10][10][100][10]",
				"[20][10][40][10][40][10][20][20][20][20][20][20][20][20][20][20][20][20][10][20][10][10][20][20][20][10][20][20][10][20][10][60,grow][10]"));

		this.transaction = transaction;
		otherTransactions = new ArrayList<Transaction>();

		UIManager.put("ComboBox.disabledForeground", _Settings.labelColor);
		UIManager.put("TextField.inactiveForeground", _Settings.labelColor);
		UIManager.put("TextArea.inactiveForeground", _Settings.labelColor);

		lblLpo = new JLabel("LPO");
		lblLpo.setHorizontalAlignment(SwingConstants.RIGHT);
		lblLpo.setForeground(_Settings.labelColor);
		lblLpo.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblLpo.setBorder(null);
		lblLpo.setBackground(_Settings.backgroundColor);
		contentPane.add(lblLpo, "cell 7 0 2 1,grow");

		txtLpo = new JTextField();
		txtLpo.setHorizontalAlignment(SwingConstants.CENTER);
		txtLpo.setForeground(_Settings.textFieldColor);
		txtLpo.setFont(new Font("Century Gothic", Font.PLAIN, 17));
		txtLpo.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtLpo.setBackground(_Settings.backgroundColor);
		contentPane.add(txtLpo, "cell 10 0 4 1,grow");

		lblPi = new JLabel("PI");
		lblPi.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPi.setForeground(_Settings.labelColor);
		lblPi.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblPi.setBorder(null);
		lblPi.setBackground(_Settings.backgroundColor);
		contentPane.add(lblPi, "cell 14 0,grow");

		txtPi = new JTextField();
		txtPi.setHorizontalAlignment(SwingConstants.CENTER);
		txtPi.setForeground(_Settings.textFieldColor);
		txtPi.setFont(new Font("Century Gothic", Font.PLAIN, 17));
		txtPi.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtPi.setBackground(_Settings.backgroundColor);
		contentPane.add(txtPi, "cell 16 0 4 1,grow");

		lblInvoice = new JLabel("Invoice");
		lblInvoice.setHorizontalAlignment(SwingConstants.RIGHT);
		lblInvoice.setForeground(_Settings.labelColor);
		lblInvoice.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblInvoice.setBorder(null);
		lblInvoice.setBackground(_Settings.backgroundColor);
		contentPane.add(lblInvoice, "cell 21 0 4 1,alignx trailing,growy");

		txtInvoice = new JTextField();
		txtInvoice.setHorizontalAlignment(SwingConstants.CENTER);
		txtInvoice.setForeground(_Settings.textFieldColor);
		txtInvoice.setFont(new Font("Century Gothic", Font.PLAIN, 17));
		txtInvoice.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtInvoice.setBackground(_Settings.backgroundColor);
		contentPane.add(txtInvoice, "cell 26 0 3 1,grow");

		lblId = new JLabel("Transaction ID");
		lblId.setBorder(null);
		lblId.setHorizontalAlignment(SwingConstants.CENTER);
		lblId.setFont(new Font("Arial Black", Font.PLAIN, 11));
		lblId.setForeground(_Settings.labelColor);
		contentPane.add(lblId, "cell 1 1 4 1,grow");

		txtId = new JTextField();
		txtId.setText(Integer
				.toString(((PurchaseTransactions) DatabaseFacade.getDatabase("PurchaseTransactions")).maxID() + 1));
		txtId.setEditable(false);
		txtId.setHorizontalAlignment(SwingConstants.CENTER);
		txtId.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtId.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtId.setBackground(null);
		txtId.setForeground(_Settings.labelColor);
		txtId.setColumns(5);
		contentPane.add(txtId, "cell 1 2 4 3,grow");

		SortedComboBoxModel<String> scbmUser = new SortedComboBoxModel<String>();
		MyComboBoxRenderer mcbrUser = new MyComboBoxRenderer();

		List<String> userCategories = (List<String>) DatabaseFacade.getDatabase("UserCategories").getList();
		SortedComboBoxModel<String> scbmUserCategory = new SortedComboBoxModel<String>();
		MyComboBoxRenderer mcbrUserCategory = new MyComboBoxRenderer();
		List<String> ttUserCategory = new ArrayList<String>();
		for (String category : userCategories) {
			scbmUserCategory.addElement(category);
			ttUserCategory.add(category);
		}
		ttUserCategory.sort(null);
		mcbrUserCategory.setTooltips(ttUserCategory);

		cbSupplier = new JComboBox<String>(scbmUser);
		cbSupplier.setRenderer(mcbrUser);
		cbSupplier.setMaximumSize(new Dimension(210, 40));
		cbSupplier.setFont(new Font("Century Gothic", Font.BOLD, 17));
		cbSupplier.setForeground(Color.DARK_GRAY);
		cbSupplier.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if ((e.getStateChange() == ItemEvent.SELECTED)) {
					isSupplierSelected = true;
					validateButton();
				}
			}
		});

		cbCategory = new JComboBox<String>(scbmUserCategory);
		cbCategory.setRenderer(mcbrUserCategory);
		cbCategory.setMaximumSize(new Dimension(210, 40));
		cbCategory.setFont(new Font("Century Gothic", Font.BOLD, 17));
		cbCategory.setForeground(Color.DARK_GRAY);
		cbCategory.setSelectedIndex(-1);
		cbCategory.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if ((e.getStateChange() == ItemEvent.SELECTED)) {
					String category = (String) cbCategory.getSelectedItem();
					UserCategories db = (UserCategories) DatabaseFacade.getDatabase("UserCategories");
					List<UserObject> userObjects = db.get(category);
					List<String> ttUser = new ArrayList<String>();
					cbSupplier.removeAllItems();
					for (UserObject userObject : userObjects) {
						String userString = ValueFormatter.formatUserObject(userObject);
						cbSupplier.addItem(userString);
						ttUser.add(userString);
					}
					cbSupplier.setSelectedIndex(-1);
					ttUser.sort(null);
					mcbrUser.setTooltips(ttUser);
					isSupplierSelected = false;
					validateButton();
				}
			}
		});

		lblCategory = new JLabel("<html><div style='text-align: center;'>User</div><div style='text-align: center;'>Category</div></html>");
		lblCategory.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCategory.setForeground(_Settings.labelColor);
		lblCategory.setBackground(null);
		lblCategory.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblCategory.setBorder(null);
		contentPane.add(lblCategory, "cell 6 2 3 1,grow");
		contentPane.add(cbCategory, "cell 10 2 6 1,grow");

		lblSupplier = new JLabel("Supplier");
		lblSupplier.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSupplier.setForeground(_Settings.labelColor);
		lblSupplier.setBackground(null);
		lblSupplier.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblSupplier.setBorder(null);
		contentPane.add(lblSupplier, "cell 16 2 2 1,grow");
		contentPane.add(cbSupplier, "cell 19 2 9 1,grow");

		List<Store> stores = (List<Store>) DatabaseFacade.getDatabase("Stores").getList();
		SortedComboBoxModel<String> scbmStore = new SortedComboBoxModel<String>();
		MyComboBoxRenderer mcbrStore = new MyComboBoxRenderer();
		List<String> ttStore = new ArrayList<String>();
		for (Store store : stores) {
			String storeString = ValueFormatter.formatStore(store);
			scbmStore.addElement(storeString);
			ttStore.add(storeString);
		}
		mcbrStore.setTooltips(ttStore);

		List<TransactionCategory> categories = (List<TransactionCategory>) DatabaseFacade
				.getDatabase("TransactionCategories").getList();
		SortedComboBoxModel<String> scbmCategory = new SortedComboBoxModel<String>();
		MyComboBoxRenderer mcbrCategory = new MyComboBoxRenderer();
		List<String> ttCategory = new ArrayList<String>();
		for (TransactionCategory category : categories) {
			scbmCategory.addElement(category.getName());
			ttCategory.add(category.getName());
		}
		ttCategory.sort(null);
		mcbrCategory.setTooltips(ttCategory);

		cbStore = new JComboBox<String>(scbmStore);
		cbStore.setRenderer(mcbrStore);
		cbStore.setMaximumSize(new Dimension(210, 32767));
		cbStore.setFont(new Font("Century Gothic", Font.BOLD, 17));
		cbStore.setForeground(Color.DARK_GRAY);
		cbStore.setSelectedIndex(-1);
		cbStore.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if ((e.getStateChange() == ItemEvent.SELECTED)) {
					isStoreSelected = true;
					validateButton();
					resetItems();
					resetAmounts();
					resetQuantitys();
					resetPrices();
					resetNews();
					resetUnits();

					Items db = (Items) DatabaseFacade.getDatabase("Items");
					List<Item> items = db.getList();
					for (JComboBox<String> cb : cbItems) {
						ItemListener listener = cb.getItemListeners()[0];
						cb.removeItemListener(listener);

						SortedComboBoxModel<String> scbmItem = new SortedComboBoxModel<String>(true);
						MyComboBoxRenderer mcbrItem = new MyComboBoxRenderer();
						List<String> ttItem = new ArrayList<String>();
						for (Item item: items) {
							String itemString = ValueFormatter.formatItemForComboBox(item);
							scbmItem.addElement(itemString);
							ttItem.add(itemString);
						}
						Collections.sort(ttItem, new Comparator<String>() {

							@Override
							public int compare(String arg0, String arg1) {
								// TODO Auto-generated method stub
								String string0 = arg0.substring(arg0.indexOf(" ") + 1);
								String string1 = arg1.substring(arg1.indexOf(" ") + 1);
								return string0.compareTo(string1);
							}
						});
						mcbrItem.setTooltips(ttItem);
						cb.setRenderer(mcbrItem);

						for (Item item : items)
							cb.addItem(ValueFormatter.formatItemForComboBox(item));
						cb.setSelectedIndex(-1);
						cb.addItemListener(listener);
					}
					cbItems.get(0).setEnabled(true);
				}
			}
		});

		lblStore = new JLabel("Store");
		lblStore.setHorizontalAlignment(SwingConstants.RIGHT);
		lblStore.setBackground(null);
		lblStore.setForeground(_Settings.labelColor);
		lblStore.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblStore.setBorder(null);
		contentPane.add(lblStore, "flowx,cell 6 4 3 1,grow");
		contentPane.add(cbStore, "cell 10 4 6 1,grow");

		lblDate = new JLabel("Date");
		lblDate.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDate.setForeground(_Settings.labelColor);
		lblDate.setBackground(null);
		lblDate.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblDate.setBorder(null);
		contentPane.add(lblDate, "cell 16 4 2 1,grow");

		dateChooser = new JDateChooser();
		dateChooser.setMaximumSize(new Dimension(210, 2147483647));
		dateChooser.setFont(new Font("Century Gothic", Font.BOLD, 17));
		dateChooser.setBorder(null);
		dateChooser.setDateFormatString("dd-MMM-yyyy");
		JTextFieldDateEditor dateEditor = (JTextFieldDateEditor) dateChooser.getComponent(1);
		dateChooser.addPropertyChangeListener("date", new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent e) {
				// TODO Auto-generated method stub
				date = (Date) e.getNewValue();
				isDateEmpty = false;
				validateButton();
			}
		});
		contentPane.add(dateChooser, "cell 19 4 9 1,grow");
		dateEditor.setHorizontalAlignment(JTextField.CENTER);
		dateEditor.setEnabled(false);
		dateEditor.setDisabledTextColor(Color.DARK_GRAY);

		lblOriginal = new JLabel("Original");
		lblOriginal.setVerticalAlignment(SwingConstants.BOTTOM);
		lblOriginal.setHorizontalAlignment(SwingConstants.CENTER);
		lblOriginal.setForeground(_Settings.labelColor);
		lblOriginal.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblOriginal.setBorder(null);
		lblOriginal.setBackground(_Settings.backgroundColor);
		contentPane.add(lblOriginal, "cell 20 6 6 1,grow");

		lblNew = new JLabel("New");
		lblNew.setVerticalAlignment(SwingConstants.BOTTOM);
		lblNew.setHorizontalAlignment(SwingConstants.CENTER);
		lblNew.setForeground(_Settings.labelColor);
		lblNew.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblNew.setBorder(null);
		lblNew.setBackground(_Settings.backgroundColor);
		contentPane.add(lblNew, "cell 30 6,grow");

		lblNo = new JLabel("No.");
		lblNo.setHorizontalAlignment(SwingConstants.CENTER);
		lblNo.setForeground(_Settings.labelColor);
		lblNo.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblNo.setBorder(null);
		lblNo.setBackground(_Settings.backgroundColor);
		contentPane.add(lblNo, "cell 1 7,grow");

		lblItem = new JLabel("Item");
		lblItem.setHorizontalAlignment(SwingConstants.CENTER);
		lblItem.setForeground(_Settings.labelColor);
		lblItem.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblItem.setBorder(null);
		lblItem.setBackground(_Settings.backgroundColor);
		contentPane.add(lblItem, "flowx,cell 3 7 10 1,grow");

		lblUnit = new JLabel("Unit");
		lblUnit.setHorizontalAlignment(SwingConstants.CENTER);
		lblUnit.setForeground(_Settings.labelColor);
		lblUnit.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblUnit.setBorder(null);
		lblUnit.setBackground(_Settings.backgroundColor);
		contentPane.add(lblUnit, "cell 17 7 2 1,grow");

		lblPrice = new JLabel("Price / unit");
		lblPrice.setHorizontalAlignment(SwingConstants.CENTER);
		lblPrice.setForeground(_Settings.labelColor);
		lblPrice.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblPrice.setBorder(null);
		lblPrice.setBackground(_Settings.backgroundColor);
		contentPane.add(lblPrice, "cell 20 7 6 1,grow");

		lblUnits = new ArrayList<JLabel>();
		for (int i = 0; i < 10; i++) {
			JLabel lbl = new JLabel();
			lbl.setHorizontalAlignment(SwingConstants.CENTER);
			lbl.setForeground(_Settings.labelColor);
			lbl.setBackground(_Settings.backgroundColor);
			lbl.setBorder(null);
			lbl.setFont(new Font("Century Gothic", Font.BOLD, 17));
			contentPane.add(lbl, "cell 17 " + (8 + i) + " 2 1,grow");
			lblUnits.add(lbl);
		}

		lblNos = new ArrayList<JLabel>();
		for (int i = 0; i < 10; i++) {
			JLabel lbl = new JLabel(Integer.toString(i + 1));
			lbl.setHorizontalAlignment(SwingConstants.CENTER);
			lbl.setForeground(_Settings.labelColor);
			lbl.setBackground(_Settings.backgroundColor);
			lbl.setBorder(null);
			lbl.setFont(new Font("Century Gothic", Font.BOLD, 17));
			contentPane.add(lbl, "cell 1 " + (8 + i) + ",grow");
			lblNos.add(lbl);
		}

		cbItems = new ArrayList<JComboBox<String>>();
		for (int i = 0; i < 10; i++) {
			final int index = i;
			SortedComboBoxModel<String> scbmCb = new SortedComboBoxModel<>(true);
			JComboBox<String> cb = new JComboBox<String>(scbmCb);
			cb.setMaximumSize(new Dimension(325, 999));
			cb.setEnabled(false);
			cb.setForeground(Color.DARK_GRAY);
			cb.setFont(new Font("Century Gothic", Font.BOLD, 14));
			cb.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent evt) {
					// TODO Auto-generated method stub
					if (evt.getStateChange() == ItemEvent.SELECTED) {
						JTextField txtQuantity = txtQuantitys.get(index);
						txtQuantity.setEnabled(true);
						txtQuantity.setText("0");
						JTextField txtPrice = txtPrices.get(index);
						txtPrice.setEnabled(true);
						txtPrice.setText("0.00");
						JTextField txtAmount = txtAmounts.get(index);
						txtAmount.setText("0.00");
						JLabel lblUnit = lblUnits.get(index);
						lblUnit.setText(ValueFormatter.parseItemFromComboBox((String) cb.getSelectedItem()).getUnit());
						calculateNewPrice();
						if (index != 9)
							cbItems.get(index + 1).setEnabled(true);
						validateButton();
					}
				}
			});
			contentPane.add(cb, "cell 3 " + (8 + i) + " 10 1,grow");
			cbItems.add(cb);
		}

		txtNews = new ArrayList<JTextField>();
		for (int i = 0; i < 10; i++) {
			JTextField txt = new JTextField();
			txt.setEditable(false);
			txt.setHorizontalAlignment(SwingConstants.CENTER);
			txt.setForeground(_Settings.textFieldColor);
			txt.setBackground(_Settings.backgroundColor);
			txt.setFont(new Font("Century Gothic", Font.BOLD, 17));
			txt.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
			txt.addFocusListener(new FocusAdapter() {

				@Override
				public void focusGained(FocusEvent e) {
					// TODO Auto-generated method stub
					calculateNewPrice();
				}
			});
			contentPane.add(txt, "cell 30 " + (8 + i) + ",grow");
			txtNews.add(txt);
		}

		txtQuantitys = new ArrayList<JTextField>();
		for (int i = 0; i < 10; i++) {
			final int index = i;
			JTextField txt = new JTextField();
			txt.setEnabled(false);
			txt.setHorizontalAlignment(SwingConstants.CENTER);
			txt.setForeground(_Settings.textFieldColor);
			txt.setBackground(_Settings.backgroundColor);
			txt.setFont(new Font("Century Gothic", Font.BOLD, 17));
			txt.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
			txt.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					// TODO Auto-generated method stub
					if (!isValidNumeric(txt)) {
						txt.setText("0");
						new MyOptionPane("Quantity must be positive number!", MyOptionPane.INFORMATION_MESSAGE);
						return;
					} else {
						double quantity = ValueFormatter.parseQuantity(txt.getText());
						txt.setText(ValueFormatter.formatQuantity(quantity));
						JTextField txtPrice = txtPrices.get(index);
						JTextField txtAmount = txtAmounts.get(index);
						double price = ValueFormatter.parseMoney(txtPrice.getText());
						txtAmount.setText(ValueFormatter.formatMoney(calculateAmount(quantity, price)));
						calculateNewPrice();
						validateButton();
					}
				}

				@Override
				public void focusGained(FocusEvent e) {
					// TODO Auto-generated method stub
					txt.selectAll();
				}
			});
			contentPane.add(txt, "cell 14 " + (8 + i) + " 2 1,grow");
			txtQuantitys.add(txt);
		}

		txtPrices = new ArrayList<JTextField>();
		for (int i = 0; i < 10; i++) {
			final int index = i;
			JTextField txt = new JTextField();
			txt.setEnabled(false);
			txt.setHorizontalAlignment(SwingConstants.CENTER);
			txt.setForeground(_Settings.textFieldColor);
			txt.setBackground(_Settings.backgroundColor);
			txt.setFont(new Font("Century Gothic", Font.BOLD, 17));
			txt.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
			txt.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					// TODO Auto-generated method stub
					if (!isValidNumeric(txt)) {
						txt.setText("0.00");
						new MyOptionPane("Price must be positive number!", MyOptionPane.INFORMATION_MESSAGE);
						return;
					} else {
						double price = ValueFormatter.parseMoney(txt.getText());
						txt.setText(ValueFormatter.formatMoney(price));
						JTextField txtAmount = txtAmounts.get(index);
						JTextField txtQuantity = txtQuantitys.get(index);
						double quantity = ValueFormatter.parseQuantity(txtQuantity.getText());
						txtAmount.setText(ValueFormatter.formatMoney(calculateAmount(quantity, price)));
						calculateNewPrice();
						validateButton();
					}
				}

				@Override
				public void focusGained(FocusEvent e) {
					// TODO Auto-generated method stub
					txt.selectAll();
				}
			});
			contentPane.add(txt, "cell 20 " + (8 + i) + " 6 1,grow");
			txtPrices.add(txt);
		}

		txtAmounts = new ArrayList<JTextField>();
		for (int i = 0; i < 10; i++) {
			final int index = i;
			JTextField txt = new JTextField();
			txt.setEnabled(true);
			txt.setEditable(false);
			txt.setHorizontalAlignment(SwingConstants.CENTER);
			txt.setForeground(_Settings.textFieldColor);
			txt.setBackground(_Settings.backgroundColor);
			txt.setFont(new Font("Century Gothic", Font.BOLD, 17));
			txt.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
			txt.getDocument().addDocumentListener(new DocumentListener() {

				@Override
				public void removeUpdate(DocumentEvent arg0) {
					// TODO Auto-generated method stub
					txtSubtotal.setText(ValueFormatter.formatMoney(calculateSubtotal()));
				}

				@Override
				public void insertUpdate(DocumentEvent arg0) {
					// TODO Auto-generated method stub
					txtSubtotal.setText(ValueFormatter.formatMoney(calculateSubtotal()));
				}

				@Override
				public void changedUpdate(DocumentEvent arg0) {
					// TODO Auto-generated method stub
					txtSubtotal.setText(ValueFormatter.formatMoney(calculateSubtotal()));
				}
			});
			txt.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent arg0) {
					// TODO Auto-generated method stub
					if (!txt.getText().isEmpty()) {
						JTextField txtPrice = txtPrices.get(index);
						JTextField txtQuantity = txtQuantitys.get(index);
						double price = ValueFormatter.parseMoney(txtPrice.getText());
						double quantity = ValueFormatter.parseQuantity(txtQuantity.getText());
						txt.setText(ValueFormatter.formatMoney(calculateAmount(quantity, price)));
					}
				}

				@Override
				public void focusGained(FocusEvent arg0) {
					// TODO Auto-generated method stub

				}
			});
			contentPane.add(txt, "cell 27 " + (8 + i) + " 2 1,grow");
			txtAmounts.add(txt);
		}

		lblAmount = new JLabel("Amount");
		lblAmount.setHorizontalAlignment(SwingConstants.CENTER);
		lblAmount.setForeground(_Settings.labelColor);
		lblAmount.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblAmount.setBorder(null);
		lblAmount.setBackground(_Settings.backgroundColor);
		contentPane.add(lblAmount, "cell 27 7 2 1,grow");

		lblNewPrice = new JLabel("Price / unit");
		lblNewPrice.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewPrice.setForeground(_Settings.labelColor);
		lblNewPrice.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblNewPrice.setBorder(null);
		lblNewPrice.setBackground(_Settings.backgroundColor);
		contentPane.add(lblNewPrice, "cell 30 7,grow");

		lblItems = new JLabel("Item(s):");
		lblItems.setHorizontalAlignment(SwingConstants.LEFT);
		lblItems.setForeground(_Settings.labelColor);
		lblItems.setFont(new Font("Arial Black", Font.PLAIN, 14));
		lblItems.setBorder(null);
		lblItems.setBackground(_Settings.backgroundColor);
		contentPane.add(lblItems, "cell 1 19,grow");

		lblItemStatus = new JLabel();
		lblItemStatus.setHorizontalAlignment(SwingConstants.LEFT);
		lblItemStatus.setForeground(_Settings.textFieldColor);
		lblItemStatus.setFont(new Font("Century Gothic", Font.BOLD | Font.ITALIC, 14));
		lblItemStatus.setBorder(null);
		lblItemStatus.setBackground(_Settings.backgroundColor);
		contentPane.add(lblItemStatus, "cell 2 19 4 1,grow");

		lblPayments = new JLabel("Payment(s):");
		lblPayments.setHorizontalAlignment(SwingConstants.LEFT);
		lblPayments.setForeground(_Settings.labelColor);
		lblPayments.setFont(new Font("Arial Black", Font.PLAIN, 14));
		lblPayments.setBorder(null);
		lblPayments.setBackground(_Settings.backgroundColor);
		contentPane.add(lblPayments, "cell 7 19 2 1,grow");

		lblPaymentStatus = new JLabel();
		lblPaymentStatus.setHorizontalAlignment(SwingConstants.LEFT);
		lblPaymentStatus.setForeground(_Settings.textFieldColor);
		lblPaymentStatus.setFont(new Font("Century Gothic", Font.BOLD | Font.ITALIC, 14));
		lblPaymentStatus.setBorder(null);
		lblPaymentStatus.setBackground(_Settings.backgroundColor);
		contentPane.add(lblPaymentStatus, "cell 9 19 4 1,grow");

		txtVat = new JTextField();
		txtVat.setText("0.00");
		txtVat.setHorizontalAlignment(SwingConstants.CENTER);
		txtVat.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtVat.setFont(new Font("Century Gothic", Font.BOLD, 17));
		txtVat.setBackground(_Settings.backgroundColor);
		txtVat.setForeground(_Settings.textFieldColor);
		txtVat.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				if (!isValidNumeric(txtVat)) {
					txtVat.setText("0.00");
					new MyOptionPane("VAT must be positive number!", MyOptionPane.INFORMATION_MESSAGE);
					return;
				} else {
					double vat = ValueFormatter.parseMoney(txtVat.getText());
					txtVat.setText(ValueFormatter.formatMoney(vat));
					txtTotal.setText(ValueFormatter.formatMoney(calculateTotal()));
				}
				validateButton();
			}

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				txtVat.selectAll();
				btnSave.setEnabled(false);
			}
		});
		contentPane.add(txtVat, "cell 23 20 6 2,grow");

		lblSubtotal = new JLabel("Subtotal");
		lblSubtotal.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSubtotal.setForeground(_Settings.labelColor);
		lblSubtotal.setFont(new Font("Arial Black", Font.PLAIN, 14));
		lblSubtotal.setBorder(null);
		lblSubtotal.setBackground(_Settings.backgroundColor);
		contentPane.add(lblSubtotal, "cell 16 19 6 1,grow");

		txtSubtotal = new JTextField();
		txtSubtotal.setEditable(false);
		txtSubtotal.setText("0.00");
		txtSubtotal.setHorizontalAlignment(SwingConstants.CENTER);
		txtSubtotal.setForeground(_Settings.textFieldColor);
		txtSubtotal.setFont(new Font("Century Gothic", Font.BOLD, 17));
		txtSubtotal.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtSubtotal.setBackground(_Settings.backgroundColor);
		txtSubtotal.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				if (_Settings.enableAutoVatCalculation)
					txtVat.setText(ValueFormatter.formatMoney(calculateVat()));
				txtTotal.setText(ValueFormatter.formatMoney(calculateTotal()));
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				if (_Settings.enableAutoVatCalculation)
					txtVat.setText(ValueFormatter.formatMoney(calculateVat()));
				txtTotal.setText(ValueFormatter.formatMoney(calculateTotal()));
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				if (_Settings.enableAutoVatCalculation)
					txtVat.setText(ValueFormatter.formatMoney(calculateVat()));
				txtTotal.setText(ValueFormatter.formatMoney(calculateTotal()));
			}
		});
		contentPane.add(txtSubtotal, "cell 23 19 6 1,grow");

		lblVat = new JLabel("VAT");
		lblVat.setHorizontalAlignment(SwingConstants.RIGHT);
		lblVat.setForeground(_Settings.labelColor);
		lblVat.setBackground(null);
		lblVat.setFont(new Font("Arial Black", Font.PLAIN, 14));
		lblVat.setBorder(null);
		contentPane.add(lblVat, "cell 16 20 6 2,grow");

		btnDeliver = new JButton("DELIVER");
		btnDeliver.setEnabled(false);
		btnDeliver.setForeground(_Settings.labelColor);
		btnDeliver.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnDeliver.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnDeliver.setBackground(_Settings.backgroundColor);
		btnDeliver.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				if (deliverItems()) {
					lblItemStatus.setText(getTransaction().getItemStatus().toString());
					txtStatus.setText("Items delivered!");
					LOGGER.Activity.log("Purchase", LOGGER.DELIVER,
							ValueFormatter.formatBalanceId(getTransaction().getId(), PurchaseTransaction.class));
					btnDeliver.setEnabled(false);
					txtInvoice.setEnabled(false);
					txtLpo.setEnabled(false);
					txtPi.setEnabled(false);
					((Main) owner).cardItem.calculateStock();
					((Main) owner).cardItem.updateTable();
					((Main) owner).cardStore.updateTable();
					((Main) owner).cardPurchase.updateTable();
				} else
					txtStatus.setText("Items could not be delivered.");
			}
		});
		contentPane.add(btnDeliver, "cell 1 21 5 3,grow");

		btnPay = new JButton("PAY");
		btnPay.setEnabled(false);
		btnPay.setForeground(_Settings.labelColor);
		btnPay.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnPay.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnPay.setBackground(_Settings.backgroundColor);
		btnPay.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				frames.Transaction frame = new frames.Transaction(getThis(), getTransaction(),
						TransactionType.PURCHASE);
				frame.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent e) {
						// TODO Auto-generated method stub
						PurchaseTransaction transaction = (PurchaseTransaction) frame.getTransaction();
						if (!transaction.getPayments().isEmpty()) {
							btnSave.setEnabled(false);
							updatePaymentStatus();
							PurchaseTransactions db = (PurchaseTransactions) DatabaseFacade
									.getDatabase("PurchaseTransactions");
							db.setDirty(true);
							((Main) owner).cardPurchase.updateTable();
						}
					}
				});
			}
		});
		contentPane.add(btnPay, "cell 7 21 4 3,grow");

		btnOther = new JButton("OTHER");
		btnOther.setForeground(_Settings.labelColor);
		btnOther.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnOther.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnOther.setBackground(_Settings.backgroundColor);
		btnOther.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				frames.Transaction frame = new frames.Transaction(getThis(), getOtherTransactions(),
						TransactionType.PURCHASE_COST);
				frame.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent e) {
						// TODO Auto-generated method stub
						otherTransactions = (List<Transaction>) frame.getTransaction();
						if (!otherTransactions.isEmpty()) {
							double sum = 0;
							for (Transaction t : otherTransactions)
								sum += t.getAmount() + t.getTax();
							txtOther.setText(ValueFormatter.formatMoney(sum));
							double oldSum = ValueFormatter.parseMoney(txtOther.getText());
							if (sum != oldSum)
								txtStatus.setText("Transaction added!");
							txtTotal.setText(ValueFormatter.formatMoney(calculateTotal()));
							calculateNewPrice();
						}
					}
				});
			}
		});
		contentPane.add(btnOther, "cell 12 21 3 3,grow");

		lblDiscount = new JLabel("Discount");
		lblDiscount.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDiscount.setForeground(_Settings.labelColor);
		lblDiscount.setFont(new Font("Arial Black", Font.PLAIN, 14));
		lblDiscount.setBorder(null);
		lblDiscount.setBackground(_Settings.backgroundColor);
		contentPane.add(lblDiscount, "cell 16 22 6 1,grow");

		txtDiscount = new JTextField();
		txtDiscount.setText("0.00");
		txtDiscount.setHorizontalAlignment(SwingConstants.CENTER);
		txtDiscount.setForeground(_Settings.textFieldColor);
		txtDiscount.setFont(new Font("Century Gothic", Font.BOLD, 17));
		txtDiscount.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtDiscount.setBackground(_Settings.backgroundColor);
		txtDiscount.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				if (!isValidNumeric(txtDiscount)) {
					txtDiscount.setText("0.00");
					new MyOptionPane("Discount must be positive number!", MyOptionPane.INFORMATION_MESSAGE);
					return;
				} else {
					double discount = ValueFormatter.parseMoney(txtDiscount.getText());
					txtDiscount.setText(ValueFormatter.formatMoney(discount));
					txtTotal.setText(ValueFormatter.formatMoney(calculateTotal()));
				}
				validateButton();
			}

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				txtDiscount.selectAll();
				btnSave.setEnabled(false);
			}
		});
		contentPane.add(txtDiscount, "cell 23 22 6 1,grow");

		lblOther = new JLabel("Other");
		lblOther.setHorizontalAlignment(SwingConstants.RIGHT);
		lblOther.setForeground(_Settings.labelColor);
		lblOther.setFont(new Font("Arial Black", Font.PLAIN, 14));
		lblOther.setBorder(null);
		lblOther.setBackground(_Settings.backgroundColor);
		contentPane.add(lblOther, "cell 16 23 6 1,grow");

		txtOther = new JTextField();
		txtOther.setEditable(false);
		txtOther.setText("0.00");
		txtOther.setHorizontalAlignment(SwingConstants.CENTER);
		txtOther.setForeground(_Settings.textFieldColor);
		txtOther.setFont(new Font("Century Gothic", Font.BOLD, 17));
		txtOther.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtOther.setBackground(_Settings.backgroundColor);
		contentPane.add(txtOther, "cell 23 23 6 1,grow");

		lblTotal = new JLabel("Total");
		lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTotal.setForeground(_Settings.labelColor);
		lblTotal.setBackground(null);
		lblTotal.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblTotal.setBorder(null);
		contentPane.add(lblTotal, "cell 16 24 6 3,grow");

		txtTotal = new JTextField();
		txtTotal.setText("0.00");
		txtTotal.setHorizontalAlignment(SwingConstants.CENTER);
		txtTotal.setForeground(_Settings.textFieldColor);
		txtTotal.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtTotal.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtTotal.setBackground(_Settings.backgroundColor);
		txtTotal.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				validateButton();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				validateButton();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				validateButton();
			}
		});
		txtTotal.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {
				// TODO Auto-generated method stub
				if (!isValidNumeric(txtTotal)) {
					txtTotal.setText(ValueFormatter.formatMoney(calculateTotal()));
				} else {
					double total = ValueFormatter.parseMoney(txtTotal.getText());
					txtTotal.setText(ValueFormatter.formatMoney(total));
				}
				validateButton();
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				// TODO Auto-generated method stub
				txtTotal.selectAll();
				btnSave.setEnabled(false);
			}
		});
		contentPane.add(txtTotal, "cell 23 24 6 3,grow");

		lblPaid = new JLabel("Paid:");
		lblPaid.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPaid.setForeground(_Settings.labelColor);
		lblPaid.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblPaid.setBorder(null);
		lblPaid.setBackground(_Settings.backgroundColor);
		contentPane.add(lblPaid, "cell 1 26 3 1,grow");

		lblPaidAmount = new JLabel("0.00");
		lblPaidAmount.setHorizontalAlignment(SwingConstants.LEFT);
		lblPaidAmount.setForeground(_Settings.labelColor);
		lblPaidAmount.setFont(new Font("Century Gothic", Font.BOLD, 17));
		lblPaidAmount.setBorder(null);
		lblPaidAmount.setBackground(_Settings.backgroundColor);
		contentPane.add(lblPaidAmount, "cell 5 26 6 1,grow");

		lblPending = new JLabel("Pending:");
		lblPending.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPending.setForeground(_Settings.labelColor);
		lblPending.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblPending.setBorder(null);
		lblPending.setBackground(_Settings.backgroundColor);
		contentPane.add(lblPending, "cell 1 27 3 1,grow");

		lblPendingAmount = new JLabel("0.00");
		lblPendingAmount.setHorizontalAlignment(SwingConstants.LEFT);
		lblPendingAmount.setForeground(_Settings.labelColor);
		lblPendingAmount.setFont(new Font("Century Gothic", Font.BOLD, 17));
		lblPendingAmount.setBorder(null);
		lblPendingAmount.setBackground(_Settings.backgroundColor);
		contentPane.add(lblPendingAmount, "cell 5 27 6 1,grow");

		txtStatus = new JTextField();
		txtStatus.setEditable(false);
		txtStatus.setBackground(null);
		txtStatus.setForeground(_Settings.labelColor);
		txtStatus.setBorder(null);
		txtStatus.setHorizontalAlignment(SwingConstants.RIGHT);
		txtStatus.setFont(new Font("Arial Black", Font.PLAIN, 15));
		txtStatus.setColumns(10);
		contentPane.add(txtStatus, "cell 16 29 12 1,grow");

		btnNew = new JButton("NEW");
		btnNew.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnNew.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnNew.setBackground(_Settings.backgroundColor);
		btnNew.setForeground(_Settings.labelColor);
		btnNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setFormEnabled(true);
				reset();
			}
		});
		contentPane.add(btnNew, "cell 1 31 5 1,grow");

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
		contentPane.add(btnClose, "cell 16 31 6 1,grow");

		btnSave = new JButton("SAVE");
		btnSave.setEnabled(false);
		btnSave.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnSave.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnSave.setBackground(_Settings.backgroundColor);
		btnSave.setForeground(_Settings.labelColor);
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int id = Integer.parseInt(txtId.getText());
				UserObject supplier = ValueFormatter.parseUserObject((String) cbSupplier.getSelectedItem());
				Store store = ValueFormatter.parseStore((String) cbStore.getSelectedItem());
				List<Stock> items = new ArrayList<Stock>();
				for (int i = 0; i < 10; i++) {
					if (cbItems.get(i).getSelectedIndex() != -1) {
						Item item = ValueFormatter.parseItemFromComboBox((String) cbItems.get(i).getSelectedItem());
						double quantity = ValueFormatter.parseQuantity(txtQuantitys.get(i).getText());
						double price = ValueFormatter.parseMoney(txtNews.get(i).getText());
						items.add(new Stock(item, quantity, price));
					}
				}
				double discount = ValueFormatter.parseMoney(txtDiscount.getText());
				double vat = ValueFormatter.parseMoney(txtVat.getText());
				double total = ValueFormatter.parseMoney(txtTotal.getText());
				List<Transaction> payments = new ArrayList<Transaction>();
				TransactionStatus itemStatus = TransactionStatus.UNDELIVERED;
				TransactionStatus paymentStatus = TransactionStatus.UNPAID;
				String lpoNo = ValueFormatter.formatTextSafely(txtLpo.getText());
				String piNo = ValueFormatter.formatTextSafely(txtPi.getText());
				String invoiceNo = ValueFormatter.formatTextSafely(txtInvoice.getText());
				if (addTransaction(id, supplier, store, items, discount, vat, total, payments, itemStatus,
						paymentStatus, lpoNo, piNo, invoiceNo)) {
					txtStatus.setText("Record saved!");
					setFormEnabled(false);
					btnOther.setEnabled(false);
					btnDeliver.setEnabled(true);
					btnPay.setEnabled(true);
					txtInvoice.setEnabled(true);
					txtLpo.setEnabled(true);
					txtPi.setEnabled(true);
					PurchaseTransactions db = (PurchaseTransactions) DatabaseFacade.getDatabase("PurchaseTransactions");
					setTransaction(db.get(db.find(db.maxID())));
					LOGGER.Activity.log("Purchase", LOGGER.ADD,
							ValueFormatter.formatBalanceId(id, PurchaseTransaction.class));
					lblItemStatus.setText(itemStatus.toString());
					updatePaymentStatus();
					((Main) owner).cardPurchase.updateTable();
				} else
					txtStatus.setText("An error occurred.");
			}
		});
		contentPane.add(btnSave, "cell 23 31 6 1,grow");

		getRootPane().setDefaultButton(btnSave);

		if (this.transaction != null)
			populateFrame();

		lblQuantity = new JLabel("Quantity");
		lblQuantity.setHorizontalAlignment(SwingConstants.CENTER);
		lblQuantity.setForeground(_Settings.labelColor);
		lblQuantity.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblQuantity.setBorder(null);
		lblQuantity.setBackground(_Settings.backgroundColor);
		contentPane.add(lblQuantity, "cell 14 7 2 1,grow");

		pack();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}

}
