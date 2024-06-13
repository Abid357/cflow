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

import cores.Product;
import cores.Stock;
import cores.Transaction;
import databases.Items;
import databases.Productions;
import frames.Main;
import globals.DatabaseFacade;
import globals.ValueFormatter;
import globals._Settings;
import helpers.MyComboBoxRenderer;
import helpers.SortedComboBoxModel;
import helpers.TransactionStatus;
import net.miginfocom.swing.MigLayout;

public class Production extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static String SORT_BY_DATE = "SORT BY: DATE";
	private static String SORT_BY_ID = "SORT BY: ID";

	private Main mainFrame;
	private JTable tblProduction;
	private JDateChooser dcFrom, dcTo;
	private Date toDate, fromDate;
	private DefaultTableModel dtm;
	private String[] columnHeaders = { "ID", "PRODUCT", "QUANTITY", "UNIT", "UNIT PRICE", "TOTAL COST", "STATUS" };
	private JComboBox<String> cbProduct, cbStatus;
	private MyComboBoxRenderer mcbrProduct;
	private JButton btnSort;

	public void updateOptions() {
		cbProduct.removeAllItems();
		cbProduct.addItem("ANY");
		List<String> ttProduct = new ArrayList<String>();
		Items dbItems = (Items) DatabaseFacade.getDatabase("Items");
		List<Product> itemList = dbItems.getProductList();
		for (Product item : itemList) {
			String itemString = ValueFormatter.formatItemForComboBox(item);
			cbProduct.addItem(itemString);
			ttProduct.add(itemString);

		}
		ttProduct.sort(null);
		ttProduct.add(0, "ANY");
		mcbrProduct.setTooltips(ttProduct);
		cbProduct.setSelectedIndex(0);
	}

	public void resetSelections() {
		cbProduct.setSelectedIndex(0);
		cbStatus.setSelectedIndex(0);
		fromDate = null;
		toDate = null;
		dcFrom.setDate(null);
		dcTo.setDate(null);
		updateTable();
	}

	private double calculateCost(cores.Production production) {
		double materialCost = 0.0;
		List<Stock> materials = production.getMaterials();
		for (int i = 0; i < materials.size(); i++) {
			if (production.getMaterialStores().get(i) != null)
				materialCost += (materials.get(i).getQuantity() * materials.get(i).getPrice());
		}
		double otherCost = 0.0;
		List<Transaction> costs = production.getCosts();
		for (int i = 0; i < costs.size(); i++)
			otherCost += costs.get(i).getAmount() + costs.get(i).getTax();
		return materialCost + otherCost;
	}

	public void updateTable() {
		dtm.setRowCount(0);
		Productions db = (Productions) DatabaseFacade.getDatabase("Productions");
		List<cores.Production> list = db.getList();

		String selectedProduct = (String) cbProduct.getSelectedItem();
		if (!selectedProduct.equals("ANY")) {
			Product product = (Product) ValueFormatter.parseItemFromComboBox(selectedProduct);
			List<cores.Production> productBasedProductions = new ArrayList<cores.Production>();
			for (cores.Production production : list)
				if (production.getProduct().getItem().equals(product))
					productBasedProductions.add(production);
			list = productBasedProductions;
		}
		if (fromDate != null) {
			List<cores.Production> datedProductions = new ArrayList<cores.Production>();
			for (cores.Production production : list)
				if (production.getStartDate().after(fromDate) || production.getStartDate().equals(fromDate))
					datedProductions.add(production);
			list = datedProductions;
		}
		if (toDate != null) {
			List<cores.Production> datedProductions = new ArrayList<cores.Production>();
			for (cores.Production production : list)
				if (production.getStartDate().before(toDate) || production.getStartDate().equals(toDate))
					datedProductions.add(production);
			list = datedProductions;
		}
		String selectedItemStatus = (String) cbStatus.getSelectedItem();
		if (!selectedItemStatus.equals("ANY")) {
			List<cores.Production> statusProductions = new ArrayList<cores.Production>();
			for (cores.Production production : list)
				if (production.getStatus().equals(TransactionStatus.valueOf(selectedItemStatus)))
					statusProductions.add(production);
			list = statusProductions;
		}

		if (btnSort.getText().equals(SORT_BY_DATE))
			Collections.sort(list);
		else
			Collections.sort(list, new Comparator<cores.Production>() {
				public int compare(cores.Production p1, cores.Production p2) {
					return p2.getStartDate().compareTo(p1.getStartDate());
				}
			});

		for (cores.Production production : list) {
			dtm.addRow(new Object[] { production.getAlternativeId(), production.getProduct().getItem().getName(),
					ValueFormatter.formatQuantity(production.getProduct().getQuantity()), production.getProductUnit(),
					ValueFormatter.formatMoneyNicely(production.getProduct().getPrice()),
					ValueFormatter.formatMoneyNicely(calculateCost(production)), production.getStatus() });
		}
	}

	/**
	 * Create the panel.
	 */
	public Production(Main frame) {
		this.mainFrame = frame;
		setBackground(_Settings.backgroundColor);
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setLayout(new MigLayout("", "[150][10][150][10][150][10][150][26.00][150][grow]",
				"[50][10][20][30][10][grow][10][50]"));

		JButton btnAddSale = new JButton("ADD PRODUCTION");
		btnAddSale.setBackground(_Settings.backgroundColor);
		btnAddSale.setForeground(_Settings.labelColor);
		btnAddSale.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnAddSale.setFont(new Font("Arial", Font.BOLD, 14));
		btnAddSale.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				new frames.Production(mainFrame, null);
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

		JButton btnPrintStatement = new JButton("PRINT STATEMENT");
		btnPrintStatement.setBackground(_Settings.backgroundColor);
		btnPrintStatement.setForeground(_Settings.labelColor);
		btnPrintStatement.setFont(new Font("Arial", Font.BOLD, 14));
		btnPrintStatement.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		add(btnPrintStatement, "cell 2 0,grow");
		add(btnSort, "cell 6 0,grow");

		JLabel lblProduct = new JLabel("Product");
		lblProduct.setFont(new Font("Arial", Font.BOLD, 15));
		lblProduct.setHorizontalAlignment(SwingConstants.CENTER);
		lblProduct.setBorder(null);
		lblProduct.setBackground(null);
		lblProduct.setForeground(_Settings.labelColor);
		add(lblProduct, "cell 0 2,grow");

		JLabel lblFrom = new JLabel("From");
		lblFrom.setHorizontalAlignment(SwingConstants.CENTER);
		lblFrom.setFont(new Font("Arial", Font.BOLD, 15));
		lblFrom.setBorder(null);
		lblFrom.setBackground(null);
		lblFrom.setForeground(_Settings.labelColor);
		add(lblFrom, "cell 2 2,grow");

		JLabel lblTo = new JLabel("To");
		lblTo.setHorizontalAlignment(SwingConstants.CENTER);
		lblTo.setFont(new Font("Arial", Font.BOLD, 15));
		lblTo.setBorder(null);
		lblTo.setBackground(null);
		lblTo.setForeground(_Settings.labelColor);
		add(lblTo, "cell 4 2,grow");

		JLabel lblStatus = new JLabel("Status");
		lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
		lblStatus.setFont(new Font("Arial", Font.BOLD, 15));
		lblStatus.setBorder(null);
		lblStatus.setBackground(null);
		lblStatus.setForeground(_Settings.labelColor);
		add(lblStatus, "cell 6 2,grow");

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
		add(btnRefresh, "cell 8 2 1 2,grow");

		mcbrProduct = new MyComboBoxRenderer();
		cbProduct = new JComboBox<String>(new SortedComboBoxModel<String>());
		cbProduct.setRenderer(mcbrProduct);
		cbProduct.setMaximumSize(new Dimension(150, 32767));
		cbProduct.setFont(new Font("Century Gothic", Font.BOLD, 14));
		cbProduct.setBorder(null);
		cbProduct.setForeground(Color.DARK_GRAY);
		add(cbProduct, "cell 0 3,grow");

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
				toDate = ValueFormatter.setTimeToZero(toDate);
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
				fromDate = ValueFormatter.setTimeToZero(fromDate);
				dcTo.setMinSelectableDate(fromDate);
				updateTable();
			}
		});

		add(dcFrom, "cell 2 3,grow");
		add(dcTo, "cell 4 3,grow");

		cbStatus = new JComboBox<String>();
		cbStatus.setMaximumSize(new Dimension(150, 32767));
		cbStatus.setFont(new Font("Century Gothic", Font.BOLD, 14));
		cbStatus.setBorder(null);
		cbStatus.setForeground(Color.DARK_GRAY);
		cbStatus.addItem("ANY");
		cbStatus.addItem(TransactionStatus.ONGOING.toString());
		cbStatus.addItem(TransactionStatus.COMPLETE.toString());
		cbStatus.setSelectedIndex(0);
		add(cbStatus, "cell 6 3,grow");

		JScrollPane spTransaction = new JScrollPane();
		spTransaction.setBackground(null);
		spTransaction.setOpaque(false);
		spTransaction.getViewport().setOpaque(false);
		add(spTransaction, "cell 0 5 10 1,grow");

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
		tblProduction = new JTable(dtm);
		tblProduction.setGridColor(_Settings.labelColor);
		tblProduction.getTableHeader().setForeground(_Settings.labelColor);
		tblProduction.getTableHeader().setBackground(_Settings.backgroundColor);
		tblProduction.getTableHeader().setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		tblProduction.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
		tblProduction.setFont(new Font("Century Gothic", Font.BOLD, 16));
		tblProduction.setBackground(_Settings.backgroundColor);
		tblProduction.setForeground(_Settings.textFieldColor);
		tblProduction.setRowHeight(30);
		tblProduction.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		tblProduction.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		tblProduction.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		tblProduction.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
		tblProduction.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
		tblProduction.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
		tblProduction.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				if (e.getClickCount() == 2) {
					int row = tblProduction.getSelectedRow();
					if (row != -1) {
						String id = (String) tblProduction.getValueAt(row, 0);
						Productions db = (Productions) DatabaseFacade.getDatabase("Productions");
						cores.Production production = db.get(db.find(id));
						new frames.Production(mainFrame, production);
					}
				}
			}
		});
		spTransaction.setViewportView(tblProduction);

		updateOptions();
		cbProduct.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if ((e.getStateChange() == ItemEvent.SELECTED)) {
					updateTable();
				}
			}
		});
		cbStatus.addItemListener(new ItemListener() {

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
