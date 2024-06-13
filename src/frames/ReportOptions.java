package frames;

import java.awt.Dialog;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import globals._Settings;
import net.miginfocom.swing.MigLayout;

public class ReportOptions extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private List<JCheckBox> checkboxes;
	private List<JLabel> labels;
	private boolean isConfirmed;
	private JTextField txtTitle;
	private List<String> columns;

	public String getTitle() {
		return txtTitle.getText();
	}

	public boolean isConfirmed() {
		return isConfirmed;
	}

	public void setConfirmed(boolean isConfirmed) {
		this.isConfirmed = isConfirmed;
	}
	
	public List<String> getColumns(){
		return columns;
	}

	public List<JCheckBox> getList() {
		return checkboxes;
	}

	/**
	 * Create the frame.
	 */
	public ReportOptions(Window owner, String[] options) {
		super(owner, "Statement Options", Dialog.ModalityType.DOCUMENT_MODAL);
		setBounds(100, 100, 337, 333);
		setAutoRequestFocus(true);
		contentPane = new JPanel();
		contentPane.setBackground(_Settings.backgroundColor);
		contentPane.setBorder(null);
		setContentPane(contentPane);
		contentPane.setLayout(
				new MigLayout("", "[10][grow][10][140,grow][10][150][10]", "[10][50][10][50][10][grow][10][60][10]"));

		JLabel lblTitle = new JLabel("Title:");
		lblTitle.setHorizontalAlignment(SwingConstants.LEFT);
		lblTitle.setBorder(null);
		lblTitle.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblTitle.setForeground(_Settings.labelColor);
		lblTitle.setBackground(null);
		contentPane.add(lblTitle, "cell 1 1,grow");

		txtTitle = new JTextField();
		txtTitle.setHorizontalAlignment(SwingConstants.CENTER);
		txtTitle.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtTitle.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtTitle.setBackground(null);
		txtTitle.setForeground(_Settings.textFieldColor);
		txtTitle.setColumns(10);
		contentPane.add(txtTitle, "cell 3 1 3 1,grow");

		JLabel lblMessage = new JLabel("Select table columns for generating the report.");
		lblMessage.setHorizontalAlignment(SwingConstants.CENTER);
		lblMessage.setVerticalAlignment(SwingConstants.BOTTOM);
		lblMessage.setBorder(null);
		lblMessage.setFont(new Font("Arial", Font.ITALIC, 20));
		lblMessage.setBackground(null);
		lblMessage.setForeground(_Settings.labelColor);
		contentPane.add(lblMessage, "cell 1 3 5 1,grow");

		JPanel panel = new JPanel();
		contentPane.add(panel, "cell 1 5 5 2,grow");
		String rows = "";
		for (int i = 0; i < options.length; i++)
			rows += "[50]";
		panel.setLayout(new MigLayout("", "[grow][15%]", rows));
		panel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel.setBackground(_Settings.backgroundColor);

		checkboxes = new ArrayList<JCheckBox>();
		labels = new ArrayList<JLabel>();
		columns = new ArrayList<String>();
		for (int i = 0; i < options.length; i++) {
			JCheckBox chkbx = new JCheckBox(options[i]);
			chkbx.setFont(new Font("Century Gothic", Font.ITALIC, 21));
			chkbx.setBackground(_Settings.backgroundColor);
			chkbx.setForeground(_Settings.labelColor);
			chkbx.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					// TODO Auto-generated method stub
					JCheckBox source = (JCheckBox) arg0.getSource();
					for (int i = 0; i < checkboxes.size(); i++)
						if (checkboxes.get(i).equals(source))
							if (source.isSelected()) {
								columns.add(source.getText());
								labels.get(i).setText(Integer.toString(columns.size()));
								break;
							}
							else{
								columns.remove(source.getText());
								labels.get(i).setText("");
								for (int j = 0; j < columns.size(); j++)
									for (int k = 0; k < checkboxes.size(); k++)
										if (checkboxes.get(k).getText().equals(columns.get(j)))
											labels.get(k).setText(Integer.toString(j + 1));
								break;
							}
				}
			});
			panel.add(chkbx, "cell 0 " + i + ",grow");
			checkboxes.add(chkbx);
			JLabel lbl = new JLabel();
			lbl.setFont(new Font("Century Gothic", Font.ITALIC, 21));
			lbl.setHorizontalAlignment(SwingConstants.RIGHT);
			lbl.setBackground(_Settings.backgroundColor);
			lbl.setForeground(_Settings.labelColor);
			panel.add(lbl, "cell 1 " + i + ",grow");
			labels.add(lbl);
		}

		JButton btnConfirm = new JButton("CONFIRM");
		btnConfirm.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnConfirm.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnConfirm.setBackground(_Settings.backgroundColor);
		btnConfirm.setForeground(_Settings.labelColor);
		btnConfirm.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String title = txtTitle.getText();
				boolean oneColumnSelected = false;
				for (JCheckBox chkbx : checkboxes)
					if (chkbx.isSelected()) {
						oneColumnSelected = true;
						break;
					}
				if (!oneColumnSelected)
					new MyOptionPane("At least one column must be selected.", MyOptionPane.INFORMATION_MESSAGE);
				else if (title.isEmpty())
					new MyOptionPane("Title cannot be empty.", MyOptionPane.INFORMATION_MESSAGE);
				else if (title.length() > 30)
					new MyOptionPane("Title cannot be more than 30 characters.", MyOptionPane.INFORMATION_MESSAGE);
				else {
					setConfirmed(true);
					dispose();
				}
			}
		});

		JButton btnClose = new JButton("CLOSE");
		btnClose.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnClose.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnClose.setBackground(_Settings.backgroundColor);
		btnClose.setForeground(_Settings.labelColor);
		btnClose.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				setConfirmed(false);
				dispose();
			}
		});
		contentPane.add(btnClose, "cell 2 7 2 1,grow");
		contentPane.add(btnConfirm, "cell 5 7,grow");

		getRootPane().setDefaultButton(btnConfirm);
		getRootPane().registerKeyboardAction(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				btnClose.doClick();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		pack();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}

}
