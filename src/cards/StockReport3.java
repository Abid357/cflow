package cards;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import cores.Store;
import databases.Stores;
import globals.DatabaseFacade;
import globals.ReportFacade;
import globals.ValueFormatter;
import globals._Settings;
import helpers.MyComboBoxRenderer;
import helpers.SortedComboBoxModel;
import net.miginfocom.swing.MigLayout;

public class StockReport3 extends JPanel implements IReport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txtTitle;
	private JComboBox<String> cbStore;
	private Store store;

	/**
	 * Create the panel.
	 */
	public StockReport3() {
		setBackground(_Settings.backgroundColor);
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setLayout(new MigLayout("", "[]", "[][]"));

		JPanel pnlTitle = new JPanel();
		pnlTitle.setBackground(_Settings.backgroundColor);
		pnlTitle.setForeground(_Settings.labelColor);
		pnlTitle.setBorder(new TitledBorder(null, "Enter a title", TitledBorder.LEADING, TitledBorder.TOP, null,
				_Settings.textFieldColor));
		pnlTitle.setBackground((Color) null);
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

		JPanel pnlStore = new JPanel();
		pnlStore.setBackground(_Settings.backgroundColor);
		pnlStore.setForeground(_Settings.labelColor);
		pnlStore.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Select a store",
				TitledBorder.LEADING, TitledBorder.TOP, null, _Settings.textFieldColor));
		add(pnlStore, "cell 0 1,grow");
		pnlStore.setLayout(new MigLayout("", "[75][10][275]", "[50]"));

		Stores dbStores = (Stores) DatabaseFacade.getDatabase("Stores");
		List<Store> lstStores = dbStores.getList();

		MyComboBoxRenderer mcbrStore = new MyComboBoxRenderer();
		List<String> ttStore = new ArrayList<String>();
		SortedComboBoxModel<String> scbmStore = new SortedComboBoxModel<String>();

		for (Store store : lstStores) {
			String storeString = ValueFormatter.formatStore(store);
			scbmStore.addElement(storeString);
			ttStore.add(storeString);
		}
		mcbrStore.setTooltips(ttStore);

		JLabel lblStore = new JLabel("Store:");
		pnlStore.add(lblStore, "cell 0 0,alignx right,growy");
		lblStore.setHorizontalAlignment(SwingConstants.RIGHT);
		lblStore.setBackground(_Settings.backgroundColor);
		lblStore.setForeground(_Settings.labelColor);
		lblStore.setFont(new Font("Arial Black", Font.BOLD, 17));
		lblStore.setBorder(null);

		cbStore = new JComboBox<String>(scbmStore);
		pnlStore.add(cbStore, "cell 2 0,grow");
		cbStore.setMaximumSize(new Dimension(275, 50));
		cbStore.setRenderer(mcbrStore);
		cbStore.setForeground(Color.DARK_GRAY);
		cbStore.setFont(new Font("Century Gothic", Font.BOLD, 17));
		cbStore.setBorder(null);
		cbStore.setSelectedIndex(-1);
		cbStore.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if ((e.getStateChange() == ItemEvent.SELECTED)) {
					store = ValueFormatter.parseStore((String) cbStore.getSelectedItem());
				}
			}
		});
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return !txtTitle.getText().isEmpty() && store != null;
	}

	@Override
	public void generateReport() {
		// TODO Auto-generated method stub
		ReportFacade.generateStockReport3(txtTitle.getText().trim(), store);
	}
}
