package frames;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import globals._Settings;

public class MyOptionPane extends JOptionPane {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message;
	private int selection;
	private String title;
	public static final int ERROR_DIALOG_BOX = 0;
	public static final int CONFIRMATION_DIALOG_BOX = 1;
	public static final int OPTION_DIALOG_BOX = 2;
	public static final int DATE_DIALOG_BOX = 3;
	public static final int POSITIVE = 4;
	public static final int NEGATIVE = 5;
	private Date date;
	
	public MyOptionPane(String message, int type, String title) {
		this.message = message;
		this.title = title;
		if (type == ERROR_DIALOG_BOX)
			createErrorDialogBox();
		else if (type == OPTION_DIALOG_BOX)
			createOptionDialogBox();
		else if (type == CONFIRMATION_DIALOG_BOX)
			createConfirmationDialogBox();
	}

	public MyOptionPane(String message, int type) {
		this(message, type,(String) null);
	}

	public MyOptionPane(String message, int type, Date minDate) {
		this.message = message;
		if (type == DATE_DIALOG_BOX)
			createDateDialogBox(minDate);
	}


	public int getSelection() {
		return selection;
	}
	
	public Date getDate() {
		return date;
	}

	private void colorOptionPane(Container c) {

		Component[] m = c.getComponents();

		for (int i = 0; i < m.length; i++) {

			if (m[i].getClass().getName() == "javax.swing.JPanel")
				m[i].setBackground(_Settings.backgroundColor);
			else if (m[i].getClass().getName() == "javax.swing.JLabel") {
				m[i].setForeground(_Settings.labelColor);
				m[i].setFont(new Font("Century Gothic", Font.BOLD, 15));
			} else if (m[i].getClass().getName() == "javax.swing.JButton") {
				m[i].setPreferredSize(new Dimension(150, 40));
				m[i].setBackground(_Settings.backgroundColor);
				m[i].setForeground(_Settings.labelColor);
				m[i].setFont(new Font("Arial Black", Font.PLAIN, 15));
				((JButton) m[i]).setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
			}

			if (c.getClass().isInstance(m[i]))
				;
			colorOptionPane((Container) m[i]);
		}
	}

	private void createDateDialogBox(Date minDate) {
		JDateChooser dateChooser = new JDateChooser();
		dateChooser.setPreferredSize(new Dimension(210, 30));
		dateChooser.setFont(new Font("Century Gothic", Font.BOLD, 15));
		dateChooser.setBorder(null);
		dateChooser.setDateFormatString("dd-MMM-yyyy");
		dateChooser.setMinSelectableDate(minDate);
		dateChooser.addPropertyChangeListener("date", new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent e) {
				// TODO Auto-generated method stub
				date = (Date) e.getNewValue();
			}
		});
		JTextFieldDateEditor dateEditor = (JTextFieldDateEditor) dateChooser.getComponent(1);
		dateEditor.setHorizontalAlignment(JTextField.CENTER);
		dateEditor.setEnabled(false);
		dateEditor.setDisabledTextColor(Color.DARK_GRAY);

		JLabel label = new JLabel("Select completion date:");
		label.setFont(new Font("Arial", Font.BOLD, 15));
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		label.setBorder(null);
		label.setBackground(null);
		label.setForeground(_Settings.labelColor);

		JPanel panel = new JPanel(new FlowLayout());
		panel.add(label);
		panel.add(dateChooser);

		Object[] params = { "Save", "Cancel" };
		JOptionPane op = new JOptionPane(panel, INFORMATION_MESSAGE, YES_NO_CANCEL_OPTION, null, params);
		colorOptionPane(op);
		op.setBackground(_Settings.backgroundColor);
		op.setForeground(_Settings.labelColor);

		String title = "Date Input";
		if (this.title != null && !this.title.isEmpty())
			title = this.title;
		JDialog d = op.createDialog(null,title);
		d.setVisible(true);
		if (op.getValue() == null || op.getValue().toString().equals("Cancel"))
			selection = NEGATIVE;
		else
			selection = POSITIVE;
	}

	private void createErrorDialogBox() {
		JOptionPane op = new JOptionPane(message, ERROR_MESSAGE);
		colorOptionPane(op);
		op.setBackground(_Settings.backgroundColor);
		op.setForeground(_Settings.labelColor);
		
		String title = "Error";
		if (this.title != null && !this.title.isEmpty())
			title = this.title;
		JDialog d = op.createDialog(null,title);
		d.setVisible(true);
	}

	private void createConfirmationDialogBox() {
		JOptionPane op = new JOptionPane(message, INFORMATION_MESSAGE);
		colorOptionPane(op);
		op.setBackground(_Settings.backgroundColor);
		op.setForeground(_Settings.labelColor);
		
		String title = "Success";
		if (this.title != null && !this.title.isEmpty())
			title = this.title;
		JDialog d = op.createDialog(null,title);
		d.setVisible(true);
	}

	private void createOptionDialogBox() {
		JOptionPane op = new JOptionPane(message, QUESTION_MESSAGE);
		op.setOptionType(JOptionPane.YES_NO_OPTION);
		colorOptionPane(op);
		op.setBackground(_Settings.backgroundColor);
		op.setForeground(_Settings.labelColor);

		String title = "Confirmation";
		if (this.title != null && !this.title.isEmpty())
			title = this.title;
		JDialog d = op.createDialog(null,title);
		d.setVisible(true);
		if (op.getValue() != null)
			selection = (int) op.getValue() + 4; // YES = 1, NO = 0 --> +4 to make it POSITIVE
		else
			selection = NEGATIVE;
	}
}