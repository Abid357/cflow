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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import cores.Material;
import cores.Product;
import cores.Stock;
import cores.Store;
import databases.Items;
import databases.Stores;
import frames.Main;
import globals.DatabaseFacade;
import globals.ItemFacade;
import globals.LOGGER;
import globals.ReportFacade;
import globals.ValueFormatter;
import globals._Settings;
import helpers.ItemType;
import helpers.SortedComboBoxModel;
import net.miginfocom.swing.MigLayout;

public class Item extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Main mainFrame;
	private JTable tblItem;
	private DefaultTableModel dtmItem, dtmStore;
	private String[] headersItem = { "ID", "NAME", "UNIT", "PRICE/UNIT", "DETAILS", "COMPONENTS", "QUANTITY" };
	private String[] headersStore = { "NAME", "OWNER", "QUANTITY", "VALUE" };
	private JComboBox<String> cbType;
	private JTable tblStore;
	private Map<String, List<Store>> stores;
	private Map<String, Double> stock;

	public void calculateStock() {
		stock = new HashMap<String, Double>();
		stores = new HashMap<String, List<Store>>();
		Items dbItem = (Items) DatabaseFacade.getDatabase("Items");
		Stores dbStore = (Stores) DatabaseFacade.getDatabase("Stores");
		List<cores.Item> lstItem = dbItem.getList();
		List<Store> lstStore = dbStore.getList();
		for (cores.Item item : lstItem) {
			double quantity = 0;
			List<Store> storesWithItem = new ArrayList<Store>();
			for (Store store : lstStore) {
				Stock stock = ItemFacade.findStockById(store.getItems(), item.getId());
				if (stock != null) {
					quantity += stock.getQuantity();
					storesWithItem.add(store);
				}
			}
			String itemString = ValueFormatter.formatItem(item);
			stock.put(itemString, quantity);
			stores.put(itemString, storesWithItem);
		}
	}

	private void identifyStores(cores.Item item) {
		dtmStore.setRowCount(0);
		String itemString = ValueFormatter.formatItem(item);
		List<Store> storesWithItem = stores.get(itemString);
		for (Store store : storesWithItem) {
			Stock stock = ItemFacade.findStockById(store.getItems(), item.getId());
			String quantity = "-";
			String value = "-";
			if (stock.getQuantity() != -1.0) {
				quantity = ValueFormatter.formatQuantity(stock.getQuantity());
				value = ValueFormatter.formatMoneyNicely(stock.getQuantity() * stock.getPrice());
			}
			dtmStore.addRow(new Object[] { store.getName(), ValueFormatter.formatUserObject(store.getOwner()), quantity,
					value });
		}
	}

	public void updateTable() {
		dtmItem.setRowCount(0);
		dtmStore.setRowCount(0);
		Items db = (Items) DatabaseFacade.getDatabase("Items");
		List<cores.Item> list = db.getList();
		String type = (String) cbType.getSelectedItem();
		if (!type.equals("ANY")) {
			List<cores.Item> items = new ArrayList<cores.Item>();
			for (cores.Item item : list)
				if (item.getType().toString().equals(type))
					items.add(item);
			list = items;
		}
		for (cores.Item item : list) {
			List<Stock> components = null;
			if (item.getType().equals(ItemType.PRODUCT))
				components = ((Product) item).getComponents();
			else if (item.getType().equals(ItemType.MATERIAL))
				components = ((Material) item).getComponents();
			String componentString = "";
			if (components != null) {
				for (Stock stock : components)
					componentString += ValueFormatter.formatItem(stock.getItem()) + ", ";
			}
			if (!componentString.isEmpty())
				componentString = componentString.substring(0, componentString.length() - 2);
			String itemString = ValueFormatter.formatItem(item);
			String quantityString = "-";
			if (!item.getType().equals(ItemType.SERVICE))
				quantityString = ValueFormatter.formatQuantity(stock.get(itemString));
			dtmItem.addRow(new Object[] { itemString, item.getName(), item.getUnit(),
					ValueFormatter.formatMoney(item.getPrice()), item.getDetails(), componentString, quantityString });
		}
	}

	/**
	 * Create the panel.
	 */
	public Item(Main frame) {
		this.mainFrame = frame;
		setBackground(_Settings.backgroundColor);
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setLayout(new MigLayout("", "[150][10][150][grow]",
				"[50][10][20][30][10][150:150,grow][10][40][10][150:150,grow]"));

		JButton btnAddItem = new JButton("ADD ITEM");
		btnAddItem.setBackground(_Settings.backgroundColor);
		btnAddItem.setForeground(_Settings.labelColor);
		btnAddItem.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnAddItem.setFont(new Font("Arial", Font.BOLD, 14));
		btnAddItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				new frames.Item(mainFrame, null);
			}
		});
		add(btnAddItem, "cell 0 0,grow");

		JLabel lblType = new JLabel("Type");
		lblType.setFont(new Font("Arial", Font.BOLD, 15));
		lblType.setHorizontalAlignment(SwingConstants.CENTER);
		lblType.setBorder(null);
		lblType.setBackground(null);
		lblType.setForeground(_Settings.labelColor);
		add(lblType, "cell 0 2,grow");

		cbType = new JComboBox<String>(new SortedComboBoxModel<String>());
		cbType.setMaximumSize(new Dimension(150, 32767));
		cbType.setFont(new Font("Century Gothic", Font.BOLD, 14));
		cbType.setBorder(null);
		cbType.setForeground(Color.DARK_GRAY);
		cbType.addItem("ANY");
		for (ItemType type : ItemType.values())
			cbType.addItem(type.toString());
		cbType.setSelectedIndex(0);
		cbType.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				// TODO Auto-generated method stub
				if (arg0.getStateChange() == ItemEvent.SELECTED)
					updateTable();
			}
		});
		add(cbType, "cell 0 3,grow");

		JButton btnPrintList = new JButton("PRINT LIST");
		btnPrintList.setBackground(_Settings.backgroundColor);
		btnPrintList.setForeground(_Settings.labelColor);
		btnPrintList.setFont(new Font("Arial", Font.BOLD, 14));
		btnPrintList.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnPrintList.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String type = (String) cbType.getSelectedItem();
				if (ReportFacade.printItemList(type))
					LOGGER.Activity.log("Item List", LOGGER.CREATE);
			}
		});
		add(btnPrintList, "cell 2 0,grow");

		JScrollPane spItem = new JScrollPane();
		spItem.setBackground(null);
		spItem.setOpaque(false);
		spItem.getViewport().setOpaque(false);
		add(spItem, "cell 0 5 4 1,grow");

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);

		dtmItem = new DefaultTableModel() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}

		};
		dtmItem.setColumnIdentifiers(headersItem);
		tblItem = new JTable(dtmItem);
		tblItem.setFillsViewportHeight(true);
		tblItem.setGridColor(_Settings.labelColor);
		tblItem.getTableHeader().setForeground(_Settings.labelColor);
		tblItem.getTableHeader().setBackground(_Settings.backgroundColor);
		tblItem.getTableHeader().setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		tblItem.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
		tblItem.setFont(new Font("Century Gothic", Font.BOLD, 16));
		tblItem.setBackground(_Settings.backgroundColor);
		tblItem.setForeground(_Settings.textFieldColor);
		tblItem.setRowHeight(30);
		tblItem.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		tblItem.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		tblItem.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		tblItem.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
		tblItem.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
		tblItem.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				int row = tblItem.getSelectedRow();
				if (row != -1) {
					cores.Item item = ValueFormatter.parseItem((String) dtmItem.getValueAt(row, 0));
					identifyStores(item);
					if (e.getClickCount() == 2) {
						new frames.Item(mainFrame, item);
					}
				}

			}
		});
		spItem.setViewportView(tblItem);

		JLabel lblStores = new JLabel("Stores");
		lblStores.setHorizontalAlignment(SwingConstants.CENTER);
		lblStores.setForeground(_Settings.labelColor);
		lblStores.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblStores.setBorder(null);
		lblStores.setBackground(_Settings.backgroundColor);
		add(lblStores, "cell 0 7 4 1,grow");

		JScrollPane spStore = new JScrollPane();
		spStore.setBackground(null);
		spStore.setOpaque(false);
		spStore.getViewport().setOpaque(false);
		add(spStore, "cell 0 9 4 1,grow");

		dtmStore = new DefaultTableModel() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}

		};
		dtmStore.setColumnIdentifiers(headersStore);
		tblStore = new JTable(dtmStore);
		tblStore.setFillsViewportHeight(true);
		tblStore.setGridColor(_Settings.labelColor);
		tblStore.getTableHeader().setForeground(_Settings.labelColor);
		tblStore.getTableHeader().setBackground(_Settings.backgroundColor);
		tblStore.getTableHeader().setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		tblStore.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
		tblStore.setFont(new Font("Century Gothic", Font.BOLD, 16));
		tblStore.setBackground(_Settings.backgroundColor);
		tblStore.setForeground(_Settings.textFieldColor);
		tblStore.setRowHeight(30);
		tblStore.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		tblStore.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		tblStore.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				int row = tblStore.getSelectedRow();
				if (row != -1 && e.getClickCount() == 2) {
					String storeString = (String) dtmStore.getValueAt(row, 0) + " ("
							+ (String) dtmStore.getValueAt(row, 1) + ")";
					Store store = ValueFormatter.parseStore(storeString);
					new frames.Store(mainFrame, store);
				}
			}
		});
		spStore.setViewportView(tblStore);

		cbType.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if ((e.getStateChange() == ItemEvent.SELECTED)) {
					updateTable();
				}
			}
		});
		calculateStock();
		updateTable();
	}
}
