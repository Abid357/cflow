package cards;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import cores.Item;
import cores.Stock;
import cores.UserObject;
import databases.Stores;
import frames.Main;
import globals.DatabaseFacade;
import globals.ValueFormatter;
import globals._Settings;
import net.miginfocom.swing.MigLayout;

public class Store extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Main mainFrame;
	private JTable tblItem;
	private DefaultTableModel dtmItem, dtmStore;
	private String[] headersItem = { "ID", "NAME", "UNIT", "PRICE/UNIT", "DETAILS", "QUANTITY", "VALUE" };
	private String[] headersStore = { "NAME", "OWNER", "NET ASSET" };
	private JTable tblStore;

	private void listItems(cores.Store store) {
		dtmItem.setRowCount(0);
		List<Stock> items = store.getItems();
		for (Stock stock : items) {
			Item item = stock.getItem();
			String quantity = "-";
			String value = "-";
			if (stock.getQuantity() != -1.0) {
				quantity = ValueFormatter.formatQuantity(stock.getQuantity());
				value = ValueFormatter.formatMoneyNicely(stock.getQuantity() * stock.getPrice());
			}
			dtmItem.addRow(new Object[] { ValueFormatter.formatItem(item), item.getName(), item.getUnit(),
					ValueFormatter.formatMoney(stock.getPrice()), item.getDetails(),
					quantity, value });
		}
	}

	public void updateTable() {
		dtmItem.setRowCount(0);
		dtmStore.setRowCount(0);
		Stores db = (Stores) DatabaseFacade.getDatabase("Stores");
		List<cores.Store> list = db.getList();
		for (cores.Store store : list)
			dtmStore.addRow(new Object[] { store.getName(), ValueFormatter.formatUserObject(store.getOwner()),
					ValueFormatter.formatMoneyNicely(store.getInventory().getBalance()) });
	}

	/**
	 * Create the panel.
	 */
	public Store(Main frame) {
		this.mainFrame = frame;
		setBackground(_Settings.backgroundColor);
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setLayout(
				new MigLayout("", "[150][10][150][10][150][grow]", "[50][10][150:150,grow][10][40][10][150:150,grow]"));

		JButton btnAddStore = new JButton("ADD STORE");
		btnAddStore.setBackground(_Settings.backgroundColor);
		btnAddStore.setForeground(_Settings.labelColor);
		btnAddStore.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnAddStore.setFont(new Font("Arial", Font.BOLD, 14));
		btnAddStore.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				new frames.Store(mainFrame, null);
			}
		});
		add(btnAddStore, "cell 0 0,grow");

		JScrollPane spItem = new JScrollPane();
		spItem.setBackground(null);
		spItem.setOpaque(false);
		spItem.getViewport().setOpaque(false);

		JButton btnAddDelivery = new JButton("ADD DELIVERY");
		btnAddDelivery.setForeground(_Settings.labelColor);
		btnAddDelivery.setFont(new Font("Arial", Font.BOLD, 14));
		btnAddDelivery.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnAddDelivery.setBackground(_Settings.backgroundColor);
		btnAddDelivery.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				new frames.Delivery(mainFrame, null);
			}
		});
		add(btnAddDelivery, "cell 2 0,grow");

		JButton btnPrintList = new JButton("PRINT LIST");
		btnPrintList.setBackground(_Settings.backgroundColor);
		btnPrintList.setForeground(_Settings.labelColor);
		btnPrintList.setFont(new Font("Arial", Font.BOLD, 14));
		btnPrintList.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		add(btnPrintList, "cell 4 0,grow");
		add(spItem, "cell 0 6 6 1,grow");

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

			public void mouseClicked(MouseEvent e) {
				int row = tblItem.getSelectedRow();
				if (row != -1 && e.getClickCount() == 2) {
					Item item = ValueFormatter.parseItem((String) dtmItem.getValueAt(row, 0));
					new frames.Item(mainFrame, item);
				}
			}
		});
		spItem.setViewportView(tblItem);

		JLabel lblItems = new JLabel("Items");
		lblItems.setHorizontalAlignment(SwingConstants.CENTER);
		lblItems.setForeground(_Settings.labelColor);
		lblItems.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblItems.setBorder(null);
		lblItems.setBackground(_Settings.backgroundColor);
		add(lblItems, "cell 0 4 6 1,grow");

		JScrollPane spStore = new JScrollPane();
		spStore.setBackground(null);
		spStore.setOpaque(false);
		spStore.getViewport().setOpaque(false);
		add(spStore, "cell 0 2 6 1,grow");

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

		tblStore.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				int row = tblStore.getSelectedRow();
				if (row != -1) {
					String name = (String) tblStore.getValueAt(row, 0);
					UserObject owner = ValueFormatter.parseUserObject((String) tblStore.getValueAt(row, 1));
					Stores db = (Stores) DatabaseFacade.getDatabase("Stores");
					cores.Store store = db.get(db.find(name, owner));
					listItems(store);
					if (e.getClickCount() == 2) {
						new frames.Store(mainFrame, store);

					}
				}
			}
		});
		spStore.setViewportView(tblStore);
		updateTable();
	}
}
