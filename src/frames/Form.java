package frames;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import builders.Background;
import builders.FormPanel;
import builders.PrintActionListener;
import globals._Settings;
import net.miginfocom.swing.MigLayout;

public class Form extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private BufferedImage image;

	/**
	 * Create the frame.
	 */
	public Form(Background background, List<JComponent> list, String title) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		contentPane = new JPanel();
		setContentPane(contentPane);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setTitle(title);

		getContentPane()
				.setLayout(new MigLayout("", "[75%][10][150][grow][10]", "[grow][100][10][100][10][100][grow]"));

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		JScrollPane spBackground = new JScrollPane(background);
		spBackground.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		spBackground.setBounds(0, 0, (int) (0.5 * screenSize.getWidth()), (int) (0.9 * screenSize.getHeight()));

		FormPanel glass = new FormPanel(list);
		glass.bindScrollPane(spBackground);
		glass.setBounds(0, 0, spBackground.getViewportBorderBounds().width - 15,
				spBackground.getViewportBorderBounds().height - 15);

		JLayeredPane lp = new JLayeredPane();
		lp.add(spBackground, Integer.valueOf(1));
		lp.add(glass, Integer.valueOf(2));
		getContentPane().add(lp, "cell 0 0 1 7,grow");

		JButton btnPrint = new JButton("PRINT");
		btnPrint.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnPrint.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnPrint.setBackground(_Settings.backgroundColor);
		btnPrint.setForeground(_Settings.labelColor);
		btnPrint.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent event) {
				drawImage(background, glass, spBackground);
	            new Thread(new PrintActionListener(image)).start();         
	        }
	    });
		getContentPane().add(btnPrint, "cell 2 1,grow");

		JButton btnSave = new JButton("SAVE");
		btnSave.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnSave.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnSave.setBackground(_Settings.backgroundColor);
		btnSave.setForeground(_Settings.labelColor);
		btnSave.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				drawImage(background, glass, spBackground);
				String directory = System.getProperty("user.home") + "/Downloads/" + title;
				JFileChooser destination = new JFileChooser();
				destination.setSelectedFile(new File(directory));
				destination.setMultiSelectionEnabled(false);
				int choice = destination.showSaveDialog(null);
				if (choice == JFileChooser.APPROVE_OPTION) {
					directory = destination.getCurrentDirectory().toString();
					String fileName = destination.getSelectedFile().getName();
					File fout = new File(directory + "\\" + fileName + ".jpg");
					    Desktop d = Desktop.getDesktop();
					try {
						ImageIO.write(image, "png", fout);
					    d.open(fout);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		getContentPane().add(btnSave, "cell 2 3,grow");

		JButton btnCancel = new JButton("CANCEL");
		btnCancel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnCancel.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnCancel.setBackground(_Settings.backgroundColor);
		btnCancel.setForeground(_Settings.labelColor);
		btnCancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				dispose();
			}
		});
		getContentPane().add(btnCancel, "cell 2 5,grow");

		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		if (background.getImage() == null)
			dispose();
	}
	
	public void drawImage(Background background, FormPanel glass, JScrollPane scroller) {
		image = new BufferedImage(background.getPreferredSize().width, background.getPreferredSize().height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();
		background.print(g2d);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		List<JComponent> list = glass.getList();
		for (JComponent c : list) {
			if (c instanceof JTextField) {
				JTextField txt = (JTextField) c;
				g2d.setColor(txt.getForeground());
				g2d.setFont(txt.getFont());
				int y = txt.getBounds().y + txt.getFont().getSize() + scroller.getVerticalScrollBar().getValue();
				int x = txt.getBounds().x + scroller.getHorizontalScrollBar().getValue();
				g2d.drawString(txt.getText(), x, y);
			}
		}
	}
}
