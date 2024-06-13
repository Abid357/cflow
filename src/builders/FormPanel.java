package builders;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class FormPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<JComponent> list;
	private int oldOffsetY;
	private int oldOffsetX;

	/**
	 * Create the panel.
	 */
	public FormPanel(List<JComponent> list) {
		this.list = list;
		oldOffsetY = 0;
		oldOffsetX = 0;
		setOpaque(false);
		setLayout(null);
		for (int i = 0; i < list.size(); i++)
			add(list.get(i));
		repaint();
	}

	public List<JComponent> getList() {
		return list;
	}

	public void bindScrollPane(JScrollPane scroller) {
		scroller.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				// TODO Auto-generated method stub
				int offsetY = e.getValue();
				for (Component c : getComponents()) {
					Rectangle bounds = new Rectangle(c.getBounds());
					Rectangle drawing = new Rectangle(bounds.x, bounds.y - (offsetY - oldOffsetY), bounds.width,
							bounds.height);
					c.setBounds(drawing);
				}
				oldOffsetY = offsetY;
			}
		});
		scroller.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {

			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				// TODO Auto-generated method stub
				int offsetX = e.getValue();
				for (Component c : getComponents()) {
					Rectangle bounds = new Rectangle(c.getBounds());
					Rectangle drawing = new Rectangle(bounds.x - (offsetX - oldOffsetX), bounds.y, bounds.width,
							bounds.height);
					c.setBounds(drawing);
				}
				oldOffsetX = offsetX;
			}
		});
	}
}
