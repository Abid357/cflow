package cards;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import globals._Settings;
import net.miginfocom.swing.MigLayout;

public class Default extends JPanel{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create the panel.
	 */
	public Default() {
		setBackground(_Settings.backgroundColor);
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setLayout(new MigLayout("", "[grow]", "[grow]"));
		
		JLabel label = new JLabel("Select a report for more options");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setBackground(_Settings.backgroundColor);
		label.setForeground(_Settings.labelColor);
		label.setFont(new Font("Century Gothic", Font.ITALIC, 17));
		label.setBorder(null);
		add(label, "cell 0 0,grow");
	}
}
