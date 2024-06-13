package cards;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cores.Inventory;
import databases.Inventories;
import globals.DatabaseFacade;
import globals.ValueFormatter;
import globals._Settings;
import net.miginfocom.swing.MigLayout;

public class Settings extends JTabbedPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JComboBox<String> cbInventory;
	private boolean isListenerEnabled;
	private JButton btnReset;
	private JLabel lblSample;
	private JTextField txtSample;
	private JPanel pSample;
	private JColorChooser ccBackground, ccLabel, ccTextField;

	private void resetColors() {
		_Settings.backgroundColorT = _Settings.backgroundColor;
		_Settings.labelColorT = _Settings.labelColor;
		_Settings.textFieldColorT = _Settings.textFieldColor;
		ccBackground.setColor(_Settings.backgroundColorT);
		ccLabel.setColor(_Settings.labelColorT);
		ccTextField.setColor(_Settings.textFieldColorT);
	}

	public void updateOptions() {
		isListenerEnabled = false;
		cbInventory.removeAllItems();
		Inventories db = (Inventories) DatabaseFacade.getDatabase("Inventories");
		List<Inventory> inventories = db.getList();
		cbInventory.addItem(null);
		for (Inventory inventory : inventories)
			cbInventory.addItem(ValueFormatter.formatInventory(inventory));
		cbInventory.setSelectedItem(_Settings.defaultInventory);
		isListenerEnabled = true;
	}

	/**
	 * Create the panel.
	 */
	public Settings() {
		UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
		UIManager.getDefaults().put("TabbedPane.tabsOverlapBorder", false);

		setBackground(_Settings.backgroundColor);
		setForeground(_Settings.labelColor);
		setBorder(null);
		setFont(new Font("Arial", Font.BOLD, 17));

		JPanel tab1 = new JPanel();
		tab1.setBackground(_Settings.backgroundColor);
		tab1.setLayout(new MigLayout("", "[10][150][10][450,grow][80][grow]", "[10][50][10][50][10][50][10][50][grow]"));
		tab1.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		add("General", tab1);

		JLabel lblDatabases = new JLabel("Databases:");
		lblDatabases.setBorder(null);
		lblDatabases.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDatabases.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblDatabases.setForeground(_Settings.labelColor);
		tab1.add(lblDatabases, "cell 1 1,grow");

		JTextField txtDatabases = new JTextField(_Settings.directory);
		txtDatabases.setBackground(_Settings.backgroundColor);
		txtDatabases.setForeground(_Settings.textFieldColor);
		txtDatabases.setHorizontalAlignment(SwingConstants.LEFT);
		txtDatabases.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtDatabases.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtDatabases.setEditable(false);
		tab1.add(txtDatabases, "cell 3 1,grow");

		JButton btnDatabases = new JButton("...");
		btnDatabases.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnDatabases.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnDatabases.setBackground(_Settings.backgroundColor);
		btnDatabases.setForeground(_Settings.labelColor);
		btnDatabases.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int choice = fileChooser.showOpenDialog(null);
				if (choice == JFileChooser.APPROVE_OPTION) {
					_Settings.directory = fileChooser.getSelectedFile().getAbsolutePath();
					txtDatabases.setText(_Settings.directory);
				}
			}
		});
		tab1.add(btnDatabases, "cell 4 1,growx,aligny center");

		JLabel lblBackup = new JLabel("Backup:");
		lblBackup.setBorder(null);
		lblBackup.setHorizontalAlignment(SwingConstants.RIGHT);
		lblBackup.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblBackup.setForeground(_Settings.labelColor);
		tab1.add(lblBackup, "cell 1 3,grow");

		JTextField txtBackup = new JTextField();
		txtBackup.setBackground(_Settings.backgroundColor);
		txtBackup.setForeground(_Settings.textFieldColor);
		txtBackup.setHorizontalAlignment(SwingConstants.LEFT);
		txtBackup.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtBackup.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtBackup.setEditable(false);
		tab1.add(txtBackup, "cell 3 3,grow");

		JButton btnBackup = new JButton("...");
		btnBackup.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnBackup.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnBackup.setBackground(_Settings.backgroundColor);
		btnBackup.setForeground(_Settings.labelColor);
		btnBackup.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int choice = fileChooser.showOpenDialog(null);
				if (choice == JFileChooser.APPROVE_OPTION) {
					String dir = fileChooser.getSelectedFile().getAbsolutePath(); // MAKE THIS PROPER BACKUP DIR
					txtBackup.setText(dir);
				}
			}
		});
		tab1.add(btnBackup, "cell 4 3,growx");

		JCheckBox cbManufacturing = new JCheckBox("Enable Manufacturing   ");
		cbManufacturing.setSelected(_Settings.enableManufacturing);
		cbManufacturing.setHorizontalTextPosition(SwingConstants.LEFT);
		cbManufacturing.setBorder(null);
		cbManufacturing.setHorizontalAlignment(SwingConstants.LEFT);
		cbManufacturing.setFont(new Font("Arial Black", Font.PLAIN, 17));
		cbManufacturing.setForeground(_Settings.labelColor);
		cbManufacturing.setBackground(_Settings.backgroundColor);
		cbManufacturing.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				_Settings.enableManufacturing = cbManufacturing.isSelected();
			}
		});
		tab1.add(cbManufacturing, "cell 1 5 3 1,grow");
		
		JCheckBox cbAutoVatCalculation = new JCheckBox("Enable Auto VAT Calculation   ");
		cbAutoVatCalculation.setSelected(_Settings.enableAutoVatCalculation);
		cbAutoVatCalculation.setHorizontalTextPosition(SwingConstants.LEFT);
		cbAutoVatCalculation.setBorder(null);
		cbAutoVatCalculation.setHorizontalAlignment(SwingConstants.LEFT);
		cbAutoVatCalculation.setFont(new Font("Arial Black", Font.PLAIN, 17));
		cbAutoVatCalculation.setForeground(_Settings.labelColor);
		cbAutoVatCalculation.setBackground(_Settings.backgroundColor);
		cbAutoVatCalculation.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				_Settings.enableAutoVatCalculation = cbAutoVatCalculation.isSelected();
			}
		});
		tab1.add(cbAutoVatCalculation, "cell 1 7 3 1,grow");

		JPanel tab2 = new JPanel();
		tab2.setBackground(_Settings.backgroundColor);
		tab2.setLayout(new MigLayout("", "[10][150][10][450,grow][grow]", "[10][50][10][50][grow]"));
		tab2.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		add("Personal", tab2);

		JLabel lblUsername = new JLabel("Username:");
		lblUsername.setBorder(null);
		lblUsername.setHorizontalAlignment(SwingConstants.RIGHT);
		lblUsername.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblUsername.setForeground(_Settings.labelColor);
		tab2.add(lblUsername, "cell 1 1,grow");

		JTextField txtUsername = new JTextField(_Settings.userName);
		txtUsername.setBackground(_Settings.backgroundColor);
		txtUsername.setForeground(_Settings.textFieldColor);
		txtUsername.setHorizontalAlignment(SwingConstants.CENTER);
		txtUsername.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtUsername.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtUsername.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {
				// TODO Auto-generated method stub
				_Settings.userName = txtUsername.getText();
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				// TODO Auto-generated method stub
				_Settings.userName = txtUsername.getText();
			}
		});
		tab2.add(txtUsername, "cell 3 1,grow");

		JLabel lblInventory = new JLabel("Default Inventory:");
		lblInventory.setBorder(null);
		lblInventory.setHorizontalAlignment(SwingConstants.RIGHT);
		lblInventory.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblInventory.setForeground(_Settings.labelColor);
		tab2.add(lblInventory, "cell 1 3,grow");

		cbInventory = new JComboBox<String>();
		cbInventory.setFont(new Font("Century Gothic", Font.BOLD, 17));
		cbInventory.setForeground(Color.DARK_GRAY);
		cbInventory.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if (isListenerEnabled)
					if (e.getStateChange() == ItemEvent.SELECTED)
						_Settings.defaultInventory = (String) cbInventory.getSelectedItem();
					else if (cbInventory.getSelectedItem() == null)
						_Settings.defaultInventory = null;
			}
		});
		Inventories db = (Inventories) DatabaseFacade.getDatabase("Inventories");
		List<Inventory> inventories = db.getList();
		cbInventory.addItem(null);
		for (Inventory inventory : inventories)
			cbInventory.addItem(ValueFormatter.formatInventory(inventory));
		cbInventory.setSelectedItem(_Settings.defaultInventory);
		if (((String) cbInventory.getSelectedItem()) == null)
			_Settings.defaultInventory = null;
		tab2.add(cbInventory, "cell 3 3,grow");
		isListenerEnabled = true;

		JPanel tab3 = new JPanel();
		tab3.setBackground(_Settings.backgroundColor);
		add("Color", tab3);
		tab3.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		tab3.setLayout(new MigLayout("", "[10][150][10][300][10][grow][10]", "[10][50][10][50][10][50][10][grow][10]"));

		pSample = new JPanel();
		pSample.setBackground(_Settings.backgroundColor);
		tab3.add(pSample, "cell 1 7 3 1,grow");
		pSample.setLayout(new MigLayout("", "[150][10][150][grow][150][10]", "[50][grow]"));

		lblSample = new JLabel("Sample:");
		lblSample.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSample.setForeground(_Settings.labelColor);
		lblSample.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblSample.setBorder(null);
		pSample.add(lblSample, "cell 0 0,alignx right,aligny center");

		txtSample = new JTextField("samples");
		txtSample.setHorizontalAlignment(SwingConstants.LEFT);
		txtSample.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtSample.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtSample.setBackground(null);
		txtSample.setForeground(_Settings.textFieldColor);
		txtSample.setColumns(10);
		pSample.add(txtSample, "cell 2 0,alignx left,aligny center");

		btnReset = new JButton("RESET");
		btnReset.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnReset.setFont(new Font("Arial Black", Font.BOLD, 14));
		btnReset.setBackground(_Settings.backgroundColor);
		btnReset.setForeground(_Settings.labelColor);
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetColors();
			}
		});
		pSample.add(btnReset, "cell 4 0,grow");

		JLabel lblBackground = new JLabel("Background:");
		lblBackground.setHorizontalAlignment(SwingConstants.RIGHT);
		lblBackground.setForeground(_Settings.labelColor);
		lblBackground.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblBackground.setBorder(null);
		tab3.add(lblBackground, "cell 1 1,grow");

		ccBackground = new JColorChooser(_Settings.backgroundColor);
		ccBackground.setBackground(_Settings.backgroundColor);
		ccBackground.setPreviewPanel(new JPanel());
		for (AbstractColorChooserPanel panel : ccBackground.getChooserPanels())
			if (!panel.getDisplayName().equals("HSV"))
				ccBackground.removeChooserPanel(panel);
			else
				panel.setBackground(_Settings.backgroundColor);
		ccBackground.getSelectionModel().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				_Settings.backgroundColorT = ccBackground.getColor();
				pSample.setBackground(_Settings.backgroundColorT);
				btnReset.setBackground(_Settings.backgroundColorT);
			}
		});
		tab3.add(ccBackground, "cell 3 1,grow");

		JLabel lblLabel = new JLabel("Label:");
		lblLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lblLabel.setForeground(_Settings.labelColor);
		lblLabel.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblLabel.setBorder(null);
		tab3.add(lblLabel, "cell 1 3,grow");

		ccLabel = new JColorChooser(_Settings.labelColor);
		ccLabel.setBackground(_Settings.backgroundColor);
		ccLabel.setPreviewPanel(new JPanel());
		for (AbstractColorChooserPanel panel : ccLabel.getChooserPanels())
			if (!panel.getDisplayName().equals("HSV"))
				ccLabel.removeChooserPanel(panel);
			else
				panel.setBackground(_Settings.backgroundColor);
		ccLabel.getSelectionModel().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				_Settings.labelColorT = ccLabel.getColor();
				lblSample.setForeground(_Settings.labelColorT);
				btnReset.setForeground(_Settings.labelColorT);
			}
		});
		tab3.add(ccLabel, "cell 3 3,grow");

		JLabel lblTextField = new JLabel("Text Field:");
		lblTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTextField.setForeground(_Settings.labelColor);
		lblTextField.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblTextField.setBorder(null);
		tab3.add(lblTextField, "cell 1 5,grow");

		ccTextField = new JColorChooser(_Settings.textFieldColor);
		ccTextField.setBackground(_Settings.backgroundColor);
		ccTextField.setPreviewPanel(new JPanel());
		for (AbstractColorChooserPanel panel : ccTextField.getChooserPanels())
			if (!panel.getDisplayName().equals("HSV"))
				ccTextField.removeChooserPanel(panel);
			else
				panel.setBackground(_Settings.backgroundColor);
		ccTextField.getSelectionModel().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				_Settings.textFieldColorT = ccTextField.getColor();
				txtSample.setForeground(_Settings.textFieldColorT);
			}
		});
		tab3.add(ccTextField, "cell 3 5,grow");

		UIManager.put("TabbedPane.background", _Settings.backgroundColor);
		UIManager.put("TabbedPane.foreground", _Settings.labelColor);
		UIManager.put("TabbedPane.opaque", true);
		UIManager.put("TabbedPane.selected", _Settings.backgroundColor);

		setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
			@Override
			protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
				return 40;
			}

			@Override
			protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect,
					Rectangle textRect) {
				rects[tabIndex].height = 45;
				rects[tabIndex].y = 0;
				super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
			}
		});
	}
}
