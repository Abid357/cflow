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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import cores.DeliveryTransaction;
import cores.Item;
import cores.Stock;
import cores.Store;
import cores.Transaction;
import databases.DeliveryTransactions;
import globals.DatabaseFacade;
import globals.ItemFacade;
import globals.LOGGER;
import globals.RecordFacade;
import globals.ValueFormatter;
import globals._Settings;
import helpers.MyComboBoxRenderer;
import helpers.SortedComboBoxModel;
import helpers.TransactionType;
import net.miginfocom.swing.MigLayout;

public class Delivery extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtTotal, txtStatus;
	private JLabel lblFrom, lblTo, lblDate, lblTotal, lblNo, lblItem, lblQuantity, lblPriceOld, lblUnit, lblPriceNew,
			lblOld, lblNew;
	private boolean isDateEmpty = true, fromStoreSelected, toStoreSelected, oneItemSelected, containsZero = true;
	private DeliveryTransaction transaction;
	private JComboBox<String> cbFrom, cbTo;
	private JDateChooser dateChooser;
	private Date date;
	private JButton btnNew, btnSave, btnClose, btnCost;
	private List<JLabel> lblUnits, lblNos;
	private List<JTextField> txtOlds, txtNews, txtQuantitys;
	private List<Transaction> otherTransactions;
	private List<JComboBox<String>> cbItems;

	private void calculateNewPrice() {
		double cost = ValueFormatter.parseMoney(txtTotal.getText());
		List<Double> quantities = new ArrayList<Double>();
		List<Double> oldPrices = new ArrayList<Double>();
		List<Double> conversions = new ArrayList<Double>();
		for (int i = 0; i < cbItems.size(); i++)
			if (cbItems.get(i).getSelectedIndex() != -1) {
				Item item = ValueFormatter.parseItemFromComboBox((String) cbItems.get(i).getSelectedItem());
				quantities.add(ValueFormatter.parseQuantity(txtQuantitys.get(i).getText()));
				conversions.add(item.getConversion());
				oldPrices.add(ValueFormatter.parseMoney(txtOlds.get(i).getText()));
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

	public void setTransaction(DeliveryTransaction transaction) {
		this.transaction = transaction;
	}

	public DeliveryTransaction getTransaction() {
		return transaction;
	}

	public List<Transaction> getOtherTransactions() {
		return otherTransactions;
	}

	public JDialog getThis() {
		return this;
	}

	private String checkStockAvailabitiy() {
		String string = (String) cbFrom.getSelectedItem();
		if (string != null) {
			Map<String, Double> map = new HashMap<String, Double>();
			Store store = ValueFormatter.parseStore((String) cbFrom.getSelectedItem());
			for (int i = 0; i < cbItems.size(); i++)
				if (cbItems.get(i).getSelectedIndex() != -1) {
					Item item = ValueFormatter.parseItemFromComboBox((String) cbItems.get(i).getSelectedItem());
					String stringId = ValueFormatter.formatItem(item);
					if (map.containsKey(stringId))
						map.put(stringId, map.get(stringId) + ValueFormatter.parseMoney(txtQuantitys.get(i).getText()));
					else
						map.put(stringId, ValueFormatter.parseMoney(txtQuantitys.get(i).getText()));
				}
			Iterator<Entry<String, Double>> iterator = map.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, Double> entry = iterator.next();
				Stock stock = ItemFacade.findStockById(store.getItems(),
						ValueFormatter.parseItem(entry.getKey()).getId());
				if (entry.getValue() > stock.getQuantity())
					return entry.getKey();
			}
		}
		return null;
	}

	private boolean addTransaction(Store fromStore, Store toStore, double total, List<Stock> items) {
		DeliveryTransactions db = (DeliveryTransactions) DatabaseFacade.getDatabase("DeliveryTransactions");
		String balanceId = ValueFormatter.formatBalanceId(db.maxID() + 1, DeliveryTransaction.class);
		double fromAmount = 0, toAmount = 0;
		for (Stock stock : items) {
			Stock fromStock = ItemFacade.findStockById(fromStore.getItems(), stock.getItem().getId());
			ItemFacade.unstock(fromStore, stock.getItem(), stock.getQuantity());
			fromAmount += fromStock.getPrice() * stock.getQuantity();
			toAmount += stock.getPrice() * stock.getQuantity();
			ItemFacade.stock(toStore, stock.getItem(), stock.getQuantity(), stock.getPrice());
		}

		RecordFacade.record(fromStore.getInventory(), fromAmount, 0.0, false, null);
		RecordFacade.addInventoryRecord(fromStore.getInventory(), fromAmount, 0.0, false, balanceId, date);
		RecordFacade.record(toStore.getInventory(), toAmount, 0.0, true, null);
		RecordFacade.addInventoryRecord(toStore.getInventory(), toAmount, 0.0, true, balanceId, date);
		return db.add(db.maxID() + 1, date, fromStore, toStore, total, otherTransactions, items);
	}

	private void validateButton() {
		containsZero = checkForZeroAmounts();
		oneItemSelected = cbItems.get(0).getSelectedIndex() != -1;
		if (!fromStoreSelected)
			txtStatus.setText("Select a source store!");
		else if (!toStoreSelected)
			txtStatus.setText("Select a destination store!");
		else if (isDateEmpty)
			txtStatus.setText("Select a date!");
		else if (!oneItemSelected)
			txtStatus.setText("Select at least one item!");
		else if (containsZero)
			txtStatus.setText("Quantity or price for an item cannot be zero!");
		else
			txtStatus.setText("");
		btnSave.setEnabled(oneItemSelected && fromStoreSelected && toStoreSelected && !isDateEmpty && !containsZero);
	}

	private void populateFrame() {
		setFormEnabled(false);
		txtTotal.setText(ValueFormatter.formatMoney(transaction.getTotal()));
		txtStatus.setText("");
		btnSave.setEnabled(false);
	}

	private void setFormEnabled(boolean isEnabled) {
		for (JTextField txt : txtQuantitys)
			txt.setEnabled(isEnabled);
		for (JTextField txt : txtOlds)
			txt.setEnabled(isEnabled);
		for (JTextField txt : txtNews)
			txt.setEnabled(isEnabled);
		for (JComboBox<String> cb : cbItems)
			cb.setEnabled(isEnabled);
		cbFrom.setEnabled(isEnabled);
		cbTo.setEnabled(isEnabled);
		dateChooser.setEnabled(isEnabled);
		txtTotal.setEnabled(isEnabled);
		btnSave.setEnabled(isEnabled);
	}

	private boolean checkForZeroAmounts() {
		for (JTextField txtQuantity : txtQuantitys)
			if (!txtQuantity.getText().isEmpty())
				if (ValueFormatter.parseQuantity(txtQuantity.getText()) == 0)
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

	private void resetQuantitys() {
		for (JTextField txt : txtQuantitys) {
			txt.setText("");
			txt.setEnabled(false);
		}
	}

	private void resetNews() {
		for (JTextField txt : txtNews)
			txt.setText("");
	}

	private void resetOlds() {
		for (JTextField txt : txtOlds)
			txt.setText("");
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
		resetQuantitys();
		resetOlds();
		resetNews();
		resetUnits();
		cbTo.setSelectedIndex(-1);
		cbFrom.setSelectedIndex(-1);
		txtStatus.setText("");
		containsZero = oneItemSelected = fromStoreSelected = toStoreSelected = false;
		btnSave.setEnabled(false);
		btnCost.setEnabled(true);
	}

	/**
	 * Create the frame.
	 */
	@SuppressWarnings("unchecked")
	public Delivery(Window owner, cores.DeliveryTransaction transaction) {
		super(owner, "Delivery Transaction", Dialog.ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		setBounds(100, 100, 850, 700);
		contentPane = new JPanel();
		contentPane.setBackground(_Settings.backgroundColor);
		contentPane.setBorder(null);
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("",
				"[10][60][10][80][130][10][20][40][10][50][50][10][10][90][10][50][10][40][10][100,grow][10]",
				"[10][40][10][40][10][20][20][20][20][20][20][20][20][20][20][20][10][10][50][10][20][10][60][10]"));

		this.transaction = transaction;
		otherTransactions = new ArrayList<Transaction>();

		UIManager.put("ComboBox.disabledForeground", _Settings.labelColor);
		UIManager.put("TextField.inactiveForeground", _Settings.labelColor);
		UIManager.put("TextArea.inactiveForeground", _Settings.labelColor);

		lblFrom = new JLabel("From");
		lblFrom.setHorizontalAlignment(SwingConstants.RIGHT);
		lblFrom.setForeground(_Settings.labelColor);
		lblFrom.setBackground(null);
		lblFrom.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblFrom.setBorder(null);
		contentPane.add(lblFrom, "cell 1 1,grow");

		List<Store> stores = (List<Store>) DatabaseFacade.getDatabase("Stores").getList();
		SortedComboBoxModel<String> scbmStore = new SortedComboBoxModel<String>();
		SortedComboBoxModel<String> scbmStore2 = new SortedComboBoxModel<String>();
		MyComboBoxRenderer mcbrStore = new MyComboBoxRenderer();
		List<String> ttStore = new ArrayList<String>();
		for (Store store : stores) {
			String storeString = ValueFormatter.formatStore(store);
			scbmStore.addElement(storeString);
			scbmStore2.addElement(storeString);
			ttStore.add(storeString);
		}
		mcbrStore.setTooltips(ttStore);

		cbFrom = new JComboBox<String>(scbmStore);
		cbFrom.setRenderer(mcbrStore);
		cbFrom.setMaximumSize(new Dimension(210, 32767));
		cbFrom.setFont(new Font("Century Gothic", Font.BOLD, 17));
		cbFrom.setForeground(Color.DARK_GRAY);
		cbFrom.setSelectedIndex(-1);
		cbFrom.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if ((e.getStateChange() == ItemEvent.SELECTED)) {
					fromStoreSelected = true;
					validateButton();
					resetItems();
					resetQuantitys();
					resetOlds();
					resetNews();
					resetUnits();
					
					Store store = ValueFormatter.parseStore((String) cbFrom.getSelectedItem());
					List<Stock> items = store.getItems();
					for (JComboBox<String> cb : cbItems) {
						ItemListener listener = cb.getItemListeners()[0];
						cb.removeItemListener(listener);

						SortedComboBoxModel<String> scbmItem = new SortedComboBoxModel<String>(true);
						MyComboBoxRenderer mcbrItem = new MyComboBoxRenderer();
						List<String> ttItem = new ArrayList<String>();
						for (Stock stock : items) {
							String itemString = ValueFormatter.formatItemForComboBox(stock.getItem());
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
						for (Stock item : items)
							cb.addItem(ValueFormatter.formatStockForComboBox(item));
						
						cb.setRenderer(mcbrItem);
						cb.setSelectedIndex(-1);
						cb.addItemListener(listener);
					}
					cbItems.get(0).setEnabled(true);
				}
			}
		});
		contentPane.add(cbFrom, "cell 3 1 2 1,grow");

		lblTo = new JLabel("To");
		lblTo.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTo.setBackground(null);
		lblTo.setForeground(_Settings.labelColor);
		lblTo.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblTo.setBorder(null);
		contentPane.add(lblTo, "flowx,cell 1 3,grow");

		cbTo = new JComboBox<String>(scbmStore2);
		cbTo.setRenderer(mcbrStore);
		cbTo.setMaximumSize(new Dimension(210, 32767));
		cbTo.setFont(new Font("Century Gothic", Font.BOLD, 17));
		cbTo.setForeground(Color.DARK_GRAY);
		cbTo.setSelectedIndex(-1);
		cbTo.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if ((e.getStateChange() == ItemEvent.SELECTED)) {
					toStoreSelected = true;
					validateButton();
				}
			}
		});
		contentPane.add(cbTo, "cell 3 3 2 1,grow");

		lblDate = new JLabel("Date");
		lblDate.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDate.setForeground(_Settings.labelColor);
		lblDate.setBackground(null);
		lblDate.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblDate.setBorder(null);
		contentPane.add(lblDate, "cell 6 3 2 1,grow");

		dateChooser = new JDateChooser();
		dateChooser.setMaximumSize(new Dimension(210, 2147483647));
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
		contentPane.add(dateChooser, "cell 9 3 5 1,grow");

		lblOld = new JLabel("Old");
		lblOld.setHorizontalAlignment(SwingConstants.CENTER);
		lblOld.setForeground(_Settings.labelColor);
		lblOld.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblOld.setBorder(null);
		lblOld.setBackground(_Settings.backgroundColor);
		contentPane.add(lblOld, "cell 15 3 3 2,growx,aligny bottom");

		lblNew = new JLabel("New");
		lblNew.setHorizontalAlignment(SwingConstants.CENTER);
		lblNew.setForeground(_Settings.labelColor);
		lblNew.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblNew.setBorder(null);
		lblNew.setBackground(_Settings.backgroundColor);
		contentPane.add(lblNew, "cell 19 3 1 2,growx,aligny bottom");

		lblNo = new JLabel("No.");
		lblNo.setHorizontalAlignment(SwingConstants.CENTER);
		lblNo.setForeground(_Settings.labelColor);
		lblNo.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblNo.setBorder(null);
		lblNo.setBackground(_Settings.backgroundColor);
		contentPane.add(lblNo, "cell 1 5,grow");

		lblItem = new JLabel("Item");
		lblItem.setHorizontalAlignment(SwingConstants.CENTER);
		lblItem.setForeground(_Settings.labelColor);
		lblItem.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblItem.setBorder(null);
		lblItem.setBackground(_Settings.backgroundColor);
		contentPane.add(lblItem, "flowx,cell 3 5 5 1,grow");

		lblQuantity = new JLabel("Quantity");
		lblQuantity.setHorizontalAlignment(SwingConstants.CENTER);
		lblQuantity.setForeground(_Settings.labelColor);
		lblQuantity.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblQuantity.setBorder(null);
		lblQuantity.setBackground(_Settings.backgroundColor);
		contentPane.add(lblQuantity, "cell 9 5 2 1,grow");

		lblUnit = new JLabel("Unit");
		lblUnit.setHorizontalAlignment(SwingConstants.CENTER);
		lblUnit.setForeground(_Settings.labelColor);
		lblUnit.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblUnit.setBorder(null);
		lblUnit.setBackground(_Settings.backgroundColor);
		contentPane.add(lblUnit, "cell 12 5 2 1,grow");

		lblPriceOld = new JLabel("Price / unit");
		lblPriceOld.setHorizontalAlignment(SwingConstants.CENTER);
		lblPriceOld.setForeground(_Settings.labelColor);
		lblPriceOld.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblPriceOld.setBorder(null);
		lblPriceOld.setBackground(_Settings.backgroundColor);
		contentPane.add(lblPriceOld, "cell 15 5 3 1,grow");

		lblPriceNew = new JLabel("Price / unit");
		lblPriceNew.setHorizontalAlignment(SwingConstants.CENTER);
		lblPriceNew.setForeground(_Settings.labelColor);
		lblPriceNew.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblPriceNew.setBorder(null);
		lblPriceNew.setBackground(_Settings.backgroundColor);
		contentPane.add(lblPriceNew, "cell 19 5,grow");

		lblUnits = new ArrayList<JLabel>();
		for (int i = 0; i < 10; i++) {
			JLabel lbl = new JLabel();
			lbl.setHorizontalAlignment(SwingConstants.CENTER);
			lbl.setForeground(_Settings.labelColor);
			lbl.setBackground(_Settings.backgroundColor);
			lbl.setBorder(null);
			lbl.setFont(new Font("Century Gothic", Font.BOLD, 17));
			contentPane.add(lbl, "cell 12 " + (6 + i) + " 2 1,grow");
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
			contentPane.add(lbl, "cell 1 " + (6 + i) + ",grow");
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
						Store store = ValueFormatter.parseStore((String) cbFrom.getSelectedItem());
						Item item = ValueFormatter.parseItemFromComboBox((String) cb.getSelectedItem());
						Stock stock = ItemFacade.findStockById(store.getItems(), item.getId());
						JTextField txtQuantity = txtQuantitys.get(index);
						txtQuantity.setEnabled(true);
						txtQuantity.setText("0");
						JTextField txtOld = txtOlds.get(index);
						txtOld.setText(ValueFormatter.formatMoney(stock.getItem().getPrice()));
						JLabel lblUnit = lblUnits.get(index);
						lblUnit.setText(item.getUnit());
						calculateNewPrice();
						if (index != 9)
							cbItems.get(index + 1).setEnabled(true);
						validateButton();
					}
				}
			});
			contentPane.add(cb, "cell 3 " + (6 + i) + " 5 1,grow");
			cbItems.add(cb);
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
						Store store = ValueFormatter.parseStore((String) cbFrom.getSelectedItem());
						Item item = ValueFormatter.parseItemFromComboBox((String) cbItems.get(index).getSelectedItem());
						double quantityAvailable = ItemFacade.findStockById(store.getItems(), item.getId())
								.getQuantity();
						calculateNewPrice();
						if (quantity > quantityAvailable) {
							txt.setText("0");
							new MyOptionPane(
									"Max quantity available: " + ValueFormatter.formatQuantity(quantityAvailable),
									MyOptionPane.ERROR_DIALOG_BOX);
							return;
						}
						validateButton();
					}
				}

				@Override
				public void focusGained(FocusEvent e) {
					// TODO Auto-generated method stub
					txt.selectAll();
				}
			});
			contentPane.add(txt, "cell 9 " + (6 + i) + " 2 1,grow");
			txtQuantitys.add(txt);
		}

		txtOlds = new ArrayList<JTextField>();
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
			contentPane.add(txt, "cell 15 " + (6 + i) + " 3 1,grow");
			txtOlds.add(txt);
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
			contentPane.add(txt, "cell 19 " + (6 + i) + ",grow");
			txtNews.add(txt);
		}

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

		btnCost = new JButton("COST");
		btnCost.setForeground(_Settings.labelColor);
		btnCost.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnCost.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnCost.setBackground(_Settings.backgroundColor);
		btnCost.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				frames.Transaction frame = new frames.Transaction(getThis(), getOtherTransactions(),
						TransactionType.DELIVERY_COST);
				frame.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent e) {
						// TODO Auto-generated method stub
						otherTransactions = (List<Transaction>) frame.getTransaction();
						if (!otherTransactions.isEmpty()) {
							double sum = 0;
							for (Transaction t : otherTransactions)
								sum += t.getAmount() + t.getTax();
							txtTotal.setText(ValueFormatter.formatMoney(sum));
							double oldSum = ValueFormatter.parseMoney(txtTotal.getText());
							if (sum != oldSum)
								txtStatus.setText("Transaction added!");
							txtTotal.setText(ValueFormatter.formatMoney(sum));
							calculateNewPrice();
						}
					}
				});
			}
		});
		contentPane.add(btnCost, "cell 7 17 4 2,grow");

		lblTotal = new JLabel("Cost");
		lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTotal.setForeground(_Settings.labelColor);
		lblTotal.setBackground(null);
		lblTotal.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblTotal.setBorder(null);
		contentPane.add(lblTotal, "cell 14 18 2 1,grow");

		txtTotal = new JTextField();
		txtTotal.setText("0.00");
		txtTotal.setEditable(false);
		txtTotal.setHorizontalAlignment(SwingConstants.CENTER);
		txtTotal.setForeground(_Settings.textFieldColor);
		txtTotal.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtTotal.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtTotal.setBackground(_Settings.backgroundColor);
		contentPane.add(txtTotal, "cell 17 18 3 1,grow");

		txtStatus = new JTextField();
		txtStatus.setHorizontalAlignment(SwingConstants.RIGHT);
		txtStatus.setForeground(_Settings.labelColor);
		txtStatus.setFont(new Font("Arial Black", Font.PLAIN, 15));
		txtStatus.setEditable(false);
		txtStatus.setColumns(10);
		txtStatus.setBorder(null);
		txtStatus.setBackground(_Settings.backgroundColor);
		contentPane.add(txtStatus, "cell 7 20 13 1,grow");
		contentPane.add(btnNew, "cell 1 22 3 1,grow");

		btnSave = new JButton("SAVE");
		btnSave.setEnabled(false);
		btnSave.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnSave.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnSave.setBackground(_Settings.backgroundColor);
		btnSave.setForeground(_Settings.labelColor);
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Store fromStore = ValueFormatter.parseStore((String) cbFrom.getSelectedItem());
				Store toStore = ValueFormatter.parseStore((String) cbTo.getSelectedItem());
				List<Stock> items = new ArrayList<Stock>();
				for (int i = 0; i < 10; i++) {
					if (cbItems.get(i).getSelectedIndex() != -1) {
						Item item = ValueFormatter.parseItemFromComboBox((String) cbItems.get(i).getSelectedItem());
						double quantity = ValueFormatter.parseQuantity(txtQuantitys.get(i).getText());
						double price = ValueFormatter.parseMoney(txtNews.get(i).getText());
						items.add(new Stock(item, quantity, price));
					}
				}
				double total = ValueFormatter.parseMoney(txtTotal.getText());
				String check = checkStockAvailabitiy();
				if (check != null)
					txtStatus.setText("Insufficient stock for " + check);
				else if (addTransaction(fromStore, toStore, total, items)) {
					txtStatus.setText("Record saved!");
					setFormEnabled(false);
					btnCost.setEnabled(false);
					DeliveryTransactions db = (DeliveryTransactions) DatabaseFacade.getDatabase("DeliveryTransactions");
					setTransaction(db.get(db.find(db.maxID())));
					LOGGER.Activity.log("Delivery", LOGGER.ADD,
							ValueFormatter.formatBalanceId(db.maxID(), DeliveryTransaction.class));
					((Main) owner).cardItem.calculateStock();
					((Main) owner).cardItem.updateTable();
					((Main) owner).cardStore.updateTable();
				} else
					txtStatus.setText("An error occurred.");
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
		contentPane.add(btnClose, "cell 13 22 3 1,grow");
		contentPane.add(btnSave, "cell 17 22 3 1,grow");

		getRootPane().setDefaultButton(btnSave);

		if (this.transaction != null)
			populateFrame();

		pack();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}

}
