package cards;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import globals.ReportFacade;
import globals.ValueFormatter;
import globals._Settings;
import net.miginfocom.swing.MigLayout;

public class SalesReport2 extends JPanel implements IReport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txtTitle;
	private Date toDate, fromDate;
	private JDateChooser dcFrom, dcTo;
	private JPanel btnPnl;
	private JPanel pnlDates;

	/**
	 * Create the panel.
	 */
	public SalesReport2() {
		setBackground(_Settings.backgroundColor);
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setLayout(new MigLayout("", "[]", "[][]"));

		btnPnl = new JPanel();
		btnPnl.setForeground(_Settings.labelColor);
		btnPnl.setBackground(_Settings.backgroundColor);
		btnPnl.setBorder(new TitledBorder(null, "Enter a title", TitledBorder.LEADING, TitledBorder.TOP, null,
				_Settings.textFieldColor));
		add(btnPnl, "cell 0 0,grow");
		btnPnl.setLayout(new MigLayout("", "[75][10][275]", "[50]"));

		JLabel lblTitle = new JLabel("Title:");
		btnPnl.add(lblTitle, "cell 0 0,alignx right,growy");
		lblTitle.setBackground(_Settings.backgroundColor);
		lblTitle.setForeground(_Settings.labelColor);
		lblTitle.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTitle.setFont(new Font("Arial Black", Font.BOLD, 17));
		lblTitle.setBorder(null);

		txtTitle = new JTextField();
		btnPnl.add(txtTitle, "cell 2 0,grow");
		txtTitle.setBackground(_Settings.backgroundColor);
		txtTitle.setForeground(_Settings.textFieldColor);
		txtTitle.setDisabledTextColor(_Settings.textFieldColor);
		txtTitle.setHorizontalAlignment(SwingConstants.CENTER);
		txtTitle.setFont(new Font("Century Gothic", Font.BOLD, 17));
		txtTitle.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtTitle.setColumns(10);

		pnlDates = new JPanel();
		pnlDates.setForeground(_Settings.labelColor);
		pnlDates.setBackground(_Settings.backgroundColor);
		pnlDates.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Select date range",
				TitledBorder.LEADING, TitledBorder.TOP, null, _Settings.textFieldColor));
		add(pnlDates, "cell 0 1,grow");
		pnlDates.setLayout(new MigLayout("", "[75][10][275]", "[50][50]"));

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
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return !txtTitle.getText().isEmpty() && fromDate != null && toDate != null;
	}

	@Override
	public void generateReport() {
		// TODO Auto-generated method stub
		ReportFacade.generateSalesReport2(txtTitle.getText().trim(), fromDate, toDate);
	}
}
