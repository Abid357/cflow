package cards;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import databases.UserCategories;
import globals.DatabaseFacade;
import globals.ReportFacade;
import globals._Settings;
import helpers.SortedComboBoxModel;
import net.miginfocom.swing.MigLayout;

public class UserReport2 extends JPanel implements IReport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txtTitle;
	private JCheckBox chckbxPerson, chckbxOrganization;
	private List<JCheckBox> checkboxes;
	private JCheckBox chckbxBalance;
	private JComboBox<String> cbCategory;
	private JLabel lblCategory;
	private JPanel pnlTitle;
	private JPanel pnlUser;
	private JPanel pnlCategory;
	private JPanel pnlOptions;

	/**
	 * Create the pnlOptions.
	 */
	public UserReport2() {
		setBackground(_Settings.backgroundColor);
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setLayout(new MigLayout("", "[]", "[][][][]"));

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

		pnlUser = new JPanel();
		pnlUser.setBackground(_Settings.backgroundColor);
		pnlUser.setForeground(_Settings.labelColor);
		pnlUser.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Select one or more user types",
				TitledBorder.LEADING, TitledBorder.TOP, null, _Settings.textFieldColor));
		add(pnlUser, "cell 0 1,grow");
		pnlUser.setLayout(new MigLayout("", "[]", "[50][50]"));

		chckbxPerson = new JCheckBox("Person");
		pnlUser.add(chckbxPerson, "cell 0 0,alignx left,growy");
		chckbxPerson.setIconTextGap(10);
		chckbxPerson.setBackground(_Settings.backgroundColor);
		chckbxPerson.setForeground(_Settings.labelColor);
		chckbxPerson.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxPerson.setFont(new Font("Arial Black", Font.BOLD, 17));
		chckbxPerson.setBorder(null);
		checkboxes.add(chckbxPerson);

		chckbxOrganization = new JCheckBox("Organization");
		pnlUser.add(chckbxOrganization, "cell 0 1,alignx left,growy");
		chckbxOrganization.setIconTextGap(10);
		chckbxOrganization.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxOrganization.setBackground(_Settings.backgroundColor);
		chckbxOrganization.setForeground(_Settings.labelColor);
		chckbxOrganization.setFont(new Font("Arial Black", Font.BOLD, 17));
		chckbxOrganization.setBorder(null);
		checkboxes.add(chckbxOrganization);

		pnlCategory = new JPanel();
		pnlCategory.setBackground(_Settings.backgroundColor);
		pnlCategory.setForeground(_Settings.labelColor);
		pnlCategory.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Select a user category",
				TitledBorder.LEADING, TitledBorder.TOP, null, _Settings.textFieldColor));
		add(pnlCategory, "cell 0 2,grow");
		pnlCategory.setLayout(new MigLayout("", "[75][10][275]", "[50]"));

		UserCategories db = (UserCategories) DatabaseFacade.getDatabase("UserCategories");
		List<String> categories = db.getList();

		SortedComboBoxModel<String> scbmCategory = new SortedComboBoxModel<String>();

		scbmCategory.addElement("ANY");
		for (String category : categories) {
			scbmCategory.addElement(category);
		}

		lblCategory = new JLabel("Category:");
		pnlCategory.add(lblCategory, "cell 0 0,alignx right,growy");
		lblCategory.setBackground(_Settings.backgroundColor);
		lblCategory.setForeground(_Settings.labelColor);
		lblCategory.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCategory.setFont(new Font("Arial Black", Font.BOLD, 17));
		lblCategory.setBorder(null);
		cbCategory = new JComboBox<String>(scbmCategory);
		pnlCategory.add(cbCategory, "cell 2 0,grow");
		cbCategory.setMaximumSize(new Dimension(275, 50));
		cbCategory.setForeground(Color.DARK_GRAY);
		cbCategory.setFont(new Font("Century Gothic", Font.BOLD, 17));
		cbCategory.setBorder(null);
		cbCategory.setSelectedIndex(0);

		pnlOptions = new JPanel();
		pnlOptions.setBackground(_Settings.backgroundColor);
		pnlOptions.setForeground(_Settings.labelColor);
		pnlOptions.setBorder(
				new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Select additional features (optional)",
						TitledBorder.LEADING, TitledBorder.TOP, null, _Settings.textFieldColor));
		add(pnlOptions, "cell 0 3,grow");
		pnlOptions.setLayout(new MigLayout("", "[]", "[50]"));

		chckbxBalance = new JCheckBox("Include balance");
		pnlOptions.add(chckbxBalance, "cell 0 0,alignx left,growy");
		chckbxBalance.setIconTextGap(10);
		chckbxBalance.setBackground(_Settings.backgroundColor);
		chckbxBalance.setForeground(_Settings.labelColor);
		chckbxBalance.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxBalance.setFont(new Font("Arial Black", Font.BOLD, 17));
		chckbxBalance.setBorder(null);
		checkboxes.add(chckbxBalance);
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return !txtTitle.getText().isEmpty() && (chckbxPerson.isSelected() || chckbxOrganization.isSelected());
	}

	@Override
	public void generateReport() {
		// TODO Auto-generated method stub
		ReportFacade.generateUserReport2(txtTitle.getText().trim(), checkboxes, cbCategory);
	}
}