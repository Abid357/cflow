package cards;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import cores.SaleTransaction;
import databases.SaleTransactions;
import globals.DatabaseFacade;
import globals.ReportFacade;
import globals.ValueFormatter;
import globals._Settings;
import helpers.SortedComboBoxModel;
import net.miginfocom.swing.MigLayout;

public class SalesReport4 extends JPanel implements IReport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txtTitle;
	private JComboBox<Integer> cbYear;

	/**
	 * Create the panel.
	 */
	public SalesReport4() {
		setBackground(_Settings.backgroundColor);
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setLayout(new MigLayout("", "[]", "[][]"));

		JPanel pnlTitle = new JPanel();
		pnlTitle.setForeground(_Settings.labelColor);
		pnlTitle.setBackground(_Settings.backgroundColor);
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

		JPanel pnlYear = new JPanel();
		pnlYear.setForeground(_Settings.labelColor);
		pnlYear.setBackground(_Settings.backgroundColor);
		pnlYear.setBorder(new TitledBorder(null, "Select a year", TitledBorder.LEADING, TitledBorder.TOP, null,
				_Settings.textFieldColor));
		add(pnlYear, "cell 0 1,grow");
		pnlYear.setLayout(new MigLayout("", "[75][10][275]", "[50]"));

		JLabel lblYear = new JLabel("Year:");
		pnlYear.add(lblYear, "cell 0 0,alignx right,growy");
		lblYear.setBackground(_Settings.backgroundColor);
		lblYear.setForeground(_Settings.labelColor);
		lblYear.setHorizontalAlignment(SwingConstants.RIGHT);
		lblYear.setFont(new Font("Arial Black", Font.BOLD, 17));
		lblYear.setBorder(null);
		
		SaleTransactions dbSales = (SaleTransactions) DatabaseFacade.getDatabase("SaleTransactions");
		List<SaleTransaction> lstSales = dbSales.getList();
		Collections.sort(lstSales, new Comparator<SaleTransaction>() {
			public int compare(SaleTransaction sale1, SaleTransaction sale2) {
				return sale1.getDate().compareTo(sale2.getDate());
			}
		});
		
		Calendar cal = Calendar.getInstance();
		int endYear = cal.get(Calendar.YEAR);
		int startYear = endYear;
		if (!lstSales.isEmpty()) {
			cal.setTime(lstSales.get(0).getDate());
			startYear = cal.get(Calendar.YEAR);
		}
		
		SortedComboBoxModel<Integer> scbmYear = new SortedComboBoxModel<>();
		for (int i = endYear; i >= startYear; i--) {
			scbmYear.addElement(i);
		}

		cbYear = new JComboBox<Integer>(scbmYear);
		pnlYear.add(cbYear, "cell 2 0,grow");
		cbYear.setMaximumSize(new Dimension(240, 50));
		cbYear.setForeground(Color.DARK_GRAY);
		cbYear.setFont(new Font("Century Gothic", Font.BOLD, 17));
		cbYear.setBorder(null);
		cbYear.setSelectedIndex(-1);
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return !txtTitle.getText().isEmpty() && cbYear.getSelectedIndex() != -1;
	}

	@Override
	public void generateReport() {
		// TODO Auto-generated method stub
		ReportFacade.generateSalesReport4(txtTitle.getText().trim(), (int)cbYear.getSelectedItem());
	}
}
