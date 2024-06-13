package frames;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import cores.Form;

public class MyTableModel extends AbstractTableModel {

	/**
	* 
	*/
	private static final long serialVersionUID = 1L;
	List<Form> forms;

	public MyTableModel(List<Form> forms) {
		this.forms = forms;
	}

	public Class<Form> getColumnClass(int columnIndex) {
		return Form.class;
	}

	public int getColumnCount() {
		return 1;
	}

	public int getRowCount() {
		return (forms == null) ? 0 : forms.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		return (forms == null) ? null : forms.get(rowIndex).getTitle();
	}
	
	public Object getValueAt(int rowIndex) {
		return getValueAt(rowIndex, 0);
	}

	public boolean isCellEditable(int columnIndex, int rowIndex) {
		return false;
	}
}