package cards;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import cores.SaleTransaction;
import databases.SaleTransactions;
import globals.DatabaseFacade;
import globals.ReportFacade;
import globals.ValueFormatter;
import globals._Settings;
import helpers.MyComboBoxRenderer;
import helpers.SortedComboBoxModel;
import net.miginfocom.swing.MigLayout;

public class SalesReport5 extends JPanel implements IReport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txtTitle;
	private JPanel pnlTitle;
	private JPanel pnlOptions;
	private JCheckBox chckbxStartDate;
	private JCheckBox chckbxLastSaleDate;
	private JCheckBox chckbxTotalSale;
	private JCheckBox chckbxTotalPaid;
	private JCheckBox chckbxOutstandingBalance;
	private List<JCheckBox> checkboxes;

	/**
	 * Create the panel.
	 */
	public SalesReport5() {
		setBackground(_Settings.backgroundColor);
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setLayout(new MigLayout("", "[]", "[][]"));

		checkboxes = new ArrayList<JCheckBox>();

		pnlTitle = new JPanel();
		pnlTitle.setBackground(_Settings.backgroundColor);
		pnlTitle.setForeground(_Settings.labelColor);
		pnlTitle.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Enter a title",
				TitledBorder.LEADING, TitledBorder.TOP, null, _Settings.textFieldColor));
		add(pnlTitle, "cell 0 0,grow");
		pnlTitle.setLayout(new MigLayout("", "[75][10][275]", "[50]"));

		JLabel lblTitle = new JLabel("Title:");
		pnlTitle.add(lblTitle, "cell 0 0,alignx right,growy");
		lblTitle.setBackground(_Settings.backgroundColor);
		lblTitle.setForeground(_Settings.labelColor);
		lblTitle.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTitle.setFont(new Font("Arial Black", Font.BOLD, 17));
		lblTitle.setBorder(null);

		txtTitle = new JTextField();
		pnlTitle.add(txtTitle, "cell 2 0,grow");
		txtTitle.setBackground(_Settings.backgroundColor);
		txtTitle.setForeground(_Settings.textFieldColor);
		txtTitle.setDisabledTextColor(_Settings.textFieldColor);
		txtTitle.setHorizontalAlignment(SwingConstants.CENTER);
		txtTitle.setFont(new Font("Century Gothic", Font.BOLD, 17));
		txtTitle.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtTitle.setColumns(10);

		SaleTransactions dbSales = (SaleTransactions) DatabaseFacade.getDatabase("SaleTransactions");
		List<SaleTransaction> lstSales = dbSales.getList();
		SortedSet<String> list = new TreeSet<String>();
		lstSales.forEach((sale) -> list.add(ValueFormatter.formatUserObject(sale.getCustomer())));

		MyComboBoxRenderer mcbrCustomer = new MyComboBoxRenderer();
		List<String> ttCustomer = new ArrayList<String>();
		SortedComboBoxModel<String> scbmCustomer = new SortedComboBoxModel<String>();

		Iterator<String> iterator = list.iterator();
		while (iterator.hasNext()) {
			String customerString = iterator.next();
			scbmCustomer.addElement(customerString);
			ttCustomer.add(customerString);
		}
		mcbrCustomer.setTooltips(ttCustomer);

		pnlOptions = new JPanel();
		pnlOptions.setForeground(_Settings.labelColor);
		pnlOptions.setBackground(_Settings.backgroundColor);
		pnlOptions.setBorder(
				new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Select additional features (optional)",
						TitledBorder.LEADING, TitledBorder.TOP, null, _Settings.textFieldColor));
		add(pnlOptions, "cell 0 1,grow");
		pnlOptions.setLayout(new MigLayout("", "[grow]", "[50][50][50][50][50]"));

		chckbxStartDate = new JCheckBox("Start Date");
		chckbxStartDate.setForeground(_Settings.labelColor);
		chckbxStartDate.setBackground(_Settings.backgroundColor);
		chckbxStartDate.setIconTextGap(10);
		chckbxStartDate.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxStartDate.setFont(new Font("Arial Black", Font.BOLD, 17));
		chckbxStartDate.setBorder(null);
		pnlOptions.add(chckbxStartDate, "cell 0 0,alignx left,growy");
		checkboxes.add(chckbxStartDate);

		chckbxLastSaleDate = new JCheckBox("Last Sale Date");
		chckbxLastSaleDate.setIconTextGap(10);
		chckbxLastSaleDate.setForeground(_Settings.labelColor);
		chckbxLastSaleDate.setBackground(_Settings.backgroundColor);
		chckbxLastSaleDate.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxLastSaleDate.setFont(new Font("Arial Black", Font.BOLD, 17));
		chckbxLastSaleDate.setBorder(null);
		pnlOptions.add(chckbxLastSaleDate, "cell 0 1,alignx left,growy");
		checkboxes.add(chckbxLastSaleDate);

		chckbxTotalSale = new JCheckBox("Total Sale");
		chckbxTotalSale.setIconTextGap(10);
		chckbxTotalSale.setForeground(_Settings.labelColor);
		chckbxTotalSale.setBackground(_Settings.backgroundColor);
		chckbxTotalSale.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxTotalSale.setFont(new Font("Arial Black", Font.BOLD, 17));
		chckbxTotalSale.setBorder(null);
		pnlOptions.add(chckbxTotalSale, "cell 0 2,alignx left,growy");
		checkboxes.add(chckbxTotalSale);

		chckbxTotalPaid = new JCheckBox("Total Paid");
		chckbxTotalPaid.setIconTextGap(10);
		chckbxTotalPaid.setForeground(_Settings.labelColor);
		chckbxTotalPaid.setBackground(_Settings.backgroundColor);
		chckbxTotalPaid.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxTotalPaid.setFont(new Font("Arial Black", Font.BOLD, 17));
		chckbxTotalPaid.setBorder(null);
		pnlOptions.add(chckbxTotalPaid, "cell 0 3,alignx left,growy");
		checkboxes.add(chckbxTotalPaid);

		chckbxOutstandingBalance = new JCheckBox("Outstanding Balance");
		chckbxOutstandingBalance.setIconTextGap(10);
		chckbxOutstandingBalance.setForeground(_Settings.labelColor);
		chckbxOutstandingBalance.setBackground(_Settings.backgroundColor);
		chckbxOutstandingBalance.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxOutstandingBalance.setFont(new Font("Arial Black", Font.BOLD, 17));
		chckbxOutstandingBalance.setBorder(null);
		checkboxes.add(chckbxOutstandingBalance);
		pnlOptions.add(chckbxOutstandingBalance, "cell 0 4,alignx left,growy");

	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return !txtTitle.getText().isEmpty();
	}

	@Override
	public void generateReport() {
		// TODO Auto-generated method stub
		ReportFacade.generateSalesReport5(txtTitle.getText().trim(), checkboxes);
	}
}
