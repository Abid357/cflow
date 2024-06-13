package helpers;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import cores.Inventory;

public class MyTableModel extends AbstractTableModel {

	/**
	* 
	*/
	private static final long serialVersionUID = 1L;
	List<Inventory> inventories;

	public MyTableModel(List<Inventory> inventories) {
		this.inventories = inventories;
	}

	public Class<Inventory> getColumnClass(int columnIndex) {
		return Inventory.class;
	}

	public int getColumnCount() {
		return 1;
	}

	public int getRowCount() {
		return (inventories == null) ? 0 : inventories.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		return (inventories == null) ? null : inventories.get(rowIndex);
	}
	
	public Object getValueAt(int rowIndex) {
		return getValueAt(rowIndex, 0);
	}

	public boolean isCellEditable(int columnIndex, int rowIndex) {
		return false;
	}
}