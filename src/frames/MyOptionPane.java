package frames;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.border.BevelBorder;

import globals._Settings;

public class MyOptionPane extends JOptionPane {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String message;
		private int selection;
		public static final int ERROR_DIALOG_BOX = 0;
		public static final int CONFIRMATION_DIALOG_BOX = 1;
		public static final int OPTION_DIALOG_BOX = 2;

		public MyOptionPane(String message, int type) {
			this.message = message;
			if (type == ERROR_DIALOG_BOX)
				createErrorDialogBox();
			else if (type == OPTION_DIALOG_BOX)
				createOptionDialogBox();
			else if (type == CONFIRMATION_DIALOG_BOX)
				createConfirmationDialogBox();
		}

		public int getSelection() {
			return selection;
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

		private void createErrorDialogBox() {
			JOptionPane op = new JOptionPane(message, ERROR_MESSAGE);
			colorOptionPane(op);
			op.setBackground(_Settings.backgroundColor);
			op.setForeground(_Settings.labelColor);
			JDialog d = op.createDialog(null, "Error");
			d.setVisible(true);
		}

		private void createConfirmationDialogBox() {
			JOptionPane op = new JOptionPane(message, INFORMATION_MESSAGE);
			colorOptionPane(op);
			op.setBackground(_Settings.backgroundColor);
			op.setForeground(_Settings.labelColor);
			JDialog d = op.createDialog(null, "Success");
			d.setVisible(true);
		}

		private void createOptionDialogBox() {

		}
	}