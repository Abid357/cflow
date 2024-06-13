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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import cores.Item;
import cores.Stock;
import cores.StoreInventory;
import cores.TransactionCategory;
import cores.UserObject;
import databases.Inventories;
import databases.Stores;
import databases.UserCategories;
import globals.DatabaseFacade;
import globals.ItemFacade;
import globals.LOGGER;
import globals.RecordFacade;
import globals.ValueFormatter;
import globals._Settings;
import helpers.MyComboBoxRenderer;
import helpers.SortedComboBoxModel;
import net.miginfocom.swing.MigLayout;

public class Store extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JLabel lblName, lblCategory;
	private boolean isOwnerSelected, isNameEmpty = true;
	private cores.Store store;
	private JComboBox<String> cbCategory, cbOwner;
	private JButton btnNew, btnSave;
	private JTextField txtName, txtStatus;
	private JLabel lblOwner;
	private JLabel lblNetAsset;
	private JTextField txtNetAsset;
	private JScrollPane spItem;
	private JTable tblItem;
	private JButton btnAdd;
	private JButton btnDelete;
	private DefaultTableModel dtm;
	private List<Stock> items;

	private String[] columnHeaders = { "ID", "NAME", "TYPE", "QUANTITY", "UNIT", "PRICE/UNIT", "VALUE" };
	private JButton btnClose;

	public void setStatus(String status) {
		txtStatus.setText(status);
	}

	public cores.Store getStore() {
		return store;
	}

	public void setStore(cores.Store store) {
		this.store = store;
	}

	public void setItems(List<Stock> items) {
		this.items = items;
	}

	public List<Stock> getItems() {
		return items;
	}

	public JDialog getThis() {
		return this;
	}

	private void validateButton() {
		if (isNameEmpty)
			txtStatus.setText("Enter a store name!");
		else if (!isOwnerSelected)
			txtStatus.setText("Select an owner!");
		else
			txtStatus.setText("");
		btnSave.setEnabled(isOwnerSelected && !isNameEmpty);
	}

	private void updateTable() {
		dtm.setRowCount(0);
		for (Stock stock : items) {
			Item item = stock.getItem();
			String quantity = "-";
			String value = "-";
			if (stock.getQuantity() != -1.0) {
				quantity = ValueFormatter.formatQuantity(stock.getQuantity());
				value = ValueFormatter.formatMoneyNicely(stock.getQuantity() * stock.getPrice());
			}
			dtm.addRow(new Object[] { ValueFormatter.formatItem(item), item.getName(), item.getType().toString(),
					quantity, item.getUnit(), ValueFormatter.formatMoneyNicely(stock.getPrice()), value });
		}
	}

	private void populateFrame() {
		if (store != null) {
			setFormEnabled(false);
			txtName.setText(store.getName());
			cbOwner.addItem(ValueFormatter.formatUserObject(store.getOwner()));
			cbOwner.setSelectedIndex(0);
			items = store.getItems();
			isNameEmpty = false;
			isOwnerSelected = true;
			txtStatus.setText("");
			updateTable();
		}
	}

	private void setFormEnabled(boolean isEnabled) {
		btnSave.setEnabled(isEnabled);
		cbCategory.setEnabled(isEnabled);
		cbOwner.setEnabled(isEnabled);
		txtName.setEnabled(isEnabled);
	}

	private void reset() {
		store = null;
		cbOwner.setSelectedIndex(-1);
		cbCategory.setSelectedIndex(-1);
		txtStatus.setText("");
		txtName.setText("");
		txtNetAsset.setText("0.00");
		isNameEmpty = true;
		isOwnerSelected = false;
		items = new ArrayList<Stock>();
		updateTable();
		btnSave.setEnabled(false);
	}

	private double calculateNetAsset() {
		double sum = 0;
		for (int i = 0; i < tblItem.getRowCount(); i++) {
			String sumString = (String) dtm.getValueAt(i, 6);
			if (!sumString.equals("-"))
				sum += ValueFormatter.parseMoney(sumString);
		}
		return sum;
	}

	private boolean addStore(String name, UserObject owner) {
		Stores dbStores = (Stores) DatabaseFacade.getDatabase("Stores");
		Inventories dbInventories = (Inventories) DatabaseFacade.getDatabase("Inventories");
		double balance = ValueFormatter.parseMoney(txtNetAsset.getText());
		if (dbStores.find(name, owner) == -1 && dbInventories.find(name) == -1) {
			String inventoryString = name + "/" + ValueFormatter.formatUserObject(owner);
			dbInventories.add(inventoryString, balance);
			StoreInventory inventory = (StoreInventory) dbInventories.get(dbInventories.find(inventoryString));
			return dbStores.add(name, owner, inventory, items);
		} else
			return false;
	}

	/**
	 * Create the frame.
	 */
	@SuppressWarnings("unchecked")
	public Store(Window windowOwner, cores.Store store) {
		super(windowOwner, "Store", Dialog.ModalityType.DOCUMENT_MODAL);
		setTitle("Store");
		setResizable(false);
		setBounds(100, 100, 850, 700);
		contentPane = new JPanel();
		contentPane.setBackground(_Settings.backgroundColor);
		contentPane.setBorder(null);
		setContentPane(contentPane);
		contentPane.setLayout(
				new MigLayout("", "[10][60,grow][10][80][130][10][100][10][210][10][80][10][50][10][150][10]", "[40][10][40][10][::400,grow][10][60][10][20,grow][10][60][10]"));

		this.store = store;
		items = new ArrayList<Stock>();

		UIManager.put("ComboBox.disabledForeground", _Settings.labelColor);
		UIManager.put("TextField.inactiveForeground", _Settings.labelColor);
		UIManager.put("TextArea.inactiveForeground", _Settings.labelColor);

		lblName = new JLabel("Name");
		lblName.setHorizontalAlignment(SwingConstants.RIGHT);
		lblName.setForeground(_Settings.labelColor);
		lblName.setBackground(null);
		lblName.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblName.setBorder(null);
		contentPane.add(lblName, "cell 1 0,grow");

		txtName = new JTextField();
		txtName.setMaximumSize(new Dimension(2147483647, 40));
		txtName.setForeground(_Settings.textFieldColor);
		txtName.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtName.setColumns(10);
		txtName.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtName.setBackground(_Settings.backgroundColor);
		txtName.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				isNameEmpty = txtName.getText().isEmpty();
				validateButton();
			}

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub

			}
		});
		contentPane.add(txtName, "cell 3 0 2 1,grow");

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

		lblCategory = new JLabel("<html><div style='text-align: center;'>User</div><div style='text-align: center;'>Category</div></html>");
		lblCategory.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCategory.setForeground(_Settings.labelColor);
		lblCategory.setBackground(_Settings.backgroundColor);
		lblCategory.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblCategory.setBorder(null);
		contentPane.add(lblCategory, "cell 6 0,grow");

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
					cbOwner.removeAllItems();
					for (UserObject userObject : userObjects) {
						String userString = ValueFormatter.formatUserObject(userObject);
						cbOwner.addItem(userString);
						ttUser.add(userString);
					}
					cbOwner.setSelectedIndex(-1);
					ttUser.sort(null);
					mcbrUser.setTooltips(ttUser);
					isOwnerSelected = false;
					validateButton();
				}
			}
		});
		contentPane.add(cbCategory, "cell 8 0,grow");

		lblOwner = new JLabel("Owner");
		lblOwner.setHorizontalAlignment(SwingConstants.RIGHT);
		lblOwner.setForeground(_Settings.labelColor);
		lblOwner.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblOwner.setBorder(null);
		lblOwner.setBackground(_Settings.backgroundColor);
		contentPane.add(lblOwner, "cell 10 0,grow");

		List<cores.Store> stores = (List<cores.Store>) DatabaseFacade.getDatabase("Stores").getList();
		SortedComboBoxModel<String> scbmStore = new SortedComboBoxModel<String>();
		MyComboBoxRenderer mcbrStore = new MyComboBoxRenderer();
		List<String> ttStore = new ArrayList<String>();
		for (cores.Store eachStore : stores) {
			String storeString = ValueFormatter.formatStore(eachStore);
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

		cbOwner = new JComboBox<String>(scbmUser);
		cbOwner.setRenderer(mcbrUser);
		cbOwner.setMaximumSize(new Dimension(210, 40));
		cbOwner.setFont(new Font("Century Gothic", Font.BOLD, 17));
		cbOwner.setForeground(Color.DARK_GRAY);
		cbOwner.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if ((e.getStateChange() == ItemEvent.SELECTED)) {
					isOwnerSelected = true;
					validateButton();
				}
			}
		});
		contentPane.add(cbOwner, "cell 12 0 3 1,grow");

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

		lblNetAsset = new JLabel("Net Asset");
		lblNetAsset.setHorizontalAlignment(SwingConstants.RIGHT);
		lblNetAsset.setForeground(_Settings.labelColor);
		lblNetAsset.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblNetAsset.setBorder(null);
		lblNetAsset.setBackground(_Settings.backgroundColor);
		contentPane.add(lblNetAsset, "cell 8 2 3 1,grow");

		txtNetAsset = new JTextField();
		txtNetAsset.setText("0.00");
		txtNetAsset.setEditable(false);
		txtNetAsset.setHorizontalAlignment(SwingConstants.CENTER);
		txtNetAsset.setForeground(_Settings.textFieldColor);
		txtNetAsset.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtNetAsset.setColumns(10);
		txtNetAsset.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtNetAsset.setBackground(_Settings.backgroundColor);
		contentPane.add(txtNetAsset, "cell 12 2 3 1,grow");

		spItem = new JScrollPane();
		spItem.setBackground(null);
		spItem.setOpaque(false);
		spItem.getViewport().setOpaque(false);
		contentPane.add(spItem, "cell 1 4 14 1,grow");

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
		dtm.addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent arg0) {
				// TODO Auto-generated method stub
				double value = calculateNetAsset();
				txtNetAsset.setText(ValueFormatter.formatMoney(value));
			}
		});

		tblItem = new JTable(dtm);
		tblItem.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblItem.setGridColor(_Settings.labelColor);
		tblItem.getTableHeader().setForeground(_Settings.labelColor);
		tblItem.getTableHeader().setBackground(_Settings.backgroundColor);
		tblItem.getTableHeader().setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		tblItem.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
		tblItem.setFont(new Font("Century Gothic", Font.BOLD, 16));
		tblItem.setBackground(_Settings.backgroundColor);
		tblItem.setForeground(_Settings.textFieldColor);
		tblItem.setRowHeight(30);
		spItem.setViewportView(tblItem);

		btnAdd = new JButton("ADD");
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frames.Stock stock = new frames.Stock((Store) getThis(), getStore(), (Store) getThis());
				stock.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent e) {
						// items = stock.getItems();
						updateTable();
					}
				});
			}
		});
		btnAdd.setForeground(_Settings.labelColor);
		btnAdd.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnAdd.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnAdd.setBackground(_Settings.backgroundColor);
		contentPane.add(btnAdd, "cell 14 6,grow");

		btnDelete = new JButton("DELETE");
		btnDelete.setForeground(_Settings.labelColor);
		btnDelete.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnDelete.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnDelete.setBackground(_Settings.backgroundColor);
		btnDelete.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				int row = tblItem.getSelectedRow();
				if (row != -1) {
					String stringId = (String) dtm.getValueAt(row, 0);
					ItemFacade.removeStockById(items, ValueFormatter.parseItem(stringId).getId());
					String amountString = (String) dtm.getValueAt(row, 6);
					if (!amountString.equals("-") && getStore() != null) {
						double amount = ValueFormatter.parseMoney(amountString);
						RecordFacade.adjustRecord(amount, false, null, getStore().getInventory(), "Stock removed");
					}
					updateTable();
					txtStatus.setText("Item deleted!");
					String storeString = "unfinished store";
					if (getStore() != null)
						storeString = ValueFormatter.formatStore(getStore());
					LOGGER.Activity.log("Stock", LOGGER.DELETE, stringId, storeString);
				} else
					txtStatus.setText("Select an item to delete!");
			}
		});
		contentPane.add(btnDelete, "cell 9 6 4 1,grow");

		txtStatus = new JTextField();
		txtStatus.setHorizontalAlignment(SwingConstants.RIGHT);
		txtStatus.setForeground(_Settings.labelColor);
		txtStatus.setFont(new Font("Arial Black", Font.PLAIN, 15));
		txtStatus.setEditable(false);
		txtStatus.setColumns(10);
		txtStatus.setBorder(null);
		txtStatus.setBackground(_Settings.backgroundColor);
		contentPane.add(txtStatus, "cell 8 8 7 1,grow");
		contentPane.add(btnNew, "cell 1 10 3 1,grow");

		btnSave = new JButton("SAVE");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String name = txtName.getText().trim();
				UserObject owner = ValueFormatter.parseUserObject((String) cbOwner.getSelectedItem());
				if (addStore(name, owner)) {
					txtStatus.setText("Record saved!");
					Stores db = (Stores) DatabaseFacade.getDatabase("Stores");
					setStore(db.get(db.find(name, owner)));
					setFormEnabled(false);
					LOGGER.Activity.log("Store", LOGGER.CREATE, ValueFormatter.formatStore(getStore()));
				} else
					txtStatus.setText("Store already exists or matches inventory name!");
			}
		});
		btnSave.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnSave.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnSave.setBackground(_Settings.backgroundColor);
		btnSave.setForeground(_Settings.labelColor);
		contentPane.add(btnSave, "cell 14 10,grow");

		btnClose = new JButton("CLOSE");
		btnClose.setForeground(_Settings.labelColor);
		btnClose.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnClose.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnClose.setBackground(_Settings.backgroundColor);
		btnClose.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				dispose();
			}
		});
		contentPane.add(btnClose, "cell 9 10 4 1,grow");

		getRootPane().setDefaultButton(btnAdd);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
				((Main) windowOwner).cardStore.updateTable();
				((Main) windowOwner).cardItem.calculateStock();
				((Main) windowOwner).cardItem.updateTable();
			}
		});

		populateFrame();

		pack();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}

}
