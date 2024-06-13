package cards;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import globals.ReportFacade;
import globals._Settings;
import net.miginfocom.swing.MigLayout;

public class StockReport2 extends JPanel implements IReport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txtTitle;

	/**
	 * Create the panel.
	 */
	public StockReport2() {
		setBackground(_Settings.backgroundColor);
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setLayout(new MigLayout("", "[]", "[]"));

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
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return !txtTitle.getText().isEmpty();
	}

	@Override
	public void generateReport() {
		// TODO Auto-generated method stub
		ReportFacade.generateStockReport2(txtTitle.getText().trim());
	}
}
