package helpers;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import cores.Inventory;
import cores.Store;
import cores.StoreInventory;
import databases.Stores;
import globals.DatabaseFacade;
import globals.ValueFormatter;
import globals._Settings;
import net.miginfocom.swing.MigLayout;

public class InventoryCell extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txtBalance;

	/**
	 * Create the panel.
	 */
	public InventoryCell(Inventory inventory) {
		setLayout(new MigLayout("", "[500][grow][200]", "[30][40][40]"));
		setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		setBackground(_Settings.backgroundColor);

		String name = "";
		JLabel lblType = new JLabel();
		if (inventory instanceof StoreInventory) {
			Stores db = (Stores) DatabaseFacade.getDatabase("Stores");
			for (Store store : db.getList())
				if (store.getInventory().equals(inventory)) {
					name = store.getName() + " of " + inventory.getName().substring(
							inventory.getName().indexOf(store.getName() + "/") + store.getName().length() + 1);
					break;
				}
			if (name.length() >= 60)
				name = name.substring(0, 60) + "...";
			lblType.setText("Store");
		} else if (inventory.getAccountNo().equals("")) {
			lblType.setText("Personal");
			name = inventory.getName();
		} else {
			lblType.setText("Bank");
			name = inventory.getName();
		}

		lblType.setVerticalAlignment(SwingConstants.BOTTOM);
		lblType.setBorder(null);
		lblType.setHorizontalAlignment(SwingConstants.LEFT);
		lblType.setFont(new Font("Arial Black", Font.PLAIN, 13));
		lblType.setForeground(_Settings.labelColor);
		add(lblType, "cell 0 0,grow");

		JLabel lblName = new JLabel();
		lblName.setText(name);
		lblName.setHorizontalAlignment(SwingConstants.LEFT);
		lblName.setBorder(null);
		lblName.setFont(new Font("Century Gothic", Font.BOLD, 21));
		lblName.setBackground(_Settings.backgroundColor);
		lblName.setForeground(_Settings.labelColor);
		add(lblName, "cell 0 1,grow");

		txtBalance = new JTextField();
		txtBalance.setText(ValueFormatter.formatMoneyNicely(inventory.getBalance()));
		txtBalance.setHorizontalAlignment(SwingConstants.CENTER);
		txtBalance.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtBalance.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtBalance.setBackground(null);
		txtBalance.setForeground(_Settings.textFieldColor);
		txtBalance.setColumns(10);
		add(txtBalance, "flowx,cell 2 1 1 2,grow");
		txtBalance.setColumns(10);

		JLabel lblAccountNo = new JLabel();
		lblAccountNo.setText(inventory.getAccountNo());
		lblAccountNo.setHorizontalAlignment(SwingConstants.LEFT);
		lblAccountNo.setBorder(null);
		lblAccountNo.setFont(new Font("Century Gothic", Font.BOLD, 21));
		lblAccountNo.setBackground(_Settings.backgroundColor);
		lblAccountNo.setForeground(_Settings.labelColor);
		add(lblAccountNo, "cell 0 2,grow");

		JLabel lblAed = new JLabel("AED");
		lblAed.setBorder(null);
		lblAed.setHorizontalAlignment(SwingConstants.LEFT);
		lblAed.setFont(new Font("Arial Black", Font.PLAIN, 13));
		lblAed.setForeground(_Settings.labelColor);
		add(lblAed, "cell 2 1");

	}

}
