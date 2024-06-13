package cores;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import builders.MyComboBox;
import builders.MyTextField;

public class Form {
	private String directory;
	private String title;
	private List<JComponent> list;

	public Form(String directory, String title, List<JComponent> list) {
		super();
		this.directory = directory;
		this.title = title;
		this.list = list;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<JComponent> getList() {
		return list;
	}

	public void setList(List<JComponent> list) {
		this.list = list;
	}

	@Override
	public String toString() {
		String string = directory + "," + title + ",";
		for (JComponent c : list) {
			if (c instanceof MyTextField)
				string += "MyTextField[";
			else if (c instanceof MyComboBox)
				string += "MyComboBox[";
			string += "bounds[" + c.getBounds().x + ";" + c.getBounds().y + ";" + c.getBounds().width + ";"
					+ c.getBounds().height + "]font[" + c.getFont().getName() + ";" + c.getFont().getStyle() + ";"
					+ c.getFont().getSize() + "]color[" + c.getForeground().getRed() + ";"
					+ c.getForeground().getGreen() + ";" + c.getForeground().getBlue() + "]]&";
		}
		return string.substring(0, string.length() - 1);
	}

	public static Form parse(String[] record) {
		String directory = record[0];
		String title = record[1];
		List<JComponent> list = new ArrayList<JComponent>();
		if (record.length > 2) {
			String listString = record[2];
			String componentString[] = listString.split("&");
			for (String string : componentString) {
				String classType = string.substring(0, string.indexOf("["));
				string = string.substring(string.indexOf("[") + 1); // remove class type
				string = string.substring(0, string.length() - 1); // remove last square bracket
				String attributeString[] = string.split("]");

				// bounds
				Rectangle bounds;
				String boundString = attributeString[0];
				boundString = boundString.substring(boundString.indexOf("[") + 1);
				String boundValue[] = boundString.split(";");
				int x = Integer.parseInt(boundValue[0]);
				int y = Integer.parseInt(boundValue[1]);
				int width = Integer.parseInt(boundValue[2]);
				int height = Integer.parseInt(boundValue[3]);
				bounds = new Rectangle(x, y, width, height);

				// font
				Font font;
				String fontString = attributeString[1];
				fontString = fontString.substring(fontString.indexOf("[") + 1);
				String fontValue[] = fontString.split(";");
				String fontName = fontValue[0];
				int fontStyle = Integer.parseInt(fontValue[1]);
				int fontSize = Integer.parseInt(fontValue[2]);
				font = new Font(fontName, fontStyle, fontSize);

				// color
				Color color;
				String colorString = attributeString[2];
				colorString = colorString.substring(colorString.indexOf("[") + 1);
				String colorValue[] = colorString.split(";");
				int R = Integer.parseInt(colorValue[0]);
				int G = Integer.parseInt(colorValue[1]);
				int B = Integer.parseInt(colorValue[2]);
				color = new Color(R, G, B);

				if (classType.equals("MyTextField"))
					list.add(new MyTextField(bounds, font, color));
			}
		}
		return new Form(directory, title, list);
	}
}
