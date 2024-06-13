package cards;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import globals.ReportFacade;
import globals._Settings;
import net.miginfocom.swing.MigLayout;

public class InventoryReport1 extends JPanel implements IReport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txtTitle;
	private JCheckBox chckbxBank, chckbxPersonal, chckbxStore;
	private List<JCheckBox> checkboxes;
	private JCheckBox chckbxBalance;
	private JLabel lblLiquidAssets;
	private JLabel lblSolidAssets;
	private JCheckBox chckbxCredit;
	private JCheckBox chckbxDebit;
	private JPanel pnlTitle;
	private JPanel pnlAsset;
	private JPanel pnlOptions;

	/**
	 * Create the panel.
	 */
	public InventoryReport1() {
		setBackground(_Settings.backgroundColor);
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setLayout(new MigLayout("", "[]", "[][][]"));

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

		checkboxes = new ArrayList<JCheckBox>();

		pnlAsset = new JPanel();
		pnlAsset.setBackground(_Settings.backgroundColor);
		pnlAsset.setForeground(_Settings.labelColor);
		pnlAsset.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),
				"Select one or more asset types", TitledBorder.LEADING, TitledBorder.TOP, null, _Settings.textFieldColor));
		add(pnlAsset, "cell 0 1,grow");
		pnlAsset.setLayout(new MigLayout("", "[grow]", "[20][50][50][20][50]"));

		lblLiquidAssets = new JLabel("Liquid Assets");
		pnlAsset.add(lblLiquidAssets, "cell 0 0,alignx center,growy");
		lblLiquidAssets.setBackground(_Settings.backgroundColor);
		lblLiquidAssets.setForeground(_Settings.textFieldColor);
		lblLiquidAssets.setHorizontalAlignment(SwingConstants.RIGHT);
		lblLiquidAssets.setFont(new Font("Century Gothic", Font.PLAIN, 18));
		lblLiquidAssets.setBorder(null);

		chckbxBank = new JCheckBox("Bank");
		pnlAsset.add(chckbxBank, "cell 0 1,alignx left,growy");
		chckbxBank.setIconTextGap(10);
		chckbxBank.setBackground(_Settings.backgroundColor);
		chckbxBank.setForeground(_Settings.labelColor);
		chckbxBank.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxBank.setFont(new Font("Arial Black", Font.BOLD, 17));
		chckbxBank.setBorder(null);
		checkboxes.add(chckbxBank);

		chckbxPersonal = new JCheckBox("Personal");
		pnlAsset.add(chckbxPersonal, "cell 0 2,alignx left,growy");
		chckbxPersonal.setIconTextGap(10);
		chckbxPersonal.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxPersonal.setBackground(_Settings.backgroundColor);
		chckbxPersonal.setForeground(_Settings.labelColor);
		chckbxPersonal.setFont(new Font("Arial Black", Font.BOLD, 17));
		chckbxPersonal.setBorder(null);
		checkboxes.add(chckbxPersonal);

		lblSolidAssets = new JLabel("Solid Assets");
		pnlAsset.add(lblSolidAssets, "cell 0 3,alignx center,growy");
		lblSolidAssets.setBackground(_Settings.backgroundColor);
		lblSolidAssets.setForeground(_Settings.textFieldColor);
		lblSolidAssets.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSolidAssets.setFont(new Font("Century Gothic", Font.PLAIN, 18));
		lblSolidAssets.setBorder(null);

		chckbxStore = new JCheckBox("Store");
		pnlAsset.add(chckbxStore, "cell 0 4,alignx left,growy");
		chckbxStore.setIconTextGap(10);
		chckbxStore.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxStore.setFont(new Font("Arial Black", Font.BOLD, 17));
		chckbxStore.setBorder(null);
		chckbxStore.setBackground(_Settings.backgroundColor);
		chckbxStore.setForeground(_Settings.labelColor);
		checkboxes.add(chckbxStore);

		pnlOptions = new JPanel();
		pnlOptions.setBackground(_Settings.backgroundColor);
		pnlOptions.setForeground(_Settings.labelColor);
		pnlOptions.setBorder(
				new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Select additional features (optional)",
						TitledBorder.LEADING, TitledBorder.TOP, null, _Settings.textFieldColor));
		add(pnlOptions, "cell 0 2,grow");
		pnlOptions.setLayout(new MigLayout("", "[grow]", "[50][50][50]"));

		chckbxBalance = new JCheckBox("Include current balance");
		pnlOptions.add(chckbxBalance, "cell 0 0,alignx left,growy");
		chckbxBalance.setIconTextGap(10);
		chckbxBalance.setBackground(_Settings.backgroundColor);
		chckbxBalance.setForeground(_Settings.labelColor);
		chckbxBalance.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxBalance.setFont(new Font("Arial Black", Font.BOLD, 17));
		chckbxBalance.setBorder(null);
		checkboxes.add(chckbxBalance);

		chckbxCredit = new JCheckBox("Include net credit");
		pnlOptions.add(chckbxCredit, "cell 0 1,alignx left,growy");
		chckbxCredit.setIconTextGap(10);
		chckbxCredit.setBackground(_Settings.backgroundColor);
		chckbxCredit.setForeground(_Settings.labelColor);
		chckbxCredit.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxCredit.setFont(new Font("Arial Black", Font.BOLD, 17));
		chckbxCredit.setBorder(null);
		checkboxes.add(chckbxCredit);

		chckbxDebit = new JCheckBox("Include net debit");
		pnlOptions.add(chckbxDebit, "cell 0 2,alignx left,growy");
		chckbxDebit.setIconTextGap(10);
		chckbxDebit.setBackground(_Settings.backgroundColor);
		chckbxDebit.setForeground(_Settings.labelColor);
		chckbxDebit.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxDebit.setFont(new Font("Arial Black", Font.BOLD, 17));
		chckbxDebit.setBorder(null);
		checkboxes.add(chckbxDebit);
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return !txtTitle.getText().isEmpty()
				&& (chckbxBank.isSelected() || chckbxPersonal.isSelected() || chckbxStore.isSelected());
	}

	@Override
	public void generateReport() {
		// TODO Auto-generated method stub
		ReportFacade.generateInventoryReport1(txtTitle.getText().trim(), checkboxes);
	}
}