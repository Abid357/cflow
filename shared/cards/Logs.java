package cards;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import frames.Main;
import globals.LOGGER;
import globals.ValueFormatter;
import globals._Settings;
import net.miginfocom.swing.MigLayout;

public class Logs extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String activityLog = null;
	private String errorLog = null;
	private JButton btnRefresh;
	private JTextArea textArea;
	
	public void refresh() {
		btnRefresh.doClick();
	}

	/**
	 * Create the panel.
	 */
	public Logs(Main frame) {
		setBackground(_Settings.backgroundColor);
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setLayout(new MigLayout("", "[150,fill][10][150][10][grow][150]", "[50][10][20][30:30][10][grow][10]"));

		JToggleButton tglbtnActivityLog = new JToggleButton("ACTIVITY LOG");
		tglbtnActivityLog.setMinimumSize(new Dimension(101, 50));
		tglbtnActivityLog.setSelected(true);
		tglbtnActivityLog.setForeground(_Settings.labelColor);
		tglbtnActivityLog.setFont(new Font("Arial", Font.BOLD, 14));
		tglbtnActivityLog.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		tglbtnActivityLog.setBackground(_Settings.backgroundColor);
		tglbtnActivityLog.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				textArea.setText(activityLog);
			}
		});
		add(tglbtnActivityLog, "cell 0 0,grow");

		JToggleButton tglbtnErrorLog = new JToggleButton("ERROR LOG");
		tglbtnErrorLog.setForeground(_Settings.labelColor);
		tglbtnErrorLog.setFont(new Font("Arial", Font.BOLD, 14));
		tglbtnErrorLog.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		tglbtnErrorLog.setBackground(_Settings.backgroundColor);
		tglbtnErrorLog.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				textArea.setText(errorLog);
			}
		});
		add(tglbtnErrorLog, "cell 2 0,grow");

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(tglbtnActivityLog);
		buttonGroup.add(tglbtnErrorLog);

		JLabel lblDate = new JLabel("Date");
		lblDate.setMinimumSize(new Dimension(23, 20));
		lblDate.setHorizontalAlignment(SwingConstants.CENTER);
		lblDate.setForeground(_Settings.labelColor);
		lblDate.setFont(new Font("Arial", Font.BOLD, 15));
		lblDate.setBorder(null);
		lblDate.setBackground(_Settings.backgroundColor);
		add(lblDate, "cell 0 2,grow");

		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, "cell 0 5 6 1,grow");

		textArea = new JTextArea();
		textArea.setMinimumSize(new Dimension(23, 23));
		textArea.setEditable(false);
		textArea.setFont(new Font("Arial", Font.PLAIN, 15));
		textArea.setBackground(_Settings.backgroundColor);
		textArea.setForeground(_Settings.textFieldColor);
		textArea.setBorder(null);
		scrollPane.setViewportView(textArea);

		JDateChooser dateChooser = new JDateChooser();
		dateChooser.setMinimumSize(new Dimension(27, 3));
		dateChooser.setFont(new Font("Century Gothic", Font.BOLD, 14));
		dateChooser.setBorder(null);
		dateChooser.setDateFormatString("dd-MMM-yyyy");
		JTextFieldDateEditor deDate = (JTextFieldDateEditor) dateChooser.getComponent(1);
		deDate.setHorizontalAlignment(JTextField.CENTER);
		deDate.setEnabled(false);
		deDate.setDisabledTextColor(Color.DARK_GRAY);
		deDate.addPropertyChangeListener("date", new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent e) {
				// TODO Auto-generated method stub
				Date date = (Date) e.getNewValue();
				if (date != null) {
					date = ValueFormatter.setTimeToZero(date);
					activityLog = LOGGER.Activity.getLogByDate(date);
					errorLog = LOGGER.Error.getLogByDate(date);
					if (tglbtnActivityLog.isSelected())
						textArea.setText(activityLog);
					else
						textArea.setText(errorLog);
				}
			}
		});
		add(dateChooser, "cell 0 3,grow");
		
		btnRefresh = new JButton("REFRESH");
		btnRefresh.setMinimumSize(new Dimension(77, 50));
		btnRefresh.setForeground(_Settings.labelColor);
		btnRefresh.setFont(new Font("Arial", Font.BOLD, 14));
		btnRefresh.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnRefresh.setBackground(_Settings.backgroundColor);
		btnRefresh.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				dateChooser.setDate(null);
				Date date = ValueFormatter.setTimeToZero(new Date());
				dateChooser.setDate(date);
			}
		});
		add(btnRefresh, "cell 5 2 1 2,grow");

	}
}
