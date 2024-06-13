package builders;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.JComboBox;

public class MyComboBox extends JComboBox<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MyComboBox(Rectangle bounds, Font font, Color color) {
		setBounds(bounds);
		setFont(font);
		setForeground(color);
	}

	@Override
	public String toString() {
		return "Drop-down Menu [x:" + getBounds().x + " y:" + getBounds().y + " width:" + getBounds().width + " height:"
				+ getBounds().height + " font:" + getFont().getName() + " " + getFont().getSize() + "pt.]";
	}
}
