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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

import cores.Item;
import cores.Store;
import databases.Stores;
import globals.DatabaseFacade;
import globals.ItemFacade;
import globals.LOGGER;
import globals.RecordFacade;
import globals.ValueFormatter;
import globals._Settings;
import helpers.ItemType;
import helpers.MyComboBoxRenderer;
import helpers.SortedComboBoxModel;
import net.miginfocom.swing.MigLayout;

public class Stock extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtQuantity;
	private JButton btnSave;
	private JButton btnClose;
	private JComboBox<String> cbItem;
	private JComboBox<String> cbStore;
	private JTextField txtStatus;
	private JLabel lblQuantity;
	private boolean isPriceEmpty = false, isQuantityEmpty = false, isItemSelected, isStoreSelected,
			isPriceValid = false, isQuantityValid = false;
	private List<cores.Stock> items;
	private JLabel lblUnit;
	private JLabel lblPrice;
	private JLabel lblPriceUnit;
	private JTextField txtPrice;

	public List<cores.Stock> getItems() {
		return items;
	}

	private boolean isQuantityValid() {
		return Pattern.compile("(\\-?[0-9]{1,3}(\\,)?)*(\\.[0-9]*)?").matcher(txtQuantity.getText()).matches();
	}

	private boolean isPriceValid() {
		return Pattern.compile("(\\-?[0-9]{1,3}(\\,)?)*(\\.[0-9]*)?").matcher(txtPrice.getText()).matches();
	}

	private static double calculateAveragePrice(double quantity1, double price1, double quantity2, double price2) {
		return ((quantity1 * price1) + (quantity2 * price2)) / (quantity1 + quantity2);
	}

	private void validateButton() {
		if (!isQuantityValid)
			txtStatus.setText("Quantity must be positive number only!");
		else if (!isItemSelected)
			txtStatus.setText("Select an item!");
		else if (isQuantityEmpty)
			txtStatus.setText("Quantity cannot be empty!");
		else if (!isPriceValid)
			txtStatus.setText("Price must be positive number only!");
		else if (isPriceEmpty)
			txtStatus.setText("Price cannot be empty!");
		else if (!isStoreSelected)
			txtStatus.setText("Select a store!");
		else
			txtStatus.setText("");
		btnSave.setEnabled(isItemSelected && isStoreSelected && !isQuantityEmpty && !isPriceEmpty && isQuantityValid
				&& isPriceValid);
	}

	/**
	 * Create the frame.
	 */
	@SuppressWarnings("unchecked")
	public Stock(Window owner, Store store, JDialog dialog) {
		super(owner, "Stock", Dialog.ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		setBounds(100, 100, 850, 700);
		contentPane = new JPanel();
		contentPane.setBackground(_Settings.backgroundColor);
		contentPane.setBorder(null);
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[10,grow][80][10][100,grow][150][10][150][10]",
				"[10][40][10][40][10][40][10][40][10][20][10][60,grow]"));

		if (dialog != null) {
			if (dialog instanceof frames.Store)
				this.items = ((frames.Store) dialog).getItems();
			else if (dialog instanceof frames.Item)
				this.items = ((frames.Item) dialog).getItems();
		}

		UIManager.put("ComboBox.disabledForeground", _Settings.labelColor);
		UIManager.put("TextField.inactiveForeground", _Settings.labelColor);
		UIManager.put("TextArea.inactiveForeground", _Settings.labelColor);

		JLabel lblItem = new JLabel("Item");
		lblItem.setBorder(null);
		lblItem.setHorizontalAlignment(SwingConstants.RIGHT);
		lblItem.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblItem.setForeground(_Settings.labelColor);
		contentPane.add(lblItem, "cell 0 1 2 1,grow");

		List<Item> itemList = (List<Item>) DatabaseFacade.getDatabase("Items").getList();
		SortedComboBoxModel<String> scbmItem = new SortedComboBoxModel<String>(true);
		MyComboBoxRenderer mcbrItem = new MyComboBoxRenderer();
		List<String> ttItem = new ArrayList<String>();
		for (Item item : itemList) {
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

		cbItem = new JComboBox<String>(scbmItem);
		cbItem.setRenderer(mcbrItem);
		cbItem.setMaximumSize(new Dimension(250, 32767));
		cbItem.setFont(new Font("Century Gothic", Font.BOLD, 17));
		cbItem.setForeground(Color.DARK_GRAY);
		cbItem.setSelectedIndex(-1);
		cbItem.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if (e.getStateChange() == ItemEvent.SELECTED) {
					isItemSelected = true;
					Item item = ValueFormatter.parseItemFromComboBox((String) cbItem.getSelectedItem());
					lblUnit.setText(item.getUnit());
					lblPriceUnit.setText("/" + item.getUnit());
					txtPrice.setText(ValueFormatter.formatMoney(item.getPrice()));
					isPriceValid = true;
					isPriceEmpty = false;
					if (dialog instanceof frames.Store && item.getType().equals(ItemType.SERVICE)) {
						txtQuantity.setEnabled(false);
						txtQuantity.setText("-");
						isQuantityValid = true;
					} else {
						txtQuantity.setEnabled(true);
						txtQuantity.setText("0");
						isQuantityValid = false;
					}
					validateButton();
				}
			}
		});
		contentPane.add(cbItem, "cell 3 1 2 1,grow");

		lblQuantity = new JLabel("Quantity");
		lblQuantity.setBorder(null);
		lblQuantity.setHorizontalAlignment(SwingConstants.RIGHT);
		lblQuantity.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblQuantity.setForeground(_Settings.labelColor);
		contentPane.add(lblQuantity, "cell 1 3,grow");

		txtQuantity = new JTextField();
		txtQuantity.setText("0");
		txtQuantity.setHorizontalAlignment(SwingConstants.CENTER);
		txtQuantity.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtQuantity.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtQuantity.setBackground(null);
		txtQuantity.setForeground(_Settings.textFieldColor);
		txtQuantity.setColumns(10);
		txtQuantity.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				isQuantityValid = isQuantityValid();
				if (isQuantityValid) {
					isQuantityEmpty = txtQuantity.getText().isEmpty();
					if (!isQuantityEmpty) {
						double quantity = ValueFormatter.parseQuantity(txtQuantity.getText());
						isQuantityValid = quantity > 0;
						txtQuantity.setText(ValueFormatter.formatQuantity(quantity));
					}
				}
				validateButton();
			}

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				txtQuantity.selectAll();
			}
		});
		contentPane.add(txtQuantity, "cell 3 3 2 1,grow");

		lblUnit = new JLabel("");
		lblUnit.setHorizontalAlignment(SwingConstants.LEFT);
		lblUnit.setForeground(_Settings.labelColor);
		lblUnit.setBackground(_Settings.backgroundColor);
		lblUnit.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblUnit.setBorder(null);
		lblUnit.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				validateButton();
			}
		});
		contentPane.add(lblUnit, "cell 6 3,grow");

		lblPrice = new JLabel("Price");
		lblPrice.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPrice.setForeground(_Settings.labelColor);
		lblPrice.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblPrice.setBorder(null);
		contentPane.add(lblPrice, "cell 1 5,grow");

		txtPrice = new JTextField();
		txtPrice.setText("0.00");
		txtPrice.setHorizontalAlignment(SwingConstants.CENTER);
		txtPrice.setForeground(_Settings.textFieldColor);
		txtPrice.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtPrice.setColumns(10);
		txtPrice.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtPrice.setBackground(_Settings.backgroundColor);
		txtPrice.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				isPriceValid = isPriceValid();
				if (isPriceValid) {
					isPriceEmpty = txtPrice.getText().isEmpty();
					if (!isPriceEmpty) {
						double price = ValueFormatter.parseMoney(txtPrice.getText());
						isPriceValid = price > 0;
						txtPrice.setText(ValueFormatter.formatMoney(price));
					}
				}
				validateButton();
			}

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				txtPrice.selectAll();
			}
		});
		contentPane.add(txtPrice, "cell 3 5 2 1,grow");

		lblPriceUnit = new JLabel("");
		lblPriceUnit.setHorizontalAlignment(SwingConstants.LEFT);
		lblPriceUnit.setForeground(_Settings.labelColor);
		lblPriceUnit.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblPriceUnit.setBorder(null);
		lblPriceUnit.setBackground(_Settings.backgroundColor);
		lblPriceUnit.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				validateButton();
			}
		});
		contentPane.add(lblPriceUnit, "cell 6 5,grow");

		JLabel lblStore = new JLabel("Store");
		lblStore.setHorizontalAlignment(SwingConstants.RIGHT);
		lblStore.setForeground(_Settings.labelColor);
		lblStore.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblStore.setBorder(null);
		contentPane.add(lblStore, "cell 1 7,grow");

		List<Store> stores = (List<Store>) DatabaseFacade.getDatabase("Stores").getList();
		SortedComboBoxModel<String> scbmStore = new SortedComboBoxModel<String>();
		MyComboBoxRenderer mcbrStore = new MyComboBoxRenderer();
		List<String> ttStore = new ArrayList<String>();
		for (Store eachStore : stores) {
			String storeString = ValueFormatter.formatStore(eachStore);
			scbmStore.addElement(storeString);
			ttStore.add(storeString);
		}
		mcbrStore.setTooltips(ttStore);

		cbStore = new JComboBox<String>(scbmStore);
		cbStore.setRenderer(mcbrStore);
		cbStore.setMaximumSize(new Dimension(250, 32767));
		cbStore.setFont(new Font("Century Gothic", Font.BOLD, 17));
		cbStore.setForeground(Color.DARK_GRAY);
		cbStore.setSelectedIndex(-1);
		cbStore.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if (e.getStateChange() == ItemEvent.SELECTED) {
					isStoreSelected = true;
					validateButton();
				}
			}
		});
		contentPane.add(cbStore, "cell 3 7 2 1,grow");

		btnSave = new JButton("SAVE");
		btnSave.setEnabled(false);
		btnSave.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnSave.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnSave.setBackground(_Settings.backgroundColor);
		btnSave.setForeground(_Settings.labelColor);
		btnSave.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				Item item = ValueFormatter.parseItemFromComboBox((String) cbItem.getSelectedItem());
				double price = ValueFormatter.parseMoney(txtPrice.getText());
				cores.Stock stock = ItemFacade.findStockById(items, item.getId());
				double quantity = -1;
				if (dialog instanceof frames.Store && item.getType().equals(ItemType.SERVICE)) {
					if (stock != null)
						stock.setPrice(price);
					else
						items.add(new cores.Stock(item, -1, price));
				} else {
					quantity = ValueFormatter.parseQuantity(txtQuantity.getText());
					if (stock != null) {
						if (stock.getPrice() != price) {
							stock.setPrice(
									calculateAveragePrice(stock.getQuantity(), stock.getPrice(), quantity, price));
						}
						stock.setQuantity(stock.getQuantity() + quantity);
					} else
						items.add(new cores.Stock(item, quantity, price));
				}

				txtStatus.setText("Record saved!");
				if (dialog != null) {
					if (dialog instanceof frames.Store) {
						frames.Store frameStore = (frames.Store) dialog;
						frameStore.setItems(items);
						frameStore.setStatus("Item added!");

						Store store = frameStore.getStore();
						if (store != null && !item.getType().equals(ItemType.SERVICE)) {
							RecordFacade.adjustRecord(quantity * price, true, null,
									frameStore.getStore().getInventory(), "New stock added");
							LOGGER.Activity.log("Stock", LOGGER.ADD, ValueFormatter.formatItem(item),
									ValueFormatter.formatStore(store));
						} else {
							LOGGER.Activity.log("Stock", LOGGER.ADD, ValueFormatter.formatItem(item),
									"unfinished store");
						}
					} else if (dialog instanceof frames.Item) {
						frames.Item frameItem = (frames.Item) dialog;
						frameItem.setItems(items);
						frameItem.setStatus("Component added!");
						LOGGER.Activity.log("Component", LOGGER.ADD, ValueFormatter.formatItem(item),
								"unfinished item");
					}
					Stores db = (Stores) DatabaseFacade.getDatabase("Stores");
					db.setDirty(true);
					dispose();
				}
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

		txtStatus = new JTextField();
		txtStatus.setEditable(false);
		txtStatus.setBackground(null);
		txtStatus.setForeground(_Settings.labelColor);
		txtStatus.setBorder(null);
		txtStatus.setHorizontalAlignment(SwingConstants.RIGHT);
		txtStatus.setFont(new Font("Arial Black", Font.PLAIN, 15));
		txtStatus.setColumns(10);
		contentPane.add(txtStatus, "cell 3 9 4 1,grow");
		contentPane.add(btnClose, "cell 4 11,aligny center,grow");
		contentPane.add(btnSave, "cell 6 11,aligny center,grow");

		if (store != null) {
			cbStore.setSelectedItem(ValueFormatter.formatStore(store));
			cbStore.setEnabled(false);
			isStoreSelected = true;
		}

		if (items != null) {
			cbStore.setEnabled(false);
			isStoreSelected = true;
		}

		pack();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}

}
