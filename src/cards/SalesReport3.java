package cards;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import cores.SaleTransaction;
import cores.UserObject;
import databases.SaleTransactions;
import globals.DatabaseFacade;
import globals.ReportFacade;
import globals.ValueFormatter;
import globals._Settings;
import helpers.MyComboBoxRenderer;
import helpers.SortedComboBoxModel;
import net.miginfocom.swing.MigLayout;

public class SalesReport3 extends JPanel implements IReport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txtTitle;
	private Date toDate, fromDate;
	private JDateChooser dcFrom, dcTo;
	private JComboBox<String> cbCustomer;
	private UserObject customer;
	private JPanel pnlTitle;
	private JPanel pnlDates;
	private JPanel pnlCustomer;
	private JPanel pnlOptions;
	private JCheckBox chckbxIncludeQuantity;
	private JCheckBox chckbxIncludeUnit;
	private JCheckBox chckbxIncludeUnitPrice;
	private JCheckBox chckbxIncludeStore;
	private JCheckBox chckbxIncludeAmount;
	private List<JCheckBox> checkboxes;

	/**
	 * Create the panel.
	 */
	public SalesReport3() {
		setBackground(_Settings.backgroundColor);
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setLayout(new MigLayout("", "[]", "[][][][]"));

		checkboxes = new ArrayList<JCheckBox>();

		pnlTitle = new JPanel();
		pnlTitle.setBackground(_Settings.backgroundColor);
		pnlTitle.setForeground(_Settings.labelColor);
		pnlTitle.setBorder(new TitledBorder(null, "Enter a title", TitledBorder.LEADING, TitledBorder.TOP, null,
				_Settings.textFieldColor));
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

		pnlDates = new JPanel();
		pnlDates.setBackground(_Settings.backgroundColor);
		pnlDates.setForeground(_Settings.labelColor);
		pnlDates.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Select date range",
				TitledBorder.LEADING, TitledBorder.TOP, null, _Settings.textFieldColor));
		add(pnlDates, "cell 0 1,grow");
		pnlDates.setLayout(new MigLayout("", "[75,fill][10][275]", "[50][50]"));

		JLabel lblFrom = new JLabel("From:");
		pnlDates.add(lblFrom, "cell 0 0,alignx right,growy");
		lblFrom.setBackground(_Settings.backgroundColor);
		lblFrom.setForeground(_Settings.labelColor);
		lblFrom.setHorizontalAlignment(SwingConstants.RIGHT);
		lblFrom.setFont(new Font("Arial Black", Font.BOLD, 17));
		lblFrom.setBorder(null);

		dcFrom = new JDateChooser();
		pnlDates.add(dcFrom, "cell 2 0,grow");
		dcFrom.setFont(new Font("Century Gothic", Font.BOLD, 17));
		dcFrom.setBorder(null);
		dcFrom.setDateFormatString("dd-MMM-yyyy");

		JLabel lblTo = new JLabel("To:");
		pnlDates.add(lblTo, "cell 0 1,alignx right,growy");
		lblTo.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTo.setBackground(_Settings.backgroundColor);
		lblTo.setForeground(_Settings.labelColor);
		lblTo.setFont(new Font("Arial Black", Font.BOLD, 17));
		lblTo.setBorder(null);

		dcTo = new JDateChooser();
		pnlDates.add(dcTo, "cell 2 1,grow");
		dcTo.setFont(new Font("Century Gothic", Font.BOLD, 17));
		dcTo.setBorder(null);
		dcTo.setDateFormatString("dd-MMM-yyyy");
		JTextFieldDateEditor deTo = (JTextFieldDateEditor) dcTo.getComponent(1);
		dcTo.addPropertyChangeListener("date", new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent e) {
				// TODO Auto-generated method stub
				toDate = (Date) e.getNewValue();
				toDate = ValueFormatter.setTimeToZero(toDate);
				dcFrom.setMaxSelectableDate(toDate);
			}
		});
		JTextFieldDateEditor deFrom = (JTextFieldDateEditor) dcFrom.getComponent(1);
		dcFrom.addPropertyChangeListener("date", new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent e) {
				// TODO Auto-generated method stub
				fromDate = (Date) e.getNewValue();
				fromDate = ValueFormatter.setTimeToZero(fromDate);
				dcTo.setMinSelectableDate(fromDate);
			}
		});
		deFrom.setHorizontalAlignment(JTextField.CENTER);
		deFrom.setEnabled(false);
		deFrom.setDisabledTextColor(Color.DARK_GRAY);
		deTo.setHorizontalAlignment(JTextField.CENTER);
		deTo.setEnabled(false);
		deTo.setDisabledTextColor(Color.DARK_GRAY);

		pnlCustomer = new JPanel();
		pnlCustomer.setBackground(_Settings.backgroundColor);
		pnlCustomer.setForeground(_Settings.labelColor);
		pnlCustomer.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Select a customer",
				TitledBorder.LEADING, TitledBorder.TOP, null, _Settings.textFieldColor));
		add(pnlCustomer, "cell 0 2,grow");
		pnlCustomer.setLayout(new MigLayout("", "[110][10][240]", "[50]"));

		JLabel lblCustomer = new JLabel("Customer:");
		pnlCustomer.add(lblCustomer, "cell 0 0,alignx right,growy");
		lblCustomer.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCustomer.setBackground(_Settings.backgroundColor);
		lblCustomer.setForeground(_Settings.labelColor);
		lblCustomer.setFont(new Font("Arial Black", Font.BOLD, 17));
		lblCustomer.setBorder(null);

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

		cbCustomer = new JComboBox<String>(scbmCustomer);
		pnlCustomer.add(cbCustomer, "cell 2 0,grow");
		cbCustomer.setMaximumSize(new Dimension(240, 50));
		cbCustomer.setRenderer(mcbrCustomer);
		cbCustomer.setForeground(Color.DARK_GRAY);
		cbCustomer.setFont(new Font("Century Gothic", Font.BOLD, 17));
		cbCustomer.setBorder(null);
		cbCustomer.setSelectedIndex(-1);

		pnlOptions = new JPanel();
		pnlOptions.setForeground(_Settings.labelColor);
		pnlOptions.setBackground(_Settings.backgroundColor);
		pnlOptions.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Select additional features (optional)",
				TitledBorder.LEADING, TitledBorder.TOP, null, _Settings.textFieldColor));
		add(pnlOptions, "cell 0 3,grow");
		pnlOptions.setLayout(new MigLayout("", "[grow]", "[50][50][50][50][50]"));

		chckbxIncludeQuantity = new JCheckBox("Include Quantity");
		chckbxIncludeQuantity.setForeground(_Settings.labelColor);
		chckbxIncludeQuantity.setBackground(_Settings.backgroundColor);
		chckbxIncludeQuantity.setIconTextGap(10);
		chckbxIncludeQuantity.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxIncludeQuantity.setFont(new Font("Arial Black", Font.BOLD, 17));
		chckbxIncludeQuantity.setBorder(null);
		pnlOptions.add(chckbxIncludeQuantity, "cell 0 0,alignx left,growy");
		checkboxes.add(chckbxIncludeQuantity);

		chckbxIncludeUnit = new JCheckBox("Include Unit");
		chckbxIncludeUnit.setIconTextGap(10);
		chckbxIncludeUnit.setForeground(_Settings.labelColor);
		chckbxIncludeUnit.setBackground(_Settings.backgroundColor);
		chckbxIncludeUnit.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxIncludeUnit.setFont(new Font("Arial Black", Font.BOLD, 17));
		chckbxIncludeUnit.setBorder(null);
		pnlOptions.add(chckbxIncludeUnit, "cell 0 1,alignx left,growy");
		checkboxes.add(chckbxIncludeUnit);

		chckbxIncludeUnitPrice = new JCheckBox("Include Unit Price");
		chckbxIncludeUnitPrice.setIconTextGap(10);
		chckbxIncludeUnitPrice.setForeground(_Settings.labelColor);
		chckbxIncludeUnitPrice.setBackground(_Settings.backgroundColor);
		chckbxIncludeUnitPrice.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxIncludeUnitPrice.setFont(new Font("Arial Black", Font.BOLD, 17));
		chckbxIncludeUnitPrice.setBorder(null);
		pnlOptions.add(chckbxIncludeUnitPrice, "cell 0 2,alignx left,growy");
		checkboxes.add(chckbxIncludeUnitPrice);

		chckbxIncludeStore = new JCheckBox("Include Store");
		chckbxIncludeStore.setIconTextGap(10);
		chckbxIncludeStore.setForeground(_Settings.labelColor);
		chckbxIncludeStore.setBackground(_Settings.backgroundColor);
		chckbxIncludeStore.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxIncludeStore.setFont(new Font("Arial Black", Font.BOLD, 17));
		chckbxIncludeStore.setBorder(null);
		pnlOptions.add(chckbxIncludeStore, "cell 0 3,alignx left,growy");
		checkboxes.add(chckbxIncludeStore);

		chckbxIncludeAmount = new JCheckBox("Include Amount");
		chckbxIncludeAmount.setIconTextGap(10);
		chckbxIncludeAmount.setForeground(_Settings.labelColor);
		chckbxIncludeAmount.setBackground(_Settings.backgroundColor);
		chckbxIncludeAmount.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxIncludeAmount.setFont(new Font("Arial Black", Font.BOLD, 17));
		chckbxIncludeAmount.setBorder(null);
		checkboxes.add(chckbxIncludeAmount);
		pnlOptions.add(chckbxIncludeAmount, "cell 0 4,alignx left,growy");
		cbCustomer.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if ((e.getStateChange() == ItemEvent.SELECTED)) {
					customer = ValueFormatter.parseUserObject((String) cbCustomer.getSelectedItem());
				}
			}
		});

	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return !txtTitle.getText().isEmpty() && fromDate != null && toDate != null && customer != null;
	}

	@Override
	public void generateReport() {
		// TODO Auto-generated method stub
		ReportFacade.generateSalesReport3(txtTitle.getText().trim(), fromDate, toDate, customer, checkboxes);
	}
}
