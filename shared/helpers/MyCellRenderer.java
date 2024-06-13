package helpers;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import cores.Inventory;

public class MyCellRenderer implements TableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable arg0, Object arg1, boolean arg2, boolean arg3, int arg4,
			int arg5) {
		// TODO Auto-generated method stub
		Inventory inventory = (Inventory) arg1;
		return new InventoryCell(inventory);
	}

}
