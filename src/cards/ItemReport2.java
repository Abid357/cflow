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

public class ItemReport2 extends JPanel implements IReport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txtTitle;
	private JCheckBox chckbxProduct, chckbxMaterial, chckbxService;
	private List<JCheckBox> checkboxes;
	private JCheckBox chckbxPrice;
	private JPanel pnlTitle;
	private JPanel pnlOptions;
	private JPanel pnlOptions2;

	/**
	 * Create the panel.
	 */
	public ItemReport2() {
		setBackground(_Settings.backgroundColor);
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setLayout(new MigLayout("", "[]", "[][][]"));

		checkboxes = new ArrayList<JCheckBox>();

		pnlTitle = new JPanel();
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

		pnlOptions = new JPanel();
		pnlOptions.setForeground(_Settings.labelColor);
		pnlOptions.setBackground(_Settings.backgroundColor);
		pnlOptions.setBorder(new TitledBorder(null, "Select one or more item types", TitledBorder.LEADING,
				TitledBorder.TOP, null, _Settings.textFieldColor));
		add(pnlOptions, "cell 0 1,grow");
		pnlOptions.setLayout(new MigLayout("", "[grow]", "[50,fill][50][50]"));

		chckbxProduct = new JCheckBox("Product");
		pnlOptions.add(chckbxProduct, "cell 0 0,alignx left,growy");
		chckbxProduct.setIconTextGap(10);
		chckbxProduct.setBackground(_Settings.backgroundColor);
		chckbxProduct.setForeground(_Settings.labelColor);
		chckbxProduct.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxProduct.setFont(new Font("Arial Black", Font.BOLD, 17));
		chckbxProduct.setBorder(null);
		checkboxes.add(chckbxProduct);

		chckbxMaterial = new JCheckBox("Material");
		pnlOptions.add(chckbxMaterial, "cell 0 1,alignx left,growy");
		chckbxMaterial.setIconTextGap(10);
		chckbxMaterial.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxMaterial.setBackground(_Settings.backgroundColor);
		chckbxMaterial.setForeground(_Settings.labelColor);
		chckbxMaterial.setFont(new Font("Arial Black", Font.BOLD, 17));
		chckbxMaterial.setBorder(null);
		checkboxes.add(chckbxMaterial);

		chckbxService = new JCheckBox("Service");
		pnlOptions.add(chckbxService, "cell 0 2,alignx left,growy");
		chckbxService.setIconTextGap(10);
		chckbxService.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxService.setFont(new Font("Arial Black", Font.BOLD, 17));
		chckbxService.setBorder(null);
		chckbxService.setBackground(_Settings.backgroundColor);
		chckbxService.setForeground(_Settings.labelColor);
		checkboxes.add(chckbxService);

		pnlOptions2 = new JPanel();
		pnlOptions2.setForeground(_Settings.labelColor);
		pnlOptions2.setBackground(_Settings.backgroundColor);
		pnlOptions2.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Select additional features (optional)",
				TitledBorder.LEADING, TitledBorder.TOP, null, _Settings.textFieldColor));
		add(pnlOptions2, "cell 0 2,grow");
		pnlOptions2.setLayout(new MigLayout("", "[grow]", "[50]"));

		chckbxPrice = new JCheckBox("Include unit price");
		pnlOptions2.add(chckbxPrice, "cell 0 0,alignx left,growy");
		chckbxPrice.setIconTextGap(10);
		chckbxPrice.setBackground(_Settings.backgroundColor);
		chckbxPrice.setForeground(_Settings.labelColor);
		chckbxPrice.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxPrice.setFont(new Font("Arial Black", Font.BOLD, 17));
		chckbxPrice.setBorder(null);
		checkboxes.add(chckbxPrice);
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return !txtTitle.getText().isEmpty()
				&& (chckbxProduct.isSelected() || chckbxMaterial.isSelected() || chckbxService.isSelected());
	}

	@Override
	public void generateReport() {
		// TODO Auto-generated method stub
		ReportFacade.generateItemReport2(txtTitle.getText().trim(), checkboxes);
	}
}