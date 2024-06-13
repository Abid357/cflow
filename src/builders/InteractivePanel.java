package builders;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class InteractivePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JTextField txtX, txtY, txtX2, txtY2, txtWidth, txtHeight;
	private Rectangle selection;
	private JList<JComponent> list;
	private JScrollPane scroller;

	public InteractivePanel() {
		selection = new Rectangle(0, 0, 0, 0);
		setOpaque(false);
		MyMouseListener listener = new MyMouseListener();
		addMouseListener(listener);
		addMouseMotionListener(listener);
	}

	/*
	 * Override this method to display graphics on JPanel. Do not override paint
	 * method!
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		for (int i = 0; i < list.getModel().getSize(); i++) {
			JComponent component = list.getModel().getElementAt(i);
			g.setColor(component.getForeground());
			Graphics2D g2d = (Graphics2D) g.create();
			int offsetY = scroller.getVerticalScrollBar().getValue();
			int offsetX = scroller.getHorizontalScrollBar().getValue();
			Rectangle bounds = component.getBounds();
			Rectangle drawing = new Rectangle(bounds.x - offsetX, bounds.y - offsetY, bounds.width, bounds.height);
			g2d.draw(drawing);
			g2d.dispose();
		}
		
		g.setColor(Color.BLACK);
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.draw(selection);
		g2d.dispose();

	}

	public Rectangle getSelection() {
		return selection;
	}

	public void bindList(JList<JComponent> list) {
		this.list = list;
	}

	public void bindScrollPane(JScrollPane scroller) {
		this.scroller = scroller;
		scroller.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				// TODO Auto-generated method stub
				selection.setBounds(0, 0, 0, 0);
				repaint();
			}
		});
		scroller.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {

			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				// TODO Auto-generated method stub
				selection.setBounds(0, 0, 0, 0);
				repaint();
			}
		});
		addMouseWheelListener(scroller.getMouseWheelListeners()[0]);
	}

	public void bindTextFields(JTextField x, JTextField y, JTextField x2, JTextField y2, JTextField width,
			JTextField height) {
		txtX = x;
		txtY = y;
		txtX2 = x2;
		txtY2 = y2;
		txtWidth = width;
		txtHeight = height;

		txtX.setText("0");
		txtY.setText("0");
		txtX2.setText("0");
		txtY2.setText("0");
		txtWidth.setText("0");
		txtHeight.setText("0");

		txtX.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {
				// TODO Auto-generated method stub
				try {
					int x = Integer.parseInt(txtX.getText());
					int x2 = Integer.parseInt(txtX2.getText());
					int minX = Math.min(x, x2);
					int width = Math.max(x, x2) - minX;
					txtWidth.setText(Integer.toString(width));
					selection.setBounds(x, selection.y, width, selection.height);
					repaint();
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
					txtX.setText("0");
				}
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				// TODO Auto-generated method stub

			}
		});

		txtY.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {
				// TODO Auto-generated method stub
				try {
					int y = Integer.parseInt(txtY.getText());
					int y2 = Integer.parseInt(txtY2.getText());
					int minY = Math.min(y, y2);
					int height = Math.max(y, y2) - minY;
					txtHeight.setText(Integer.toString(height));
					selection.setBounds(selection.x, y, selection.width, height);
					repaint();
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
					txtY.setText("0");
				}
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				// TODO Auto-generated method stub

			}
		});

		txtX2.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {
				// TODO Auto-generated method stub
				try {
					int x = Integer.parseInt(txtX.getText());
					int x2 = Integer.parseInt(txtX2.getText());
					int minX = Math.min(x, x2);
					int width = Math.max(x, x2) - minX;
					txtWidth.setText(Integer.toString(width));
					selection.setBounds(selection.x, selection.y, width, selection.height);
					repaint();
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				// TODO Auto-generated method stub

			}
		});

		txtY2.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {
				// TODO Auto-generated method stub
				try {
					int y = Integer.parseInt(txtY.getText());
					int y2 = Integer.parseInt(txtY2.getText());
					int minY = Math.min(y, y2);
					int height = Math.max(y, y2) - minY;
					txtHeight.setText(Integer.toString(height));
					selection.setBounds(selection.x, selection.y, selection.width, height);
					repaint();
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
					txtY2.setText("0");
				}
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	class MyMouseListener extends MouseAdapter {

		private Point clickPoint;

		@Override
		public void mousePressed(MouseEvent e) {
			clickPoint = e.getPoint();
			selection = new Rectangle(clickPoint.x, clickPoint.y, 0, 0);

			txtX.setText(Integer.toString(clickPoint.x));
			txtY.setText(Integer.toString(clickPoint.y));
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			Point dragPoint = e.getPoint();
			int x = Math.min(clickPoint.x, dragPoint.x);
			int y = Math.min(clickPoint.y, dragPoint.y);

			int width = Math.max(clickPoint.x, dragPoint.x) - x;
			int height = Math.max(clickPoint.y, dragPoint.y) - y;

			txtX2.setText(Integer.toString(dragPoint.x));
			txtY2.setText(Integer.toString(dragPoint.y));
			txtWidth.setText(Integer.toString(width));
			txtHeight.setText(Integer.toString(height));

			selection.setBounds(x, y, width, height);
			repaint();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}
	}
}
