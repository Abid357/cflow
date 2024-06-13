package builders;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.JTextField;

public class MyTextField extends JTextField {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MyTextField(Rectangle bounds, Font font, Color color) {
		setBounds(bounds);
		setFont(font);
		setForeground(color);
	}

	@Override
	public String toString() {
		return "Text Field [x:" + getBounds().x + " y:" + getBounds().y + " width:" + getBounds().width + " height:"
				+ getBounds().height + " font:" + getFont().getName() + " " + getFont().getSize() + "pt.]";
	}
}
