package cards;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import cores.StoreInventory;
import databases.Inventories;
import frames.Main;
import globals.DatabaseFacade;
import globals.LOGGER;
import globals.ReportFacade;
import globals.ValueFormatter;
import globals._Settings;
import helpers.MyCellRenderer;
import helpers.MyTableModel;
import net.miginfocom.swing.MigLayout;

public class Inventory extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Main mainFrame;
	private JTable tblInventory;
	private JTextField txtLiquidAsset;
	private cards.Transaction cardTransaction;
	private cards.Settings cardSettings;
	private JTextField txtSolidAsset;

	public void setTotalLiquidAsset() {
		Inventories db = (Inventories) DatabaseFacade.getDatabase("Inventories");
		List<cores.Inventory> list = db.getList();
		double total = 0;
		for (cores.Inventory inventory : list)
			if (!(inventory instanceof StoreInventory))
				total += inventory.getBalance();
		txtLiquidAsset.setText(ValueFormatter.formatMoneyNicely(total));
	}

	public void setTotalSolidAsset() {
		Inventories db = (Inventories) DatabaseFacade.getDatabase("Inventories");
		List<cores.Inventory> list = db.getList();
		double total = 0;
		for (cores.Inventory inventory : list)
			if (inventory instanceof StoreInventory)
				total += inventory.getBalance();
		txtSolidAsset.setText(ValueFormatter.formatMoneyNicely(total));
	}

	public void updateTable() {
		Inventories db = (Inventories) DatabaseFacade.getDatabase("Inventories");
		List<cores.Inventory> list = db.getList();
		tblInventory.setModel(new MyTableModel(list));
		setTotalLiquidAsset();
		setTotalSolidAsset();
	}

	/**
	 * Create the panel.
	 */
	public Inventory(Main frame, cards.Transaction card1, cards.Settings card2) {
		this.mainFrame = frame;
		setBackground(_Settings.backgroundColor);
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setLayout(new MigLayout("", "[150][10][150][10][150][10][150][10][120][10][150][10][120][10][150][grow]",
				"[50][10][grow]"));

		cardTransaction = card1;
		cardSettings = card2;

		JButton btnAddInventory = new JButton("<html><center><p>ADD INVENTORY</p></center></html>");
		btnAddInventory.setBackground(_Settings.backgroundColor);
		btnAddInventory.setForeground(_Settings.labelColor);
		btnAddInventory.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnAddInventory.setFont(new Font("Arial", Font.BOLD, 14));
		btnAddInventory.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub

				frames.Inventory frameInventory = new frames.Inventory(mainFrame);
				frameInventory.addWindowListener(new WindowAdapter() {

					@Override
					public void windowClosed(WindowEvent e) {
						// TODO Auto-generated method stub

						updateTable();
						cardTransaction.updateOptions();
						cardSettings.updateOptions();
					}
				});
			}
		});
		add(btnAddInventory, "cell 0 0,grow");

		JButton btnTransfer = new JButton("<html><center><p>ADD TRANSFER</p></center></html>");
		btnTransfer.setBackground(_Settings.backgroundColor);
		btnTransfer.setForeground(_Settings.labelColor);
		btnTransfer.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnTransfer.setFont(new Font("Arial", Font.BOLD, 14));
		btnTransfer.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub

				frames.IntraTransfer frameTransfer2 = new frames.IntraTransfer(mainFrame);
				frameTransfer2.addWindowListener(new WindowAdapter() {

					@Override
					public void windowClosed(WindowEvent e) {
						// TODO Auto-generated method stub

						updateTable();
					}
				});
			}
		});
		add(btnTransfer, "cell 2 0,grow");

		JButton btnPrintStatement = new JButton("<html><center><p>PRINT STATEMENT</p></center></html>");
		btnPrintStatement.setBackground(_Settings.backgroundColor);
		btnPrintStatement.setForeground(_Settings.labelColor);
		btnPrintStatement.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnPrintStatement.setFont(new Font("Arial", Font.BOLD, 14));
		btnPrintStatement.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub

				frames.StatementOptions statementOptions = new frames.StatementOptions(mainFrame);
				statementOptions.addWindowListener(new WindowAdapter() {

					@Override
					public void windowClosed(WindowEvent e) {
						// TODO Auto-generated method stub

						if (statementOptions.isConfirmed()) {
							List<String> list = new ArrayList<String>();
							list.add("DESCRIPTION");
							list.add("DATE");
							list.add("CREDIT");
							list.add("DEBIT");
							list.add("BALANCE");

							Date fromDate = statementOptions.getFromDate();
							if (fromDate == null) {
								Calendar cal = Calendar.getInstance();
								cal.set(2018, 0, 0);
								fromDate = cal.getTime();
							}
							Date toDate = statementOptions.getToDate();
							if (toDate == null) {
								Calendar cal = Calendar.getInstance();
								toDate = cal.getTime();
							}

//							if (PRINTER.printStatement(statementOptions.getTable(),
//									"Statement of " + statementOptions.getSelectedInventory(), fromDate, toDate, list))
//								LOGGER.Activity.log("Inventory Statement", LOGGER.CREATE);
						}
					}
				});
			}
		});
		add(btnPrintStatement, "cell 4 0,grow");

		JButton btnPrintList = new JButton("<html><center><p>PRINT LIST</p></center></html>");
		btnPrintList.setBackground(_Settings.backgroundColor);
		btnPrintList.setForeground(_Settings.labelColor);
		btnPrintList.setFont(new Font("Arial", Font.BOLD, 14));
		btnPrintList.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnPrintList.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				if (ReportFacade.printInventoryList())
					LOGGER.Activity.log("Inventory List", LOGGER.CREATE);
			}
		});
		add(btnPrintList, "cell 6 0,grow");

		JLabel lblTotalLiquidAsset = new JLabel("<html><center><p>Liquid Asset:</p></center></html>");
		lblTotalLiquidAsset.setBackground(_Settings.backgroundColor);
		lblTotalLiquidAsset.setForeground(_Settings.labelColor);
		lblTotalLiquidAsset.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTotalLiquidAsset.setFont(new Font("Arial", Font.BOLD, 17));
		add(lblTotalLiquidAsset, "cell 8 0,grow");

		txtLiquidAsset = new JTextField();
		txtLiquidAsset.setBackground(_Settings.backgroundColor);
		txtLiquidAsset.setForeground(_Settings.textFieldColor);
		txtLiquidAsset.setDisabledTextColor(_Settings.textFieldColor);
		txtLiquidAsset.setHorizontalAlignment(SwingConstants.CENTER);
		txtLiquidAsset.setFont(new Font("Century Gothic", Font.BOLD, 17));
		txtLiquidAsset.setEnabled(false);
		txtLiquidAsset.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		add(txtLiquidAsset, "cell 10 0,grow");
		txtLiquidAsset.setColumns(10);

		JScrollPane spInventory = new JScrollPane();
		spInventory.setBackground(null);
		spInventory.setOpaque(false);
		spInventory.getViewport().setOpaque(false);

		JLabel lblSolidAsset = new JLabel("<html><center><p>Solid Asset:</p></center></html>");
		lblSolidAsset.setBackground(_Settings.backgroundColor);
		lblSolidAsset.setForeground(_Settings.labelColor);
		lblSolidAsset.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSolidAsset.setFont(new Font("Arial", Font.BOLD, 17));
		add(lblSolidAsset, "cell 12 0,grow");

		txtSolidAsset = new JTextField();
		txtSolidAsset.setText("0.00");
		txtSolidAsset.setBackground(_Settings.backgroundColor);
		txtSolidAsset.setForeground(_Settings.textFieldColor);
		txtSolidAsset.setDisabledTextColor(_Settings.textFieldColor);
		txtSolidAsset.setHorizontalAlignment(SwingConstants.CENTER);
		txtSolidAsset.setFont(new Font("Century Gothic", Font.BOLD, 17));
		txtSolidAsset.setEnabled(false);
		txtSolidAsset.setColumns(10);
		txtSolidAsset.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtSolidAsset.setBackground((Color) null);
		add(txtSolidAsset, "cell 14 0,grow");
		add(spInventory, "cell 0 2 16 1,grow");

		tblInventory = new JTable();
		tblInventory.setDefaultRenderer(cores.Inventory.class, new MyCellRenderer());
		tblInventory.setBackground(_Settings.backgroundColor);
		tblInventory.setForeground(_Settings.textFieldColor);
		tblInventory.setRowHeight(120);
		tblInventory.setTableHeader(null);
		spInventory.setViewportView(tblInventory);

		updateTable();
	}
}
